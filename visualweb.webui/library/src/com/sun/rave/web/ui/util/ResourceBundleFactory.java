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

import java.util.Map;

import javax.faces.context.FacesContext;


/**
 *  <P>	This factory class provides a means to instantiate a
 *	java.util.ResouceBundle.  It implements the {@link ResourceFactory}
 *	which the {@link com.sun.rave.web.ui.renderer.template.TemplateRenderer}
 *	knows how to use to create arbitrary {@link Resource} objects.  This
 *	factory utilizes the ResourceBundleManager for efficiency.</P>
 *
 *  @see ResourceFactory
 *  @see Resource
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class ResourceBundleFactory implements ResourceFactory {

    /**
     *	<P> This is the factory method responsible for obtaining a
     *	    ResourceBundle.  This method uses the ResourceBundleManager to
     *	    manage instances of ResourceBundles per key/locale.</P>
     *
     *	<P> It should be noted that this method does not do anything if there
     *	    is already a request attribute with the given id.</P>
     *
     *	@param	context	    The FacesContext
     *	@param	descriptor  The Resource descriptor that is associated
     *			    with the requested Resource.
     *
     *	@return	The newly created Resource
     */
    public Object getResource(FacesContext context, Resource descriptor) {
	// Get the id from the descriptor, this is the id that should be used
	// to store it in the RequestScope
	String id = descriptor.getId();
	Map map = context.getExternalContext().getRequestMap();
	if (map.containsKey(id)) {
	    // It is already set
	    return map.get(id);
	}

	// Obtain the ResourceBundle
	Object resource = ResourceBundleManager.getInstance().getBundle(
	    descriptor.getExtraInfo(),
	    MessageUtil.getLocale(context));

	// The id does not exist in the request scope yet.
	map.put(id, resource);

	return resource;
    }
}
