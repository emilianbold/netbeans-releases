/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.mercurial.remote.ui.update;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.ui.branch.HgBranch;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.spi.diff.MergeVisualizer;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * Shows basic conflict resolver UI.
 *
 * This class is copy&pasted from javacvs
 *
 * 
 */
public class ResolveConflictsExecutor extends HgProgressSupport {
    
    private static final String TMP_PREFIX = "merge"; // NOI18N
    private static final String ORIG_SUFFIX = ".orig."; // NOI18N
    private static final String LOCAL = "local";                        //NOI18N
    
    static final String CHANGE_LEFT = "<<<<<<< "; // NOI18N
    static final String CHANGE_RIGHT = ">>>>>>> "; // NOI18N
    static final String CHANGE_DELIMETER = "======="; // NOI18N
    static final String CHANGE_BASE_DELIMETER = "|||||||"; // NOI18N
    
    private String leftFileRevision = null;
    private String rightFileRevision = null;

    private final VCSFileProxy file;

    public ResolveConflictsExecutor(VCSFileProxy file) {
        super();
        this.file = file;
    }

    public void exec() {
        MergeVisualizer merge = (MergeVisualizer) Lookup.getDefault().lookup(MergeVisualizer.class);
        if (merge == null) {
            throw new IllegalStateException("No Merge engine found."); // NOI18N
        }
        
        try {
            FileObject fo = file.toFileObject();
            if(fo == null) {
                Mercurial.LOG.warning("can't resolve conflicts for null fileobject : " + file + ", exists: " + file.exists());
                return;
            }
            FileLock lock = fo.lock();
            boolean mergeViewerDisplayed = false;
            try {
                mergeViewerDisplayed = handleMergeFor(file, fo, lock, merge);
            } finally {
                if (!mergeViewerDisplayed) {
                    lock.releaseLock();
                }
            }
        } catch (FileAlreadyLockedException e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Set components = TopComponent.getRegistry().getOpened();
                    for (Iterator i = components.iterator(); i.hasNext();) {
                        TopComponent tc = (TopComponent) i.next();
                        if (tc.getClientProperty(ResolveConflictsExecutor.class.getName()) != null) {
                            tc.requestActive();
                        }
                    }
                }
            });
        } catch (IOException ioex) {
            org.openide.ErrorManager.getDefault().notify(ioex);
        }
    }
    
    private boolean handleMergeFor(final VCSFileProxy file, FileObject fo, FileLock lock,
                                final MergeVisualizer merge) throws IOException {
        String mimeType = (fo == null) ? "text/plain" : fo.getMIMEType(); // NOI18N
        String ext = (fo == null) ? "" : "." + fo.getExt();             //NOI18N
        File f1 = FileUtil.normalizeFile(File.createTempFile(TMP_PREFIX, ext));
        File f2 = FileUtil.normalizeFile(File.createTempFile(TMP_PREFIX, ext));
        File f3 = FileUtil.normalizeFile(File.createTempFile(TMP_PREFIX, ext));
        f1.deleteOnExit();
        f2.deleteOnExit();
        f3.deleteOnExit();

        Charset encoding = FileEncodingQuery.getEncoding(fo);
        final Difference[] diffs = copyParts(true, file, f1, true, encoding);
        if (diffs.length == 0) {
            ConflictResolvedAction.resolved(file);  // remove conflict status
            return false;
        }

        copyParts(false, file, f2, false, encoding);
        //GraphicalMergeVisualizer merge = new GraphicalMergeVisualizer();
        String originalLeftFileRevision = leftFileRevision;
        String originalRightFileRevision = rightFileRevision;
        if (leftFileRevision != null) {
            leftFileRevision = leftFileRevision.trim();
        }
        if (rightFileRevision != null) {
            rightFileRevision = rightFileRevision.trim();
        }
        List<HgLogMessage> parentRevisions = null;
        if (LOCAL.equals(leftFileRevision)) {
            try {
                parentRevisions = HgCommand.getParents(Mercurial.getInstance().getRepositoryRoot(file), file, null);
            } catch (HgException ex) {
                Mercurial.LOG.log(Level.INFO, null, ex);
            }
            if (parentRevisions != null && parentRevisions.size() > 1) {
                leftFileRevision = formatRevision(parentRevisions.get(0));
                rightFileRevision = formatRevision(parentRevisions.get(1));
            }
        }
        if (leftFileRevision == null || leftFileRevision.equals(file.getPath() + ORIG_SUFFIX)
                || leftFileRevision.equals(LOCAL)){
            leftFileRevision = org.openide.util.NbBundle.getMessage(ResolveConflictsExecutor.class, "Diff.titleWorkingFile"); // NOI18N
        }
        if (rightFileRevision == null || rightFileRevision.equals(file.getPath() + ORIG_SUFFIX)) {
            rightFileRevision = org.openide.util.NbBundle.getMessage(ResolveConflictsExecutor.class, "Diff.titleWorkingFile"); // NOI18N
        }
        
        final StreamSource s1;
        final StreamSource s2;
        VCSFileProxySupport.associateEncoding(VCSFileProxy.createFileProxy(f1), encoding);
        VCSFileProxySupport.associateEncoding(VCSFileProxy.createFileProxy(f2), encoding);
        s1 = StreamSource.createSource(file.getName(), leftFileRevision, mimeType, f1);
        s2 = StreamSource.createSource(file.getName(), rightFileRevision, mimeType, f2);
        final StreamSource result = new MergeResultWriterInfo(f1, f2, f3, file, mimeType,
                                                              originalLeftFileRevision,
                                                              originalRightFileRevision,
                                                              fo, lock, encoding);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Component c = merge.createView(diffs, s1, s2, result);
                    if (c instanceof TopComponent) {
                        ((TopComponent) c).putClientProperty(ResolveConflictsExecutor.class.getName(), Boolean.TRUE);
                    }
                } catch (IOException ioex) {
                    org.openide.ErrorManager.getDefault().notify(ioex);
                }
            }
        });
        return true;
    }

    /**
     * Copy the file and conflict parts into another file.
     */
    private Difference[] copyParts(boolean generateDiffs, VCSFileProxy source,
                                   File dest, boolean leftPart, Charset charset) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(source.getInputStream(false), charset));
        BufferedWriter w = Files.newBufferedWriter(dest.toPath(), charset);
        ArrayList<Difference> diffList = null;
        if (generateDiffs) {
            diffList = new ArrayList<>();
        }
        try {
            String line;
            boolean isChangeLeft = false;
            boolean isChangeRight = false;
            boolean isChangeBase = false; 
            int f1l1 = 0, f1l2 = 0, f2l1 = 0, f2l2 = 0;
            StringBuilder text1 = new StringBuilder();
            StringBuilder text2 = new StringBuilder();
            int i = 1, j = 1;
            while ((line = r.readLine()) != null) {
                // As the Graphical Merge Visualizer does not support 3 way diff, 
                // remove the base diff itself.
                // Only show the diffs of the two heads against the base
                if (line.startsWith(CHANGE_BASE_DELIMETER)) {
                    isChangeBase = true;
                    continue;
                }
                if (isChangeBase && line.startsWith(CHANGE_DELIMETER)) {
                    isChangeBase = false;
                } else if (isChangeBase) {
                    continue;
                }
                if (line.startsWith(CHANGE_LEFT)) {
                    if (generateDiffs) {
                        if (leftFileRevision == null) {
                            leftFileRevision = line.substring(CHANGE_LEFT.length());
                        }
                        if (isChangeLeft) {
                            f1l2 = i - 1;
                            diffList.add((f1l1 > f1l2) ? new Difference(Difference.ADD,
                                                                        f1l1 - 1, 0, f2l1, f2l2,
                                                                        text1.toString(),
                                                                        text2.toString()) :
                                         (f2l1 > f2l2) ? new Difference(Difference.DELETE,
                                                                        f1l1, f1l2, f2l1 - 1, 0,
                                                                        text1.toString(),
                                                                        text2.toString())
                                                       : new Difference(Difference.CHANGE,
                                                                        f1l1, f1l2, f2l1, f2l2,
                                                                        text1.toString(),
                                                                        text2.toString()));
                            f1l1 = f1l2 = f2l1 = f2l2 = 0;
                            text1.delete(0, text1.length());
                            text2.delete(0, text2.length());
                        } else {
                            f1l1 = i;
                        }
                    }
                    isChangeLeft = !isChangeLeft;
                    continue;
                } else if (line.startsWith(CHANGE_RIGHT)) {
                    if (generateDiffs) {
                        if (rightFileRevision == null) {
                            rightFileRevision = line.substring(CHANGE_RIGHT.length());
                        }
                        if (isChangeRight) {
                            f2l2 = j - 1;
                            diffList.add((f1l1 > f1l2) ? new Difference(Difference.ADD,
                                                                        f1l1 - 1, 0, f2l1, f2l2,
                                                                        text1.toString(),
                                                                        text2.toString()) :
                                         (f2l1 > f2l2) ? new Difference(Difference.DELETE,
                                                                        f1l1, f1l2, f2l1 - 1, 0,
                                                                        text1.toString(),
                                                                        text2.toString())
                                                       : new Difference(Difference.CHANGE,
                                                                        f1l1, f1l2, f2l1, f2l2,
                                                                        text1.toString(),
                                                                        text2.toString()));
                                                       /*
                            diffList.add(new Difference((f1l1 > f1l2) ? Difference.ADD :
                                                        (f2l1 > f2l2) ? Difference.DELETE :
                                                                        Difference.CHANGE,
                                                        f1l1, f1l2, f2l1, f2l2));
                                                        */
                            f1l1 = f1l2 = f2l1 = f2l2 = 0;
                            text1.delete(0, text1.length());
                            text2.delete(0, text2.length());
                        } else {
                            f2l1 = j;
                        }
                    }
                    isChangeRight = !isChangeRight;
                    continue;
                } else if (isChangeRight && line.indexOf(CHANGE_RIGHT) != -1) {
                    String lineText = line.substring(0, line.lastIndexOf(CHANGE_RIGHT));
                    if (generateDiffs) {
                        if (rightFileRevision == null) {
                            rightFileRevision = line.substring(line.lastIndexOf(CHANGE_RIGHT) + CHANGE_RIGHT.length());
                        }
                        text2.append(lineText);
                        f2l2 = j;
                        diffList.add((f1l1 > f1l2) ? new Difference(Difference.ADD,
                                                                    f1l1 - 1, 0, f2l1, f2l2,
                                                                    text1.toString(),
                                                                    text2.toString()) :
                                     (f2l1 > f2l2) ? new Difference(Difference.DELETE,
                                                                    f1l1, f1l2, f2l1 - 1, 0,
                                                                    text1.toString(),
                                                                    text2.toString())
                                                   : new Difference(Difference.CHANGE,
                                                                    f1l1, f1l2, f2l1, f2l2,
                                                                    text1.toString(),
                                                                    text2.toString()));
                        f1l1 = f1l2 = f2l1 = f2l2 = 0;
                        text1.delete(0, text1.length());
                        text2.delete(0, text2.length());
                    }
                    if (!leftPart) {
                        w.write(lineText);
                        w.newLine();
                    }
                    isChangeRight = !isChangeRight;
                    continue;
                } else if (line.equals(CHANGE_DELIMETER)) {
                    if (isChangeLeft) {
                        isChangeLeft = false;
                        isChangeRight = true;
                        f1l2 = i - 1;
                        f2l1 = j;
                        continue;
                    } else if (isChangeRight) {
                        isChangeRight = false;
                        isChangeLeft = true;
                        f2l2 = j - 1;
                        f1l1 = i;
                        continue;
                    }
                } else if (line.endsWith(CHANGE_DELIMETER) && !line.endsWith(CHANGE_DELIMETER + CHANGE_DELIMETER.charAt(0))) {
                    String lineText = line.substring(0, line.length() - CHANGE_DELIMETER.length()) + "\n"; // NOI18N
                    if (isChangeLeft) {
                        text1.append(lineText);
                        if (leftPart) {
                            w.write(lineText);
                            w.newLine();
                        }
                        isChangeLeft = false;
                        isChangeRight = true;
                        f1l2 = i;
                        f2l1 = j;
                    } else if (isChangeRight) {
                        text2.append(lineText);
                        if (!leftPart) {
                            w.write(lineText);
                            w.newLine();
                        }
                        isChangeRight = false;
                        isChangeLeft = true;
                        f2l2 = j;
                        f1l1 = i;
                    }
                    continue;
                }
                if (!isChangeLeft && !isChangeRight || leftPart == isChangeLeft) {
                    w.write(line);
                    w.newLine();
                }
                if (isChangeLeft) {
                    text1.append(line + "\n"); // NOI18N
                }
                if (isChangeRight) {
                    text2.append(line + "\n"); // NOI18N
                }
                if (generateDiffs) {
                    if (isChangeLeft) {
                        i++;
                    } else if (isChangeRight) {
                        j++;
                    } else {
                        i++;
                        j++;
                    }
                }
            }
        } finally {
            try {
                r.close();
            } finally {
                w.close();
            }
        }
        if (generateDiffs) {
            return diffList.toArray(new Difference[diffList.size()]);
        } else {
            return null;
        }
    }

    @Override
    public void perform() {
        exec();
    }

    @Override
    public void run() {
        throw new RuntimeException("Not implemented"); // NOI18N
    }

    private static final int MAX_LEN = 40;
    private static String formatRevision (HgLogMessage revision) {
        StringBuilder sb = new StringBuilder(100);
        String branch = revision.getBranches().length == 0 ? HgBranch.DEFAULT_NAME : revision.getBranches()[0];
        sb.append(revision.getRevisionNumber()).append(" (").append(branch).append(") "); //NOI18N
        sb.append(revision.getShortMessage());
        if (sb.length() > MAX_LEN) {
            sb.delete(MAX_LEN, sb.length());
        }
        return sb.toString();
    }
    

    private static class MergeResultWriterInfo extends StreamSource {
        
        private final File tempf1, tempf2, tempf3;
        private final VCSFileProxy outputFile;
        private VCSFileProxy fileToRepairEntriesOf;
        private final String mimeType;
        private final String leftFileRevision;
        private final String rightFileRevision;
        private FileObject fo;
        private FileLock lock;
        private final Charset encoding;
        
        public MergeResultWriterInfo(File tempf1, File tempf2, File tempf3,
                                     VCSFileProxy outputFile, String mimeType,
                                     String leftFileRevision, String rightFileRevision,
                                     FileObject fo, FileLock lock, Charset encoding) {
            this.tempf1 = tempf1;
            this.tempf2 = tempf2;
            this.tempf3 = tempf3;
            this.outputFile = outputFile;
            this.mimeType = mimeType;
            this.leftFileRevision = leftFileRevision;
            this.rightFileRevision = rightFileRevision;
            this.fo = fo;
            this.lock = lock;
            if (encoding == null) {
                encoding = FileEncodingQuery.getEncoding(FileUtil.toFileObject(tempf1));
            }
            this.encoding = encoding;
        }
        
        @Override
        public String getName() {
            return outputFile.getName();
        }
        
        @Override
        public String getTitle() {
            return org.openide.util.NbBundle.getMessage(ResolveConflictsExecutor.class, "Merge.titleResult"); // NOI18N
        }
        
        @Override
        public String getMIMEType() {
            return mimeType;
        }
        
        @Override
        public Reader createReader() throws IOException {
            throw new IOException("No reader of merge result"); // NOI18N
        }
        
        /**
         * Create a writer, that writes to the source.
         * @param conflicts The list of conflicts remaining in the source.
         *                  Can be <code>null</code> if there are no conflicts.
         * @return The writer or <code>null</code>, when no writer can be created.
         */
        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            Writer w;
            if (fo != null) {
                w = new OutputStreamWriter(fo.getOutputStream(lock), encoding);
            } else {
                w = new OutputStreamWriter(VCSFileProxySupport.getOutputStream(outputFile), encoding);
            }
            if (conflicts == null || conflicts.length == 0) {
                fileToRepairEntriesOf = outputFile;
                return w;
            } else {
                return new MergeConflictFileWriter(w, fo, conflicts,
                                                   leftFileRevision, rightFileRevision);
            }
        }
        
        /**
         * This method is called when the visual merging process is finished.
         * All possible writting processes are finished before this method is called.
         */
        @Override
        public void close() {
            tempf1.delete();
            tempf2.delete();
            tempf3.delete();
            if (lock != null) {
                lock.releaseLock();
                lock = null;
            }
            fo = null;
            if (fileToRepairEntriesOf != null) {
                repairEntries(fileToRepairEntriesOf);
                fileToRepairEntriesOf = null;
            }
        }

        private void repairEntries(VCSFileProxy file) {
            ConflictResolvedAction.resolved(file);  // remove conflict status
        }
    }
    
    private static class MergeConflictFileWriter extends FilterWriter {
        
        private final Difference[] conflicts;
        private int lineNumber;
        private int currentConflict;
        private final String leftName;
        private final String rightName;
        private final FileObject fo;
        
        public MergeConflictFileWriter(Writer delegate, FileObject fo,
                                       Difference[] conflicts, String leftName,
                                       String rightName) throws IOException {
            super(delegate);
            this.conflicts = conflicts;
            this.leftName = leftName;
            this.rightName = rightName;
            this.lineNumber = 1;
            this.currentConflict = 0;
            if (lineNumber == conflicts[currentConflict].getFirstStart()) {
                writeConflict(conflicts[currentConflict]);
                currentConflict++;
            }
            this.fo = fo;
        }
        
        @Override
        public void write(String str) throws IOException {
            super.write(str);
            lineNumber += numChars('\n', str);
            if (currentConflict < conflicts.length && lineNumber >= conflicts[currentConflict].getFirstStart()) {
                writeConflict(conflicts[currentConflict]);
                currentConflict++;
            }
        }
        
        private void writeConflict(Difference conflict) throws IOException {
            super.write(CHANGE_LEFT + leftName + "\n"); // NOI18N
            super.write(conflict.getFirstText());
            super.write(CHANGE_DELIMETER + "\n"); // NOI18N
            super.write(conflict.getSecondText());
            super.write(CHANGE_RIGHT + rightName + "\n"); // NOI18N
        }
        
        private static int numChars(char c, String str) {
            int n = 0;
            for (int pos = str.indexOf(c); pos >= 0 && pos < str.length(); pos = str.indexOf(c, pos + 1)) {
                n++;
            }
            return n;
        }
        
        @Override
        public void close() throws IOException {
            super.close();
            if (fo != null) {
                fo.refresh(true);
            }
        }
    }
}

