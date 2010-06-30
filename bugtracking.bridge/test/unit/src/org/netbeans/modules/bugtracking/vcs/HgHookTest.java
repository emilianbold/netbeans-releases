/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.vcs;

import java.awt.EventQueue;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.ui.search.QuickSearchComboBar;
import org.netbeans.modules.bugtracking.vcs.VCSHooksConfig.PushOperation;
import org.netbeans.modules.versioning.hooks.HgHook;
import org.netbeans.modules.versioning.hooks.HgHookContext;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka
 */
public class HgHookTest extends NbTestCase {

    public HgHookTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath() + "/userdir");
    }

    public void testPanel() throws MalformedURLException, CoreException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance().setHgLink(true);
        VCSHooksConfig.getInstance().setHgResolve(true);
        VCSHooksConfig.getInstance().setHgAfterCommit(true);

        HookPanel panel = getPanel(hook, getContext());
        assertTrue(panel.pushRadioButton.isVisible());
        assertTrue(panel.pushRadioButton.isVisible());

        assertTrue(panel.linkCheckBox.isSelected());
        assertTrue(panel.resolveCheckBox.isSelected());
        assertTrue(panel.commitRadioButton.isSelected());
        assertFalse(panel.pushRadioButton.isSelected());

        VCSHooksConfig.getInstance().setHgLink(false);
        VCSHooksConfig.getInstance().setHgResolve(false);
        VCSHooksConfig.getInstance().setHgAfterCommit(false);

        panel = getPanel(hook, getContext());

        assertFalse(panel.linkCheckBox.isSelected());
        assertFalse(panel.resolveCheckBox.isSelected());
        assertFalse(panel.commitRadioButton.isSelected());
        assertTrue(panel.pushRadioButton.isSelected());
    }

    public void testBeforeCommitNoLink() throws MalformedURLException, CoreException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance().setHgLink(false);

        String msg = "msg";
        HgHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        ctx = hook.beforeCommit(ctx);
        assertNull(ctx);
    }

    public void testBeforeCommitWithLink() throws MalformedURLException, CoreException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance().setHgLink(true);

        String msg = "msg";
        HgHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        ctx = hook.beforeCommit(ctx);
        assertNotNull(ctx);
        assertNotNull(ctx.getMessage());
        assertNotSame("", ctx.getMessage());
        assertNotSame(msg, ctx.getMessage()); // issue info was added
    }

    public void testAfterCommitLink() throws MalformedURLException, CoreException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance().setHgAfterCommit(true);
        VCSHooksConfig.getInstance().setHgLink(true);
        VCSHooksConfig.getInstance().setHgResolve(false);

        String msg = "msg";
        HgHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        hook.afterCommit(ctx);
        assertNotNull(HookIssue.getInstance().comment);
        assertNotSame(-1, HookIssue.getInstance().comment.indexOf(msg));
        assertFalse(HookIssue.getInstance().closed);
    }

    public void testAfterCommitResolve() throws MalformedURLException, CoreException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance().setHgAfterCommit(true);
        VCSHooksConfig.getInstance().setHgLink(false);
        VCSHooksConfig.getInstance().setHgResolve(true);

        String msg = "msg";
        HgHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        hook.afterCommit(ctx);
        assertNull(HookIssue.getInstance().comment);
        assertTrue(HookIssue.getInstance().closed);
    }

    public void testAfterCommitLinkResolve() throws MalformedURLException, CoreException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance().setHgAfterCommit(true);
        VCSHooksConfig.getInstance().setHgLink(true);
        VCSHooksConfig.getInstance().setHgResolve(true);

        String msg = "msg";
        HgHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        hook.afterCommit(ctx);
        assertNotNull(HookIssue.getInstance().comment);
        assertNotSame(-1, HookIssue.getInstance().comment.indexOf(msg));
        assertTrue(HookIssue.getInstance().closed);
    }

    public void testAfterCommitLinkResolveAfterPush() throws MalformedURLException, CoreException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance().setHgAfterCommit(false); // PUSH!
        VCSHooksConfig.getInstance().setHgLink(true);
        VCSHooksConfig.getInstance().setHgResolve(true);

        String changeset = "#" + System.currentTimeMillis();
        String msg = "msg";
        HgHookContext ctx = getContext(msg, changeset);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        hook.afterCommit(ctx);
        assertNull(HookIssue.getInstance().comment);
        assertFalse(HookIssue.getInstance().closed);

        PushOperation a = VCSHooksConfig.getInstance().popHGPushAction(changeset);
        assertNotNull(a);
        assertNotNull(a.getIssueID());
        assertEquals(HookIssue.getInstance().getID(), a.getIssueID());
        assertNotNull(a.getMsg());
        assertNotSame("", a.getMsg());
        assertNotSame(-1, msg);
    }

    private HgHookImpl getHook() {
        Collection<HgHook> hooks = VCSHooks.getInstance().getHooks(HgHook.class);
        for (HgHook hgHook : hooks) {
            if(hgHook instanceof HgHookImpl) {
                assertNotNull(hgHook);
                assertNotNull(hgHook.getDisplayName());
                return (HgHookImpl) hgHook;
            }
        }
        return null;
    }

    public HgHookContext getContext() throws IOException {
        return getContext("msg");
    }

    public HgHookContext getContext(String msg) throws IOException {
        return getContext(msg, "1");
    }

    private HgHookContext getContext(String msg, String changeset) throws IOException {
        return new HgHookContext(new File[]{new File(getWorkDir(), "f")}, "msg", new HgHookContext.LogEntry("msg", "author", changeset, new Date(System.currentTimeMillis())));
    }

    private void setRepository(HookPanel panel) {
        HookRepository repo = new HookRepository();
        DefaultComboBoxModel model = new DefaultComboBoxModel(new Repository[] {repo});
        panel.repositoryComboBox.setModel(model);
        panel.repositoryComboBox.setSelectedItem(repo);
    }

    private void setIssue(HookPanel panel) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field f = panel.getClass().getDeclaredField("qs");
        f.setAccessible(true);
        QuickSearchComboBar qs = (QuickSearchComboBar) f.get(panel);
        Method m = qs.getClass().getDeclaredMethod("setIssue", Issue.class);
        m.setAccessible(true);
        HookIssue.getInstance().reset();
        m.invoke(qs, HookIssue.getInstance());
    }

    private HookPanel getPanel(final HgHookImpl hook, final HgHookContext ctx) throws InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        final HookPanel[] p = new HookPanel[] {null};
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                p[0] = (HookPanel)hook.createComponent(ctx);
            }
        });
        assertNotNull(p[0]);
        assertTrue(p[0] instanceof HookPanel);

        preparePanel(p[0]);

        return p[0];
    }

    private void preparePanel(HookPanel panel) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException {
        setRepository(panel);
        setIssue(panel);
        panel.enableFields(); // emulate event
    }

    private static class HookIssue extends Issue {
        static HookIssue instance;

        private boolean closed;
        private String comment;

        private HookIssue() {
            super(null);
        }

        static HookIssue getInstance() {
            if(instance == null) {
                instance = new HookIssue();
            }
            return instance;
        }

        void reset() {
            comment = null;
            closed = false;
        }
        @Override
        public String getDisplayName() {
            return "HookIssue";
        }

        @Override
        public String getTooltip() {
            return "HookIssue";
        }

        @Override
        public String getID() {
            return "1";
        }

        @Override
        public String getSummary() {
            return "HookIssue";
        }

        @Override
        public boolean isNew() {
            return false;
        }

        @Override
        public boolean refresh() {
            return true;
        }

        @Override
        public void addComment(String comment, boolean closeAsFixed) {
            this.comment = comment;
            closed = closeAsFixed;
        }

        @Override
        public void attachPatch(File file, String description) {
            // do nothing
        }

        @Override
        public BugtrackingController getController() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private class HookRepository extends Repository {

        @Override
        public Image getIcon() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return "HookRepository";
        }

        @Override
        public String getTooltip() {
            return "HookRepository";
        }

        @Override
        public String getID() {
            return "HookRepository";
        }

        @Override
        public String getUrl() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Issue getIssue(String id) {
            return HookIssue.instance;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public BugtrackingController getController() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Query createQuery() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Issue createIssue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Query[] getQueries() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<RepositoryUser> getUsers() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Issue[] simpleSearch(String criteria) {
            return new Issue[] {HookIssue.instance};
        }

        @Override
        public Lookup getLookup() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
