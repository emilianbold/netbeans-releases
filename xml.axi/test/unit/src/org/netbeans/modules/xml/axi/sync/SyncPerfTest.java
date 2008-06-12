/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        super(testName, OTA_XSD, null);
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new SyncPerfTest("testOTASyncPerformance"));
        //suite.addTest(new SyncPerfTest("testHealthcareSchemaSyncPerformance"));
        return suite;
    }

    public void testHealthcareSchemaSyncPerformance() throws Exception {
        AXIModel aModel = getModel(HL7_XSD);
        doRun(aModel.getSchemaModel(), aModel, false);
        doRun(aModel.getSchemaModel(), aModel, true);
    }
    
    public void testOTASyncPerformance() throws Exception {
        AXIModel aModel = getModel(OTA_XSD);
        doRun(aModel.getSchemaModel(), aModel, false);
        doRun(aModel.getSchemaModel(), aModel, true);
    }
    
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
