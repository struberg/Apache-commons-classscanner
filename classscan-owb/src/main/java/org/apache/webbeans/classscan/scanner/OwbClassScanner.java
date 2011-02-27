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

import javax.enterprise.classscan.ClassScanClient;
import javax.enterprise.classscan.ClassScanner;
import javax.enterprise.classscan.ScanJob;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassScanner implementation we originally programmed for Apache
 * OpenWebBeans.
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class OwbClassScanner extends ClassScanner {

    private Map<ClassLoader, ScanResult> scanResultMap = new ConcurrentHashMap<ClassLoader, ScanResult>();

    /**
     * This method initializes all {@link ClassScanClient}s.
     * It will get called lazily at the first time the scanning result
     * get requested.
     * @param loader the current ClassLoader to use
     * @return the ScanResult or <code>null</code> if not available.
     */
    private ScanResult initializeScannerClients(ClassLoader loader) {
        ServiceLoader<ClassScanClient> sl = ServiceLoader.load(ClassScanClient.class);
        Iterator<ClassScanClient> it = sl.iterator();
        if (!it.hasNext()) {
            return null;
        }

        do {
            ClassScanClient client =  it.next();
            client.invokeRegistration(this);
        } while (it.hasNext());

        return scanResultMap.get(loader);
    }


    @Override
    public synchronized void registerClient(String clientName, ScanJob scanJob) {
        ClassLoader loader = getClassLoader();
        ScanResult scanResult = scanResultMap.get(loader);
        if (scanResult == null) {
            scanResult = new ScanResult();
            scanResultMap.put(loader, scanResult);
        }

        scanResult.addScanJob(clientName, scanJob);
    }

    @Override
    public void deregisterClient(String clientName) {
        ClassLoader loader = getClassLoader();

        ScanResult scanResult = scanResultMap.get(loader);
        if (scanResult != null) {
            scanResult.getScanJobs().remove(clientName);
            if (scanResult.getScanJobs().isEmpty()) {
                // once the last ScanJob got deregistered, we can clear all the AnnotationDb
                scanResultMap.remove(loader);
            }
        }
    }

    @Override
    public Map<String, Set<String>> getAnnotationsIndex(String clientName) {
        return getAnnotationDb(clientName).getAnnotationIndex();
    }

    @Override
    public Map<String, Set<String>> getClassesIndex(String clientName) {
        return getAnnotationDb(clientName).getClassIndex();
    }

    /**
     * This method will lazily trigger the classpath scanning the first
     * time it creates the AnnotationDB.
     * @param clientName
     * @return the AnnotationDB after a successful scan
     */
    private AnnotationDB getAnnotationDb(String clientName) {
        ClassLoader loader = getClassLoader();

        ScanResult scanResult = scanResultMap.get(loader);
        if (scanResult == null) {
            scanResult = initializeScannerClients(loader);
            if (scanResult == null) {
                return null;
            }
        }
        AnnotationDB annotationDB = scanResult.getAnnotationDB();
        if (annotationDB == null) {
            annotationDB = new AnnotationDB();

            // if requested by a single client, then we perform the respective task
            boolean scanClassAnnotations = false;
            boolean scanMethodAnnotations = false;
            boolean scanFieldAnnotations = false;
            boolean scanParameterAnnotations = false;

            Set<Class<?>> classesToScan = new HashSet<Class<?>>();

            // create filters
            Set<String> urlPaths = new HashSet<String>();
            for (ScanJob scanJob : scanResult.getScanJobs().values()) {

                scanClassAnnotations |= scanJob.isScanClassAnnotations();
                scanMethodAnnotations |= scanJob.isScanMethodAnnotations();
                scanFieldAnnotations |= scanJob.isScanFieldAnnotations();
                scanParameterAnnotations |= scanJob.isScanParameterAnnotations();
                if (scanJob.getClassesToScan() != null) {
                    for (Class<?> classToScan : scanJob.getClassesToScan()) {
                        classesToScan.add(classToScan);
                    }
                }

                String[] markerFiles = scanJob.getMarkerFiles();
                if ( markerFiles != null && markerFiles.length > 0 ) {

                    for (String markerFile : markerFiles) {
                        String[] classPathUrls = findResourceBases(markerFile,  loader);
                        for (String classPathUrl :classPathUrls) {
                            urlPaths.add(classPathUrl);
                        }
                    }
                }

                //X TODO what about the other filters?
            }

            // and now we do the actual scanning
            try {
                annotationDB.setScanClassAnnotations(scanClassAnnotations);
                annotationDB.setScanMethodAnnotations(scanMethodAnnotations);
                annotationDB.setScanFieldAnnotations(scanFieldAnnotations);
                annotationDB.setScanParameterAnnotations(scanParameterAnnotations);

                if (!urlPaths.isEmpty()) {
                    annotationDB.scanArchives(urlPaths.toArray(new String[urlPaths.size()]));
                }

                if (!classesToScan.isEmpty()) {
                    annotationDB.scanClasses(classesToScan);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error in classpath scanning", e);
            }

            scanResult.setAnnotationDB(annotationDB);
        }
        return annotationDB;
    }

    /**
     * @return the ClassLoader to use.
     */
    protected ClassLoader getClassLoader()
    {
        ClassLoader loader =  Thread.currentThread().getContextClassLoader();

        if (loader == null)
        {
            loader = OwbClassScanner.class.getClassLoader();
        }

        return loader;
    }

    /**
     * Find the base paths of all available resources with the given
     * resourceName in the classpath.
     * The returned Strings will <i>NOT</i> contain the resourceName itself!
     *
     * @param resourceName the name of the resource, e.g. 'META-INF/beans.xml'
     * @param loader the ClassLoader which should be used
     * @return array of Strings with the URL path to the resources.
     */
    private String[] findResourceBases(String resourceName, ClassLoader loader)
    {
        ArrayList<String> list = new ArrayList<String>();
        try
        {
            Enumeration<URL> urls = loader.getResources(resourceName);

            while (urls.hasMoreElements())
            {
                URL url = urls.nextElement();
                String urlString = url.toString();

                int idx = urlString.lastIndexOf(resourceName);
                urlString = urlString.substring(0, idx);

                list.add(urlString);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return list.toArray(new String[list.size()]);

    }


    private class ScanResult {
        private AnnotationDB annotationDB;
        private Map<String, ScanJob> scanJobs = new HashMap<String, ScanJob>();

        public ScanResult() {
        }

        public void addScanJob(String clientName, ScanJob scanJob) {
            scanJobs.put(clientName, scanJob);
        }

        public AnnotationDB getAnnotationDB() {
            return annotationDB;
        }

        public void setAnnotationDB(AnnotationDB annotationDB) {
            this.annotationDB = annotationDB;
        }

        public Map<String, ScanJob> getScanJobs() {
            return scanJobs;
        }


    }

}
