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
package org.apache.commons.classscan.spi;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * <p>SPI of the ClassScanner itself.</p>
 *
 * <p>This abstract class will register it's implementation via the
 * {@link java.util.ServiceLoader} mechanism. There must be exactly one
 * implementation in the classpath which registers itself with a file
 * META-INF/services/org.apache.commons.classscan.spi.ClassScanner</p>
 *
 * <p>Each ContextClassLoader will get it's own results. But the
 * implementation is free to either perform the whole classpath
 * scanning for each ContextClassLoader or setup a parent chain.</p>
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public abstract class ClassScanner {

    public final static String[] DEFAULT_IGNORED_PACKAGES = {"javax", "java", "sun", "com.sun", "javassist"};

    /**
     * Access the registered ClassScanner implementation.
     * This is a 'highlander method' means, there must only be one of them ...
     * (of course 1 for each ContextClassLoader to be more precious).
     *
     * @return the ClassScanner singleton implementation for this ClassLoader
     */
    public final static ClassScanner getInstance() {
        ServiceLoader<ClassScanner> sl = ServiceLoader.load(ClassScanner.class);
        Iterator<ClassScanner> it = sl.iterator();
        if (!it.hasNext()) {
            throw new RuntimeException("No ClassScanner available!");
        }
        ClassScanner instance = it.next();
        if (it.hasNext()) {
            throw new RuntimeException("Ambiguous ClassScanner found!");
        }

        return instance;
    }




    /**
     * This method typically gets called from the {@link ClassScanClient#invokeRegistration(ClassScanner)}
     * method to tell the scanner which patterns must be scanned for
     *
     * @param clientName the {@link ClassScanClient} identifies itself via this name. e.g. 'org.apache.myfaces'
     *        or 'org.apache.openwebbeans'
     * @param markerFiles optional array of marker file names, e.g. 'META-INF/beans.xml'. If this parameter is non-null
     *        then jars will only get scanned
     * @param packageIncludes
     * @param packageExcludes
     * @param scanClassAnnotations
     * @param scanMethodAnnotations
     * @param scanFieldAnnotations
     * @param scanParameterAnnotations
     */
    public abstract void registerClient(String clientName, String[] markerFiles,
                                       String[] packageIncludes, String[] packageExcludes,
                                       boolean scanClassAnnotations, boolean scanMethodAnnotations,
                                       boolean scanFieldAnnotations, boolean scanParameterAnnotations);

    /**
     * This method should get called once a client doesn't need any information from the ClassScanner anymore.
     *
     * @param clientName the name as used in
     *        {@link #registerClient(String, String[], String[], String[], boolean, boolean, boolean, boolean)
     */
    public abstract void deregisterClient(String clientName);

    /**
     * @return a Map keyed by the fully qualified string name of a annotation class.  The Set returned is
     * a list of classes that use that annotation somehow.
     */
    public abstract Map<String, Set<String>> getAnnotationsIndex(String clientName);

    /**
     * @return a Map keyed by the list of classes scanned.  The value set returned is a list of annotations
     * used by that class.
     */
    public abstract Map<String, Set<String>> getClassesIndex(String clientName);

}
