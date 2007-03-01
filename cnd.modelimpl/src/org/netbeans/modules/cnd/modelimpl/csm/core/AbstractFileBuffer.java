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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;

/**
 *
 * @author Vladimir Voskresensky
 */
public abstract class AbstractFileBuffer implements FileBuffer {
    private final String absPath;
    
    protected AbstractFileBuffer(File file) {
        this.absPath = FilePathCache.getString(file.getAbsolutePath());
    }

    public void addChangeListener(ChangeListener listener) {
    }

    public void removeChangeListener(ChangeListener listener) {
    }

    public File getFile() {
        return new File(absPath);
    }
    
    public abstract int getLength();
    public abstract String getText(int start, int end) throws IOException;
    public abstract String getText() throws IOException;
    public abstract InputStream getInputStream() throws IOException;
    public abstract boolean isFileBased();
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    protected void write(DataOutput output) throws IOException {
        assert this.absPath != null;
        output.writeUTF(this.absPath);
    }  
    
    protected AbstractFileBuffer(DataInput input) throws IOException {
        this.absPath = FilePathCache.getString(input.readUTF());
        assert this.absPath != null;
    }    
}
