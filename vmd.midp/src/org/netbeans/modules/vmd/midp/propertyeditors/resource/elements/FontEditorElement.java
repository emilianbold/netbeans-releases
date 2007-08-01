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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
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
        attachListeners();

        this.defaultFont = sampleLabel.getFont();
    }

    public JComponent getJComponent() {
        return this;
    }

    public TypeID getTypeID() {
        return FontCD.TYPEID;
    }

    public List<String> getPropertyValueNames() {
        return Arrays.asList(new String[] {FontCD.PROP_FONT_KIND, FontCD.PROP_FACE, FontCD.PROP_STYLE, FontCD.PROP_SIZE});
    }
    
    private void attachListeners() {
        ActionListener kindActionListener = new KindActionListener();
        defaultRadioButton.addActionListener(kindActionListener);
        staticRadioButton.addActionListener(kindActionListener);
        inputRadioButton.addActionListener(kindActionListener);
        customRadioButton.addActionListener(kindActionListener);

        ActionListener faceActionListener = new FaceActionListener();
        systemRadioButton.addActionListener(faceActionListener);
        monospaceRadioButton.addActionListener(faceActionListener);
        proportionalRadioButton.addActionListener(faceActionListener);

        ActionListener styleActionListener = new StyleActionListener();
        plainCheckBox.addActionListener(styleActionListener);
        boldCheckBox.addActionListener(styleActionListener);
        italicCheckBox.addActionListener(styleActionListener);
        underlinedCheckBox.addActionListener(styleActionListener);

        ActionListener sizeActionListener = new SizeActionListener();
        smallRadioButton.addActionListener(sizeActionListener);
        mediumRadioButton.addActionListener(sizeActionListener);
        largeRadioButton.addActionListener(sizeActionListener);
    }

    private void setKindUnselected() {
        defaultRadioButton.setSelected(false);
        staticRadioButton.setSelected(false);
        inputRadioButton.setSelected(false);
        customRadioButton.setSelected(false);
    }

    private void setDefaultFont() {
        systemRadioButton.setSelected(false);
        monospaceRadioButton.setSelected(false);
        proportionalRadioButton.setSelected(false);

        plainCheckBox.setSelected(false);
        boldCheckBox.setSelected(false);
        italicCheckBox.setSelected(false);
        underlinedCheckBox.setSelected(false);

        smallRadioButton.setSelected(false);
        mediumRadioButton.setSelected(false);
        largeRadioButton.setSelected(false);

        setSampleFont(true);
    }

    private void setFaceSizeStyleEnabled(boolean isEnabled) {
        faceLabel.setEnabled(isEnabled);
        systemRadioButton.setEnabled(isEnabled);
        monospaceRadioButton.setEnabled(isEnabled);
        proportionalRadioButton.setEnabled(isEnabled);

        styleLabel.setEnabled(isEnabled);
        plainCheckBox.setEnabled(isEnabled);
        boldCheckBox.setEnabled(isEnabled);
        italicCheckBox.setEnabled(isEnabled);
        underlinedCheckBox.setEnabled(isEnabled);

        sizeLabel.setEnabled(isEnabled);
        smallRadioButton.setEnabled(isEnabled);
        mediumRadioButton.setEnabled(isEnabled);
        largeRadioButton.setEnabled(isEnabled);
    }

    private void setKindEnabled(boolean isEnabled) {
        kindLabel.setEnabled(isEnabled);
        defaultRadioButton.setEnabled(isEnabled);
        staticRadioButton.setEnabled(isEnabled);
        inputRadioButton.setEnabled(isEnabled);
        customRadioButton.setEnabled(isEnabled);
    }

    private void setAllEnabled(boolean isEnabled) {
        setKindEnabled(isEnabled);
        setFaceSizeStyleEnabled(isEnabled);
    }

    private void setKindSelected(int kindCode) {
        switch (kindCode) {
            case FontCD.VALUE_KIND_DEFAULT:
                defaultRadioButton.setSelected(true);
                break;
            case FontCD.VALUE_KIND_CUSTOM:
                customRadioButton.setSelected(true);
                break;
            case FontCD.VALUE_KIND_STATIC:
                staticRadioButton.setSelected(true);
                break;
            case FontCD.VALUE_KIND_INPUT:
                inputRadioButton.setSelected(true);
        }
    }

    private void setFaceSelected(int faceCode) {
        switch (faceCode) {
            case FontCD.VALUE_FACE_SYSTEM:
                systemRadioButton.setSelected(true);
                break;
            case FontCD.VALUE_FACE_MONOSPACE:
                monospaceRadioButton.setSelected(true);
                break;
            case FontCD.VALUE_FACE_PROPORTIONAL:
                proportionalRadioButton.setSelected(true);
        }
    }

    private void setSizeSelected(int sizeCode) {
        switch (sizeCode) {
            case FontCD.VALUE_SIZE_SMALL:
                smallRadioButton.setSelected(true);
                break;
            case FontCD.VALUE_SIZE_MEDIUM:
                mediumRadioButton.setSelected(true);
                break;
            case FontCD.VALUE_SIZE_LARGE:
                largeRadioButton.setSelected(true);
        }
    }

    private void setStyleSelected(int styleCode) {
        if (styleCode == FontCD.VALUE_STYLE_PLAIN) {
            plainCheckBox.setSelected(true);
        } else {
            boldCheckBox.setSelected((styleCode & FontCD.VALUE_STYLE_BOLD) != 0);
            italicCheckBox.setSelected((styleCode & FontCD.VALUE_STYLE_ITALIC) != 0);
            underlinedCheckBox.setSelected((styleCode & FontCD.VALUE_STYLE_UNDERLINED) != 0);
        }
    }

    private void setStateOfButtons() {
        int kindCode = currentStub.getKind();
        setKindSelected(kindCode);

        if (kindCode == FontCD.VALUE_KIND_CUSTOM) {
            int faceCode = currentStub.getFace();
            int styleCode = currentStub.getStyle();
            int sizeCode = currentStub.getSize();

            setFaceSelected(faceCode);
            setStyleSelected(styleCode);
            setSizeSelected(sizeCode);
        } else {
            setFaceSizeStyleEnabled(false);
        }
    }

    private void setSampleFont(boolean isDefault) {
        if (isDefault) {
            sampleLabel.setFont(defaultFont);
            return;
        }

        DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
        if (document != null) {
            int kindCode = currentStub.getKind();
            int faceCode = currentStub.getFace();
            int styleCode = currentStub.getStyle();
            int sizeCode = currentStub.getSize();
            sampleLabel.setFont(ScreenSupport.getFont(document, kindCode, faceCode, styleCode, sizeCode));
        }
    }

    public void setDesignComponentWrapper(final DesignComponentWrapper wrapper) {
        if (wrapper == null) {
            // UI stuff
            setKindUnselected();
            setDefaultFont();
            setAllEnabled(false);
            return;
        }

        long componentID = wrapper.getComponentID();
        final int[] kindCode = new int[] {FontCD.VALUE_KIND_DEFAULT};
        final int[] faceCode = new int[] {FontCD.VALUE_FACE_SYSTEM};
        final int[] styleCode = new int[] {FontCD.VALUE_STYLE_PLAIN};
        final int[] sizeCode = new int[] {FontCD.VALUE_SIZE_MEDIUM};

        final DesignComponent component = wrapper.getComponent();
        if (component != null) { // existing component
            if (!component.getType().equals(getTypeID())) {
                throw new IllegalArgumentException("Passed component must have typeID " + getTypeID() + " instead passed " + component.getType()); // NOI18N
            }

            componentID = component.getComponentID();
            component.getDocument().getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    PropertyValue propertyValue = component.readProperty(FontCD.PROP_FONT_KIND);
                    if (!isPropertyValueAUserCodeType(propertyValue)) {
                        kindCode[0] = MidpTypes.getInteger(propertyValue);
                    }
                    propertyValue = component.readProperty(FontCD.PROP_FACE);
                    if (!isPropertyValueAUserCodeType(propertyValue)) {
                        faceCode[0] = MidpTypes.getInteger(propertyValue);
                    }
                    propertyValue = component.readProperty(FontCD.PROP_STYLE);
                    if (!isPropertyValueAUserCodeType(propertyValue)) {
                        styleCode[0] = MidpTypes.getInteger(propertyValue);
                    }
                    propertyValue = component.readProperty(FontCD.PROP_SIZE);
                    if (!isPropertyValueAUserCodeType(propertyValue)) {
                        sizeCode[0] = MidpTypes.getInteger(propertyValue);
                    }
                }
            });
        }

        if (wrapper.hasChanges()) {
            Map<String, PropertyValue> changes = wrapper.getChanges();
            for (String propertyName : changes.keySet()) {
                final PropertyValue propertyValue = changes.get(propertyName);
                if (FontCD.PROP_FONT_KIND.equals(propertyName)) {
                    kindCode[0] = MidpTypes.getInteger(propertyValue);
                } else if (FontCD.PROP_FACE.equals(propertyName)) {
                    faceCode[0] = MidpTypes.getInteger(propertyValue);
                } else if (FontCD.PROP_STYLE.equals(propertyName)) {
                    styleCode[0] = MidpTypes.getInteger(propertyValue);
                } else if (FontCD.PROP_SIZE.equals(propertyName)) {
                    sizeCode[0] = MidpTypes.getInteger(propertyValue);
                }
            }
        }

        currentStub = new FontStub(componentID, kindCode[0], faceCode[0], styleCode[0], sizeCode[0]);

        // UI stuff
        setStateOfButtons();
        setKindEnabled(true);
        setFaceSizeStyleEnabled(kindCode[0] == FontCD.VALUE_KIND_CUSTOM);
        setSampleFont(false);
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

        @Override
        public String toString() {
            return "[componentID=" + componentID + ", kind=" + kind + ", face=" + face + ", style=" + style + ", size=" + size + "]"; // NOI18N
        }
    }

    private class KindActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            int code = FontCD.VALUE_KIND_DEFAULT;

            if (src == customRadioButton) {
                setFaceSizeStyleEnabled(true);
                code = FontCD.VALUE_KIND_CUSTOM;
            } else if (src == defaultRadioButton) {
                setFaceSizeStyleEnabled(false);
                code = FontCD.VALUE_KIND_DEFAULT;
            } else if (src == staticRadioButton) {
                setFaceSizeStyleEnabled(false);
                code = FontCD.VALUE_KIND_STATIC;
            } else if (src == inputRadioButton) {
                setFaceSizeStyleEnabled(false);
                code = FontCD.VALUE_KIND_INPUT;
            }

            currentStub.setKind(code);
            setSampleFont(false);
            fireElementChanged(currentStub.getComponentID(), FontCD.PROP_FONT_KIND, MidpTypes.createIntegerValue(code));
        }
    }

    private class SizeActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            int code = FontCD.VALUE_SIZE_MEDIUM;

            if (src == smallRadioButton) {
                code = FontCD.VALUE_SIZE_SMALL;
            } else if (src == largeRadioButton) {
                code = FontCD.VALUE_SIZE_LARGE;
            } 

            currentStub.setSize(code);
            setSampleFont(false);
            fireElementChanged(currentStub.getComponentID(), FontCD.PROP_SIZE, MidpTypes.createIntegerValue(code));
        }
    }

    private class FaceActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            int code = FontCD.VALUE_FACE_SYSTEM;

            if (src == monospaceRadioButton) {
                code = FontCD.VALUE_FACE_MONOSPACE;
            } else if (src == proportionalRadioButton) {
                code = FontCD.VALUE_FACE_PROPORTIONAL;
            } 

            currentStub.setFace(code);
            setSampleFont(false);
            fireElementChanged(currentStub.getComponentID(), FontCD.PROP_FACE, MidpTypes.createIntegerValue(code));
        }
    }

    private class StyleActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            int code = FontCD.VALUE_STYLE_PLAIN;
            if (src == plainCheckBox) {
                if (plainCheckBox.isSelected()) {
                    boldCheckBox.setSelected(false);
                    italicCheckBox.setSelected(false);
                    underlinedCheckBox.setSelected(false);
                }
            } else {
                plainCheckBox.setSelected(!(boldCheckBox.isSelected() || italicCheckBox.isSelected() || underlinedCheckBox.isSelected()));
                
                if (boldCheckBox.isSelected()) {
                    code |= FontCD.VALUE_STYLE_BOLD;
                }
                if (italicCheckBox.isSelected()) {
                    code |= FontCD.VALUE_STYLE_ITALIC;
                }
                if (underlinedCheckBox.isSelected()) {
                    code |= FontCD.VALUE_STYLE_UNDERLINED;
                }
            }
            
            currentStub.setStyle(code);
            setSampleFont(false);
            fireElementChanged(currentStub.getComponentID(), FontCD.PROP_STYLE, MidpTypes.createIntegerValue(code));
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        kindButtonGroup = new javax.swing.ButtonGroup();
        faceButtonGroup = new javax.swing.ButtonGroup();
        sizeButtonGroup = new javax.swing.ButtonGroup();
        defaultRadioButton = new javax.swing.JRadioButton();
        staticRadioButton = new javax.swing.JRadioButton();
        inputRadioButton = new javax.swing.JRadioButton();
        customRadioButton = new javax.swing.JRadioButton();
        kindLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        sampleLabel = new javax.swing.JLabel();
        faceLabel = new javax.swing.JLabel();
        systemRadioButton = new javax.swing.JRadioButton();
        monospaceRadioButton = new javax.swing.JRadioButton();
        proportionalRadioButton = new javax.swing.JRadioButton();
        sizeLabel = new javax.swing.JLabel();
        smallRadioButton = new javax.swing.JRadioButton();
        mediumRadioButton = new javax.swing.JRadioButton();
        largeRadioButton = new javax.swing.JRadioButton();
        styleLabel = new javax.swing.JLabel();
        plainCheckBox = new javax.swing.JCheckBox();
        boldCheckBox = new javax.swing.JCheckBox();
        italicCheckBox = new javax.swing.JCheckBox();
        underlinedCheckBox = new javax.swing.JCheckBox();

        kindButtonGroup.add(defaultRadioButton);
        defaultRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(defaultRadioButton, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.defaultRadioButton.text")); // NOI18N
        defaultRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        kindButtonGroup.add(staticRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(staticRadioButton, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.staticRadioButton.text")); // NOI18N
        staticRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        staticRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        kindButtonGroup.add(inputRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(inputRadioButton, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.inputRadioButton.text")); // NOI18N
        inputRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        inputRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        kindButtonGroup.add(customRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(customRadioButton, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.customRadioButton.text")); // NOI18N
        customRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        customRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        kindLabel.setText(org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.kindLabel.text")); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.jPanel2.border.title"))); // NOI18N
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(null);

        sampleLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        sampleLabel.setText(org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.sampleLabel.text")); // NOI18N
        jPanel1.add(sampleLabel);
        sampleLabel.setBounds(0, 0, 310, 16);

        jPanel2.add(jPanel1, java.awt.BorderLayout.CENTER);

        faceLabel.setText(org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.faceLabel.text")); // NOI18N
        faceLabel.setEnabled(false);

        faceButtonGroup.add(systemRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(systemRadioButton, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.systemRadioButton.text")); // NOI18N
        systemRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        systemRadioButton.setEnabled(false);
        systemRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        faceButtonGroup.add(monospaceRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(monospaceRadioButton, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.monospaceRadioButton.text")); // NOI18N
        monospaceRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        monospaceRadioButton.setEnabled(false);
        monospaceRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        faceButtonGroup.add(proportionalRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(proportionalRadioButton, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.proportionalRadioButton.text")); // NOI18N
        proportionalRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        proportionalRadioButton.setEnabled(false);
        proportionalRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        sizeLabel.setText(org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.sizeLabel.text")); // NOI18N
        sizeLabel.setEnabled(false);

        sizeButtonGroup.add(smallRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(smallRadioButton, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.smallRadioButton.text")); // NOI18N
        smallRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        smallRadioButton.setEnabled(false);
        smallRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        sizeButtonGroup.add(mediumRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(mediumRadioButton, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.mediumRadioButton.text")); // NOI18N
        mediumRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mediumRadioButton.setEnabled(false);
        mediumRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        sizeButtonGroup.add(largeRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(largeRadioButton, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.largeRadioButton.text")); // NOI18N
        largeRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        largeRadioButton.setEnabled(false);
        largeRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        styleLabel.setText(org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.styleLabel.text")); // NOI18N
        styleLabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(plainCheckBox, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.plainCheckBox.text")); // NOI18N
        plainCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        plainCheckBox.setEnabled(false);
        plainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(boldCheckBox, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.boldCheckBox.text")); // NOI18N
        boldCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        boldCheckBox.setEnabled(false);
        boldCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(italicCheckBox, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.italicCheckBox.text")); // NOI18N
        italicCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        italicCheckBox.setEnabled(false);
        italicCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(underlinedCheckBox, org.openide.util.NbBundle.getMessage(FontEditorElement.class, "FontEditorElement.underlinedCheckBox.text")); // NOI18N
        underlinedCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        underlinedCheckBox.setEnabled(false);
        underlinedCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(defaultRadioButton)
                    .add(staticRadioButton)
                    .add(inputRadioButton)
                    .add(customRadioButton)
                    .add(kindLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(faceLabel)
                    .add(systemRadioButton)
                    .add(monospaceRadioButton)
                    .add(proportionalRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(largeRadioButton)
                    .add(sizeLabel)
                    .add(mediumRadioButton)
                    .add(smallRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(styleLabel)
                    .add(underlinedCheckBox)
                    .add(boldCheckBox)
                    .add(plainCheckBox)
                    .add(italicCheckBox))
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
        );

        layout.linkSize(new java.awt.Component[] {boldCheckBox, customRadioButton, defaultRadioButton, faceLabel, inputRadioButton, italicCheckBox, kindLabel, largeRadioButton, mediumRadioButton, monospaceRadioButton, plainCheckBox, proportionalRadioButton, sizeLabel, smallRadioButton, staticRadioButton, styleLabel, systemRadioButton, underlinedCheckBox}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(kindLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(defaultRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(staticRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(inputRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(customRadioButton))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(faceLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(systemRadioButton)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(monospaceRadioButton)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(proportionalRadioButton))
                        .add(layout.createSequentialGroup()
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(sizeLabel)
                                .add(styleLabel))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(smallRadioButton)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(mediumRadioButton)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(largeRadioButton))
                        .add(layout.createSequentialGroup()
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createSequentialGroup()
                                    .add(20, 20, 20)
                                    .add(plainCheckBox)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(boldCheckBox))
                                .add(layout.createSequentialGroup()
                                    .add(62, 62, 62)
                                    .add(italicCheckBox)))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(underlinedCheckBox))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox boldCheckBox;
    private javax.swing.JRadioButton customRadioButton;
    private javax.swing.JRadioButton defaultRadioButton;
    private javax.swing.ButtonGroup faceButtonGroup;
    private javax.swing.JLabel faceLabel;
    private javax.swing.JRadioButton inputRadioButton;
    private javax.swing.JCheckBox italicCheckBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.ButtonGroup kindButtonGroup;
    private javax.swing.JLabel kindLabel;
    private javax.swing.JRadioButton largeRadioButton;
    private javax.swing.JRadioButton mediumRadioButton;
    private javax.swing.JRadioButton monospaceRadioButton;
    private javax.swing.JCheckBox plainCheckBox;
    private javax.swing.JRadioButton proportionalRadioButton;
    private javax.swing.JLabel sampleLabel;
    private javax.swing.ButtonGroup sizeButtonGroup;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JRadioButton smallRadioButton;
    private javax.swing.JRadioButton staticRadioButton;
    private javax.swing.JLabel styleLabel;
    private javax.swing.JRadioButton systemRadioButton;
    private javax.swing.JCheckBox underlinedCheckBox;
    // End of variables declaration//GEN-END:variables
}
