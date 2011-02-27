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
package javax.enterprise.classscan.spi.test;

import javax.enterprise.classscan.ClassScanner;
import javax.enterprise.classscan.ScanJob;

import java.util.*;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class MockClassScanner extends ClassScanner {

    @Override
    public void registerClient(String clientName, ScanJob job) {
        registeredClients.add(clientName);
    }

    @Override
    public void deregisterClient(String clientName) {

    }

    @Override
    public Map<String, Set<String>> getAnnotationsIndex(String clientName) {
        return null;
    }

    @Override
    public Map<String, Set<String>> getClassesIndex(String clientName) {
        return new HashMap<String, Set<String>>();
    }

    private List<String> registeredClients = new ArrayList<String>();

    public List<String> getRegisteredClients() {
        return registeredClients;
    }
}
