/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version 1.0
 * (the "License"). You may not use this file except in compliance with the
 * License. A copy of the License is available at http://www.sun.com/.
 *
 * The Original Code is the Jemmy library. The Initial Developer of the
 * Original Code is Alexandre Iline. All Rights Reserved.
 *
 * ---------------------------------------------------------------------------
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy.operators;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A JUnit test for JFileChooser.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JFileChooserOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the file chooser.
     */
    private JFileChooser fileChooser;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JFileChooserOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup for testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        fileChooser = new JFileChooser();
        frame.getContentPane().add(fileChooser);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
        frame.dispose();
        frame = null;
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(JFileChooserOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JDialog dialog = new JDialog();
        dialog.setModal(false);
        dialog.getContentPane().add(fileChooser);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        
        JFileChooserOperator operator2 = new JFileChooserOperator(operator);
        assertNotNull(operator2);
        
        dialog.setVisible(false);
        dialog.dispose();
        dialog = null;
        
        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);
        assertNotNull(operator3);
    }

    /**
     * Test findJFileChooserDialog method.
     */
    public void testFindJFileChooserDialog() {
    }

    /**
     * Test of waitJFileChooserDialog method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testWaitJFileChooserDialog() {
    }

    /**
     * Test findJFileChooser method.
     */
    public void testFindJFileChooser() {
        frame.setVisible(true);
        
        JFileChooser fileChooser1 = JFileChooserOperator.findJFileChooser(frame);
        assertNotNull(fileChooser1);
        
        JDialog dialog = new JDialog();
        dialog.setModal(false);
        dialog.getContentPane().add(fileChooser);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        
        JFileChooser fileChooser2 = JFileChooserOperator.findJFileChooser();
        assertNotNull(fileChooser2);
        
        dialog.setVisible(false);
        dialog.dispose();
        dialog = null;
    }
    
    /**
     * Test of waitJFileChooser method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testWaitJFileChooser() {
    }

    /**
     * Test of setTimeouts method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetTimeouts() {
    }

    /**
     * Test of getTimeouts method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetTimeouts() {
    }

    /**
     * Test of setOutput method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetOutput() {
    }

    /**
     * Test of getOutput method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetOutput() {
    }

    /**
     * Test of getPathCombo method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetPathCombo() {
    }

    /**
     * Test of getFileTypesCombo method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetFileTypesCombo() {
    }

    /**
     * Test of getApproveButton method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetApproveButton() {
    }

    /**
     * Test of getCancelButton method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetCancelButton() {
    }

    /**
     * Test of getHomeButton method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetHomeButton() {
    }

    /**
     * Test of getUpLevelButton method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetUpLevelButton() {
    }

    /**
     * Test of getListToggleButton method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetListToggleButton() {
    }

    /**
     * Test of getDetailsToggleButton method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetDetailsToggleButton() {
    }

    /**
     * Test of getPathField method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetPathField() {
    }

    /**
     * Test of getFileList method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetFileList() {
    }

    /**
     * Test of approve method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testApprove() {
    }

    /**
     * Test of cancel method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testCancel() {
        // TODO add your test code.
    }

    /**
     * Test of chooseFile method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testChooseFile() {
        // TODO add your test code.
    }

    /**
     * Test of goUpLevel method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGoUpLevel() {
        // TODO add your test code.
    }

    /**
     * Test of goHome method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGoHome() {
        // TODO add your test code.
    }

    /**
     * Test of clickOnFile method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testClickOnFile() {
        // TODO add your test code.
    }

    /**
     * Test of enterSubDir method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testEnterSubDir() {
        // TODO add your test code.
    }

    /**
     * Test of selectFile method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSelectFile() {
        // TODO add your test code.
    }

    /**
     * Test of selectPathDirectory method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSelectPathDirectory() {
        // TODO add your test code.
    }

    /**
     * Test of selectFileType method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSelectFileType() {
        // TODO add your test code.
    }

    /**
     * Test of checkFileDisplayed method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testCheckFileDisplayed() {
        // TODO add your test code.
    }

    /**
     * Test of getFileCount method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetFileCount() {
        // TODO add your test code.
    }

    /**
     * Test of getFiles method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetFiles() {
        // TODO add your test code.
    }

    /**
     * Test of waitFileCount method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testWaitFileCount() {
        // TODO add your test code.
    }

    /**
     * Test of waitFileDisplayed method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testWaitFileDisplayed() {
        // TODO add your test code.
    }

    /**
     * Test of accept method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testAccept() {
        // TODO add your test code.
    }

    /**
     * Test of addActionListener method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testAddActionListener() {
        // TODO add your test code.
    }

    /**
     * Test of addChoosableFileFilter method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testAddChoosableFileFilter() {
        // TODO add your test code.
    }

    /**
     * Test of approveSelection method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testApproveSelection() {
        // TODO add your test code.
    }

    /**
     * Test of cancelSelection method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testCancelSelection() {
        // TODO add your test code.
    }

    /**
     * Test of changeToParentDirectory method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testChangeToParentDirectory() {
        // TODO add your test code.
    }

    /**
     * Test of ensureFileIsVisible method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testEnsureFileIsVisible() {
        // TODO add your test code.
    }

    /**
     * Test of getAcceptAllFileFilter method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetAcceptAllFileFilter() {
        // TODO add your test code.
    }

    /**
     * Test of getAccessory method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetAccessory() {
        // TODO add your test code.
    }

    /**
     * Test of getApproveButtonMnemonic method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetApproveButtonMnemonic() {
        // TODO add your test code.
    }

    /**
     * Test of getApproveButtonText method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetApproveButtonText() {
        // TODO add your test code.
    }

    /**
     * Test of getApproveButtonToolTipText method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetApproveButtonToolTipText() {
        // TODO add your test code.
    }

    /**
     * Test of getChoosableFileFilters method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetChoosableFileFilters() {
        // TODO add your test code.
    }

    /**
     * Test of getCurrentDirectory method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetCurrentDirectory() {
        // TODO add your test code.
    }

    /**
     * Test of getDescription method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetDescription() {
        // TODO add your test code.
    }

    /**
     * Test of getDialogTitle method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetDialogTitle() {
        // TODO add your test code.
    }

    /**
     * Test of getDialogType method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetDialogType() {
        // TODO add your test code.
    }

    /**
     * Test of getFileFilter method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetFileFilter() {
        // TODO add your test code.
    }

    /**
     * Test of getFileSelectionMode method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetFileSelectionMode() {
        // TODO add your test code.
    }

    /**
     * Test of getFileSystemView method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetFileSystemView() {
        // TODO add your test code.
    }

    /**
     * Test of getFileView method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetFileView() {
        // TODO add your test code.
    }

    /**
     * Test of getIcon method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetIcon() {
        // TODO add your test code.
    }

    /**
     * Test of getName method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetName() {
        // TODO add your test code.
    }

    /**
     * Test of getSelectedFile method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetSelectedFile() {
        // TODO add your test code.
    }

    /**
     * Test of getSelectedFiles method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetSelectedFiles() {
        // TODO add your test code.
    }

    /**
     * Test of getTypeDescription method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetTypeDescription() {
        // TODO add your test code.
    }

    /**
     * Test of getUI method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testGetUI() {
        // TODO add your test code.
    }

    /**
     * Test of isDirectorySelectionEnabled method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testIsDirectorySelectionEnabled() {
        // TODO add your test code.
    }

    /**
     * Test of isFileHidingEnabled method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testIsFileHidingEnabled() {
        // TODO add your test code.
    }

    /**
     * Test of isFileSelectionEnabled method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testIsFileSelectionEnabled() {
        // TODO add your test code.
    }

    /**
     * Test of isMultiSelectionEnabled method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testIsMultiSelectionEnabled() {
        // TODO add your test code.
    }

    /**
     * Test of isTraversable method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testIsTraversable() {
        // TODO add your test code.
    }

    /**
     * Test of removeActionListener method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testRemoveActionListener() {
        // TODO add your test code.
    }

    /**
     * Test of removeChoosableFileFilter method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testRemoveChoosableFileFilter() {
        // TODO add your test code.
    }

    /**
     * Test of rescanCurrentDirectory method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testRescanCurrentDirectory() {
        // TODO add your test code.
    }

    /**
     * Test of resetChoosableFileFilters method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testResetChoosableFileFilters() {
        // TODO add your test code.
    }

    /**
     * Test of setAccessory method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetAccessory() {
        // TODO add your test code.
    }

    /**
     * Test of setApproveButtonMnemonic method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetApproveButtonMnemonic() {
        // TODO add your test code.
    }

    /**
     * Test of setApproveButtonText method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetApproveButtonText() {
        // TODO add your test code.
    }

    /**
     * Test of setApproveButtonToolTipText method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetApproveButtonToolTipText() {
        // TODO add your test code.
    }

    /**
     * Test of setCurrentDirectory method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetCurrentDirectory() {
        // TODO add your test code.
    }

    /**
     * Test of setDialogTitle method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetDialogTitle() {
        // TODO add your test code.
    }

    /**
     * Test of setDialogType method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetDialogType() {
        // TODO add your test code.
    }

    /**
     * Test of setFileFilter method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetFileFilter() {
        // TODO add your test code.
    }

    /**
     * Test of setFileHidingEnabled method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetFileHidingEnabled() {
        // TODO add your test code.
    }

    /**
     * Test of setFileSelectionMode method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetFileSelectionMode() {
        // TODO add your test code.
    }

    /**
     * Test of setFileSystemView method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetFileSystemView() {
        // TODO add your test code.
    }

    /**
     * Test of setFileView method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetFileView() {
        // TODO add your test code.
    }

    /**
     * Test of setMultiSelectionEnabled method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetMultiSelectionEnabled() {
        // TODO add your test code.
    }

    /**
     * Test of setSelectedFile method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetSelectedFile() {
        // TODO add your test code.
    }

    /**
     * Test of setSelectedFiles method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testSetSelectedFiles() {
        // TODO add your test code.
    }

    /**
     * Test of showDialog method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testShowDialog() {
        // TODO add your test code.
    }

    /**
     * Test of showOpenDialog method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testShowOpenDialog() {
        // TODO add your test code.
    }

    /**
     * Test of showSaveDialog method, of class org.netbeans.jemmy.operators.JFileChooserOperator.
     */
    public void testShowSaveDialog() {
        // TODO add your test code.
    }
    
}
