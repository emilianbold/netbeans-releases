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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.model;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

/**
 * Default implementation of {@link Web} module.
 * @author Tomas Mysik
 * @see ApplicationImpl
 */
public class WebImpl implements Web {

    private final String webUri;
    private final String contextRoot;

    /**
     * Constructor with all properties.
     * @param webUri module <tt>URI</tt>.
     * @param contextRoot module context root.
     */
    public WebImpl(final String webUri, final String contextRoot) {
        this.webUri = webUri;
        this.contextRoot = contextRoot;
    }

    public void setWebUri(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getWebUri() {
        return webUri;
    }

    public void setWebUriId(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getWebUriId() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setContextRoot(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRootId(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getContextRootId() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public Object getValue(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        final String newLine = System.getProperty("line.separator");

        sb.append(this.getClass().getName() + " Object {");
        sb.append(newLine);
        
        sb.append(" Module Uri: ");
        sb.append(webUri);
        sb.append(newLine);

        sb.append(" Module context root: ");
        sb.append(contextRoot);
        sb.append(newLine);

        sb.append("}");
        return sb.toString();
    }
}
