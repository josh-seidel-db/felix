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
package org.apache.felix.ipojo.test.scenarios.dependency;

import java.util.Properties;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.junit4osgi.OSGiTestCase;
import org.apache.felix.ipojo.test.scenarios.service.CheckService;
import org.apache.felix.ipojo.test.scenarios.util.Utils;
import org.osgi.framework.ServiceReference;

public class OptionalMultipleFilterDependencies extends OSGiTestCase {
	
	ComponentInstance instance1, instance2, instance3;
	ComponentInstance fooProvider1, fooProvider2;
	
	public void setUp() {
		try {
			Properties prov = new Properties();
			prov.put("name", "FooProvider1");
			fooProvider1 = Utils.getFactoryByName(context, "SimpleFilterCheckServiceProvider").createComponentInstance(prov);
			fooProvider1.stop();
			
			prov = new Properties();
            prov.put("name", "FooProvider2");
            fooProvider2 = Utils.getFactoryByName(context, "SimpleFilterCheckServiceProvider").createComponentInstance(prov);
            fooProvider2.stop();
		
			Properties i1 = new Properties();
			i1.put("name", "Subscriber1");
			instance1 = Utils.getFactoryByName(context, "OptionalMultipleFilterCheckServiceSubscriber").createComponentInstance(i1);
			
			Properties i2 = new Properties();
            i2.put("name", "Subscriber2");
            Properties ii2 = new Properties();
            ii2.put("id2", "(toto=A)");
            i2.put("requires.filters", ii2);
            instance2 = Utils.getFactoryByName(context, "OptionalMultipleFilterCheckServiceSubscriber2").createComponentInstance(i2);
            
            Properties i3 = new Properties();
            i3.put("name", "Subscriber3");
            Properties ii3 = new Properties();
            ii3.put("id1", "(toto=A)");
            i3.put("requires.filters", ii3);
            instance3 = Utils.getFactoryByName(context, "OptionalMultipleFilterCheckServiceSubscriber").createComponentInstance(i3);
		
		} catch(Exception e) { 
		    e.printStackTrace();
		    fail(e.getMessage()); }
		
	}
	
	public void tearDown() {
		instance1.dispose();
		instance2.dispose();
		instance3.dispose();
		fooProvider1.dispose();
		fooProvider2.dispose();
		instance1 = null;
		instance2 = null;
		instance3 = null;
		fooProvider1 = null;
		fooProvider2 = null;
	}
	
	public void testMultipleNotMatch() {
	    instance1.start();
	    
		ServiceReference arch_ref = Utils.getServiceReferenceByName(context, Architecture.class.getName(), instance1.getInstanceName());
		assertNotNull("Check architecture availability", arch_ref);
		InstanceDescription id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
		assertTrue("Check instance validity - 1", id.getState() == ComponentInstance.VALID);
		ServiceReference cs_instance_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance1.getInstanceName());
        assertNotNull("Check CheckService availability", cs_instance_ref);
        CheckService cs_instance = (CheckService) context.getService(cs_instance_ref);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 1", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 1", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
        
		fooProvider1.start();
		
		id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
		assertTrue("Check instance validity - 2", id.getState() == ComponentInstance.VALID);
		assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 2", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 2", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
		
