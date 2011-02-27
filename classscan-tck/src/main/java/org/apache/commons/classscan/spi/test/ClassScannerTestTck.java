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

import javax.enterprise.classscan.ClassScanner;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class ClassScannerTestTck {

    @Test
    public void testClassScanner() throws Exception {
        ClassScanner cs = ClassScanner.getInstance();
        Assert.assertNotNull(cs);

        Map<String, Set<String>> classIndex = cs.getClassesIndex("org.apache.commons.classscan.test1");
        Assert.assertNotNull(classIndex);
        Assert.assertEquals(4, classIndex.size());

    }
}
