/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.test.xml.schema.sequential;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.ListModel;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.w3c.dom.Node;
import org.netbeans.test.xml.schema.lib.sequential.SequentialTest;
import org.netbeans.test.xml.schema.lib.sequential.TestSequence;
import org.netbeans.test.xml.schema.lib.dom.parser.NodeIterator;
import org.netbeans.test.xml.schema.lib.dom.parser.SchemaDOMBuilder;
import org.netbeans.test.xml.schema.lib.dom.parser.ExtraNodeInfo;
import org.netbeans.test.xml.schema.lib.types.ComponentCategories;
import org.netbeans.test.xml.schema.lib.util.Helpers;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;

import java.io.File;

/**
 *
 * @author ca@netbeans.org
 */
public abstract class PropertyVerifier extends TestSequence {
    
    static final String PROJECT_NAME = "XSDTestProject";
    
    static final int NO_EVENT_TIMEOUT = 500;
    
    private SchemaDOMBuilder m_builder = new SchemaDOMBuilder();
    
    private SchemaMultiView m_opMultiView = null;
    private Node m_domNode;
    
    private ColumnViewNode m_node = new ColumnViewNode(0, 0);
    
    private PropertySheetOperator m_opPropertySheet;
    
    protected abstract String getSchemaName();
    
    protected abstract int getFirstLine();
    
    private ExtraNodeInfo getExtraNodeInfo() {
        return ExtraNodeInfo.getExtraNodeInfo(m_domNode);
    }
    
    public void setupOnce() {
        
//        MainWindowOperator.getDefault().maximize();
        
        System.setProperty("jelly.wait.no.event", "false");

        //String strProjectsFolder = System.getProperty("xtest.data") + "/projects/" + PROJECT_NAME + "/src/qa/xmltools/samples";
        String strProjectsFolder = System.getProperty( "nbjunit.workdir" ) + File.separator + ".." + File.separator + "data" + File.separator + PROJECT_NAME + File.separator + "src" + File.separator + "qa" + File.separator + "xmltools" + File.separator + "samples";
//---
        m_builder.setFileName(strProjectsFolder + "/" + getSchemaName() + ".xsd");
        
        m_builder.setInitialLineNumber(getFirstLine());
        
        m_builder.build();

        m_builder.print( );
        
//---
        Helpers.closeTopComponentIfOpened("Navigator");
        
        new PropertiesAction().performMenu();
        m_opPropertySheet = new PropertySheetOperator();
        
        openSchema();
        
        m_opMultiView = new SchemaMultiView(getSchemaName());
        
        m_opMultiView.switchToSchema();
        
        m_opMultiView.switchToSchemaColumns();
        
        m_testList = new LinkedList<SequentialTest>();
        m_testList.add(new AttributesCategory());
        m_testList.add(new AttributeGroupsCategory());
        
        m_testList.add(new ComplexTypesCategory());
        m_testList.add(new ElementsCategory());
        
        m_testList.add(new GroupsCategory());
        m_testList.add(new SimpleTypesCategory());
    }
    
    public void finalCleanup() {
        if (m_opMultiView != null) {
            m_opMultiView.close();
        }
    }
    
    private void waitNoEvent() {
        Helpers.waitNoEvent(NO_EVENT_TIMEOUT);
    }
    
    public class ColumnViewTestSequence extends TestSequence {
        private ComponentCategories m_category;
        private JListOperator m_opList = null;
        
        protected ArrayList<ColumnViewNode> m_trace = new ArrayList<ColumnViewNode>();
        
        NodeIterator m_nodeIterator;
        
        public ColumnViewTestSequence(ComponentCategories category) {
            m_category = category;
        }
        
        private String getFullTestName(String strMethodName) {
            ExtraNodeInfo nodeInfo = getExtraNodeInfo();
            String str = strMethodName + "-component[" + nodeInfo.getColumnViewName() + "]";
            return Helpers.getFullTestName(str);
        }
        
