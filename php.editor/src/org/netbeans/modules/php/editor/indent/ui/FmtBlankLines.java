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

        bNamespaceLabel = new javax.swing.JLabel();
        bNamespaceField = new javax.swing.JTextField();
        aNamespaceLabel = new javax.swing.JLabel();
        aNamespaceField = new javax.swing.JTextField();
        bUseLabel = new javax.swing.JLabel();
        bUseField = new javax.swing.JTextField();
        aUseLabel = new javax.swing.JLabel();
        aUseField = new javax.swing.JTextField();
        bClassLabel = new javax.swing.JLabel();
        bClassField = new javax.swing.JTextField();
        aClassLabel = new javax.swing.JLabel();
        aClassField = new javax.swing.JTextField();
        aClassHeaderLabel = new javax.swing.JLabel();
        aClassHeaderField = new javax.swing.JTextField();
        bFieldsLabel = new javax.swing.JLabel();
        bFieldsField = new javax.swing.JTextField();
        aFieldsLabel = new javax.swing.JLabel();
        aFieldsField = new javax.swing.JTextField();
        bMethodsLabel = new javax.swing.JLabel();
        bMethodsField = new javax.swing.JTextField();
        aMethodsLabel = new javax.swing.JLabel();
        aMethodsField = new javax.swing.JTextField();
        bFunctionEndLabel = new javax.swing.JLabel();
        bFunctionEndField = new javax.swing.JTextField();
        bClassEndLabel = new javax.swing.JLabel();
        bClassEndField = new javax.swing.JTextField();

        setName(org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_BlankLines")); // NOI18N
        setOpaque(false);

        bNamespaceLabel.setLabelFor(bNamespaceField);
        org.openide.awt.Mnemonics.setLocalizedText(bNamespaceLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeNameSpace")); // NOI18N

        bNamespaceField.setColumns(5);

        aNamespaceLabel.setLabelFor(aNamespaceField);
        org.openide.awt.Mnemonics.setLocalizedText(aNamespaceLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterNamespace")); // NOI18N

        aNamespaceField.setColumns(5);

        bUseLabel.setLabelFor(bUseField);
        org.openide.awt.Mnemonics.setLocalizedText(bUseLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeUse")); // NOI18N

        bUseField.setColumns(5);

        aUseLabel.setLabelFor(aUseField);
        org.openide.awt.Mnemonics.setLocalizedText(aUseLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterImports")); // NOI18N

        aUseField.setColumns(5);

        bClassLabel.setLabelFor(bClassField);
        org.openide.awt.Mnemonics.setLocalizedText(bClassLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeClass")); // NOI18N

        bClassField.setColumns(5);

        aClassLabel.setLabelFor(aClassField);
        org.openide.awt.Mnemonics.setLocalizedText(aClassLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterClass")); // NOI18N

        aClassField.setColumns(5);

        aClassHeaderLabel.setLabelFor(aClassHeaderField);
        org.openide.awt.Mnemonics.setLocalizedText(aClassHeaderLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterClassHeader")); // NOI18N

        aClassHeaderField.setColumns(5);

        bFieldsLabel.setLabelFor(bFieldsField);
        org.openide.awt.Mnemonics.setLocalizedText(bFieldsLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeFields")); // NOI18N

        bFieldsField.setColumns(5);

        aFieldsLabel.setLabelFor(aFieldsField);
        org.openide.awt.Mnemonics.setLocalizedText(aFieldsLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterFields")); // NOI18N

        aFieldsField.setColumns(5);

        bMethodsLabel.setLabelFor(bMethodsField);
        org.openide.awt.Mnemonics.setLocalizedText(bMethodsLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeMethods")); // NOI18N

        bMethodsField.setColumns(5);

        aMethodsLabel.setLabelFor(aMethodsField);
        org.openide.awt.Mnemonics.setLocalizedText(aMethodsLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterMethods")); // NOI18N

        aMethodsField.setColumns(5);

        bFunctionEndLabel.setLabelFor(bFunctionEndField);
        org.openide.awt.Mnemonics.setLocalizedText(bFunctionEndLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeMethodsEnd")); // NOI18N

        bFunctionEndField.setColumns(5);

        bClassEndLabel.setLabelFor(bClassEndField);
        org.openide.awt.Mnemonics.setLocalizedText(bClassEndLabel, org.openide.util.NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeClassEnd")); // NOI18N

        bClassEndField.setColumns(5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bNamespaceLabel)
                    .addComponent(aNamespaceLabel)
                    .addComponent(bUseLabel)
                    .addComponent(aUseLabel)
                    .addComponent(bClassLabel)
                    .addComponent(aClassHeaderLabel)
                    .addComponent(bClassEndLabel)
                    .addComponent(bMethodsLabel)
                    .addComponent(aMethodsLabel)
                    .addComponent(bFunctionEndLabel)
                    .addComponent(aClassLabel)
                    .addComponent(bFieldsLabel)
                    .addComponent(aFieldsLabel))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(aClassHeaderField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bNamespaceField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aMethodsField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aClassField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bClassField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bClassEndField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bUseField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aFieldsField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bFunctionEndField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bMethodsField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aNamespaceField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bFieldsField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aUseField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {aClassField, aClassHeaderField, aFieldsField, aMethodsField, aNamespaceField, aUseField, bClassField, bFieldsField, bMethodsField, bNamespaceField, bUseField});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bNamespaceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bNamespaceLabel))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aNamespaceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aNamespaceLabel))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bUseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bUseLabel))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aUseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aUseLabel))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bClassField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bClassLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aClassHeaderLabel)
                    .addComponent(aClassHeaderField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bClassEndLabel)
                    .addComponent(bClassEndField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aClassField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aClassLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bFieldsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bFieldsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aFieldsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aFieldsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bMethodsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bMethodsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aMethodsLabel)
                    .addComponent(aMethodsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bFunctionEndField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bFunctionEndLabel))
                .addGap(50, 50, 50))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {aClassField, aClassHeaderField, aFieldsField, aMethodsField, aNamespaceField, aUseField, bClassField, bFieldsField, bMethodsField, bNamespaceField, bUseField});

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
    private javax.swing.JTextField aUseField;
    private javax.swing.JLabel aUseLabel;
    private javax.swing.JTextField bClassEndField;
    private javax.swing.JLabel bClassEndLabel;
    private javax.swing.JTextField bClassField;
    private javax.swing.JLabel bClassLabel;
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
    // End of variables declaration//GEN-END:variables
    
}
