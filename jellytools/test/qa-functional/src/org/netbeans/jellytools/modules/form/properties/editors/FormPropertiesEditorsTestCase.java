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
package org.netbeans.jellytools.modules.form.properties.editors;

import java.io.IOException;
import junit.framework.TestSuite;
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
    protected void setUp() throws IOException {
        System.out.println("### "+getName()+" ###");
        openDataProjects("SampleProject");
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