        public void setupOnce() {
            m_testList = new LinkedList<SequentialTest>();
            m_testList.add(new ColumnViewNavigator());
            m_testList.add(new PropertiesTestSequence());
            
            m_node.m_col = 0;
            m_node.m_row = m_category.getValue();
            m_opList = m_opMultiView.getColumnListOperator(m_node.m_col);
            m_opList.selectItem(m_node.m_row);
            waitNoEvent();
            
            m_nodeIterator = m_builder.getNodeIterator(m_category);
        }
        
        public void setup() {

            boolean bRows = false;
            int col = m_node.m_col + 1;
            
            clearTestStatus();
            
            m_opList = m_opMultiView.getColumnListOperator(col);
            if (m_opList == null) {
                bRows = true;
            } else {
                ListModel model = m_opList.getModel();
                int row = -1;
                int modelSize = model.getSize();
                while(true) {
                    if (++row >= modelSize) {
                        bRows = true;
                        break;
                    }
                    
                    String strValue = model.getElementAt(row).toString();
                    if (col > 1 && strValue.indexOf("[Global") >= 0) {
                        continue;
                    }
                    
                    m_trace.add(new ColumnViewNode(m_node));
                    m_node.m_col = col;
                    m_node.m_row = row;
                    preExecute();
                    break;
                }
            }
            
            if (bRows) {
                while (true) {
                    m_opList = m_opMultiView.getColumnListOperator(m_node.m_col);
                    if ((m_node.m_row + 1) < m_opList.getModel().getSize()) {
                        m_node.m_row++;
                        preExecute();
                        break;
                    } else {
                        int index = m_trace.size() - 1;
                        if (index >= 0) {
                            m_node = m_trace.remove(index);
                            if (m_node.m_col == 0) {
                                setCompleted();
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        private void preExecute() {
            m_domNode = m_nodeIterator.next();
        }
        
        public void cleanup() {
            // Overrrides besic implementation to execute ColumnViewTestSequence in a cycle
        }
        
        public class ColumnViewNavigator extends SequentialTest {
            
            public void setupOnce() {
                clearCompleted();
            }
            
            public void execute() {
                startTest();
                
                m_opList.selectItem(m_node.m_row);
                Helpers.waitNoEvent();
                
                String strItem = m_opList.getSelectedValue().toString();
                strItem = strItem.substring(0, strItem.lastIndexOf("[")-1);
                
                ExtraNodeInfo sn = getExtraNodeInfo();
                
                if (!strItem.equals(sn.getColumnViewName())) {
                    fail("DOM name [" + sn.getColumnViewName() + "] isn't equal to [" + strItem + "] one displayed in column view.");
                }
                endTest();
            }
            
            protected String getTestName() {
                return getFullTestName("ColumnViewNavigator");
            }
        }
        
        public class PropertiesTestSequence extends TestSequence {
            private PropertySheetOperator m_pso;
            
            public void setupOnce() {
                clearCompleted();
                
                ExtraNodeInfo sn = getExtraNodeInfo();
                
                String name = sn.getComponentName(); //XMLElement.getUnqualifiledName(m_element.getName());
                String refName = sn.getNamedAttrValue("ref"); //m_element.getAttributeValue("ref");
                String strParent = sn.isGlobal() ? "" : sn.getParentColumnViewName(); // m_element.isGlobal() ? "" : m_element.getParentNameUnqualified();
                
                m_pso = new PropertySheetOperator();
                
                m_pso.setComparator(new StringComparator() {
                    public boolean equals(String caption, String match) {
                        return caption.equals(match);
                    }
                });
                
                m_testList = new LinkedList<SequentialTest>();
                
                if (!name.equals("documentation")) {
                    m_testList.add(new PropIDVerifier("ID" + sn.getLineNumber()));
                    m_testList.add(new ChangesInSourceVerifier("id", "ID" + sn.getLineNumber()));
                }
                
                if (name.equals("schema")) {
                    m_testList.add(new PropAttrFormDefaultVerifier());
                    m_testList.add(new PropElemFormDefaultVerifier());
                    
                } else if (name.equals("element") && refName == null) {
                    m_testList.add(new PropNameVerifier());
                    m_testList.add(new PropNillableVerifier());
                    m_testList.add(new PropFixedValueVerifier());
                    m_testList.add(new PropDefaultValueVerifier());
                    
                    m_testList.add(new ElementPropBlockVerifier());
                    
                    if (sn.isGlobal()) {
                        m_testList.add(new PropFinalVerifier());
                    } else {
                        m_testList.add(new PropFormVerifier());
                        
                        if (strParent.equals("all")) {
                            m_testList.add(new PropMinOccursAllVerifier());
                        } else {
                            m_testList.add(new PropMinOccursVerifier());
                            m_testList.add(new PropMaxOccursVerifier());
                        }
                    }
                } else if (name.equals("complexType")) {
                    m_testList.add(new PropMixedContentVerifier());
                    if (sn.isGlobal()) {
                        m_testList.add(new PropNameVerifier());
                        m_testList.add(new PropAbstractVerifier());
                        m_testList.add(new PropBlockVerifier());
                        m_testList.add(new PropFinalVerifier());
                    }
                } else if (name.equals("attribute")) {
                    if (refName == null) {
                        m_testList.add(new PropNameVerifier());
                    }
                    m_testList.add(new PropFixedValueVerifier());
                    m_testList.add(new PropDefaultValueVerifier());
                }
            }
            
            public void cleanup() {
                m_opMultiView.switchToSchema();
                setCompleted();
            }
            
            public class ChangesInSourceVerifier extends SequentialTest {
                private String m_strSrcChanges;
                private boolean m_verifyAbsence =  false;
                
                public ChangesInSourceVerifier(String strAttrName, String strAttrValue) {
                    m_strSrcChanges = strAttrName + "=\"" + strAttrValue + "\"";
                }
                
                public ChangesInSourceVerifier(String strAbsentString) {
                    m_verifyAbsence = true;
                    m_strSrcChanges = strAbsentString;
                }
                
                public void execute() {
                    startTest();
                    
                    int index = m_opList.getSelectedIndex();
                    Point p = m_opList.getClickPoint(index);
                    m_opList.clickForPopup(p.x, p.y);
                    new JPopupMenuOperator().pushMenu(getGoToSourceMenu());
                    waitNoEvent();
                    
                    EditorOperator opEditor = new EditorOperator(getSchemaName());
                    
                    int line = opEditor.getLineNumber();
                    ExtraNodeInfo nodeInfo = getExtraNodeInfo();
                    int expectedLineNumber = nodeInfo.getLineNmb();
                    
                    if (expectedLineNumber != line) {
                        fail("\"Go To Source\" jumped on the wrong line number " + line + " instead of " + expectedLineNumber);
                    }
                    
                    String strLine = opEditor.getText(line);
                    
                    String strPattern = "";
                    if (m_verifyAbsence) {
                        strPattern = ".* (" + m_strSrcChanges + "){0}.*\\s*";
                    } else {
                        strPattern = ".* (" + m_strSrcChanges + "){1}.*\\s*";
                    }
                    
                    if (!strLine.matches(strPattern)) {
                        fail("Invalid or missing attribute " + m_strSrcChanges + " in line " + strLine);
                    }
                    
                    endTest();
                }
                
                public String getTestName() {
                    return getFullTestName("ChangesInSourceVerifier-[" + m_strSrcChanges + (m_verifyAbsence ? "]-(absence)" : "]"));
                }
                
                public void cleanup() {
                    m_opMultiView.switchToSchema();
                    waitNoEvent();
                    
                    setCompleted();
                }
                
                private String getGoToSourceMenu() {
                    ExtraNodeInfo sn = getExtraNodeInfo();
                
                    String name = sn.getComponentName();
                    
                    if ( name.equals("attributeGroup") && sn.isGlobal() ||
                            name.equals("attribute")  && sn.isGlobal() ||
                            name.equals("annotation") ||
                            name.equals("complexType") && !sn.isGlobal() ||
                            name.equals("simpleType")  ||
                            name.equals("union")  ||
                            name.equals("unique")  ||
                            name.equals("selector")  ||
                            name.equals("field")  ||
                            name.equals("key")  ||
                            name.equals("keyref")  ||
                            name.equals("group") && sn.isGlobal()  ||
                            name.equals("enumeration")
                            ) {
                        return "Go To Source";
                    }  else {
                        return "Go To|Source";
                    }
                }
            }
            
            public class SingleValueSetter extends SequentialTest {
                private String m_strProperty;
                private String m_strValue;
                
                public SingleValueSetter(String strProperty, String strValue) {
                    m_strProperty = strProperty;
                    m_strValue = strValue;
                }
                
                public void execute() {
                    startTest();
                    
                    Property p = new Property(m_pso, m_strProperty);
                    p.setValue(m_strValue);
                    waitNoEvent();
                    
                    endTest();
                }
                
                public String getTestName() {
                    return getFullTestName("SingleValueSetter-[" + m_strProperty + "=\"" + m_strValue +"\"]");
                }
                
            }
            
            public class DefaultValueSetter extends SequentialTest {
                private String m_strProperty;
                
                public DefaultValueSetter(String strProperty) {
                    m_strProperty = strProperty;
                }
                
                public void execute() {
                    startTest();
                    
                    Property p = new Property(m_pso, m_strProperty);
                    p.setDefaultValue();
                    waitNoEvent();
                    
                    endTest();
                }
                
                public String getTestName() {
                    return getFullTestName("DefaultValueSetter-[" + m_strProperty+"]");
                }
            }
            
            public class PropIDVerifier extends SequentialTest {
                private String m_ID;
                
                public PropIDVerifier(String ID) {
                    m_ID = ID;
                }
                
                public void execute() {
                    clearTestStatus();
                    startTest();
                    
                    Property p = new Property(m_pso, "ID");
                    p.setValue(m_ID);
                    waitNoEvent();
                    
                    endTest();
                }
                
                public String getTestName() {
                    return getFullTestName("PropIDVerifier");
                }
            }
            
            public class PropAttrFormDefaultVerifier extends TestSequence {
                String ATTRIBUTE_FORM_DEFAULT       = "Attribute Form Default";
                String ATTR_ATTRIBUTE_FORM_DEFAULT  = "attributeFormDefault";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(ATTRIBUTE_FORM_DEFAULT,             "Unqualified (not set)"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_ATTRIBUTE_FORM_DEFAULT));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(ATTRIBUTE_FORM_DEFAULT,             "Qualified"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_ATTRIBUTE_FORM_DEFAULT,  "qualified"));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(ATTRIBUTE_FORM_DEFAULT,             "Unqualified"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_ATTRIBUTE_FORM_DEFAULT,  "unqualified"));
                }
            }
            
            public class PropElemFormDefaultVerifier extends TestSequence {
                String ELEMENT_FORM_DEFAULT       = "Element Form Default";
                String ATTR_ELEMENT_FORM_DEFAULT  = "elementFormDefault";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(ELEMENT_FORM_DEFAULT,             "Unqualified (not set)"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_ELEMENT_FORM_DEFAULT));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(ELEMENT_FORM_DEFAULT,             "Qualified"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_ELEMENT_FORM_DEFAULT,  "qualified"));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(ELEMENT_FORM_DEFAULT,             "Unqualified"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_ELEMENT_FORM_DEFAULT,  "unqualified"));
                }
            }
            
