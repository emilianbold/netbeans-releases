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

package org.netbeans.modules.web.jsf.xdm.model;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.DefaultLocale;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.api.facesmodel.Ordering;
import org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigModelImpl;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Petr Pisl, ads
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
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);

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
    
    public void testDefaultLocale() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-locale.xml");
        FacesConfig facesConfig = model.getRootComponent();
        
        model.startTransaction();
        Application application = facesConfig.getApplications().get(0);
        LocaleConfig config = application.getLocaleConfig().get(0);
        DefaultLocale locale = config.getDefaultLocale();
        assertNull( "test xml file doesn't containt default-locale element," +
        		" but its found there", locale );
        
        locale = model.getFactory().createDefatultLocale();
        config.setDefaultLocale(locale);
        
        model.endTransaction();
        model.sync();
        
        Element element  = Util.getElement( config.getPeer(), 0 );
        assertEquals( "Element locale-config should contain " +
                "default-locale as first child element, " +
                "but it contians :" +element.getNodeName(), element.getNodeName(), "default-locale");
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);
    }
    
    public void testEmptyLocale() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-locale.xml");
        FacesConfig facesConfig = model.getRootComponent();
        
        model.startTransaction();
        Application application = facesConfig.getApplications().get(0);
        LocaleConfig config = application.getLocaleConfig().get(0);
        clearConfig(config);
        
        SupportedLocale locale = model.getFactory().createSupportedLocale();
        config.addSupportedLocales(locale);
        DefaultLocale defaultLocale = model.getFactory().createDefatultLocale();
        config.setDefaultLocale( defaultLocale );
        locale = model.getFactory().createSupportedLocale();
        config.addSupportedLocales( locale );
        
        model.endTransaction();
        model.sync();
        
        Element element  = Util.getElement( config.getPeer(), 0 );
        assertEquals( "Element locale-config should contain " +
        		"default-locale as first child element, " +
        		"but it contians :" +element.getNodeName(), element.getNodeName(), "default-locale");
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);
    }
    
    public void testOrdering() throws Exception{
        JSFConfigModel model = Util.loadRegistryModel("faces-config-ordering.xml");
        FacesConfig facesConfig = model.getRootComponent();
        
        model.startTransaction();
        
        List<Ordering> orderings = facesConfig.getOrderings();
        assertEquals( 1, orderings.size());
        
        Ordering ordering = orderings.get(0);
        assertNull(ordering.getAfter());
        assertNull(ordering.getBefore());
        
        ordering.setBefore( model.getFactory().createBefore());
        ordering.setAfter( model.getFactory().createAfter());
        
        assertNotNull(ordering.getAfter());
        assertNotNull(ordering.getBefore());
        
        Element element = Util.getElement( ordering.getPeer(), 0);
        assertEquals( "after", element.getNodeName());
        
        element = Util.getElement( ordering.getPeer(), 1);
        assertEquals("before", element.getNodeName());
        
        model.endTransaction();
        model.sync();
        
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);
    }
    
    public void testNavigationCase() throws Exception{
        JSFConfigModel model = Util.loadRegistryModel("faces-config-navigation-case.xml");
        FacesConfig facesConfig = model.getRootComponent();
        
        model.startTransaction();
        
        List<NavigationRule> rules = facesConfig.getNavigationRules();
        assertEquals( 1 , rules.size());
        
        NavigationRule rule = rules.get(0);
        List<NavigationCase> cases = rule.getNavigationCases();
        assertEquals( 1 , cases.size());
        
        NavigationCase caze = cases.get(0);
        
        assertNotNull( caze.getRedirect());
        
        caze.setToViewId( "toViewId");
        caze.setIf( model.getFactory().createIf());
        caze.setFromAction("fromAction");
        caze.addDescription( model.getFactory().createDescription());
        
        model.endTransaction();
        model.sync();
        
        Element element = Util.getElement(caze.getPeer(), 0);
        assertEquals( "description",  element.getNodeName());
        
        element = Util.getElement(caze.getPeer(), 1);
        assertEquals( "icon",  element.getNodeName());
        
        element = Util.getElement(caze.getPeer(), 2);
        assertEquals( "from-action",  element.getNodeName());
        
        element = Util.getElement(caze.getPeer(), 3);
        assertEquals( "from-outcome",  element.getNodeName());
        
        element = Util.getElement(caze.getPeer(), 4);
        assertEquals( "if",  element.getNodeName());
        
        element = Util.getElement(caze.getPeer(), 5);
        assertEquals( "to-view-id",  element.getNodeName());
        
        element = Util.getElement(caze.getPeer(), 6);
        assertEquals( "redirect",  element.getNodeName());
        
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);
    }

    private void clearConfig( LocaleConfig config ) {
        for ( SupportedLocale loc : config.getSupportedLocales() ){
            config.removeSupportedLocale(loc);
        }
        if ( config.getDefaultLocale() != null ){
            config.setDefaultLocale( null );
        }
    }
}
