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

package org.netbeans.test.xml.schema.core.lib;
import javax.swing.JList;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.test.xml.schema.core.lib.util.Helpers;

/**
 *
 * @author ca@netbeans.org
 */
public class SchemaMultiView {
    
    private TopComponentOperator m_opTopComponent;
    
    private JToggleButtonOperator m_opSchemaButton;
    private JToggleButtonOperator m_opDesignButton;
    private JToggleButtonOperator m_opSourceButton;
    
    private JToggleButtonOperator m_opColumnsButton;
    private JToggleButtonOperator m_opTreeButton;
    
    private boolean m_bMacOS = true;
    
    /**
     * Creates a new instance of SchemaMultiView
     */
    public SchemaMultiView(String strTopComponentName) {
        m_opTopComponent = new TopComponentOperator(strTopComponentName);
        
        // On MacOS
        JComponentOperator opToolbar =  Helpers.getComponentOperator(m_opTopComponent, "org.netbeans.core.multiview.TabsComponent$TB", 0);
        
        // On Win/UNIX
        if (opToolbar == null) {
            opToolbar =  Helpers.getComponentOperator(m_opTopComponent, "javax.swing.JToolBar", 0);
            m_bMacOS = false;
        }
        
        JComponentOperator opSchemaViewToolbar = Helpers.getComponentOperator(m_opTopComponent, "javax.swing.JToolBar", m_bMacOS ? 0: 1);
        
        m_opSchemaButton = new JToggleButtonOperator(opToolbar, "Schema");
        m_opDesignButton = new JToggleButtonOperator(opToolbar, "Design");
        m_opSourceButton = new JToggleButtonOperator(opToolbar, "Source");
        
        switchToSchema();
        
        m_opColumnsButton = new JToggleButtonOperator(opSchemaViewToolbar, 0);
        m_opTreeButton = new JToggleButtonOperator(opSchemaViewToolbar, 1);
    }
    
    public TopComponentOperator getTopComponentOperator() {
        return m_opTopComponent;
    }
    
    public void switchToSchema() {
        m_opSchemaButton.push();
        Helpers.waitNoEvent();
    }
    
    public void switchToDesign() {
        m_opDesignButton.push();
        Helpers.waitNoEvent();
    }
    
    public void switchToSource() {
        m_opSourceButton.push();
        Helpers.waitNoEvent();
    }
    
    public void switchToSchemaColumns() {
        m_opColumnsButton.push();
        Helpers.waitNoEvent();
    }
    
    public void switchToSchemaTree() {
        m_opTreeButton.push();
        Helpers.waitNoEvent();
    }
    
    public JListOperator getColumnListOperator(int column) {
        JComponentOperator opComponent = Helpers.getComponentOperator(m_opTopComponent, "org.netbeans.modules.xml.xam.ui.column.ColumnListView$ColumnList", column, 200);
        if (opComponent == null) {
            return null;
        }
        JList list = (JList) opComponent.getSource();
        
        return new JListOperator(list);
    }
    
    public void close() {
        new SaveAllAction().performAPI();
        
        m_opTopComponent.close();
    }
}
