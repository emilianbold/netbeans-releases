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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.performance.scalability;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Pavel Flaška
 */
public class TabSwitchSpeedTest extends NbTestCase {

    private static final long SWITCH_LIMIT = 100; // ms
    private FileObject[] openFiles;
    
    public TabSwitchSpeedTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(TabSwitchSpeedTest.class);
    }

    @Override
    public void setUp() throws Exception {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        
        openFiles = new FileObject[30];
        for (int i = 0; i < openFiles.length; i++) {
            openFiles[i] = FileUtil.createData(root, "empty" + i + ".txt");
        }
    }

    public void testSimpleSwitch() throws InterruptedException, InvocationTargetException, IOException {
        for (int i = 0; i < openFiles.length; i++) {
            DataObject dobj = DataObject.find(openFiles[i]);
            EditorCookie cookie = dobj.getCookie(EditorCookie.class);
            cookie.open();
        }
        Thread.sleep(5000);
        final TopComponent[][] ref = new TopComponent[1][0];
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                TopComponent tc = TopComponent.getRegistry().getActivated();
                Mode mode = WindowManager.getDefault().findMode(tc);
                ref[0] = mode.getTopComponents();
            }
            
        });
        final TopComponent[] openedComponents = ref[0];
        
        for (int i = openedComponents.length - 1; i > 0; i--) {
            final int index = i;
            final long[] time = new long[2];
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    time[0] = System.currentTimeMillis();
                    openedComponents[index].requestActive();
                }
            });
            Thread.sleep(SWITCH_LIMIT);
        }
        long a = System.currentTimeMillis();
        
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
            }
        });
        long b = System.currentTimeMillis();
        fail("Result: " + (b - a));
    }
    
    public void testSwitchAfterModification() throws InterruptedException, InvocationTargetException, IOException {
        for (int i = 0; i < openFiles.length; i++) {
            DataObject dobj = DataObject.find(openFiles[i]);
            EditorCookie cookie = dobj.getCookie(EditorCookie.class);
            cookie.open();
        }
        Thread.sleep(5000);
        final TopComponent[][] ref = new TopComponent[1][0];
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                TopComponent tc = TopComponent.getRegistry().getActivated();
                Mode mode = WindowManager.getDefault().findMode(tc);
                ref[0] = mode.getTopComponents();
            }
            
        });
        final TopComponent[] openedComponents = ref[0];
        
        for (int i = openedComponents.length - 1; i > 0; i--) {
            final int index = i;
            final long[] time = new long[2];
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    try {
                        time[0] = System.currentTimeMillis();
                        openedComponents[index].requestActive();
                        DataObject dob = openedComponents[index].getLookup().lookup(DataObject.class);
                        EditorCookie cookie = dob.getCookie(EditorCookie.class);
                        cookie.getDocument().insertString(0, "Test", null);
                    } catch (BadLocationException ex) {
                        System.err.println(ex);
                    }
                }
            });
            Thread.sleep(SWITCH_LIMIT);
        }
        long a = System.currentTimeMillis();
        
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
            }
        });
        long b = System.currentTimeMillis();
        fail("Result: " + (b - a));
    }
}


