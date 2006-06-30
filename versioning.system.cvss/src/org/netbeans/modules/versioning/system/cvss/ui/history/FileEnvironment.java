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
package org.netbeans.modules.versioning.system.cvss.ui.history;

import java.io.*;
import java.net.*;
import java.util.*;

import org.openide.text.*;
import org.openide.ErrorManager;
import org.netbeans.modules.versioning.system.cvss.VersionsCache;
import org.netbeans.modules.versioning.system.cvss.IllegalCommandException;
import org.netbeans.modules.versioning.system.cvss.NotVersionedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;


/**
 * Defines numb read-only File environment.
 *
 * @author  Petr Kuzel
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
                    File remoteFile = VersionsCache.getInstance().getRemoteFile(peer, revision, null);
                    in = new FileInputStream(remoteFile);                    
                }
                return in;
            } catch (MalformedURLException ex) {
                ErrorManager err = ErrorManager.getDefault();
                IOException ioex = new IOException();
                err.annotate(ioex, ex);
                err.annotate(ioex, ErrorManager.USER, null, null, null, null);
                throw ioex;
            } catch (IOException ex) {
                ErrorManager err = ErrorManager.getDefault();
                IOException ioex = new IOException();
                err.annotate(ioex, ex);
                err.annotate(ioex, ErrorManager.USER, null, null, null, null);
                throw ioex;
            } catch (CommandException ex) {
                ErrorManager err = ErrorManager.getDefault();
                IOException ioex = new IOException();
                err.annotate(ioex, ex);
                err.annotate(ioex, ErrorManager.USER, null, null, null, null);
                throw ioex;
            } catch (IllegalCommandException ex) {
                ErrorManager err = ErrorManager.getDefault();
                IOException ioex = new IOException();
                err.annotate(ioex, ex);
                err.annotate(ioex, ErrorManager.USER, null, null, null, null);
                throw ioex;
            } catch (AuthenticationException ex) {
                ErrorManager err = ErrorManager.getDefault();
                IOException ioex = new IOException();
                err.annotate(ioex, ex);
                err.annotate(ioex, ErrorManager.USER, null, null, null, null);
                throw ioex;
            } catch (NotVersionedException ex) {
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
