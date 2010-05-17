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

/*
 * ElementCustomizer.java
 *
 * Created on January 17, 2006, 10:26 PM
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import java.io.IOException;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.ui.basic.editors.SchemaComponentSelectionPanel;
import org.netbeans.modules.xml.xam.ui.customizer.MessageDisplayer;

/**
 * Element customizer
 *
 * @author  Ajit Bhate
 */
abstract class ElementCustomizer<T extends Element>
        extends AbstractSchemaComponentCustomizer<T>
        implements PropertyChangeListener {
    
    static final long serialVersionUID = 1L;
    
    /**
     * Creates new form ElementCustomizer
     */
    public ElementCustomizer(SchemaComponentReference<T> reference,
            SchemaComponent parent) {
        super(reference, parent);
        initComponents();
        reset();
    }
    
    public void applyChanges() throws IOException {
        saveName();
        if(_getType()!= getUIType() || _getStyle() != getUIStyle()) {
            setModelType();
        }
    }
    
    public void reset() {
        removeListeners();
        initializeModel();
        initializeUISelection();
        addListeners();
        if(hasParent()) {
            setSaveEnabled(false);
        } else {
            setSaveEnabled(true);
        }
        setResetEnabled(false);
    }
    
    /**
     * Returns current name of the element
     */
    protected String _getName() {
        String retValue = super._getName();
        return retValue==null?"":retValue;
    }
    
    /**
     * Returns current type of the element
     */
    private GlobalType _getType() {
        return currentGlobalType;
    }
    
    /**
     * Returns current style of the element
     */
    private ElementTypeStyle _getStyle() {
        return currentStyle;
    }
    
    /**
     * initializes type variable
     */
    protected void _setType(GlobalType type) {
        currentGlobalType = type;
        currentStyle = ElementTypeStyle.EXISTING;
    }
    
    /**
     * extracts type and style from local type and initializes type and style variables
     */
    protected void _setType(LocalType localType) {
        currentGlobalType = null;
        currentStyle = ElementTypeStyle.NO_TYPE;
        if(localType instanceof LocalSimpleType) {
            currentStyle = ElementTypeStyle.ANONYMOUS_SIMPLE;
        } else if(localType instanceof LocalComplexType) {
            currentStyle = ElementTypeStyle.ANONYMOUS_COMPLEX;
        }
    }
    
    /**
     * initializes non ui elements
     */
    protected abstract void initializeModel();
    
    /**
     * Changes the type of element
     *
     */
    protected abstract void setModelType();
    
    /**
     * selects model node on ui
     */
    private void selectModelNode() {
//		componentSelectionPanel.removePropertyChangeListener(this);
        componentSelectionPanel.setInitialSelection(_getType());
//		componentSelectionPanel.addPropertyChangeListener(this);
    }
    
    /**
     *
     *
     */
    private void initializeTypeView() {
        componentSelectionPanel = new SchemaComponentSelectionPanel<GlobalType>(
                getReference().get().getModel(),GlobalType.class, null, null, true);
        componentSelectionPanel.addPropertyChangeListener(this);
        typePanel.add(componentSelectionPanel.getTypeSelectionPanel(),
                java.awt.BorderLayout.CENTER);
        componentSelectionPanel.getTypeSelectionPanel().getAccessibleContext().
                setAccessibleParent(typePanel);
    }
    
    private void initializeUISelection() {
        getMessageDisplayer().clear();
        nameTextField.getDocument().removeDocumentListener(nameListener);
        nameTextField.setText(_getName());
        if(!hasParent()) nameTextField.setSelectionStart(0);
        nameTextField.getDocument().addDocumentListener(nameListener);
        ElementTypeStyle style = _getStyle();
        ItemListener[] listeners;
        switch(style) {
            case NO_TYPE:
                noTypeRadioButton.setSelected(true);
                break;
            case ANONYMOUS_COMPLEX:
                anonymousComplexRadioButton.setSelected(true);
                break;
            case ANONYMOUS_SIMPLE:
                anonymousSimpleRadioButton.setSelected(true);
                break;
            case EXISTING:
                existingRadioButton.setSelected(true);
                break;
        }
        enableDisableListView();
        if(style==ElementTypeStyle.EXISTING) {
            selectModelNode();
        }
        setPreviewText();
    }
    
    protected LocalType createLocalType(SchemaComponentFactory factory,
            ElementTypeStyle style, GlobalType reference) {
        switch (style) {
            case ANONYMOUS_COMPLEX :
                LocalComplexType lct = factory.createLocalComplexType();
                Sequence sequence = factory.createSequence();
                lct.setDefinition(sequence);
                return lct;
            case ANONYMOUS_SIMPLE :
                LocalSimpleType lst = factory.createLocalSimpleType();
                SimpleTypeRestriction str = factory.createSimpleTypeRestriction();
                str.setBase(ElementCustomizer.createStringTypeReference(factory,str));
                lst.setDefinition(str);
                return lst;
        }
        return null;
    }
    
    /**
     * Retrieve the selected name from the UI.
     *
     * @return  name from UI(nameTextField).
     */
    protected String getUIName() {
        return nameTextField.getText();
    }
    
    /**
     * Retrieve the selected type from the UI.
     *
     * @return  global type from UI, either simple or complex.
     */
    protected GlobalType getUIType() {
        ElementTypeStyle style = getUIStyle();
        if(style == ElementTypeStyle.NO_TYPE ||
                style == ElementTypeStyle.ANONYMOUS_COMPLEX ||
                style == ElementTypeStyle.ANONYMOUS_SIMPLE) {
            return null;
        }
        return componentSelectionPanel.getCurrentSelection();
    }
    
    /**
     * Retrieve the selected style from the UI.
     *
     */
    protected ElementTypeStyle getUIStyle() {
        if (noTypeRadioButton.isSelected())
            return ElementTypeStyle.NO_TYPE;
        
        if (anonymousComplexRadioButton.isSelected())
            return ElementTypeStyle.ANONYMOUS_COMPLEX;
        
        if (anonymousSimpleRadioButton.isSelected())
            return ElementTypeStyle.ANONYMOUS_SIMPLE;
        
        if (existingRadioButton.isSelected())
            return ElementTypeStyle.EXISTING;
        
        return null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Enum
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     *
     *
     */
    enum ElementTypeStyle {
        NO_TYPE, ANONYMOUS_COMPLEX, ANONYMOUS_SIMPLE, EXISTING
    }
    
    /**
     * This method is called from within the constructor to
     * initializeTypeView the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        elementTypeButtonGroup = new javax.swing.ButtonGroup();
        previewLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        previewPane = new javax.swing.JEditorPane() {
            static final long serialVersionUID = 1L;
            // disable mouse and mouse motion events
            protected void processMouseEvent(java.awt.event.MouseEvent e) {
                e.consume();
            }
            protected void processMouseMotionEvent(java.awt.event.MouseEvent e) {
                e.consume();
            }
        };
        typeLabel = new javax.swing.JLabel();
        anonymousComplexRadioButton = new javax.swing.JRadioButton();
        anonymousSimpleRadioButton = new javax.swing.JRadioButton();
        noTypeRadioButton = new javax.swing.JRadioButton();
        existingRadioButton = new javax.swing.JRadioButton();
        typePanel = new javax.swing.JPanel();
        mPanel = new javax.swing.JPanel();
        namePanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();

        previewLabel.setLabelFor(previewPane);
        org.openide.awt.Mnemonics.setLocalizedText(previewLabel, org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "TITLE_CustomizerForm_Preview"));
        previewLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "HINT_ElementForm_Preview"));

        previewPane.setEditable(false);
        previewPane.setContentType("text/xml");
        previewPane.setEnabled(false);
        jScrollPane1.setViewportView(previewPane);

        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "LBL_CustomizerForm_Type", new Object[] {}));
        typeLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "HINT_ElementForm_Type"));

        elementTypeButtonGroup.add(anonymousComplexRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(anonymousComplexRadioButton, org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "LBL_ElementForm_AnonComplexType"));
        anonymousComplexRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "HINT_ElementForm_ComplexType"));
        anonymousComplexRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        anonymousComplexRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        elementTypeButtonGroup.add(anonymousSimpleRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(anonymousSimpleRadioButton, org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "LBL_ElementForm_AnonSimpleType"));
        anonymousSimpleRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "HINT_ElementForm_SimpleType"));
        anonymousSimpleRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        anonymousSimpleRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        elementTypeButtonGroup.add(noTypeRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(noTypeRadioButton, org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "LBL_ElementForm_NoType"));
        noTypeRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "HINT_ElementForm_NoType"));
        noTypeRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noTypeRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        elementTypeButtonGroup.add(existingRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(existingRadioButton, org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "LBL_ElementForm_ExistingType"));
        existingRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "HINT_ElementForm_ExistingType"));
        existingRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        existingRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        typePanel.setLayout(new java.awt.BorderLayout());

        initializeTypeView();

        mPanel.setLayout(new java.awt.BorderLayout());

        mPanel.add(getMessageDisplayer().getComponent(),java.awt.BorderLayout.CENTER);

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "LBL_CustomizerForm_Name"));
        nameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ElementCustomizer.class, "HINT_ElementForm_Name"));

        nameTextField.setEditable(!hasParent());

        org.jdesktop.layout.GroupLayout namePanelLayout = new org.jdesktop.layout.GroupLayout(namePanel);
        namePanel.setLayout(namePanelLayout);
        namePanelLayout.setHorizontalGroup(
            namePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(namePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(nameLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
                .addContainerGap())
        );
        namePanelLayout.setVerticalGroup(
            namePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(namePanelLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(namePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(typeLabel)
                .addContainerGap(462, Short.MAX_VALUE))
            .add(namePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                    .add(previewLabel)
                    .add(layout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(anonymousSimpleRadioButton)
                            .add(anonymousComplexRadioButton)
                            .add(noTypeRadioButton)
                            .add(existingRadioButton))))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(38, 38, 38)
                .add(typePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                .addContainerGap())
            .add(mPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(namePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(11, 11, 11)
                .add(typeLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(anonymousComplexRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(anonymousSimpleRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(noTypeRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(existingRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(typePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                .add(11, 11, 11)
                .add(previewLabel)
                .add(5, 5, 5)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(mPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    ////////////////////////
    // event handling
    ////////////////////////
    /**
     * Since it implements PCL.
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(SchemaComponentSelectionPanel.PROPERTY_SELECTION)) {
            setPreviewText();
            determineValidity();
        }
    }
    
    private void enableDisableListView() {
        switch (getUIStyle()) {
            case NO_TYPE:
            case ANONYMOUS_COMPLEX:
            case ANONYMOUS_SIMPLE:
                componentSelectionPanel.setEnabled(false);
                break;
            case EXISTING:
                componentSelectionPanel.setEnabled(true);
                break;
        }
    }
    
    
    /**
     * Based on the current radio button status and node selections, decide
     * if we are in a valid state for accepting the user's input.
     */
    private void determineValidity() {
        getMessageDisplayer().clear();
        if(!isNameChanged() &&
                _getStyle() == getUIStyle() &&
                _getType() == getUIType()) {
            if(hasParent()) {
                setSaveEnabled(false);
            } else {
                setSaveEnabled(true);
            }
            setResetEnabled(false);
        } else {
            setResetEnabled(true);
            boolean valid = false;
            ElementTypeStyle style = getUIStyle();
            if(style == ElementTypeStyle.NO_TYPE ||
                    style == ElementTypeStyle.ANONYMOUS_COMPLEX ||
                    style == ElementTypeStyle.ANONYMOUS_SIMPLE) {
                valid = true;
            } else if (getUIType()!=null) {
                valid = true;
            }
            setSaveEnabled(valid && (!isNameChanged() || isNameValid()));
            if(!valid)
                getMessageDisplayer().annotate(org.openide.util.NbBundle.
                        getMessage(ElementCustomizer.class,
                        "MSG_Type_Error"),
                        MessageDisplayer.Type.ERROR);
        }
    }
    
    private void addListeners() {
        if (nameListener == null) {
            nameListener = new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    setPreviewText();
                    determineValidity();
                }
                public void insertUpdate(DocumentEvent e) {
                    setPreviewText();
                    determineValidity();
                }
                public void removeUpdate(DocumentEvent e) {
                    setPreviewText();
                    determineValidity();
                }
            };
        }
        nameTextField.getDocument().addDocumentListener(nameListener);
        if(typeButtonsListener == null) {
            typeButtonsListener = new ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                        enableDisableListView();
                        setPreviewText();
                        determineValidity();
                    }
                }
            };
        }
        noTypeRadioButton.addItemListener(typeButtonsListener);
        anonymousComplexRadioButton.addItemListener(typeButtonsListener);
        anonymousSimpleRadioButton.addItemListener(typeButtonsListener);
        existingRadioButton.addItemListener(typeButtonsListener);
    }
    
    private void removeListeners() {
        nameTextField.getDocument().removeDocumentListener(nameListener);
        noTypeRadioButton.removeItemListener(typeButtonsListener);
        anonymousComplexRadioButton.removeItemListener(typeButtonsListener);
        anonymousSimpleRadioButton.removeItemListener(typeButtonsListener);
        existingRadioButton.removeItemListener(typeButtonsListener);
    }
    
    
    private void setPreviewText() {
        StringBuffer retValue = new StringBuffer("<");
        String prefix = getReference().get().getPeer().getPrefix();
        if(prefix!=null) retValue.append(prefix+":");
        retValue.append("element name=\"");
        retValue.append(getUIName());
        retValue.append("\"");
        switch(getUIStyle()) {
            case NO_TYPE:
                retValue.append(">");
                break;
            case EXISTING:
                retValue.append(" type=\"");
                GlobalType gt = getUIType();
                if(gt!=null) {
                    retValue.append(gt.getName());
                }
                retValue.append("\">");
                break;
            case ANONYMOUS_COMPLEX:
                retValue.append(">");
                retValue.append("\n    <");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("complexType>");
                retValue.append("\n        <");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("sequence/>");
                retValue.append("\n    </");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("complexType>");
                break;
            case ANONYMOUS_SIMPLE:
                retValue.append(">");
                retValue.append("\n    <");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("simpleType>");
                retValue.append("\n        <");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("restriction base=\"");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("string\"/>");
                retValue.append("\n    </");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("simpleType>");
                break;
        }
        retValue.append("\n</");
        if(prefix!=null) retValue.append(prefix+":");
        retValue.append("element>\n");
        previewPane.setText(retValue.toString());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JRadioButton anonymousComplexRadioButton;
    public javax.swing.JRadioButton anonymousSimpleRadioButton;
    public javax.swing.ButtonGroup elementTypeButtonGroup;
    public javax.swing.JRadioButton existingRadioButton;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JPanel mPanel;
    public javax.swing.JLabel nameLabel;
    public javax.swing.JPanel namePanel;
    public javax.swing.JTextField nameTextField;
    public javax.swing.JRadioButton noTypeRadioButton;
    public javax.swing.JLabel previewLabel;
    public javax.swing.JEditorPane previewPane;
    public javax.swing.JLabel typeLabel;
    public javax.swing.JPanel typePanel;
    // End of variables declaration//GEN-END:variables
    
    // creates primitive string global reference
    static NamedComponentReference<GlobalSimpleType>
            createStringTypeReference(SchemaComponentFactory factory,
            SchemaComponent component) {
        Collection<GlobalSimpleType> types = SchemaModelFactory.getDefault().
                getPrimitiveTypesModel().getSchema().getSimpleTypes();
        for (GlobalSimpleType type : types) {
            if (type.getName().equals("string")) {
                return factory.createGlobalReference(type,
                        GlobalSimpleType.class, component);
            }
        }
        return null;
    }
    
    private DocumentListener nameListener;
    private ItemListener typeButtonsListener;
    private transient ElementTypeStyle currentStyle;
    private transient GlobalType currentGlobalType;
    private transient SchemaComponentSelectionPanel<GlobalType>
            componentSelectionPanel;
}
