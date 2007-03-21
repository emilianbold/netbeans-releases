package org.netbeans.modules.web.jsf.xdm.model;

import java.util.List;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.editor.JSFEditorUtilities;
import org.openide.loaders.DataObject;

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
//    
//    public void testSyncNotWellFormedElement() throws Exception {
//        
//        List<NavigationRule> navRules;
//        
//        JSFConfigModel model = Util.loadRegistryModel("faces-config-03.xml");
//        
//        navRules = model.getRootComponent().getNavigationRules();
//        
//        assertEquals("afaa", navRules.get(0).getFromViewId());
//        
//        Util.setDocumentContentTo(model, "faces-config-notwellformed.xml");
//        navRules = model.getRootComponent().getNavigationRules();
//        
//        assertEquals("afaa", navRules.get(0).getFromViewId());
//    }
    
    
}
