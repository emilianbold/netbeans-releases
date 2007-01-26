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

import junit.framework.*;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;
        
/**
 * The test measures the performance of healthcare schema by
 * visiting the model just 2 level deep.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelPerf2Test extends AbstractTestCase {
            
    public static final String TEST_XSD  = "resources/hl7/fields.xsd";
        
    /**
     * AXIModelPerf2Test
     */
    public AXIModelPerf2Test(String testName) {
        super(testName, TEST_XSD, null);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AXIModelPerf2Test.class);        
        return suite;
    }
    
    public void testHealthCareSchema() throws Exception {
        AXIModel model = getAXIModel();
        long startTime = System.currentTimeMillis();
        for(AXIComponent child : model.getRoot().getChildren()) {
            child.getChildren();
        }
        long endTime = System.currentTimeMillis();
        print("Time taken to traverse HL7 model upto 2 level: " + (endTime - startTime));
    }
    
}
