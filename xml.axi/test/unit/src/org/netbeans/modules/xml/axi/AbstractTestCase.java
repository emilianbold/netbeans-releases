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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.axi;

import java.io.File;
import java.net.URL;
import junit.framework.*;
import org.netbeans.modules.xml.axi.impl.AXIModelImpl;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

        
/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractTestCase extends TestCase {

    //make it true if you want to see System.out.println messages.
    public static final boolean printUnitTestResults = false;    
    
    protected String schemaFileName;
    protected String globalElementName;    
    protected AXIModel axiModel;
    protected Element globalElement;
    protected URL referenceXML;
    protected boolean canCompareExpectedResultWithActual = true;
    
    
    /**
     * AbstractTestCase
     */
    public AbstractTestCase(String testName, 
            String schemaFileName, String globalElementName) {
        super(testName);
        this.schemaFileName = schemaFileName;
        this.globalElementName = globalElementName;
    }

    protected void setUp() throws Exception {
        loadModel(this.schemaFileName);
    }
	
    protected void loadModel(String schemaFileName) throws Exception {
        this.schemaFileName = schemaFileName;
        this.axiModel = getModel(schemaFileName);
        this.globalElement = findAXIGlobalElement(globalElementName);        
        String compareAgainst = schemaFileName.substring(0, schemaFileName.indexOf(".xsd")) + ".xml";
        referenceXML = AbstractTestCase.class.getResource(compareAgainst);
        if(referenceXML == null) {
            canCompareExpectedResultWithActual = false;
            return;
        }
    }

    protected AXIModel getModel(String schemaFileName) throws Exception {
        URL url = AbstractTestCase.class.getResource(schemaFileName);
        File file = new File(url.toURI());
        file = FileUtil.normalizeFile(file);
        return TestCatalogModel.getDefault().
                getAXIModel(FileUtil.toFileObject(file));                
    }
    
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
            
    protected AXIModel getAXIModel() {
        return axiModel;
    }
    
    protected SchemaModel getSchemaModel() {
        return getAXIModel().getSchemaModel();
    }
    
    protected Element findAXIGlobalElement(String name) {
        if(name == null)
            return null;
        
        for(Element e : axiModel.getRoot().getElements()) {
            if(e.getName().equals(name)) {
                return e;
            }
        }
        
        return null;
    }
    
    protected ContentModel findContentModel(String name) {
        for(ContentModel cm : axiModel.getRoot().getContentModels()) {
            if(cm.getName().equals(name)) {
                return cm;
            }
        }
        
        return null;
    }
    
    protected void validateSchema(SchemaModel sm) {
        boolean status = 
			((AXIModelImpl)getAXIModel()).getState()==Model.State.VALID;//((AXIModelImpl)getAXIModel()).validate();
        assertTrue("Schema Validation failed", status);
    }
    
    public final void print(String message) {
        if(printUnitTestResults) {        
            System.out.println(message);
        }
    }
}
