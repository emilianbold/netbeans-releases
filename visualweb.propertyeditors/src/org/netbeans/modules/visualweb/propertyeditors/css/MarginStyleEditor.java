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
package org.netbeans.modules.visualweb.propertyeditors.css;

import org.netbeans.modules.visualweb.propertyeditors.css.model.CssStyleData;
import java.awt.BorderLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

/**
 * Borders Style editor.
 * @author  Winston Prakash
 *          Jeff Hoffman (HIE design)
 */
public class MarginStyleEditor extends StyleEditor {
    
    /** Creates new form FontStyleEditor */
    public MarginStyleEditor(CssStyleData styleData) {
        setName("marginStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(StyleBuilderDialog.class, "MARGIN_EDITOR_DISPNAME"));
        initComponents();
        marginPanel.add(new MarginDataTable(styleData), BorderLayout.CENTER);
    }
    
    private void initComponents() {//GEN-BEGIN:initComponents
        marginPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout(0, 5));

        marginPanel.setLayout(new java.awt.BorderLayout());

        marginPanel.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)), new javax.swing.border.EtchedBorder()));
        add(marginPanel, java.awt.BorderLayout.NORTH);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel marginPanel;
    // End of variables declaration//GEN-END:variables
    
}
