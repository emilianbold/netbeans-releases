/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.codemodel.bridge.trace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JMenuItem;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.codemodel.CMDiagnostic;
import org.netbeans.modules.cnd.api.codemodel.CMFile;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.api.codemodel.CMSourceLocation;
import org.netbeans.modules.cnd.api.codemodel.query.CMQuery;
import org.netbeans.modules.cnd.api.codemodel.query.CMUtilities;
import org.netbeans.modules.cnd.api.codemodel.visit.CMDeclaration;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntityReference;
import org.netbeans.modules.cnd.api.codemodel.visit.CMInclude;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitLocation;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery;
import org.netbeans.modules.cnd.spi.codemodel.trace.CMTraceUtils;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CMTraceIndexAction extends NodeAction {
    protected final static boolean TEST_XREF = Boolean.getBoolean("test.xref.action"); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor("CM Indexer", 1);
    private final JMenuItem presenter;
    
    public CMTraceIndexAction() {
        presenter = new JMenuItem(getName());
        presenter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onActionPerformed();
            }
        });
    }
    
    @Override
    public final String getName() {
	return NbBundle.getMessage(getClass(), ("CTL_CMTraceIndexAction")); // NOI18N
    }
    
    @Override
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        return getPresenter();
    }    
    
    @Override
    public JMenuItem getPopupPresenter() {
        return getPresenter();
    }
    
    private JMenuItem getPresenter() {
        presenter.setEnabled(true);
        presenter.setVisible(CMTraceIndexAction.TEST_XREF);
        return presenter;
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes)  {
        return true;
    }

    private void onActionPerformed() {
        performAction(getActivatedNodes());
    }
    
    /** Actually nobody but us call this since we have a presenter. */
    @Override
    public void performAction(final Node[] activatedNodes) {
        final Collection<CMIndex> indices = getIndices(activatedNodes);
        if (!indices.isEmpty()) {
            RP.post(new Runnable() {

                @Override
                public void run() {
                    indexImpl(indices);
                }

            });
        }
    }

    private void indexImpl(Collection<CMIndex> indices) {
        String taskName = "Testing Indexing"; // NOI18N
        InputOutput io = IOProvider.getDefault().getIO(taskName, false); // NOI18N
        io.select();
        final OutputWriter out = io.getOut();
        final OutputWriter err = io.getErr();
        final AtomicBoolean canceled = new AtomicBoolean(false);

        final ProgressHandle handle = ProgressHandle.createHandle(taskName, new Cancellable() {
            @Override
            public boolean cancel() {
                canceled.set(true);
                return true;
            }
        });

        handle.start();

        long time = System.currentTimeMillis();

        try {
            for (CMIndex idx : indices) {
                TestIndexCallback visitor = new TestIndexCallback(canceled, out, err);
                CMVisitQuery.visitIndex(idx, visitor, CMVisitQuery.VisitOptions.SkipParsedBodiesInSession);
            }
        } finally {
            handle.finish();
            out.printf("%s\n", canceled.get() ? "Cancelled" : "Done"); //NOI18N
            out.printf("%s took %d ms\n", taskName, System.currentTimeMillis() - time); // NOI18N

            err.flush();
            out.flush();
            err.close();
            out.close();
        }
    }
    
    @Override
    protected boolean asynchronous () {
        return false;
    }
    
    /**
     * Gets the collection of native projects that correspond the given nodes.
     *
     * @return in the case all nodes correspond to native projects - collection
     * of native projects; otherwise null
     */
    protected Collection<CMIndex> getIndices(Node[] nodes) {
        Set<CMIndex> indices = new HashSet<>();
        for (Node node : nodes) {
            CMIndex idx = node.getLookup().lookup(CMIndex.class);
            if (idx == null) {
                Project prj = node.getLookup().lookup(Project.class);
                if (prj == null) {
                    Object o = node.getValue("Project"); // NOI18N
                    if (o instanceof Project) {
                        prj = (Project) o;
                    }
                }
                if (prj != null) {
                    indices.addAll(CMQuery.getIndices(prj));
                }
            } else {
                indices.add(idx);
            }
        }
        return indices;
    }
    
    private static final class TestIndexCallback implements CMVisitQuery.IndexCallback {
        private final OutputWriter out;
        private final OutputWriter err;
        private final AtomicBoolean canceled;

        private TestIndexCallback(AtomicBoolean canceled, OutputWriter out, OutputWriter err) {
            this.out = out;
            this.err = err;
            this.canceled = canceled;
        }

        @Override
        public boolean isCancelled() {
            return canceled.get();
        }

        @Override
        public void onDiagnostics(Iterable<CMDiagnostic> diagnostics) {
            out.printf("onDiagnostics:\n");
            int idx = 0;
            for (CMDiagnostic d : diagnostics) {
                try {
                    out.println(++idx + ":" + CMTraceUtils.toString(d), OpenLink.create(d.getLocation()));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public void onIndclude(CMInclude include) {
            try {
                out.println("onIndclude: " + CMTraceUtils.toString(include), OpenLink.create(include.getHashLocation()));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void onTranslationUnit() {
            out.println("onTranslationUnit");
        }

        @Override
        public void onDeclaration(CMDeclaration decl) {
            try {
                out.println("onDeclaration: " + CMTraceUtils.toString(decl), OpenLink.create(decl.getLocation()));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void onReference(CMEntityReference ref) {
            try {
                out.println("onReference: " + CMTraceUtils.toString(ref), OpenLink.create(ref.getLocation()));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }    

    private static class OpenLink implements OutputListener {
        private final CMSourceLocation loc;

        private OpenLink(CMVisitLocation vLoc) {
            this.loc = vLoc.getLocation();
        }
        
        private OpenLink(CMSourceLocation loc) {
            this.loc = loc;
        }
        
        public static OpenLink create(CMVisitLocation vLoc) {
            if (vLoc == null) {
                return null;
            }
            return create(vLoc.getLocation());
        }
        
        public static OpenLink create(CMSourceLocation loc) {
            if (loc == null || !loc.isValid() || loc.isInSystemHeader()) {
                return null;
            }
            CMFile file = loc.getFile();
            if (file == null) {
                return null;
            }
//            if (!file.getFilePath().toString().contains("/home/")) {
//                return null;
//            }
            return new OpenLink(loc);
        }
        
        @Override
        public void outputLineAction(OutputEvent ev) {
            CMUtilities.openSource(loc);
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
    }
}
