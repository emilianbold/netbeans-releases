/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.cnd.refactoring.support;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.PositionRef;

/**
 * based on org.netbeans.api.java.source.ModificationResult
 *
 * Class that collects changes for one project
 *
 * @author Dusan Balek
 * @author Vladimir Voskresensky
 */
public final class ModificationResult {

    private final CsmProject project;
    Map<FileObject, List<Difference>> diffs = new HashMap<FileObject, List<Difference>>();

    /** Creates a new instance of ModificationResult */
    public ModificationResult(CsmProject project) {
        this.project = project;
    }

    // API of the class --------------------------------------------------------
    public void addDifference(FileObject fo, Difference diff) {
        List<Difference> foDiffs = diffs.get(fo);
        if (foDiffs == null) {
            foDiffs = new LinkedList<Difference>();
            diffs.put(fo, foDiffs);
        }
        foDiffs.add(diff);
    }

    public Set<? extends FileObject> getModifiedFileObjects() {
        return diffs.keySet();
    }

    public List<? extends Difference> getDifferences(FileObject fo) {
        return diffs.get(fo);
    }

    /**
     * Once all of the changes have been collected, this method can be used
     * to commit the changes to the source files
     */
    public void commit() throws IOException {
        try {
            for (Map.Entry<FileObject, List<Difference>> me : diffs.entrySet()) {
                commit(me.getKey(), me.getValue(), null);
            }
        } finally {
            if (this.project != null) {
                // need to reparse project
                this.project.waitParse();
            }
        }
    }

    private void commit(final FileObject fo, final List<Difference> differences, Writer out) throws IOException {
        DataObject dObj = DataObject.find(fo);
        EditorCookie ec = dObj != null ? dObj.getCookie(org.openide.cookies.EditorCookie.class) : null;
        // if editor cookie was found and user does not provided his own
        // writer where he wants to see changes, commit the changes to 
        // found document.
        if (ec != null && out == null) {
            final Document doc = ec.getDocument();
            if (doc != null) {
                final IOException ioe[] = new IOException[]{null};
                Runnable runnable = new Runnable() {

                    public void run() {
                        for (Difference diff : differences) {
                            if (diff.isExcluded()) {
                                continue;

                            }
                            try {
                                switch (diff.getKind()) {
                                    case INSERT:
                                        doc.insertString(diff.getStartPosition().getOffset(), diff.getNewText(), null);
                                        break;
                                    case REMOVE:
                                        doc.remove(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset());
                                        break;
                                    case CHANGE:
                                        doc.remove(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset());
                                        doc.insertString(diff.getStartPosition().getOffset(), diff.getNewText(), null);
                                        break;
                                }
                            } catch (BadLocationException ex) {
                                IOException ioex = new IOException();
                                ioex.initCause(ex);
                                ioe[0] = ioex;
                            }
                        }
                    }
                };
                if (doc instanceof BaseDocument) {
                    doc.putProperty("cnd.refactoring.modification.event", Boolean.TRUE);
                    ((BaseDocument) doc).runAtomic(runnable);
                    doc.putProperty("cnd.refactoring.modification.event", Boolean.FALSE);
                } else {
                    runnable.run();
                }
                if (ioe[0] != null) {
                    throw ioe[0];
                }
                return;
            }
        }
        InputStream ins = null;
        ByteArrayOutputStream baos = null;
        Reader in = null;
        try {
            Charset encoding = FileEncodingQuery.getEncoding(fo);
            ins = fo.getInputStream();
            baos = new ByteArrayOutputStream();
            FileUtil.copy(ins, baos);

            ins.close();
            ins = null;
            byte[] arr = baos.toByteArray();
            int arrLength = convertToLF(arr);
            baos.close();
            baos = null;
            in = new InputStreamReader(new ByteArrayInputStream(arr, 0, arrLength), encoding);
            // initialize standard commit output stream, if user
            // does not provide his own writer
            if (out == null) {
                out = new OutputStreamWriter(fo.getOutputStream(), encoding);
            }
            int offset = 0;
            for (Difference diff : differences) {
                if (diff.isExcluded()) {
                    continue;
                }
                int pos = diff.getStartPosition().getOffset();
                int toread = pos - offset;
                char[] buff = new char[toread];
                int n;
                int rc = 0;
                while ((n = in.read(buff, 0, toread - rc)) > 0 && rc < toread) {
                    out.write(buff, 0, n);
                    rc += n;
                    offset += n;
                }
                switch (diff.getKind()) {
                    case INSERT:
                        out.write(diff.getNewText());
                        break;
                    case REMOVE:
                        int len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                        in.skip(len);
                        offset += len;
                        break;
                    case CHANGE:
                        len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                        in.skip(len);
                        offset += len;
                        out.write(diff.getNewText());
                        break;
                }
            }
            char[] buff = new char[1024];
            int n;
            while ((n = in.read(buff)) > 0) {
                out.write(buff, 0, n);
            }
        } finally {
            if (ins != null) {
                ins.close();
            }
            if (baos != null) {
                baos.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    private int convertToLF(byte[] buff) {
        int j = 0;
        for (int i = 0; i < buff.length; i++) {
            if (buff[i] != '\r') {
                buff[j++] = buff[i];
            }
        }
        return j;
    }

    /**
     * Returned string represents preview of resulting source. No difference
     * really is applied. Respects {@code isExcluded()} flag of difference.
     * 
     * @param   there can be more resulting source, user has to specify
     *          which wants to preview.
     * @return  if changes are applied source looks like return string
     */
    public String getResultingSource(FileObject fileObject) throws IOException {
        assert fileObject != null : "Provided fileObject is null";
        StringWriter writer = new StringWriter();
        commit(fileObject, diffs.get(fileObject), writer);

        return writer.toString();
    }

    public Set<File> getNewFiles() {
        Set<File> newFiles = new HashSet<File>();
        for (List<Difference> ds : diffs.values()) {
            for (Difference d : ds) {
                if (d.getKind() == Difference.Kind.CREATE) {
                    //newFiles.add(new File(((CreateChange) d).getFileObject().toUri()));
                }
            }
        }
        return newFiles;
    }

    public static final class Difference {

        final Kind kind;
        final PositionRef startPos;
        final PositionRef endPos;
        final String oldText;
        final String newText;
        final String description;
        private boolean excluded;

        public Difference(Kind kind, PositionRef startPos, PositionRef endPos, String oldText, String newText, String description) {
            this.kind = kind;
            this.startPos = startPos;
            this.endPos = endPos;
            this.oldText = oldText;
            this.newText = newText;
            this.description = description;
            this.excluded = false;
        }

        public Kind getKind() {
            return kind;
        }

        public PositionRef getStartPosition() {
            return startPos;
        }

        public PositionRef getEndPosition() {
            return endPos;
        }

        public String getOldText() {
            return oldText;
        }

        public String getNewText() {
            return newText;
        }

        public boolean isExcluded() {
            return excluded;
        }

        public void exclude(boolean b) {
            excluded = b;
        }

        @Override
        public String toString() {
            return kind + "<" + startPos.getOffset() + ", " + endPos.getOffset() + ">: " + oldText + " -> " + newText; // NOI18N
        }

        public String getDescription() {
            return description;
        }

        public static enum Kind {

            INSERT,
            REMOVE,
            CHANGE,
            CREATE
        }
    }
}
