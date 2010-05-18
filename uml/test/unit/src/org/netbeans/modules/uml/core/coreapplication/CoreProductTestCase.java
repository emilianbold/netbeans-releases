/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */



package org.netbeans.modules.uml.core.coreapplication;

import junit.framework.TestCase;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.eventframework.EventDispatchController;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.roundtripframework.RTMode;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
/**
 *
 */
public class CoreProductTestCase extends TestCase
{
    private static final String wsLoc = "TestWS.etw";
    private static final String wsName = "TestWS";
    private TestCoreProductChangeListener m_ProdListener =
        new TestCoreProductChangeListener();
    private ICoreProductEventDispatcher m_Dispatcher = null;
    
    public static boolean callingPreInit = false;
    public static boolean callingInit = false;
    public static boolean callingPreQuit = false;
    
    public CoreProductTestCase()
    {
        super();
    }
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(CoreProductTestCase.class);
    }
    
    public void testCreateWorkspace()
    {
        CoreProduct prod = new CoreProduct();
        try
        {
            prod.createWorkspace(wsLoc,wsName);
            IWorkspace space = prod.openWorkspace(wsLoc);
            assertEquals(space.getName(),wsName);
            assertEquals(wsLoc, space.getLocation());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    //The initialize() method of CoreProduct sets all the attributes of
    //the class, so by calling that single method, we can assert all the
    //attributes of CoreProduct at once.
    public void testInitialize() throws Exception
    {
        CoreProduct prod = new CoreProduct();
        prod.createWorkspace(wsLoc,wsName);
        
        IApplication application = prod.initialize();
        
        //We have to initialize it again to test whether the init event is getting fired or not.
        //as the event is fired when the product is getting initialized and
        //inialization is required to register the events.
        prod.initialize();
        
        assertNotNull(application);
        
        IWorkspace space = prod.getCurrentWorkspace();
        assertNotNull(space);
        assertEquals(wsLoc, space.getLocation());
        
        assertNotNull(prod.getLanguageManager());
        
        assertNotNull(prod.getFacilityManager());
        assertNotNull(prod.getPreferenceManager());
        assertNotNull(prod.getNavigatorFactory());
        assertNotNull(prod.getTemplateManager());
        assertNotNull(prod.getConfigManager());
        assertNotNull(prod.getRoundTripController());
        
        // conover - default is OFF now that RT is being disabled
        assertEquals(prod.getRoundTripController().getMode(),RTMode.RTM_OFF);
        
        assertTrue(CoreProductTestCase.callingInit);
        assertTrue(CoreProductTestCase.callingPreInit);
    }
    
    //quit() method releases all the resources of CoreProduct
    public void testQuit()
    {
        CoreProduct prod = new CoreProduct();
        IApplication application = prod.initialize();
        prod.quit();
        assertNull(prod.getApplication());
        IWorkspace space = prod.getCurrentWorkspace();
        assertNull(space);
    }
    
    protected void setUp()
    {
        EventDispatchRetriever ret = EventDispatchRetriever.instance();
        IEventDispatchController cont = ret.getController();
        if (cont == null)
        {
            cont = new EventDispatchController();
        }
        m_Dispatcher =  (ICoreProductEventDispatcher)
        cont.retrieveDispatcher(EventDispatchNameKeeper.EDT_COREPRODUCT_KIND);
        if (m_Dispatcher == null)
        {
            m_Dispatcher =  new CoreProductEventDispatcher();
            cont.addDispatcher(EventDispatchNameKeeper.EDT_COREPRODUCT_KIND,
                m_Dispatcher);
        }
        m_Dispatcher.registerForInitEvents(m_ProdListener);
        ret.setController(cont);
    }
    
    protected void tearDown()
    {
        m_Dispatcher.revokeInitSink(m_ProdListener);
    }
    
}


