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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.screen.display.ScreenSupport;

/**
 *
 * @author Anton Chechel
 */
public class FontEditorElement extends PropertyEditorResourceElement {

    private FontStub currentStub;
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
                
                if (kindList.hasFocus()) {
                    int kindCode = FontCD.getKindByValue(kindList.getSelectedValue());
                    currentStub.setKind(kindCode);
                    setState(currentStub);
                    fireElementChanged(currentStub.getComponentID(), FontCD.PROP_FONT_KIND, MidpTypes.createIntegerValue(kindCode));
                }
            }
        });
        
        faceList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (faceList.hasFocus()) {
                    int faceCode = FontCD.getFaceByValue(faceList.getSelectedValue());
                    currentStub.setFace(faceCode);
                    setState(currentStub);
                    fireElementChanged(currentStub.getComponentID(), FontCD.PROP_FACE, MidpTypes.createIntegerValue(faceCode));
                }
            }
        });

        styleList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (styleList.hasFocus()) {
                    int styleCode = FontCD.getStyleByValue(styleList.getSelectedValue());
                    currentStub.setStyle(styleCode);
                    setState(currentStub);
                    fireElementChanged(currentStub.getComponentID(), FontCD.PROP_STYLE, MidpTypes.createIntegerValue(styleCode));
                }
            }
        });

        sizeList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (sizeList.hasFocus()) {
                    int sizeCode = FontCD.getSizeByValue(sizeList.getSelectedValue());
                    currentStub.setSize(sizeCode);
                    setState(currentStub);
                    fireElementChanged(currentStub.getComponentID(), FontCD.PROP_SIZE, MidpTypes.createIntegerValue(sizeCode));
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

        model = (DefaultListModel) styleList.getModel();
        model.addElement(FontCD.LABEL_STYLE_PLAIN);
        model.addElement(FontCD.LABEL_STYLE_BOLD);
        model.addElement(FontCD.LABEL_STYLE_ITALIC);
        model.addElement(FontCD.LABEL_STYLE_UNDERLINED);

        model = (DefaultListModel) sizeList.getModel();
        model.addElement(FontCD.LABEL_SIZE_SMALL);
        model.addElement(FontCD.LABEL_SIZE_MEDIUM);
        model.addElement(FontCD.LABEL_SIZE_LARGE);
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
    
    private void setState(FontStub stub) {
        int kindCode = stub.getKind();
        kindList.setSelectedValue(FontCD.getKindByCode(kindCode), true);

        if (kindCode == FontCD.VALUE_KIND_CUSTOM) {
            int faceCode = stub.getFace();
            int styleCode = stub.getStyle();
            int sizeCode = stub.getSize();

            faceList.setSelectedValue(FontCD.getFaceByCode(faceCode), true);
            styleList.setSelectedValue(FontCD.getStyleByCode(styleCode), true);
            sizeList.setSelectedValue(FontCD.getSizeByCode(sizeCode), true);

            DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
            if (document != null) {
                sampleLabel.setFont(ScreenSupport.getFont(document, kindCode, faceCode, styleCode, sizeCode));
            }
        } else {
            setListsEnabledExceptKind(false);
        }
    }

    public void setDesignComponentWrapper(final DesignComponentWrapper wrapper) {
        if (wrapper == null) {
            // UI stuff
            kindList.clearSelection();
            setDefaultFont();
            setListsEnabled(false);
            return;
        }

        final DesignComponent component = wrapper.getComponent();
        if (component != null) { // existing component
            if (component.getType() != FontCD.TYPEID) {
                throw new IllegalArgumentException("Passed component must have typeID " + FontCD.TYPEID + " instead passed " + component.getType()); // NOI18N
            }

            long componentID = component.getComponentID();
            final int[] kindCode = new int[1];
            final int[] faceCode = new int[1];
            final int[] styleCode = new int[1];
            final int[] sizeCode = new int[1];
            component.getDocument().getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    kindCode[0] = MidpTypes.getInteger(component.readProperty(FontCD.PROP_FONT_KIND));
                    faceCode[0] = MidpTypes.getInteger(component.readProperty(FontCD.PROP_FACE));
                    styleCode[0] = MidpTypes.getInteger(component.readProperty(FontCD.PROP_STYLE));
                    sizeCode[0] = MidpTypes.getInteger(component.readProperty(FontCD.PROP_SIZE));
                }
            });
            currentStub = new FontStub(componentID, kindCode[0], faceCode[0], styleCode[0], sizeCode[0]);
        
            // UI stuff
            setState(currentStub);
            kindList.setEnabled(true);
            if (kindCode[0] != FontCD.VALUE_KIND_CUSTOM) {
                setListsEnabledExceptKind(false);
            }
        } else { // virtual component

        }
    }

    private static class FontStub {
        private long componentID;
        private int kind;
        private int face;
        private int style;
        private int size;
        private boolean isChanged;

        public FontStub(long componentID, int kind, int face, int style, int size) {
            this.componentID = componentID;
            this.kind = kind;
            this.face = face;
            this.style = style;
            this.size = size;
        }

        public long getComponentID() {
            return componentID;
        }

        public boolean isChanged() {
            return isChanged;
        }

        public void setWasChanged(boolean isChanged) {
            this.isChanged = isChanged;
        }

        public int getKind() {
            return kind;
        }

        public void setKind(int kind) {
            this.kind = kind;
        }

        public int getFace() {
            return face;
        }

        public void setFace(int face) {
            this.face = face;
        }

        public int getStyle() {
            return style;
        }

        public void setStyle(int style) {
            this.style = style;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
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
                    .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                    .add(jScrollPane5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sampleLabel)
                    .add(jLabel1)))
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
