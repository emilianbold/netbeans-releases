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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.model.tasks.CndParserResult;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.ui.NamedOption;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Vladimir Kvashin
 */
public class OverrideTaskFactory extends IndexingAwareParserResultTask<CndParserResult> {
    private AtomicBoolean canceled = new AtomicBoolean(false);
    private static final RequestProcessor RP = new RequestProcessor("OverrideTaskFactory runner", 1); //NOI18N"
    private static final int TASK_DELAY = getInt("cnd.overrides.delay", 500); // NOI18N

    public OverrideTaskFactory(String mimeType) {
        super(TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    public void run(CndParserResult result, SchedulerEvent event) {
        synchronized (this) {
            canceled.set(true);
            canceled = new AtomicBoolean(false);
        }
        FileObject fo = result.getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return;
        }
        Document doc = result.getSnapshot().getSource().getDocument(false);
        if (!(doc instanceof StyledDocument)) {
            return;
        }
        CsmFile csmFile = result.getCsmFile();
        if (csmFile == null) {
            return;
        }
        DataObject dobj = null;
        try {
            dobj = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace(System.err);
        }
        if (dobj == null) {
            return;
        }
        if (canceled.get()) {
            return;
        }
        RP.post(new RunnerImpl(dobj, csmFile, (StyledDocument)doc, canceled), TASK_DELAY);
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public final synchronized void cancel() {
        canceled.set(true);
    }

    private static boolean isEnabled() {
        return NamedOption.getAccessor().getBoolean(OverrideOptions.NAME);
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

    private static final class RunnerImpl implements Runnable {

        private final DataObject dobj;
        private final CsmFile file;
        private final Reference<StyledDocument> weakDoc;
        private final AtomicBoolean canceled;

        private RunnerImpl(DataObject dobj, CsmFile file, StyledDocument doc, AtomicBoolean canceled){
            this.dobj = dobj;
            this.file = file;
            weakDoc = new WeakReference<StyledDocument>((StyledDocument) doc);
            this.canceled = canceled;
        }

        @Override
        public void run() {
            if (!isEnabled()) {
                AnnotationsHolder.clearIfNeed(dobj);
                return;
            }
            if (canceled.get()) {
                return;
            }
            StyledDocument doc = weakDoc.get();
            if (doc != null) {
                addAnnotations(file, doc, dobj, canceled);
            }
        }

        private void addAnnotations(CsmFile file, StyledDocument doc, DataObject dobj, AtomicBoolean canceled) {
            final Collection<BaseAnnotation> toAdd = new ArrayList<BaseAnnotation>();
            BaseAnnotation.LOGGER.log(Level.FINE, ">> Computing annotations for {0}", file);
            long time = System.currentTimeMillis();
            ComputeAnnotations.getInstance(file, doc, canceled).computeAnnotations(toAdd);
            time = System.currentTimeMillis() - time;
            BaseAnnotation.LOGGER.log(Level.FINE, "<< Computed annotations for {0} in {1} ms", new Object[] { file, time });
            if (canceled.get()) {
                return;
            }
            AnnotationsHolder.get(dobj).setNewAnnotations(toAdd);
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
    
    @MimeRegistrations({
        @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = TaskFactory.class)
    })
    public static final class OverrideTaskFactoryImpl extends TaskFactory {
        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singletonList(new OverrideTaskFactory(snapshot.getMimeType()));
        }
    }
}
