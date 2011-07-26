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
package org.apache.commons.classscan.api;

/**
 * Classpath scanning job from a single {@link ClassScanClient}.
 * This is basically a filter which restricts the amount of classes
 * which should be scanned to speedup the scanning.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class ScanJob {

    private String[] markerFiles;
    private String[] packageIncludes;
    private String[] packageExcludes;
    private boolean scanClassAnnotations;
    private boolean scanMethodAnnotations;
    private boolean scanFieldAnnotations;
    private boolean scanParameterAnnotations;
    private Class<?>[] classesToScan;

    /**
     * public ct
     * @param markerFiles optional array of marker file names, e.g. 'META-INF/beans.xml'. If this parameter is non-null
     *        then jars will only get scanned
     * @param packageIncludes
     * @param packageExcludes
     * @param scanClassAnnotations
     * @param scanMethodAnnotations
     * @param scanFieldAnnotations
     * @param scanParameterAnnotations
     */
    public ScanJob(String[] markerFiles,
                   String[] packageIncludes, String[] packageExcludes,
                   boolean scanClassAnnotations, boolean scanMethodAnnotations,
                   boolean scanFieldAnnotations, boolean scanParameterAnnotations) {
        this.markerFiles = markerFiles;
        this.packageIncludes = packageIncludes;
        this.packageExcludes = packageExcludes;
        this.scanClassAnnotations = scanClassAnnotations;
        this.scanMethodAnnotations = scanMethodAnnotations;
        this.scanFieldAnnotations = scanFieldAnnotations;
        this.scanParameterAnnotations = scanParameterAnnotations;
    }

    /**
     * If this method is used, ClassScanner will pares those classes
     * explicitly.
     * This is pretty handy for unit tests and other situations
     * where only single classes need to get scanned.
     * @param classesToScan
     */
    public void setClassesToScan(Class<?>[] classesToScan) {
        this.classesToScan = classesToScan;
    }

    public String[] getMarkerFiles() {
        return markerFiles;
    }

    public String[] getPackageIncludes() {
        return packageIncludes;
    }

    public String[] getPackageExcludes() {
        return packageExcludes;
    }

    public boolean isScanClassAnnotations() {
        return scanClassAnnotations;
    }

    public boolean isScanMethodAnnotations() {
        return scanMethodAnnotations;
    }

    public boolean isScanFieldAnnotations() {
        return scanFieldAnnotations;
    }

    public boolean isScanParameterAnnotations() {
        return scanParameterAnnotations;
    }

    public Class<?>[] getClassesToScan() {
        return classesToScan;
    }
}
