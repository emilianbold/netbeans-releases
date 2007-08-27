package org.netbeans.modules.web.jsf.xdm.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.util.Exceptions;

public class SyncUpdateTest extends NbTestCase {
    
    public SyncUpdateTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    // for collecting and testing events
    Hashtable <String, PropertyChangeEvent> events = new Hashtable<String, PropertyChangeEvent>();
    
    public void testSyncRuleElement() throws Exception {
        
        JSFConfigModel model = Util.loadRegistryModel("faces-config-03.xml");
        
        NavigationRule rule = model.getRootComponent().getNavigationRules().get(0);
        
        assertEquals("afaa", rule.getFromViewId());
        
        Util.setDocumentContentTo(model, "faces-config-04.xml");
        
        assertEquals("newafaa", rule.getFromViewId());
    }
    
    public boolean propertyChangeCalled = false;
    public int eventCounter = 0;
    public void testSyncNotWellFormedElement() throws Exception {
        propertyChangeCalled = false;
        List<NavigationRule> navRules;
        
        JSFConfigModel model = Util.loadRegistryModel("faces-config-05.xml");
        
        navRules = model.getRootComponent().getNavigationRules();
        
        assertEquals("index.jsp", navRules.get(0).getFromViewId());
        
        Document document;
        //An Excpetion should be thrown.
        try {
            Util.setDocumentContentTo(model,"faces-config-notwellformed.xml");
        } catch (IOException ioe ){
            assertEquals("java.io.IOException: Invalid token '<' found in document: Please use the text editor to resolve the issues...", ioe.toString());
            //            System.out.println(ioe);
        }
        
        //An Exception Should be thrown
        try {
            navRules = model.getRootComponent().getNavigationRules();
        } catch ( IllegalStateException ise ) {
            assertEquals("java.lang.IllegalStateException: The model is not initialized or is broken.", ise.toString());
        }
        
        
        model.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent arg0) {
                propertyChangeCalled = true;
            }
        });
        Util.setDocumentContentTo(model,"faces-config-wellformed.xml");
        
        assertTrue(propertyChangeCalled);
    }
    
    public void testSyncRemoveRule() throws Exception {
        
        propertyChangeCalled = false;
        
        /* Load a file that has a simple rule*/
        JSFConfigModel model = Util.loadRegistryModel("faces-config-03.xml");
        
         model.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                events.put(event.getPropertyName(), event);
                System.out.format("Event (RemoveRule):  property name: %s, old value: %s, new value %s%n", event.getPropertyName(), event.getOldValue(), event.getNewValue());
            }
        });       

        try {
            Util.setDocumentContentTo(model,"faces-config-empty.xml");
        } catch (IOException ioe ){
            ioe.printStackTrace();
            assertTrue(false);
        }
        
        assertNotNull(events.get(FacesConfig.NAVIGATION_RULE));
    }
    
    public void testSyncRemoveCase_99325() throws Exception {
        
        events.clear();
        
        /* Load a file that has a simple rule*/
        JSFConfigModel model = Util.loadRegistryModel("faces-config-99325.xml");
        
         model.addComponentListener(new ComponentListener () {

            public void valueChanged(ComponentEvent event) {
                System.out.println(event.toString());
                if (event.getSource() instanceof NavigationCase) {
                    NavigationCase ncase = (NavigationCase) event.getSource();
                    if (ncase.getToViewId().equals("RealPage5.jsp") 
                            && ncase.getFromOutcome() == null) {
                        events.put(NavigationCase.FROM_OUTCOME, new PropertyChangeEvent(ncase, NavigationCase.FROM_OUTCOME, null, null));
                    }
                }
            }

            public void childrenAdded(ComponentEvent event) {
            }

            public void childrenDeleted(ComponentEvent event) {
            }
         });
         
         model.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                System.out.format("Event (99325):  property name: %s, old value: %s, new value %s%n", event.getPropertyName(), event.getOldValue(), event.getNewValue());
                events.put(event.getPropertyName(), event);
            }
        });       

        try {
            Util.setDocumentContentTo(model,"faces-config-99325-middle.xml");
            Util.setDocumentContentTo(model,"faces-config-99325-middle2.xml");
            Util.setDocumentContentTo(model,"faces-config-99325-end.xml");
        } catch (IOException ioe ){
            ioe.printStackTrace();
            assertTrue(false);
        }
        
        assertNotNull(events.get(NavigationCase.FROM_OUTCOME));
        assertNotNull(events.get(NavigationRule.NAVIGATION_CASE));
        assertNotNull(events.get(FacesConfig.NAVIGATION_RULE));
    }
    
    public void testSynceRenamePageInMode_l100321() throws Exception {
        
        propertyChangeCalled = false;
        eventCounter = 0;
        
        /* Load a file that has a simple rule*/
        JSFConfigModel configModel = Util.loadRegistryModel("faces-config-100321.xml");
        
         configModel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent arg0) {
                propertyChangeCalled = true;
                eventCounter ++;
                System.out.format("eventa:  property name: %s, old value: %s, new value %s%n", arg0.getPropertyName(), arg0.getOldValue(), arg0.getNewValue());
            }
        });       
        String oldDisplayName = "OLDFILENAME.jsp";
        String newDisplayName = "NEWFILENAME.jsp";
        configModel.startTransaction();
        FacesConfig facesConfig = configModel.getRootComponent();
        List<NavigationRule> navRules = facesConfig.getNavigationRules();
        for( NavigationRule navRule : navRules ){
            if ( navRule.getFromViewId().equals(oldDisplayName) ){
                navRule.setFromViewId(newDisplayName);
            }
            List<NavigationCase> navCases = navRule.getNavigationCases();
            for( NavigationCase navCase : navCases ) {
                if ( navCase.getToViewId().equals(oldDisplayName) ) {
                    navCase.setToViewId(newDisplayName);
                }
            }
        }
        
        configModel.endTransaction();
        try {
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }        
        assertTrue(propertyChangeCalled);
        assertEquals(3, eventCounter);
    }
    
}
