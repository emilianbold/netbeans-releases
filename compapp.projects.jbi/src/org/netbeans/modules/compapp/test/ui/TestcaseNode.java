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


package org.netbeans.modules.compapp.test.ui;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.netbeans.modules.compapp.test.ui.actions.TestCaseDebugAction;
import org.netbeans.modules.compapp.test.ui.actions.TestCaseResultsDeleteAction;
import org.netbeans.modules.compapp.test.ui.actions.TestcaseCookie;
import org.netbeans.modules.compapp.test.ui.actions.TestcaseDiffAction;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.test.ui.actions.TestCaseRunAction;
import org.netbeans.modules.compapp.test.util.FileNodeUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.actions.DeleteAction;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 * A node that represents a test case.
 *
 * @author Bing Lu
 * @author Jun Qian
 */
public class TestcaseNode extends FilterNode {
    private static final java.util.logging.Logger mLogger =
            java.util.logging.Logger.getLogger("org.netbeans.modules.compapp.projects.jbi.ui.TestcaseNode"); // NOI18N
    
    private static Image WARNING_BADGE = Utilities.loadImage(
            "org/netbeans/modules/compapp/test/ui/resources/warningBadge.gif", true); // NOI18N
    
    private static Image TEST_CASE_ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/test/ui/resources/testCase.png", true); // NOI18N
    
    public static final String ACTUAL_OUTPUT_REGEX = "^Actual_\\d{14}(_[FS])?.xml$"; // NOI18N
    public static final String SUCCESSFUL_ACTUAL_OUTPUT_REGEX = "^Actual_\\d{14}_S.xml$"; // NOI18N
    public static final String FAILED_ACTUAL_OUTPUT_REGEX = "^Actual_\\d{14}(_F)?.xml$"; // NOI18N
    
    
    private PropertyFileWrapper mPropertyFileWrapper;
    private FileObject mTestcaseDir;
    private FileObject mPropertyFile;
    private TestcaseCookie mTestcaseCookie;
    private DiffTopComponent mDiffTopComponent;
    private JbiProject mProject;
    private FileChangeListener mFileChangeListener;
    private FileChangeListener mTestDirChangeListener;
    private FileChangeListener mTestResultsDirChangeListener;
    private FileChangeListener mTestCaseResultsDirChangeListener;
    private TestcaseChildren mChildren;
    
    private FileObject mTestDir;             // test
    private FileObject mTestResultsDir;      // test/results
    private FileObject mTestCaseResultsDir;  // test/results/<TestCaseName>
        
    // a set of test cases that are currently running (#84900)
    private static Set<FileObject> runningTestCases = new HashSet<FileObject>();
    
