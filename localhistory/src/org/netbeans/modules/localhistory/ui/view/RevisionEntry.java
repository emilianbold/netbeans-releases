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
package org.netbeans.modules.localhistory.ui.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileObject;
import javax.swing.Action;
import org.netbeans.modules.localhistory.utils.FileUtils;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
/**
 *
 * @author Tomas Stupka
 */
// XXX do we need this?
public abstract class RevisionEntry {
//    public static final User EMPTY_USER;
//    public static final Revision LOCAL_REVISION;
//    static {
//        EMPTY_USER = new User() {
//            @Override public String getDisplayValueShort() { return ""; }
//            @Override public String getDisplayValue() { return ""; }
//        };
//        final String local = NbBundle.getMessage(RevisionEntry.class, "LBL_Local");
//        LOCAL_REVISION = new Revision() {
//            @Override public String getDisplayValueShort() { return local; }
//            @Override public String getDisplayValue() { return local; }
//        };
//    }
    protected List<RevisionEntry> siblingEntries = Collections.emptyList();

    public abstract File getFile();
    public abstract long getTimestamp();
    public abstract Date getDate();
    public abstract String getMessage();
    public abstract Action[] getActions();
    public abstract String getUsername();
    public abstract String getUsernameShort();
    public abstract File getHistoryFile() throws IOException;
    public abstract String getMIMEType();
    public abstract String getRevision(); 
    public abstract String getRevisionShort();
    public abstract void setSiblings(List<RevisionEntry> entries);
        
    public static RevisionEntry createRevisionEntry(StoreEntry se) {
        return new LHRevisionEntry(se);
    }
    
    public static RevisionEntry createRevisionEntry(VCSHistoryProvider.HistoryEntry revision, VCSHistoryProvider provider, File file) {
        return new VCSRevisionEntry(revision, provider, file);
    }

    public boolean isLocalHistory() {
        return false;
    }
    
    public List<RevisionEntry> getSiblingEntries() {
        return siblingEntries;
    }
    
    private static class VCSRevisionEntry extends RevisionEntry {
        private final VCSHistoryProvider.HistoryEntry historyEntry;
        private final File file;
        private final VCSHistoryProvider provider;
        private String mimeType;

        public VCSRevisionEntry(VCSHistoryProvider.HistoryEntry historyEntry, VCSHistoryProvider provider, File file) {
            this.historyEntry = historyEntry;
            this.provider = provider;
            this.file = file;
        }

        @Override
        public File getFile() {
            return file; 
        }

        @Override
        public long getTimestamp() {
            return historyEntry.getDateTime().getTime();
        }

        @Override
        public Date getDate() {
            return historyEntry.getDateTime();
        }

        @Override
        public String getMessage() {
            String msg = historyEntry.getMessage();
            return msg != null ? msg : ""; // NOI18N
        }

        @Override
        public void setSiblings(List<RevisionEntry> entries) {
            siblingEntries = new ArrayList<RevisionEntry>(entries.size());
            for (RevisionEntry entry : entries) {
                // add only real siblings, not itself
                // XXX sure it's only VCSRevisionEntry?
                if(entry instanceof VCSRevisionEntry) { // XXX
                    if (!((VCSRevisionEntry)entry).file.equals(file)) {
                        siblingEntries.add(entry);
                    }
                }
            }
            siblingEntries = Collections.unmodifiableList(siblingEntries);
        }
        
        @Override
        public File getHistoryFile() throws IOException {
            File tempFolder = Utils.getTempFolder();
            // we have to hold references to these files, otherwise associated encoding for them will be lost
            // Utils.associateEncoding holds only a weak reference
            File tmpFile = new File(tempFolder, historyEntry.getFiles()[0].getName()); // XXX
            historyEntry.getRevisionFile(historyEntry.getFiles()[0], tmpFile); // XXX
            Utils.associateEncoding(historyEntry.getFiles()[0], tmpFile); // XXX
            return tmpFile;
        }

        @Override
        public String getMIMEType() {
            if(mimeType == null) {
                mimeType = getMimeType(historyEntry.getFiles()[0]); // XXX
            }        
            return mimeType;
        }

        public String getUsername() {
            return historyEntry.getUsername();
        }
        
        public String getUsernameShort() {
            return historyEntry.getUsernameShort();
        }

        public String getRevision() {
            return historyEntry.getRevision();
        }
        public String getRevisionShort() {
            return historyEntry.getRevision();
        }

        @Override
        public Action[] getActions() {
            return historyEntry.getActions();
        }

    }
    
    private static class LHRevisionEntry extends RevisionEntry {
    
        private String mimeType;
        private final StoreEntry se;
        private LHRevisionEntry(StoreEntry se) {
            this.se = se;
        }

        @Override
        public boolean isLocalHistory() {
            return true;
        }

        public File getFile() {
            return se.getFile();
        }

        public long getTimestamp() {
            return se.getTimestamp();
        }

        public Date getDate() {
            return se.getDate();
        }

        public String getMessage() {
            return se.getLabel();
        }

        public void setSiblings(List<RevisionEntry> entries) {
            siblingEntries = new ArrayList<RevisionEntry>(entries.size());
            for (RevisionEntry entry : entries) {
                // add only real siblings, not itself
                // XXX sure it's only LHRevisionEntry?
                if(entry instanceof LHRevisionEntry) {
                    if (((LHRevisionEntry)entry).se.representsFile() && !getFile().equals(entry.getFile())) {
                        siblingEntries.add(entry);
                    }
                } // else {
//                    System.out.println("");
//                }
            }
            siblingEntries = Collections.unmodifiableList(siblingEntries);
        }
        
        public File getHistoryFile() throws IOException {
            File tempFolder = Utils.getTempFolder();
            // we have to hold references to these files, otherwise associated encoding for them will be lost
            // Utils.associateEncoding holds only a weak reference
            File tmpFile = new File(tempFolder, se.getFile().getName());
            extractHistoryFile(tmpFile);
            return tmpFile;
        }


        public String getMIMEType() {
            if(mimeType == null) {
                mimeType = getMimeType(se.getFile());
            }        
            return mimeType;
        }
        
        /**
        * Copies entry file's content to the given temporary file
        * @param entry contains the file's content
        * @param tmpHistoryFile target temporary file
        * @throws java.io.IOException
        */
        private void extractHistoryFile (File tmpHistoryFile) throws IOException {
            try {
                File file = se.getFile();
                tmpHistoryFile.deleteOnExit();
                FileUtils.copy(se.getStoreFileInputStream(), tmpHistoryFile);
                Utils.associateEncoding(file, tmpHistoryFile);
            } catch (IOException e) {
                LocalHistory.LOG.log(Level.WARNING, "Error while retrieving history for file {0} stored as {1}", new Object[]{se.getFile(), se.getStoreFile()}); // NOI18N
            }
        }

        public String getUsername() {
            return "";
        }
        
        public String getUsernameShort() {
            return "";
        }
        
        public String getRevision() {
            return NbBundle.getMessage(RevisionEntry.class, "LBL_Local");
        }
        
        public String getRevisionShort() {
            return NbBundle.getMessage(RevisionEntry.class, "LBL_Local");
        }

        @Override
        public Action[] getActions() {
            return new Action[] {
                SystemAction.get(RevertFileAction.class),
                SystemAction.get(DeleteAction.class)    
            };
        }
    }
    
    private static String getMimeType(File file) {
        FileObject fo = FileUtils.toFileObject(file);
        if(fo != null) {
            return fo.getMIMEType();   
        } else {
            return "content/unknown"; // NOI18N
        }                
    }
}
