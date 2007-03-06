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
package org.netbeans.jellytools.modules.form.properties.editors;

import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.FormNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.junit.NbTestSuite;

/**
 * Common ancestor for all tests in package 
 * org.netbeans.jellytools.modules.form.properties.editors.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class FormPropertiesEditorsTestCase extends JellyTestCase {
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static NbTestSuite suite() {
        return null;
    }
    
    /** Clean up after each test case. */
    protected void tearDown() {
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public FormPropertiesEditorsTestCase(String testName) {
        super(testName);
    }

    
    /** Opens sample form, property sheet for Form node and custom editor for title property. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        if(fceo == null) {
            Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
            FormNode node = new FormNode(sample1, SAMPLE_FRAME_NAME);
            node.open();
            // wait for form opened
            new FormDesignerOperator(SAMPLE_FRAME_NAME);
            // open and close general properties
            new PropertiesAction().perform();
            new PropertySheetOperator().close();
            ComponentInspectorOperator inspector = new ComponentInspectorOperator();
            // "Form JFrameSample"
            node = new FormNode(inspector.treeComponents(), "[JFrame]"); // NOI18N
            // open Properties of [JFrame] under Form JFrameSample node
            node.properties();
            PropertySheetOperator pso = new PropertySheetOperator("[JFrame]"); // NOI18N
            Property p = new Property(pso, PROPERTY_NAME);
            p.openEditor();
            fceo = new FormCustomEditorOperator(PROPERTY_NAME);
        }
    }
    
    protected static final String SAMPLE_FRAME_NAME = "JFrameSample"; // NOI18N
    protected static final String PROPERTY_NAME = "title"; // NOI18N
    protected static FormCustomEditorOperator fceo;
}

