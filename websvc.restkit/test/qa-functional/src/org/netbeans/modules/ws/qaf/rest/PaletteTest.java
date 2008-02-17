/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.ws.qaf.rest;

import java.awt.datatransfer.Transferable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.PaletteOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.junit.NbTestSuite;
import org.openide.cookies.EditorCookie;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 * Test Drag'n'Drop of the resource from the REST palette into the RESTful
 * web service
 *
 * @author lukas
 */
public class PaletteTest extends RestNodeTest {

    private static final String[] PALETTE_ITEMS = {
        "AdSenseForContent", "AdSenseForSearch", "GoogleMap", //NOI18N
        "AddressVerification40", "EmailVerify30", "IPAddressLookup", //NOI18N
        "ReversePhoneLookup", "SalesandUseTaxComplete40", "NewsSearch" //NOI18N
    };

    public PaletteTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test AdSenseForContent service
     */
    public void testAdSenseForContent() {
        Node n = getResourceNode(services[2]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[2].substring(0, 14));
        assertNotNull(services[2] + " not opened?", eo); //NOI18N
        InputProcessor ip = new InputProcessor() {

            public void setInput(NbDialogOperator dialogOperator) {
                dialogOperator.ok();
            }
        };
        dragAndDrop(0, services[2].substring(0, 14), ip);
    }

    /**
     * Test AdSenseForSearch service
     */
    public void testAdSenseForSearch() {
        Node n = getResourceNode(services[2]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[2].substring(0, 14));
        assertNotNull(services[2] + " not opened?", eo); //NOI18N
        InputProcessor ip = new InputProcessor() {

            public void setInput(NbDialogOperator dialogOperator) {
                dialogOperator.ok();
            }
        };
        dragAndDrop(1, services[2].substring(0, 14), ip);
    }

    /**
     * Test Google Map service
     */
    public void testMap() {
        Node n = getResourceNode(services[2]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[2].substring(0, 14));
        assertNotNull(services[2] + " not opened?", eo); //NOI18N
        InputProcessor ip = new InputProcessor() {

            public void setInput(NbDialogOperator dialogOperator) {
                dialogOperator.ok();
            }
        };
        dragAndDrop(2, services[2].substring(0, 14), ip);
    }

    /**
     * Test AddressVerification service
     */
    public void testAddressVerification() {
        Node n = getResourceNode(services[0]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[0]);
        assertNotNull(services[0] + " not opened?", eo); //NOI18N
        InputProcessor ip = new InputProcessor() {

            public void setInput(NbDialogOperator dialogOperator) {
                dialogOperator.ok();
            }
        };
        dragAndDrop(3, services[0], ip);
    }

    /**
     * Test EmailVerify service
     */
    public void testEmailVerify() {
        Node n = getResourceNode(services[0]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[0]);
        assertNotNull(services[0] + " not opened?", eo); //NOI18N
        InputProcessor ip = new InputProcessor() {

            public void setInput(NbDialogOperator dialogOperator) {
                dialogOperator.ok();
            }
        };
        dragAndDrop(4, services[0], ip);
    }

    /**
     * Test IPAddressLookup service
     */
    public void testIPAddressLookup() {
        Node n = getResourceNode(services[0]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[0]);
        assertNotNull(services[0] + " not opened?", eo); //NOI18N
        InputProcessor ip = new InputProcessor() {

            public void setInput(NbDialogOperator dialogOperator) {
                dialogOperator.ok();
            }
        };
        dragAndDrop(5, services[0], ip);
    }

    /**
     * Test ReversePhoneLookup service
     */
    public void testReversePhoneLookup() {
        Node n = getResourceNode(services[0]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[0]);
        assertNotNull(services[0] + " not opened?", eo); //NOI18N
        InputProcessor ip = new InputProcessor() {

            public void setInput(NbDialogOperator dialogOperator) {
                dialogOperator.ok();
            }
        };
        dragAndDrop(6, services[0], ip);
    }

    /**
     * Test SalesandUseTaxComplete service
     */
    public void testSalesandUseTaxComplete() {
        Node n = getResourceNode(services[0]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[0]);
        assertNotNull(services[0] + " not opened?", eo); //NOI18N
        InputProcessor ip = new InputProcessor() {

            public void setInput(NbDialogOperator dialogOperator) {
                dialogOperator.ok();
            }
        };
        dragAndDrop(7, services[0], ip);
    }

    /**
     * Test Yahoo NewsSearch service
     */
    public void testNewsSearch() {
        Node n = getResourceNode(services[2]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[2].substring(0, 14));
        assertNotNull(services[2] + " not opened?", eo); //NOI18N
        InputProcessor ip = new InputProcessor() {

            public void setInput(NbDialogOperator dialogOperator) {
                dialogOperator.ok();
            }
        };
        dragAndDrop(8, services[2].substring(0, 14), ip);
    }

    private void dragAndDrop(int paletteItem, String target, InputProcessor input) {
        PaletteOperator po = PaletteOperator.invoke();
        po.expand(new JCheckBoxOperator(po, 0), true);
        po.expand(new JCheckBoxOperator(po, 1), true);
        po.expand(new JCheckBoxOperator(po, 2), true);
        Object o = null;
        if (paletteItem < 3) {
            //Google
            JListOperator j = new JListOperator(po, 0);
            j.clearSelection();
            j.selectItem(paletteItem);
            o = j.getModel().getElementAt(paletteItem);
        } else if (paletteItem < 8) {
            //StrikeIron
            JListOperator j = new JListOperator(po, 1);
            j.clearSelection();
            j.clickOnItem(PALETTE_ITEMS[paletteItem]);
            o = j.getModel().getElementAt(paletteItem - 3);
        } else {
            //Yahoo
            JListOperator j = new JListOperator(po, 2);
            j.clearSelection();
            j.clickOnItem(PALETTE_ITEMS[paletteItem]);
            o = j.getModel().getElementAt(paletteItem - 8);
        }
        final Transferable src = drag(o);
        drop(paletteItem, src, target, input);
    }

