/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.wizards;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.junit.GuiUtils;
import org.netbeans.modules.junit.NamedObject;
import org.netbeans.modules.junit.SizeRestrictedPanel;
import org.netbeans.modules.junit.TestCreator;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;

/**
 *
 * @author  Marian Petras
 */
public class SimpleTestStepLocation implements WizardDescriptor.Panel {
    
    private final String testClassNameSuffix
            = NbBundle.getMessage(TestCreator.class,
                                  "PROP_test_classname_suffix");        //NOI18N
    
    private Component visualComp;
    private List changeListeners;
    private JTextField tfClassToTest;
    private JButton btnBrowse;
    private JTextField tfTestClass;
    private JTextField tfProjectName;
    private JComboBox cboxLocation;
    private JTextField tfCreatedFile;
    
    private JCheckBox chkPublic;
    private JCheckBox chkProtected;
    private JCheckBox chkPackagePrivate;
    private JCheckBox chkSetUp;
    private JCheckBox chkTearDown;
    private JCheckBox chkMethodBodies;
    private JCheckBox chkJavadoc;
    private JCheckBox chkHints;
    
    private Project project;
    private SourceGroup srcSourceGroup;
    private SourceGroup testSourceGroup;
    private FileObject srcRootFolder;
    private FileObject testRootFolder;
    private FileObject srcFile;
    private String testsRootDirName = "";                               //NOI18N
    private String srcRelFileNameSys = "";                              //NOI18N
    private String testRelFileName = "";                                //NOI18N
    private boolean classNameValid = false;
    private boolean classExists = false;
    private boolean isValid = false;
    private TemplateWizard wizard;
    private String msgClassNameInvalid;
    private String msgClassToTestDoesNotExist;
        
    public SimpleTestStepLocation() {
        super();
        visualComp = createVisualComp();
    }
    
