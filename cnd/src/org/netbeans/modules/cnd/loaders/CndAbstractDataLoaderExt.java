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
package org.netbeans.modules.cnd.loaders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiDataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Sergey Grinev
 */
public abstract class CndAbstractDataLoaderExt extends CndAbstractDataLoader {

    protected CndAbstractDataLoaderExt(String representationClassName) {
        super(representationClassName);
    }

    @Override
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        return new CndFormatExt(obj, primaryFile);
    }

    private static class CndFormatExt extends CndFormat {

        public CndFormatExt(MultiDataObject obj, FileObject primaryFile) {
            super(obj, primaryFile);
        }

        @Override
        public FileObject createFromTemplate(FileObject f, String name) throws IOException {
            // we don't want extension to be taken from template filename for our customized dialog
            String ext;
            if (MIMEExtensions.isCustomizableExtensions(getFile().getMIMEType())) {
                ext = FileUtil.getExtension(name);
                if (ext.length() != 0) {
                    name = name.substring(0, name.length() - ext.length() - 1);
                }
            } else {
                // use default
                ext = getFile().getExt();
            }

            FileObject fo = f.createData(name, ext);

            java.text.Format frm = createFormat(f, name, ext);

            EditorKit kit = createEditorKit(getFile().getMIMEType());
            Document doc = kit.createDefaultDocument();

            BufferedReader r = new BufferedReader(new InputStreamReader(
                    getFile().getInputStream(), FileEncodingQuery.getEncoding(getFile())));
            try {
                FileLock lock = fo.lock();
                try {
                    Charset encoding = FileEncodingQuery.getEncoding(fo);
                    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                            fo.getOutputStream(lock), encoding));
                    try {
                        String current;
                        String line = null;
                        int offset = 0;
                        while ((current = r.readLine()) != null) {
                            if (line != null) {
                                doc.insertString(offset, "\n", null); // NOI18N
                                offset++;
                            }
                            line = frm.format(current);
                            doc.insertString(offset, line, null);
                            offset += line.length();
                        }
                        doc.insertString(doc.getLength(), "\n", null); // NOI18N
                        offset++;
                        Reformat reformat = Reformat.get(doc);
                        reformat.lock();
                        try {
                            reformat.reformat(0, doc.getLength());
                        } finally {
                            reformat.unlock();
                        }
                        w.write(doc.getText(0, doc.getLength()));
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        w.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            } finally {
                r.close();
            }

            // copy attributes
            FileUtil.copyAttributes(getFile(), fo);

            // unmark template state
            setTemplate(fo, false);

            return fo;
        }

        private EditorKit createEditorKit(String mimeType) {
            EditorKit kit;
            kit = JEditorPane.createEditorKitForContentType(mimeType);
            if (kit == null) {
                kit = new javax.swing.text.DefaultEditorKit();
            }
            return kit;
        }
    }
}
