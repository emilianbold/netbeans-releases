/*
 * MultiFileTest.java
 * JUnit based test
 *
 * Created on December 8, 2005, 12:08 PM
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.xam.locator.api.ModelSource;
import org.netbeans.modules.xml.xam.locator.api.ModelSourceImpl;
import org.netbeans.modules.xml.xam.locator.api.DepResolverException;
import org.netbeans.modules.xml.xam.locator.api.DependencyResolver;
import org.netbeans.modules.xml.xam.locator.api.ResolverStateEnum;
import org.netbeans.modules.xml.xam.locator.impl.catalog.DepResolverImpl;
import org.netbeans.modules.xml.schema.model.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Administrator
 */
public class MultiFileTest extends TestCase {
    
    private static String TEST_XSD = "resources/OrgChart.xsd";
    
    public MultiFileTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    public void testGetImportedModelSources() throws Exception {
        System.out.println("getImportedModelSources");
        
        // get the model for OrgChart.xsd
        URL orgChartUrl = getClass().getResource("../resources/OrgChart.xsd");
        File orgChartFile = new File(orgChartUrl.toURI());
        FileObject orgChartFileObj = FileUtil.toFileObject(orgChartFile);
        ModelSource localTestModelSource = new TestModelSourceImpl(orgChartFileObj,false);
        ModelSource testModelSource = new TestModelSource(localTestModelSource, TestResolver.getDefault());
        SchemaModel sm = SchemaModelFactory.getDefault().getModel(testModelSource);
        
        //register address.xsd with namespace (this is to be done only once
        URL addressUrl = getClass().getResource("../resources/address.xsd");
        sm.getModelSource().getResolver().addURI(new URI("http://www.altova.com/IPO"),addressUrl.toURI());
        
        // get imported model sources
        SchemaImpl schema = (SchemaImpl)sm.getSchema();
        Collection<ModelSource> importedModelSources = schema.getImportedModelSources();
        assertEquals(1,importedModelSources.size());
        
        ModelSource importedModelSource = importedModelSources.iterator().next();
        assertEquals("address.xsd",importedModelSource.getFileObject().getNameExt());
        
        // get imported model
        ModelSource testImportedModelSource = new TestModelSourceImpl(importedModelSource.getFileObject(),false);
        SchemaModel sm1 = SchemaModelFactory.getDefault().getModel(testImportedModelSource);
        assertNotNull(sm1);
        assertEquals("http://www.altova.com/IPO",sm1.getSchema().getTargetNamespace());
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    public void testGetIncludedModelSources() throws Exception {
        System.out.println("getIncludedModelSources");
        
        // get the model for OrgChart.xsd
        URL orgChartUrl = getClass().getResource("../resources/ipo.xsd");
        File orgChartFile = new File(orgChartUrl.toURI());
        FileObject orgChartFileObj = FileUtil.toFileObject(orgChartFile);
        ModelSource localTestModelSource = new TestModelSourceImpl(orgChartFileObj,false);
        ModelSource testModelSource = new TestModelSource(localTestModelSource, TestResolver.getDefault());
        SchemaModel sm = SchemaModelFactory.getDefault().getModel(testModelSource);
        
        //register address.xsd with relative location (this is to be done only once
        URL addressUrl = getClass().getResource("../resources/address.xsd");
        sm.getModelSource().getResolver().addURI(new URI("address.xsd"),addressUrl.toURI());
        
        // get included model sources
        SchemaImpl schema = (SchemaImpl)sm.getSchema();
        Collection<ModelSource> includedModelSources = schema.getIncludedModelSources();
        assertEquals(1,includedModelSources.size());
        
        ModelSource importedModelSource = includedModelSources.iterator().next();
        assertEquals("address.xsd",importedModelSource.getFileObject().getNameExt());
        
        // get included model
        ModelSource testImportedModelSource = new TestModelSourceImpl(importedModelSource.getFileObject(),false);
        SchemaModel sm1 = SchemaModelFactory.getDefault().getModel(testImportedModelSource);
        assertNotNull(sm1);
        assertEquals(schema.getTargetNamespace(),sm1.getSchema().getTargetNamespace());
    }
    
    private static class TestModelSourceImpl extends ModelSourceImpl {
        
        private DependencyResolver resolver = null;
        private Document document = null;
        
        public TestModelSourceImpl(FileObject modelSourceFileObject, boolean isReadOnly) {
            super(modelSourceFileObject,isReadOnly);
        }
        
        public Document getDocument() throws IOException {
            if (document == null) {
                try {
                    document = org.netbeans.modules.xml.schema.model.Util.loadDocument(
                            getFileObject().getInputStream());
                } catch (IOException ioe) {
                    throw ioe;
                } catch (Exception ex) {
                    throw new IOException(ex.getMessage());
                }
            }
            return document;
        }
    }
}
