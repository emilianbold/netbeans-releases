package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import java.io.File;
import junit.framework.*;
import org.netbeans.modules.websvc.wsitconf.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitconf.util.TestUtil;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class MtomTest extends TestCase {
    
    public MtomTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception { }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testMtom() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");
        WSDLComponentFactory fact = model.getFactory();
        
        model.startTransaction();

        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];
        
        assertFalse("MTOM enabled indicated on empty WSDL", TransportModelHelper.isMtomEnabled(b));
        TransportModelHelper.enableMtom(b);
        assertTrue("MTOM not enabled correctly", TransportModelHelper.isMtomEnabled(b));
        TransportModelHelper.disableMtom(b);
        assertFalse("MTOM enabled indicated", TransportModelHelper.isMtomEnabled(b));
      
        model.endTransaction();

        TestUtil.dumpToFile(model.getBaseDocument(), new File("C:\\MtomService.wsdl"));
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}