    // will frequently accept an element from some data mTestcaseDir in the constructor:
    public TestcaseNode(JbiProject project, FileObject testcaseDir) {
        super(DataFolder.findFolder(testcaseDir).getNodeDelegate(),
                new TestcaseChildren(project, testcaseDir));
        mTestcaseDir = testcaseDir;
        mProject = project;
        
        registerPropertyFileChangeListener(testcaseDir);
        
        mTestcaseCookie = new TestcaseCookie(this);
        
        final String testCaseName = mTestcaseDir.getName();
        
        // set the model listener
        mFileChangeListener = new FileChangeAdapter() {
            public void fileChanged(FileEvent fe) {
                checkOutputChange(fe.getFile());
                update();
            }
            
            public void fileDataCreated(FileEvent fe) {
                checkOutputChange(fe.getFile());
                update();
            }
            
            public void fileDeleted(FileEvent fe) {
                checkOutputChange(fe.getFile());
                update();
            }
            
            public void fileRenamed(FileRenameEvent fe) {
                checkOutputChange(fe.getFile());
                update();
            }
        };
        // keep an eye on the <TESTCASE> directory
        getFileObject().addFileChangeListener(mFileChangeListener);
        
        // keep an eye on the test/ directory
        mTestDir = mProject.getTestDirectory();
        mTestDirChangeListener = new FileChangeAdapter() {
            public void fileFolderCreated(FileEvent fe) {
                FileObject fo = fe.getFile();
                if (fo.getName().equals("results")) {   // FIXME  // NOI18N
                    mTestResultsDir = fo;
                    mTestResultsDir.addFileChangeListener(mTestResultsDirChangeListener);
                }
                
                FileObject testCaseResultsDir = fo.getFileObject(testCaseName);
                if (testCaseResultsDir != null) {
                    testCaseResultsDir.addFileChangeListener(mTestCaseResultsDirChangeListener);
                }
                
                update();
            }
            
            public void fileDeleted(FileEvent fe) {
                FileObject fo = fe.getFile();
                if (fo.getName().equals("results")) {   // FIXME // NOI18N
                    fo.removeFileChangeListener(mTestResultsDirChangeListener);
                }
                
                update();
            }
        };
        mTestDir.addFileChangeListener(mTestDirChangeListener);
        
        // keep an eye on the test/results/ directory
        mTestResultsDirChangeListener = new FileChangeAdapter() {
            public void fileFolderCreated(FileEvent fe) {
                FileObject fo = fe.getFile();
                if (fo.getName().equals(testCaseName)) {
                    mTestCaseResultsDir = fo;
                    mTestCaseResultsDir.addFileChangeListener(mTestCaseResultsDirChangeListener);
                }
                
                update();
            }
            
            public void fileDeleted(FileEvent fe) {
                
                FileObject fo = fe.getFile();
                if (fo.getName().equals(testCaseName)) {
                    fo.removeFileChangeListener(mTestCaseResultsDirChangeListener);
                }
                
                update();
            }
        };
        mTestResultsDir = mProject.getTestResultsDirectory();
        if (mTestResultsDir != null) {
            mTestResultsDir.addFileChangeListener(mTestResultsDirChangeListener);
        }
        
        // keep an eye on the test/results/<TESTCASE> directory
        mTestCaseResultsDirChangeListener = new FileChangeAdapter() {
            public void fileDataCreated(FileEvent fe) {
                update();
                doFirstResultCheck(fe.getFile());
            }
            
            public void fileDeleted(FileEvent fe) {
                update();
            }
            
            public void fileRenamed(FileRenameEvent fe) {
                update();
            }
            
            private void doFirstResultCheck(FileObject newFO) {
                FileObject outputFO = getOutputFile();
                if (outputFO == null) {
                    return;
                }
                File outputFile = FileUtil.toFile(outputFO);
                if (outputFile.length() == 0 &&
                        newFO.getNameExt().matches(ACTUAL_OUTPUT_REGEX) &&
                        FileUtil.toFile(newFO).length() != 0) {
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(TestcaseNode.class, "MSG_OverwriteEmptyOutput"), // NOI18N
                            NbBundle.getMessage(TestcaseNode.class, "TTL_OverwriteEmptyOutput"), // NOI18N
                            NotifyDescriptor.YES_NO_OPTION);
                    if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
                        try {
                            FileNodeUtil.overwriteFile(newFO, outputFO);
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                            NotifyDescriptor d1 = new NotifyDescriptor.Message(e.getMessage(),
                                    NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(d1);
                        }
                    }
                }
            }
        };
        if (mTestResultsDir != null) {
            mTestCaseResultsDir = mTestResultsDir.getFileObject(testCaseName);
            if (mTestCaseResultsDir != null) {
                mTestCaseResultsDir.addFileChangeListener(mTestCaseResultsDirChangeListener);
            }
        }
        
