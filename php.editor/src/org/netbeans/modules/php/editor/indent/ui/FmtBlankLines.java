/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.editor.indent.ui;

import java.io.IOException;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import static org.netbeans.modules.php.editor.indent.FmtOptions.*;
import static org.netbeans.modules.php.editor.indent.FmtOptions.CategorySupport.OPTION_ID;
/**
 *
 * @author  phrebejk
 */
public class FmtBlankLines extends javax.swing.JPanel {
    
    /** Creates new form FmtBlankLines */
    public FmtBlankLines() {
        initComponents();

        bNamespaceField.putClientProperty(OPTION_ID, blankLinesBeforeNamespace);
        aNamespaceField.putClientProperty(OPTION_ID, blankLinesAfterNamespace);
        bUseField.putClientProperty(OPTION_ID, blankLinesBeforeUse);
        aUseField.putClientProperty(OPTION_ID, blankLinesAfterUse);
        bClassField.putClientProperty(OPTION_ID, blankLinesBeforeClass);
        aClassField.putClientProperty(OPTION_ID, blankLinesAfterClass);
        aClassHeaderField.putClientProperty(OPTION_ID, blankLinesAfterClassHeader);
        bFieldsField.putClientProperty(OPTION_ID, blankLinesBeforeField);
        aFieldsField.putClientProperty(OPTION_ID, blankLinesAfterField);
        bMethodsField.putClientProperty(OPTION_ID, blankLinesBeforeFunction );
        aMethodsField.putClientProperty(OPTION_ID, blankLinesAfterFunction);
        bFunctionEndField.putClientProperty(OPTION_ID, blankLinesBeforeFunctionEnd);
        bClassEndField.putClientProperty(OPTION_ID, blankLinesBeforeClassEnd);
	aOpenPHPTagField.putClientProperty(OPTION_ID, blankLinesAfterOpenPHPTag);
	aOpenPHPTagHTMLField.putClientProperty(OPTION_ID, blankLinesAfterOpenPHPTagInHTML);
	bClosePHPTagField.putClientProperty(OPTION_ID, blankLinesBeforeClosePHPTag);
        
        bNamespaceField.addKeyListener(new NumericKeyListener());
        aNamespaceField.addKeyListener(new NumericKeyListener());
        bUseField.addKeyListener(new NumericKeyListener());
        aUseField.addKeyListener(new NumericKeyListener());
        bClassField.addKeyListener(new NumericKeyListener());
        aClassField.addKeyListener(new NumericKeyListener());
        bClassEndField.addKeyListener(new NumericKeyListener());
        aClassHeaderField.addKeyListener(new NumericKeyListener());
        bFieldsField.addKeyListener(new NumericKeyListener());
        aFieldsField.addKeyListener(new NumericKeyListener());
        bMethodsField.addKeyListener(new NumericKeyListener());
        aMethodsField.addKeyListener(new NumericKeyListener());
        bFunctionEndField.addKeyListener(new NumericKeyListener());
	aOpenPHPTagField.addKeyListener(new NumericKeyListener());
	aOpenPHPTagHTMLField.addKeyListener(new NumericKeyListener());
	bClosePHPTagField.addKeyListener(new NumericKeyListener());
        
    }
    
