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

package org.netbeans.modules.xml.schema.ui.nodes;

import org.openide.nodes.Node;

/**
 * Indicates if a node is read-only or writable.
 *
 * @author Nathan Fiedler
 */
public class ReadOnlyCookie implements Node.Cookie {
    /** True if nodes are writable, false to be immutable. */
    private boolean readonly;

    /**
     * Creates a new instance of ReadOnlyCookie that is writable.
     */
    public ReadOnlyCookie() {
        this(false);
    }

    /**
     * Creates a new instance of ReadOnlyCookie.
     *
     * @param  value  true if nodes are read-only, false if writable.
     */
    public ReadOnlyCookie(boolean value) {
        super();
        readonly = value;
    }

    /**
     * Indicates if read-only or not.
     *
     * @return  true if read-only, false if writable.
     */
    public boolean isReadOnly() {
        return readonly;
    }

    /**
     * Changes the read-only value.
     *
     * @param  value  true if nodes are read-only, false if writable.
     */
    public void setReadOnly(boolean value) {
        readonly = value;
    }
}