        mChildren = (TestcaseChildren) getChildren();
    }
    
    public void setName(String name) {
        String oldName = getName();
        
        super.setName(name);
        
        if (getName().equals(name)) {
            // if successful, we also need to update test case results 
            // directory name   
            FileLock fileLock = null;
            try {
                fileLock = mTestCaseResultsDir.lock();
                mTestCaseResultsDir.rename(fileLock, name, null);                
            } catch (IOException ex) {
                // revert back
                super.setName(oldName);
                
                NotifyDescriptor d1 = new NotifyDescriptor.Message(ex.getMessage(),
                        NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d1);
            } finally {
                if (fileLock != null) {
                    fileLock.releaseLock();
                }
            }
        }
    }
    
    private void registerPropertyFileChangeListener(final FileObject testcaseDir) {
        
        List<PropertySpec> specList = new ArrayList<PropertySpec>();
        specList.add(PropertySpec.DESCRIPTION);
        specList.add(PropertySpec.DESTINATION);
        specList.add(PropertySpec.SOAP_ACTION);
        specList.add(PropertySpec.INPUT_FILE);
        specList.add(PropertySpec.OUTPUT_FILE);
        specList.add(PropertySpec.CONCURRENT_THREADS);
        specList.add(PropertySpec.INVOKES_PER_THREAD);
        specList.add(PropertySpec.TEST_TIMEOUT);
        specList.add(PropertySpec.CALCULATE_THROUGHPUT);
        specList.add(PropertySpec.COMPARISON_TYPE);
        specList.add(PropertySpec.FEATURE_STATUS);
        
        // Is there only one property file per test case?
        mPropertyFile = testcaseDir.getFileObject("Invoke.properties"); // NOI18N
        if (mPropertyFile == null) {
            mPropertyFile = testcaseDir.getFileObject("Concurrent.properties"); // NOI18N
        }
        if (mPropertyFile == null) {
            mPropertyFile = testcaseDir.getFileObject("Correlation.properties"); // NOI18N
        }
        mPropertyFileWrapper = new PropertyFileWrapper(mPropertyFile, specList);
        mPropertyFile.addFileChangeListener(new PropertyFileChangeListener());
    }
    
    private void update() {
        RequestProcessor.getDefault().post(
                new Runnable() {
            public void run() {
                updateChildren();
            }
        });
    }
    
    private FileObject getFileObject() {
        DataObject dataObj = getLookup().lookup(DataObject.class);
        return dataObj.getPrimaryFile();
    }
    
    private void updateChildren() {
        if (mChildren != null) {
            mChildren.addNotify();
        }
    }
    
    // @overwrite
    public boolean canCut() {
        return false;
    }
    
    // @overwrite
    public boolean canCopy() {
        return false;
    }
    
    public Node.Cookie getCookie(Class type) {
        if (type == TestcaseCookie.class) {
            return mTestcaseCookie;
        }
        return super.getCookie(type);
    }
    
    public JbiProject getProject() {
        return mProject;
    }
    
    public FileObject getTestCaseDir() {
        return mTestcaseDir;
    }
    
    public Action[] getActions(boolean context) {
        List<Action> actionList = new ArrayList<Action>();
        actionList.add(SystemAction.get(TestCaseRunAction.class));
        actionList.add(SystemAction.get(TestCaseDebugAction.class));
        actionList.add(null);
        actionList.add(SystemAction.get(TestcaseDiffAction.class));
        actionList.add(null);
        actionList.add(SystemAction.get(DeleteAction.class));
        actionList.add(SystemAction.get(TestCaseResultsDeleteAction.class));
        actionList.add(null);
        actionList.add(SystemAction.get(PropertiesAction.class));
        
        return actionList.toArray(new Action[0]);
    }
    
    public void destroy() throws IOException {
        
        super.destroy();
        
        // Close diff view
        if (isDiffTopComponentVisible()) {
            closeDiffTopComponent();
            releaseDiffTopComponent();
        }
        
        // Delete test case result directory
        FileObject actualFileLocation = getResultFileLocation();
        if (actualFileLocation != null) {
            try {
                actualFileLocation.delete();
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public Node.PropertySet[] getPropertySets() {
        return mPropertyFileWrapper.getSheet().toArray();
    }
    
    public FileObject getOutputFile() {
        return getOutputFile(false);
    }
    
    public FileObject getOutputFile(boolean create) {
        FileObject testCaseDir = getTestCaseDir();
        FileObject ret = testCaseDir.getFileObject("Output.xml"); // NOI18N
        
        if (ret == null) {
            try {
                ret = testCaseDir.createData("Output.xml"); // NOI18N
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return ret;
    }
    
    private FileObject getResultFileLocation() {
        FileObject actualFolder = null;
        
        FileObject resultsFolder = mProject.getTestResultsDirectory();
        if (resultsFolder != null) {
            String testcaseName = mTestcaseDir.getName();
            actualFolder = resultsFolder.getFileObject(testcaseName);
            if (actualFolder == null) {
                try {
                    actualFolder = resultsFolder.createFolder(testcaseName);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return actualFolder;
    }
    
    public List<String> getSortedResultFileNameList(boolean showFailedOnly) {
        List<String> list = new ArrayList<String>();
        
        FileObject resultFileLocation = getResultFileLocation();
        if (resultFileLocation != null) {
            FileObject[] fos = getResultFileLocation().getChildren();
            for (int i = 0; i < fos.length; i++) {
                String name = fos[i].getNameExt();
                if (showFailedOnly && name.matches(FAILED_ACTUAL_OUTPUT_REGEX) ||
                        !showFailedOnly && name.matches(ACTUAL_OUTPUT_REGEX)) {
                    list.add(name);
                }
            }
            Collections.sort(list);
        }
        
        return list;
    }
    
    public FileObject getLatestResultFileObject() {
        FileObject resultFO = null;
        
        List<String> resultFileNames = getSortedResultFileNameList(false);
        int length = resultFileNames.size();
        if (length > 0) {
            String latestResultFileName = resultFileNames.get(length - 1);
            FileObject resultDir = getResultFileLocation();
            resultFO = resultDir.getFileObject(latestResultFileName);
        }
        
        return resultFO;
    }
    
    public void showDiffTopComponentVisible() {
        if (mDiffTopComponent == null) {
            mDiffTopComponent = new DiffTopComponent(this);
        } else {
            mDiffTopComponent.refreshView(true, true);
        }
        mDiffTopComponent.open();
        mDiffTopComponent.requestActive();
    }
    
    public void showDiffTopComponentVisible(String actualFileName) {
        if (mDiffTopComponent == null) {
            mDiffTopComponent = new DiffTopComponent(this);
        }
        
        mDiffTopComponent.refreshView(true, actualFileName);
        
        mDiffTopComponent.open();
        mDiffTopComponent.requestActive();
    }
    
    public void closeDiffTopComponent() {
        if (SwingUtilities.isEventDispatchThread()) {
            mDiffTopComponent.close();
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        mDiffTopComponent.close();
                    }
                });
            } catch (java.lang.InterruptedException ex) {
                ex.printStackTrace();
            } catch (java.lang.reflect.InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public boolean isDiffTopComponentVisible() {
        return mDiffTopComponent != null;
    }
    
    public void refreshDiffTopComponent() {
        if (mDiffTopComponent != null) {
            mDiffTopComponent.refreshView(true, true);
        }
    }
    
    public void releaseDiffTopComponent() {
        mDiffTopComponent = null;
    }
    
    public org.netbeans.api.diff.StreamSource getActualStreamSource(String fileName) {
        FileObject fo = getResultFileLocation().getFileObject(fileName);
        File file = null;
        if (fo != null) {
            file = FileUtil.toFile(fo);
        }
        return new DiffStreamSource(fileName, TestCaseResultNode.getActualResultTimeStamp(fileName), file);
    }
    
    public org.netbeans.api.diff.StreamSource getExpectedStreamSource() {
        FileObject fo = mTestcaseDir.getFileObject("Output.xml");   // NOI18N
        File file = FileUtil.toFile(fo);
        return new DiffStreamSource("Output.xml", // NOI18N
                java.util.ResourceBundle.getBundle("org/netbeans/modules/compapp/test/ui/Bundle").getString("Expected_Output"), file); // NOI18N
    }
    
    /**
     * Checks whether the test case represented by this node is running.
     */
    public boolean isTestCaseRunning() {
        return isTestCaseRunning(mTestcaseDir);
    }
    
    /**
     * Sets whether the test case represented by this node is running.
     */
    public void setTestCaseRunning(boolean isRunning) {
        setTestCaseRunning(mTestcaseDir, isRunning); 
    }
    
    /**
     * Checks whether the test case corresponding to the given file object 
     * is running.
     * 
     * @param testcaseDirFO file object corresponding to a test case directory
     */
    public static boolean isTestCaseRunning(FileObject testcaseDirFO) {
        return runningTestCases.contains(testcaseDirFO);
    }
    
    /**
     * Sets whether the test case corresponding to the given file object 
     * is running.
     * 
     * @param testcaseDirFO file object corresponding to a test case directory
     */
    public static void setTestCaseRunning(FileObject testcaseDirFO, boolean isRunning) {
        if (isRunning) {
            System.out.println("Set test case running: " + testcaseDirFO);
            runningTestCases.add(testcaseDirFO);
        } else {
            System.out.println("Set test case not running: " + testcaseDirFO);
            runningTestCases.remove(testcaseDirFO);            
        }
    }
    
    class PropertyFileChangeListener extends FileChangeAdapter {
        public void fileChanged(FileEvent fe) {
            mPropertyFileWrapper.loadProperties();
            // Notify Properties pane to repaint
            TestcaseNode.this.firePropertyChange(null, null, null);
        }        
    }
    
    // Until we provide Paste action in the context menu, paste or DnD is
    // forbidden for now. (IZ 79815)
    // @overwrite
    public PasteType[] getPasteTypes(Transferable t) {
        return new PasteType[] {};
    }
    
    // @overwrite
    public PasteType getDropType(Transferable t, int action, int index) {
        return null;
    }
    
    // TODO: see TestCaseDeleteAction
//    public void destroy() throws java.io.IOException {
//         // close diff view
//        if (isDiffTopComponentVisible()) {
//            closeDiffTopComponent();
//            releaseDiffTopComponent();
//        }
//
//        super.destroy();
//    }
    
    public Image getIcon(int type) {
        return computeIcon(false, type);
    }
    
    public Image getOpenedIcon(int type)  {
        return computeIcon(true, type);
    }
    
    public void deleteResults() {
        // Close diff view
        if (isDiffTopComponentVisible()) {
            closeDiffTopComponent();
            releaseDiffTopComponent();
        }
        
        // Delete test case result directory
        FileObject actualFileLocation = getResultFileLocation();
        if (actualFileLocation != null) {
            try {
                actualFileLocation.delete();
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private Image computeIcon(boolean opened, int type) {
        FileObject fo = mTestcaseDir.getFileObject("Output.xml");   // NOI18N
        
        Image image = TEST_CASE_ICON; //opened ? super.getOpenedIcon(type) : super.getIcon(type);
        File file = null;
        
        if (fo != null) {
            file = FileUtil.toFile(fo);
        }
        
        if (file != null && file.length() > 0) {
            setShortDescription(this.getName());
            return image;
        } else {
            setShortDescription(NbBundle.getMessage(TestcaseNode.class, "HINT_WARNING_BADGE")); // NOI18N
            return Utilities.mergeImages(image, WARNING_BADGE, 15, 8); //7, 5);
        }
    }
    
    private void checkOutputChange(FileObject fo) {
        if (fo != null && fo.getNameExt().equals("Output.xml")) {   // NOI18N
            fireIconChange();
        }
    }
}
