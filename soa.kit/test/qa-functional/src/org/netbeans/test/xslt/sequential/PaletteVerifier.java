/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.xslt.sequential;

import java.awt.Point;
import java.util.LinkedList;
import javax.swing.JSplitPane;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.AttachWindowAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JSplitPaneOperator;
import org.netbeans.test.xslt.lib.Helpers;
import org.netbeans.test.xslt.lib.PaletteOperator;
import org.netbeans.test.xslt.lib.XSLTEditorOperator;
import org.netbeans.test.xslt.lib.sequential.SequentialTest;
import org.netbeans.test.xslt.lib.sequential.TestSequence;

/**
 *
 * @author ca@netbeans.org
 */

public class PaletteVerifier  extends TestSequence {
    static final int NO_EVENT_TIMEOUT = 500;
    static final String FILE_NAME = "newXslFile1.xsl";
    
    private XSLTEditorOperator m_opEditor = null;;
    private Point m_point = new Point(50, 50);
    
    public void setupOnce() {
        final String PROJECT_NAME = "XSLTTestProject";
        final String J_SPLIT_PANE = "javax.swing.JSplitPane";
        
        MainWindowOperator.getDefault().maximize();
        
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        
        ProjectRootNode nodeProjectRoot = pto.getProjectRootNode(PROJECT_NAME);
        nodeProjectRoot.select();
        org.netbeans.jellytools.nodes.Node nodeXSL = new org.netbeans.jellytools.nodes.Node(nodeProjectRoot, "Transformation Files|" + FILE_NAME);
        
        new OpenAction().performPopup(nodeXSL);
        waitNoEvent();
        
        Helpers.closeTopComponentIfOpened("Navigator");
        Helpers.closeTopComponentIfOpened("Properties");
        PaletteOperator opPalette = new PaletteOperator();
        new AttachWindowAction("Projects", AttachWindowAction.BOTTOM).perform(opPalette);
        waitNoEvent();
        
        TopComponentOperator opTopComponent = new TopComponentOperator(FILE_NAME);
        
        JComponentOperator opComponent = Helpers.getComponentOperator(opTopComponent, J_SPLIT_PANE, 0);
        JSplitPaneOperator opTargetSplitter = new JSplitPaneOperator((JSplitPane) opComponent.getSource());
        opTargetSplitter.moveDivider(0.7);
        waitNoEvent();
        
        opComponent = Helpers.getComponentOperator(opTopComponent, J_SPLIT_PANE, 1);
        JSplitPaneOperator opSourceSplitter = new JSplitPaneOperator((JSplitPane) opComponent.getSource());
        opSourceSplitter.moveDivider(0.4);
        waitNoEvent();
        
        m_opEditor = new XSLTEditorOperator(FILE_NAME);
        
        m_testList = new LinkedList<SequentialTest>();
        
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.OPERATOR, "equal", "Equal", 2, 1259604289L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.OPERATOR, "greater_or_equal", "Greater or Equal", 2, 3562843263L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.OPERATOR, "greater_than", "Greater Than", 2, 274115667L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.OPERATOR, "not_equal", "Not Equal", 2, 1264736979L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.OPERATOR, "less_than", "Less Than", 2, 4180514297L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.OPERATOR, "less_or_equal", "Less or Equal", 2, 3803618736L));
        
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.OPERATOR, "addition", "Addition", 2, 2444918072L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.OPERATOR, "subtraction", "Subtraction", 2, 658454300L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.OPERATOR, "multiplication", "Multiplication", 2, 296426505L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.OPERATOR, "division", "Div", 2, 963691707L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.OPERATOR, "mod", "Mod", 2, 3302433631L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.OPERATOR, "negative", "Negative", 1, 2719633178L));
        
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.STRING, "string-literal", "String Literal", 0, 3785901851L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.STRING, "string", "String", 1, 987964481L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.STRING, "concat", "Concat", 3, 571814599L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.STRING, "starts-with", "Starts With", 2, 2967593260L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.STRING, "contains", "Contains", 2, 3178659884L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.STRING, "substring-before", "Substring Before", 2, 3249169143L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.STRING, "substring-after", "Substring After", 2, 2021827526L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.STRING, "substring", "Substring", 3, 1077252319L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.STRING, "normalize-space", "Normalize Space", 1, 350959450L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.STRING, "translate", "Translate", 3, 2350529303L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.STRING, "string-length", "String Length", 1, 2135201417L));
        
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.NUMBER, "number", "Number", 1, 3877202165L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.NUMBER, "sum", "Sum", 1, 3522810582L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.NUMBER, "floor", "Floor", 1, 105863058L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.NUMBER, "ceiling", "Ceiling", 1, 1518032065L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.NUMBER, "round", "Round", 1, 2152844442L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.NUMBER, "numeric-literal", "Numeric Literal", 0, 3180052806L));
        
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.BOOLEAN, "boolean", "Boolean", 1, 1723479037L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.BOOLEAN, "not", "Not", 1, 3486937687L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.BOOLEAN, "true", "True", 0, 35389258L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.BOOLEAN, "false", "False", 0, 2685725001L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.BOOLEAN, "lang", "Lang", 1, 2595635305L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.BOOLEAN, "or", "Or", 2, 1082682017L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.BOOLEAN, "and", "And", 2, 1514851678L));
        
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.NODES, "last", "Last", 0, 90976362L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.NODES, "position", "Position", 0, 1779060867L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.NODES, "count", "Count", 1, 387503695L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.NODES, "local-name", "Local Name", 1, 105668041L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.NODES, "namespace-uri", "Namespace URI", 1, 3424196234L));
        m_testList.add(new PaletteItemTestSequence(PaletteOperator.Groups.NODES, "name", "Name", 1, 190429898L));
    }
    
    public void finalCleanup() {
        if (m_opEditor != null) {
            new SaveAllAction().performAPI();
        }
    }
    
    public class PaletteItemTestSequence extends TestSequence {
        private PaletteOperator.Groups m_group;
        private String m_strItem;
        private String m_strMethoidName;
        private int m_inPorts;
        private long m_checkSum;
        
        private boolean m_itemCreated = false;
        
        public PaletteItemTestSequence(PaletteOperator.Groups group,
                String strItem,
                String strMethoidName,
                int inPorts,
                long checkSum) {
            m_group = group;
            m_strItem = strItem;
            m_strMethoidName = strMethoidName;
            m_inPorts = inPorts;
            m_checkSum = checkSum;
        }
        
        public void setupOnce() {
            m_testList = new LinkedList<SequentialTest>();
            m_testList.add(new DropItem());
            m_testList.add(new VerifyChangesInSource(m_checkSum));
            m_testList.add(new RemoveItem());
        }
        
        public void setup() {
            clearTestStatus();
        }
        
        public class DropItem extends SequentialTest {
            public void execute() {
                startTest();
                
                m_opEditor.dropPaletteItemOnCanvas(m_group, m_strItem, m_point);
                
                if (m_inPorts > 0) {
                    m_opEditor.bindSourceToMethoid("shipTo|name", m_strMethoidName, 0, 0);
                }
                
                if (m_inPorts > 1) {
                    m_opEditor.bindSourceToMethoid("shipTo|street", m_strMethoidName, 0, 1);
                }
                
                if (m_inPorts > 2) {
                    m_opEditor.bindSourceToMethoid("shipTo|city", m_strMethoidName, 0, 2);
                }
                
                m_opEditor.bindMethoidToTarget(m_strMethoidName, 0, "purchaseOrder2|shipTo|name");
            
                m_itemCreated = true;
                
                endTest();
            }
            
            public String getTestName() {
                return Helpers.getFullTestName("DropItem-[" + m_strItem+"]");
            }
            
        }
        
        public class VerifyChangesInSource extends SequentialTest {
            private long m_checkSum;
            
            public VerifyChangesInSource(long checkSum) {
                m_checkSum = checkSum;
            }
            
            public void execute() {
                startTest();
                
                m_opEditor.switchToSource();
                
                EditorOperator opNBEditor = new EditorOperator(FILE_NAME);
                String strText = opNBEditor.getText();
                Helpers.writeJemmyLog("{" + strText + "}");
                
                m_opEditor.switchToDesign();
                
                if ( !Helpers.isCRC32Equal(strText, m_checkSum)) {
                    fail("The source check sum doesn't match the golden value");
                }
            }
            
            public String getTestName() {
                return Helpers.getFullTestName("VerifyChangesInSource-[" + m_strItem+"]");
            }
        }
        
        public class RemoveItem extends SequentialTest {
            public void execute() {
                if (m_itemCreated) {
                    clearTestStatus();
                }
                
                startTest();
                
                m_opEditor.removeMethoid(m_strMethoidName, 0);
                
                endTest();
            }
            
            public String getTestName() {
                return Helpers.getFullTestName("RemoveItem-[" + m_strItem+"]");
            }
        }
    }
    
    private void waitNoEvent() {
        Helpers.waitNoEvent(NO_EVENT_TIMEOUT);
    }
    
}
