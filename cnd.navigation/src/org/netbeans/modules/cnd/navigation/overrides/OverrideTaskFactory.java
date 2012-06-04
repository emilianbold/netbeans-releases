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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.navigation.overrides;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.model.tasks.CsmFileTaskFactory.PhaseRunner;
import org.netbeans.modules.cnd.model.tasks.EditorAwareCsmFileTaskFactory;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.ui.NamedOption;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Vladimir Kvashin
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.model.tasks.CsmFileTaskFactory.class, position=50)
public class OverrideTaskFactory extends EditorAwareCsmFileTaskFactory {

    private static final int TASK_DELAY = getInt("cnd.overrides.delay", 500); // NOI18N
    private static final int RESCHEDULE_DELAY = getInt("cnd.overrides.reschedule.delay", 500); // NOI18N

    public OverrideTaskFactory() {
    }

    @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=1300)
    public static final class OverrideOptions extends NamedOption {
        private static final String NAME = "overrides-annotations"; //NOI18N

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public OptionKind getKind() {
            return OptionKind.Boolean;
        }

        @Override
        public Object getDefaultValue() {
            return true;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(OverrideTaskFactory.class, "Show-overrides-annotations");
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(OverrideTaskFactory.class, "Show-overrides-annotations-AD");
        }
    }
    
    private static boolean isEnabled() {
        return NamedOption.getAccessor().getBoolean(OverrideOptions.NAME);
    }

    @Override
    protected PhaseRunner createTask(FileObject fo) {
        PhaseRunner pr = null;
        if (isEnabled()) {
            try {
                final DataObject dobj = DataObject.find(fo);
                    EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
                    final CsmFile file = CsmUtilities.getCsmFile(dobj, false, false);
                    final StyledDocument doc = ec.getDocument();
                    if (doc != null && file != null) {
                        pr = new PhaseRunnerImpl(dobj, file, doc);
                    }
            } catch (DataObjectNotFoundException ex)  {
                ex.printStackTrace(System.err);
            }
        }
        return pr != null ? pr : lazyRunner();
    }

    @Override
    protected int taskDelay() {
        return TASK_DELAY;
    }

    @Override
    protected int rescheduleDelay() {
        return RESCHEDULE_DELAY;
    }

    private static int getInt(String name, int result){
        String text = System.getProperty(name);
        if( text != null ) {
            try {
                result = Integer.parseInt(text);
            } catch(NumberFormatException e){
                // default value
            }
        }
        return result;
    }

    private static final class PhaseRunnerImpl implements PhaseRunner {

        private final DataObject dobj;
        private final CsmFile file;
        private final WeakReference<StyledDocument> weakDoc;

        private PhaseRunnerImpl(DataObject dobj,CsmFile file, Document doc){
            this.dobj = dobj;
            this.file = file;
            if (doc instanceof StyledDocument) {
                weakDoc = new WeakReference<StyledDocument>((StyledDocument) doc);
            } else {
                weakDoc = null;
            }
        }

        @Override
        public void run(Phase phase) {
            if (!isEnabled()) {
                AnnotationsHolder.clearIfNeed(dobj);
                return;
            }
            StyledDocument doc = getDocument();
            if (doc != null) {
                if (phase == Phase.PARSED || phase == Phase.INIT || phase == Phase.PROJECT_PARSED) {
                    addAnnotations(file, doc, dobj);
                } else if (phase == Phase.CLEANUP) {
                    clearAnnotations(doc, dobj);
                }
            }
        }

        protected StyledDocument getDocument() {
            return weakDoc != null ? weakDoc.get() : null;
        }

        private void addAnnotations(CsmFile file, StyledDocument doc, DataObject dobj) {
            final Collection<BaseAnnotation> toAdd = new ArrayList<BaseAnnotation>();
            BaseAnnotation.LOGGER.log(Level.FINE, ">> Computing annotations for {0}", file);
            long time = System.currentTimeMillis();
            ComputeAnnotations.getInstance(file, doc, dobj).computeAnnotations(toAdd);
            time = System.currentTimeMillis() - time;
            BaseAnnotation.LOGGER.log(Level.FINE, "<< Computed sannotations for {0} in {1} ms", new Object[] { file, time });
            AnnotationsHolder.get(dobj).setNewAnnotations(toAdd);
        }

        private void clearAnnotations(StyledDocument doc, DataObject dobj) {
            AnnotationsHolder.clearIfNeed(dobj);
        }


        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isHighPriority() {
            return false;
        }

        @Override
        public String toString() {
            if (file == null) {
                return "OverrideTaskFactory runner"; //NOI18N
            } else {
                return "OverrideTaskFactory runner for "+file.getAbsolutePath(); //NOI18N
            }
        }
    }
}
