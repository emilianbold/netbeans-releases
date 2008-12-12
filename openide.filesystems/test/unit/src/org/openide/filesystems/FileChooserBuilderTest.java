/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.openide.filesystems;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileFilter;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.util.RequestProcessor;
import static org.junit.Assert.*;

/**
 *
 * @author tim
 */
public class FileChooserBuilderTest extends NbTestCase {

    public FileChooserBuilderTest(String name) {
        super(name);
    }

    /**
     * Test of setDirectoriesOnly method, of class FileChooserBuilder.
     */
    @Test
    public void testSetDirectoriesOnly() {
        System.out.println("setDirectoriesOnly");
        FileChooserBuilder instance = new FileChooserBuilder("x");
        boolean dirsOnly = instance.createFileChooser().getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY;
        assertFalse(dirsOnly);
        instance.setDirectoriesOnly(true);
        dirsOnly = instance.createFileChooser().getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY;
        assertTrue(dirsOnly);
    }

    /**
     * Test of setFilesOnly method, of class FileChooserBuilder.
     */
    @Test
    public void testSetFilesOnly() {
        System.out.println("setFilesOnly");
        FileChooserBuilder instance = new FileChooserBuilder("y");
        boolean filesOnly = instance.createFileChooser().getFileSelectionMode() == JFileChooser.FILES_ONLY;
        assertFalse(filesOnly);
        instance.setFilesOnly(true);
        filesOnly = instance.createFileChooser().getFileSelectionMode() == JFileChooser.FILES_ONLY;
        assertTrue(filesOnly);
    }

    /**
     * Test of setTitle method, of class FileChooserBuilder.
     */
    @Test
    public void testSetTitle() {
        System.out.println("setTitle");
        FileChooserBuilder instance = new FileChooserBuilder("a");
        assertNull(instance.createFileChooser().getDialogTitle());
        instance.setTitle("foo");
        assertEquals("foo", instance.createFileChooser().getDialogTitle());
    }

    /**
     * Test of setApproveText method, of class FileChooserBuilder.
     */
    @Test
    public void testSetApproveText() {
        System.out.println("setApproveText");
        FileChooserBuilder instance = new FileChooserBuilder("b");
        assertNull(instance.createFileChooser().getDialogTitle());
        instance.setApproveText("bar");
        assertEquals("bar", instance.createFileChooser().getApproveButtonText());
    }

    /**
     * Test of setFileFilter method, of class FileChooserBuilder.
     */
    @Test
    public void testSetFileFilter() {
        System.out.println("setFileFilter");
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File f) {
                return true;
            }

