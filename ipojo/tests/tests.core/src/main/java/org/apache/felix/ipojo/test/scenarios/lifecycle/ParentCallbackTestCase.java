/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.ipojo.test.scenarios.lifecycle;

import java.util.Properties;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.junit4osgi.OSGiTestCase;
import org.apache.felix.ipojo.test.scenarios.service.CheckService;
import org.apache.felix.ipojo.test.scenarios.util.Utils;
import org.osgi.framework.ServiceReference;

public class ParentCallbackTestCase extends OSGiTestCase {
	
	ComponentInstance instance; // Instance under test
	ComponentInstance fooProvider;

	public void setUp() {
		Properties p2 = new Properties();
		p2.put("name", "fooProvider");
		fooProvider = Utils.getComponentInstance(context, "FooProviderType-1", p2);
		fooProvider.stop();
		
		Properties p1 = new Properties();
		p1.put("name", "callback");
		instance = Utils.getComponentInstance(context, "ParentCallbackCheckService", p1);
		
	}
	
	public void tearDown() {
		instance.dispose();
		fooProvider.dispose();
		instance= null;
		fooProvider = null;
	}
	
	public void testCallback() {
		// Check instance is invalid
		ServiceReference arch_ref = Utils.getServiceReferenceByName(context, Architecture.class.getName(), instance.getInstanceName());
		assertNotNull("Check architecture availability", arch_ref);
		InstanceDescription id_dep = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
		assertTrue("Check instance invalidity - 1", id_dep.getState() == ComponentInstance.INVALID);
		
		// Start fooprovider
		fooProvider.start();
		
		// Check instance validity
		id_dep = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
		assertTrue("Check instance validity - 1", id_dep.getState() == ComponentInstance.VALID);
		
		// Check service providing
		ServiceReference cs_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance.getInstanceName());
		assertNotNull("Check CheckService availability", cs_ref);
		CheckService cs = (CheckService) context.getService(cs_ref);
		
		// Check int property		
		assertEquals("Check pojo count - 2", id_dep.getCreatedObjects().length, 1);
		
		fooProvider.stop();
		
		id_dep = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
		assertTrue("Check instance invalidity - 2", id_dep.getState() == ComponentInstance.INVALID);
		
		fooProvider.start();
		
		// Check instance validity
		id_dep = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
		assertTrue("Check instance validity - 2", id_dep.getState() == ComponentInstance.VALID);
		
		// Check service providing
		cs_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance.getInstanceName());
		assertNotNull("Check CheckService availability", cs_ref);
		cs = (CheckService) context.getService(cs_ref);
		assertTrue("check CheckService invocation", cs.check());
		
		// Clean up
		context.ungetService(arch_ref);
		context.ungetService(cs_ref);
		cs = null;
		id_dep = null;
	}
		

}