            public class PropNameVerifier extends TestSequence {
                private String m_strLastName = null;
                
                public void setupOnce() {
                    final String newName = "abcde";
                    
                    Property p = new Property(m_pso, "Name");
                    m_strLastName = p.getValue();
                    
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropNameGetter());
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter("Name", newName));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier("name",  newName));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter("Name", m_strLastName));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier("name",  m_strLastName));
                }
                
                public class PropNameGetter extends SequentialTest {
                    public void execute() {
                        startTest();
                        
                        if (m_strLastName == null) {
                            fail("Name property is not found in property sheet");
                        }
                        
                        endTest();
                    }
                    
                    public String getTestName() {
                        return getFullTestName("PropNameGetter");
                    }
                }
            }
            
            public class PropNillableVerifier extends TestSequence {
                String NILLABLE      = "Nillable";
                String ATTR_NILLABLE = "nillable";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(NILLABLE,            "True"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_NILLABLE, "true"));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(NILLABLE,            "False"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_NILLABLE, "false"));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(NILLABLE,             "False (Not set)"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_NILLABLE));
                }
            }
            
            public class PropFixedValueVerifier extends TestSequence {
                private String FIXED_VALUE = "Fixed Value";
                private String ATTR_FIXED  = "fixed";
                private String VALUE = "FV";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(FIXED_VALUE,      VALUE));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_FIXED, VALUE));
                    
                    m_testList.add(new PropertiesTestSequence.DefaultValueSetter(FIXED_VALUE));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_FIXED));
                }
            }
            
            public class PropDefaultValueVerifier extends TestSequence {
                private String DEFAULT_VALUE = "Default Value";
                private String ATTR_DEFAULT  = "default";
                private String VALUE = "DV";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(DEFAULT_VALUE,      VALUE));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_DEFAULT, VALUE));
                    
                    m_testList.add(new PropertiesTestSequence.DefaultValueSetter(DEFAULT_VALUE));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_DEFAULT));
                }
            }
            
            public class PropMixedContentVerifier extends TestSequence {
                String MIXED_CONTENT = "Mixed Content";
                String ATTR_MIXED = "mixed";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(MIXED_CONTENT, "True"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MIXED, "true"));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(MIXED_CONTENT, "False"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MIXED,  "false"));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(MIXED_CONTENT, "False (not set)"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MIXED));
                }
            }
            
            public class PropMinOccursVerifier extends TestSequence {
                String MIN_OCCURS = "Min Occurs";
                String ATTR_MIN_OCCURS = "minOccurs";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(MIN_OCCURS, "0"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MIN_OCCURS, "0"));
                    
                    m_testList.add(new PropertiesTestSequence.DefaultValueSetter(MIN_OCCURS));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MIN_OCCURS));
                }
            }
            
            public class PropMaxOccursVerifier extends TestSequence {
                String MAX_OCCURS = "Max Occurs";
                String ATTR_MAX_OCCURS = "maxOccurs";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(MAX_OCCURS, "10"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MAX_OCCURS, "10"));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(MAX_OCCURS, "unbounded"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MAX_OCCURS, "unbounded"));
                    
                    m_testList.add(new PropertiesTestSequence.DefaultValueSetter(MAX_OCCURS));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MAX_OCCURS));
                }
            }
            
            public class PropMinOccursAllVerifier extends TestSequence {
                String MIN_OCCURS = "Min Occurs";
                String ATTR_MIN_OCCURS = "minOccurs";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(MIN_OCCURS, "0"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MIN_OCCURS, "0"));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(MIN_OCCURS, "1"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MIN_OCCURS, "1"));
                    
                    m_testList.add(new PropertiesTestSequence.DefaultValueSetter(MIN_OCCURS));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MIN_OCCURS));
                }
            }
            
            public class PropMaxOccursAllVerifier extends TestSequence {
                String MAX_OCCURS = "Max Occurs";
                String ATTR_MAX_OCCURS = "maxOccurs";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(MAX_OCCURS, "0"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MAX_OCCURS, "0"));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(MAX_OCCURS, "1"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MAX_OCCURS, "1"));
                    
                    m_testList.add(new PropertiesTestSequence.DefaultValueSetter(MAX_OCCURS));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_MAX_OCCURS));
                }
            }
            
            public class PropAbstractVerifier extends TestSequence {
                private String ABSTRACT = "Abstract";
                private String ATTR_ABSTRACT = "abstract";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(ABSTRACT, "True"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_ABSTRACT, "true"));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(ABSTRACT, "False"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_ABSTRACT,  "false"));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(ABSTRACT, "False (not set)"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_ABSTRACT));
                }
            }
            
            public class PropFormVerifier extends TestSequence {
                private String FORM = "Form";
                private String ATTR_FORM = "form";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(FORM, "Qualified"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_FORM, "qualified"));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(FORM, "Unqualified"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_FORM,  "unqualified"));
                    
                    m_testList.add(new PropertiesTestSequence.SingleValueSetter(FORM, "Default for schema (not set)"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_FORM));
                }
            }
            
            public class DialogValueSetter extends SequentialTest {
                private String m_strProperty;
                private String m_strRadio;
                private String m_strCheck;
                
                private String m_strAttr;
                private String m_strValue;
                
                
                private String USE_SCHEMA_DEFAULT = "Use schema default";
                
                public DialogValueSetter(String strAttr, String strValue, String strProperty, String strRadio, String strCheck) {
                    m_strProperty = strProperty;
                    m_strRadio = strRadio;
                    m_strCheck = strCheck;
                    
                    m_strAttr = strAttr;
                    m_strValue = strValue;
                    
                }
                
                public void execute() {

                    startTest();
                    
                    Property p = new Property(m_pso, m_strProperty);
                    p.openEditor();
                    
                    JDialogOperator opEditor = new JDialogOperator();
                    waitNoEvent();
                    
                    new JRadioButtonOperator(opEditor, USE_SCHEMA_DEFAULT).push();
                    
                    new JRadioButtonOperator(opEditor, m_strRadio).push();
                    
                    if (m_strCheck != null) {
                        new JCheckBoxOperator(opEditor, m_strCheck).push();
                    }
                    
                    new JButtonOperator(opEditor, "OK").push();
                    opEditor.waitClosed();
                    
                    endTest();
                }
                
                public String getTestName() {
                    String strName = "DialogValueSetter-[" + m_strAttr + "=\"" + m_strValue + "\"]";
                    return getFullTestName(strName);
                }
            }
            
            public class PropBlockVerifier extends TestSequence {
                protected String ATTRIBUTE_PROHIBITED_SUBSTITUTIONS = "Prohibited Substitutions (Block) ";
                protected String ATTR_BLOCK  = "block";
                protected String strCheckBoxGroup = "Block substitutions of the following kinds:";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.DialogValueSetter(ATTR_BLOCK, "", ATTRIBUTE_PROHIBITED_SUBSTITUTIONS, "Allow all substitutions (empty value)", null));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_BLOCK, ""));
                    
                    m_testList.add(new PropertiesTestSequence.DialogValueSetter(ATTR_BLOCK, "#all", ATTRIBUTE_PROHIBITED_SUBSTITUTIONS, "Block all substitutions (#all)", null));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_BLOCK, "#all"));
                    
                    m_testList.add(new PropertiesTestSequence.DialogValueSetter(ATTR_BLOCK, "extension", ATTRIBUTE_PROHIBITED_SUBSTITUTIONS, strCheckBoxGroup, "Extension"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_BLOCK, "extension"));
                    
                    m_testList.add(new PropertiesTestSequence.DialogValueSetter(ATTR_BLOCK, "restriction", ATTRIBUTE_PROHIBITED_SUBSTITUTIONS, strCheckBoxGroup, "Restriction"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_BLOCK, "restriction"));
                    
                    m_testList.add(new PropertiesTestSequence.DialogValueSetter(ATTR_BLOCK, "", ATTRIBUTE_PROHIBITED_SUBSTITUTIONS, "Use schema default", null));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_BLOCK));
                }
            }
            
            public class ElementPropBlockVerifier extends PropBlockVerifier {
                
                public void setupOnce() {
                    super.setupOnce();
                    
                    m_testList.add(new PropertiesTestSequence.DialogValueSetter(ATTR_BLOCK, "substitution", ATTRIBUTE_PROHIBITED_SUBSTITUTIONS, strCheckBoxGroup, "Substitution"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_BLOCK, "substitution"));
                    
                    m_testList.add(new PropertiesTestSequence.DialogValueSetter(ATTR_BLOCK, "", ATTRIBUTE_PROHIBITED_SUBSTITUTIONS, "Use schema default", null));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_BLOCK));
                }
            }
            
            public class PropFinalVerifier extends TestSequence {
                protected String ATTRIBUTE_PROHIBITED_DERIVATIONS = "Prohibited Derivations (Final)";
                protected String ATTR_FINAL  = "final";
                protected String strCheckBoxGroup = "Prevent type derivations of the following kinds:";
                
                public void setupOnce() {
                    m_testList = new LinkedList<SequentialTest>();
                    
                    m_testList.add(new PropertiesTestSequence.DialogValueSetter(ATTR_FINAL, "", ATTRIBUTE_PROHIBITED_DERIVATIONS, "Allow all type derivations (empty value)", null));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_FINAL, ""));
                    
                    m_testList.add(new PropertiesTestSequence.DialogValueSetter(ATTR_FINAL, "#all", ATTRIBUTE_PROHIBITED_DERIVATIONS, "Prevent all type derivations (#all)", null));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_FINAL, "#all"));
                    
                    m_testList.add(new PropertiesTestSequence.DialogValueSetter(ATTR_FINAL, "extension", ATTRIBUTE_PROHIBITED_DERIVATIONS, strCheckBoxGroup, "Extension"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_FINAL, "extension"));
                    
                    m_testList.add(new PropertiesTestSequence.DialogValueSetter(ATTR_FINAL, "restriction", ATTRIBUTE_PROHIBITED_DERIVATIONS, strCheckBoxGroup, "Restriction"));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_FINAL, "restriction"));
                    
                    m_testList.add(new PropertiesTestSequence.DialogValueSetter(ATTR_FINAL, "", ATTRIBUTE_PROHIBITED_DERIVATIONS, "Use schema default", null));
                    m_testList.add(new PropertiesTestSequence.ChangesInSourceVerifier(ATTR_FINAL));
                }
            }
        }
    }
    
    public class AttributesCategory extends ColumnViewTestSequence {
        public AttributesCategory() {
            super(ComponentCategories.ATTRIBUTES);
        }
    }
    
    public class AttributeGroupsCategory extends ColumnViewTestSequence {
        public AttributeGroupsCategory() {
            super(ComponentCategories.ATTRIBUTE_GROUPS);
        }
    }
    
    public class ComplexTypesCategory extends ColumnViewTestSequence {
        public ComplexTypesCategory() {
            super(ComponentCategories.COMPLEX_TYPES);
        }
    }
    
    public class ElementsCategory extends ColumnViewTestSequence {
        public ElementsCategory() {
            super(ComponentCategories.ELEMENTS);
        }
    }
    
    public class GroupsCategory extends ColumnViewTestSequence {
        public GroupsCategory() {
            super(ComponentCategories.GROUPS);
        }
    }
    
    public class SimpleTypesCategory extends ColumnViewTestSequence {
        public SimpleTypesCategory() {
            super(ComponentCategories.SIMPLE_TYPES);
        }
    }
    
    public class ColumnViewNode {
        public int m_row;
        public int m_col;
        
        public ColumnViewNode(ColumnViewNode node) {
            m_row = node.m_row;
            m_col = node.m_col;
        }
        
        public ColumnViewNode(int row, int col) {
            m_row = row;
            m_col = col;
        }
    }
    
    private void openSchema() {
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        
        ProjectRootNode nodeProjectRoot = pto.getProjectRootNode(PROJECT_NAME);
        nodeProjectRoot.select();
        org.netbeans.jellytools.nodes.Node nodeXSD = new org.netbeans.jellytools.nodes.Node(nodeProjectRoot, "Source Packages|qa.xmltools.samples|" + getSchemaName() + ".xsd");
        
        new OpenAction().performPopup(nodeXSD);
        waitNoEvent();
    }

  public PropertyVerifier( String s )
  {
    super( s );
  }

  public PropertyVerifier( )
  {
    super( );
  }

}
