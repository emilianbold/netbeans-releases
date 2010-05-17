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

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalAttributeContainer;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.ui.basic.editors.SchemaComponentSelectionPanel;
import org.netbeans.modules.xml.xam.ui.customizer.MessageDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Ajit Bhate
 */
public class ComplexTypeCustomizer<T extends ComplexType>
        extends AbstractSchemaComponentCustomizer<T>
        implements PropertyChangeListener {
    
    static final long serialVersionUID = 1L;
    /** Creates new form ComplexTypeCustomizer */
    public ComplexTypeCustomizer(SchemaComponentReference<T> reference,
            SchemaComponent parent) {
        super(reference, parent);
        initComponents();
        reset();
    }
    
    /** Creates new form ComplexTypeCustomizer */
    public ComplexTypeCustomizer(SchemaComponentReference<T> reference) {
        this(reference,null);
    }
    
    /**
     * non UI component (model) initialization and access methods
     */
    
    /**
     * Initializes non ui from model
     */
    private void initialize() {
        ComplexType type = getSchemaComponent();
        if(type==null) return;
        ComplexTypeDefinition definition =type.getDefinition();
        contentType = ContentType.NoType;
        if(definition instanceof All) {
            contentType = ContentType.All;
        } else if(definition instanceof Choice) {
            contentType = ContentType.Choice;
        } else if(definition instanceof Sequence) {
            contentType = ContentType.Sequence;
        } else if(definition instanceof GroupReference) {
            contentType = ContentType.Group;
            gRef = ((GroupReference)definition).getRef().get();
        } else if(definition instanceof ComplexContent) {
            ComplexContent cc = (ComplexContent)definition;
            if(cc.getLocalDefinition() instanceof ComplexContentRestriction) {
                contentType = ContentType.Restriction;
                ComplexContentRestriction ccr =
                        (ComplexContentRestriction)cc.getLocalDefinition();
                gRef = ccr.getBase().get();
                ComplexTypeDefinition innerDefinition = ccr.getDefinition();
                if(innerDefinition instanceof All) {
                    innerContentType = ContentType.All;
                } else if(innerDefinition instanceof Choice) {
                    innerContentType = ContentType.Choice;
                } else if(innerDefinition instanceof Sequence) {
                    innerContentType = ContentType.Sequence;
                } else if(innerDefinition instanceof GroupReference) {
                    innerContentType = ContentType.Group;
                }
            } else if(cc.getLocalDefinition() instanceof ComplexExtension) {
                contentType = ContentType.Extension;
                ComplexExtension ce =
                        (ComplexExtension)cc.getLocalDefinition();
                gRef = ce.getBase().get();
                ComplexExtensionDefinition innerDefinition = ce.getLocalDefinition();
                if(innerDefinition instanceof All) {
                    innerContentType = ContentType.All;
                } else if(innerDefinition instanceof Choice) {
                    innerContentType = ContentType.Choice;
                } else if(innerDefinition instanceof Sequence) {
                    innerContentType = ContentType.Sequence;
                } else if(innerDefinition instanceof GroupReference) {
                    innerContentType = ContentType.Group;
                } else {
                    innerContentType = ContentType.NoType;
                }
            }
        } else if(definition instanceof SimpleContent) {
            SimpleContent sc = (SimpleContent)definition;
            if(sc.getLocalDefinition() instanceof SimpleContentRestriction) {
                contentType = ContentType.Restriction;
                SimpleContentRestriction scr =
                        (SimpleContentRestriction)sc.getLocalDefinition();
                if(scr.getBase()!=null)
                    gRef = scr.getBase().get();
            } else if(sc.getLocalDefinition() instanceof SimpleExtension) {
                contentType = ContentType.Extension;
                gRef = ((SimpleExtension)sc.getLocalDefinition()).getBase().get();
            }
        }
    }
    
    private ContentType getModelContentType() {
        return contentType;
    }
    
    private ContentType getModelInnerContentType() {
        return innerContentType;
    }
    
    private ReferenceableSchemaComponent getModelReference() {
        return gRef;
    }
    
    /**
     * UI component initialization and access methods
     */
    
    /**
     * Initializes ui elements
     */
    private void initializeUI() {
        getMessageDisplayer().clear();
        if(isNameable()) {
            nameTextField.setText(_getName());
            if(!hasParent()) nameTextField.setSelectionStart(0);
        } else {
            namePanel.setVisible(false);
        }
        
        if(getModelContentType()==null) {
            inlineDefinitionButton.setSelected(true);
        } else {
            switch(getModelContentType()) {
                case NoType:
                    inlineDefinitionButton.setSelected(true);
                    noTypeButton.setSelected(true);
                    break;
                case Sequence:
                    inlineDefinitionButton.setSelected(true);
                    sequenceButton.setSelected(true);
                    break;
                case Choice:
                    inlineDefinitionButton.setSelected(true);
                    choiceButton.setSelected(true);
                    break;
                case All:
                    inlineDefinitionButton.setSelected(true);
                    allButton.setSelected(true);
                    break;
                case Group:
                    existingDefinitionButton.setSelected(true);
                    groupButton.setSelected(true);
                    break;
                case Extension:
                    existingDefinitionButton.setSelected(true);
                    extensionButton.setSelected(true);
                    innerContentComboBox.setSelectedItem(getModelInnerContentType());
                    break;
                case Restriction:
                    existingDefinitionButton.setSelected(true);
                    restrictionButton.setSelected(true);
                    break;
            }
        }
        switchDefinitionPanels();
        selectModelNode();
        enableDisableComboBox();
        setPreviewText();
    }
    
    private void enableDisableComboBox() {
        if(getUIContentType()==ContentType.Extension &&
                getUIReference() instanceof GlobalComplexType &&
                !(((GlobalComplexType)getUIReference()).getDefinition()
                instanceof SimpleContent)) {
            innerContentLabel.setEnabled(true);
            innerContentComboBox.setEnabled(true);
            if(innerContentComboBox.getSelectedItem()==null)
                innerContentComboBox.setSelectedIndex(0);
        } else {
            innerContentLabel.setEnabled(false);
            innerContentComboBox.setEnabled(false);
        }
    }
    
    /**
     * enables disables the inner base type panel and inner content combo box.
     */
    private void switchDefinitionPanels() {
        if(existingDefinitionButton.isSelected()) {
            ((CardLayout)definitionPanel.getLayout()).show(definitionPanel,"reference"); //NO I18N
            if(referenceButtonGroup.getSelection()==null)
                referenceButtonGroup.getElements().nextElement().setSelected(true);
            //groupButton.setSelected(true);
            switchInnerContentPanels();
        } else {
            ((CardLayout)definitionPanel.getLayout()).show(definitionPanel,"compositor"); //NO I18N
            if(compositorButtonGroup.getSelection()==null)
                compositorButtonGroup.getElements().nextElement().setSelected(true);
//				sequenceButton.setSelected(true);
        }
        enableDisableComboBox();
    }
    
    private void switchInnerContentPanels() {
        if(groupButton.isSelected())
            ((CardLayout)innerContentPanel.getLayout()).show(innerContentPanel,"group");
        else
            ((CardLayout)innerContentPanel.getLayout()).show(innerContentPanel,"type");
    }
    
    /**
     * selects model node on ui
     */
    private void selectModelNode() {
        switch(getUIContentType()) {
            case Group:
                groupSelectionPanel.setInitialSelection((GlobalGroup)getModelReference());
                break;
            case Extension:
            case Restriction:
                typeSelectionPanel.setInitialSelection((GlobalType)getModelReference());
                break;
            default:
        }
    }
    
    /**
     * adds listeners
     */
    private void addListeners() {
        if(isNameable()) {
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
        }
        if (buttonListener ==null) {
            buttonListener = new ItemListener() {
                public void itemStateChanged(ItemEvent evt) {
                    Object source = evt.getSource();
                    if(source==extensionButton) {
                        enableDisableComboBox();
                    }
                    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                        if(source==inlineDefinitionButton ||
                                source==existingDefinitionButton) {
                            switchDefinitionPanels();
                        }
                        if(source==groupButton ||
                                source==extensionButton ||
                                source==restrictionButton) {
                            switchInnerContentPanels();
                        }
                        setPreviewText();
                        determineValidity();
                    }
                }
            };
        }
        inlineDefinitionButton.addItemListener(buttonListener);
        existingDefinitionButton.addItemListener(buttonListener);
        noTypeButton.addItemListener(buttonListener);
        sequenceButton.addItemListener(buttonListener);
        allButton.addItemListener(buttonListener);
        choiceButton.addItemListener(buttonListener);
        groupButton.addItemListener(buttonListener);
        extensionButton.addItemListener(buttonListener);
        restrictionButton.addItemListener(buttonListener);
        innerContentComboBox.addItemListener(buttonListener);
    }
    
    /**
     * removes listeners
     */
    private void removeListeners() {
        if(isNameable()) nameTextField.getDocument().removeDocumentListener(nameListener);
        inlineDefinitionButton.removeItemListener(buttonListener);
        existingDefinitionButton.removeItemListener(buttonListener);
        noTypeButton.removeItemListener(buttonListener);
        sequenceButton.removeItemListener(buttonListener);
        allButton.removeItemListener(buttonListener);
        choiceButton.removeItemListener(buttonListener);
        groupButton.removeItemListener(buttonListener);
        extensionButton.removeItemListener(buttonListener);
        restrictionButton.removeItemListener(buttonListener);
        innerContentComboBox.removeItemListener(buttonListener);
    }
    
    /**
     * returns name from ui name text field
     */
    protected String getUIName() {
        return nameTextField.getText();
    }
    
    /**
     * returns ui content type selection from content list
     */
    private ContentType getUIContentType() {
        if(inlineDefinitionButton.isSelected()) {
            if(noTypeButton.isSelected())
                return ContentType.NoType;
            if(sequenceButton.isSelected())
                return ContentType.Sequence;
            if(choiceButton.isSelected())
                return ContentType.Choice;
            if(allButton.isSelected())
                return ContentType.All;
        }
        if(existingDefinitionButton.isSelected()) {
            if(groupButton.isSelected())
                return ContentType.Group;
            if(extensionButton.isSelected())
                return ContentType.Extension;
            if(restrictionButton.isSelected())
                return ContentType.Restriction;
        }
        return null;
    }
    
    /**
     * returns ui inner content type selection from inner content combo box
     */
    private ContentType getUIInnerContentType() {
        if (!innerContentComboBox.isEnabled())
            return null;
        return (ContentType) innerContentComboBox.getSelectedItem();
    }
    
    /**
     * returns ui reference selection from inner type/ref panel
     */
    private ReferenceableSchemaComponent getUIReference() {
        switch(getUIContentType()) {
            case Group:
                return groupSelectionPanel.getCurrentSelection();
            case Extension:
            case Restriction:
                return typeSelectionPanel.getCurrentSelection();
            default:
        }
        return null;
    }
    
    /**
     * initializes the base type (group ref) panel, with view and explorer
     *
     */
    private void initializeTypePanel() {
        typeSelectionPanel = new SchemaComponentSelectionPanel<GlobalType>(
                getReference().get().getModel(),GlobalType.class, null,
                Collections.singleton(getReference().get()), true);
        typeSelectionPanel.addPropertyChangeListener(this);
        groupSelectionPanel = new SchemaComponentSelectionPanel<GlobalGroup>(
                getReference().get().getModel(),GlobalGroup.class, null, null, false);
        groupSelectionPanel.addPropertyChangeListener(this);
        innerContentPanel.add(groupSelectionPanel.getTypeSelectionPanel(),"group");
        innerContentPanel.add(typeSelectionPanel.getTypeSelectionPanel(),"type");
        typeSelectionPanel.getTypeSelectionPanel().getAccessibleContext().
                setAccessibleParent(innerContentPanel);
        groupSelectionPanel.getTypeSelectionPanel().getAccessibleContext().
                setAccessibleParent(innerContentPanel);
    }
    
    private Class<? extends ReferenceableSchemaComponent> getReferenceType() {
        ArrayList<Class<? extends SchemaComponent>> childTypes =
                new ArrayList<Class<? extends SchemaComponent>>();
        switch(getUIContentType()) {
            case Group:
                return GlobalGroup.class;
            case Extension:
            case Restriction:
                return GlobalType.class;
            default:
        }
        return null;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        compositorButtonGroup = new javax.swing.ButtonGroup();
        definitionButtonGroup = new javax.swing.ButtonGroup();
        referenceButtonGroup = new javax.swing.ButtonGroup();
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
        definitionPanel = new javax.swing.JPanel();
        compositorPanel = new javax.swing.JPanel();
        sequenceButton = new javax.swing.JRadioButton();
        choiceButton = new javax.swing.JRadioButton();
        allButton = new javax.swing.JRadioButton();
        noTypeButton = new javax.swing.JRadioButton();
        compositorLabel = new javax.swing.JLabel();
        referencePanel = new javax.swing.JPanel();
        extensionButton = new javax.swing.JRadioButton();
        restrictionButton = new javax.swing.JRadioButton();
        groupButton = new javax.swing.JRadioButton();
        innerContentLabel = new javax.swing.JLabel();
        innerContentComboBox = new javax.swing.JComboBox();
        innerContentPanel = new javax.swing.JPanel();
        referenceLabel = new javax.swing.JLabel();
        typeLabel = new javax.swing.JLabel();
        inlineDefinitionButton = new javax.swing.JRadioButton();
        existingDefinitionButton = new javax.swing.JRadioButton();
        mPanel = new javax.swing.JPanel();
        namePanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();

        previewLabel.setLabelFor(previewPane);
        org.openide.awt.Mnemonics.setLocalizedText(previewLabel, org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "TITLE_CustomizerForm_Preview"));
        previewLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Preview"));

        previewPane.setEditable(false);
        previewPane.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Preview"));
        previewPane.setContentType("text/xml");
        previewPane.setEnabled(false);
        jScrollPane1.setViewportView(previewPane);

        definitionPanel.setLayout(new java.awt.CardLayout());

        definitionPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        compositorButtonGroup.add(sequenceButton);
        org.openide.awt.Mnemonics.setLocalizedText(sequenceButton, ContentType.Sequence.getLabel());
        sequenceButton.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Content_Sequence"));
        sequenceButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sequenceButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        compositorButtonGroup.add(choiceButton);
        org.openide.awt.Mnemonics.setLocalizedText(choiceButton, ContentType.Choice.getLabel());
        choiceButton.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Content_Choice"));
        choiceButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        choiceButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        compositorButtonGroup.add(allButton);
        org.openide.awt.Mnemonics.setLocalizedText(allButton, ContentType.All.getLabel());
        allButton.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Content_All"));
        allButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        allButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        compositorButtonGroup.add(noTypeButton);
        org.openide.awt.Mnemonics.setLocalizedText(noTypeButton, ContentType.NoType.getLabel());
        noTypeButton.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Content_NoType"));
        noTypeButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noTypeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(compositorLabel, org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "LBL_ComplexType_Inline_Definition_Panel", new Object[] {}));
        compositorLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Inline_Definition_Panel"));

        org.jdesktop.layout.GroupLayout compositorPanelLayout = new org.jdesktop.layout.GroupLayout(compositorPanel);
        compositorPanel.setLayout(compositorPanelLayout);
        compositorPanelLayout.setHorizontalGroup(
            compositorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(compositorPanelLayout.createSequentialGroup()
                .add(compositorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(compositorLabel)
                    .add(compositorPanelLayout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(compositorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(sequenceButton)
                            .add(choiceButton)
                            .add(allButton)
                            .add(noTypeButton))))
                .addContainerGap(384, Short.MAX_VALUE))
        );
        compositorPanelLayout.setVerticalGroup(
            compositorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(compositorPanelLayout.createSequentialGroup()
                .add(compositorLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sequenceButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(choiceButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(allButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(noTypeButton)
                .addContainerGap(187, Short.MAX_VALUE))
        );
        definitionPanel.add(compositorPanel, "compositor");

        referenceButtonGroup.add(extensionButton);
        org.openide.awt.Mnemonics.setLocalizedText(extensionButton, ContentType.Extension.getLabel());
        extensionButton.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Content_Extension"));
        extensionButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        extensionButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        referenceButtonGroup.add(restrictionButton);
        org.openide.awt.Mnemonics.setLocalizedText(restrictionButton, ContentType.Restriction.getLabel());
        restrictionButton.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Content_Restriction"));
        restrictionButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        restrictionButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        referenceButtonGroup.add(groupButton);
        org.openide.awt.Mnemonics.setLocalizedText(groupButton, ContentType.Group.getLabel());
        groupButton.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Content_Group"));
        groupButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        groupButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        innerContentLabel.setLabelFor(innerContentComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(innerContentLabel, org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "LBL_ComplexType_InnerContent"));
        innerContentLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_InnerContent"));
        innerContentLabel.setEnabled(false);

        innerContentComboBox.setModel(new DefaultComboBoxModel(
            new Object[]
            {
                ContentType.Sequence,
                ContentType.Choice,
                ContentType.All,
                ContentType.NoType,
                ContentType.Group,
            }
        ));
        innerContentComboBox.setEnabled(false);

        innerContentPanel.setLayout(new java.awt.CardLayout());

        initializeTypePanel();

        org.openide.awt.Mnemonics.setLocalizedText(referenceLabel, org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "LBL_ComplexType_Existing_Definition_Panel", new Object[] {}));
        referenceLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Existing_Definition_Panel"));

        org.jdesktop.layout.GroupLayout referencePanelLayout = new org.jdesktop.layout.GroupLayout(referencePanel);
        referencePanel.setLayout(referencePanelLayout);
        referencePanelLayout.setHorizontalGroup(
            referencePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(referenceLabel)
            .add(referencePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(extensionButton)
                .add(5, 5, 5)
                .add(innerContentLabel)
                .add(5, 5, 5)
                .add(innerContentComboBox, 0, 278, Short.MAX_VALUE)
                .addContainerGap())
            .add(referencePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(restrictionButton)
                .addContainerGap(385, Short.MAX_VALUE))
            .add(referencePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(referencePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(referencePanelLayout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(innerContentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE))
                    .add(referencePanelLayout.createSequentialGroup()
                        .add(groupButton)
                        .addContainerGap(385, Short.MAX_VALUE))))
        );

        referencePanelLayout.linkSize(new java.awt.Component[] {extensionButton, groupButton, restrictionButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        referencePanelLayout.setVerticalGroup(
            referencePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(referencePanelLayout.createSequentialGroup()
                .add(referenceLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(referencePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(extensionButton)
                    .add(innerContentLabel)
                    .add(innerContentComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(restrictionButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(groupButton)
                .add(0, 0, 0)
                .add(innerContentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))
        );
        definitionPanel.add(referencePanel, "reference");

        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "LBL_ComplexType_Content", new Object[] {}));
        typeLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Content"));

        definitionButtonGroup.add(inlineDefinitionButton);
        org.openide.awt.Mnemonics.setLocalizedText(inlineDefinitionButton, org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "LBL_ComplexType_Inline_Definition_Button"));
        inlineDefinitionButton.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Inline_Definition_Button"));
        inlineDefinitionButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        inlineDefinitionButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        definitionButtonGroup.add(existingDefinitionButton);
        org.openide.awt.Mnemonics.setLocalizedText(existingDefinitionButton, org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "LBL_ComplexType_Existing_Definition_Button"));
        existingDefinitionButton.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Existing_Definition_Button"));
        existingDefinitionButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        existingDefinitionButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mPanel.setLayout(new java.awt.BorderLayout());

        mPanel.add(getMessageDisplayer().getComponent(),java.awt.BorderLayout.CENTER);

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "LBL_CustomizerForm_Name"));
        nameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ComplexTypeCustomizer.class, "HINT_ComplexType_Name"));

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
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(existingDefinitionButton)
                            .add(inlineDefinitionButton))))
                .addContainerGap(357, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(previewLabel)
                .addContainerGap(448, Short.MAX_VALUE))
            .add(namePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(definitionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(namePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(11, 11, 11)
                .add(typeLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(inlineDefinitionButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(existingDefinitionButton)
                .add(11, 11, 11)
                .add(definitionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                .add(11, 11, 11)
                .add(previewLabel)
                .add(5, 5, 5)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(mPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allButton;
    private javax.swing.JRadioButton choiceButton;
    private javax.swing.ButtonGroup compositorButtonGroup;
    private javax.swing.JLabel compositorLabel;
    private javax.swing.JPanel compositorPanel;
    private javax.swing.ButtonGroup definitionButtonGroup;
    private javax.swing.JPanel definitionPanel;
    private javax.swing.JRadioButton existingDefinitionButton;
    private javax.swing.JRadioButton extensionButton;
    private javax.swing.JRadioButton groupButton;
    private javax.swing.JRadioButton inlineDefinitionButton;
    private javax.swing.JComboBox innerContentComboBox;
    private javax.swing.JLabel innerContentLabel;
    private javax.swing.JPanel innerContentPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel mPanel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel namePanel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JRadioButton noTypeButton;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JEditorPane previewPane;
    private javax.swing.ButtonGroup referenceButtonGroup;
    private javax.swing.JLabel referenceLabel;
    private javax.swing.JPanel referencePanel;
    private javax.swing.JRadioButton restrictionButton;
    private javax.swing.JRadioButton sequenceButton;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables
    
    /**
     * content type from model
     */
    private transient ContentType contentType;
    /**
     * inner content type from model
     */
    private transient ContentType innerContentType;
    /**
     * reference to group or base type from model
     */
    private transient ReferenceableSchemaComponent gRef;
    /**
     * name document listener
     */
    private transient DocumentListener nameListener;
    /**
     * content radio buttons listener
     */
    private transient ItemListener buttonListener;
    /**
     * parent component for new type
     */
    private transient SchemaComponent parent;
    
    /**
     * group selection panel
     */
    private transient SchemaComponentSelectionPanel<GlobalGroup>
            groupSelectionPanel;
    /**
     * type selection panel
     */
    private transient SchemaComponentSelectionPanel<GlobalType>
            typeSelectionPanel;
    
    
    
    private enum ContentType {
        NoType(NbBundle.getMessage(ComplexTypeCustomizer.class,
        "LBL_ComplexType_Content_NoType")),
        Sequence(NbBundle.getMessage(ComplexTypeCustomizer.class,
        "LBL_ComplexType_Content_Sequence")),
        Choice(NbBundle.getMessage(ComplexTypeCustomizer.class,
        "LBL_ComplexType_Content_Choice")),
        All(NbBundle.getMessage(ComplexTypeCustomizer.class,
        "LBL_ComplexType_Content_All")),
        Group(NbBundle.getMessage(ComplexTypeCustomizer.class,
        "LBL_ComplexType_Content_Group")),
        Extension(NbBundle.getMessage(ComplexTypeCustomizer.class,
        "LBL_ComplexType_Content_Extension")),
        Restriction(NbBundle.getMessage(ComplexTypeCustomizer.class,
        "LBL_ComplexType_Content_Restriction"));
        ContentType(String label) {
            this.label = label;
        }
        
        public String toString() {
            int idx = org.openide.awt.Mnemonics.findMnemonicAmpersand(label);
            if(idx>-1&&idx<label.length()) {
                String toStr = label.substring(0,idx);
                if(idx+1<label.length()) {
                    toStr = toStr.concat(label.substring(idx+1,label.length()));
                }
                return toStr;
            }
            return label;
        }
        
        public String getLabel() {
            return label;
        }
        
        private String label;
    }
    
    protected void applyChanges() throws IOException {
        saveName();
        ContentType uiContentType = getUIContentType();
        ReferenceableSchemaComponent uiRef = getUIReference();
        switch(uiContentType) {
            case NoType:
            case All:
            case Sequence:
            case Choice:
                if(uiContentType!=getModelContentType()) saveContentType();
                break;
            case Group:
                if(uiRef instanceof GlobalGroup && (uiContentType!=
                        getModelContentType() || uiRef!=getModelReference()))
                    saveContentType();
                break;
            case Restriction:
                if(uiRef instanceof GlobalType && (uiContentType!=
                        getModelContentType() || uiRef!=getModelReference()))
                    saveContentType();
                break;
            case Extension:
                if(uiRef instanceof GlobalSimpleType ||
                        uiRef instanceof GlobalComplexType &&
                        ((GlobalComplexType)uiRef).getDefinition() instanceof
                        SimpleContent) {
                    if(uiContentType!=getModelContentType() ||
                            uiRef!=getModelReference()) {
                        saveContentType();
                    }
                } else if(uiRef instanceof GlobalComplexType) {
                    ContentType uiInnerContentType = getUIInnerContentType();
                    if(uiInnerContentType!=null) {
                        if(uiContentType!=getModelContentType() ||
                                uiRef!=getModelReference() ||
                                uiInnerContentType!= getModelInnerContentType()) {
                            saveContentType();
                        }
                    }
                }
        }
    }
    
    ComplexType saveContentType() {
        ComplexType type = getSchemaComponent();
        SchemaComponentFactory factory = getSchemaModel().getFactory();
        ComplexTypeDefinition currentDef = type.getDefinition();
        ReferenceableSchemaComponent ref = getUIReference();
        LocalAttributeContainer lac = type;
        if(currentDef instanceof ComplexContent) {
            ComplexContent cc = (ComplexContent)currentDef;
            if(cc.getLocalDefinition() instanceof ComplexContentRestriction) {
                lac = (ComplexContentRestriction)cc.getLocalDefinition();
            } else if(cc.getLocalDefinition() instanceof ComplexExtension) {
                lac = (ComplexExtension)cc.getLocalDefinition();
            }
        } else if(currentDef instanceof SimpleContent) {
            SimpleContent sc = (SimpleContent)currentDef;
            if(sc.getLocalDefinition() instanceof SimpleContentRestriction) {
                lac = (SimpleContentRestriction)sc.getLocalDefinition();
            } else if(sc.getLocalDefinition() instanceof SimpleExtension) {
                lac = (SimpleExtension)sc.getLocalDefinition();
            }
        }
        switch (getUIContentType()) {
            case NoType:
                assert currentDef != null;
                type.setDefinition(null);
                break;
            case All:
                assert !(currentDef instanceof All);
                All all = factory.createAll();
                // try to preserve old contents
                if(currentDef instanceof Choice || currentDef instanceof Sequence) {
                    copyContent(currentDef, all);
                }
                type.setDefinition(all);
                break;
            case Choice:
                assert !(currentDef instanceof Choice);
                Choice choice = factory.createChoice();
                // try to preserve old contents
                if(currentDef instanceof All || currentDef instanceof Sequence) {
                    copyContent(currentDef, choice);
                }
                type.setDefinition(choice);
                break;
            case Sequence:
                Sequence sequence = factory.createSequence();
                // try to preserve old contents
                if(currentDef instanceof All || currentDef instanceof Choice) {
                    copyContent(currentDef, sequence);
                }
                type.setDefinition(sequence);
                break;
            case Group:
                assert ref instanceof GlobalGroup;
                GroupReference group;
                if(currentDef instanceof GroupReference)
                    group = (GroupReference) currentDef;
                else
                    group = factory.createGroupReference();
                group.setRef(group.createReferenceTo((GlobalGroup)ref, GlobalGroup.class));
                type.setDefinition(group);
                break;
            case Extension:
                assert ref instanceof GlobalType;
                if(ref instanceof GlobalSimpleType ||
                        ref instanceof GlobalComplexType &&
                        ((GlobalComplexType)ref).getDefinition() instanceof SimpleContent) {
                    SimpleExtension se = null;
                    if(currentDef instanceof SimpleContent) {
                        SimpleContent sc = (SimpleContent)currentDef;
                        if(sc.getLocalDefinition() instanceof SimpleExtension) {
                            se = (SimpleExtension)sc.getLocalDefinition();
                        } else {
                            se = factory.createSimpleExtension();
                            moveAttributeContents(lac,se);
                            sc.setLocalDefinition(se);
                        }
                    } else {
                        SimpleContent sc = factory.createSimpleContent();
                        se = factory.createSimpleExtension();
                        moveAttributeContents(lac,se);
                        sc.setLocalDefinition(se);
                        type.setDefinition(sc);
                    }
                    se.setBase(se.createReferenceTo(
                            (GlobalType)ref, GlobalType.class));
                } else if(ref instanceof GlobalComplexType) {
                    assert getUIInnerContentType()!=null;
                    ComplexExtension ce = null;
                    if(currentDef instanceof ComplexContent) {
                        ComplexContent cc = (ComplexContent)currentDef;
                        if(cc.getLocalDefinition() instanceof ComplexExtension) {
                            ce = (ComplexExtension)cc.getLocalDefinition();
                        } else {
                            ce = factory.createComplexExtension();
                            copyContent(cc.getLocalDefinition(),ce);
                            //moveAttributeContents(lac,ce);
                            cc.setLocalDefinition(ce);
                        }
                    } else {
                        ComplexContent cc = factory.createComplexContent();
                        ce = factory.createComplexExtension();
                        moveComplexContents(lac,ce);
                        cc.setLocalDefinition(ce);
                        type.setDefinition(cc);
                    }
                    GlobalComplexType gct = (GlobalComplexType)ref;
                    ce.setBase(ce.createReferenceTo(gct, GlobalType.class));
                    if(getUIInnerContentType()!=getModelInnerContentType()) {
                        ComplexExtensionDefinition ced = null;
                        switch(getUIInnerContentType()) {
                            case Group:
                                ced = factory.createGroupReference();
                                break;
                            case All:
                                ced = factory.createAll();
                                break;
                            case Sequence:
                                ced = factory.createSequence();
                                break;
                            case Choice:
                                ced = factory.createChoice();
                                break;
                            default:
                        }
                        if(ced!=null && ce.getLocalDefinition()!=null) {
                            copyContent(ce.getLocalDefinition(),ced);
                        }
                        ce.setLocalDefinition(ced);
                    }
                }
                break;
            case Restriction:
                assert ref instanceof GlobalType;
                if(ref instanceof GlobalSimpleType ||
                        ref instanceof GlobalComplexType &&
                        ((GlobalComplexType)ref).getDefinition() instanceof SimpleContent) {
                    SimpleContentRestriction scr = null;
                    if(currentDef instanceof SimpleContent) {
                        SimpleContent sc = (SimpleContent)currentDef;
                        if(sc.getLocalDefinition() instanceof SimpleContentRestriction) {
                            scr = (SimpleContentRestriction)sc.getLocalDefinition();
                            if(scr.getInlineType()!=null) scr.setInlineType(null);
                        } else {
                            scr = factory.createSimpleContentRestriction();
                            moveAttributeContents(lac,scr);
                            sc.setLocalDefinition(scr);
                        }
                    } else {
                        SimpleContent sc = factory.createSimpleContent();
                        scr = factory.createSimpleContentRestriction();
                        moveAttributeContents(lac,scr);
                        sc.setLocalDefinition(scr);
                        type.setDefinition(sc);
                    }
                    scr.setBase(scr.createReferenceTo(
                            (GlobalType)ref, GlobalType.class));
                } else if(ref instanceof GlobalComplexType) {
                    ComplexContentRestriction ccr = null;
                    if(currentDef instanceof ComplexContent) {
                        ComplexContent cc = (ComplexContent)currentDef;
                        if(cc.getLocalDefinition() instanceof ComplexContentRestriction) {
                            ccr = (ComplexContentRestriction)cc.getLocalDefinition();
                        } else {
                            ccr = factory.createComplexContentRestriction();
                            moveAttributeContents(lac,ccr);
                            cc.setLocalDefinition(ccr);
                        }
                    } else {
                        ComplexContent cc = factory.createComplexContent();
                        ccr = factory.createComplexContentRestriction();
                        moveAttributeContents(lac,ccr);
                        cc.setLocalDefinition(ccr);
                        type.setDefinition(cc);
                    }
                    GlobalComplexType gct = (GlobalComplexType)ref;
                    ccr.setBase(ccr.createReferenceTo(
                            gct, GlobalComplexType.class));
                    ComplexTypeDefinition newInnerDef = null;
                    
                    if(gct.getDefinition() instanceof All ||
                            gct.getDefinition() instanceof Choice ||
                            gct.getDefinition() instanceof Sequence ||
                            gct.getDefinition() instanceof GroupReference) {
                        newInnerDef = gct.getDefinition();
                    } else if(gct.getDefinition() instanceof ComplexContent) {
                        ComplexContent cc = (ComplexContent)gct.getDefinition();
                        if (cc.getLocalDefinition() instanceof ComplexContentRestriction) {
                            newInnerDef = ((ComplexContentRestriction)cc.
                                    getLocalDefinition()).getDefinition();
                        } else if (cc.getLocalDefinition() instanceof ComplexExtension) {
                            newInnerDef = (ComplexTypeDefinition)((ComplexExtension)cc.
                                    getLocalDefinition()).getLocalDefinition();
                        }
                    }
                    if(ccr != null)
                        ccr.setDefinition((ComplexTypeDefinition) newInnerDef.copy(ccr));
                }
                break;
        }
        this.contentType = contentType;
        return type;
    }
    
    private void copyContent(final SchemaComponent oldParent,
            final SchemaComponent newParent) {
        if(oldParent==null || newParent==null) return;
        SchemaModel model = getSchemaModel();
        for(SchemaComponent child :oldParent.getChildren()) {
            if(newParent.canPaste(child))
                model.addChildComponent(newParent,child.copy(newParent),-1);
        }
    }
    
    private void moveAttributeContents(final LocalAttributeContainer oldParent,
            final LocalAttributeContainer newParent) {
        if(oldParent==null || newParent==null) return;
        SchemaModel model = getSchemaModel();
        ArrayList<Class<? extends SchemaComponent>> attrChildTypes =
                new ArrayList<Class<? extends SchemaComponent>>(4);
        attrChildTypes.add(LocalAttribute.class);
        attrChildTypes.add(AttributeReference.class);
        attrChildTypes.add(AttributeGroupReference.class);
        attrChildTypes.add(AnyAttribute.class);
        for(SchemaComponent child :oldParent.getChildren(attrChildTypes)) {
            if(newParent.canPaste(child))
                model.addChildComponent(newParent,child.copy(newParent),-1);
            model.removeChildComponent(child);
        }
    }
        
    private void moveComplexContents(final LocalAttributeContainer oldParent,
            final LocalAttributeContainer newParent) {
        if(oldParent==null || newParent==null) return;
        SchemaModel model = getSchemaModel();
        ArrayList<Class<? extends SchemaComponent>> childTypes =
                new ArrayList<Class<? extends SchemaComponent>>(4);
        childTypes.add(LocalAttribute.class);
        childTypes.add(AttributeReference.class);
        childTypes.add(AttributeGroupReference.class);
        childTypes.add(AnyAttribute.class);
        childTypes.add(ComplexTypeDefinition.class);
        for(SchemaComponent child :oldParent.getChildren(childTypes)) {
            if(newParent.canPaste(child))
                model.addChildComponent(newParent,child.copy(newParent),-1);
            model.removeChildComponent(child);
        }
    }
    
    public void reset() {
        removeListeners();
        initialize();
        initializeUI();
        addListeners();
        if(hasParent()) {
            setSaveEnabled(false);
        } else {
            setSaveEnabled(true);
        }
        setResetEnabled(false);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ComplexTypeCustomizer.class);
    }
    
    /**
     * listens to property change events from explorer manager node selections
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SchemaComponentSelectionPanel.PROPERTY_SELECTION)) {
            enableDisableComboBox();
            setPreviewText();
            determineValidity();
        }
    }
    
    /**
     * determines validity of customizer.
     */
    private void determineValidity() {
        getMessageDisplayer().clear();
        boolean nameChanged = isNameable()&&isNameChanged();
        if(!nameChanged && getUIContentType() == getModelContentType() &&
                getUIInnerContentType()==getModelInnerContentType() &&
                getUIReference()==getModelReference()) {
            if(hasParent()) {
                setSaveEnabled(false);
            } else {
                setSaveEnabled(true);
            }
            setResetEnabled(false);
        } else {
            setResetEnabled(true);
            boolean valid = false;
            switch(getUIContentType()) {
                case Extension:
                    if(getUIReference() instanceof GlobalSimpleType ||
                            getUIReference() instanceof GlobalComplexType &&
                            (((GlobalComplexType)getUIReference()).getDefinition()
                            instanceof SimpleContent || getUIInnerContentType()!=null)) {
                        valid = true;
                    }
                    break;
                case Restriction:
                    if(getUIReference() instanceof GlobalType) {
                        valid = true;
                    }
                    break;
                case Group:
                    if(getUIReference() instanceof GlobalGroup) {
                        valid = true;
                    }
                    break;
                case NoType:
                case All:
                case Sequence:
                case Choice:
                    valid = true;
                    break;
                default:
            }
            setSaveEnabled(valid && (!isNameChanged() || isNameValid()));
            if(!valid) {
                if(getUIContentType()==ContentType.Group) {
                    getMessageDisplayer().annotate(org.openide.util.NbBundle.
                            getMessage(ComplexTypeCustomizer.class,
                            "MSG_Group_Error"),
                            MessageDisplayer.Type.ERROR);
                } else {
                    getMessageDisplayer().annotate(org.openide.util.NbBundle.
                            getMessage(ComplexTypeCustomizer.class,
                            "MSG_Type_Error"),
                            MessageDisplayer.Type.ERROR);
                }
            }
        }
    }
    
    private void setPreviewText() {
        StringBuffer retValue = new StringBuffer("<");
        String prefix = getReference().get().getPeer().getPrefix();
        if(prefix!=null) retValue.append(prefix+":");
        retValue.append("complexType");
        if(isNameable()) {
            retValue.append(" name=\"");
            retValue.append(getUIName());
            retValue.append("\"");
        }
        retValue.append(">");
        ReferenceableSchemaComponent ref = getUIReference();
        boolean complexContent = true;
        if(ref instanceof GlobalSimpleType ||
                ref instanceof GlobalComplexType &&
                ((GlobalComplexType)ref).getDefinition()
                instanceof SimpleContent)
            complexContent = false;
        switch(getUIContentType()) {
            case NoType:
                break;
            case Sequence:
                retValue.append("\n    <");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("sequence/>");
                break;
            case Choice:
                retValue.append("\n    <");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("choice/>");
                break;
            case All:
                retValue.append("\n    <");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("all/>");
                break;
            case Group:
                retValue.append("\n    <");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("group ref=\"");
                if(ref!=null) {
                    retValue.append(ref.getName());
                }
                retValue.append("\"/>");
                break;
            case Extension:
                if(complexContent) {
                    retValue.append("\n    <");
                    if(prefix!=null) retValue.append(prefix+":");
                    retValue.append("complexContent>");
                } else {
                    retValue.append("\n    <");
                    if(prefix!=null) retValue.append(prefix+":");
                    retValue.append("simpleContent>");
                }
                retValue.append("\n        <");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("extension base=\"");
                if(ref!=null) {
                    retValue.append(ref.getName());
                }
                retValue.append("\">");
                if(complexContent && ref!=null) {
                    switch(getUIInnerContentType()) {
                        case NoType:
                            break;
                        case Sequence:
                            retValue.append("\n            <");
                            if(prefix!=null) retValue.append(prefix+":");
                            retValue.append("sequence/>");
                            break;
                        case Choice:
                            retValue.append("\n            <");
                            if(prefix!=null) retValue.append(prefix+":");
                            retValue.append("choice/>");
                            break;
                        case All:
                            retValue.append("\n            <");
                            if(prefix!=null) retValue.append(prefix+":");
                            retValue.append("all/>");
                            break;
                        case Group:
                            retValue.append("\n            <");
                            if(prefix!=null) retValue.append(prefix+":");
                            retValue.append("group ref=\"\"/>");
                            break;
                    }
                }
                retValue.append("\n        </");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("extension>");
                if(complexContent) {
                    retValue.append("\n    </");
                    if(prefix!=null) retValue.append(prefix+":");
                    retValue.append("complexContent>");
                } else {
                    retValue.append("\n    </");
                    if(prefix!=null) retValue.append(prefix+":");
                    retValue.append("simpleContent>");
                }
                break;
            case Restriction:
                if(complexContent) {
                    retValue.append("\n    <");
                    if(prefix!=null) retValue.append(prefix+":");
                    retValue.append("complexContent>");
                } else {
                    retValue.append("\n    <");
                    if(prefix!=null) retValue.append(prefix+":");
                    retValue.append("simpleContent>");
                }
                retValue.append("\n        <");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("restriction base=\"");
                if(ref!=null) {
                    retValue.append(ref.getName());
                }
                retValue.append("\">");
                retValue.append("\n        </");
                if(prefix!=null) retValue.append(prefix+":");
                retValue.append("restriction>");
                if(complexContent) {
                    retValue.append("\n    </");
                    if(prefix!=null) retValue.append(prefix+":");
                    retValue.append("complexContent>");
                } else {
                    retValue.append("\n    </");
                    if(prefix!=null) retValue.append(prefix+":");
                    retValue.append("simpleContent>");
                }
                break;
        }
        retValue.append("\n</");
        if(prefix!=null) retValue.append(prefix+":");
        retValue.append("complexType>\n");
        previewPane.setText(retValue.toString());
    }
    
    private SchemaModel getSchemaModel() {
        return getSchemaComponent().getModel();
    }
    
    private ComplexType getSchemaComponent() {
        if(getReference()==null || getReference().get()==null) return null;
        return getReference().get();
    }
}
