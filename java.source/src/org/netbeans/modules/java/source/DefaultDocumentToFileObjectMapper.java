/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.text.Document;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.*;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default implementation of {@link DocumentToFileObjectMapper}
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = DocumentToFileObjectMapper.class)
public final class DefaultDocumentToFileObjectMapper implements DocumentToFileObjectMapper {

    @Override
    public @NullUnknown FileObject getFileObject(@NonNull final Document doc) {
        final Object source = doc.getProperty(Document.StreamDescriptionProperty);
        if (source instanceof DataObject) {
            DataObject dObj = (DataObject) source;
            return dObj.getPrimaryFile();
        }
        final String mimeType = (String) doc.getProperty("mimeType"); //NOI18N
        if ("text/x-dialog-binding".equals(mimeType)) { //NOI18N
            InputAttributes attributes = (InputAttributes) doc.getProperty(InputAttributes.class);
            LanguagePath path = LanguagePath.get(MimeLookup.getLookup(mimeType).lookup(Language.class));
            Document d = (Document) attributes.getValue(path, "dialogBinding.document"); //NOI18N
            if (d != null) {
                Object obj = d.getProperty(Document.StreamDescriptionProperty);
                if (obj instanceof DataObject) {
                    DataObject dObj = (DataObject) obj;
                    return dObj.getPrimaryFile();
                }
            }
            return (FileObject) attributes.getValue(path, "dialogBinding.fileObject"); //NOI18N
        }
        return null;
    }

    @Override
    public @NullUnknown Document getDocument(@NonNull final FileObject fo, boolean forceOpen) throws IOException {
        final DataObject dObj = DataObject.find(fo);
        if (dObj != null) {
            EditorCookie ec = dObj.getCookie(EditorCookie.class);
            if (ec != null) {
                return forceOpen ? ec.openDocument() : ec.getDocument();
            }
        }
        return null;
    }

    @Override
    public @NullUnknown ListenerHandle addTokenHierarchyListener(@NonNull final FileObject fo, @NonNull final TokenHierarchyListener listener) throws IOException {
        final DataObject dobj = DataObject.find(fo);
        final EditorCookie.Observable ec = dobj.getCookie(EditorCookie.Observable.class);
        if (ec == null) {
            return null;
        }
        Handle handle = new Handle(ec, listener);
        ec.addPropertyChangeListener(WeakListeners.propertyChange(handle, ec));
        Document doc = ec.getDocument();
        if (doc != null) {
            TokenHierarchy th = TokenHierarchy.get(doc);
            th.addTokenHierarchyListener(handle.lexListener = WeakListeners.create(TokenHierarchyListener.class, listener, th));
            handle.document = doc;
        }
        return handle;
    }
    
    private static final class Handle implements ListenerHandle, PropertyChangeListener {

        private final EditorCookie.Observable ec;
        private final TokenHierarchyListener listener;
        private volatile Document document = null;
        private TokenHierarchyListener lexListener = null;

        private Handle(EditorCookie.Observable ec, TokenHierarchyListener listener) {
            this.ec = ec;
            this.listener = listener;
        }
        
        @Override
        public Document getDocument() {
            return document;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                Object old = evt.getOldValue();
                if (old instanceof Document && lexListener != null) {
                    TokenHierarchy th = TokenHierarchy.get((Document) old);
                    th.removeTokenHierarchyListener(lexListener);
                    lexListener = null;
                }
                Document doc = ec.getDocument();
                if (doc != null) {
                    TokenHierarchy th = TokenHierarchy.get(doc);
                    th.addTokenHierarchyListener(lexListener = WeakListeners.create(TokenHierarchyListener.class, listener, th));
                    this.document = doc;    //set before rescheduling task to avoid race condition
                } else {
                    //reset document
                    this.document = doc;
                }
            }
        }        
    }
}
