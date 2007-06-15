/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.ui;

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
        
        JavaSource js = JavaSourceAccessor.INSTANCE.create(ClasspathInfo.create(fileObject), JavaSourceAccessor.INSTANCE.create(fileObject, offset, length, component), Collections.singletonList(fileObject));
        
        doc.putProperty(JavaSource.class, js);
        
        if (doc.getProperty(Language.class) == null) {
            doc.putProperty(Language.class, JavaTokenId.language());
        }
        
        return js;
    }

}