		ServiceReference cs_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider1.getInstanceName());
		assertNotNull("Check CheckService availability", cs_ref);
		CheckService cs = (CheckService) context.getService(cs_ref);
		// change the value of the property toto
		cs.check();
		
		id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 3", id.getState() == ComponentInstance.VALID);
        cs_instance_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance1.getInstanceName());
        assertNotNull("Check CheckService availability", cs_instance_ref);
        cs_instance = (CheckService) context.getService(cs_instance_ref);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check Array size - 3", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        assertTrue("Check service Binding - 3", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        
        fooProvider2.start();
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 4", id.getState() == ComponentInstance.VALID);
        cs_instance_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance1.getInstanceName());
        assertNotNull("Check CheckService availability", cs_instance_ref);
        cs_instance = (CheckService) context.getService(cs_instance_ref);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check Array size - 4", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        assertTrue("Check service Binding - 4", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        
        ServiceReference cs_ref2 = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider2.getInstanceName());
        assertNotNull("Check CheckService availability", cs_ref2);
        CheckService cs2 = (CheckService) context.getService(cs_ref2);
        // change the value of the property toto
        cs2.check();
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check Array size - 5", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(2)));
        assertTrue("Check service Binding - 5", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(2)));
        
        // change the value of the property toto
        cs.check();
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 6", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service Binding - 6", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        assertTrue("Check Array size - 6", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        
		fooProvider2.stop();
		
		id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
		assertTrue("Check instance validity - 7", id.getState() == ComponentInstance.VALID);
		assertTrue("Check service Binding - 7", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
		assertTrue("Check Array size - 7", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
		
		fooProvider2.start();
		
		id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 8", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service Binding - 8", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        assertTrue("Check Array size - 8", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
		
		fooProvider2.stop();
		
		id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 9", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service Binding - 9", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 9", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
		
        id = null;
        cs = null;
        cs2 = null;
        cs_instance = null;
        context.ungetService(cs_instance_ref);
        context.ungetService(arch_ref);
        context.ungetService(cs_ref);
        context.ungetService(cs_ref2);		
	}
	
	public void testMultipleMatch() {
	    
	    fooProvider1.start();
	    fooProvider2.start();
	    
	    ServiceReference cs_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider1.getInstanceName());
        assertNotNull("Check CheckService availability", cs_ref);
        CheckService cs = (CheckService) context.getService(cs_ref);
        // change the value of the property toto
        cs.check();
        
        ServiceReference cs_ref2 = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider2.getInstanceName());
        assertNotNull("Check CheckService availability", cs_ref2);
        CheckService cs2 = (CheckService) context.getService(cs_ref2);
        // change the value of the property toto
        cs2.check();
	    
        instance1.start();
        
        ServiceReference arch_ref = Utils.getServiceReferenceByName(context, Architecture.class.getName(), instance1.getInstanceName());
        assertNotNull("Check architecture availability", arch_ref);
        InstanceDescription id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 1", id.getState() == ComponentInstance.VALID);
        
        ServiceReference cs_instance_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance1.getInstanceName());
        assertNotNull("Check CheckService availability", cs_instance_ref);
        CheckService cs_instance = (CheckService) context.getService(cs_instance_ref);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 1", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(2)));
        assertTrue("Check Array size - 1", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(2)));
        
        // change the value of the property toto
        cs.check();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 2", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 2", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        assertTrue("Check Array size - 2", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        
        fooProvider2.stop();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 3", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 3", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 3", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
        
        fooProvider2.start();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 4", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 4", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        assertTrue("Check Array size - 4", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        
        cs.check();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 5", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 5", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(2)));
        assertTrue("Check Array size - 5", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(2)));
        
        id = null;
        cs = null;
        cs2 = null;
        cs_instance = null;
        context.ungetService(cs_instance_ref);
        context.ungetService(arch_ref);
        context.ungetService(cs_ref);
        context.ungetService(cs_ref2);      
    }
	
	public void testMultipleNotMatchInstance() {
        instance3.start();
        
        ServiceReference arch_ref = Utils.getServiceReferenceByName(context, Architecture.class.getName(), instance3.getInstanceName());
        assertNotNull("Check architecture availability", arch_ref);
        InstanceDescription id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 1", id.getState() == ComponentInstance.VALID);
        ServiceReference cs_instance_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance3.getInstanceName());
        assertNotNull("Check CheckService availability", cs_instance_ref);
        CheckService cs_instance = (CheckService) context.getService(cs_instance_ref);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 1", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 1", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
        
        fooProvider1.start();
        ServiceReference cs_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider1.getInstanceName());
        assertNotNull("Check CheckService availability", cs_ref);
        CheckService cs = (CheckService) context.getService(cs_ref);
        // change the value of the property toto
        cs.check();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 2", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 2", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 2", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
        
        cs_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider1.getInstanceName());
        assertNotNull("Check CheckService availability", cs_ref);
        cs = (CheckService) context.getService(cs_ref);
        // change the value of the property toto
        cs.check();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 3", id.getState() == ComponentInstance.VALID);
        cs_instance_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance3.getInstanceName());
        assertNotNull("Check CheckService availability", cs_instance_ref);
        cs_instance = (CheckService) context.getService(cs_instance_ref);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check Array size - 3", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        assertTrue("Check service Binding - 3", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        
        fooProvider2.start();
        ServiceReference cs_ref2 = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider2.getInstanceName());
        assertNotNull("Check CheckService availability", cs_ref2);
        CheckService cs2 = (CheckService) context.getService(cs_ref2);
        // change the value of the property toto
        cs2.check();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 4", id.getState() == ComponentInstance.VALID);
        cs_instance_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance3.getInstanceName());
        assertNotNull("Check CheckService availability", cs_instance_ref);
        cs_instance = (CheckService) context.getService(cs_instance_ref);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check Array size - 4", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        assertTrue("Check service Binding - 4", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        
        // change the value of the property toto
        cs2.check();
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check Array size - 5", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(2)));
        assertTrue("Check service Binding - 5", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(2)));
        
        // change the value of the property toto
        cs.check();
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 6", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service Binding - 6", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        assertTrue("Check Array size - 6", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        
        fooProvider2.stop();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 7", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service Binding - 7", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 7", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
        
        fooProvider2.start();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 8", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service Binding - 8", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        assertTrue("Check Array size - 8", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        
        fooProvider2.stop();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 9", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service Binding - 9", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 9", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
        
        id = null;
        cs = null;
        cs2 = null;
        cs_instance = null;
        context.ungetService(cs_instance_ref);
        context.ungetService(arch_ref);
        context.ungetService(cs_ref);
        context.ungetService(cs_ref2);
    }
    
    public void testMultipleMatchInstance() {
        
        fooProvider1.start();
        fooProvider2.start();
        instance3.start();
        
        ServiceReference cs_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider1.getInstanceName());
        assertNotNull("Check CheckService availability", cs_ref);
        CheckService cs = (CheckService) context.getService(cs_ref);
        
        ServiceReference cs_ref2 = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider2.getInstanceName());
        assertNotNull("Check CheckService availability", cs_ref2);
        
        ServiceReference arch_ref = Utils.getServiceReferenceByName(context, Architecture.class.getName(), instance3.getInstanceName());
        assertNotNull("Check architecture availability", arch_ref);
        InstanceDescription id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 1", id.getState() == ComponentInstance.VALID);
        
        ServiceReference cs_instance_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance3.getInstanceName());
        assertNotNull("Check CheckService availability", cs_instance_ref);
        CheckService cs_instance = (CheckService) context.getService(cs_instance_ref);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 1", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(2)));
        assertTrue("Check Array size - 1", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(2)));
        
        // change the value of the property toto
        cs.check();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 2", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 2", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        assertTrue("Check Array size - 2", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        
        fooProvider2.stop();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 3", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 3", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 3", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
        
        fooProvider2.start();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 4", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 4", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        assertTrue("Check Array size - 4", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        
        cs.check();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 5", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 5", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(2)));
        assertTrue("Check Array size - 5", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(2)));
        
        id = null;
        cs = null;
        cs_instance = null;
        context.ungetService(cs_instance_ref);
        context.ungetService(arch_ref);
        context.ungetService(cs_ref);
        context.ungetService(cs_ref2);        
    }
	
    public void testMultipleNotMatchInstanceWithoutFilter() {
        instance2.start();
        
        ServiceReference arch_ref = Utils.getServiceReferenceByName(context, Architecture.class.getName(), instance2.getInstanceName());
        assertNotNull("Check architecture availability", arch_ref);
        InstanceDescription id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 1", id.getState() == ComponentInstance.VALID);
        ServiceReference cs_instance_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance2.getInstanceName());
        assertNotNull("Check CheckService availability", cs_instance_ref);
        CheckService cs_instance = (CheckService) context.getService(cs_instance_ref);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 1", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 1", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
        
        fooProvider1.start();
        ServiceReference cs_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider1.getInstanceName());
        assertNotNull("Check CheckService availability", cs_ref);
        CheckService cs = (CheckService) context.getService(cs_ref);
        // change the value of the property toto
        cs.check();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 2", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 2", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 2", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
        
        cs_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider1.getInstanceName());
        assertNotNull("Check CheckService availability", cs_ref);
        cs = (CheckService) context.getService(cs_ref);
        // change the value of the property toto
        cs.check();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 3", id.getState() == ComponentInstance.VALID);
        cs_instance_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance2.getInstanceName());
        assertNotNull("Check CheckService availability", cs_instance_ref);
        cs_instance = (CheckService) context.getService(cs_instance_ref);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check Array size - 3", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        assertTrue("Check service Binding - 3", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        
        fooProvider2.start();
        ServiceReference cs_ref2 = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider2.getInstanceName());
        assertNotNull("Check CheckService availability", cs_ref2);
        CheckService cs2 = (CheckService) context.getService(cs_ref2);
        // change the value of the property toto
        cs2.check();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 4", id.getState() == ComponentInstance.VALID);
        cs_instance_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance2.getInstanceName());
        assertNotNull("Check CheckService availability", cs_instance_ref);
        cs_instance = (CheckService) context.getService(cs_instance_ref);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check Array size - 4", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        assertTrue("Check service Binding - 4", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        
        // change the value of the property toto
        cs2.check();
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check Array size - 5", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(2)));
        assertTrue("Check service Binding - 5", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(2)));
        
        // change the value of the property toto
        cs.check();
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 6", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service Binding - 6", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        assertTrue("Check Array size - 6", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        
        fooProvider2.stop();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 7", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service Binding - 7", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 7", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
        
        fooProvider2.start();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 8", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service Binding - 8", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        assertTrue("Check Array size - 8", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        
        fooProvider2.stop();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 9", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service Binding - 9", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 9", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
        
        id = null;
        cs = null;
        cs2 = null;
        cs_instance = null;
        context.ungetService(cs_instance_ref);
        context.ungetService(arch_ref);
        context.ungetService(cs_ref);
        context.ungetService(cs_ref2);   
    }
    
    public void testMultipleMatchInstanceWithoutFilter() {

        
        fooProvider1.start();
        fooProvider2.start();
        instance2.start();
        
        ServiceReference cs_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider1.getInstanceName());
        assertNotNull("Check CheckService availability", cs_ref);
        CheckService cs = (CheckService) context.getService(cs_ref);
        
        ServiceReference cs_ref2 = Utils.getServiceReferenceByName(context, CheckService.class.getName(), fooProvider2.getInstanceName());
        assertNotNull("Check CheckService availability", cs_ref2);
        
        ServiceReference arch_ref = Utils.getServiceReferenceByName(context, Architecture.class.getName(), instance2.getInstanceName());
        assertNotNull("Check architecture availability", arch_ref);
        InstanceDescription id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 1", id.getState() == ComponentInstance.VALID);
        
        ServiceReference cs_instance_ref = Utils.getServiceReferenceByName(context, CheckService.class.getName(), instance2.getInstanceName());
        assertNotNull("Check CheckService availability", cs_instance_ref);
        CheckService cs_instance = (CheckService) context.getService(cs_instance_ref);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 1", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(2)));
        assertTrue("Check Array size - 1", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(2)));
        
        // change the value of the property toto
        cs.check();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 2", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 2", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        assertTrue("Check Array size - 2", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        
        fooProvider2.stop();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 3", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 3", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(0)));
        assertTrue("Check Array size - 3", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(0)));
        
        fooProvider2.start();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 4", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 4", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(1)));
        assertTrue("Check Array size - 4", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(1)));
        
        cs.check();
        
        id = ((Architecture) context.getService(arch_ref)).getInstanceDescription();
        assertTrue("Check instance validity - 5", id.getState() == ComponentInstance.VALID);
        assertTrue("Check service invocation", cs_instance.check());
        assertTrue("Check service Binding - 5", ((Integer)cs_instance.getProps().get("Bind")).equals(new Integer(2)));
        assertTrue("Check Array size - 5", ((Integer)cs_instance.getProps().get("Size")).equals(new Integer(2)));
        
        id = null;
        cs = null;
        cs_instance = null;
        context.ungetService(cs_instance_ref);
        context.ungetService(arch_ref);
        context.ungetService(cs_ref);
        context.ungetService(cs_ref2);      
    }

}