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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.vmd.midp.propertyeditors.resource.elements;

import java.awt.Font;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.screen.display.ScreenSupport;

/**
 *
 * @author Anton Chechel
 */
public class FontEditorElement extends JPanel implements PropertyEditorResourceElement {

    private Font defaultFont;

    public FontEditorElement() {
        initComponents();
        initListModels();
        attachListeners();

        this.defaultFont = sampleLabel.getFont();
    }

    public JComponent getJComponent() {
        return this;
    }

    private void attachListeners() {
        kindList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                Object selectedValue = kindList.getSelectedValue();
                if (!FontCD.LABEL_KIND_CUSTOM.equals(selectedValue)) {
                    setDefaultFont();
                    setListsEnabledExceptKind(false);
                } else {
                    setListsEnabledExceptKind(true);
                }
            }
        });
    }

    private void initListModels() {
        DefaultListModel model = (DefaultListModel) kindList.getModel();
        model.addElement(FontCD.LABEL_KIND_CUSTOM);
        model.addElement(FontCD.LABEL_KIND_DEFAULT);
        model.addElement(FontCD.LABEL_KIND_STATIC);
        model.addElement(FontCD.LABEL_KIND_INPUT);

        model = (DefaultListModel) faceList.getModel();
        model.addElement(FontCD.LABEL_FACE_SYSTEM);
        model.addElement(FontCD.LABEL_FACE_MONOSPACE);
        model.addElement(FontCD.LABEL_FACE_PROPORTIONAL);

        model = (DefaultListModel) sizeList.getModel();
        model.addElement(FontCD.LABEL_SIZE_SMALL);
        model.addElement(FontCD.LABEL_SIZE_MEDIUM);
        model.addElement(FontCD.LABEL_SIZE_LARGE);

        model = (DefaultListModel) styleList.getModel();
        model.addElement(FontCD.LABEL_STYLE_PLAIN);
        model.addElement(FontCD.LABEL_STYLE_BOLD);
        model.addElement(FontCD.LABEL_STYLE_ITALIC);
        model.addElement(FontCD.LABEL_STYLE_UNDERLINED);
    }

    private void setDefaultFont() {
        faceList.clearSelection();
        styleList.clearSelection();
        sizeList.clearSelection();
        sampleLabel.setFont(defaultFont);
    }

    private void setListsEnabledExceptKind(boolean isEnabled) {
        faceList.setEnabled(isEnabled);
        styleList.setEnabled(isEnabled);
        sizeList.setEnabled(isEnabled);
    }

    private void setListsEnabled(boolean isEnabled) {
        kindList.setEnabled(isEnabled);
        setListsEnabledExceptKind(isEnabled);
    }

    public void setDesignComponent(final DesignComponent component) {
        if (component == null) {
            kindList.clearSelection();
            setDefaultFont();
            setListsEnabled(false);
            return;
        }

        if (component.getType() != FontCD.TYPEID) {
            throw new IllegalArgumentException("Passed component must have typeID " + FontCD.TYPEID + " instead passed " + component.getType()); // NOI18N
        }

        setListsEnabled(true);

        component.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                int kindCode = MidpTypes.getInteger(component.readProperty(FontCD.PROP_FONT_KIND));
                if (kindCode == FontCD.VALUE_KIND_DEFAULT) {
                    kindList.setSelectedValue(FontCD.LABEL_KIND_DEFAULT, true);
                } else if (kindCode == FontCD.VALUE_KIND_CUSTOM) {
                    kindList.setSelectedValue(FontCD.LABEL_KIND_CUSTOM, true);
                } else if (kindCode == FontCD.VALUE_KIND_STATIC) {
                    kindList.setSelectedValue(FontCD.LABEL_KIND_STATIC, true);
                } else if (kindCode == FontCD.VALUE_KIND_INPUT) {
                    kindList.setSelectedValue(FontCD.LABEL_KIND_INPUT, true);
                }

                if (kindCode == FontCD.VALUE_KIND_CUSTOM) {
                    int faceCode = MidpTypes.getInteger(component.readProperty(FontCD.PROP_FACE));
                    if (faceCode == FontCD.VALUE_FACE_SYSTEM) {
                        faceList.setSelectedValue(FontCD.LABEL_FACE_SYSTEM, true);
                    } else if (faceCode == FontCD.VALUE_FACE_MONOSPACE) {
                        faceList.setSelectedValue(FontCD.LABEL_FACE_MONOSPACE, true);
                    } else if (faceCode == FontCD.VALUE_FACE_PROPORTIONAL) {
                        faceList.setSelectedValue(FontCD.LABEL_FACE_PROPORTIONAL, true);
                    }

                    int styleCode = MidpTypes.getInteger(component.readProperty(FontCD.PROP_STYLE));
                    if (styleCode == FontCD.VALUE_STYLE_PLAIN) {
                        styleList.setSelectedValue(FontCD.LABEL_STYLE_PLAIN, true);
                    } else if (styleCode == FontCD.VALUE_STYLE_BOLD) {
                        styleList.setSelectedValue(FontCD.LABEL_STYLE_BOLD, true);
                    } else if (styleCode == FontCD.VALUE_STYLE_ITALIC) {
                        styleList.setSelectedValue(FontCD.LABEL_STYLE_ITALIC, true);
                    } else if (styleCode == FontCD.VALUE_STYLE_UNDERLINED) {
                        styleList.setSelectedValue(FontCD.LABEL_STYLE_UNDERLINED, true);
                    }

                    int sizeCode = MidpTypes.getInteger(component.readProperty(FontCD.PROP_SIZE));
                    if (sizeCode == FontCD.VALUE_SIZE_SMALL) {
                        sizeList.setSelectedValue(FontCD.LABEL_SIZE_SMALL, true);
                    } else if (sizeCode == FontCD.VALUE_SIZE_MEDIUM) {
                        sizeList.setSelectedValue(FontCD.LABEL_SIZE_MEDIUM, true);
                    } else if (sizeCode == FontCD.VALUE_SIZE_LARGE) {
                        sizeList.setSelectedValue(FontCD.LABEL_SIZE_LARGE, true);
                    }

                    sampleLabel.setFont(ScreenSupport.getFont(component));
                }
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        kindList = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        faceList = new javax.swing.JList();
        jScrollPane4 = new javax.swing.JScrollPane();
        sizeList = new javax.swing.JList();
        jScrollPane5 = new javax.swing.JScrollPane();
        styleList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        sampleLabel = new javax.swing.JLabel();

        kindList.setModel(new DefaultListModel());
        kindList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(kindList);

        faceList.setModel(new DefaultListModel());
        faceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(faceList);

        sizeList.setModel(new DefaultListModel());
        sizeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(sizeList);

        styleList.setModel(new DefaultListModel());
        jScrollPane5.setViewportView(styleList);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.jLabel1.text")); // NOI18N

        sampleLabel.setText(org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.sampleLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6)
                        .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(sampleLabel)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(sampleLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
        // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList faceList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JList kindList;
    private javax.swing.JLabel sampleLabel;
    private javax.swing.JList sizeList;
    private javax.swing.JList styleList;
    // End of variables declaration//GEN-END:variables
}
