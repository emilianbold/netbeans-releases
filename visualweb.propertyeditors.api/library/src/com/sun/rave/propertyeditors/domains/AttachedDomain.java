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
package com.sun.rave.propertyeditors.domains;

import com.sun.rave.designtime.DesignProperty;

/**
 * Specialized domain that is attached to a {@link com.sun.rave.designtime.DesignProperty},
 * to provide access to the dynamic context of the property being edited. If a property
 * editor instantiates a {@link Domain} instance and discovers that it is an
 * instanceof {@link AttachedDomain}, it should call
 * <code>setDesignProperty()</code> before calling <code>getElements()</code>.
 * In addition, after the use of an instance has been completed, pass
 * <code>null</code> to <code>setProperty()</code> to release the
 * reference.</p>
 */
public abstract class AttachedDomain extends Domain {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a new instance that is not attached to any
     * {@link DesignProperty}.</p>
     */
    public AttachedDomain() {
        super();
    }


    /**
     * <p>Construct a new instance that is attached to the specified
     * {@link DesignProperty}.</p>
     */
    public AttachedDomain(DesignProperty designProperty) {
        super();
        setDesignProperty(designProperty);
    }


    // -------------------------------------------------------------- Properties


    /**
     * <p>The {@link DesignProperty} we are associated with.</p>
     */
    protected DesignProperty designProperty;


    /**
     * <p>Return the {@link DesignProperty} with which we are associated.</p>
     */
    public DesignProperty getDesignProperty() {
        return this.designProperty;
    }


    /**
     * <p>Set the {@link DesignProperty} with which we are associated.</p>
     *
     * @param designProperty The new associated {@link DesignProperty}
     */
    public void setDesignProperty(DesignProperty designProperty) {
        this.designProperty = designProperty;
    }


}
