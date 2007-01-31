/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
    
    private boolean useZip;
    
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
