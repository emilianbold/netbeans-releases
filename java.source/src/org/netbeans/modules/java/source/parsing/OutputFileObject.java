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

package org.netbeans.modules.java.source.parsing;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;

/**
 *
 * @author Tomas Zezula
 */
public class OutputFileObject extends FileObjects.FileBase {

    private final File baseFolder;


    public static OutputFileObject create (final File baseFolder, final File diskDelegate) {
        assert baseFolder != null && diskDelegate != null;
        String[] pkgNamePair = FileObjects.getFolderAndBaseName(FileObjects.getRelativePath(baseFolder,diskDelegate),File.separatorChar);
        return new OutputFileObject (baseFolder, diskDelegate, FileObjects.convertFolder2Package(pkgNamePair[0],File.separatorChar), pkgNamePair[1]);
    }
    
    /** Creates a new instance of ClassUpdateFileObject */
   private OutputFileObject (final File baseFolder, final File diskDelegate, final String packageName, final String baseName) {
        super (diskDelegate, packageName, baseName);        
        this.baseFolder = baseFolder;    
    }

    public java.nio.CharBuffer getCharContent(boolean ignoreEncodingErrors) throws java.io.IOException {
        throw new UnsupportedOperationException ("Binary file");
    }

    public boolean delete() {
        return this.f.delete();        
    }

    public String getPath() {
        return this.f.getPath();
    }
    
    public URI toUri () {
        return this.f.toURI();
    }

    public long getLastModified() {
        return this.f.lastModified();
    }

    public java.io.InputStream openInputStream() throws java.io.IOException {
        return new FileInputStream (this.f);
    }

    public java.io.OutputStream openOutputStream() throws java.io.IOException {
        return new FileOutputStream (f);
    }

    public java.io.Reader openReader(boolean b) throws java.io.IOException {
        return new FileReader (this.f);
    }

    public java.io.Writer openWriter() throws java.io.IOException {
        return new FileWriter (this.f);
    }    
    
    public @Override String toString () {
        return this.f.getAbsolutePath();
    }
    
    public @Override boolean equals (Object other) {
        if (other instanceof OutputFileObject) {
            OutputFileObject ofo = (OutputFileObject) other;
            return this.baseFolder.equals(ofo.baseFolder) &&
                   this.f.equals(ofo.f);
        }
        return false;
    }
    
    public @Override int hashCode () {
        return this.f.hashCode();
    }        
}
