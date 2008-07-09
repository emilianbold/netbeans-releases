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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.ui;

import java.lang.ref.WeakReference;
import java.util.Collections;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.PositionConverter;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Dusan Balek, Jan Lahoda
 * @since 1.1
 */
public final class DialogBinding {

    private DialogBinding() {}
    
    /**
     * Bind given component and given file together.
     * @param fileObject to bind
     * @param offset position at which content of the component will be virtually placed
     * @param length how many characters replace from the original file
     * @param component component to bind
     * @return {@link JavaSource} or null
     * @throws {@link IllegalArgumentException} if fileObject is null
     * @since 1.1
     */
    public static JavaSource bindComponentToFile(FileObject fileObject, int offset, int length, JTextComponent component) throws IllegalArgumentException {
        if (fileObject == null) {
            throw new IllegalArgumentException ("fileObject == null");  //NOI18N
        }
        if (!fileObject.isValid()) {
            return null;
        }
        if (!"text/x-java".equals(FileUtil.getMIMEType(fileObject)) && !"java".equals(fileObject.getExt())) {  //NOI18N
            //TODO: JavaSource cannot be created for all kinds of files, but text/x-java is too restrictive:
            return null;
        }
        Document doc = component.getDocument();
        
        if (doc.getProperty(JavaSource.class) != null) {
            throw new IllegalArgumentException("A JavaSource is already attached to the given component.");
        }
        
        final JavaSource js = JavaSourceAccessor.getINSTANCE().create(ClasspathInfo.create(fileObject), JavaSourceAccessor.getINSTANCE().create(fileObject, offset, length, component), Collections.singletonList(fileObject));
        
        doc.putProperty(JavaSource.class, new WeakReference<JavaSource>(null) {
            @Override
            public JavaSource get() {
                return js;
            }
        });
        doc.putProperty(Document.StreamDescriptionProperty, fileObject);
        
        if (doc.getProperty(Language.class) == null) {
            doc.putProperty(Language.class, JavaTokenId.language());
        }
        
        return js;
    }

}
