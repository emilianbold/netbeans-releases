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
package org.netbeans.modules.xml.catalog.lib;

import java.io.*;
import java.net.*;
import java.util.*;

import org.openide.text.*;
import org.openide.ErrorManager;


/**
 * Defines numb read-only URL environment but finding CloneableOpenSupport (outerclass).
 * It hardcodes <code>text/xml</code> MIME type.
 *
 * @author  Petr Kuzel
 * @version
 */
public abstract class URLEnvironment implements CloneableEditorSupport.Env {

    /** Serial Version UID */
    private static final long serialVersionUID =9098933339895727443L;

    private final URL peer;
    
    private transient Date modified;
        
    /** Creates new StreamEnvironment */
    public URLEnvironment(URL url) {
        if (url == null) throw new NullPointerException();
        peer = url;
        modified = new Date();
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

    /** 
     * @return "text/xml" 
     */
    public java.lang.String getMimeType() {
        return "text/xml"; // NOI18N
    }

    /**
     * Always return fresh stream.
     */
    public java.io.InputStream inputStream() throws java.io.IOException {
        try {
            return peer.openStream();
        } catch (IOException ex) {
            // #21556
            // annotate exception as USER error, he provided wrong URL
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(ex, ErrorManager.USER, null, null, null, null);
            throw ex;
        }
    }
    
    public void addVetoableChangeListener(java.beans.VetoableChangeListener vetoableChangeListener) {
    }
    
    public void addPropertyChangeListener(java.beans.PropertyChangeListener propertyChangeListener) {
    }
            
}
