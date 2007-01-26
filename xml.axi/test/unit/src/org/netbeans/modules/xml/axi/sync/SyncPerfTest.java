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

package org.netbeans.modules.xml.axi.sync;

import junit.framework.*;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaModel;

        
/**
 * The unit test covers various use cases of sync on Element
 * and ElementRef.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SyncPerfTest extends AbstractSyncTestCase {
                
    public static final String OTA_XSD  = "resources/OTA_TravelItinerary.xsd";
    public static final String HL7_XSD  = "resources/hl7/fields.xsd";
    
    /**
     * SyncElementTest
     */
    public SyncPerfTest(String testName) {
        super(testName, HL7_XSD, null);
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite(SyncPerfTest.class);
        return suite;
    }

    public void testHealthcareSchemaSyncPerformance() {
        doRun(getSchemaModel(), getAXIModel(), false);
        //doRun(getSchemaModel(), getAXIModel(), true);
    }
    
//    public void testOTASyncPerformance() throws Exception {
//        AXIModel hlModel = getModel(OTA_XSD);
//        doRun(hlModel.getSchemaModel(), hlModel, false);
//        doRun(hlModel.getSchemaModel(), hlModel, true);
//    }
    
    private void doRun(SchemaModel sModel, AXIModel aModel, boolean worstCase) {
        if(worstCase) {
            DeepAXITreeVisitor visitor = new DeepAXITreeVisitor();
            visitor.visit(aModel.getRoot());
        }
        int schemaChildCount = sModel.getSchema().getChildren().size();
        int axiChildCount = aModel.getRoot().getChildren().size();
        try {
            getSchemaModel().startTransaction();
            GlobalElement ge = getSchemaModel().getFactory().createGlobalElement();
            ge.setName("NewGlobalElement");
            getSchemaModel().getSchema().addElement(ge);
            getSchemaModel().endTransaction();
            long startTime = System.currentTimeMillis();
            getAXIModel().sync();
            long endTime = System.currentTimeMillis();
            print("Time taken to sync: " +
                    (endTime - startTime));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        assert(schemaChildCount+1 == sModel.getSchema().getChildren().size());
        assert(axiChildCount+1 == aModel.getRoot().getChildren().size());
    }
}
