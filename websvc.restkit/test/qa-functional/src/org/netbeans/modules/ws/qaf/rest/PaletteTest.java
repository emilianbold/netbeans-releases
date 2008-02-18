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
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
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
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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

    protected String getRestPackage() {
        return "o.n.m.qa.test"; //NOI18N
    }

    /**
     * Test AdSenseForContent service
     */
    public void testAdSenseForContent() {
        Node n = getMethodsNode(services[2]);
        n.tree().clickOnPath(getMethodNode(n, "getJson").getTreePath(), 2); //NOI18N
        EditorOperator eo = new EditorOperator(services[2].substring(0, 14));
        assertNotNull(services[2] + " not opened?", eo); //NOI18N
        String resourcePath = "myAdSenseForContent/"; //NOI18N
        String resource = "AdSenseForContentResource"; //NOI18N
        InputProcessor ip = new DefaultInputProcessor(resourcePath, null);
        dragAndDrop(0, services[2].substring(0, 14), ip);
        String getter = "get" + Character.toUpperCase(resourcePath.charAt(0))  //NOI18N
                + resourcePath.substring(1, resourcePath.length() - 1);
        checkResource(eo, resourcePath, getter, resource);
    }

    /**
     * Test AdSenseForSearch service
     */
    public void testAdSenseForSearch() {
        Node n = getMethodsNode(services[2]);
        n.tree().clickOnPath(getMethodNode(n, "getJson").getTreePath(), 2); //MOI18N
        EditorOperator eo = new EditorOperator(services[2].substring(0, 14));
        assertNotNull(services[2] + " not opened?", eo); //NOI18N
        String resourcePath = "myAdSenseForSearch/"; //NOI18N
        String resource = "AdSenseForSearchResource"; //NOI18N
        InputProcessor ip = new DefaultInputProcessor(resourcePath, null);
        dragAndDrop(1, services[2].substring(0, 14), ip);
        String getter = "get" + Character.toUpperCase(resourcePath.charAt(0))  //NOI18N
                + resourcePath.substring(1, resourcePath.length() - 1);
        checkResource(eo, resourcePath, getter, resource);
    }

    /**
     * Test Google Map service
     */
    public void testMap() {
        Node n = getMethodsNode(services[2]);
        n.tree().clickOnPath(getMethodNode(n, "getJson").getTreePath(), 2); //MOI18N
        EditorOperator eo = new EditorOperator(services[2].substring(0, 14));
        assertNotNull(services[2] + " not opened?", eo); //NOI18N
        String resourcePath = "myGoogleMap/"; //NOI18N
        String resource = "GoogleMapResource"; //NOI18N
        InputProcessor ip = new DefaultInputProcessor(resourcePath, null);
        dragAndDrop(2, services[2].substring(0, 14), ip);
        String getter = "get" + Character.toUpperCase(resourcePath.charAt(0))  //NOI18N
                + resourcePath.substring(1, resourcePath.length() - 1);
        checkResource(eo, resourcePath, getter, resource);
    }

    public void testGoogleServices() {
        //TODO: build and deploy project
        //TODO: run some GET to verify running services
        //TODO: undeploy project
        //clean up original project - remove added code && created files
        Set<String> toDelete = new HashSet<String>();
        toDelete.add("AdSenseForContentResource"); //NOI18N
        toDelete.add("AdSenseForSearchResource"); //NOI18N
        toDelete.add("GenericRefConverter"); //NOI18N
        toDelete.add("GoogleMapResource"); //NOI18N
        toDelete.add("RestConnection"); //NOI18N
        cleanResource(services[2].substring(0, 14), "@PUT", toDelete); //NOI18N
    }

    /**
     * Test AddressVerification service
     */
    public void testAddressVerification() {
        Node n = getResourceNode(services[0]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[0]);
        assertNotNull(services[0] + " not opened?", eo); //NOI18N
        String resourcePath = "myUSAddressVerification/"; //NOI18N
        String resource = "USAddressVerificationResource"; //NOI18N
        InputProcessor ip = new DefaultInputProcessor(resourcePath, null);
        dragAndDrop(3, services[0], ip);
        String getter = "get" + Character.toUpperCase(resourcePath.charAt(0))  //NOI18N
                + resourcePath.substring(1, resourcePath.length() - 1);
        checkResource(eo, resourcePath, getter, resource);
    }

    /**
     * Test EmailVerify service
     */
    public void testEmailVerify() {
        Node n = getResourceNode(services[0]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[0]);
        assertNotNull(services[0] + " not opened?", eo); //NOI18N
        String resourcePath = "myEmailVerification/"; //NOI18N
        String resource = "EmailVerificationResource"; //NOI18N
        InputProcessor ip = new DefaultInputProcessor(resourcePath, null);
        dragAndDrop(4, services[0], ip);
        String getter = "get" + Character.toUpperCase(resourcePath.charAt(0)) //NOI18N
                + resourcePath.substring(1, resourcePath.length() - 1);
        checkResource(eo, resourcePath, getter, resource);
    }

    /**
     * Test IPAddressLookup service
     */
    public void testIPAddressLookup() {
        Node n = getResourceNode(services[0]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[0]);
        assertNotNull(services[0] + " not opened?", eo); //NOI18N
        String resourcePath = "myDNS/"; //NOI18N
        String resource = "DNSResource"; //NOI18N
        InputProcessor ip = new DefaultInputProcessor(resourcePath, null);
        dragAndDrop(5, services[0], ip);
        String getter = "get" + Character.toUpperCase(resourcePath.charAt(0)) //NOI18N
                + resourcePath.substring(1, resourcePath.length() - 1);
        checkResource(eo, resourcePath, getter, resource);
    }

    /**
     * Test ReversePhoneLookup service
     */
    public void testReversePhoneLookup() {
        Node n = getResourceNode(services[0]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[0]);
        assertNotNull(services[0] + " not opened?", eo); //NOI18N
        String resourcePath = "myStandardReverseLookup/"; //NOI18N
        String resource = "StandardReverseLookupResource"; //NOI18N
        InputProcessor ip = new DefaultInputProcessor(resourcePath, null);
        dragAndDrop(6, services[0], ip);
        String getter = "get" + Character.toUpperCase(resourcePath.charAt(0)) //NOI18N
                + resourcePath.substring(1, resourcePath.length() - 1);
        checkResource(eo, resourcePath, getter, resource);
    }

    /**
     * Test SalesandUseTaxComplete service
     */
    public void testSalesandUseTaxComplete() {
        Node n = getResourceNode(services[0]);
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo = new EditorOperator(services[0]);
        assertNotNull(services[0] + " not opened?", eo); //NOI18N
        String resourcePath = "myTaxDataComplete/"; //NOI18N
        String resource = "TaxDataCompleteResource"; //NOI18N
        InputProcessor ip = new DefaultInputProcessor(resourcePath, null);
        dragAndDrop(7, services[0], ip);
        String getter = "get" + Character.toUpperCase(resourcePath.charAt(0)) //NOI18N
                + resourcePath.substring(1, resourcePath.length() - 1);
        checkResource(eo, resourcePath, getter, resource);
    }

    public void testStrikeIronServices() {
        //TODO: build and deploy project
        //TODO: run some GET to verify running services
        //TODO: undeploy project
        //clean up original project - remove added code && created files
        Set<String> toDelete = new HashSet<String>();
        toDelete.add("DNSConverter"); //NOI18N
        toDelete.add("DNSResource"); //NOI18N
        toDelete.add("EmailVerificationConverter"); //NOI18N
        toDelete.add("EmailVerificationResource"); //NOI18N
        toDelete.add("GenericRefConverter"); //NOI18N
        toDelete.add("StandardReverseLookupConverter"); //NOI18N
        toDelete.add("StandardReverseLookupResource"); //NOI18N
        toDelete.add("TaxDataCompleteConverter"); //NOI18N
        toDelete.add("TaxDataCompleteResource"); //NOI18N
        toDelete.add("USAddressVerificationConverter"); //NOI18N
        toDelete.add("USAddressVerificationResource"); //NOI18N
        cleanResource(services[0], "@DELETE", toDelete); //NOI18N
    }

    /**
     * Test Yahoo NewsSearch service
     */
    public void testNewsSearch() {
        Node n = getSubresourcesNode(services[1]);
        n.tree().clickOnPath(getSubresourceNode(n, "{name} : ItemResource").getTreePath(), 2); //NOI18N
        EditorOperator eo = new EditorOperator(services[1].substring(0, 13));
        assertNotNull(services[1] + " not opened?", eo); //NOI18N
        //TBD ???
        String resourcePath = "myNewsSearch/"; //NOI18N
        String resource = "NewsSearchResource"; //NOI18N
        InputProcessor ip = new DefaultInputProcessor(resourcePath, null);
        dragAndDrop(8, services[1].substring(0, 13), ip);
        String getter = "get" + Character.toUpperCase(resourcePath.charAt(0)) //NOI18N
                + resourcePath.substring(1, resourcePath.length() - 1);
        checkResource(eo, resourcePath, getter, resource);
    }

    public void testYahooServices() {
        //TODO: build and deploy project
        //TODO: run some GET to verify running services
        //TODO: undeploy project
        //clean up original project - remove added code && created files
        Set<String> toDelete = new HashSet<String>();
        //TBD ???
        toDelete.add("Converter"); //NOI18N
        toDelete.add("Resource"); //NOI18N
        cleanResource(services[0], "@Path(\"{name}\")", toDelete); //NOI18N
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
        InputCatcher ic = new InputCatcher(paletteItem, input);
        //start the thread which will catch shown dialogs
        ic.start();
        try {
            //do drop and wait for a result
            Thread dropper = new Dropper(target, drag, ic);
            SwingUtilities.invokeAndWait(dropper);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void checkResource(EditorOperator eo, String resourcePath,
            String methodName, String newResourceName) {
        // let's wait for a while here first...
        ProjectSupport.waitScanFinished();
        new EventTool().waitNoEvent(1000);
        //check new method in the resource
        assertTrue("missing @Path?", eo.contains("\"" + resourcePath + "\"")); //NOI18N
        assertTrue("missing method declaration?", eo.contains("public " + newResourceName + " " + methodName + "() {")); //NOI18N
        //check new resource class
        assertTrue("source for [" + newResourceName + "] missing?", getFileFromProject(newResourceName).exists()); //NOI18N
        //check nodes in the UI
        String name = eo.getName();
        Node n = getSubresourcesNode(name.substring(0, name.length() - 5));
        if (n.isCollapsed()) {
            n.expand();
        } else {
            n.collapse();
            n.expand();
        }
        long timeout = 5000 + System.currentTimeMillis();
        while (!n.isChildPresent(newResourceName) && System.currentTimeMillis() < timeout) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        assertTrue("locator node for [" + newResourceName + "] missing?", n.isChildPresent(newResourceName));
        n = getResourceNode(newResourceName); //NOI18N
        if (n.isCollapsed()) {
            n.expand();
        }
        n.tree().clickOnPath(n.getTreePath(), 2);
        EditorOperator eo2 = new EditorOperator(newResourceName); //NOI18N
        assertNotNull(newResourceName + " not opened?", eo2); //NOI18N
        eo2.close(false);
    }

    private void cleanResource(String resourceName, String st, Set<String> toDelete) {
        //clean the resource class (remove added code)
        EditorOperator eo = new EditorOperator(resourceName);
        assertNotNull(resourceName + " not opened?", eo); //NOI18N
        eo.select(st);
        //clean added methods
        int begin = eo.getLineNumber() + 4;
        eo.pushKey(KeyEvent.VK_PAGE_DOWN);
        eo.pushKey(KeyEvent.VK_PAGE_DOWN);
        eo.pushKey(KeyEvent.VK_PAGE_DOWN);
        eo.select(begin, eo.getLineNumber() - 2);
        eo.pushKey(KeyEvent.VK_DELETE);
        //clean added imports
        if (eo.contains("import java.util.List")) { //NOI18N
            eo.select("import java.util.List"); //NOI18N
            eo.deleteLine(eo.getLineNumber());
        }
        while (eo.contains("import com.")) { //NOI18N
            eo.select("import com."); //NOI18N
            eo.deleteLine(eo.getLineNumber());
        }
        eo.close(true);
        //delete created files
        SourcePackagesNode spn = new SourcePackagesNode(getProjectRootNode());
        Node pn = new Node(spn, getRestPackage());
        pn.expand();
        Set<String> deleted = new HashSet<String>();
        //Safe Delete
        String sdTitle = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_SafeDel");
        //Delete
        String delLabel = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_SafeDel_Delete");
        for (String s : toDelete) {
            if (getFileFromProject(s).exists()) {
                new Node(pn, s).performPopupAction(delLabel);
                new NbDialogOperator(sdTitle).ok();
                deleted.add(s);
            }
        }
        assertEquals("should have only 3 nodes", 3, getRestNode().getChildren().length);
        toDelete.removeAll(deleted);
        assertTrue(toDelete + " we're not created (nor deleted)", toDelete.isEmpty()); //NOI18N
    }

    private File getFileFromProject(String fileName) {
        FileObject fo = getProject().getProjectDirectory().getFileObject("src/java"); //NOI18N
        File f = FileUtil.toFile(fo);
        return new File(f, getRestPackage().replace('.', File.separatorChar) + File.separatorChar + fileName + ".java"); //NOI18N
    }

    public static TestSuite suite() {
        TestSuite suite = new NbTestSuite();
        //see http://www.netbeans.org/issues/show_bug.cgi?id=127557
//        suite.addTest(new PaletteTest("testAdSenseForContent")); //NOI18N
//        suite.addTest(new PaletteTest("testAdSenseForSearch")); //NOI18N
        suite.addTest(new PaletteTest("testMap")); //NOI18N
        suite.addTest(new PaletteTest("testGoogleServices")); //NOI18N
        suite.addTest(new PaletteTest("testAddressVerification")); //NOI18N
        suite.addTest(new PaletteTest("testEmailVerify")); //NOI18N
        suite.addTest(new PaletteTest("testIPAddressLookup")); //NOI18N
        suite.addTest(new PaletteTest("testReversePhoneLookup")); //NOI18N
//        suite.addTest(new PaletteTest("testSalesandUseTaxComplete")); //NOI18N
        suite.addTest(new PaletteTest("testStrikeIronServices")); //NOI18N
//        suite.addTest(new PaletteTest("testNewsSearch")); //NOI18N
//        suite.addTest(new PaletteTest("testYahooServices")); //NOI18N
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
        private InputCatcher ic;

        Dropper(String target, Transferable drag, InputCatcher ic) {
            this.target = target;
            this.drag = drag;
            this.ic = ic;
        }

        @Override
        public void run() {
            while (!ic.isDone()) {
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
    }

    /**
     * Thread responsible for catching shown dialogs and submitting user's input
     */
    private static class InputCatcher extends Thread {

        private int paletteItem;
        private InputProcessor input;
        private boolean done;
        private long time;
        private static final long TIMEOUT = 30000;

        InputCatcher(int paletteItem, InputProcessor input) {
            super("Waiter to catch input for " + PALETTE_ITEMS[paletteItem]); //NOI18N
            this.paletteItem = paletteItem;
            this.input = input;
            done = false;
            time = System.currentTimeMillis();
        }

        @Override
        public void run() {
            //Web Site Certified by Unknown Authority
            String certDialogLabel = Bundle.getStringTrimmed(
                    "org.netbeans.modules.xml.retriever.impl.Bundle",
                    "TTL_CertifiedWebSite");
            //Customize {0} Component
            String dialogLabel = Bundle.getStringTrimmed(
                    "org.netbeans.modules.websvc.rest.component.palette.Bundle",
                    "LBL_CustomizeComponent",
                    new String[]{PALETTE_ITEMS[paletteItem]});
            //first accept certificate for HTTPS connection (if it appears)
            //then wait for "Customize..." dialog and let the test take
            //care of it (it should at least close it)
            while (!isDone()) {
                JDialog dialog = JDialogOperator.findJDialog(certDialogLabel, true, true);
                if (dialog != null) {
                    new NbDialogOperator(dialog).yes();
                    time = System.currentTimeMillis();
                    continue;
                }
                dialog = JDialogOperator.findJDialog(dialogLabel, true, true);
                if (dialog != null) {
                    input.setInput(new NbDialogOperator(dialog));
                    synchronized (this) {
                        done = true;
                    }
                    break;
                }
                try {
                    Thread.sleep(100);
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
            JDialog dialog = JDialogOperator.findJDialog(dialogLabel, true, true);
            if (dialog != null) {
                new NbDialogOperator(dialog).waitClosed();
            }
        }

        public synchronized boolean isDone() {
            return done || time + TIMEOUT < System.currentTimeMillis();
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

    private static class DefaultInputProcessor implements InputProcessor {

        private String path;
        private String methodName;

        DefaultInputProcessor(String path, String methodName) {
            this.path = path;
            this.methodName = methodName;
        }

        public void setInput(NbDialogOperator dialogOperator) {
            JTextFieldOperator jtfo = null;
            if (path != null) {
                jtfo = new JTextFieldOperator(dialogOperator, 0);
                jtfo.clearText();
                jtfo.typeText(path);
            }
            if (methodName != null) {
                jtfo = new JTextFieldOperator(dialogOperator, 1);
                jtfo.clearText();
                jtfo.typeText(methodName);
            }
            dialogOperator.ok();
        }
    }
}
