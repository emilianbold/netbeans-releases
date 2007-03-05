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
package com.sun.rave.web.ui.util;

import com.sun.rave.web.ui.component.util.descriptors.Resource;

import javax.faces.context.FacesContext;


/**
 *  This file defines the ResourceFactory interface.  Resources are added to
 *  the Request scope so that they may be accessed easily using JSF EL
 *  value-binding, or by other convient means.
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public interface ResourceFactory {

    /**
     *	This is the factory method responsible for getting the Resource.
     *
     *	@param	context	    The FacesContext
     *	@param	descriptor  The Resource descriptor that is associated
     *			    with the requested Resource.
     *
     *	@return	The newly created Resource
     */
    public Object getResource(FacesContext context, Resource descriptor);
}
