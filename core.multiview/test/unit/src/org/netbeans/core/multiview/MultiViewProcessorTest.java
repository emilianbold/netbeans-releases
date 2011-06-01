/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.core.multiview;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.test.AnnotationProcessorTestUtils;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MultiViewProcessorTest extends NbTestCase {
    
    public MultiViewProcessorTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        MVE.closeState = null;
        CloseH.globalElements = null;
        CloseH.retValue = null;
        DD.d = null;
        DD.ret = -1;
        MockServices.setServices(DD.class);
    }

    public void testMultiViewsCreate() {
        TopComponent mvc = MultiViews.createMultiView("text/figaro", new LP(Lookup.EMPTY));
        assertNotNull("MultiViewComponent created", mvc);
        mvc.open();
        mvc.requestActive();
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(mvc);
        assertNotNull("Handler found", handler);
        MultiViewPerspective[] arr = handler.getPerspectives();
        assertEquals("One perspetive found", 1, arr.length);
        assertEquals("Figaro", arr[0].getDisplayName());

        CloseH.retValue = true;
        MVE.closeState = MultiViewFactory.createUnsafeCloseState("warn", null, null);
        assertTrue("Closed OK", mvc.close());
        assertNotNull(CloseH.globalElements);
        assertEquals("One handle", 1, CloseH.globalElements.length);
        assertEquals("states are the same", MVE.closeState, CloseH.globalElements[0]);
    }

    public void testCloneableMultiViewsCreate() {
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        
        CloneableTopComponent cmv = MultiViews.createCloneableMultiView("text/context", new LP(lookup));
        assertNotNull("MultiViewComponent created", cmv);
        TopComponent mvc = cmv.cloneTopComponent();
        doCheck(mvc, ic);
        
        CntAction accept = new CntAction();
        CntAction discard = new CntAction();
        CloseH.retValue = false;
        MVE.closeState = MultiViewFactory.createUnsafeCloseState("warn", accept, discard);
        DD.ret = 2;
        mvc.open();
        assertFalse("Closed cancelled", mvc.close());
        assertEquals("No accept", 0, accept.cnt);
        assertEquals("No discard", 0, discard.cnt);
        MVE.closeState = MultiViewFactory.createUnsafeCloseState("warn", accept, discard);
        DD.ret = 1;
        DD.d = null;
        mvc.open();
        assertTrue("Changes discarded, close accepted", mvc.close());
        assertEquals("Still no accept", 0, accept.cnt);
        assertEquals("One discard", 1, discard.cnt);
        MVE.closeState = MultiViewFactory.createUnsafeCloseState("warn", accept, discard);
        DD.ret = 0;
        DD.d = null;
        mvc.open();
        assertTrue("Closed accepted OK", mvc.close());
        assertEquals("Three buttons", 3, DD.d.getOptions().length);
        assertNull("Not called, we use default handler", CloseH.globalElements);
    }

    public void testCloneableMultiViewsSerialize() throws Exception {
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        
        CloneableTopComponent cmv = MultiViews.createCloneableMultiView("text/context", new LP(lookup));
        assertPersistence("Always", TopComponent.PERSISTENCE_ALWAYS, cmv);
        assertNotNull("MultiViewComponent created", cmv);
        NbMarshalledObject mar = new NbMarshalledObject(cmv);
        TopComponent mvc = (TopComponent) mar.get();
        doCheck(mvc, ic);
    }

    private void assertPersistence(String msg, int pt, TopComponent cmv) {
        CharSequence log = Log.enable("org.netbeans.core.multiview", Level.WARNING);
        int res = cmv.getPersistenceType();
        if (log.length() > 0) {
            fail("There should be no warnings to compute getPersistenceType():\n" + log);
        }
        assertEquals(msg, pt, res);
    }
    
    private void doCheck(TopComponent mvc, InstanceContent ic) {
        assertNotNull("MultiViewComponent cloned", mvc);
        MultiViewHandler handler = MultiViews.findMultiViewHandler(mvc);
        assertNotNull("Handler found", handler);
        MultiViewPerspective[] arr = handler.getPerspectives();
        assertEquals("One perspetive found", 1, arr.length);
        assertEquals("Contextual", arr[0].getDisplayName());

        assertPersistence("Always", TopComponent.PERSISTENCE_ALWAYS, mvc);
        
        mvc.open();
        mvc.requestActive();
        mvc.requestVisible();
        
        handler.requestActive(arr[0]);
        assertNull("No integer now", mvc.getLookup().lookup(Integer.class));
        ic.add(1);
        assertEquals("1 now", Integer.valueOf(1), mvc.getLookup().lookup(Integer.class));
    }
    
    public void testNotSourceView() {
        int cnt = 0;
        for (MultiViewDescription d : MimeLookup.getLookup("text/context").lookupAll(MultiViewDescription.class)) {
            cnt++;
            assertFalse(
                "No view in text/context has source element",
                MultiViewCloneableTopComponent.isSourceView(d)
            );
        }
        if (cnt == 0) {
            fail("There shall be at least one description");
        }
    }
    
    public void testCompileInApt() throws Exception {
        clearWorkDir();
        String src = "\n"
                + "import org.netbeans.core.spi.multiview.MultiViewElement;\n"
                + "public class Test extends org.netbeans.core.multiview.MultiViewProcessorTest.MVE {\n"
        + "@MultiViewElement.Registration(displayName = \"Testing\","
        + "iconBase = \"none\","
        + "mimeType = \"text/ble\","
        + "persistenceType = 0,"
        + "preferredID = \"bleple\")"
                + "  public static MultiViewElement create() {\n"
                + "    return new Test();\n"
                + "  }\n"
                + "}\n";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "pkg.Test", src);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertTrue("Compilation should succeed:\n" + os.toString(), res);
    }
    
    public void testIsSourceView() {
        int cnt = 0;
        for (MultiViewDescription d : MimeLookup.getLookup("text/plain").lookupAll(MultiViewDescription.class)) {
            cnt++;
            assertTrue(
                "All views in text/plain have source element: " + d,
                MultiViewCloneableTopComponent.isSourceView(d)
            );
        }
        if (cnt == 0) {
            fail("There shall be at least one description");
        }
    }

    public void testMultiViewsContextCreate() {
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        
        TopComponent mvc = MultiViews.createMultiView("text/context", new LP(lookup));
        assertNotNull("MultiViewComponent created", mvc);
        MultiViewHandler handler = MultiViews.findMultiViewHandler(mvc);
        assertNotNull("Handler found", handler);
        MultiViewPerspective[] arr = handler.getPerspectives();
        assertEquals("One perspetive found", 1, arr.length);
        assertEquals("Contextual", arr[0].getDisplayName());
        
        mvc.open();
        mvc.requestActive();
        mvc.requestVisible();
        
        handler.requestActive(arr[0]);
        assertNull("No integer now", mvc.getLookup().lookup(Integer.class));
        ic.add(1);
        assertEquals("1 now", Integer.valueOf(1), mvc.getLookup().lookup(Integer.class));
    }

    @MimeRegistration(mimeType="text/figaro", service=CloseOperationHandler.class)
    public static class CloseH implements CloseOperationHandler {
        static CloseOperationState[] globalElements;
        static Boolean retValue;
        @Override
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            assertNull("globalElement not specified yet", globalElements);
            assertNotNull("We know what to return", retValue);
            boolean r = retValue;
            retValue = null;
            globalElements = elements;
            return r;
        }
    }

    @MultiViewElement.Registration(
        displayName="org.netbeans.core.multiview.TestBundle#FIGARO",
        iconBase="none",
        mimeType="text/figaro",
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID="figaro"
    )
    public static class MVE extends JPanel implements MultiViewElement {
        static CloseOperationState closeState;
        
        private JPanel toolbar = new JPanel();
        
        public MVE() {
        }
        
        @Override
        public JComponent getVisualRepresentation() {
            return this;
        }

        @Override
        public JComponent getToolbarRepresentation() {
            return toolbar;
        }

        @Override
        public Action[] getActions() {
            return new Action[0];
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public void componentOpened() {
        }

        @Override
        public void componentClosed() {
        }

        @Override
        public void componentShowing() {
        }

        @Override
        public void componentHidden() {
        }

        @Override
        public void componentActivated() {
        }

        @Override
        public void componentDeactivated() {
        }

        @Override
        public UndoRedo getUndoRedo() {
            return UndoRedo.NONE;
        }

        @Override
        public void setMultiViewCallback(MultiViewElementCallback callback) {
        }

        @Override
        public CloseOperationState canCloseElement() {
            if (closeState != null) {
                return closeState;
            }
            return CloseOperationState.STATE_OK;
        }
    } // end of MVE
    
    @MultiViewElement.Registration(
        displayName="Contextual",
        iconBase="none",
        mimeType="text/context",
        persistenceType=TopComponent.PERSISTENCE_ALWAYS,
        preferredID="context"
    )
    public static CntxMVE create(Lookup lkp) {
        return new CntxMVE(lkp);
    }
    static class CntxMVE extends MVE {
        private Lookup context;
        public CntxMVE(Lookup context) {
            this.context = context;
        }
        public CntxMVE() {
        }

        @Override
        public Lookup getLookup() {
            return context;
        }
    } // end of CntxMVE

    @MultiViewElement.Registration(
        displayName="Source",
        iconBase="none",
        mimeType="text/plain",
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID="source"
    )
    public static class SourceMVC extends MVE implements CloneableEditorSupport.Pane {
        @Override
        public JEditorPane getEditorPane() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CloneableTopComponent getComponent() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void ensureVisible() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    private static class LP implements Lookup.Provider, Serializable {
        private static final Map<Integer,Lookup> map = new HashMap<Integer, Lookup>();
        
        private final int cnt;
        public LP(Lookup lkp) {
            synchronized (map) {
                cnt = map.size() + 1;
                map.put(cnt, lkp);
            }
        }
        
        @Override
        public Lookup getLookup() {
            return map.get(cnt);
        }
    }
    
    public static final class DD extends DialogDisplayer {
        static int ret;
        static NotifyDescriptor d;
        
        @Override
        public Object notify(NotifyDescriptor descriptor) {
            assertNull("No descriptor yet", d);
            if (ret == -1) {
                fail("We should know what to return");
            }
            d = descriptor;
            if (d.getOptions().length <= ret) {
                fail("not enough options. Need index " + ret + " but is just " + Arrays.toString(d.getOptions()));
            }
            Object obj = d.getOptions()[ret];
            ret = -1;
            return obj;
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    private static final class CntAction extends AbstractAction {
        int cnt;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            cnt++;
        }
    }
}
