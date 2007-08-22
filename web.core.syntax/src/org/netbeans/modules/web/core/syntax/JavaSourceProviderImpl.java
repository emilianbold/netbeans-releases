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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.core.syntax;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class JavaSourceProviderImpl implements JavaSourceProvider {
    
    /** Creates a new instance of JavaSourceProviderImpl */
    public JavaSourceProviderImpl() {
    }
    
    public boolean accept(FileObject file) {
        if (FileOwnerQuery.getOwner(file) == null){
            // turn off scriptlets editing support for files that do not belong
            // to any project and therefore it is not possible to determine
            // class path, see issue 108842
            return false;
        }
        
        return "text/x-jsp".equals(file.getMIMEType()); //NOI18N
    }
    
    public PositionTranslatingJavaFileFilterImplementation forFileObject(FileObject file) {
        if (accept(file)) {
            return new FilterImpl(file);
        } else {
            return null;
        }
    }
    
    private static final class FilterImpl implements PositionTranslatingJavaFileFilterImplementation {
        private FileObject file;
        private SimplifiedJSPServlet data;
        
        private FilterImpl(FileObject file) {
            this.file = file;
        }
        
        public int getOriginalPosition(int javaSourcePosition) {
            if (data != null)
                return data.getRealOffset(javaSourcePosition);
            else
                return javaSourcePosition;
        }

        public int getJavaSourcePosition(int originalPosition) {
            if (data != null) {
                return data.getShiftedOffset(originalPosition);
            } else {
                return originalPosition;
            }
        }

        public Reader filterReader(Reader r) {
            return new StringReader("class SimplifiedJSPServlet {}"); //NOI18N
        }

        public CharSequence filterCharSequence(CharSequence charSequence) {
            try {
                DataObject od = DataObject.find(file);
                EditorCookie ec = od.getCookie(EditorCookie.class);
                Document doc = ec.openDocument();
                
                data = new SimplifiedJSPServlet(doc);
                
                data.process();
                return data.getVirtualClassBody();
            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
                data = null;
                return charSequence;
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
                data = null;
                return charSequence;
            }
        }

        public Writer filterWriter(Writer w) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }
    }

}
