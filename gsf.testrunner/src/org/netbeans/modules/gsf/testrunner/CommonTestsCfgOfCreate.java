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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.gsf.testrunner;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.modules.gsf.testrunner.api.*;
import org.netbeans.modules.gsf.testrunner.plugin.CommonSettingsProvider;
import org.netbeans.modules.gsf.testrunner.plugin.CommonTestUtilProvider;
import org.netbeans.modules.gsf.testrunner.plugin.GuiUtilsProvider;
import org.netbeans.modules.gsf.testrunner.plugin.RootsProvider;
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
import org.openide.util.NbPreferences;


/**
 *
 * @author  vstejskal
 * @author  Marian Petras
 */
@SuppressWarnings("serial")
public class CommonTestsCfgOfCreate extends SelfResizingPanel implements ChangeListener {
    
    /** suffix of test classes */
    private static final String TEST_CLASS_SUFFIX = "Test";             //NOI18N
    
    /**
     * nodes selected when the Create Tests action was invoked
     */
    private  Node[] nodes;
    /** whether the tests will be created for multiple classes */
    private  boolean multipleClasses;
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
    /** */
    private List<String> testingFrameworks;
//    public static final String JUNIT_TEST_FRAMEWORK = "JUnit";             //NOI18N
//    public static final String TESTNG_TEST_FRAMEWORK = "TestNG";             //NOI18N
    private String selectedTestingFramework = null;
    public static final String PROP_TESTING_FRAMEWORK = "testingFramework";  //NOI18N
    
    /**
     * is at least one target folder/source group available?
     *
     * @see  #isAcceptable()
     */
    private boolean hasTargetFolders = false;

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
    /** layer index for a message about invalid class name */
    private static final int MSG_TYPE_CLASSNAME_INVALID = 1;
    /** layer index for a message about non-default class name */
    private static final int MSG_TYPE_CLASSNAME_NOT_DEFAULT = 2;
    /** layer index for a message about modified files */
    private static final int MSG_TYPE_MODIFIED_FILES = 3;
    /** */
    private MessageStack msgStack = new MessageStack(4);

    private Collection<SourceGroup> createdSourceRoots = new ArrayList<SourceGroup>();
    
    public CommonTestsCfgOfCreate(Node[] nodes) {
        assert (nodes != null) && (nodes.length != 0);
        this.nodes = nodes;
    }

