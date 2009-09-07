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

package org.netbeans.test.ide;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.Project;
import org.netbeans.junit.Log;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.view.TreeView;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class WatchProjects {
    private static Logger LOG = Logger.getLogger(WatchProjects.class.getName());
    
    
    private WatchProjects() {
    }
    
    public static void initialize() throws Exception {
        Log.enableInstances(Logger.getLogger("TIMER"), "Project", Level.FINEST);
        
    }
    
    private static void cleanWellKnownStaticFields() throws Exception {
        Object o;
        
//        resetJTreeUIs(Frame.getFrames());

        tryCloseNavigator();

        StringSelection ss = new StringSelection("");
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
//      Toolkit.getDefaultToolkit().getSystemSelection().setContents(ss, ss);
//      fix for Issue 146901
        Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemSelection();
        if (clipBoard != null) {
            clipBoard.setContents(ss, ss);
        }  
        Clipboard cc = Lookup.getDefault().lookup(Clipboard.class);
        Assert.assertNotNull("There is a clipboard in lookup", cc);
        cc.setContents(ss, ss);
/*
        for (Frame f : Frame.getFrames()) {
            f.setVisible(false);
        }
 */
// XXX: uncommented because of the csl.api & related changes
        JFrame empty = new JFrame("Clear");
        empty.getContentPane().setLayout(new FlowLayout());
        empty.getContentPane().add(new JEditorPane());
        empty.pack();
        empty.setVisible(true);
        empty.requestFocusInWindow();
// --------------------------------------------------------

        
        clearField("sun.awt.im.InputContext", "previousInputMethod");
        clearField("sun.awt.im.InputContext", "inputMethodWindowContext");
        clearField("sun.awt.im.CompositionAreaHandler", "compositionAreaOwner");
//        clearField("sun.awt.AppContext", "mainAppContext");
//        clearField("org.netbeans.modules.beans.BeanPanel", "INSTANCE");
        clearField("java.awt.KeyboardFocusManager", "focusedWindow");
        clearField("java.awt.KeyboardFocusManager", "activeWindow");
        clearField("java.awt.KeyboardFocusManager", "focusOwner");
        clearField("java.awt.KeyboardFocusManager", "permanentFocusOwner");
//        clearField("org.netbeans.jemmy.EventTool", "listenerSet");
        clearField("sun.awt.X11.XKeyboardFocusManagerPeer", "currentFocusOwner");
        clearField("sun.awt.X11.XKeyboardFocusManagerPeer", "currentFocusedWindow");
//        clearField("org.netbeans.modules.java.navigation.CaretListeningFactory", "INSATNCE");
//        clearField("org.netbeans.modules.editor.hints.HintsUI", "INSTANCE");
//        clearField("org.netbeans.modules.websvc.core.ProjectWebServiceView", "views");
//        clearField("org.netbeans.api.java.source.support.OpenedEditors", "DEFAULT");
//        clearField("org.netbeans.spi.palette.PaletteSwitch", "theInstance");
//        clearField("org.netbeans.core.NbMainExplorer$MainTab", "lastActivated");
//        clearField("org.netbeans.core.NbMainExplorer$MainTab", "DEFAULT");
/*
        o = getFieldValue("org.netbeans.api.java.source.JavaSource", "toRemove");
        if (o instanceof Collection) {
            Collection c = (Collection) o;
            c.clear();
        }
        o = getFieldValue("org.netbeans.api.java.source.JavaSource", "requests");
        if (o instanceof Collection) {
            Collection c = (Collection) o;
            c.clear();
        }
*/
        clearField("sun.awt.im.InputContext", "previousInputMethod");
        clearField("sun.awt.im.InputContext", "inputMethodWindowContext");
    }
    

    public static void assertTextDocuments() throws Exception {
        for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
            final EditorCookie ec = tc.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                ec.close();
            }
        }
        cleanWellKnownStaticFields();
        System.setProperty("assertgc.paths", "5");
        Log.assertInstances("Are all documents GCed?", "TextDocument");
    }
    
    public static void assertProjects() throws Exception {
        Object o;

        OpenProjects.getDefault().close(
            OpenProjects.getDefault().getOpenProjects()
        );
        Project p = new Project() {
            public FileObject getProjectDirectory() {
                return FileUtil.getConfigRoot();
            }

            public Lookup getLookup() {
                return Lookup.EMPTY;
            }

        };
        OpenProjects.getDefault().open(new Project[] { p }, false);
        OpenProjects.getDefault().setMainProject(p);

        cleanWellKnownStaticFields();
        if (Boolean.getBoolean("ignore.random.failures")) {
            // remove the if we don't care about random failures
            // reported as #
            //removeTreeView(Frame.getFrames());
        }

        System.setProperty("assertgc.paths", "5");
        Log.assertInstances("Checking if all projects are really garbage collected", "Project");
    }
    
    private static void removeTreeView(Component[] arr) throws Exception {
        for (Component c : arr) {
            if (c instanceof TreeView) {
                Set<?> set = (Set<?>) getField(TreeView.class, "visHolder").get(c);
                set.clear();
                continue;
            }
            if (c instanceof Container) {
                Container o = (Container)c;
                removeTreeView(o.getComponents());
            }
        }
    }
    private static void resetJTreeUIs(Component[] arr) {
        for (Component c : arr) {
            if (c instanceof JTree) {
                JTree jt = (JTree)c;
                jt.updateUI();
            }
            if (c instanceof Container) {
                Container o = (Container)c;
                resetJTreeUIs(o.getComponents());
            }
        }
    }

    /** 
     * #124061 workaround - close navigator before tests
     */
    private static void tryCloseNavigator() throws Exception {
        for (TopComponent c : TopComponent.getRegistry().getOpened()) {
            LOG.fine("Processing TC " + c.getDisplayName() + "class " + c.getClass().getName());
            if (c.getClass().getName().equals("org.netbeans.modules.navigator.NavigatorTC")) {
                final TopComponent navigator = c;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        navigator.close();
                    }
                });
                LOG.fine("tryCloseNavigator: Navigator closed, OK!");
                break;
            }
        }
        clearField("org.netbeans.modules.navigator.NavigatorTC", "instance");
        clearField("org.netbeans.modules.navigator.ProviderRegistry","instance");
    }

    private static Object clearField(String clazz, String... name) throws Exception {
        Object ret = null;
        for (int i = 0; i < name.length; i++) {
            Field f;
            try {
                f = i == 0 ? getField(clazz, name[0]) : getField(ret.getClass(), name[i]);
            } catch (NoSuchFieldException ex) {
                LOG.log(Level.WARNING, "Cannot get " + name[i]);
                continue;
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.WARNING, "Cannot class " + clazz);
                continue;
            }
            Object now = ret;
            ret = f.get(now);
            for (int tryHarder = 0;; tryHarder++) {
                f.set(now, null);
                if (f.get(now) == null) {
                    break;
                }
                if (tryHarder == 10) {
                    Assert.fail("Field is really cleared " + f + " but was: " + f.get(now));
                }
                Thread.sleep(100);
            }
            if (ret == null) {
                LOG.info("Getting " + f + " from " + now + " returned null");
                break;
            }
        }
        return ret;
    }
    private static Object getFieldValue(String clazz, String... name) throws Exception {
        Object ret = null;
        for (int i = 0; i < name.length; i++) {
            Field f = i == 0 ? getField(clazz, name[0]) : getField(ret.getClass(), name[i]);
            ret = f.get(ret);
        }
        return ret;
    }
    
    
    private static Field getField(String clazz, String name) throws NoSuchFieldException, ClassNotFoundException {
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        if (l == null) {
            l = WatchProjects.class.getClassLoader();
        }
        Class<?> c = Class.forName(clazz, true, l);
        return getField(c, name);
    }
    private static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        Field f = clazz.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }
    
    
    public static void waitScanFinished() {
        try {
            class Wait implements Runnable {

                boolean initialized;
                boolean ok;

                public void run() {
                    if (initialized) {
                        ok = true;
                        return;
                    }
                    initialized = true;
                    boolean canceled = ScanDialog.runWhenScanFinished(this, "tests");
                    Assert.assertFalse("Dialog really finished", canceled);
                    Assert.assertTrue("Runnable run", ok);
                }
            }
            Wait wait = new Wait();
            SwingUtilities.invokeAndWait(wait);
        } catch (Exception ex) {
            throw (AssertionFailedError)new AssertionFailedError().initCause(ex);
        }
    }
}
