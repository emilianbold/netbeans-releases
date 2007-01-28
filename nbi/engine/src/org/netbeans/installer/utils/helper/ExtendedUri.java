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
 *  
 * $Id$
 */
package org.netbeans.installer.utils.helper;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Kirill Sorokin
 */
public class ExtendedUri {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private URI       remote     = null;
    private List<URI> alternates = new LinkedList<URI>();
    private URI       local      = null;
    private long      size       = 0;
    private String    md5        = null;
    
    public ExtendedUri(final URI remote, final long size, final String md5) {
        this.remote = remote;
        this.size   = size;
        this.md5    = md5;
    }
    
    public ExtendedUri(final URI remote, final List<URI> alternates, final long size, final String md5) {
        this.remote = remote;
        this.size   = size;
        this.md5    = md5;
        
        this.alternates.addAll(alternates);
    }
    
    public ExtendedUri(final URI remote, final URI local, final long size, final String md5) {
        this.remote = remote;
        this.local  = local;
        this.size   = size;
        this.md5    = md5;
    }
    
    public ExtendedUri(final URI remote, final List<URI> alternates, final URI local, final long size, final String md5) {
        this.remote = remote;
        this.local  = local;
        this.size   = size;
        this.md5    = md5;
        
        this.alternates.addAll(alternates);
    }
    
    public URI getRemote() {
        return remote;
    }
    
    public List<URI> getAlternates() {
        return alternates;
    }
    
    public URI getLocal() {
        return local;
    }
    
    public void setLocal(URI uri) {
        local = uri;
    }
    
    public long getSize() {
        return size;
    }
    
    public String getMd5() {
        return md5;
    }
}
