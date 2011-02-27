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
package javax.enterprise.classscan;

/**
 * <p>This SPI is for all clients of the ClassScanner.
 * A class scanner client is any java library which needs some
 * classpath scanning to be performed.</p>
 *
 * <p>The {@link ClassScanner} will use the {@link java.util.ServiceLoader}
 * mechanism for registering all clients. This will be done at the time the
 * first library requests a scanning result from the ClassScanner. This will
 * lazily trigger the registration of all ClassScanClients and perform
 * the actuall classpath scanning accoring to the information gathered by
 * all registered ClassScanClients.</p>
 *
 * <p>If a program library doen't need any further information from the
 * ClassScanner, it should tell this by calling the
 * {@link ClassScanner#deregisterClient(String)} method.</p>
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public interface ClassScanClient {

    /**
     * This method will get called by the {@link ClassScanner} to tell the
     * ClassScanClient that it might register it's needs via
     * {@link ClassScanner#registerClient(String, ScanJob)}
     *
     * @param scanner
     */
    public void invokeRegistration(ClassScanner scanner);
}
