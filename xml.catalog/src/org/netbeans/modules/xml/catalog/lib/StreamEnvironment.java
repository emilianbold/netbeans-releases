/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.catalog.lib;

import java.io.*;
import java.util.*;

import org.openide.text.*;

/**
 * Defines numb InputStream environment but finding CloneableOpenSupport (outerclass).
 *
 * @author  Petr Kuzel
 * @version 
 */
public abstract class StreamEnvironment implements CloneableEditorSupport.Env {

    /** Serial Version UID */
    private static final long serialVersionUID =9098951539895727443L;
    
    private final InputStream peer;
    
    private final Date modified;

    
    /** Creates new StreamEnvironment */
    public StreamEnvironment(InputStream in) {
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
