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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.SourceGroup;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 *
 * @author  vstejskal
 * @author  Marian Petras
 */
public final class JUnitCfgOfCreate extends SelfResizingPanel
                                    implements ChangeListener {
    
    /** suffix of test classes */
    private static final String TEST_CLASS_SUFFIX = "Test";             //NOI18N
    
    /**
     * nodes selected when the Create Tests action was invoked
     */
    private final Node[] nodes;
    /** whether the tests will be created for multiple classes */
    private final boolean multipleClasses;
    /** whether a single package/folder is selected */
    private boolean singlePackage;
    /** whether a single class is selected */
    private boolean singleClass;
    /** test class name specified in the form (or <code>null</code>) */
    private String testClassName;
    /** registered change listeners */
    private List<ChangeListener> changeListeners;
    /** */
    private String initialMessage;
    
    /**
     * is at least one target folder/source group available?
     *
     * @see  #isAcceptable()
     */
    private boolean hasTargetFolders = false;

    /**
     * is combination of checkbox states acceptable?
     *
     * @see  #isAcceptable()
     */
    private boolean checkBoxesOK;
    
    /**
     * message about invalid configuration of checkboxes
     * in the <em>Method Access Levels</em> group
     */
    private String msgChkBoxesInvalid;
    
    /**
     * is the entered class name non-empty and valid?
     *
     * @see  #isAcceptable()
     */
    private boolean classNameValid;
    
    /**
     * is the current form contents acceptable?
     *
     * @see  #isAcceptable()
     */
    private boolean isAcceptable;
    
    /** layer index for a message about an empty set of target folders */
    private static final int MSG_TYPE_NO_TARGET_FOLDERS = 0;
    /** layer index for a message about invalid configuration of checkboxes */
    private static final int MSG_TYPE_INVALID_CHKBOXES = 1;
    /** layer index for a message about invalid class name */
    private static final int MSG_TYPE_CLASSNAME_INVALID = 2;
    /** layer index for a message about non-default class name */
    private static final int MSG_TYPE_CLASSNAME_NOT_DEFAULT = 3;
    /** */
    private MessageStack msgStack = new MessageStack(4);

    /**
     * Creates a JUnit configuration panel.
     *
     * @param  nodes  nodes selected when the Create Tests action was invoked
     */
    JUnitCfgOfCreate(Node[] nodes) {
        assert (nodes != null) && (nodes.length != 0);
        
        this.nodes = nodes;
        multipleClasses = checkMultipleClasses();
        
        initBundle();
        try {
            initComponents();
            setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 11));
            addAccessibleDescriptions();
            initializeCheckBoxStates();
            fillFormData();
            checkAcceptability();
            setupUserInteraction();
            
            /*
             * checkAcceptability() must not be called
             *        before initializeCheckBoxStates() and fillFormData()
             * setupUserInteraction must not be called
             *        before initializeCheckBoxStates()
             */
            
        } finally {
            unlinkBundle();
        }
    }
    
    private void addAccessibleDescriptions() {
        
        // window
        this.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.AD"));
        
        // text-field and combo-box
        
        if (this.tfClassName != null) {
            this.tfClassName.setToolTipText(
                  bundle.getString("JUnitCfgOfCreate.clsName.toolTip"));//NOI18N
            this.tfClassName.getAccessibleContext().setAccessibleName(
                  bundle.getString("JUnitCfgOfCreate.clsName.AN"));     //NOI18N
            this.tfClassName.getAccessibleContext().setAccessibleDescription(
                  bundle.getString("JUnitCfgOfCreate.clsName.AD"));     //NOI18N
        }
        
        this.cboxLocation.setToolTipText(
                bundle.getString("JUnitCfgOfCreate.location.toolTip")); //NOI18N
        this.cboxLocation.getAccessibleContext().setAccessibleName(
                bundle.getString("JUnitCfgOfCreate.location.AN"));      //NOI18N
        this.cboxLocation.getAccessibleContext().setAccessibleDescription(
                bundle.getString("JUnitCfgOfCreate.location.AD"));      //NOI18N
        
        // check boxes
        this.chkPublic.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkPublic.toolTip"));
        this.chkPublic.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkPublic.AD"));        
        
        this.chkProtected.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkProtected.toolTip"));
        this.chkProtected.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkProtected.AD"));        
        
        this.chkPackage.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkPackage.toolTip"));
        this.chkPackage.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkPackage.AD"));
        
        this.chkComments.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkComments.toolTip"));        
        this.chkComments.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkComments.AD"));
        
        this.chkContent.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkContent.toolTip"));
        this.chkContent.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkContent.AD"));
        
        this.chkJavaDoc.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkJavaDoc.toolTip"));
        this.chkJavaDoc.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkJavaDoc.AD"));
        
        if (multipleClasses) {
            this.chkExceptions.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkExceptions.toolTip"));
            this.chkExceptions.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkExceptions.AD"));
        
            this.chkAbstractImpl.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkAbstractImpl.toolTip"));
            this.chkAbstractImpl.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkAbstractImpl.AD"));
        
            this.chkPackagePrivateClasses.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkPackagePrivateClasses.toolTip"));
            this.chkPackagePrivateClasses.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkPackagePrivateClasses.AD"));
        
            this.chkGenerateSuites.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkGenerateSuites.toolTip"));
            this.chkGenerateSuites.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkGenerateSuites.AD"));
        }
        
    }
    
    /**
     * Checks whether multiple classes may be selected.
     * It also detects whether exactly one package/folder or exactly one class
     * is selected and sets values of variables {@link #singlePackage}
     * and {@link #singleClass} accordingly.
     *
     * @return  <code>false</code> if there is exactly one node selected
     *          and the node represents a single <code>DataObject</code>,
     *          not a folder or another <code>DataObject</code> container;
     *          <code>true</code> otherwise
     */
    private boolean checkMultipleClasses() {
        if (nodes.length > 1) {
            return true;
        }
        
        Lookup nodeLookup = nodes[0].getLookup();
        if (nodeLookup.lookup(DataObject.Container.class) != null) {
            singlePackage = nodeLookup.lookup(DataFolder.class)
                            != null;
            return true;
        }
        
        singleClass = false;
        DataObject dataObj = nodeLookup.lookup(DataObject.class);
        if (dataObj == null) {
            return true;
        }
        
        singleClass = dataObj.getPrimaryFile().isData();
        return !singleClass;
    }
    
    /**
     * Displays a configuration dialog and updates JUnit options according
     * to the user's settings.
     *
     * @param  nodes  nodes selected when the Create Test action was invoked
     */
    boolean configure() {
        
        // create and display the dialog:
        String title = NbBundle.getMessage(JUnitCfgOfCreate.class,
                                           "JUnitCfgOfCreate.Title");   //NOI18N
        ChangeListener changeListener;
        final JButton btnOK = new JButton(
                NbBundle.getMessage(JUnitCfgOfCreate.class, "LBL_OK")); //NOI18N
        btnOK.getAccessibleContext().setAccessibleName(NbBundle.getMessage(JUnitCfgOfCreate.class, "AN_OK"));
        btnOK.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JUnitCfgOfCreate.class, "AD_OK"));
        btnOK.setEnabled(isAcceptable());
        addChangeListener(changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                btnOK.setEnabled(isAcceptable());
            }
        });
        
        Object returned = DialogDisplayer.getDefault().notify(
                new DialogDescriptor (
                        this,
                        title,
                        true,                       //modal
                        new Object[] {btnOK, DialogDescriptor.CANCEL_OPTION},
                        btnOK,                      //initial value
                        DialogDescriptor.DEFAULT_ALIGN,
                        new HelpCtx(JUnitCfgOfCreate.class),
                        (ActionListener) null
                ));
        removeChangeListener(changeListener);
        
        if (returned == btnOK) {
            rememberCheckBoxStates();
            testClassName = (tfClassName != null) ? tfClassName.getText() 
                                                  : null;
            return true;
        }
        return false;
    }
    
    /**
     * Returns whether a test for a single class is to be created.
     *
     * @return  true if there is only one node selected and the node
     *          represents a class
     */
    boolean isSingleClass() {
        return singleClass;
    }
    
    /**
     * Returns the class name entered in the text-field.
     *
     * @return  class name entered in the form,
     *          or <code>null</code> if the form did not contain
     *          the field for entering class name
     */
    String getTestClassName() {
        return testClassName;
    }
    
    /** resource bundle used during initialization of this panel */
    public ResourceBundle bundle;
    
    /**
     * Reads JUnit settings and initializes checkboxes accordingly.
     *
     * @see  #rememberCheckBoxStates
     */
    private void initializeCheckBoxStates() {
        final JUnitSettings settings = JUnitSettings.getDefault();
        
        chkPublic.setSelected(settings.isMembersPublic());
        chkProtected.setSelected(settings.isMembersProtected());
        chkPackage.setSelected(settings.isMembersPackage());
        chkComments.setSelected(settings.isBodyComments());
        chkContent.setSelected(settings.isBodyContent());
        chkJavaDoc.setSelected(settings.isJavaDoc());        
        if (multipleClasses) {
            chkGenerateSuites.setSelected(settings.isGenerateSuiteClasses());        
            chkPackagePrivateClasses.setSelected(
                    settings.isIncludePackagePrivateClasses());
            chkAbstractImpl.setSelected(settings.isGenerateAbstractImpl());
            chkExceptions.setSelected(settings.isGenerateExceptionClasses());
        }
        chkSetUp.setSelected(settings.isGenerateSetUp());
        chkTearDown.setSelected(settings.isGenerateTearDown());
        
        checkChkBoxesStates();
    }
    
    /**
     * Stores settings given by checkbox states to JUnit settings.
     *
     * @see  #initializeCheckBoxStatesf
     */
    private void rememberCheckBoxStates() {
        final JUnitSettings settings = JUnitSettings.getDefault();
        
        settings.setMembersPublic(chkPublic.isSelected());
        settings.setMembersProtected(chkProtected.isSelected());
        settings.setMembersPackage(chkPackage.isSelected());
        settings.setBodyComments(chkComments.isSelected());
        settings.setBodyContent(chkContent.isSelected());
        settings.setJavaDoc(chkJavaDoc.isSelected());
        if (multipleClasses) {
            settings.setGenerateSuiteClasses(chkGenerateSuites.isSelected());
            settings.setIncludePackagePrivateClasses(
                    chkPackagePrivateClasses.isSelected());
            settings.setGenerateAbstractImpl(chkAbstractImpl.isSelected());
            settings.setGenerateExceptionClasses(chkExceptions.isSelected());
        }
        settings.setGenerateSetUp(chkSetUp.isSelected());
        settings.setGenerateTearDown(chkTearDown.isSelected());
    }
    
    /**
     * Loads a resource bundle so that it can be used during intialization
     * of this panel.
     *
     * @see  #unlinkBundle
     */
    private void initBundle() {
        bundle = NbBundle.getBundle(JUnitCfgOfCreate.class);
    }
    
    /**
     * Nulls the resource bundle so that it is not held in memory when it is
     * not used.
     *
     * @see  #initBundle
     */
    private void unlinkBundle() {
        bundle = null;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        
        add(createNameAndLocationPanel(), BorderLayout.NORTH);
        add(createMessagePanel(), BorderLayout.CENTER);
        add(createCodeGenPanel(), BorderLayout.SOUTH);
    }
    
    /**
     */
    private void setupUserInteraction() {
        final ItemListener listener = new CheckBoxListener();

        chkPublic.addItemListener(listener);
        chkProtected.addItemListener(listener);
        chkPackage.addItemListener(listener);
    }
    
    /**
     */
    private void checkChkBoxesStates() {
        checkBoxesOK = chkPublic.isSelected()
                       || chkProtected.isSelected()
                       || chkPackage.isSelected();
        if (checkBoxesOK) {
            setMessage(null, MSG_TYPE_INVALID_CHKBOXES);
        } else {
            if (msgChkBoxesInvalid == null) {
                //PENDING - text of the message:
                msgChkBoxesInvalid = NbBundle.getMessage(
                        JUnitCfgOfCreate.class,
                        "MSG_AllMethodTypesDisabled");                  //NOI18N
            }
            setMessage(msgChkBoxesInvalid, MSG_TYPE_INVALID_CHKBOXES);
        }
    }
    
    /**
     * Listener object that listens on state changes of some check-boxes.
     */
    private final class CheckBoxListener implements ItemListener {
        public CheckBoxListener () {}
        
        public void itemStateChanged(ItemEvent e) {
            final Object source = e.getSource();
            
            assert source == chkPublic
                   || source == chkProtected
                   || source == chkPackage;
            checkChkBoxesStates();
            checkAcceptability();
        }
        
    }
    
    /**
     */
    private Component createNameAndLocationPanel() {
        JPanel panel = new JPanel();
        
        final boolean askForClassName = singleClass;
        
        JLabel lblClassToTest = new JLabel();
        JLabel lblClassName = askForClassName ? new JLabel() : null;
        JLabel lblLocation = new JLabel();
        
        String classToTestKey = singlePackage
                                ? "LBL_PackageToTest"                   //NOI18N
                                : singleClass
                                  ? "LBL_ClassToTest"                   //NOI18N
                                  : "LBL_MultipleClassesSelected";      //NOI18N
                                    
        Mnemonics.setLocalizedText(
                lblClassToTest,
                NbBundle.getMessage(getClass(), classToTestKey));
        if (askForClassName) {
            Mnemonics.setLocalizedText(
                    lblClassName,
                    NbBundle.getMessage(getClass(), "LBL_ClassName"));  //NOI18N
        }
        Mnemonics.setLocalizedText(
                lblLocation,
                NbBundle.getMessage(getClass(), "LBL_Location"));       //NOI18N
        
        if (singlePackage || singleClass) {
            lblClassToTestValue = new JLabel();
        }
        if (askForClassName) {
            tfClassName = new ClassNameTextField();
            tfClassName.setChangeListener(this);
        }
        cboxLocation = new JComboBox();
        
        if (askForClassName) {
            lblClassName.setLabelFor(tfClassName);
        }
        lblLocation.setLabelFor(cboxLocation);
        
        if (lblClassToTestValue != null) {
            Font labelFont = javax.swing.UIManager.getDefaults()
                             .getFont("TextField.font");                //NOI18N
            if (labelFont != null) {
                lblClassToTestValue.setFont(labelFont);
            }
        }
        
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.anchor = GridBagConstraints.WEST;
        gbcLeft.insets.bottom = 12;
        gbcLeft.insets.right = 6;
        
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.anchor = GridBagConstraints.WEST;
        gbcRight.insets.bottom = 12;
        gbcRight.weightx = 1.0f;
        gbcRight.fill = GridBagConstraints.BOTH;
        gbcRight.gridwidth = GridBagConstraints.REMAINDER;
        
        if (lblClassToTestValue != null) {
            panel.add(lblClassToTest,      gbcLeft);
            panel.add(lblClassToTestValue, gbcRight);
        } else {
            panel.add(lblClassToTest,   gbcRight);
        }
        if (askForClassName) {
            panel.add(lblClassName,     gbcLeft);
            panel.add(tfClassName,      gbcRight);
        }
        gbcLeft.insets.bottom = 0;
        gbcRight.insets.bottom = 0;
        panel.add(lblLocation,      gbcLeft);
        panel.add(cboxLocation,     gbcRight);
        
        return panel;
    }
    
    /**
     */
    private void checkClassNameValidity() {
        if (tfClassName == null) {
            classNameValid = true;
            return;
        }
        
        String key = null;
        final int state = tfClassName.getStatus();
        switch (state) {
            case ClassNameTextField.STATUS_EMPTY:
                //PENDING - polish the message:
                key = "MSG_ClassnameMustNotBeEmpty";                    //NOI18N
                break;
            case ClassNameTextField.STATUS_INVALID:
                //PENDING - polish the message:
                key = "MSG_InvalidClassName";                           //NOI18N
                break;
            case ClassNameTextField.STATUS_VALID_NOT_DEFAULT:
                //PENDING - polish the message:
                key = "MSG_ClassNameNotDefault";                        //NOI18N
                break;
        }
        if (state != ClassNameTextField.STATUS_VALID_NOT_DEFAULT) {
            setMessage(null, MSG_TYPE_CLASSNAME_NOT_DEFAULT);
        }
        setMessage((key != null)
                           ? NbBundle.getMessage(getClass(), key)
                           : null,
                   MSG_TYPE_CLASSNAME_INVALID);
        
        classNameValid =
                (state == ClassNameTextField.STATUS_VALID)
                || (state == ClassNameTextField.STATUS_VALID_NOT_DEFAULT);
    }
    
    /**
     * This method gets called if status of contents of the Class Name
     * text field changes. See <code>STATUS_xxx</code> constants
     * in class <code>ClassNameTextField</code>.
     *
     * @param  e  event describing the state change event
     *            (unused in this method)
     */
    public void stateChanged(ChangeEvent e) {
        checkClassNameValidity();
        checkAcceptability();
    }
    
    /**
     */
    private void checkAcceptability() {
        final boolean wasAcceptable = isAcceptable;
        isAcceptable = hasTargetFolders && classNameValid && checkBoxesOK;
        if (isAcceptable != wasAcceptable) {
            fireStateChange();
        }
    }
    
    /**
     * Are the values filled in the form acceptable?
     *
     * @see  #addChangeListener
     */
    private boolean isAcceptable() {
        return isAcceptable;
    }
    
    /**
     * This method is called the first time this panel's children are painted.
     * By default, this method just calls {@link #adjustWindowSize()}.
     *
     * @param  g  <code>Graphics</code> used to paint this panel's children
     */
    protected void paintedFirstTime(java.awt.Graphics g) {
        if (initialMessage != null) {
            displayMessage(initialMessage);
            initialMessage = null;
        }
    }
    
    /**
     * Displays a given message in the message panel and resizes the dialog
     * if necessary. If the message cannot be displayed immediately,
     * because of this panel not displayed (painted) yet, displaying the message
     * is deferred until this panel is painted.
     *
     * @param  message  message to be displayed, or <code>null</code> if
     *                  the currently displayed message (if any) should be
     *                  removed
     */
    private void setMessage(final String message, final int msgType) {
        String msgToDisplay = msgStack.setMessage(msgType, message);
        if (msgToDisplay == null) {
            return;                     //no change
        }

        /* display the message: */
        if (!isPainted()) {
            initialMessage = msgToDisplay;
        } else {
            displayMessage(msgToDisplay);
        }
    }
    
    /**
     * Displays a given message in the message panel and resizes the dialog
     * if necessary.
     *
     * @param  message  message to be displayed, or <code>null</code> if
     *                  the currently displayed message (if any) should be
     *                  removed
     * @see  #adjustWindowSize()
     */
    private void displayMessage(String message) {
        if (message == null) {
            message = "";                                               //NOI18N
        }
        
        txtAreaMessage.setText(message);
        adjustWindowSize();
    }
    
    /**
     */
    private Component createMessagePanel() {
        txtAreaMessage = (JTextArea) GuiUtils.createMultilineLabel(""); //NOI18N
        
        Color color = UIManager.getColor("nb.errorForeground");         //NOI18N
        if (color == null) {
            color = new Color(89, 79, 191);   //RGB suggested by Bruce in #28466
        }
        txtAreaMessage.setForeground(color);

        return txtAreaMessage;
    }
    
    /**
     * Creates a panel containing controls for settings code generation options.
     *
     * @return   created panel
     */
    private Component createCodeGenPanel() {
        
        /* create the components: */
        String[] chkBoxIDs;
        JCheckBox[] chkBoxes;
        if (multipleClasses) {
            chkBoxIDs = new String[] {
                GuiUtils.CHK_PUBLIC,
                GuiUtils.CHK_PROTECTED,
                GuiUtils.CHK_PACKAGE,
                GuiUtils.CHK_PACKAGE_PRIVATE_CLASSES,
                GuiUtils.CHK_ABSTRACT_CLASSES,
                GuiUtils.CHK_EXCEPTION_CLASSES,
                GuiUtils.CHK_SUITES,
                GuiUtils.CHK_SETUP,
                GuiUtils.CHK_TEARDOWN,
                GuiUtils.CHK_METHOD_BODIES,
                GuiUtils.CHK_JAVADOC,
                GuiUtils.CHK_HINTS
            };
        } else {
            chkBoxIDs = new String[] {
                GuiUtils.CHK_PUBLIC,
                GuiUtils.CHK_PROTECTED,
                GuiUtils.CHK_PACKAGE,
                null, // CHK_PACKAGE_PRIVATE_CLASSES,
                null, // CHK_ABSTRACT_CLASSES,
                null, // CHK_EXCEPTION_CLASSES,
                null, // CHK_SUITES,
                GuiUtils.CHK_SETUP,
                GuiUtils.CHK_TEARDOWN,
                GuiUtils.CHK_METHOD_BODIES,
                GuiUtils.CHK_JAVADOC,
                GuiUtils.CHK_HINTS
            };
        }
        chkBoxes = GuiUtils.createCheckBoxes(chkBoxIDs);
        int i = 0;
        chkPublic           = chkBoxes[i++];
        chkProtected        = chkBoxes[i++];
        chkPackage          = chkBoxes[i++];
        chkPackagePrivateClasses = chkBoxes[i++];       //may be null
        chkAbstractImpl     = chkBoxes[i++];            //may be null
        chkExceptions       = chkBoxes[i++];            //may be null
        chkGenerateSuites   = chkBoxes[i++];            //may be null
        chkSetUp            = chkBoxes[i++];
        chkTearDown         = chkBoxes[i++];        
        chkContent          = chkBoxes[i++];
        chkJavaDoc          = chkBoxes[i++];
        chkComments         = chkBoxes[i++];
        
        /* create groups of checkboxes: */
        JComponent methodAccessLevels = GuiUtils.createChkBoxGroup(
                bundle.getString("JUnitCfgOfCreate.groupAccessLevels"), //NOI18N
                new JCheckBox[] {chkPublic, chkProtected, chkPackage});
        JComponent classTypes = null;
        JComponent optionalClasses = null;
        if (multipleClasses) {
            classTypes = GuiUtils.createChkBoxGroup(
                bundle.getString("JUnitCfgOfCreate.groupClassTypes"),   //NOI18N
                new JCheckBox[] {chkPackagePrivateClasses,
                                 chkAbstractImpl, chkExceptions});
            optionalClasses = GuiUtils.createChkBoxGroup(
                bundle.getString("JUnitCfgOfCreate.groupOptClasses"),   //NOI18N
                new JCheckBox[] {chkGenerateSuites});
        }
        JComponent optionalCode = GuiUtils.createChkBoxGroup(
                bundle.getString("JUnitCfgOfCreate.groupOptCode"),      //NOI18N
                new JCheckBox[] {chkSetUp, chkTearDown, chkContent});
        JComponent optionalComments = GuiUtils.createChkBoxGroup(
                bundle.getString("JUnitCfgOfCreate.groupOptComments"),  //NOI18N
                new JCheckBox[] {chkJavaDoc, chkComments});
        
        /* create the left column of options: */
        Box leftColumn = Box.createVerticalBox();
        leftColumn.add(methodAccessLevels);
        if (multipleClasses) {
            leftColumn.add(Box.createVerticalStrut(11));
            leftColumn.add(classTypes);
        } else {
            /*
             * This strut ensures that width of the left column is not limited.
             * If it was limited, the rigth column would not move when the
             * dialog is horizontally resized.
             */
            leftColumn.add(Box.createVerticalStrut(0));
        }
        leftColumn.add(Box.createVerticalGlue());
        
        /* create the right column of options: */
        Box rightColumn = Box.createVerticalBox();
        if (multipleClasses) {
            rightColumn.add(optionalClasses);
            rightColumn.add(Box.createVerticalStrut(11));
        }
        rightColumn.add(optionalCode);
        rightColumn.add(Box.createVerticalStrut(11));
        rightColumn.add(optionalComments);
        rightColumn.add(Box.createVerticalGlue());
        
        /* compose the final panel: */
        //JPanel jpCodeGen = new SizeRestrictedPanel(false, true);
        JPanel jpCodeGen = new JPanel();
        jpCodeGen.setLayout(new BoxLayout(jpCodeGen, BoxLayout.X_AXIS));
        jpCodeGen.add(leftColumn);
        jpCodeGen.add(Box.createHorizontalStrut(24));
        jpCodeGen.add(rightColumn);
        
        /* decorate the panel: */
        addTitledBorder(jpCodeGen,
                  new Insets(12, 12, 11, 12),
                  bundle.getString("JUnitCfgOfCreate.jpCodeGen.title"));//NOI18N
        
        /* tune the layout: */
        methodAccessLevels.setAlignmentX(0.0f);
        if (multipleClasses) {
            classTypes.setAlignmentX(0.0f);
            optionalClasses.setAlignmentX(0.0f);
        }
        optionalCode.setAlignmentX(0.0f);
        optionalComments.setAlignmentX(0.0f);
        
        return jpCodeGen;
    }
    
    /**
     * Adds a border and a title around a given component.
     * If the component already has some border, it is overridden (not kept).
     *
     * @param  component  component the border and title should be added to
     * @param  insets  insets between the component and the titled border
     * @param  title  text of the title
     */
    private static void addTitledBorder(JComponent component,
                                        Insets insets,
                                        String title) {
        Border insideBorder = BorderFactory.createEmptyBorder(
                insets.top, insets.left, insets.bottom, insets.right);
        Border outsideBorder = new TitledBorder(
                BorderFactory.createEtchedBorder(), title);
        component.setBorder(new CompoundBorder(outsideBorder, insideBorder));
    }
    
    /**
     */
    FileObject getTargetFolder() {
        Object selectedLocation = cboxLocation.getSelectedItem();
        
        if (selectedLocation == null) {
            return null;
        }
        
        if (selectedLocation instanceof SourceGroup) {
            return ((SourceGroup) selectedLocation).getRootFolder();
        }
        assert selectedLocation instanceof FileObject;      //root folder
        return (FileObject) selectedLocation;
    }
    
    /**
     * Initializes form in the Test Settings panel of the dialog.
     */
    private void fillFormData() {
        final DataObject dataObj = (DataObject)
                                  nodes[0].getLookup().lookup(DataObject.class);
        final FileObject fileObj = dataObj.getPrimaryFile();
        
        if (singleClass) {
            assert nodes.length == 1;
            
            ClassPath cp = ClassPath.getClassPath(fileObj, ClassPath.SOURCE);
            String className = cp.getResourceName(fileObj, '.', false);
            lblClassToTestValue.setText(className);
            
            if (tfClassName != null) {
                String prefilledName = className + TEST_CLASS_SUFFIX;
                tfClassName.setText(prefilledName);
                tfClassName.setDefaultText(prefilledName);
                tfClassName.setCaretPosition(prefilledName.length());
            }
        } else if (singlePackage) {
            assert nodes.length == 1;
            
            ClassPath cp = ClassPath.getClassPath(fileObj, ClassPath.SOURCE);
            String packageName = cp.getResourceName(fileObj, '.', true);
            if (packageName.length() == 0) {
                packageName = NbBundle.getMessage(
                        getClass(),
                        "DefaultPackageName");                          //NOI18N
            }
            lblClassToTestValue.setText(packageName);
        } else {
            //PENDING
        }
        
        setupLocationChooser(fileObj);
        
        checkClassNameValidity();
    }
    
    /**
     */
    private void setupLocationChooser(FileObject refFileObject) {
        Object[] targetFolders = TestUtil.getTestTargets(refFileObject);
        if (targetFolders.length != 0) {
            hasTargetFolders = true;
            cboxLocation.setModel(new DefaultComboBoxModel(targetFolders));
            cboxLocation.setRenderer(new LocationChooserRenderer());
        } else {
            hasTargetFolders = false;
            //PENDING - message text:
            String msgNoTargetsFound = NbBundle.getMessage(
                                        getClass(),
                                        refFileObject.isFolder()
                                                ? "MSG_NoTestTarget_Fo" //NOI18N
                                                : "MSG_NoTestTarget_Fi",//NOI18N
                                        refFileObject.getNameExt());
            setMessage(msgNoTargetsFound, MSG_TYPE_NO_TARGET_FOLDERS);
            disableComponents();
        }
    }
    
    /**
     * Renderer which specially handles values of type
     * <code>SourceGroup</code> and <code>FileObject</code>.
     * It displays display names of these objects, instead of their default
     * string representation (<code>toString()</code>).
     *
     * @see  SourceGroup#getDisplayName()
     * @see  FileUtil#getFileDisplayName(FileObject)
     */
    private final class LocationChooserRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public LocationChooserRenderer () {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            String text = value instanceof SourceGroup
                        ? ((SourceGroup) value).getDisplayName()
                        : value instanceof FileObject
                              ?  FileUtil.getFileDisplayName((FileObject) value)
                              : value.toString();
            setText(text);
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
        
        // #93658: GTK needs name to render cell renderer "natively"
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
            
    }

    /**
     * Registers a change listener.
     * Registered change listeners are notified when acceptability
     * of values in the form changes.
     *
     * @param  l  listener to be registered
     * @see  #isAcceptable
     * @see  #removeChangeListener
     */
    private void addChangeListener(ChangeListener l) {
        if (changeListeners == null) {
            changeListeners = new ArrayList<ChangeListener>(3);
        }
        changeListeners.add(l);
    }
    
    /**
     * Unregisters the given change listener.
     * If the given listener has not been registered before, calling this
     * method does not have any effect.
     *
     * @param  l  change listener to be removed
     * @see  #addChangeListener
     */
    private void removeChangeListener(ChangeListener l) {
        if (changeListeners != null
                && changeListeners.remove(l)
                && changeListeners.isEmpty()) {
            changeListeners = null;
        }
    }
    
    /**
     * Notifies all registered change listeners about a change.
     *
     * @see  #addChangeListener
     */
    private void fireStateChange() {
        if (changeListeners != null) {
            ChangeEvent e = new ChangeEvent(this);
            for (Iterator i = changeListeners.iterator(); i.hasNext(); ) {
                ((ChangeListener) i.next()).stateChanged(e);
            }
        }
    }
    
    /**
     * Disables all interactive visual components of this dialog
     * except the OK, Cancel and Help buttons.
     */
    private void disableComponents() {
        final Stack<Container> stack = new Stack<Container>();
        stack.push(this);
        
        while (!stack.empty()) {
            Container container = stack.pop();
            Component comps[] = container.getComponents();
            for (int i = 0; i < comps.length; i++) {
                final java.awt.Component comp = comps[i];
                
                if (comp == txtAreaMessage) {
                    continue;
                }
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    stack.push(panel);

                    final Border border = panel.getBorder();
                    if (border != null) {
                        disableBorderTitles(border);
                    }
                    continue;
                }
                comp.setEnabled(false);
                if (comp instanceof java.awt.Container) {
                    Container nestedCont = (Container) comp;
                    if (nestedCont.getComponentCount() != 0) {
                        stack.push(nestedCont);
                    }
                }
            }
        }
    }
    
    /**
     */
    private static void disableBorderTitles(Border border) {
        
        if (border instanceof TitledBorder) {
            disableBorderTitle((TitledBorder) border);
            return;
        }
        
        if (!(border instanceof CompoundBorder)) {
            return;
        }
        
        Stack<CompoundBorder> stack = new Stack<CompoundBorder>();
        stack.push((CompoundBorder) border);
        while (!stack.empty()) {
            CompoundBorder cb = stack.pop();
            
            Border b;
            b = cb.getOutsideBorder();
            if (b instanceof CompoundBorder) {
                stack.push((CompoundBorder) b);
            } else if (b instanceof TitledBorder) {
                disableBorderTitle((TitledBorder) b);
            }
            
            b = cb.getInsideBorder();
            if (b instanceof CompoundBorder) {
                stack.push((CompoundBorder) b);
            } else if (b instanceof TitledBorder) {
                disableBorderTitle((TitledBorder) b);
            }
        }
    }
    
    /**
     */
    private static void disableBorderTitle(TitledBorder border) {
        final Color color = UIManager.getColor(
                "Label.disabledForeground");                        //NOI18N
        if (color != null) {
            border.setTitleColor(color);
        }
    }

    private JLabel lblClassToTestValue;
    private ClassNameTextField tfClassName;
    private JTextArea txtAreaMessage;
    private JComboBox cboxLocation;
    private JCheckBox chkAbstractImpl;
    private JCheckBox chkComments;
    private JCheckBox chkContent;
    private JCheckBox chkExceptions;
    private JCheckBox chkGenerateSuites;
    private JCheckBox chkJavaDoc;
    private JCheckBox chkPackage;
    private JCheckBox chkPackagePrivateClasses;
    private JCheckBox chkProtected;
    private JCheckBox chkPublic;
    private JCheckBox chkSetUp;
    private JCheckBox chkTearDown;

}
