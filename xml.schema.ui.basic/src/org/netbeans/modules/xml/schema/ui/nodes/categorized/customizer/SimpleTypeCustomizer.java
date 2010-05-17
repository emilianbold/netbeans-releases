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
 * AttributeCustomizer.java
 *
 * Created on January 17, 2006, 10:26 PM
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.xml.xam.ui.customizer.MessageDisplayer;
import org.openide.util.HelpCtx;

import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.List;
import org.netbeans.modules.xml.schema.model.Union;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleTypeDefinition;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.ui.basic.editors.SchemaComponentSelectionPanel;

/**
 * Attribute customizer
 *
 * @author  Ajit Bhate
 */
public class SimpleTypeCustomizer<T extends SimpleType>
        extends AbstractSchemaComponentCustomizer<T>
        implements PropertyChangeListener {
    
    static final long serialVersionUID = 1L;
    
    /**
     * Creates new form AttributeCustomizer
     */
    public SimpleTypeCustomizer(SchemaComponentReference<T> reference) {
        this(reference,null,null);
    }
    
    /** Creates new form SimpleTypeCustomizer */
    public SimpleTypeCustomizer(SchemaComponentReference<T> reference,
            SchemaComponent parent, GlobalSimpleType currentGlobalSimpleType) {
        super(reference, parent);
        this.currentGlobalSimpleType = currentGlobalSimpleType;
        initComponents();
        reset();
    }
    
    public void applyChanges() throws IOException {
        SimpleType type = getReference().get();
        saveName();
        DerivationType uiDType = getUIDerivationType();
        TypeDefinition uiTDef = getUITypeDefinition();
        switch (uiDType) {
            case RESTRICTION:
            case LIST:
                if(uiTDef == TypeDefinition.EXISTING && getUIType()==null)
                    break;
                if(uiDType != _getDerivationType() ||
                        uiTDef != _getTypeDefinition() || getUIType() != _getType())
                    setModelType();
                break;
            case UNION:
                if(uiDType != _getDerivationType())
                    setModelType();
                break;
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
     * initializes non ui elements from model
     */
    private void initializeModel() {
        SimpleType type = getReference().get();
        SimpleTypeDefinition definition = type.getDefinition();
        if (definition instanceof SimpleTypeRestriction) {
            derivation = DerivationType.RESTRICTION;
            SimpleTypeRestriction str = (SimpleTypeRestriction)definition;
            if(str.getBase()!=null) {
                if(hasParent())
                    currentGlobalSimpleType = str.getBase().get();
                typeDef = TypeDefinition.EXISTING;
            } else if(str.getInlineType()!=null)
                typeDef = TypeDefinition.INLINE;
        } else if (definition instanceof List) {
            derivation = DerivationType.LIST;
            List list = (List)definition;
            if(list.getType()!=null) {
                currentGlobalSimpleType = list.getType().get();
                typeDef = TypeDefinition.EXISTING;
            } else if(list.getInlineType()!=null)
                typeDef = TypeDefinition.INLINE;
        } else if(definition instanceof Union) {
            derivation = DerivationType.UNION;
            Union u = (Union)definition;
        }
    }
    
    /**
     * Initializes UI from model values
     */
    private void initializeUISelection() {
        getMessageDisplayer().clear();
        if(isNameable()) {
            nameTextField.setText(_getName());
            if(!hasParent()) nameTextField.setSelectionStart(0);
        } else {
            namePanel.setVisible(false);
        }
        DerivationType dType = _getDerivationType();
        if(dType!=null) {
            switch(dType) {
                case RESTRICTION:
                    restrictionButton.setSelected(true);
                    break;
                case LIST:
                    listButton.setSelected(true);
                    break;
                case UNION:
                    unionButton.setSelected(true);
                    break;
            }
        }
        TypeDefinition type = _getTypeDefinition();
        if(type!=null) {
            switch(type) {
                case EXISTING:
                    useExistingButton.setSelected(true);
                    break;
                case INLINE:
                    inlineTypeButton.setSelected(true);
                    break;
            }
        }
        switchTypePanels();
        selectModelNode();
        setPreviewText();
    }
    
    private void addListeners() {
        if(nameListener == null) {
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
        if (buttonListener ==null) {
            buttonListener = new ItemListener() {
                public void itemStateChanged(ItemEvent evt) {
                    Object source = evt.getSource();
                    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                        switchTypePanels();
                        setPreviewText();
                        determineValidity();
                    }
                }
            };
        }
        restrictionButton.addItemListener(buttonListener);
        listButton.addItemListener(buttonListener);
        unionButton.addItemListener(buttonListener);
        useExistingButton.addItemListener(buttonListener);
        inlineTypeButton.addItemListener(buttonListener);
    }
    
    private void removeListeners() {
        nameTextField.getDocument().removeDocumentListener(nameListener);
    }
    
    /**
     * enables disables the inner base type panel and inner content combo box.
     */
    private void switchTypePanels() {
        boolean enableTypeDefPanel = getUIDerivationType()!=DerivationType.UNION;
        typeDefPanel.setEnabled(enableTypeDefPanel);
        useExistingButton.setEnabled(enableTypeDefPanel);
        inlineTypeButton.setEnabled(enableTypeDefPanel);
        if(enableTypeDefPanel && getUITypeDefinition()==null)
            useExistingButton.setSelected(true);
        boolean enableTypePanel = enableTypeDefPanel && useExistingButton.isSelected();
        componentSelectionPanel.setEnabled(enableTypePanel);
    }
    
    /**
     * selects model node on ui
     */
    private void selectModelNode() {
        componentSelectionPanel.setInitialSelection(_getType());
    }
    
    /**
     * Returns current type initialized from model
     */
    private GlobalSimpleType _getType() {
        return currentGlobalSimpleType;
    }
    
    private DerivationType _getDerivationType() {
        return derivation;
    }
    
    private TypeDefinition _getTypeDefinition() {
        return typeDef;
    }
    
    /**
     * Retrieve the selected name from the UI.
     *
     * @return  name from UI(nameTextField).
     */
    protected String getUIName() {
        return nameTextField.getText();
    }
    
    private DerivationType getUIDerivationType() {
        if(restrictionButton.isSelected())
            return DerivationType.RESTRICTION;
        if(listButton.isSelected())
            return DerivationType.LIST;
        if(unionButton.isSelected())
            return DerivationType.UNION;
        return null;
    }
    
    private TypeDefinition getUITypeDefinition() {
        if(getUIDerivationType()==DerivationType.UNION) return null;
        if(useExistingButton.isSelected())
            return TypeDefinition.EXISTING;
        if(inlineTypeButton.isSelected())
            return TypeDefinition.INLINE;
        return null;
    }
    
    /**
     * Retrieve the selected type from the UI.
     *
     * @return  global type from UI, either simple or complex.
     */
    private GlobalSimpleType getUIType() {
        if(getUIDerivationType()==DerivationType.UNION) return null;
        if(getUITypeDefinition()==TypeDefinition.INLINE) return null;
        return componentSelectionPanel.getCurrentSelection();
    }
    
    private void setModelType() {
        SimpleType type = getReference().get();
        SimpleTypeDefinition definition = type.getDefinition();
        SchemaComponentFactory factory = type.getModel().getFactory();
        GlobalSimpleType gst = getUIType();
        LocalSimpleType inlineType = null;
        if (definition instanceof SimpleTypeRestriction) {
            inlineType = ((SimpleTypeRestriction)definition).getInlineType();
        } else if (definition instanceof List) {
            inlineType = ((List)definition).getInlineType();
        }
        switch(getUIDerivationType()) {
            case RESTRICTION:
                SimpleTypeRestriction str;
                if (definition instanceof SimpleTypeRestriction) {
                    str = (SimpleTypeRestriction)definition;
                    if(gst!=null) {
                        if(inlineType!=null) str.setInlineType(null);
                        str.setBase(factory.createGlobalReference(
                                gst, GlobalSimpleType.class, str));
                    } else {
                        if(str.getBase()!=null) str.setBase(null);
                        if(inlineType==null) {
                            str.setInlineType(createLocalSimpleType(factory));
                        }
                    }
                } else {
                    str = factory.createSimpleTypeRestriction();
                    if(gst!=null) {
                        str.setBase(factory.createGlobalReference(
                                gst, GlobalSimpleType.class, str));
                    } else if(inlineType!=null) {
                        str.setInlineType((LocalSimpleType)inlineType.copy(str));
                    } else {
                        str.setInlineType(createLocalSimpleType(factory));
                    }
                    type.setDefinition(str);
                }
                break;
            case LIST:
                List list;
                if (definition instanceof List) {
                    list = (List)definition;
                    if(gst!=null) {
                        if(inlineType!=null) list.setInlineType(null);
                        list.setType(factory.createGlobalReference(
                                gst, GlobalSimpleType.class, list));
                    } else {
                        if(list.getType()!=null) list.setType(null);
                        if(inlineType==null) {
                            list.setInlineType(createLocalSimpleType(factory));
                        }
                    }
                } else {
                    list = factory.createList();
                    if(gst!=null) {
                        list.setType(factory.createGlobalReference(
                                gst, GlobalSimpleType.class, list));
                    } else if(inlineType!=null) {
                        list.setInlineType((LocalSimpleType)inlineType.copy(list));
                    } else {
                        list.setInlineType(createLocalSimpleType(factory));
                    }
                    type.setDefinition(list);
                }
                break;
            case UNION:
                Union u;
                if (definition instanceof Union) {
                    // TODO
                } else {
                    u = factory.createUnion();
                    type.setDefinition(u);
                }
                //TODO save membertypes
        }
    }
    
    private LocalSimpleType createLocalSimpleType
            (final SchemaComponentFactory factory) {
        LocalSimpleType lst = factory.createLocalSimpleType();
        SimpleTypeRestriction str = factory.createSimpleTypeRestriction();
        str.setBase(ElementCustomizer.createStringTypeReference(factory,str));
        lst.setDefinition(str);
        return lst;
    }
    
    /**
     *
     *
     */
    private void initializeTypeView() {
        componentSelectionPanel = new SchemaComponentSelectionPanel<GlobalSimpleType>(
                getReference().get().getModel(),GlobalSimpleType.class,
                null, Collections.singleton(getReference().get()), true);
        componentSelectionPanel.addPropertyChangeListener(this);
        typePanel.add(componentSelectionPanel.getTypeSelectionPanel(),java.awt.BorderLayout.CENTER);
        componentSelectionPanel.getTypeSelectionPanel().getAccessibleContext().
                setAccessibleParent(typePanel);
    }
    
    /**
     * Since it implements PCL.
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(SchemaComponentSelectionPanel.PROPERTY_SELECTION)) {
            setPreviewText();
            determineValidity();
        }
    }
    
    /**
     * Based on the current radio button status and node selections, decide
     * if we are in a valid state for accepting the user's input.
     */
    private void determineValidity() {
        getMessageDisplayer().clear();
        boolean nameChanged = isNameable()&&isNameChanged();
        if(!nameChanged && getUIDerivationType()==_getDerivationType() &&
                getUITypeDefinition()==_getTypeDefinition() &&
                getUIType()== _getType()) {
            if(hasParent()) {
                setSaveEnabled(false);
            } else {
                setSaveEnabled(true);
            }
            setResetEnabled(false);
            return;
        } else {
            setResetEnabled(true);
            boolean valid = false;
            DerivationType uiDType = getUIDerivationType();
            TypeDefinition uiTDef = getUITypeDefinition();
            switch (uiDType) {
                case RESTRICTION:
                case LIST:
                    if(uiTDef == TypeDefinition.EXISTING && getUIType()!=null ||
                            uiTDef == TypeDefinition.INLINE)
                        valid = true;
                    break;
                case UNION:
                    valid = true;
                    break;
            }
            setSaveEnabled(valid && (!isNameChanged() || isNameValid()));
            if(!valid)
                getMessageDisplayer().annotate(org.openide.util.NbBundle.
                        getMessage(SimpleTypeCustomizer.class,
                        "MSG_Type_Error"),
                        MessageDisplayer.Type.ERROR);
        }
    }
    
    private String getInlineTypePreviewText() {
        StringBuffer retValue = new StringBuffer("\n        <");
        String prefix = getReference().get().getPeer().getPrefix();
        if(prefix!=null) retValue.append(prefix+":");
        retValue.append("simpleType>");
        retValue.append("\n            <");
        if(prefix!=null) retValue.append(prefix+":");
        retValue.append("restriction base=\"");
        if(prefix!=null) retValue.append(prefix+":");
        retValue.append("string\">");
        retValue.append("\n        </");
        if(prefix!=null) retValue.append(prefix+":");
        retValue.append("simpleType>");
        return retValue.toString();
    }
    
    private void setPreviewText() {
        StringBuffer retValue = new StringBuffer("<");
        String prefix = getReference().get().getPeer().getPrefix();
        if(prefix!=null) retValue.append(prefix+":");
        retValue.append("simpleType");
        if(isNameable()) {
            retValue.append(" name=\"");
            retValue.append(getUIName());
            retValue.append("\"");
        }
        retValue.append(">");
        DerivationType dt = getUIDerivationType();
        TypeDefinition t = getUITypeDefinition();
        GlobalSimpleType gst = getUIType();
        if(dt!=null) {
            switch(dt) {
                case RESTRICTION:
                    retValue.append("\n    <");
                    if(prefix!=null) retValue.append(prefix+":");
                    retValue.append("restriction");
                    if(t==TypeDefinition.EXISTING) {
                        retValue.append(" base=\"");
                        if(gst!=null)
                            retValue.append(gst.getName());
                        retValue.append("\"/>");
                    } else if(t==TypeDefinition.INLINE) {
                        retValue.append(">");
                        retValue.append(getInlineTypePreviewText());
                        retValue.append("\n    </");
                        if(prefix!=null) retValue.append(prefix+":");
                        retValue.append("restriction>");
                    } else {
                        retValue.append("/>");
                    }
                    break;
                case LIST:
                    retValue.append("\n    <");
                    if(prefix!=null) retValue.append(prefix+":");
                    retValue.append("list");
                    if(t==TypeDefinition.EXISTING) {
                        retValue.append(" itemType=\"");
                        if(gst!=null)
                            retValue.append(gst.getName());
                        retValue.append("\"/>");
                    } else if(t==TypeDefinition.INLINE) {
                        retValue.append(">");
                        retValue.append(getInlineTypePreviewText());
                        retValue.append("\n    </");
                        if(prefix!=null) retValue.append(prefix+":");
                        retValue.append("list>");
                    } else {
                        retValue.append("/>");
                    }
                    break;
                case UNION:
                    retValue.append("\n    <");
                    if(prefix!=null) retValue.append(prefix+":");
                    retValue.append("union/>");
                    break;
            }
        }
        retValue.append("\n</");
        if(prefix!=null) retValue.append(prefix+":");
        retValue.append("simpleType>\n");
        previewPane.setText(retValue.toString());
        
    }
    
    /**
     * This method is called from within the constructor to
     * initializeTypeView the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        derivationGroup = new javax.swing.ButtonGroup();
        typeGroup = new javax.swing.ButtonGroup();
        typeDefPanel = new javax.swing.JPanel();
        typePanel = new javax.swing.JPanel();
        useExistingButton = new javax.swing.JRadioButton();
        inlineTypeButton = new javax.swing.JRadioButton();
        baseTypeLabel = new javax.swing.JLabel();
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
        restrictionButton = new javax.swing.JRadioButton();
        listButton = new javax.swing.JRadioButton();
        unionButton = new javax.swing.JRadioButton();
        mPanel = new javax.swing.JPanel();
        namePanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();

        setToolTipText("");
        typePanel.setLayout(new java.awt.BorderLayout());

        initializeTypeView();

        typeGroup.add(useExistingButton);
        org.openide.awt.Mnemonics.setLocalizedText(useExistingButton, org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "LBL_SimpleTypeForm_Existing_Definition_Button"));
        useExistingButton.setToolTipText(org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "HINT_SimpleTypeForm_Existing_Definition_Button"));
        useExistingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        typeGroup.add(inlineTypeButton);
        org.openide.awt.Mnemonics.setLocalizedText(inlineTypeButton, org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "LBL_SimpleTypeForm_Inline_Definition_Button"));
        inlineTypeButton.setToolTipText(org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "HINT_SimpleTypeForm_Inline_Definition_Button"));
        inlineTypeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(baseTypeLabel, org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "TITLE_SimpleTypeForm_BaseTypePanel", new Object[] {}));
        baseTypeLabel.setToolTipText(org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "HINT_SimpleTypeForm_BaseTypePanel"));

        org.jdesktop.layout.GroupLayout typeDefPanelLayout = new org.jdesktop.layout.GroupLayout(typeDefPanel);
        typeDefPanel.setLayout(typeDefPanelLayout);
        typeDefPanelLayout.setHorizontalGroup(
            typeDefPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(typeDefPanelLayout.createSequentialGroup()
                .add(typeDefPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(baseTypeLabel)
                    .add(typeDefPanelLayout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(typeDefPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(inlineTypeButton)
                            .add(useExistingButton)))
                    .add(typeDefPanelLayout.createSequentialGroup()
                        .add(30, 30, 30)
                        .add(typePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)))
                .addContainerGap())
        );
        typeDefPanelLayout.setVerticalGroup(
            typeDefPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(typeDefPanelLayout.createSequentialGroup()
                .add(baseTypeLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(inlineTypeButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(useExistingButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(typePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE))
        );

        previewLabel.setLabelFor(previewPane);
        org.openide.awt.Mnemonics.setLocalizedText(previewLabel, org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "TITLE_CustomizerForm_Preview"));
        previewLabel.setToolTipText(org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "HINT_SimpleTypeForm_Preview"));

        previewPane.setEditable(false);
        previewPane.setContentType("text/xml");
        previewPane.setEnabled(false);
        jScrollPane1.setViewportView(previewPane);

        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "TITLE_SimpleTypeForm_DerivationTypePanel", new Object[] {}));
        typeLabel.setToolTipText(org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "HINT_SimpleTypeForm_DerivationTypePanel"));

        derivationGroup.add(restrictionButton);
        org.openide.awt.Mnemonics.setLocalizedText(restrictionButton, org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "LBL_SimpleTypeForm_Restriction_Button"));
        restrictionButton.setToolTipText(org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "HINT_SimpleTypeForm_Restriction_Button"));
        restrictionButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        derivationGroup.add(listButton);
        org.openide.awt.Mnemonics.setLocalizedText(listButton, org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "LBL_SimpleTypeForm_List_Button"));
        listButton.setToolTipText(org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "HINT_SimpleTypeForm_List_Button"));
        listButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        derivationGroup.add(unionButton);
        org.openide.awt.Mnemonics.setLocalizedText(unionButton, org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "LBL_SimpleTypeForm_Union_Button"));
        unionButton.setToolTipText(org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "HINT_SimpleTypeForm_Union_Button"));
        unionButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mPanel.setLayout(new java.awt.BorderLayout());

        mPanel.add(getMessageDisplayer().getComponent(),java.awt.BorderLayout.CENTER);

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "LBL_CustomizerForm_Name"));
        nameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(SimpleTypeCustomizer.class, "HINT_SimpleTypeForm_Name"));

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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(typeLabel)
                    .add(layout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(listButton)
                            .add(restrictionButton)
                            .add(unionButton))))
                .addContainerGap(398, Short.MAX_VALUE))
            .add(namePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                    .add(previewLabel))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(typeDefPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(namePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(11, 11, 11)
                .add(typeLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(restrictionButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(listButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(unionButton)
                .add(11, 11, 11)
                .add(typeDefPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(11, 11, 11)
                .add(previewLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(mPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SimpleTypeCustomizer.class);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel baseTypeLabel;
    public javax.swing.ButtonGroup derivationGroup;
    public javax.swing.JRadioButton inlineTypeButton;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JRadioButton listButton;
    public javax.swing.JPanel mPanel;
    public javax.swing.JLabel nameLabel;
    public javax.swing.JPanel namePanel;
    public javax.swing.JTextField nameTextField;
    public javax.swing.JLabel previewLabel;
    public javax.swing.JEditorPane previewPane;
    public javax.swing.JRadioButton restrictionButton;
    public javax.swing.JPanel typeDefPanel;
    public javax.swing.ButtonGroup typeGroup;
    public javax.swing.JLabel typeLabel;
    public javax.swing.JPanel typePanel;
    public javax.swing.JRadioButton unionButton;
    public javax.swing.JRadioButton useExistingButton;
    // End of variables declaration//GEN-END:variables
    
    private DocumentListener nameListener;
    private transient ItemListener buttonListener;
    private transient GlobalSimpleType currentGlobalSimpleType;
    private transient DerivationType derivation;
    private transient TypeDefinition typeDef;
    private transient SchemaComponentSelectionPanel<GlobalSimpleType>
            componentSelectionPanel;
    private enum DerivationType {RESTRICTION,LIST,UNION};
    private enum TypeDefinition {EXISTING,INLINE};
    
}
