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
    
    public void testSyncSimpleTypeElement() throws Exception {
        
        List<NavigationRule> navRules;
        
        JSFConfigModel model = Util.loadRegistryModel("faces-config-03.xml");
        Document doc = Util.getResourceAsDocument("faces-config-03.xml");
             
        if( ! (doc instanceof BaseDocument) ) {
            fail("Can not cast Document into BaseDocument");
        }
//        
//        String text = doc.getText(0, doc.getLength());
//        int offset = text.indexOf("from-view-id");                
//        String fromview = JSFEditorUtilities.getNavigationRule((BaseDocument) doc, offset);
        
        navRules = model.getRootComponent().getNavigationRules();
        
        assertEquals("afaa", navRules.get(0).getFromViewId());
        
        Util.setDocumentContentTo(model, "faces-config-04.xml");
        navRules = model.getRootComponent().getNavigationRules();
        
        assertEquals("newafaa", navRules.get(0).getFromViewId());
    }
    
    
}
