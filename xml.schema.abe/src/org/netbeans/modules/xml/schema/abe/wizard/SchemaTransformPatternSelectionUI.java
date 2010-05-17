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

package org.netbeans.modules.xml.schema.abe.wizard;

import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.openide.util.NbBundle;

public final class SchemaTransformPatternSelectionUI extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private transient SchemaGenerator.Pattern edp;
    
    /**
     * Creates new form SchemaTransformPatternSelectionUI
     */
    public SchemaTransformPatternSelectionUI(SchemaGenerator.Pattern edp) {
        this.edp = edp;
        initComponents();
        patternExamplePane.setActionMap(null);patternExamplePane.getActions();
        patternExamplePane.getEditorKit().getActions();
        reset();
    }
    
    public String getName() {
        return NbBundle.getMessage(
                SchemaTransformPatternSelectionUI.class,
                "SchemaTransform_PatternSelection");
    }
    
    public void reset() {
        removeListeners();
        initializeUISelection();
        addListeners();
    }
    
    private void addListeners() {
        if(elementCheckBoxListener == null) {
            elementCheckBoxListener = new ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED
                            || evt.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
                        selectPattern(getSelectedDesignPattern());
                    }
                }
            };
        }
        if(typeCheckBoxListener == null) {
            typeCheckBoxListener = new ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED
                            || evt.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
                        selectPattern(getSelectedDesignPattern());
                    }
                }
            };
        }
        singleGlobalElement.addItemListener(elementCheckBoxListener);
        createtype.addItemListener(elementCheckBoxListener);
    }
    
    private void removeListeners() {
        singleGlobalElement.removeItemListener(elementCheckBoxListener);
        createtype.removeItemListener(elementCheckBoxListener);
    }
    
    private void initializeUISelection() {
        selectInitialDesignPattern(edp);
        
        existingPatternText.setText(
                NbBundle.getMessage(
                SchemaTransformPatternSelectionUI.class,
                "LBL_SchemaTransform_"+edp.toString()));
    }
    
    public boolean isSingleGlobalElementSelected() {
        return singleGlobalElement.isSelected();
    }
    
    public void setSingleGlobalElementSelected(boolean select) {
        singleGlobalElement.setSelected(select);
        multipleGlobalElement.setSelected(!select);
    }
    
    public boolean isTypeReuseSelected() {
        return createtype.isSelected();
    }
    
    public void setTypeReuseSelected(boolean select) {
        createtype.setSelected(select);
        noType.setSelected(!select);
    }
    
    private void selectPattern(SchemaGenerator.Pattern p){
        selectedPatternText.setText(
                NbBundle.getMessage(
                SchemaTransformPatternSelectionUI.class,
                "LBL_SchemaTransform_"+p.toString()));
        patternDescText.setText(
                NbBundle.getMessage(
                SchemaTransformPatternSelectionUI.class,
                "MSG_SchemaTransform_"+p.toString()+"_Desc"));
//		patternDescText.setToolTipText(
//			NbBundle.getMessage(
//				SchemaTransformPatternSelectionUI.class,
//				"MSG_SchemaTransform_"+p.toString()+"_Desc"));
        patternExamplePane.setText(
                NbBundle.getMessage(
                SchemaTransformPatternSelectionUI.class,
                "MSG_SchemaTransform_"+p.toString()+"_Example"));
        patternExamplePane.setCaretPosition(0);
        fireChange();
    }
    
    public SchemaGenerator.Pattern getSelectedDesignPattern() {
        if(isSingleGlobalElementSelected() &&
                !isTypeReuseSelected())
            return SchemaGenerator.Pattern.RUSSIAN_DOLL;
        else if(isSingleGlobalElementSelected() &&
                isTypeReuseSelected())
            return SchemaGenerator.Pattern.VENITIAN_BLIND;
        else if(!isSingleGlobalElementSelected() &&
                !isTypeReuseSelected())
            return SchemaGenerator.Pattern.SALAMI_SLICE;
        else if(!isSingleGlobalElementSelected() &&
                isTypeReuseSelected())
            return SchemaGenerator.Pattern.GARDEN_OF_EDEN;
        
        return SchemaGenerator.DEFAULT_DESIGN_PATTERN;
    }
    
    private void selectInitialDesignPattern(SchemaGenerator.Pattern p) {
        if(p == SchemaGenerator.Pattern.RUSSIAN_DOLL) {
            setSingleGlobalElementSelected(true);
            setTypeReuseSelected(false);
        } else if(p == SchemaGenerator.Pattern.VENITIAN_BLIND) {
            setSingleGlobalElementSelected(true);
            setTypeReuseSelected(true);
        } else if(p == SchemaGenerator.Pattern.SALAMI_SLICE) {
            setSingleGlobalElementSelected(false);
            setTypeReuseSelected(false);
        } else if(p == SchemaGenerator.Pattern.GARDEN_OF_EDEN) {
            setSingleGlobalElementSelected(false);
            setTypeReuseSelected(true);
        }
        
        selectPattern(p);
    }
    
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        globalElementGroup = new javax.swing.ButtonGroup();
        typeGroup = new javax.swing.ButtonGroup();
        selectedPatternDescLabel = new javax.swing.JLabel();
        patternExampleLabel = new javax.swing.JLabel();
        selectedPatternText = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        patternExamplePane = new javax.swing.JEditorPane()
        {
            static final long serialVersionUID = 1L;
            // disable mouse and mouse motion events
            protected void processMouseEvent(java.awt.event.MouseEvent e)
            {
                e.consume();
            }
            protected void processMouseMotionEvent(java.awt.event.MouseEvent e)
            {
                e.consume();
            }
        };
        existingPatternDescLabel = new javax.swing.JLabel();
        existingPatternText = new javax.swing.JLabel();
        singleGlobalElement = new javax.swing.JRadioButton();
        multipleGlobalElement = new javax.swing.JRadioButton();
        globalElement = new javax.swing.JLabel();
        reusableType = new javax.swing.JLabel();
        createtype = new javax.swing.JRadioButton();
        noType = new javax.swing.JRadioButton();
        patternDescText = new javax.swing.JLabel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/schema/abe/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(selectedPatternDescLabel, bundle.getString("LBL_SchemaTransform_Selected_DesignPattern_Desc")); // NOI18N
        selectedPatternDescLabel.setToolTipText(bundle.getString("HINT_SchemaTransform_Selected_DesignPattern_Desc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(patternExampleLabel, bundle.getString("LBL_SchemaTransform_DesignPattern_Example")); // NOI18N
        patternExampleLabel.setToolTipText(bundle.getString("HINT_SchemaTransform_DesignPattern_Example")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectedPatternText, bundle.getString("LBL_SchemaTransform_Selected_DesignPattern_Text")); // NOI18N

        jScrollPane1.setMinimumSize(null);
        jScrollPane1.setPreferredSize(null);

        patternExamplePane.setContentType("text/xml");
        patternExamplePane.setEditable(false);
        patternExamplePane.setToolTipText(bundle.getString("HINT_SchemaTransform_Example")); // NOI18N
        patternExamplePane.setEnabled(false);
        patternExamplePane.setMinimumSize(null);
        patternExamplePane.setPreferredSize(null);
        jScrollPane1.setViewportView(patternExamplePane);
        patternExamplePane.getAccessibleContext().setAccessibleName(bundle.getString("MSG_SchemaTransform_Example")); // NOI18N
        patternExamplePane.getAccessibleContext().setAccessibleDescription(bundle.getString("HINT_SchemaTransform_Example")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(existingPatternDescLabel, bundle.getString("LBL_SchemaTransform_Existing_DesignPattern_Desc")); // NOI18N
        existingPatternDescLabel.setToolTipText(bundle.getString("HINT_SchemaTransform_Existing_DesignPattern_Desc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(existingPatternText, bundle.getString("LBL_SchemaTransform_Existing_DesignPattern_Text")); // NOI18N

        globalElementGroup.add(singleGlobalElement);
        org.openide.awt.Mnemonics.setLocalizedText(singleGlobalElement, bundle.getString("LBL_SchemaTransform_SingleGlobalElement")); // NOI18N
        singleGlobalElement.setToolTipText(bundle.getString("HINT_SchemaTransform_SingleGlobalElement")); // NOI18N
        singleGlobalElement.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        singleGlobalElement.setMargin(new java.awt.Insets(0, 0, 0, 0));

        globalElementGroup.add(multipleGlobalElement);
        org.openide.awt.Mnemonics.setLocalizedText(multipleGlobalElement, bundle.getString("LBL_SchemaTransform_MultipleGlobalElement")); // NOI18N
        multipleGlobalElement.setToolTipText(bundle.getString("HINT_SchemaTransform_MultipleGlobalElement")); // NOI18N
        multipleGlobalElement.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        multipleGlobalElement.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(globalElement, bundle.getString("LBL_SchemaTransform_GlobalElement")); // NOI18N
        globalElement.setToolTipText(bundle.getString("HINT_SchemaTransform_GlobalElement")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(reusableType, bundle.getString("LBL_SchemaTransform_Type")); // NOI18N
        reusableType.setToolTipText(bundle.getString("HINT_SchemaTransform_Type")); // NOI18N

        typeGroup.add(createtype);
        org.openide.awt.Mnemonics.setLocalizedText(createtype, bundle.getString("LBL_SchemaTransform_ReusableTypes")); // NOI18N
        createtype.setToolTipText(bundle.getString("HINT_SchemaTransform_ReusableTypes")); // NOI18N
        createtype.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        createtype.setMargin(new java.awt.Insets(0, 0, 0, 0));

        typeGroup.add(noType);
        org.openide.awt.Mnemonics.setLocalizedText(noType, bundle.getString("LBL_SchemaTransform_No_ReusableTypes")); // NOI18N
        noType.setToolTipText(bundle.getString("HINT_SchemaTransform_No_ReusableTypes")); // NOI18N
        noType.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noType.setMargin(new java.awt.Insets(0, 0, 0, 0));

        patternDescText.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        patternDescText.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        patternDescText.setMaximumSize(new java.awt.Dimension(333333, 333333));
        patternDescText.setMinimumSize(null);
        patternDescText.setPreferredSize(null);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, patternDescText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, globalElement)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, singleGlobalElement)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, multipleGlobalElement)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, reusableType)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(noType)
                            .add(createtype)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(selectedPatternDescLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(selectedPatternText)
                        .add(35, 35, 35)
                        .add(existingPatternDescLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(existingPatternText))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, patternExampleLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(globalElement)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(singleGlobalElement)
                .add(7, 7, 7)
                .add(multipleGlobalElement)
                .add(6, 6, 6)
                .add(reusableType)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(createtype)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(noType)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectedPatternDescLabel)
                    .add(selectedPatternText)
                    .add(existingPatternDescLabel)
                    .add(existingPatternText))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(patternDescText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(patternExampleLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 95, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(29, 29, 29))
        );

        patternDescText.getAccessibleContext().setAccessibleName(bundle.getString("MSG_SchemaTransform_Desc")); // NOI18N
        patternDescText.getAccessibleContext().setAccessibleDescription(bundle.getString("HINT_SchemaTransform_Desc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton createtype;
    private javax.swing.JLabel existingPatternDescLabel;
    private javax.swing.JLabel existingPatternText;
    private javax.swing.JLabel globalElement;
    private javax.swing.ButtonGroup globalElementGroup;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton multipleGlobalElement;
    private javax.swing.JRadioButton noType;
    private javax.swing.JLabel patternDescText;
    private javax.swing.JLabel patternExampleLabel;
    private javax.swing.JEditorPane patternExamplePane;
    private javax.swing.JLabel reusableType;
    private javax.swing.JLabel selectedPatternDescLabel;
    private javax.swing.JLabel selectedPatternText;
    private javax.swing.JRadioButton singleGlobalElement;
    private javax.swing.ButtonGroup typeGroup;
    // End of variables declaration//GEN-END:variables
    
    private transient ItemListener typeCheckBoxListener;
    private transient ItemListener elementCheckBoxListener;
    private transient SchemaGenerator.Pattern currentPattern;
}