    /**
     * Creates a configuration panel.
     *
     * @param nodes  nodes selected when the Create Tests action was invoked
     * @param isShowMsgFilesWillBeSaved if {@code true} then a warning message
     *        like "Warning: All modified files will be saved." will be
     *        displayed on the panel, otherwise (i.e. if {@code false}) then
     *        the message won't be displayed.
     */
    public void createCfgPanel(boolean isShowMsgFilesWillBeSaved) {
//        assert (nodes != null) && (nodes.length != 0);
//        this.nodes = nodes;
        multipleClasses = checkMultipleClasses();
        
        initBundle();
        try {
            initComponents();
            if(isShowMsgFilesWillBeSaved) {
                String msg = bundle.getString("MSG_MODIFIED_FILES"); // NOI18N
                setMessage(msg, MSG_TYPE_MODIFIED_FILES);
            }
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
        this.getAccessibleContext().setAccessibleDescription(bundle.getString("CommonTestsCfgOfCreate.AD"));
        
        // text-field and combo-box
        
        if (this.tfClassName != null) {
            this.tfClassName.setToolTipText(
                  bundle.getString("CommonTestsCfgOfCreate.clsName.toolTip"));//NOI18N
            this.tfClassName.getAccessibleContext().setAccessibleName(
                  bundle.getString("CommonTestsCfgOfCreate.clsName.AN"));     //NOI18N
            this.tfClassName.getAccessibleContext().setAccessibleDescription(
                  bundle.getString("CommonTestsCfgOfCreate.clsName.AD"));     //NOI18N
        }
        
        this.cboxLocation.setToolTipText(
                bundle.getString("CommonTestsCfgOfCreate.location.toolTip")); //NOI18N
        this.cboxLocation.getAccessibleContext().setAccessibleName(
                bundle.getString("CommonTestsCfgOfCreate.location.AN"));      //NOI18N
        this.cboxLocation.getAccessibleContext().setAccessibleDescription(
                bundle.getString("CommonTestsCfgOfCreate.location.AD"));      //NOI18N
        
        this.cboxLocation.setToolTipText(
                bundle.getString("CommonTestsCfgOfCreate.framework.toolTip")); //NOI18N
        this.cboxLocation.getAccessibleContext().setAccessibleName(
                bundle.getString("CommonTestsCfgOfCreate.framework.AN"));      //NOI18N
        this.cboxLocation.getAccessibleContext().setAccessibleDescription(
                bundle.getString("CommonTestsCfgOfCreate.framework.AD"));      //NOI18N
        
        // check boxes
        this.chkPublic.setToolTipText(bundle.getString("CommonTestsCfgOfCreate.chkPublic.toolTip"));
        this.chkPublic.getAccessibleContext().setAccessibleDescription(bundle.getString("CommonTestsCfgOfCreate.chkPublic.AD"));        
        
        this.chkProtected.setToolTipText(bundle.getString("CommonTestsCfgOfCreate.chkProtected.toolTip"));
        this.chkProtected.getAccessibleContext().setAccessibleDescription(bundle.getString("CommonTestsCfgOfCreate.chkProtected.AD"));        
        
        this.chkPackage.setToolTipText(bundle.getString("CommonTestsCfgOfCreate.chkPackage.toolTip"));
        this.chkPackage.getAccessibleContext().setAccessibleDescription(bundle.getString("CommonTestsCfgOfCreate.chkPackage.AD"));
        
        this.chkComments.setToolTipText(bundle.getString("CommonTestsCfgOfCreate.chkComments.toolTip"));        
        this.chkComments.getAccessibleContext().setAccessibleDescription(bundle.getString("CommonTestsCfgOfCreate.chkComments.AD"));
        
        this.chkContent.setToolTipText(bundle.getString("CommonTestsCfgOfCreate.chkContent.toolTip"));
        this.chkContent.getAccessibleContext().setAccessibleDescription(bundle.getString("CommonTestsCfgOfCreate.chkContent.AD"));
        
        this.chkJavaDoc.setToolTipText(bundle.getString("CommonTestsCfgOfCreate.chkJavaDoc.toolTip"));
        this.chkJavaDoc.getAccessibleContext().setAccessibleDescription(bundle.getString("CommonTestsCfgOfCreate.chkJavaDoc.AD"));
        
        if (multipleClasses) {
            this.chkExceptions.setToolTipText(bundle.getString("CommonTestsCfgOfCreate.chkExceptions.toolTip"));
            this.chkExceptions.getAccessibleContext().setAccessibleDescription(bundle.getString("CommonTestsCfgOfCreate.chkExceptions.AD"));
        
            this.chkAbstractImpl.setToolTipText(bundle.getString("CommonTestsCfgOfCreate.chkAbstractImpl.toolTip"));
            this.chkAbstractImpl.getAccessibleContext().setAccessibleDescription(bundle.getString("CommonTestsCfgOfCreate.chkAbstractImpl.AD"));
        
            this.chkPackagePrivateClasses.setToolTipText(bundle.getString("CommonTestsCfgOfCreate.chkPackagePrivateClasses.toolTip"));
            this.chkPackagePrivateClasses.getAccessibleContext().setAccessibleDescription(bundle.getString("CommonTestsCfgOfCreate.chkPackagePrivateClasses.AD"));
        
            this.chkGenerateSuites.setToolTipText(bundle.getString("CommonTestsCfgOfCreate.chkGenerateSuites.toolTip"));
            this.chkGenerateSuites.getAccessibleContext().setAccessibleDescription(bundle.getString("CommonTestsCfgOfCreate.chkGenerateSuites.AD"));
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
    public boolean configure() {
        
        String title = "";
        String btnTxt = "";
        String btnAN = "";
        String btnAD = "";
        Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
        for (GuiUtilsProvider provider : providers) {
            title = provider.getMessageFor("CommonTestsCfgOfCreate.Title");   //NOI18N
            btnTxt = provider.getMessageFor("LBL_OK");   //NOI18N
            btnAN = provider.getMessageFor("AN_OK");   //NOI18N
            btnAD = provider.getMessageFor("AD_OK");   //NOI18N
            break;
        }
        
        
        // create and display the dialog:
        ChangeListener changeListener;
        final JButton btnOK = new JButton(btnTxt);
        btnOK.getAccessibleContext().setAccessibleName(btnAN);//NbBundle.getMessage(GuiUtils.class, "AN_OK"));
        btnOK.getAccessibleContext().setAccessibleDescription(btnAD);//NbBundle.getMessage(GuiUtils.class, "AD_OK"));
        btnOK.setEnabled(isAcceptable());
        addChangeListener(changeListener = new ChangeListener() {
            @Override
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
                        new HelpCtx(CommonTestsCfgOfCreate.class),
                        (ActionListener) null
                ));
        removeChangeListener(changeListener);
        
        if (returned == btnOK) {
            rememberCheckBoxStates();
	    setLastSelectedTestingFramework();
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
    public boolean isSingleClass() {
        return singleClass;
    }

    public boolean isSinglePackage() {
        return singlePackage;
    }
    
    /**
     * Returns the class name entered in the text-field.
     *
     * @return  class name entered in the form,
     *          or <code>null</code> if the form did not contain
     *          the field for entering class name
     */
    public String getTestClassName() {
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
        boolean chkPublicB = true;
        boolean chkProtectedB = true;
        boolean chkPackageB = true;
        boolean chkCommentsB = true;
        boolean chkContentB = true;
        boolean chkJavaDocB = true;
        boolean chkGenerateSuitesB = true;
        boolean chkPackagePrivateClassesB = true;
        boolean chkAbstractImplB = true;
        boolean chkExceptionsB = true;
        boolean chkSetUpB = true;
        boolean chkTearDownB = true;
        boolean chkBeforeClassB = true;
        boolean chkAfterClassB = true;
        Collection<? extends CommonSettingsProvider> providers = Lookup.getDefault().lookupAll(CommonSettingsProvider.class);
        for (CommonSettingsProvider provider : providers) {
            chkPublicB = provider.isMembersPublic();
            chkProtectedB = provider.isMembersProtected();
            chkPackageB = provider.isMembersPackage();
            chkCommentsB = provider.isBodyComments();
            chkContentB = provider.isBodyContent();
            chkJavaDocB = provider.isJavaDoc();
            chkGenerateSuitesB = provider.isGenerateSuiteClasses();
            chkPackagePrivateClassesB = provider.isIncludePackagePrivateClasses();
            chkAbstractImplB = provider.isGenerateAbstractImpl();
            chkExceptionsB = provider.isGenerateExceptionClasses();
            chkSetUpB = provider.isGenerateSetUp();
            chkTearDownB = provider.isGenerateTearDown();
            chkBeforeClassB = provider.isGenerateClassSetUp();
            chkAfterClassB = provider.isGenerateClassTearDown();
            break;
        }
        
        chkPublic.setSelected(chkPublicB);
        chkProtected.setSelected(chkProtectedB);
        chkPackage.setSelected(chkPackageB);
        chkComments.setSelected(chkCommentsB);
        chkContent.setSelected(chkContentB);
        chkJavaDoc.setSelected(chkJavaDocB);
        if (multipleClasses) {
            chkGenerateSuites.setSelected(chkGenerateSuitesB);
            chkPackagePrivateClasses.setSelected(chkPackagePrivateClassesB);
            chkAbstractImpl.setSelected(chkAbstractImplB);
            chkExceptions.setSelected(chkExceptionsB);
        }
        chkSetUp.setSelected(chkSetUpB);
        chkTearDown.setSelected(chkTearDownB);
        chkBeforeClass.setSelected(chkBeforeClassB);
        chkAfterClass.setSelected(chkAfterClassB);
    }
    
    /**
     * Stores settings given by checkbox states to JUnit settings.
     *
     * @see  #initializeCheckBoxStatesf
     */
    private void rememberCheckBoxStates() {
        Collection<? extends CommonSettingsProvider> providers = Lookup.getDefault().lookupAll(CommonSettingsProvider.class);
        for (CommonSettingsProvider provider : providers) {
            provider.setMembersPublic(chkPublic.isSelected());
            provider.setMembersProtected(chkProtected.isSelected());
            provider.setMembersPackage(chkPackage.isSelected());
            provider.setBodyComments(chkComments.isSelected());
            provider.setBodyContent(chkContent.isSelected());
            provider.setJavaDoc(chkJavaDoc.isSelected());
            if (multipleClasses) {
                provider.setGenerateSuiteClasses(chkGenerateSuites.isSelected());
                provider.setIncludePackagePrivateClasses(
                        chkPackagePrivateClasses.isSelected());
                provider.setGenerateAbstractImpl(chkAbstractImpl.isSelected());
                provider.setGenerateExceptionClasses(chkExceptions.isSelected());
            }
            provider.setGenerateSetUp(chkSetUp.isSelected());
            provider.setGenerateTearDown(chkTearDown.isSelected());
            provider.setGenerateClassSetUp(chkBeforeClass.isSelected());
            provider.setGenerateClassTearDown(chkAfterClass.isSelected());
            break;
        }
    }

    private void setLastSelectedTestingFramework() {
	getPreferences().put(PROP_TESTING_FRAMEWORK, selectedTestingFramework);
    }

    private String getLastSelectedTestingFramework() {
	return getPreferences().get(PROP_TESTING_FRAMEWORK, "");
    }

    private static Preferences getPreferences() {
	return NbPreferences.forModule(CommonTestsCfgOfCreate.class);
    }

    /**
     * Loads a resource bundle so that it can be used during intialization
     * of this panel.
     *
     * @see  #unlinkBundle
     */
    private void initBundle() {
        Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
        for (GuiUtilsProvider provider : providers) {
            bundle = provider.getBundle();
            break;
        }
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
	JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout(0, 12));
        
        jPanel.add(createNameAndLocationPanel(), BorderLayout.NORTH);
        jPanel.add(createMessagePanel(), BorderLayout.CENTER);
        jPanel.add(createCodeGenPanel(), BorderLayout.SOUTH);

	add(jPanel);
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
     * Listener object that listens on state changes of some check-boxes.
     */
    private final class CheckBoxListener implements ItemListener {
        public CheckBoxListener () {}
        
        @Override
        public void itemStateChanged(ItemEvent e) {
            final Object source = e.getSource();
            
            assert source == chkPublic
                   || source == chkProtected
                   || source == chkPackage;
            checkAcceptability();
        }
        
    }

//    public String getTestingFramework() {
//        Object selectedTestingFramework = cboxFramework.getSelectedItem();
//        if(selectedTestingFramework == null) {
//            return null;
//        }
//        return selectedTestingFramework.toString().equals(TESTNG_TEST_FRAMEWORK) ? TESTNG_TEST_FRAMEWORK : JUNIT_TEST_FRAMEWORK;
//    }
    
    private String getTestingFrameworkSuffix() {
        Object tf = cboxFramework.getSelectedItem();
        if(tf == null) {
            return "";
        }
        String testngFramework = "";
        Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
        for (GuiUtilsProvider provider : providers) {
            testngFramework = provider.getTestngFramework();
            break;
        }
        return tf.toString().equals(testngFramework) ? "NG" : ""; //NOI18N
    }
    
    private void fireFrameworkChanged() {
        if (tfClassName != null) {
            DataObject dataObj = nodes[0].getLookup().lookup(DataObject.class);
            FileObject fileObj = dataObj.getPrimaryFile();

            ClassPath cp = ClassPath.getClassPath(fileObj, ClassPath.SOURCE);
            String className = cp.getResourceName(fileObj, '.', false);

            String prefilledName = className + getTestingFrameworkSuffix() + TEST_CLASS_SUFFIX;
            tfClassName.setText(prefilledName);
            tfClassName.setDefaultText(prefilledName);
            tfClassName.setCaretPosition(prefilledName.length());
        }
        setSelectedTestingFramework();
    }
    
    private void setSelectedTestingFramework() {
        Object tf = cboxFramework.getSelectedItem();
        if(tf != null) {
            selectedTestingFramework = tf.toString();
        }
    }
    
    public String getSelectedTestingFramework() {
        return selectedTestingFramework;
    }
    
    public void addTestingFrameworks(ArrayList<String> testingFrameworksToAdd) {
        for(String testingFramework : testingFrameworksToAdd) {
            testingFrameworks.add(testingFramework);
        }
        cboxFramework.setModel(new DefaultComboBoxModel(testingFrameworks.toArray()));
	cboxFramework.setSelectedItem(getLastSelectedTestingFramework());
        fireFrameworkChanged();
    }

    public void setPreselectedLocation(Object location) {
	if (location != null) {
	    cboxLocation.setSelectedItem(location);
	    cboxLocation.setEnabled(false);
	}
    }

    public void setPreselectedFramework(String testingFramework) {
	if (testingFramework != null) {
	    cboxFramework.setSelectedItem(testingFramework);
	    cboxFramework.setEnabled(false);
	    setSelectedTestingFramework();
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
        JLabel lblFramework = new JLabel();
        
        String classToTestKey = singlePackage
                                ? "LBL_PackageToTest"                   //NOI18N
                                : singleClass
                                  ? "LBL_ClassToTest"                   //NOI18N
                                  : "LBL_MultipleClassesSelected";      //NOI18N
        String classToTest = "";
        String classname = "";
        String location = "";
        String framework = "";
        Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
        for (GuiUtilsProvider provider : providers) {
            classToTest = provider.getMessageFor(classToTestKey);
            classname = provider.getMessageFor("LBL_ClassName");   //NOI18N
            location = provider.getMessageFor("LBL_Location");   //NOI18N
            framework = provider.getMessageFor("LBL_Framework");   //NOI18N
            break;
        }
        Mnemonics.setLocalizedText(
                lblClassToTest,
                classToTest);
        if (askForClassName) {
            Mnemonics.setLocalizedText(
                    lblClassName,
                    classname);
        }
        Mnemonics.setLocalizedText(
                lblLocation,
                location);
        Mnemonics.setLocalizedText(
                lblFramework,
                framework);
        
        if (singlePackage || singleClass) {
            lblClassToTestValue = new JLabel();
        }
        if (askForClassName) {
            tfClassName = new ClassNameTextField();
            tfClassName.setChangeListener(this);
        }
        cboxLocation = new JComboBox();
        cboxFramework = new JComboBox();
        testingFrameworks = new ArrayList<String>();
        cboxFramework.setModel(new DefaultComboBoxModel(testingFrameworks.toArray()));
        cboxFramework.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireFrameworkChanged();
            }
        });
        
        if (askForClassName) {
            lblClassName.setLabelFor(tfClassName);
        }
        lblLocation.setLabelFor(cboxLocation);
        lblFramework.setLabelFor(cboxFramework);
        
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
        panel.add(lblLocation,      gbcLeft);
        panel.add(cboxLocation,     gbcRight);
        gbcLeft.insets.bottom = 0;
        gbcRight.insets.bottom = 0;
        panel.add(lblFramework,      gbcLeft);
        panel.add(cboxFramework,     gbcRight);
        
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
                key = "MSG_ClassnameMustNotBeEmpty";                    //NOI18N
                break;
            case ClassNameTextField.STATUS_INVALID:
                key = "MSG_InvalidClassName";                           //NOI18N
                break;
            case ClassNameTextField.STATUS_VALID_NOT_DEFAULT:
                key = "MSG_ClassNameNotDefault";                        //NOI18N
                break;
            case ClassNameTextField.STATUS_VALID_END_NOT_TEST:
                key = "MSG_ClassNameEndNotTest";                        //NOI18N
                break;
        }
        if (state != ClassNameTextField.STATUS_VALID_NOT_DEFAULT) {
            setMessage(null, MSG_TYPE_CLASSNAME_NOT_DEFAULT);
        }
        String message = "";
        if (key != null) {
            Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
            for (GuiUtilsProvider provider : providers) {
                message = provider.getMessageFor(key);
                break;
            }
        }
        setMessage((key != null)
                           ? message
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
    @Override
    public void stateChanged(ChangeEvent e) {
        checkClassNameValidity();
        checkAcceptability();
    }
    
    /**
     */
    private void checkAcceptability() {
        final boolean wasAcceptable = isAcceptable;
        isAcceptable = hasTargetFolders && classNameValid;
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
    @Override
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
        Color color = UIManager.getColor("nb.errorForeground");         //NOI18N
        if (color == null) {
            color = new Color(89, 79, 191);   //RGB suggested by Bruce in #28466
        }
        Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
        for (GuiUtilsProvider provider : providers) {
            txtAreaMessage = provider.createMultilineLabel("", color);      //NOI18N
            break;
        }
        return txtAreaMessage;
    }
    
    /**
     * Creates a panel containing controls for settings code generation options.
     *
     * @return   created panel
     */
    private Component createCodeGenPanel() {
        
        /* create the components: */
        String[] chkBoxIDs = new String[14];
        JCheckBox[] chkBoxes;
        Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
        if (multipleClasses) {            
            for (GuiUtilsProvider provider : providers) {
                chkBoxIDs = new String[]{
                    provider.getCheckboxText("CHK_PUBLIC"),
                    provider.getCheckboxText("CHK_PROTECTED"),
                    provider.getCheckboxText("CHK_PACKAGE"),
                    provider.getCheckboxText("CHK_PACKAGE_PRIVATE_CLASSES"),
                    provider.getCheckboxText("CHK_ABSTRACT_CLASSES"),
                    provider.getCheckboxText("CHK_EXCEPTION_CLASSES"),
                    provider.getCheckboxText("CHK_SUITES"),
                    provider.getCheckboxText("CHK_SETUP"),
                    provider.getCheckboxText("CHK_TEARDOWN"),
                    provider.getCheckboxText("CHK_BEFORE_CLASS"),
                    provider.getCheckboxText("CHK_AFTER_CLASS"),
                    provider.getCheckboxText("CHK_METHOD_BODIES"),
                    provider.getCheckboxText("CHK_JAVADOC"),
                    provider.getCheckboxText("CHK_HINTS")
                };
                break;
            }
        } else {
            for (GuiUtilsProvider provider : providers) {
                chkBoxIDs = new String[]{
                    provider.getCheckboxText("CHK_PUBLIC"),
                    provider.getCheckboxText("CHK_PROTECTED"),
                    provider.getCheckboxText("CHK_PACKAGE"),
                    null, // CHK_PACKAGE_PRIVATE_CLASSES,
                    null, // CHK_ABSTRACT_CLASSES,
                    null, // CHK_EXCEPTION_CLASSES,
                    null, // CHK_SUITES,
                    provider.getCheckboxText("CHK_SETUP"),
                    provider.getCheckboxText("CHK_TEARDOWN"),
                    provider.getCheckboxText("CHK_BEFORE_CLASS"),
                    provider.getCheckboxText("CHK_AFTER_CLASS"),
                    provider.getCheckboxText("CHK_METHOD_BODIES"),
                    provider.getCheckboxText("CHK_JAVADOC"),
                    provider.getCheckboxText("CHK_HINTS")
                };
                break;
            }
        }
        chkBoxes = new JCheckBox[chkBoxIDs.length];
        for (GuiUtilsProvider provider : providers) {
            chkBoxes = provider.createCheckBoxes(chkBoxIDs);
            break;
        }
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
        chkBeforeClass      = chkBoxes[i++];
        chkAfterClass       = chkBoxes[i++];
        chkContent          = chkBoxes[i++];
        chkJavaDoc          = chkBoxes[i++];
        chkComments         = chkBoxes[i++];
        
        /* create groups of checkboxes: */
        JComponent methodAccessLevels = null;
        for (GuiUtilsProvider provider : providers) {
            methodAccessLevels = provider.createChkBoxGroup(
                bundle.getString("CommonTestsCfgOfCreate.groupAccessLevels"), //NOI18N
                new JCheckBox[] {chkPublic, chkProtected, chkPackage});
            break;
        }
        JComponent classTypes = null;
        JComponent optionalClasses = null;
        if (multipleClasses) {
            for (GuiUtilsProvider provider : providers) {
                classTypes = provider.createChkBoxGroup(
                        bundle.getString("CommonTestsCfgOfCreate.groupClassTypes"), //NOI18N
                        new JCheckBox[]{chkPackagePrivateClasses,
                            chkAbstractImpl, chkExceptions});
                optionalClasses = provider.createChkBoxGroup(
                        bundle.getString("CommonTestsCfgOfCreate.groupOptClasses"), //NOI18N
                        new JCheckBox[]{chkGenerateSuites});
                break;
            }
        }
        JComponent optionalCode = null;
        JComponent optionalComments = null;
        for (GuiUtilsProvider provider : providers) {
            optionalCode = provider.createChkBoxGroup(
                    bundle.getString("CommonTestsCfgOfCreate.groupOptCode"), //NOI18N
                    new JCheckBox[]{chkSetUp, chkTearDown, chkBeforeClass, chkAfterClass, chkContent});
            optionalComments = provider.createChkBoxGroup(
                    bundle.getString("CommonTestsCfgOfCreate.groupOptComments"), //NOI18N
                    new JCheckBox[]{chkJavaDoc, chkComments});
            break;
        }
        
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
                  bundle.getString("CommonTestsCfgOfCreate.jpCodeGen.title"));//NOI18N
        
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
    public FileObject getTargetFolder() {
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
        final DataObject dataObj = nodes[0].getLookup().lookup(DataObject.class);
        final FileObject fileObj = dataObj.getPrimaryFile();
        
        if (singleClass) {
            assert nodes.length == 1;
            
            ClassPath cp = ClassPath.getClassPath(fileObj, ClassPath.SOURCE);
            String className = cp.getResourceName(fileObj, '.', false);
            lblClassToTestValue.setText(className);
            
            if (tfClassName != null) {
                String prefilledName = className + getTestingFrameworkSuffix() + TEST_CLASS_SUFFIX;
                tfClassName.setText(prefilledName);
                tfClassName.setDefaultText(prefilledName);
                tfClassName.setCaretPosition(prefilledName.length());
            }
        } else if (singlePackage) {
            assert nodes.length == 1;
            
            ClassPath cp = ClassPath.getClassPath(fileObj, ClassPath.SOURCE);
            String packageName = (cp == null) ? "" : cp.getResourceName(fileObj, '.', true);
            if (packageName.length() == 0) {
                Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
                for (GuiUtilsProvider provider : providers) {
                    packageName = provider.getMessageFor("DefaultPackageName");    //NOI18N
                    break;
                }
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
        Collection<? extends CommonTestUtilProvider> providers = Lookup.getDefault().lookupAll(CommonTestUtilProvider.class);
        Object[] targetFolders = null;
        for (CommonTestUtilProvider provider : providers) {
            targetFolders = provider.getTestTargets(refFileObject);
            break;
        }
        if (targetFolders.length == 0) {
            Project owner = FileOwnerQuery.getOwner(refFileObject);
            if (owner != null) {
                String type = "";
                String hint = "";
                Collection<? extends RootsProvider> rootProviders = Lookup.getDefault().lookupAll(RootsProvider.class);
                for (RootsProvider rootProvider : rootProviders) {
                    type = rootProvider.getSourceRootType();
                    hint = rootProvider.getProjectTestsHint();
                    break;
                }
                final SourceGroup grp = SourceGroupModifier.createSourceGroup(owner, type, hint);
                if (grp != null) {
                    createdSourceRoots.add(grp);
                    providers = Lookup.getDefault().lookupAll(CommonTestUtilProvider.class);
                    for (CommonTestUtilProvider provider : providers) {
                        targetFolders = provider.getTestTargets(refFileObject);
                        break;
                    }
                }
            }
        }

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

    public Collection<? extends SourceGroup> getCreatedSourceRoots() {
        return Collections.unmodifiableCollection(createdSourceRoots);
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
        
        @Override
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
        @Override
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
    private JTextComponent txtAreaMessage;
    private JComboBox cboxLocation;
    private JComboBox cboxFramework;
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
    private JCheckBox chkBeforeClass;
    private JCheckBox chkAfterClass;

}