    /**
     * Get Transferable object from an instance of DefaultItem using reflection
     *
     * @param paletteItem instance of DefaultItem
     * @return Transferable for drag operation
     */
    private Transferable drag(Object paletteItem) {
        try {
            Method m = paletteItem.getClass().getDeclaredMethod("drag"); //NOI18N
            Transferable t = (Transferable) m.invoke(paletteItem);
            return t;
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Perform actual drop operation
     *
     * @param paletteItem to get full title of popup dialogs
     * @param drag what to drop
     * @param target name of editor tab (where to drop)
     * @param input input for generation process
     */
    private void drop(final int paletteItem, final Transferable drag,
            final String target, final InputProcessor input) {
        //Need to catch dialogs which are shown during drop operation
        Thread t = new Thread(new Runnable() {

            public void run() {
                //Customize {0} Component
                String dialogLabel = Bundle.getStringTrimmed(
                        "org.netbeans.modules.websvc.rest.component.palette.Bundle",
                        "LBL_CustomizeComponent",
                        new String[]{PALETTE_ITEMS[paletteItem]});
                //first wait for "Customize..." dialog and let the test take
                //care of it (it should at least close it)
                while (true) {
                    JDialog dialog = JDialogOperator.findJDialog(dialogLabel, true, true);
                    if (dialog != null) {
                        input.setInput(new NbDialogOperator(dialog));
                        break;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                //second wait until generation process ends
                //LBL_RestComponentProgress
                dialogLabel = Bundle.getStringTrimmed(
                        "org.netbeans.modules.websvc.rest.component.palette.Bundle",
                        "LBL_RestComponentProgress",
                        new String[]{PALETTE_ITEMS[paletteItem]});
                // wait at most 60 second until generation progress dialog dismiss
                // that's enough for simple services
                long timeout = 60000;
                if (paletteItem > 2 && paletteItem < 8) {
                    // wait at most 240 second for StrikeIrons services
                    // (it runs wsimport which can take some time :(
                    timeout = 240000;
                }
                JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", timeout); //NOI18N
                new NbDialogOperator(dialogLabel).waitClosed();
            }
        }, "Waiter to catch input for " + PALETTE_ITEMS[paletteItem]); //NOI18N
        //start the thread which will catch shown dialogs
        t.start();
        try {
            //do drop and wait for a result
            Thread dropper = new Dropper(target, drag);
            SwingUtilities.invokeAndWait(dropper);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static TestSuite suite() {
        //tests will be enabled as soon as
        //http://www.netbeans.org/issues/show_bug.cgi?id=127557 will be fixed
        TestSuite suite = new NbTestSuite();
//        suite.addTest(new PaletteTest("testAddSenseForContent")); //NOI18N
//        suite.addTest(new PaletteTest("testAddSenseForSearch")); //NOI18N
        suite.addTest(new PaletteTest("testMap")); //NOI18N
        suite.addTest(new PaletteTest("testAddressVerification")); //NOI18N
        suite.addTest(new PaletteTest("testEmailVerify")); //NOI18N
        suite.addTest(new PaletteTest("testIPAddressLookup")); //NOI18N
        suite.addTest(new PaletteTest("testReversePhoneLookup")); //NOI18N
        suite.addTest(new PaletteTest("testSalesandUseTaxComplete")); //NOI18N
//        suite.addTest(new PaletteTest("testNewsSearch")); //NOI18N
        return suite;
    }

    public static void main(String... args) {
        TestRunner.run(suite());
    }

    /**
     * Thread responsible for dropping Transferable into the correct editor tab
     */
    private static class Dropper extends Thread {

        private String target;
        private Transferable drag;

        Dropper(String target, Transferable drag) {
            this.target = target;
            this.drag = drag;
        }

        @Override
        public synchronized void run() {
            Set<TopComponent> comps = TopComponent.getRegistry().getOpened();
            for (TopComponent tc : comps) {
                org.openide.nodes.Node[] arr = tc.getActivatedNodes();
                if (arr != null) {
                    for (int j = 0; j < arr.length; j++) {
                        if (!arr[j].getName().equals(target)) {
                            continue;
                        }
                        EditorCookie ec = arr[j].getCookie(EditorCookie.class);
                        if (ec != null) {
                            JEditorPane[] panes = ec.getOpenedPanes();
                            if (panes != null) {
                                panes[0].getTransferHandler().importData(panes[0], drag);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Each test case using drag and drop operation must implement this
     * interface to be able to provide input for a dialog which allows one
     * customization of a resource to be generated
     *
     * Simplest implementation which accepts default values can be:
     * <pre>
     *    InputProcessor ip = new InputProcessor() {
     *      public void setInput(NbDialogOperator dialogOperator) {
     *          dialogOperator.ok();
     *      }
     *    };
     * </pre>
     */
    private static interface InputProcessor {

        void setInput(NbDialogOperator dialogOperator);
    }
}
