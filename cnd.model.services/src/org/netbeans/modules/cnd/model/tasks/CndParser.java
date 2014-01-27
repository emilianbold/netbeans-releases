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

package org.netbeans.modules.cnd.model.tasks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmStandaloneFileProvider;
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
import org.openide.util.Exceptions;
import org.openide.util.WeakSet;

/**
 *
 * @author Alexander Simon
 */
public final class CndParser extends Parser implements CsmProgressListener {
    public final static Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.model.tasks"); //NOI18N
    private CndParserResult cndParserResult;
    private static final class Lock{}
    private final Lock lock = new Lock();
    //Listener support
    private final ChangeSupport listeners = new ChangeSupport(this);
    private static final Set<CndParser> regestry = new WeakSet<CndParser>();

    private CndParser(Collection<Snapshot> snapshots) {
        synchronized(lock) {
            cndParserResult = new CndParserResult(null, snapshots.size() == 1 ? snapshots.iterator().next() : null, 0);
        }
        synchronized(regestry) {
            regestry.add(this);
        }
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        LOG.log(Level.FINE, "parse called for {0}", snapshot); //NOI18N
        if (snapshot == null) {
            return;
        }
        long oldVersion;
        CsmFile oldFile;
        synchronized(lock) {
            if (this.cndParserResult == null) {
                oldVersion = 0;
                oldFile = null;
            } else {
                oldVersion = this.cndParserResult.getFileVersion();
                oldFile = this.cndParserResult.getCsmFile();
            }
        }
        final FileObject fo = snapshot.getSource().getFileObject();
        CsmFile file = CsmUtilities.getCsmFile(fo, false, false);
        boolean allowStandalone = true;
        if (allowStandalone && file == null) {
            file = CsmStandaloneFileProvider.getDefault().getCsmFile(fo);
        }
        if (file != null) {
            try {
                file.scheduleParsing(true);
            } catch (InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
            }
        }
        synchronized(lock) {
            long fileVersion = CsmFileInfoQuery.getDefault().getFileVersion(file);
            if (oldVersion != fileVersion) {
                this.cndParserResult = new CndParserResult(file, snapshot, fileVersion);
            }
        }
        if (oldFile != null && file != oldFile) {
            CsmStandaloneFileProvider.getDefault().notifyClosed(oldFile);
        }
    }
    
    @Override
    public void cancel(CancelReason reason, SourceModificationEvent event) {
        super.cancel(reason, event);
    }

    @Override
    public CndParserResult getResult(Task task) throws ParseException {
        synchronized(lock) {
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
        synchronized(lock) {
            if (cndParserResult != null) {
                Snapshot snapshot = cndParserResult.getSnapshot();
                if (snapshot != null) {
                    FileObject fo = snapshot.getSource().getFileObject();
                    if (fo != null) {
                        CsmFile file = project.findFile(fo.getPath(), false, false);
                        if (file != null) {
                            LOG.log(Level.FINE, "update parse result for {0} because project ready", snapshot); //NOI18N
                            long fileVersion = CsmFileInfoQuery.getDefault().getFileVersion(file);
                            cndParserResult = new CndParserResult(file, snapshot, fileVersion);
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
        synchronized(lock) {
            if (cndParserResult != null) {
                Snapshot snapshot = cndParserResult.getSnapshot();
                if (snapshot != null) {
                    FileObject fo = snapshot.getSource().getFileObject();
                    if (fo != null && fo.equals(file.getFileObject())) {
                        LOG.log(Level.FINE, "update parse result for {0} because file parsed", snapshot); //NOI18N
                        long fileVersion = CsmFileInfoQuery.getDefault().getFileVersion(file);
                        cndParserResult = new CndParserResult(file, snapshot, fileVersion);
                        listeners.fireChange();
                    }
                }
            }
        }
    }

    @Override
    public void parserIdle() {
    }
    
    public static final void firePropertyChanged() {
        HashSet<CndParser> set = new HashSet<CndParser>();
        synchronized(regestry) {
            set.addAll(regestry);
        }
        for(CndParser parser : set) {
            if (parser == null) {
                continue;
            }
            synchronized(parser.lock) {
                if (parser.cndParserResult != null) {
                    Snapshot snapshot = parser.cndParserResult.getSnapshot();
                    if (snapshot != null) {
                        FileObject fo = snapshot.getSource().getFileObject();
                        if (fo != null) {
                            CsmFile file = CsmUtilities.getCsmFile(fo, false, false);
                            if (file != null) {
                                LOG.log(Level.FINE, "update parse result for {0} because property changed", snapshot); //NOI18N
                                long fileVersion = CsmFileInfoQuery.getDefault().getFileVersion(file);
                                parser.cndParserResult = new CndParserResult(file, snapshot, fileVersion);
                                parser.listeners.fireChange();
                            }
                        }
                    }
                }
            }
        }
    }
    
    @MimeRegistrations({
        @MimeRegistration(mimeType=MIMENames.C_MIME_TYPE, service=ParserFactory.class),
        @MimeRegistration(mimeType=MIMENames.CPLUSPLUS_MIME_TYPE, service=ParserFactory.class),
        @MimeRegistration(mimeType=MIMENames.HEADER_MIME_TYPE, service=ParserFactory.class),
        @MimeRegistration(mimeType=MIMENames.FORTRAN_MIME_TYPE, service=ParserFactory.class)
    })
    public static class FactoryImpl extends ParserFactory {
        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            CndParser cndParser = new CndParser(snapshots);
            CsmListeners.getDefault().addProgressListener(cndParser);
            return cndParser;
        }
    }
}
