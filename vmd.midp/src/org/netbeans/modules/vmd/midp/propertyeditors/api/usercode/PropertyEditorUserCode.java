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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.midp.propertyeditors.api.usercode;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.DialogBinding;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertyEditorSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * This class allows create PropertyEditor which supports User Code.
 *
 * See for example PropertyEditorString.
 *
 * <b>WARNING - while overriding init method, you have to call super.init() method too.</b>
 *
 * @author Anton Chechel
 */
public abstract class PropertyEditorUserCode extends DesignPropertyEditor {

    public static final PropertyValue NULL_VALUE = PropertyValue.createNull();
    public static final String NULL_TEXT = NbBundle.getMessage(PropertyEditorUserCode.class, "LBL_STRING_NULL"); // NOI18N
    public static final String USER_CODE_TEXT = NbBundle.getMessage(PropertyEditorUserCode.class, "LBL_STRING_USER_CODE"); // NOI18N
    private static final Icon ICON_WARNING = new ImageIcon(Utilities.loadImage("org/netbeans/modules/vmd/midp/resources/warning.gif")); // NOI18N
    private static final Icon ICON_ERROR = new ImageIcon(Utilities.loadImage("org/netbeans/modules/vmd/midp/resources/error.gif")); // NOI18N
    
    private final CustomEditor customEditor;
    private JRadioButton userCodeRadioButton;
    private final JLabel messageLabel;
    private String userCodeLabel;
    private String userCode = ""; // NOI18N
    protected WeakReference<DesignComponent> component;

