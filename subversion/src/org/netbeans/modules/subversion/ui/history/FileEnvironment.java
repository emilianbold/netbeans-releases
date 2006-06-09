/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.history;

import org.openide.text.CloneableEditorSupport;
import org.openide.ErrorManager;
import org.netbeans.modules.subversion.VersionsCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.*;

/**
 * Defines numb read-only File environment.
 *
 * @author Maros Sandor
 */
public abstract class FileEnvironment implements CloneableEditorSupport.Env {

    /** Serial Version UID */
    private static final long serialVersionUID = 1L;
    
    private String mime = "text/plain";  // NOI18N
    
    private final File peer;

    private final String revision;

    private transient Date modified;
        
    /** Creates new StreamEnvironment */
    public FileEnvironment(File baseFile, String revision, String mime) {
        if (baseFile == null) throw new NullPointerException();
        peer = baseFile;
        modified = new Date();
        this.revision = revision;
        if (mime != null) {
            this.mime = mime;
        }
    }
        
    public void markModified() throws java.io.IOException {
        throw new IOException("r/o"); // NOI18N
    }    
    
    public void unmarkModified() {
    }    

    public void removePropertyChangeListener(java.beans.PropertyChangeListener propertyChangeListener) {
    }
    
    public boolean isModified() {
        return false;
    }
    
    public java.util.Date getTime() {
        return modified;
    }
    
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener vetoableChangeListener) {
    }
    
    public boolean isValid() {
        return true;
    }
    
    public java.io.OutputStream outputStream() throws java.io.IOException {
        throw new IOException("r/o"); // NOI18N
    }

    public java.lang.String getMimeType() {
        return mime;
    }

    /**
     * Always return fresh stream.
     */
    public java.io.InputStream inputStream() throws java.io.IOException {
        return new LazyInputStream();
    }

    private class LazyInputStream extends InputStream {

        private InputStream in;

        public LazyInputStream() {
        }

        private InputStream peer() throws IOException {
            try {
                if (in == null) {
                    File remoteFile = VersionsCache.getInstance().getFileRevision(peer, revision);
                    in = new FileInputStream(remoteFile);                    
                }
                return in;
            } catch (IOException ex) {
                ErrorManager err = ErrorManager.getDefault();
                IOException ioex = new IOException();
                err.annotate(ioex, ex);
                err.annotate(ioex, ErrorManager.USER, null, null, null, null);
                throw ioex;
            }
        }

        public int available() throws IOException {
            return peer().available();
        }

        public void close() throws IOException {
            peer().close();
        }

        public void mark(int readlimit) {
        }

        public boolean markSupported() {
            return false;
        }

        public int read() throws IOException {
            return peer().read();
        }

        public int read(byte b[]) throws IOException {
            return peer().read(b);
        }

        public int read(byte b[], int off, int len) throws IOException {
            return peer().read(b, off, len);
        }

        public void reset() throws IOException {
            peer().reset();
        }

        public long skip(long n) throws IOException {
            return peer().skip(n);
        }

    }

    public void addVetoableChangeListener(java.beans.VetoableChangeListener vetoableChangeListener) {
    }
    
    public void addPropertyChangeListener(java.beans.PropertyChangeListener propertyChangeListener) {
    }
            
}
