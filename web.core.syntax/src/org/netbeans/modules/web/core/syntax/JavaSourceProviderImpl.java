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
package org.netbeans.modules.web.core.syntax;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceProvider.class)
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
        
        return "text/x-jsp".equals(file.getMIMEType()) || "text/x-tag".equals(file.getMIMEType()); //NOI18N
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
            //Workaround for issue #114951 - Can't close/saveall project: Error "Cannot get exclusive access"
            try {
                r.close();
            }catch(IOException ioe) {
                Logger.global.log(Level.WARNING, "Cannot close stream.", ioe);
            }
            return new StringReader("class SimplifiedJSPServlet {}"); //NOI18N
        }

        public CharSequence filterCharSequence(CharSequence charSequence) {
            try {
                DataObject od = DataObject.find(file);
                EditorCookie ec = od.getCookie(EditorCookie.class);
                Document doc = ec.openDocument();
                
                data = new SimplifiedJSPServlet(doc, charSequence);
                
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
