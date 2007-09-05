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

package org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model;

import java.io.Serializable;

/**
 * JBI service connection
 *
 * @author tli
 */
public class Connection implements Serializable {

    /**
     * DOCUMENT ME!
     */
    private Endpoint consume;

    /**
     * DOCUMENT ME!
     */
    private Endpoint provide;


    /**
     * DOCUMENT ME!
     *
     * @param consume
     * @param provide
     */
    public Connection(Endpoint consume, Endpoint provide) {
        this.consume = consume;
        this.provide = provide;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the consume endpoint.
     */
    public Endpoint getConsume() {
        return consume;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the provide endpoint.
     */
    public Endpoint getProvide() {
        return provide;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Connection other = (Connection) obj;
        if (this.consume != other.consume && (this.consume == null || !this.consume.equals(other.consume))) {
            return false;
        }
        if (this.provide != other.provide && (this.provide == null || !this.provide.equals(other.provide))) {
            return false;
        }
        return true;
    }
    
    public String toString() {
        return consume.getFullyQualifiedName() + " -> " + provide.getFullyQualifiedName();
    }
}
