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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.*;
import antlr.collections.*;

import java.util.zip.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 *
 * @author Vladimir Kvashin
 */
public class Cache {
    
    private interface Delegate {
        public void writeAst(AST tree, File file) throws IOException ;
        public AST readAst(File file) throws IOException, ClassNotFoundException;
    }
    
    private static class AstWrapper implements Serializable {
        public AST ast;
        public AstWrapper(AST ast) {
            this.ast = ast;
        }
    }
    
    private abstract class Base implements Delegate {
        
        abstract protected InputStream getFileInputStream(File srcFile) throws IOException;
        
        abstract protected OutputStream getFileOutputStream(File srcFile) throws IOException;
            
        public void writeAst(AST tree, File srcFile) throws IOException { 
            ObjectOutput out = new ObjectOutputStream(getFileOutputStream(srcFile));
            AstWrapper w = new AstWrapper(tree);
            out.writeObject(w);
            out.close();
        }

        public AST readAst(File srcFile) throws IOException, ClassNotFoundException {
            AstWrapper w = null;
            ObjectInputStream in = new ObjectInputStream(getFileInputStream(srcFile));
            w = (AstWrapper) in.readObject();
            in.close();
            return w.ast;
        }    
    }
    
    private class Plain extends Base implements Delegate { 

        private File cacheDir;
        
        public Plain() {
            cacheDir = new File("test-model-cache"); // NOI18N
            cacheDir.mkdirs();
        }
        
        protected InputStream getFileInputStream(File srcFile) throws IOException {
            File cache = getCacheFile(srcFile);
            return new BufferedInputStream(new FileInputStream(cache), TraceFlags.BUF_SIZE);
        }

        protected OutputStream getFileOutputStream(File srcFile) throws IOException {
            File cache = getCacheFile(srcFile);
            return new FileOutputStream(cache);
        }
        
        protected File getCacheFile(File sourceFile) {
            return new File(cacheDir, getEntryName(sourceFile));
        }
        
        protected String getEntryName(File sourceFile) {
            //return sourceFile.getName() + ".ast";
            return sourceFile.getAbsolutePath().substring(1) + ".ast"; // NOI18N
        }
        
    }

    private class Zipped extends Plain implements Delegate { 
        
        private ZipFile zip;
        
        public Zipped() throws IOException {
            zip = new ZipFile(new File("test-model-cache.zip"), ZipFile.OPEN_READ); // NOI18N
        }
        
        protected InputStream getFileInputStream(File srcFile) throws IOException {
            String entryName = getEntryName(srcFile);
            ZipEntry entry = zip.getEntry(entryName);
            if( entry == null ) {
                throw new ZipException("Entry not found: " + entryName); // NOI18N
            }
            else {
                InputStream is = zip.getInputStream(entry);
                return is;
            }
        }

    }
    
    Delegate delegate;
    
    public Cache(boolean useZip) throws IOException {
        delegate = useZip ? (Delegate) new Zipped() : (Delegate) new Plain();
    }
    
    public void writeAst(AST tree, File sourcFile) throws IOException { 
        delegate.writeAst(tree, sourcFile);
    }
    
    public AST readAst(File sourcFile) throws IOException, ClassNotFoundException {
        return delegate.readAst(sourcFile);
    }
    

    

    
        
}
