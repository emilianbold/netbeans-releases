/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.findbugs;

import edu.umd.cs.findbugs.BugCategory;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.findbugs.options.FindBugsPanel;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class AnalyzerImpl implements Analyzer {

    private final Context ctx;

    public AnalyzerImpl(Context ctx) {
        this.ctx = ctx;
    }

    private Thread processingThread;
    private final AtomicBoolean cancel = new AtomicBoolean();

    @Override
    public Iterable<? extends ErrorDescription> analyze() {
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        int i = 0;

        ctx.start(ctx.getScope().getSourceRoots().size() + ctx.getScope().getFolders().size() + ctx.getScope().getFiles().size());

        for (FileObject sr : ctx.getScope().getSourceRoots()) {
            if (cancel.get()) return Collections.emptyList();
            result.addAll(doRunFindBugs(sr));
            ctx.progress(++i);
        }

        for (FileObject file : ctx.getScope().getFiles()) {
            if (cancel.get()) return Collections.emptyList();
            ClassPath source = ClassPath.getClassPath(file, ClassPath.SOURCE);
            FileObject sr = source.findOwnerRoot(file);
            if (sr == null) {
                //XXX: what can be done?
                continue;
            }
            for (ErrorDescription ed : doRunFindBugs(sr)) {
                if (FileUtil.isParentOf(file, ed.getFile()) || file == ed.getFile()) {
                    result.add(ed);
                }
            }
            ctx.progress(++i);
        }

        for (NonRecursiveFolder nrf : ctx.getScope().getFolders()) {
            if (cancel.get()) return Collections.emptyList();
            ClassPath source = ClassPath.getClassPath(nrf.getFolder(), ClassPath.SOURCE);
            FileObject sr = source.findOwnerRoot(nrf.getFolder());
            if (sr == null) {
                //XXX: what can be done?
                continue;
            }
            for (ErrorDescription ed : doRunFindBugs(sr)) {
                if (nrf.getFolder() == ed.getFile().getParent()) {
                    result.add(ed);
                }
            }
            ctx.progress(++i);
        }

        ctx.finish();

        return result;
    }

    private List<ErrorDescription> doRunFindBugs(FileObject sourceRoot) {
        Thread.interrupted();//clear interrupted flag
        synchronized (this) {
            processingThread = Thread.currentThread();
        }
        try {
            return RunFindBugs.runFindBugs(null, ctx.getSettings(), ctx.getSingleWarningId(), sourceRoot, null, null);
        } finally {
            synchronized(this) {
                processingThread = null;
            }
            Thread.interrupted();//clear interrupted flag
        }
    }

    @Override
    public boolean cancel() {
        cancel.set(true);
        synchronized(this) {
            if (processingThread != null) {
                processingThread.interrupt();
            }
        }
        return false;
    }

    @ServiceProvider(service=AnalyzerFactory.class, supersedes="org.netbeans.modules.findbugs.installer.FakeAnalyzerFactory")
    public static final class AnalyzerFactoryImpl extends AnalyzerFactory {

        @Messages("DN_FindBugs=FindBugs")
        public AnalyzerFactoryImpl() {
            super("findbugs", Bundle.DN_FindBugs(), "edu/umd/cs/findbugs/gui2/bugSplash3.png");
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            List<WarningDescription> result = new ArrayList<WarningDescription>();
            DetectorFactoryCollection dfc = DetectorFactoryCollection.instance();

            for (DetectorFactory df : dfc.getFactories()) {
                for (BugPattern bp : df.getReportedBugPatterns()) {
                    BugCategory c = dfc.getBugCategory(bp.getCategory());

                    if (c.isHidden()) continue;

                    result.add(WarningDescription.create(RunFindBugs.PREFIX_FINDBUGS + bp.getType(), bp.getShortDescription(), bp.getCategory(), c.getShortDescription()));
                }
            }

            return result;
        }

        @Override
        public CustomizerProvider<Void, FindBugsPanel> getCustomizerProvider() {
            return new CustomizerProvider<Void, FindBugsPanel>() {
                @Override public Void initialize() {
                    return null;
                }
                @Override public FindBugsPanel createComponent(CustomizerContext<Void, FindBugsPanel> context) {
                    FindBugsPanel result = context.getPreviousComponent();

                    if (result == null) {
                        result = new FindBugsPanel(null, context);
                    }

                    result.setSettings(context.getSettings());

                    return result;
                }
            };
        }

        @Override
        public Analyzer createAnalyzer(Context context) {
            return new AnalyzerImpl(context);
        }

        @Override
        public void warningOpened(ErrorDescription warning) {
            if (NbPreferences.forModule(RunInEditor.class).getBoolean(RunInEditor.RUN_IN_EDITOR, RunInEditor.RUN_IN_EDITOR_DEFAULT)) return;

            HintsController.setErrors(warning.getFile(), RunInEditor.HINTS_KEY, Collections.singleton(warning));
        }

    }
}
