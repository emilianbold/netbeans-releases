/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsf.xdm.model;

import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigModelImpl;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.NodeList;

/**
 *
 * @author Petr Pisl
 */
public class ElementOrderingTest extends NbTestCase {

    public ElementOrderingTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    
    protected void setUp() throws Exception {
        Logger.getLogger(JSFConfigModelImpl.class.getName()).setLevel(Level.FINEST);
    }
    
    protected void tearDown() throws Exception {
    }
    
    
    
    public void testNavigationCase99906() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-99906.xml");
        FacesConfig facesConfig = model.getRootComponent();
        
        NavigationRule rule = facesConfig.getNavigationRules().get(0);
        
        model.startTransaction();
        rule.setFromViewId("frompage.jsp");
        model.endTransaction();
        model.sync();
        
        NodeList nodes = rule.getPeer().getChildNodes();
        assertEquals(nodes.item(1).getNodeName(), "from-view-id");
        assertEquals(nodes.item(5).getNodeName(), "navigation-case");
        Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);

    }
    
    /**
     * This test makes sure that from-outcome is always listed before to-view-id regardless of which was set first.
     * @throws java.lang.Exception 
     */
    public void test98691() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-empty.xml");
        FacesConfig facesConfig = model.getRootComponent();
        
        model.startTransaction();
        NavigationRule newRule = model.getFactory().createNavigationRule();
        newRule.setFromViewId("frompage.jsp");
        NavigationCase newCase = model.getFactory().createNavigationCase();
        
        //When order is switched.
        newCase.setToViewId("toPage.jsp");
        newCase.setFromOutcome("fromoutcome");
        
        newRule.addNavigationCase(newCase);
        facesConfig.addNavigationRule(newRule);
        model.endTransaction();
        model.sync();
        
        NodeList list = newCase.getPeer().getChildNodes();
        
        assertEquals(list.item(1).getNodeName(), "from-outcome");
        assertEquals(list.item(3).getNodeName(), "to-view-id");
        
        //One more test to make sure that even if the outcome is reset, it is still listed as first.
        model.startTransaction();
        newCase.setFromOutcome("fromoutcome2");
        model.endTransaction();
        model.sync();
        
        NodeList list2 = newCase.getPeer().getChildNodes();        
        assertEquals(list2.item(1).getNodeName(), "from-outcome");
        assertEquals(list2.item(3).getNodeName(), "to-view-id");
    }
}
