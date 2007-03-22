package org.netbeans.modules.web.jsf.xdm.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;

public class SyncUpdateTest extends NbTestCase {
    
    public SyncUpdateTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testSyncRuleElement() throws Exception {
        
        List<NavigationRule> navRules;
        
        JSFConfigModel model = Util.loadRegistryModel("faces-config-03.xml");
        
        navRules = model.getRootComponent().getNavigationRules();
        
        assertEquals("afaa", navRules.get(0).getFromViewId());
        
        Util.setDocumentContentTo(model, "faces-config-04.xml");
        navRules = model.getRootComponent().getNavigationRules();
        
        assertEquals("newafaa", navRules.get(0).getFromViewId());
    }
    
    public boolean propertyChangeCalled = false; 
    public void testSyncNotWellFormedElement() throws Exception {
        
        List<NavigationRule> navRules;
        
        JSFConfigModel model = Util.loadRegistryModel("faces-config-04.xml");
        
        navRules = model.getRootComponent().getNavigationRules();
        
        assertEquals("newafaa", navRules.get(0).getFromViewId());
        
        Document document;
        //An Excpetion should be thrown.
        try {
            Util.setDocumentContentTo(model,"faces-config-notwellformed.xml");
        } catch (IOException ioe ){
            assertEquals("java.io.IOException: Invalid token '</faces-config' found in document: Please use the text editor to resolve the issues...", ioe.toString());
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
        Util.setDocumentContentTo(model,"faces-config-05.xml");
        
        assertTrue(propertyChangeCalled);
    }

    
    
    
    
}