            @Override
            public String getDescription() {
                return "X";
            }
        };
        FileChooserBuilder instance = new FileChooserBuilder("c");
        instance.setFileFilter(filter);
        assertEquals(filter, instance.createFileChooser().getFileFilter());
    }

    /**
     * Test of setDefaultWorkingDirectory method, of class FileChooserBuilder.
     */
    @Test
    public void testSetDefaultWorkingDirectory() throws IOException {
        System.out.println("setDefaultWorkingDirectory");
        FileChooserBuilder instance = new FileChooserBuilder("d");
        File dir = getWorkDir();
        assertTrue("tmpdir is not sane", dir.exists() && dir.isDirectory());
        instance.setDefaultWorkingDirectory(dir);
        assertEquals(dir, instance.createFileChooser().getCurrentDirectory());
    }

    /**
     * Test of setFileHiding method, of class FileChooserBuilder.
     */
    @Test
    public void testSetFileHiding() {
        System.out.println("setFileHiding");
        FileChooserBuilder instance = new FileChooserBuilder("e");
        assertFalse(instance.createFileChooser().isFileHidingEnabled());
        instance.setFileHiding(true);
        assertTrue(instance.createFileChooser().isFileHidingEnabled());
    }

    /**
     * Test of setControlButtonsAreShown method, of class FileChooserBuilder.
     */
    @Test
    public void testSetControlButtonsAreShown() {
        System.out.println("setControlButtonsAreShown");
        FileChooserBuilder instance = new FileChooserBuilder("f");
        assertTrue(instance.createFileChooser().getControlButtonsAreShown());
        instance.setControlButtonsAreShown(false);
        assertFalse(instance.createFileChooser().getControlButtonsAreShown());
    }

    /**
     * Test of setAccessibleDescription method, of class FileChooserBuilder.
     */
    @Test
    public void testSetAccessibleDescription() {
        System.out.println("setAccessibleDescription");
        FileChooserBuilder instance = new FileChooserBuilder("g");
        String desc = "desc";
        instance.setAccessibleDescription(desc);
        assertEquals(desc, instance.createFileChooser().getAccessibleContext().getAccessibleDescription());
    }

    /**
     * Test of createFileChooser method, of class FileChooserBuilder.
     */
    @Test
    public void testCreateFileChooser() {
        System.out.println("createFileChooser");
        FileChooserBuilder instance = new FileChooserBuilder("h");
        assertNotNull(instance.createFileChooser());
    }

    private static AbstractButton findDefaultButton(Container c) {
        if (c instanceof AbstractButton && "Snorkelbreath".equals(((AbstractButton) c).getText())) {
            return (JButton) c;
        } else {
            for (Component comp : c.getComponents()) {
                if (comp instanceof Container) {
                    AbstractButton result = findDefaultButton((Container) comp);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    @Test
    public void testForceUseOfDefaultWorkingDirectory() throws InterruptedException, IOException {
        System.out.println("forceUseOfDefaultWorkingDirectory");
        FileChooserBuilder instance = new FileChooserBuilder("i");
        instance.setDirectoriesOnly(true);
        final File toDir = getWorkDir();
        final File selDir = new File(toDir, "sel" + System.currentTimeMillis());
        if (!selDir.exists()) {
            assertTrue(selDir.mkdirs());
        }

        instance.setApproveText("Snorkelbreath");
        final JFileChooser ch = instance.createFileChooser();
        class X implements Runnable, AncestorListener {
            volatile int run = -2;
            public void run() {
                run++;
                System.out.println("  run " + run);
                switch (run) {
                    case -1:
                        break;
                    case 0:
                        ch.setCurrentDirectory(toDir);
                        assertEquals(toDir, ch.getCurrentDirectory());
                        break;
                    case 1:
                        ch.setSelectedFile(selDir);
                        break;
                    case 2:
                        assertTrue(ch.isVisible());
                        AbstractButton defButton = findDefaultButton(ch.getTopLevelAncestor());
                        assertNotNull(defButton);
                        assertTrue(defButton.isEnabled());
                        defButton.doClick();
                        break;
                    case 3:
                        synchronized (X.this) {
                            X.this.notifyAll();
                        }
                        break;
                    default:
                        return;
                }
                EventQueue.invokeLater(this);
            }

            public void ancestorAdded(AncestorEvent event) {
                run();
            }

            public void ancestorRemoved(AncestorEvent event) {
            }

            public void ancestorMoved(AncestorEvent event) {
            }
        }

        X x = new X();
        ch.addAncestorListener(x);
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                assertEquals(JFileChooser.APPROVE_OPTION, ch.showOpenDialog(null));
            }
        }).waitFinished(5000);
        synchronized (x) {
            x.wait(5000);
        }

        assertEquals(toDir, ch.getCurrentDirectory());

        instance = new FileChooserBuilder("i");
        assertEquals("Directory not retained", toDir, instance.createFileChooser().getCurrentDirectory());

        File userHome = new File(System.getProperty("user.home"));
        assertTrue("Environment not sane", userHome.exists() && userHome.isDirectory());
        instance.forceUseOfDefaultWorkingDirectory(true).setDefaultWorkingDirectory(userHome);

        assertEquals(userHome, instance.createFileChooser().getCurrentDirectory());
    }
}