    protected PropertyEditorUserCode(String userCodeLabel) {
        this.userCodeLabel = userCodeLabel;
        messageLabel = new JLabel(" "); //NOI18N
        Color nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            nbErrorForeground = new Color(255, 0, 0);
        }
        messageLabel.setForeground(nbErrorForeground);
        customEditor = new CustomEditor();
    }

    /**
     * This method should be invoked from subclass to init elements.
     */
    protected void initElements(Collection<PropertyEditorElement> elements) {
        customEditor.init(elements);
    }

    /**
     * <b>WARNING - while override, you have to call super.init() method too.</b>
     */
    @Override
    public void init(DesignComponent component) {
        if (component != null) {
            this.component = new WeakReference<DesignComponent>(component);
        }
    }

    /**
     * Updates state of custom editor and returns it to edit property value.
     */
    @Override
    public final Component getCustomEditor() {
        if (!customEditor.isShowing()) {
            PropertyValue value = (PropertyValue) super.getValue();
            if (isCurrentValueAUserCodeType()) {
                customEditor.setUserCodeText(value.getUserCode());
                customEditor.updateState(null);
            } else {
                customEditor.setUserCodeText(null);
                customEditor.updateState(value);
            }
            customEditor.init();
        }
        return customEditor;
    }

    @Override
    public boolean isExecuteInsideWriteTransactionUsed() {
        return false;
    }

    /**
     * Returns text for inplace property editor.
     * <b>WARNING this method shoud be overriden and if it returns null then overriden getAsText() should return own text</b>
     */
    @Override
    public String getAsText() {
        if (isCurrentValueAUserCodeType()) {
            return USER_CODE_TEXT;
        } else if (isCurrentValueANull()) {
            return NULL_TEXT;
        }
        return null;
    }

    /**
     * Sets property value depending on given text. Invoked from inplace editor.
     */
    @Override
    public void setAsText(String text) {
        if (canWrite()) {
            if (text.equals(NULL_TEXT)) {
                super.setValue(NULL_VALUE);
            } else {
                customEditor.setText(text);
            }
        }
    }

    @Override
    public Boolean canEditAsText() {
        if (isCurrentValueAUserCodeType()) {
            return false;
        }
        return MidpPropertyEditorSupport.singleSelectionEditAsTextOnly();
    }

    @Override
    public boolean canWrite() {
        return MidpPropertyEditorSupport.singleSelectionEditAsTextOnly();
    }

    /**
     * <b>WARNING! If override invoke super.customEditorOKButtonPressed()</b>
     */
    @Override
    public void customEditorOKButtonPressed() {
        if (userCodeRadioButton.isSelected()) {
            PropertyEditorUserCode.super.setValue(PropertyValue.createUserCode(userCode));
        }
    }

    /**
     * @return boolean whether current property value has kind of user code
     */
    protected boolean isCurrentValueAUserCodeType() {
        PropertyValue value = (PropertyValue) super.getValue();
        return value != null && value.getKind() == PropertyValue.Kind.USERCODE;
    }

    /**
     * @return boolean whether current property value has kind of NULL
     */
    protected boolean isCurrentValueANull() {
        PropertyValue value = (PropertyValue) super.getValue();
        return value == null || value.getKind() == PropertyValue.Kind.NULL;
    }

    /**
     * Displays warning message on the custom property editor panel
     * @param message to be displayed
     */
    protected void displayWarning(String message) {
        messageLabel.setText(message);
        messageLabel.setIcon(ICON_WARNING);
    }

    /**
     * Displays error message on the custom property editor panel
     * @param message to be displayed
     */
    protected void displayError(String message) {
        messageLabel.setText(message);
        messageLabel.setIcon(ICON_ERROR);
    }

    /**
     * Clears error/warning message on the custom property editor panel
     */
    protected void clearErrorStatus() {
        messageLabel.setText(" "); //NOI18N
        messageLabel.setIcon(null);
    }

    private final class CustomEditor extends JPanel implements DocumentListener, ActionListener {

        private Collection<PropertyEditorElement> elements;
        private JEditorPane userCodeEditorPane;

        public void init(Collection<PropertyEditorElement> elements) {
            this.elements = elements;
            initComponents();
        }

        private void initComponents() {
            setLayout(new GridBagLayout());
            ButtonGroup buttonGroup = new ButtonGroup();
            GridBagConstraints constraints = new GridBagConstraints();
            boolean isAnyElementVerticallyResizable = false;
            for (PropertyEditorElement element : elements) {
                JRadioButton rb = element.getRadioButton();
                buttonGroup.add(rb);
                constraints.insets = new Insets(12, 12, 6, 12);
                constraints.anchor = GridBagConstraints.NORTHWEST;
                constraints.gridx = GridBagConstraints.REMAINDER;
                constraints.gridy = GridBagConstraints.RELATIVE;
                constraints.weightx = 1.0;
                constraints.weighty = 0.0;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                add(rb, constraints);

                constraints.insets = new Insets(0, 32, 12, 12);
                constraints.anchor = GridBagConstraints.NORTHWEST;
                constraints.gridx = GridBagConstraints.REMAINDER;
                constraints.gridy = GridBagConstraints.RELATIVE;
                constraints.weightx = 1.0;
                constraints.weighty = element.isVerticallyResizable() ? 1.0 : 0.0;
                constraints.fill = element.isVerticallyResizable() ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
                add(element.getCustomEditorComponent(), constraints);

                if (element.isVerticallyResizable()) {
                    isAnyElementVerticallyResizable = true;
                }
            }

            userCodeRadioButton = new JRadioButton();
            Mnemonics.setLocalizedText(userCodeRadioButton, NbBundle.getMessage(PropertyEditorUserCode.class, "LBL_USER_CODE", userCodeLabel)); // NOI18N
            userCodeRadioButton.addActionListener(this);
            buttonGroup.add(userCodeRadioButton);

            constraints.insets = new Insets(12, 12, 6, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = GridBagConstraints.REMAINDER;
            constraints.gridy = GridBagConstraints.RELATIVE;
            constraints.weightx = 1.0;
            constraints.weighty = 0.0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            add(userCodeRadioButton, constraints);

            JScrollPane jsp = new JScrollPane();
            userCodeEditorPane = new JEditorPane();
            //userCodeEditorPane.setFont(userCodeRadioButton.getFont());
            SwingUtilities.invokeLater(new Runnable() {

                //otherwise we get: java.lang.AssertionError: BaseKit.install() incorrectly called from non-AWT thread.
                public void run() {
                    userCodeEditorPane.setContentType("text/x-java"); // NOI18N
                    userCodeEditorPane.getDocument().addDocumentListener(CustomEditor.this);
                }
            });
            jsp.setViewportView(userCodeEditorPane);
            jsp.setPreferredSize(new Dimension(400, 100));
            jsp.setMinimumSize(new Dimension(400, 100));

            constraints.insets = new Insets(0, 32, 12, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = GridBagConstraints.REMAINDER;
            constraints.gridy = GridBagConstraints.RELATIVE;
            constraints.weightx = 1.0;
            constraints.weighty = isAnyElementVerticallyResizable ? 0.0 : 1.0;
            constraints.fill = isAnyElementVerticallyResizable ? GridBagConstraints.HORIZONTAL : GridBagConstraints.BOTH;
            add(jsp, constraints);

            constraints.insets = new Insets(0, 12, 0, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = GridBagConstraints.REMAINDER;
            constraints.gridy = GridBagConstraints.RELATIVE;
            constraints.weightx = 1.0;
            constraints.weighty = 0.0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            add(messageLabel, constraints);

            selectDefaultRadioButton();
        }

        public void init() {
            if (component == null || component.get() == null) {
                return;
            }
            DesignComponent _component = component.get();

            javax.swing.text.Document swingDoc = userCodeEditorPane.getDocument();
            if (swingDoc.getProperty(JavaSource.class) == null) {
                DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(_component.getDocument());
                swingDoc.putProperty(Document.StreamDescriptionProperty, context.getDataObject());
                int offset = CodeUtils.getMethodOffset(context);
                DialogBinding.bindComponentToFile(context.getDataObject().getPrimaryFile(), offset, 0, userCodeEditorPane);
            }
        }

        /**
         * Updates state of custom editor
         * @param value if null - clear state
         */
        public void updateState(PropertyValue value) {
            for (PropertyEditorElement element : elements) {
                element.updateState(value);
            }
        }

        /**
         * Sets text for each element
         * @param text to be set
         */
        public void setText(String text) {
            for (PropertyEditorElement element : elements) {
                element.setTextForPropertyValue(text);
            }
        }

        /**
         * Sets text for user code pane
         * @param text to be set
         */
        public void setUserCodeText(String text) {
            if (text != null) {
                userCodeEditorPane.setText(text);
                userCodeRadioButton.setSelected(true);
                userCodeRadioButton.requestFocus();
            } else {
                userCodeEditorPane.setText(null);
                selectDefaultRadioButton();
            }
        }

        public void insertUpdate(DocumentEvent evt) {
            userCodeRadioButton.setSelected(true);
            setNewValue();
        }

        public void removeUpdate(DocumentEvent evt) {
            userCodeRadioButton.setSelected(true);
            setNewValue();
        }

        public void changedUpdate(DocumentEvent evt) {
        }

        public void actionPerformed(ActionEvent evt) {
            setNewValue();
        }

        private void setNewValue() {
            userCode = userCodeEditorPane.getText();
        }

        private void selectDefaultRadioButton() {
            boolean wasSelected = false;
            for (PropertyEditorElement element : elements) {
                if (element.isInitiallySelected()) {
                    element.getRadioButton().setSelected(true);
                    element.getRadioButton().requestFocus();
                    wasSelected = true;
                    break;
                }
            }
            userCodeRadioButton.setSelected(!wasSelected);
            if (!wasSelected) {
                userCodeEditorPane.requestFocus();
            }
        }
    }
}