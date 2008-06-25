/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
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
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 *
 * The Original Software is the Jemmy library. The Initial Developer of the
 * Original Software is Alexandre Iline. All Rights Reserved.
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
package org.netbeans.jemmy.operators;



import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import java.io.File;

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

        

        File file = new File(".");

        fileChooser.setCurrentDirectory(file);

        

        File file2 = new File("showit.txt");

        if(!file2.exists()) file2.createNewFile();

        

        File file3 = new File("showit");

        file3.mkdir();

    }

    

    /**

     * Cleanup after testing.

     */

    protected void tearDown() throws Exception {

        frame.setVisible(false);

        frame.dispose();

        frame = null;

        

        File file = new File("showit.txt");

        file.delete();

        

        File file2 = new File("showit");

        file2.delete();

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

     * Test waitJFileChooserDialog method.

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

     * Test waitJFileChooser method.

     */

    public void testWaitJFileChooser() {

        frame.setVisible(true);

        

        JFileChooser fileChooser1 = JFileChooserOperator.waitJFileChooser(frame);

        assertNotNull(fileChooser1);

    }

    

    /**

     * Test getPathCombo method.

     */

    public void testGetPathCombo() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getPathCombo();

    }

    

    /**

     * Test getFileTypesCombo method.

     */

    public void testGetFileTypesCombo() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getFileTypesCombo();

    }

    

    /**

     * Test getApproveButton method.

     */

    public void testGetApproveButton() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getApproveButton();

    }

    

    /**

     * Test getCancelButton method.

     */

    public void testGetCancelButton() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getCancelButton();

    }

    

    /**

     * Test getHomeButton method.

     */

    public void testGetHomeButton() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getHomeButton();

    }

    

    /**

     * Test getUpLevelButton method.

     */

    public void testGetUpLevelButton() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getUpLevelButton();

    }

    

    /**

     * Test getListToggleButton method.

     */

    public void testGetListToggleButton() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getListToggleButton();

    }

    

    /**

     * Test getDetailsToggleButton method.

     */

    public void testGetDetailsToggleButton() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getDetailsToggleButton();

    }

    

    /**

     * Test getPathField method.

     */

    public void testGetPathField() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getPathField();

    }

    

    /**

     * Test getFileList method.

     */

    public void testGetFileList() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getFileList();

    }

    

    /**

     * Test approve method.

     */

    public void testApprove() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.approve();

    }

    

    /**

     * Test cancel method.

     */

    public void testCancel() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.cancel();

    }

    

    /**

     * Test chooseFile method.

     */

    public void testChooseFile() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.chooseFile("1234");

    }

    

    /**

     * Test goUpLevel method.

     */

    public void testGoUpLevel() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setCurrentDirectory(new File("showit"));

        operator3.goUpLevel();

    }

    

    /**

     * Test goHome method.

     */

    public void testGoHome() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.goHome();

    }

    

    /**

     * Test clickOnFile method.

     */

    public void testClickOnFile() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.clickOnFile("showit.txt");

    }

    

    /**

     * Test enterSubDir method.

     */

    public void testEnterSubDir() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.enterSubDir("showit");

    }

    

    /**

     * Test selectFile method.

     */

    public void testSelectFile() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.selectFile("showit.txt");

    }

    

    /**

     * Test selectPathDirectory method.

     */

    public void testSelectPathDirectory() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.selectPathDirectory("1234");

    }

    

    /**

     * Test selectFileType method.

     */

    public void testSelectFileType() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.selectFileType("1234");

    }

    

    /**

     * Test checkFileDisplayed method.

     */

    public void testCheckFileDisplayed() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.checkFileDisplayed("showit.txt");

    }

    

    /**

     * Test getFileCount method.

     */

    public void testGetFileCount() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getFileCount();

    }

    

    /**

     * Test getFiles method.

     */

    public void testGetFiles() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getFiles();

    }

    

    /**

     * Test waitFileCount method.

     */

    public void testWaitFileCount() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        fileChooser.setCurrentDirectory(new File("showit"));

        operator3.waitFileCount(0);

        fileChooser.setCurrentDirectory(new File("."));

    }

    

    /**

     * Test waitFileDisplayed method.

     */

    public void testWaitFileDisplayed() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.waitFileDisplayed("showit.txt");

    }

    

    /**

     * Test accept method.

     */

    public void testAccept() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.accept(new File("showit.txt"));

    }

    

    /**

     * Test addActionListener method.

     */

    public void testAddActionListener() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        ActionListenerTest listener = new ActionListenerTest();

        operator3.addActionListener(listener);

        operator3.removeActionListener(listener);

    }

    

    /**

     * Inner class needed for testing.

     */

    public class ActionListenerTest implements ActionListener {

        public void actionPerformed(ActionEvent e) {

        }

    }

    

    /**

     * Test addChoosableFileFilter method.

     */

    public void testAddChoosableFileFilter() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.addChoosableFileFilter(operator3.getChoosableFileFilters()[0]);

        operator3.removeChoosableFileFilter(operator3.getChoosableFileFilters()[0]);

    }

    

    /**

     * Test approveSelection method.

     */

    public void testApproveSelection() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.approveSelection();

    }

    

    /**

     * Test cancelSelection method.

     */

    public void testCancelSelection() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.cancelSelection();

    }

    

    /**

     * Test changeToParentDirectory method.

     */

    public void testChangeToParentDirectory() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.changeToParentDirectory();

    }

    

    /**

     * Test ensureFileIsVisible method.

     */

    public void testEnsureFileIsVisible() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.ensureFileIsVisible(new File("showit.txt"));

    }

    

    /**

     * Test getAcceptAllFileFilter method.

     */

    public void testGetAcceptAllFileFilter() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getAcceptAllFileFilter();

    }

    

    /**

     * Test getAccessory method.

     */

    public void testGetAccessory() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setAccessory(operator3.getAccessory());

    }

    

    /**

     * Test getApproveButtonMnemonic method.

     */

    public void testGetApproveButtonMnemonic() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setApproveButtonMnemonic(operator3.getApproveButtonMnemonic());

        operator3.setApproveButtonMnemonic('a');

    }

    

    /**

     * Test getApproveButtonText method.

     */

    public void testGetApproveButtonText() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setApproveButtonText(operator3.getApproveButtonText());

    }

    

    /**

     * Test getApproveButtonToolTipText method.

     */

    public void testGetApproveButtonToolTipText() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setApproveButtonToolTipText(operator3.getApproveButtonToolTipText());

    }

    

    /**

     * Test getChoosableFileFilters method.

     */

    public void testGetChoosableFileFilters() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getChoosableFileFilters();

    }

    

    /**

     * Test getCurrentDirectory method.

     */

    public void testGetCurrentDirectory() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setCurrentDirectory(operator3.getCurrentDirectory());

    }

    

    /**

     * Test getDescription method.

     */

    public void testGetDescription() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getDescription(new File("showit.txt"));

    }

    

    /**

     * Test getDialogTitle method.

     */

    public void testGetDialogTitle() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setDialogTitle(operator3.getDialogTitle());

    }

    

    /**

     * Test getDialogType method.

     */

    public void testGetDialogType() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setDialogType(operator3.getDialogType());

    }

    

    /**

     * Test getFileFilter method.

     */

    public void testGetFileFilter() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setFileFilter(operator3.getFileFilter());

    }

    

    /**

     * Test getFileSelectionMode method.

     */

    public void testGetFileSelectionMode() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setFileSelectionMode(operator3.getFileSelectionMode());

    }

    

    /**

     * Test getFileSystemView method.

     */

    public void testGetFileSystemView() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setFileSystemView(operator3.getFileSystemView());

    }

    

    /**

     * Test getFileView method.

     */

    public void testGetFileView() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setFileView(operator3.getFileView());

    }

    

    /**

     * Test getIcon method.

     */

    public void testGetIcon() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getIcon(new File("showit.txt"));

    }

    

    /**

     * Test getName method.

     */

    public void testGetName() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getName(new File("showit.txt"));

    }

    

    /**

     * Test getSelectedFile method.

     */

    public void testGetSelectedFile() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setSelectedFile(operator3.getSelectedFile());

    }

    

    /**

     * Test getSelectedFiles method.

     */

    public void testGetSelectedFiles() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setSelectedFiles(operator3.getSelectedFiles());

    }

    

    /**

     * Test getTypeDescription method.

     */

    public void testGetTypeDescription() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getTypeDescription(new File("showit.txt"));

    }

    

    /**

     * Test getUI method.

     */

    public void testGetUI() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.getUI();

    }

    

    /**

     * Test isDirectorySelectionEnabled method.

     */

    public void testIsDirectorySelectionEnabled() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.isDirectorySelectionEnabled();

    }

    

    /**

     * Test isFileHidingEnabled method.

     */

    public void testIsFileHidingEnabled() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setFileHidingEnabled(operator3.isFileHidingEnabled());

    }

    

    /**

     * Test isFileSelectionEnabled method.

     */

    public void testIsFileSelectionEnabled() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.isFileSelectionEnabled();

    }

    

    /**

     * Test isMultiSelectionEnabled method.

     */

    public void testIsMultiSelectionEnabled() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.setMultiSelectionEnabled(operator3.isMultiSelectionEnabled());

    }

    

    /**

     * Test isTraversable method.

     */

    public void testIsTraversable() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.isTraversable(new File("showit.txt"));

    }

    

    /**

     * Test removeActionListener method.

     */

    public void testRemoveActionListener() {

        // TODO add your test code.

    }

    

    /**

     * Test removeChoosableFileFilter method.

     */

    public void testRemoveChoosableFileFilter() {

        // TODO add your test code.

    }

    

    /**

     * Test rescanCurrentDirectory method.

     */

    public void testRescanCurrentDirectory() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.rescanCurrentDirectory();

    }

    

    /**

     * Test resetChoosableFileFilters method.

     */

    public void testResetChoosableFileFilters() {

        frame.setVisible(true);

        

        JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        operator3.resetChoosableFileFilters();

    }

        

    /**

     * Test showDialog method.

     */

    public void testShowDialog() {

        final JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        Runnable runnable = new Runnable() {

            public void run() {

                operator3.showDialog(null, "Plus");

            }

        };

        

        new Thread(runnable).start();



        try {

            Thread.sleep(1000);

        }

        catch(InterruptedException exception) {

        }

        operator3.setVisible(false);

    }

    

    /**

     * Test showOpenDialog method.

     */

    public void testShowOpenDialog() {

        final JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);

        

        Runnable runnable = new Runnable() {

            public void run() {

                operator3.showOpenDialog(null);

            }

        };

        

        new Thread(runnable).start();

        

        try {

            Thread.sleep(1000);

        }

        catch(InterruptedException exception) {

        }

        operator3.setVisible(false);

    }

    

    /**

     * Test showSaveDialog method.

     */

    public void testShowSaveDialog() {

        final JFileChooserOperator operator3 = new JFileChooserOperator(fileChooser);

        assertNotNull(operator3);



        Runnable runnable = new Runnable() {

            public void run() {

                operator3.showSaveDialog(null);

            }

        };

        

        new Thread(runnable).start();



        try {

            Thread.sleep(1000);

        }

        catch(InterruptedException exception) {

        }

        operator3.setVisible(false);

    }

}

