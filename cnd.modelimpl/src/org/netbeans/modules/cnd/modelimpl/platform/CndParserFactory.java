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

package org.netbeans.modules.cnd.modelimpl.platform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmStandaloneFileProvider;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakSet;

/**
 *
 */
@MimeRegistrations({
    @MimeRegistration(mimeType=MIMENames.C_MIME_TYPE, service=ParserFactory.class),
    @MimeRegistration(mimeType=MIMENames.CPLUSPLUS_MIME_TYPE, service=ParserFactory.class),
    @MimeRegistration(mimeType=MIMENames.HEADER_MIME_TYPE, service=ParserFactory.class),
    @MimeRegistration(mimeType=MIMENames.FORTRAN_MIME_TYPE, service=ParserFactory.class)
})
public final class CndParserFactory extends ParserFactory {

    private static final Set<ParserImpl> registry = new WeakSet<>();
    private final static Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.model.tasks"); //NOI18N
    private static final int NO_DOCUMENT_VERSION = -1;

    @Override
    public Parser createParser(Collection<Snapshot> snapshots) {
        // filter out snapshots that are not suitable (templates, files from zip, etc)
        boolean filter = false;
        int size = 0;
        // perform fast check: don't copy a collection
        // if all snapshots are suitable (which is most likely the case)
        for (Snapshot s : snapshots) {
            FileObject fo = s.getSource().getFileObject();
            if (CsmUtilities.isCsmSuitable(fo)) {
                size++;
            } else {
                filter = true;
            }
        }
        ParserImpl cndParser;
        if (filter) {
            if (size == 0) {
                return null;
            }
            Collection<Snapshot> filtered = new ArrayList<>(size);
            for (Snapshot s : snapshots) {
                FileObject fo = s.getSource().getFileObject();
                if (CsmUtilities.isCsmSuitable(fo)) {
                    filtered.add(s);
                }
            }
            cndParser = new ParserImpl(filtered);
        } else {
            cndParser = new ParserImpl(snapshots);
        }
        CsmListeners.getDefault().addProgressListener(cndParser);
        return cndParser;
    }

    private static class ParserImpl extends Parser implements CsmProgressListener {

        private CndParserResult cndParserResult;

        private static final class Lock {
        }
        private final Lock lock = new Lock();
        //Listener support
        private final ChangeSupport listeners = new ChangeSupport(this);

        private ParserImpl(Collection<Snapshot> snapshots) {
            synchronized (lock) {
                cndParserResult = new CndParserResult(null, snapshots.size() == 1 ? snapshots.iterator().next() : null, 0, NO_DOCUMENT_VERSION);
            }
            synchronized (registry) {
                registry.add(this);
            }
        }

        @Override
        public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            LOG.log(Level.FINE, "parse called for {0}", snapshot); //NOI18N
            if (snapshot == null) {
                return;
            }
            boolean invalidResult;
            long oldVersion;
            CsmFile oldFile;
            CharSequence oldText;
            long oldDocVersion;
            synchronized (lock) {
                if (this.cndParserResult == null) {
                    invalidResult = false;
                    oldVersion = 0;
                    oldFile = null;
                    oldText = null;
                    oldDocVersion = NO_DOCUMENT_VERSION;
                } else {
                    invalidResult = this.cndParserResult.isInvalid();
                    oldVersion = this.cndParserResult.getFileVersion();
                    oldFile = this.cndParserResult.getCsmFile();
                    oldText = this.cndParserResult.getSnapshot().getText();
                    oldDocVersion = this.cndParserResult.getDocumentVersion();
                }
            }
            final FileObject fo = snapshot.getSource().getFileObject();
            long docVersion = getDocumentVersion(snapshot);
            CsmFile file = CsmUtilities.getCsmFile(fo, false, false);
            boolean allowStandalone = true;
            if (allowStandalone && file == null) {
                file = CsmStandaloneFileProvider.getDefault().getCsmFile(fo);
            }
            if (file != null) {
                if (!TraceFlags.USE_PARSER_API) {
                    try {
                        file.scheduleParsing(true);
                    } catch (InterruptedException ex) {
    //                Exceptions.printStackTrace(ex);
                    }
                }
            }
            synchronized (lock) {
                long fileVersion = CsmFileInfoQuery.getDefault().getFileVersion(file);
                if (invalidResult || (oldVersion != fileVersion) || !snapshot.getText().equals(oldText) || (docVersion != oldDocVersion)) {
                    if (TraceFlags.USE_PARSER_API) {
                        if (file instanceof FileImpl) {
                            FileImpl fileImpl = (FileImpl) file;
                            ProjectBase projectImpl = fileImpl.getProjectImpl(false);
                            if (projectImpl != null) {
                                projectImpl.onSnapshotChanged(fileImpl, snapshot);
                            }
                        } else {
                            if(file != null) {
                                throw new IllegalStateException(
                                        "should be instance of FileImpl: " + file.getClass()); //NOI18N
                            }
                        }
                    }
                    this.cndParserResult = new CndParserResult(file, snapshot, fileVersion, docVersion);
                }
            }
            if (oldFile != null && file != oldFile) {
                if (!CsmStandaloneFileProvider.getDefault().isStandalone(file)) {
                    CsmStandaloneFileProvider.getDefault().notifyClosed(oldFile);
                }
            }
        }

