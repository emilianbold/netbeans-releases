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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Dusan Balek
 */
public final class DialogBindingEmbeddingProvider extends EmbeddingProvider {

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        Document doc = snapshot.getSource().getDocument(true);
        try {
            LanguagePath path = LanguagePath.get(MimeLookup.getLookup(snapshot.getMimeType()).lookup(Language.class));
            InputAttributes attributes = (InputAttributes) doc.getProperty(InputAttributes.class);
            int offset = (Integer)attributes.getValue(path, "dialogBinding.offset"); //NOI18N
            int length = (Integer)attributes.getValue(path, "dialogBinding.length"); //NOI18N
            Document d = (Document) attributes.getValue(path, "dialogBinding.document"); //NOI18N
            if (d != null) {
                String mimeType = DocumentUtilities.getMimeType(d);
                ArrayList<Embedding> ret = new ArrayList<Embedding>(3);
                ret.add(snapshot.create(d.getText(0, offset), mimeType));
                ret.add(snapshot.create(0, snapshot.getText().length(), mimeType));
                ret.add(snapshot.create(d.getText(offset + length, d.getLength() - offset - length), mimeType));
                return Collections.singletonList(Embedding.create(ret));
            } else {
                FileObject fileObject = (FileObject) attributes.getValue(path, "dialogBinding.fileObject"); //NOI18N
                String mimeType = fileObject.getMIMEType();
                InputStream inputStream = fileObject.getInputStream();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, FileEncodingQuery.getEncoding(fileObject)));
                    CharBuffer charBuffer = CharBuffer.allocate(4096);
                    StringBuilder sb = new StringBuilder();
                    int i = bufferedReader.read(charBuffer);
                    while (i > 0) {
                        charBuffer.flip();
                        sb.append(charBuffer);
                        charBuffer.clear();
                        i = bufferedReader.read(charBuffer);
                    }
                    ArrayList<Embedding> ret = new ArrayList<Embedding>(3);
                    ret.add(snapshot.create(sb.subSequence(0, offset), mimeType));
                    ret.add(snapshot.create(0, snapshot.getText().length(), mimeType));
                    ret.add(snapshot.create(sb.subSequence(offset + length, sb.length()), mimeType));
                    return Collections.singletonList(Embedding.create(ret));
                } finally {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void cancel() {
    }

    public static final class Factory extends TaskFactory {

        @Override
        public Collection<SchedulerTask> create(final Snapshot snapshot) {
            return Collections.<SchedulerTask>singletonList(new DialogBindingEmbeddingProvider());
        }
    }
}
