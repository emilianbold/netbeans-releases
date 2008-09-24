/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.vmd.midp.propertyeditors.api.usercode;

import java.awt.event.FocusEvent;
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
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;
import java.util.*;
import javax.swing.text.Keymap;
import javax.swing.undo.UndoManager;
import org.netbeans.editor.ActionFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.vmd.midp.propertyeditors.CleanUp;

/**
 * This class allows create PropertyEditor which supports User Code.
 *
 * See for example PropertyEditorString.
 *
 * <b>WARNING - while overriding init method, you have to call super.init() method too.</b>
 *
 * @author Anton Chechel
 */
public abstract class PropertyEditorUserCode extends DesignPropertyEditor implements PropertyEditorMessageAwareness {

    public static final PropertyValue NULL_VALUE = PropertyValue.createNull();
    public static final String NULL_TEXT = NbBundle.getMessage(PropertyEditorUserCode.class, "LBL_STRING_NULL"); // NOI18N
    public static final String USER_CODE_TEXT = NbBundle.getMessage(PropertyEditorUserCode.class, "LBL_STRING_USER_CODE"); // NOI18N
    private static final Icon ICON_WARNING = new ImageIcon(Utilities.loadImage("org/netbeans/modules/vmd/midp/resources/warning.gif")); // NOI18N
    private static final Icon ICON_ERROR = new ImageIcon(Utilities.loadImage("org/netbeans/modules/vmd/midp/resources/error.gif")); // NOI18N
    private CustomEditor customEditor;
    private JRadioButton userCodeRadioButton;
    private JLabel messageLabel;
    private String userCodeLabel;
    private String userCode = ""; // NOI18N
    protected WeakReference<DesignComponent> component;

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (customEditor != null) {
            customEditor.cleanUp();
            customEditor = null;
        }
        userCodeRadioButton = null;
        messageLabel = null;
        this.component = null;
    }

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
     * This method should be invoked from subclass to init elements. Develpers can controll position of 
     * the Elements by seting index as a Integer value in the elements map. 
     */
    protected void initElements(LinkedHashMap<PropertyEditorElement, Integer> elements) {
        customEditor.init(elements);
    }

    /**
     * It returns radio buttom added to PropertyEditorUserCode
     * @return null if radio burron is not created yet
     */
    protected JRadioButton getUserCodeRadioButton() {
        return userCodeRadioButton;
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
     * If you averide this method CALL super.getCustomEditor!!!!!
     */
    @Override
    public Component getCustomEditor() {
        initCustomEditor();
        return customEditor;
    }

    private void initCustomEditor() {
        PropertyValue value = (PropertyValue) super.getValue();
        if (isCurrentValueAUserCodeType()) {
            customEditor.setUserCodeText(value.getUserCode());
            customEditor.updateState(null);
        } else {
            customEditor.setUserCodeText(null);
            customEditor.updateState(value);
        }
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
            customEditor.setNewValue();
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
    public void displayWarning(String message) {
        messageLabel.setText(message);
        messageLabel.setIcon(ICON_WARNING);
    }

    /**
     * Displays error message on the custom property editor panel
     * @param message to be displayed
     */
    public void displayError(String message) {
        messageLabel.setText(message);
        messageLabel.setIcon(ICON_ERROR);
    }

    /**
     * Clears error/warning message on the custom property editor panel
     */
    public void clearErrorStatus() {
        messageLabel.setText(" "); //NOI18N
        messageLabel.setIcon(null);
    }

    private static void setupTextUndoRedo(javax.swing.text.JTextComponent editor) {
        String os = System.getProperty("os.name").toLowerCase(); //NOI18N

        KeyStroke[] undoKeys = null;
        KeyStroke[] redoKeys = null;

        if (os.indexOf("mac") != -1) { //NOI18N
            undoKeys = new KeyStroke[]{KeyStroke.getKeyStroke(KeyEvent.VK_UNDO, 0),
                        KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.META_MASK)
                    };
            redoKeys = new KeyStroke[]{KeyStroke.getKeyStroke(KeyEvent.VK_AGAIN, 0),
                        KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.META_MASK)
                    };
        } else {
            undoKeys = new KeyStroke[]{KeyStroke.getKeyStroke(KeyEvent.VK_UNDO, 0),
                        KeyStroke.getKeyStroke(KeyEvent.VK_Z, 130)
                    };
            redoKeys = new KeyStroke[]{KeyStroke.getKeyStroke(KeyEvent.VK_AGAIN, 0),
                        KeyStroke.getKeyStroke(KeyEvent.VK_Y, 130)
                    };
        }

        Keymap keymap = editor.getKeymap();
        Action undoAction = new ActionFactory.UndoAction();
        for (KeyStroke k : undoKeys) {
            keymap.removeKeyStrokeBinding(k);
            keymap.addActionForKeyStroke(k, undoAction);
        }
        Action redoAction = new ActionFactory.RedoAction();
        for (KeyStroke k : redoKeys) {
            keymap.removeKeyStrokeBinding(k);
            keymap.addActionForKeyStroke(k, redoAction);
        }
        Object currentUM = editor.getDocument().getProperty(BaseDocument.UNDO_MANAGER_PROP);
        if (currentUM instanceof UndoManager) {
            editor.getDocument().removeUndoableEditListener((UndoManager) currentUM);
        }
        UndoManager um = new UndoManager();
        editor.getDocument().addUndoableEditListener(um);
        editor.getDocument().putProperty(BaseDocument.UNDO_MANAGER_PROP, um);
    }

    private final class CustomEditor extends JPanel implements DocumentListener, ActionListener, FocusListener {

        private Collection<PropertyEditorElement> elements;
        private Map<PropertyEditorElement, Integer> elementsMap;
        private JEditorPane userCodeEditorPane;

        public void init(Collection<PropertyEditorElement> elements) {
            this.elements = elements;
            initComponents();
        }

        public void init(Map<PropertyEditorElement, Integer> elementsMap) {
            this.elementsMap = elementsMap;
            this.elements = elementsMap.keySet();
            initComponents();
        }

        void cleanUp() {
            if (elementsMap != null) {
                for (PropertyEditorElement pee : elementsMap.keySet()) {
                    if (pee instanceof CleanUp) {
                        ((CleanUp) pee).clean(null);
                    }
                 }
                elementsMap.clear();
                elementsMap = null;
            }
            if (elements != null) {
                elements = null;
            }
            if (userCodeEditorPane != null && userCodeEditorPane.getDocument() != null) {
                userCodeEditorPane.getDocument().removeDocumentListener(this);
                userCodeEditorPane = null;
            }
            this.removeAll();
        }

        private void initComponents() {
            setLayout(new GridBagLayout());
            ButtonGroup buttonGroup = new ButtonGroup();
            GridBagConstraints constraints = new GridBagConstraints();
            boolean isAnyElementVerticallyResizable = false;
            for (PropertyEditorElement element : elements) {
                if (elementsMap == null || elementsMap != null && elementsMap.get(element) == null) {
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
                }
                if (element.isVerticallyResizable()) {
                    isAnyElementVerticallyResizable = true;
                }
            }

            userCodeRadioButton = new JRadioButton();
            Mnemonics.setLocalizedText(userCodeRadioButton, NbBundle.getMessage(
                    PropertyEditorUserCode.class, "LBL_USER_CODE", userCodeLabel)); // NOI18N
            userCodeRadioButton.getAccessibleContext().setAccessibleName(
                    NbBundle.getMessage(PropertyEditorUserCode.class, "ACSN_USER_CODE", userCodeLabel)); //NOI18N
            userCodeRadioButton.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(PropertyEditorUserCode.class, "ACSD_USER_CODE", userCodeLabel)); //NOI18N
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
            userCodeEditorPane.getAccessibleContext().setAccessibleName(
                    userCodeRadioButton.getAccessibleContext().getAccessibleName());
            userCodeEditorPane.getAccessibleContext().setAccessibleDescription(
                    userCodeRadioButton.getAccessibleContext().getAccessibleDescription());
            userCodeEditorPane.addFocusListener(this);
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

            for (PropertyEditorElement element : elements) {
                if (elementsMap != null && elementsMap.get(element) != null) {
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
                }
                if (element.isVerticallyResizable()) {
                    isAnyElementVerticallyResizable = true;
                }
            }


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

        public void initRetoucheStuff() {
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
                PropertyEditorUserCode.setupTextUndoRedo(userCodeEditorPane);
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
            if (userCodeEditorPane.hasFocus()) {
                userCodeRadioButton.setSelected(true);
                setNewValue();
            }
        }

        public void removeUpdate(DocumentEvent evt) {
            if (userCodeEditorPane.hasFocus()) {
                userCodeRadioButton.setSelected(true);
                setNewValue();
            }
        }

        public void changedUpdate(DocumentEvent evt) {
        }

        public void actionPerformed(ActionEvent evt) {
            setNewValue();
        }

        @Override
        public void addNotify() {
            customEditor.initRetoucheStuff();
            super.addNotify();
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

        public void focusGained(FocusEvent e) {
            if (e.getSource() == userCodeEditorPane) {
                userCodeRadioButton.setSelected(true);
            }

        }

        public void focusLost(FocusEvent e) {
        }
    }
}
