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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.css.visual.api;

import org.netbeans.modules.css.visual.ui.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Style Builder main panel
 * 
 * @author Marek Fukala
 * @version 1.0
 */
public final class StyleBuilderPanel extends JPanel {

    private List<StyleEditor> styleEditorList = new ArrayList<StyleEditor>();

    public static StyleBuilderPanel createInstance() {
        return new StyleBuilderPanel();
    }
    
    /** Creates new form StyleBuilderPanel */
    private StyleBuilderPanel() {
        initComponents();
        initialize();
    }

    private void initialize(){
        styleEditorList.add(new FontStyleEditor());
        styleEditorList.add(new BackgroundStyleEditor());
        styleEditorList.add(new TextBlockStyleEditor());
        styleEditorList.add(new BorderStyleEditor());
        styleEditorList.add(new MarginStyleEditor());
        styleEditorList.add(new PositionStyleEditor());
        //styleEditorList.add(new ListStyleEditor());
        //styleEditorList.add(new OtherStyleEditor());
        for(StyleEditor styleEditor : styleEditorList) {
            JScrollPane spane = new JScrollPane(styleEditor);
            spane.setBorder(new EmptyBorder(1,1,1,1));
            jTabbedPane1.addTab(styleEditor.getDisplayName(), spane);
        }
        jTabbedPane1.setSelectedIndex(0);
    }

    public void setContent(CssRuleContext content){
        for(StyleEditor editor : styleEditorList) {
            editor.setContent(content);
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();

        setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
    
}