    private Component createVisualComp() {
        JLabel lblClassToTest = new JLabel();
        JLabel lblCreatedTestClass = new JLabel();
        JLabel lblProject = new JLabel();
        JLabel lblLocation = new JLabel();
        JLabel lblFile = new JLabel();
        tfClassToTest = new JTextField();
        btnBrowse = new JButton();
        tfTestClass = new JTextField();
        tfProjectName = new JTextField();
        cboxLocation = new JComboBox();
        tfCreatedFile = new JTextField();
        
        ResourceBundle bundle
                = NbBundle.getBundle(SimpleTestStepLocation.class);
        
        Mnemonics.setLocalizedText(lblClassToTest,
                                   bundle.getString("LBL_ClassToTest"));//NOI18N
        Mnemonics.setLocalizedText(lblCreatedTestClass,
                                   bundle.getString("LBL_TestClass"));  //NOI18N
        Mnemonics.setLocalizedText(lblProject,
                                   bundle.getString("LBL_Project"));    //NOI18N
        Mnemonics.setLocalizedText(lblLocation,
                                   bundle.getString("LBL_Location"));   //NOI18N
        Mnemonics.setLocalizedText(lblFile,
                                   bundle.getString("LBL_CreatedFile"));//NOI18N
        Mnemonics.setLocalizedText(btnBrowse,
                                   bundle.getString("LBL_Browse"));     //NOI18N
        
        lblClassToTest.setLabelFor(tfClassToTest);
        lblCreatedTestClass.setLabelFor(tfTestClass);
        lblProject.setLabelFor(tfProjectName);
        lblFile.setLabelFor(tfCreatedFile);
        lblLocation.setLabelFor(cboxLocation);
        
        tfTestClass.setEditable(false);
        tfProjectName.setEditable(false);
        tfCreatedFile.setEditable(false);
        
        tfTestClass.setFocusable(false);
        tfProjectName.setFocusable(false);
        tfCreatedFile.setFocusable(false);
        
        setUpInteraction();
        
        JCheckBox[] chkBoxes;
        
        JComponent accessLevels = GuiUtils.createChkBoxGroup(
                NbBundle.getMessage(
                        GuiUtils.class,
                        "JUnitCfgOfCreate.groupAccessLevels"),          //NOI18N
                chkBoxes = GuiUtils.createCheckBoxes(new String[] {
                        GuiUtils.CHK_PUBLIC,
                        GuiUtils.CHK_PROTECTED,
                        GuiUtils.CHK_PACKAGE}));
        chkPublic = chkBoxes[0];
        chkProtected = chkBoxes[1];
        chkPackagePrivate = chkBoxes[2];
        
        JComponent optCode = GuiUtils.createChkBoxGroup(
                NbBundle.getMessage(
                        GuiUtils.class,
                        "JUnitCfgOfCreate.groupOptCode"),               //NOI18N
                chkBoxes = GuiUtils.createCheckBoxes(new String[] {
                        GuiUtils.CHK_SETUP,
                        GuiUtils.CHK_TEARDOWN,
                        GuiUtils.CHK_METHOD_BODIES}));
        chkSetUp = chkBoxes[0];
        chkTearDown = chkBoxes[1];
        chkMethodBodies = chkBoxes[2];
        
        JComponent optComments = GuiUtils.createChkBoxGroup(
                NbBundle.getMessage(
                        GuiUtils.class,
                        "JUnitCfgOfCreate.groupOptComments"),           //NOI18N
                chkBoxes = GuiUtils.createCheckBoxes(new String[] {
                        GuiUtils.CHK_JAVADOC,
                        GuiUtils.CHK_HINTS}));
        chkJavadoc = chkBoxes[0];
        chkHints = chkBoxes[1];
                        
        /* set layout of the components: */
        JPanel targetPanel
                = new SizeRestrictedPanel(new GridBagLayout(), false, true);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridy = 0;
        
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0d;
        gbc.insets = new Insets(0, 0, 6, 12);
        targetPanel.add(lblClassToTest, gbc);
        
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0d;
        gbc.insets = new Insets(0, 0, 6, 0);
        targetPanel.add(tfClassToTest, gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0d;
        gbc.insets = new Insets(0, 11, 6, 0);
        targetPanel.add(btnBrowse, gbc);
        
        gbc.gridy++;
        
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0d;
        gbc.insets = new Insets(0, 0, 24, 12);
        targetPanel.add(lblCreatedTestClass, gbc);
        
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0d;
        gbc.insets = new Insets(0, 0, 24, 0);
        targetPanel.add(tfTestClass, gbc);
        
        gbc.gridy++;
        
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0d;
        gbc.insets = new Insets(0, 0, 6, 12);
        targetPanel.add(lblProject, gbc);
        
        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0d;
        gbc.insets = new Insets(0, 0, 6, 0);
        targetPanel.add(tfProjectName, gbc);
        
        gbc.gridy++;
        
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0d;
        gbc.insets = new Insets(0, 0, 12, 12);
        targetPanel.add(lblLocation, gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0d;
        gbc.insets = new Insets(0, 0, 12, 0);
        targetPanel.add(cboxLocation, gbc);
        
        gbc.gridy++;
        
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0d;
        gbc.insets = new Insets(0, 0, 0, 12);
        targetPanel.add(lblFile, gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0d;
        gbc.insets = new Insets(0, 0, 0, 0);
        targetPanel.add(tfCreatedFile, gbc);
        
        JComponent accessLevelsBox = new SizeRestrictedPanel(true, false);
        accessLevelsBox.setLayout(
                new BoxLayout(accessLevelsBox, BoxLayout.Y_AXIS));
        accessLevelsBox.add(accessLevels);
        accessLevelsBox.add(Box.createVerticalGlue());
        
        JComponent optionalCodeBox = new SizeRestrictedPanel(true, false);
        optionalCodeBox.setLayout(
                new BoxLayout(optionalCodeBox, BoxLayout.Y_AXIS));
        optionalCodeBox.add(optCode);
        optionalCodeBox.add(Box.createVerticalStrut(11));
        optionalCodeBox.add(optComments);
        optionalCodeBox.add(Box.createVerticalGlue());
        
        JComponent optionsBox = new SizeRestrictedPanel(false, true);
        optionsBox.setLayout(
                new BoxLayout(optionsBox, BoxLayout.X_AXIS));
        optionsBox.add(accessLevelsBox);
        optionsBox.add(Box.createHorizontalStrut(18));
        optionsBox.add(optionalCodeBox);
        optionsBox.add(Box.createHorizontalGlue());
        
        Box result = Box.createVerticalBox();
        result.add(targetPanel);
        result.add(Box.createVerticalStrut(12));
        result.add(new JSeparator() {
            public java.awt.Dimension getMaximumSize() {
                java.awt.Dimension maximumSize = super.getMaximumSize();
                maximumSize.height = getPreferredSize().height;
                return maximumSize;
            }
        });
        result.add(Box.createVerticalStrut(12));
        result.add(optionsBox);
        //result.add(Box.createVerticalGlue());  //not necessary
        
        /* tune layout of the components within the box: */
        targetPanel.setAlignmentX(0.0f);
        optionsBox.setAlignmentX(0.0f);
        optCode.setAlignmentX(0.0f);
        optComments.setAlignmentX(0.0f);

        result.setName(bundle.getString("LBL_panel_ChooseClass"));
        return result;
    }
    
    private void setUpLocationComboBox() {
        Collection sourceGroupPairs = Utils.getSourceGroupPairs(project);
        //PENDING - what if the collection of pairs is empty?
        //PENDING - should not the pairs be sorted (alphabetically)?
        Iterator i = sourceGroupPairs.iterator();
        NamedObject[] items = new NamedObject[sourceGroupPairs.size()];
        for (int j = 0; j < items.length; j++) {
            SourceGroup[] sourceGroupPair = (SourceGroup[]) i.next();
            SourceGroup tests = sourceGroupPair[1];
            items[j] = new NamedObject(sourceGroupPair,
                                       tests.getDisplayName());
        }
        cboxLocation.setModel(new DefaultComboBoxModel(items));
        //PENDING - if possible, we should pre-set the test source group
        //          corresponding to the currently selected node
        locationChanged();
        
        cboxLocation.setEditable(false);
    }
    
    private void setUpInteraction() {
        btnBrowse.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectClass();
                }
        });
        tfClassToTest.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    classNameChanged();
                }
                public void removeUpdate(DocumentEvent e) {
                    classNameChanged();
                }
                public void changedUpdate(DocumentEvent e) {
                    classNameChanged();
                }
        });
        cboxLocation.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    locationChanged();
                }
        });
    }
    
    private void locationChanged() {
        Object item = cboxLocation.getSelectedItem();
        SourceGroup[] pair = (SourceGroup[]) ((NamedObject) item).object;
        srcSourceGroup = pair[0];
        testSourceGroup = pair[1];
        srcRootFolder = srcSourceGroup.getRootFolder();
        testRootFolder = testSourceGroup.getRootFolder();
        testsRootDirName
                = FileUtil.getFileDisplayName(testRootFolder);
        updateCreatedFileName();
        if (classNameValid) {
            checkSelectedClassExists();
        }
        setValidity();
    }
    
    private void classNameChanged() {
        String className = tfClassToTest.getText().trim();
        String testClassName = tfTestClass.getText();
        String fileName;
        if (className.length() != 0) {
            testClassName = className + testClassNameSuffix;
            srcRelFileNameSys = className.replace('.', '/')
                                + ".java";                              //NOI18N
            testRelFileName = testClassName.replace('.', File.separatorChar)
                              + ".java";                                //NOI18N
        } else {
            testClassName = "";                                         //NOI18N
            srcRelFileNameSys = "";                                     //NOI18N
            testRelFileName = "";                                       //NOI18N
        }
        tfTestClass.setText(testClassName);
        updateCreatedFileName();
        
        if (checkClassNameValidity()) {
            checkSelectedClassExists();
        }
        setValidity();
    }
    
    private void updateCreatedFileName() {
        tfCreatedFile.setText(testsRootDirName + '/' + testRelFileName);
    }
    
    private boolean checkClassNameValidity() {
        String className = tfClassToTest.getText().trim();
        classNameValid = Utils.isValidClassName(className);
        return classNameValid;
    }
    
    private boolean checkSelectedClassExists() {
        srcFile = srcRootFolder.getFileObject(srcRelFileNameSys);
        classExists = (srcFile != null);
        return classExists;
    }
    
    private void setValidity() {
        boolean wasValid = isValid;
        
        boolean valid = true;
        if (tfClassToTest.getText().trim().length() == 0) {
            setErrorMsg(null);
            valid = false;
        } else if (!classNameValid) {
            if (msgClassNameInvalid == null) {
                msgClassNameInvalid = NbBundle.getMessage(
                        SimpleTestStepLocation.class,
                        "MSG_InvalidClassName");                    //NOI18N
            }
            setErrorMsg(msgClassNameInvalid);
            valid = false;
        } else if (!classExists) {
            if (msgClassToTestDoesNotExist == null) {
                msgClassToTestDoesNotExist = NbBundle.getMessage(
                        SimpleTestStepLocation.class,
                        "MSG_ClassToTestDoesNotExist");                 //NOI18N
            }
            setErrorMsg(msgClassToTestDoesNotExist);
            valid = false;
        } else {
            setErrorMsg(null);
        }
        isValid = valid;
        
        if (isValid != wasValid) {
            fireChange();
        }
    }
    
    private void setErrorMsg(String message) {
        if (wizard != null) {
            wizard.putProperty("WizardPanel_errorMessage", message);    //NOI18N
        }
    }
    
    /**
     * Displays a class chooser dialog and lets the user to select a class.
     * If the user confirms they choice, full name of the selected class
     * is put into the <em>Class To Test</em> text field.
     */
    private void selectClass() {
        try {
            /*
            Collection sourceGroups = Utils.getSourceSourceGroups(project);
            Node[] sourceGroupNodes = new Node[sourceGroups.size()];
            Iterator iterator = sourceGroups.iterator();
            for (int i = 0; i < sourceGroupNodes.length; i++) {
                SourceGroup srcGroup = (SourceGroup) iterator.next();
                AbstractNode srcGroupNode = new AbstractNode(
                       PackageView.createPackageView(srcGroup.getRootFolder()));
                srcGroupNode.setIconBase(PACKAGES_NODE_ICON_BASE);
                
                srcGroupNode.setName(srcGroup.getName());
                srcGroupNode.setDisplayName(srcGroup.getDisplayName());
                sourceGroupNodes[i] = srcGroupNode;
            }
            
            Node rootNode;
            if (sourceGroupNodes.length == 1) {
                rootNode = new FilterNode(
                        sourceGroupNodes[0],
                        new LogicalViewRootChildren(sourceGroupNodes[0]));
            } else {
                Children children = new Children.Array();
                children.add(sourceGroupNodes);
                
                AbstractNode node = new AbstractNode(children);
                rootNode = new FilterNode(node,
                                          new LogicalViewRootChildren(node));
                rootNode.setName("Project Source Roots");               //NOI18N
                rootNode.setDisplayName(
                        NbBundle.getMessage(SimpleTestStepLocation.class,
                                            "LBL_Sources"));            //NOI18N
            }
             */
            
            Node srcGroupNode = PackageView.createPackageView(srcSourceGroup);
            // Note: precise structure of this view is *not* specified by the API.
            
            Node rootNode = new FilterNode(
                    srcGroupNode,
                    new JavaChildren(srcGroupNode));
            
            NodeAcceptor acceptor = new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    Node.Cookie cookie;
                    return nodes.length == 1
                           && (cookie = nodes[0].getCookie(DataObject.class))
                              != null
                           && ((DataObject) cookie).getPrimaryFile().isFolder()
                              == false;
                }
            };
            
            Node selectedNode = NodeOperation.getDefault().select(
                    NbBundle.getMessage(SimpleTestStepLocation.class,
                                        "LBL_WinTitle_SelectClass"),    //NOI18N
                    NbBundle.getMessage(SimpleTestStepLocation.class,
                                        "LBL_SelectClassToTest"),       //NOI18N
                    rootNode,
                    acceptor)[0];
            
            /* promote the selection to the text field: */
            tfClassToTest.setText(getClassName(selectedNode));
        } catch (UserCancelException ex) {
            // if the user cancels the choice, do nothing
        }
    }
    
    private String getClassName(Node node) {
        FileObject selectedFile
                = ((DataObject) node.getCookie(DataObject.class))
                  .getPrimaryFile();
        
        //PENDING: is it ensured that the classpath is non-null?
        return ClassPath.getClassPath(selectedFile, ClassPath.SOURCE)
               .getResourceName(selectedFile, '.', false);
    }
    
    public Component getComponent() {
        return visualComp;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public HelpCtx getHelp() {
        //PENDINGg
        return null;
    }
    
    public void readSettings(Object settings) {
        wizard = (TemplateWizard) settings;
        
        chkPublic.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_PUBLIC)));
        chkProtected.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_PROTECTED)));
        chkPackagePrivate.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_PACKAGE)));
        chkSetUp.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_SETUP)));
        chkTearDown.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_TEARDOWN)));
        chkMethodBodies.setSelected(
           Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_METHOD_BODIES)));
        chkJavadoc.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_JAVADOC)));
        chkHints.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_HINTS)));
    }
    
    public void storeSettings(Object settings) {
        wizard = (TemplateWizard) settings;
        
        wizard.putProperty(SimpleTestCaseWizard.PROP_CLASS_TO_TEST,
                           srcFile);
        wizard.putProperty(SimpleTestCaseWizard.PROP_TEST_ROOT_FOLDER,
                           testRootFolder);
        
        wizard.putProperty(GuiUtils.CHK_PUBLIC,
                           Boolean.valueOf(chkPublic.isSelected()));
        wizard.putProperty(GuiUtils.CHK_PROTECTED,
                           Boolean.valueOf(chkProtected.isSelected()));
        wizard.putProperty(GuiUtils.CHK_PACKAGE,
                           Boolean.valueOf(chkPackagePrivate.isSelected()));
        wizard.putProperty(GuiUtils.CHK_SETUP,
                           Boolean.valueOf(chkSetUp.isSelected()));
        wizard.putProperty(GuiUtils.CHK_TEARDOWN,
                           Boolean.valueOf(chkTearDown.isSelected()));
        wizard.putProperty(GuiUtils.CHK_METHOD_BODIES,
                           Boolean.valueOf(chkMethodBodies.isSelected()));
        wizard.putProperty(GuiUtils.CHK_JAVADOC,
                           Boolean.valueOf(chkJavadoc.isSelected()));
        wizard.putProperty(GuiUtils.CHK_HINTS,
                           Boolean.valueOf(chkHints.isSelected()));
    }
    
    public void addChangeListener(ChangeListener l) {
        if (changeListeners == null) {
            changeListeners = new ArrayList(4);
        }
        changeListeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        if (changeListeners != null) {
            if (changeListeners.remove(l) && changeListeners.isEmpty()) {
                changeListeners = null;
            }
        }
    }
    
    private void fireChange() {
        if (changeListeners != null) {
            ChangeEvent e = new ChangeEvent(this);
            for (Iterator i = changeListeners.iterator(); i.hasNext(); ) {
                ((ChangeListener) i.next()).stateChanged(e);
            }
        }
    }
    
    void setProject(Project project) {
        if (project == this.project) {
            return;
        }
        if (project == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        
        this.project = project;
        tfProjectName.setText(
                ProjectUtils.getInformation(project).getDisplayName());
        setUpLocationComboBox();
        //PENDING - not yet finished
    }
    
}