    public static PreferencesCustomizer.Factory getController() {
        String preview = "";
        try {
            preview = Utils.loadPreviewText(FmtBlankLines.class.getClassLoader().getResourceAsStream("org/netbeans/modules/php/editor/indent/ui/BlankLines.php"));
        } catch (IOException ex) {
            // TODO log it
        }

        return new CategorySupport.Factory("blank-lines", FmtBlankLines.class, preview); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        aFieldsField = new javax.swing.JTextField();
        bFieldsField = new javax.swing.JTextField();
        aFieldsLabel = new javax.swing.JLabel();
        bClosePHPTagLabel = new javax.swing.JLabel();
        aClassHeaderField = new javax.swing.JTextField();
        bFieldsLabel = new javax.swing.JLabel();
        aClassField = new javax.swing.JTextField();
        aClassHeaderLabel = new javax.swing.JLabel();
        bClassField = new javax.swing.JTextField();
        aClassLabel = new javax.swing.JLabel();
        bMethodsField = new javax.swing.JTextField();
        bMethodsLabel = new javax.swing.JLabel();
        aMethodsLabel = new javax.swing.JLabel();
        aMethodsField = new javax.swing.JTextField();
        bFunctionEndLabel = new javax.swing.JLabel();
        bFunctionEndField = new javax.swing.JTextField();
        bClassEndLabel = new javax.swing.JLabel();
        bClassEndField = new javax.swing.JTextField();
        aOpenPHPTagLebel = new javax.swing.JLabel();
        aOpenPHPTagField = new javax.swing.JTextField();
        bNamespaceField = new javax.swing.JTextField();
        bNamespaceLabel = new javax.swing.JLabel();
        bClassLabel = new javax.swing.JLabel();
        aOpenPHPTagHTMLField = new javax.swing.JTextField();
        bUseField = new javax.swing.JTextField();
        bUseLabel = new javax.swing.JLabel();
        bClosePHPTagField = new javax.swing.JTextField();
        aNamespaceField = new javax.swing.JTextField();
        aOpenPHPTagHTMLLabel = new javax.swing.JLabel();
        aNamespaceLabel = new javax.swing.JLabel();
        aUseField = new javax.swing.JTextField();
        aUseLabel = new javax.swing.JLabel();

        setName(org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_BlankLines")); // NOI18N
        setOpaque(false);

        aFieldsField.setColumns(5);

        bFieldsField.setColumns(5);

        aFieldsLabel.setLabelFor(aFieldsField);
        org.openide.awt.Mnemonics.setLocalizedText(aFieldsLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterFields")); // NOI18N

        bClosePHPTagLabel.setLabelFor(bClosePHPTagField);
        org.openide.awt.Mnemonics.setLocalizedText(bClosePHPTagLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeClosePHPTag")); // NOI18N

        aClassHeaderField.setColumns(5);

        bFieldsLabel.setLabelFor(bFieldsField);
        org.openide.awt.Mnemonics.setLocalizedText(bFieldsLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeFields")); // NOI18N

        aClassField.setColumns(5);

        aClassHeaderLabel.setLabelFor(aClassHeaderField);
        org.openide.awt.Mnemonics.setLocalizedText(aClassHeaderLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterClassHeader")); // NOI18N

        bClassField.setColumns(5);

        aClassLabel.setLabelFor(aClassField);
        org.openide.awt.Mnemonics.setLocalizedText(aClassLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterClass")); // NOI18N

        bMethodsField.setColumns(5);

        bMethodsLabel.setLabelFor(bMethodsField);
        org.openide.awt.Mnemonics.setLocalizedText(bMethodsLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeMethods")); // NOI18N

        aMethodsLabel.setLabelFor(aMethodsField);
        org.openide.awt.Mnemonics.setLocalizedText(aMethodsLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterMethods")); // NOI18N

        aMethodsField.setColumns(5);

        bFunctionEndLabel.setLabelFor(bFunctionEndField);
        org.openide.awt.Mnemonics.setLocalizedText(bFunctionEndLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeMethodsEnd")); // NOI18N

        bFunctionEndField.setColumns(5);

        bClassEndLabel.setLabelFor(bClassEndField);
        org.openide.awt.Mnemonics.setLocalizedText(bClassEndLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeClassEnd")); // NOI18N

        bClassEndField.setColumns(5);

        aOpenPHPTagLebel.setLabelFor(aOpenPHPTagField);
        org.openide.awt.Mnemonics.setLocalizedText(aOpenPHPTagLebel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterPHPOpenTag")); // NOI18N

        aOpenPHPTagField.setText(org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aOpenPHPTagField.text")); // NOI18N

        bNamespaceField.setColumns(5);

        bNamespaceLabel.setLabelFor(bNamespaceField);
        org.openide.awt.Mnemonics.setLocalizedText(bNamespaceLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeNameSpace")); // NOI18N

        bClassLabel.setLabelFor(bClassField);
        org.openide.awt.Mnemonics.setLocalizedText(bClassLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeClass")); // NOI18N

        bUseField.setColumns(5);

        bUseLabel.setLabelFor(bUseField);
        org.openide.awt.Mnemonics.setLocalizedText(bUseLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeUse")); // NOI18N

        aNamespaceField.setColumns(5);

        aOpenPHPTagHTMLLabel.setLabelFor(aOpenPHPTagHTMLField);
        org.openide.awt.Mnemonics.setLocalizedText(aOpenPHPTagHTMLLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeOpenTagInHTML")); // NOI18N

        aNamespaceLabel.setLabelFor(aNamespaceField);
        org.openide.awt.Mnemonics.setLocalizedText(aNamespaceLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterNamespace")); // NOI18N

        aUseField.setColumns(5);

        aUseLabel.setLabelFor(aUseField);
        org.openide.awt.Mnemonics.setLocalizedText(aUseLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterImports")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bNamespaceLabel)
                    .addComponent(bUseLabel)
                    .addComponent(aNamespaceLabel)
                    .addComponent(aUseLabel)
                    .addComponent(bClassLabel)
                    .addComponent(aClassHeaderLabel)
                    .addComponent(bClassEndLabel)
                    .addComponent(aClassLabel)
                    .addComponent(bFieldsLabel)
                    .addComponent(aFieldsLabel)
                    .addComponent(bClosePHPTagLabel)
                    .addComponent(aOpenPHPTagHTMLLabel)
                    .addComponent(aOpenPHPTagLebel)
                    .addComponent(bFunctionEndLabel)
                    .addComponent(aMethodsLabel)
                    .addComponent(bMethodsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(aOpenPHPTagHTMLField, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                    .addComponent(aOpenPHPTagField, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                    .addComponent(bFunctionEndField, 0, 0, Short.MAX_VALUE)
                    .addComponent(bMethodsField, 0, 0, Short.MAX_VALUE)
                    .addComponent(aFieldsField, 0, 0, Short.MAX_VALUE)
                    .addComponent(bNamespaceField, 0, 0, Short.MAX_VALUE)
                    .addComponent(aClassField, 0, 0, Short.MAX_VALUE)
                    .addComponent(bClassEndField, 0, 0, Short.MAX_VALUE)
                    .addComponent(aClassHeaderField, 0, 0, Short.MAX_VALUE)
                    .addComponent(bClassField, 0, 0, Short.MAX_VALUE)
                    .addComponent(aUseField, 0, 0, Short.MAX_VALUE)
                    .addComponent(bUseField, 0, 0, Short.MAX_VALUE)
                    .addComponent(aNamespaceField, 0, 0, Short.MAX_VALUE)
                    .addComponent(bFieldsField, 0, 0, Short.MAX_VALUE)
                    .addComponent(aMethodsField, 0, 0, Short.MAX_VALUE)
                    .addComponent(bClosePHPTagField, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bNamespaceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bNamespaceLabel))
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aNamespaceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aNamespaceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bUseLabel)
                    .addComponent(bUseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aUseLabel)
                    .addComponent(aUseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bClassField, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bClassLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aClassHeaderField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aClassHeaderLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bClassEndField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bClassEndLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aClassField, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aClassLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bFieldsField, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bFieldsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aFieldsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aFieldsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bMethodsField, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bMethodsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aMethodsField, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aMethodsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bFunctionEndField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bFunctionEndLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aOpenPHPTagField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aOpenPHPTagLebel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aOpenPHPTagHTMLField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aOpenPHPTagHTMLLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bClosePHPTagField)
                    .addComponent(bClosePHPTagLabel))
                .addContainerGap())
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField aClassField;
    private javax.swing.JTextField aClassHeaderField;
    private javax.swing.JLabel aClassHeaderLabel;
    private javax.swing.JLabel aClassLabel;
    private javax.swing.JTextField aFieldsField;
    private javax.swing.JLabel aFieldsLabel;
    private javax.swing.JTextField aMethodsField;
    private javax.swing.JLabel aMethodsLabel;
    private javax.swing.JTextField aNamespaceField;
    private javax.swing.JLabel aNamespaceLabel;
    private javax.swing.JTextField aOpenPHPTagField;
    private javax.swing.JTextField aOpenPHPTagHTMLField;
    private javax.swing.JLabel aOpenPHPTagHTMLLabel;
    private javax.swing.JLabel aOpenPHPTagLebel;
    private javax.swing.JTextField aUseField;
    private javax.swing.JLabel aUseLabel;
    private javax.swing.JTextField bClassEndField;
    private javax.swing.JLabel bClassEndLabel;
    private javax.swing.JTextField bClassField;
    private javax.swing.JLabel bClassLabel;
    private javax.swing.JTextField bClosePHPTagField;
    private javax.swing.JLabel bClosePHPTagLabel;
    private javax.swing.JTextField bFieldsField;
    private javax.swing.JLabel bFieldsLabel;
    private javax.swing.JTextField bFunctionEndField;
    private javax.swing.JLabel bFunctionEndLabel;
    private javax.swing.JTextField bMethodsField;
    private javax.swing.JLabel bMethodsLabel;
    private javax.swing.JTextField bNamespaceField;
    private javax.swing.JLabel bNamespaceLabel;
    private javax.swing.JTextField bUseField;
    private javax.swing.JLabel bUseLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
}
