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
package org.apache.commons.classscan.spi.test;

import org.apache.commons.classscan.spi.ClassScanClient;
import org.apache.commons.classscan.spi.ClassScanner;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class MockClassScannerClient implements ClassScanClient {

    public static final String NAME = "org.apache.commons.classscan.test1";

    @Override
    public void invokeRegistration(ClassScanner scanner) {
        scanner.registerClient(NAME, null, null, null,
                               true, true, true, true);
    }
}
