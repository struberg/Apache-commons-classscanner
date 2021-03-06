/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.classscan.scanner;


import javassist.ClassPool;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import org.scannotation.archiveiterator.FileIterator;
import org.scannotation.archiveiterator.Filter;
import org.scannotation.archiveiterator.JarIterator;
import org.scannotation.archiveiterator.StreamIterator;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>The base of this class has been taken from
 * <a href="http://sourceforge.net/projects/scannotation/">scannotation</a>
 * which is not actively maintained anymore. Since it has Apache License v2.0,
 * we maintain a bug fixed and highly adopted version on our own.</p>
 *
 * <p>The class allows you to scan an arbitrary set of "archives" for .class files.
 * These class files are parsed to see what annotations they use.  Two indexes
 * are created. The javax, java, sun, com.sun, and javassist packages will
 * not be scanned by default. </p>
 *
 *
 * <p>One is a map of annotations and what classes use those annotations.
 * This could be used, for example, by an EJB deployer to find all the EJBs
 * contained in the archive. Another one is a map of classes and what
 * annotations those classes use.</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class AnnotationDB implements Serializable
{
    protected Map<String, Set<String>> annotationIndex = new HashMap<String, Set<String>>();
    protected Map<String, Set<String>> implementsIndex = new HashMap<String, Set<String>>();
    protected Map<String, Set<String>> classIndex = new HashMap<String, Set<String>>();

    protected boolean scanClassAnnotations = true;
    protected boolean scanMethodAnnotations = true;
    protected boolean scanParameterAnnotations = true;
    protected boolean scanFieldAnnotations = true;
    protected String[] ignoredPackages = {"javax", "java", "sun", "com.sun", "javassist"};

    public void scanClasses(Set<Class<?>> classesToScan) {
        try {
            for (Class<?> cls : classesToScan) {
                URL url = ClassPool.getDefault().find(cls.getName());
                scanClass(url.openStream());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public class CrossReferenceException extends Exception
    {
        private Set<String> unresolved;

        public CrossReferenceException(Set<String> unresolved)
        {
            this.unresolved = unresolved;
        }

        public Set<String> getUnresolved()
        {
            return unresolved;
        }
    }

    public String[] getIgnoredPackages()
    {
        return ignoredPackages;
    }

    /**
     * Override/overwrite any ignored packages
     *
     * @param ignoredPackages cannot be null
     */
    public void setIgnoredPackages(String[] ignoredPackages)
    {
        this.ignoredPackages = ignoredPackages;
    }

    public void addIgnoredPackages(String... ignored)
    {
        String[] tmp = new String[ignoredPackages.length + ignored.length];
        int i = 0;
        for (String ign : ignoredPackages)
        {
            tmp[i++] = ign;
        }
        for (String ign : ignored)
        {
            tmp[i++] = ign;
        }
    }

    /**
     * This method will cross reference annotations in the annotation index with any meta-annotations that they have
     * and create additional entries as needed.  For example:
     *
     * @HttpMethod("GET") public @interface GET {}
     * <p/>
     * The HttpMethod index will have additional classes added to it for any classes annotated with annotations that
     * have the HttpMethod meta-annotation.
     * <p/>
     * WARNING: If the annotation class has not already been scaned, this method will load all annotation classes indexed
     * as a resource so they must be in your classpath
     */
    public void crossReferenceMetaAnnotations() throws CrossReferenceException
    {
        Set<String> unresolved = new HashSet<String>();

        Set<String> index = new HashSet<String>();
        index.addAll(annotationIndex.keySet());

        for (String annotation : index)
        {
            if (ignoreScan(annotation))
            {
                continue;
            }
            if (classIndex.containsKey(annotation))
            {
                for (String xref : classIndex.get(annotation))
                {
                    annotationIndex.get(xref).addAll(annotationIndex.get(annotation));
                }
                continue;
            }
            InputStream bits = Thread.currentThread().getContextClassLoader().getResourceAsStream(annotation.replace('.', '/') + ".class");
            if (bits == null)
            {
                unresolved.add(annotation);
                continue;
            }
            try
            {
                scanClass(bits);
            }
            catch (IOException e)
            {
                unresolved.add(annotation);
            }
            for (String xref : classIndex.get(annotation))
            {
                annotationIndex.get(xref).addAll(annotationIndex.get(annotation));
            }

        }
        if (unresolved.size() > 0)
        {
            throw new CrossReferenceException(unresolved);
        }
    }


    private boolean ignoreScan(String intf)
    {
        for (String ignored : ignoredPackages)
        {
            if (intf.startsWith(ignored + "."))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * returns a map keyed by the fully qualified string name of a annotation class.  The Set returned is
     * a list of classes that use that annotation somehow.
     */
    public Map<String, Set<String>> getAnnotationIndex()
    {
        return annotationIndex;
    }

    /**
     * returns a map keyed by the list of classes scanned.  The value set returned is a list of annotations
     * used by that class.
     */
    public Map<String, Set<String>> getClassIndex()
    {
        return classIndex;
    }


    /**
     * Whether or not you want AnnotationDB to scan for class level annotations
     *
     * @param scanClassAnnotations
     */
    public void setScanClassAnnotations(boolean scanClassAnnotations)
    {
        this.scanClassAnnotations = scanClassAnnotations;
    }

    /**
     * Wheter or not you want AnnotationDB to scan for method level annotations
     *
     * @param scanMethodAnnotations
     */
    public void setScanMethodAnnotations(boolean scanMethodAnnotations)
    {
        this.scanMethodAnnotations = scanMethodAnnotations;
    }

    /**
     * Whether or not you want AnnotationDB to scan for parameter level annotations
     *
     * @param scanParameterAnnotations
     */
    public void setScanParameterAnnotations(boolean scanParameterAnnotations)
    {
        this.scanParameterAnnotations = scanParameterAnnotations;
    }

    /**
     * Whether or not you want AnnotationDB to scan for parameter level annotations
     *
     * @param scanFieldAnnotations
     */
    public void setScanFieldAnnotations(boolean scanFieldAnnotations)
    {
        this.scanFieldAnnotations = scanFieldAnnotations;
    }


    /**
     * Scan a url that represents an "archive"  this is a classpath directory or jar file
     *
     * @param urls variable list of URLs to scan as archives
     * @throws IOException
     */
    public void scanArchives(String... urls) throws IOException
    {
        for (String urlPath : urls)
        {
            scanUrlPath(urlPath);
        }

    }

    /**
     * Scan all classes from the given url
     * @param urlPath
     * @throws IOException
     */
    private void scanUrlPath(String urlPath) throws IOException
    {
        Filter filter = new Filter()
        {
            public boolean accepts(String filename)
            {
                if (filename.endsWith(".class"))
                {
                    if (filename.startsWith("/"))
                    {
                        filename = filename.substring(1);
                    }

                    if (!ignoreScan(filename.replace('/', '.')))
                    {
                        return true;
                    }
                }
                return false;
            }
        };

        StreamIterator it;
        String jarUrlPath = isJarUrl(urlPath);
        if (jarUrlPath != null)
        {
            it = new JarIterator((new URL(jarUrlPath)).openStream(), filter);
        }
        else
        {
            //X TODO check %20 for spaces ...
            File f = new File( (new URL(urlPath)).getFile() );
            it = new FileIterator(f, filter);
        }

        InputStream stream;
        while ((stream = it.next()) != null)
        {
            scanClass(stream);
        }
    }

    /**
     * check if the given url path is a Jar
     * @param urlPath
     * @return
     */
    private String isJarUrl(String urlPath)
    {
        if (urlPath.endsWith("!/") && urlPath.length() > 6)
        {
            urlPath = urlPath.substring(4, urlPath.length() - 2);
            return urlPath;
        }

        return null;
    }
    /**
     * Parse a .class file for annotations
     *
     * @param bits input stream pointing to .class file bits
     * @throws IOException
     */
    public void scanClass(InputStream bits) throws IOException
    {
        DataInputStream dstream = new DataInputStream(new BufferedInputStream(bits));
        ClassFile cf = null;
        try
        {
            cf = new ClassFile(dstream);
            classIndex.put(cf.getName(), new HashSet<String>());

            if (scanClassAnnotations)
            {
                scanClass(cf);
            }

            if (scanMethodAnnotations || scanParameterAnnotations)
            {
                scanMethods(cf);
            }

            if (scanFieldAnnotations)
            {
                scanFields(cf);
            }

            // create an index of interfaces the class implements
            if (cf.getInterfaces() != null)
            {
                Set<String> intfs = new HashSet<String>();
                for (String intf : cf.getInterfaces())
                {
                    intfs.add(intf);
                }

                implementsIndex.put(cf.getName(), intfs);
            }

        }
        finally
        {
            dstream.close();
            bits.close();
        }
    }

    protected void scanClass(ClassFile cf)
    {
        String className = cf.getName();
        AnnotationsAttribute visible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.visibleTag);
        AnnotationsAttribute invisible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.invisibleTag);

        if (visible != null)
        {
            populate(visible.getAnnotations(), className);
        }

        if (invisible != null)
        {
            populate(invisible.getAnnotations(), className);
        }
    }

    /**
     * Scanns both the method and its parameters for annotations.
     *
     * @param cf
     */
    protected void scanMethods(ClassFile cf)
    {
        List methods = cf.getMethods();
        if (methods == null)
        {
            return;
        }

        for (Object obj : methods)
        {
            MethodInfo method = (MethodInfo) obj;
            if (scanMethodAnnotations)
            {
                AnnotationsAttribute visible = (AnnotationsAttribute) method.getAttribute(AnnotationsAttribute.visibleTag);
                AnnotationsAttribute invisible = (AnnotationsAttribute) method.getAttribute(AnnotationsAttribute.invisibleTag);

                if (visible != null)
                {
                    populate(visible.getAnnotations(), cf.getName());
                }
                if (invisible != null)
                {
                    populate(invisible.getAnnotations(), cf.getName());
                }
            }
            if (scanParameterAnnotations)
            {
                ParameterAnnotationsAttribute paramsVisible = (ParameterAnnotationsAttribute) method.getAttribute(ParameterAnnotationsAttribute.visibleTag);
                ParameterAnnotationsAttribute paramsInvisible = (ParameterAnnotationsAttribute) method.getAttribute(ParameterAnnotationsAttribute.invisibleTag);

                if (paramsVisible != null && paramsVisible.getAnnotations() != null)
                {
                    for (Annotation[] anns : paramsVisible.getAnnotations())
                    {
                        populate(anns, cf.getName());
                    }
                }
                if (paramsInvisible != null && paramsInvisible.getAnnotations() != null)
                {
                    for (Annotation[] anns : paramsInvisible.getAnnotations())
                    {
                        populate(anns, cf.getName());
                    }
                }
            }
        }
    }

    protected void scanFields(ClassFile cf)
    {
        List fields = cf.getFields();
        if (fields == null)
        {
            return;
        }

        for (Object obj : fields)
        {
            FieldInfo field = (FieldInfo) obj;
            AnnotationsAttribute visible = (AnnotationsAttribute) field.getAttribute(AnnotationsAttribute.visibleTag);
            AnnotationsAttribute invisible = (AnnotationsAttribute) field.getAttribute(AnnotationsAttribute.invisibleTag);

            if (visible != null)
            {
                populate(visible.getAnnotations(), cf.getName());
            }
            if (invisible != null)
            {
                populate(invisible.getAnnotations(), cf.getName());
            }
        }


    }

    protected void populate(Annotation[] annotations, String className)
    {
        if (annotations == null)
        {
            return;
        }

        Set<String> classAnnotations = classIndex.get(className);
        for (Annotation ann : annotations)
        {
            Set<String> classes = annotationIndex.get(ann.getTypeName());
            if (classes == null)
            {
                classes = new HashSet<String>();
                annotationIndex.put(ann.getTypeName(), classes);
            }
            classes.add(className);
            classAnnotations.add(ann.getTypeName());
        }
    }

}
