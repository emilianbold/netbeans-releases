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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;

/**
 * This test traverses the AXI model for a given schema and
 * checks the parent component at each level.
 *
 * @author Samaresh
 */
public class CheckParentTest extends AbstractTestCase {
    public static final String TEST_XSD         = "resources/po.xsd";
    
    public CheckParentTest(String testName) {
        super(testName, TEST_XSD, null);
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite(CheckParentTest.class);
        return suite;
    }
    
    public void testParentNotNull() {
        DeepModelVisitor visitor = new DeepModelVisitor();
        visitor.traverse(getAXIModel().getRoot());
    }
        
    private class DeepModelVisitor extends DeepAXITreeVisitor {
        private int counter = 0;

        public void traverse(AXIDocument document) {
            document.accept(this);
        }

        protected void visitChildren(AXIComponent component) {
            if(!(component instanceof AXIDocument))
                assert(component.getParent() != null);
            counter++;
            super.visitChildren(component);
        }
    }    
}
