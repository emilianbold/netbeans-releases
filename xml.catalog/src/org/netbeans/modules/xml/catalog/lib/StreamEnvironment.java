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
import java.util.*;

import org.openide.text.*;

/**
 * Defines numb InputStream environment but finding CloneableOpenSupport (outerclass).
 * It hardcodes <code>text/xml</code> MIME type.
 *
 * @author  Petr Kuzel
 * @version
 * @deprecated in favour of URLEnvironment. It can reopen the stream.
 */
public abstract class StreamEnvironment implements CloneableEditorSupport.Env {

    /** Serial Version UID */
    private static final long serialVersionUID =9098951539895727443L;

    private InputStream peer;
    
    private final Date modified;

    
    /** Creates new StreamEnvironment */
    public StreamEnvironment(InputStream in) {
        if (in == null) throw new NullPointerException();
        peer = in;
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
    
    public java.io.InputStream inputStream() throws java.io.IOException {
        return peer;
    }
    
    public void addVetoableChangeListener(java.beans.VetoableChangeListener vetoableChangeListener) {
    }
    
    public void addPropertyChangeListener(java.beans.PropertyChangeListener propertyChangeListener) {
    }
            
}