        @Override
        public void cancel(CancelReason reason, SourceModificationEvent event) {
            super.cancel(reason, event);
        }

        @Override
        public CndParserResult getResult(Task task) throws ParseException {
            synchronized (lock) {
                LOG.log(Level.FINE, "getResult for {0}, Task={1}, Result={2}", new Object[]{task.getClass().getName(), System.identityHashCode(task), System.identityHashCode(cndParserResult)}); //NOI18N
                return cndParserResult;
            }
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
            listeners.addChangeListener(changeListener);
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
            listeners.removeChangeListener(changeListener);
        }

        @Override
        public void projectParsingStarted(CsmProject project) {
        }

        @Override
        public void projectFilesCounted(CsmProject project, int filesCount) {
        }

        @Override
        public void projectParsingFinished(CsmProject project) {
            fireProjectReadyImpl(project);
        }

        @Override
        public void projectParsingCancelled(CsmProject project) {
        }

        @Override
        public void projectLoaded(CsmProject project) {
            fireProjectReadyImpl(project);
        }

        private void fireProjectReadyImpl(CsmProject project) {
            synchronized (lock) {
                if (cndParserResult != null) {
                    Snapshot snapshot = cndParserResult.getSnapshot();
                    if (snapshot != null) {
                        FileObject fo = snapshot.getSource().getFileObject();
                        if (fo != null) {
                            CsmFile file = project.findFile(fo.getPath(), false, false);
                            if (file != null) {
                                LOG.log(Level.FINE, "update parse result for {0} because project ready", snapshot); //NOI18N
                                long fileVersion = CsmFileInfoQuery.getDefault().getFileVersion(file);
                                cndParserResult = new CndParserResult(file, snapshot, fileVersion, getDocumentVersion(snapshot));
                                listeners.fireChange();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void fileInvalidated(CsmFile file) {
        }

        @Override
        public void fileAddedToParse(CsmFile file) {
        }

        @Override
        public void fileParsingStarted(CsmFile file) {
        }

        @Override
        public void fileParsingFinished(CsmFile file) {
            synchronized (lock) {
                if (cndParserResult != null) {
                    Snapshot snapshot = cndParserResult.getSnapshot();
                    if (snapshot != null) {
                        FileObject fo = snapshot.getSource().getFileObject();
                        if (fo != null && fo.equals(file.getFileObject())) {
                            LOG.log(Level.FINE, "update parse result for {0} because file parsed", snapshot); //NOI18N
                            long fileVersion = CsmFileInfoQuery.getDefault().getFileVersion(file);
                            cndParserResult = new CndParserResult(file, snapshot, fileVersion, getDocumentVersion(snapshot));
                            listeners.fireChange();
                        }
                    }
                }
            }
        }

        @Override
        public void parserIdle() {
        }

        @Override
        public void fileRemoved(CsmFile file) {
        }
    }

    public static final void firePropertyChanged() {
        HashSet<ParserImpl> set = new HashSet<>();
        synchronized (registry) {
            set.addAll(registry);
        }
        for (ParserImpl parser : set) {
            if (parser == null) {
                continue;
            }
            synchronized (parser.lock) {
                if (parser.cndParserResult != null) {
                    Snapshot snapshot = parser.cndParserResult.getSnapshot();
                    if (snapshot != null) {
                        FileObject fo = snapshot.getSource().getFileObject();
                        if (fo != null) {
                            CsmFile file = CsmUtilities.getCsmFile(fo, false, false);
                            if (file != null) {
                                LOG.log(Level.FINE, "update parse result for {0} because property changed", snapshot); //NOI18N
                                long fileVersion = CsmFileInfoQuery.getDefault().getFileVersion(file);
                                parser.cndParserResult = new CndParserResult(file, snapshot, fileVersion, getDocumentVersion(snapshot));
                                parser.listeners.fireChange();
                            }
                        }
                    }
                }
            }
        }
    }

    private static long getDocumentVersion(Snapshot snapshot) {
        final Document doc = snapshot.getSource().getDocument(false);
        long docVersion = NO_DOCUMENT_VERSION;
        if (doc != null) {
            // use instance hash code as version number to not hold reference to doc itself
            docVersion = System.identityHashCode(doc);
        }
        return docVersion;
    }


}
