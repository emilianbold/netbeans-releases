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
package com.sun.rave.web.ui.component.util.event;


/**
 *  <P>	This class implements the OutputType interface to provide a way to
 *	get/set Output values from a ServletRequest attribute Map.</P>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class RequestAttributeOutputType implements OutputType {

    /**
     *	<P> This method is responsible for retrieving the value of the Output
     *	    from a Request attribute.  'key' may be null, if this occurs, a
     *	    default name will be provided.  That name will follow the
     *	    following format:</P>
     *
     *	<P> [handler-id]:[key]</P>
     *
     *	@param	context	    The HandlerContext
     *
     *	@param	outDesc	    The IODescriptor for this Output value in
     *			    which to obtain the value
     *
     *	@param	key	    The optional 'key' to use when retrieving the
     *			    value from the ServletRequest attribute Map.
     *
     *	@return The requested value.
     */
    public Object getValue(HandlerContext context, IODescriptor outDesc, String key) {
	if (key == null) {
	    // Provide a reasonably unique default
	    key = context.getHandlerDefinition().getId()+':'+outDesc.getName();
	}

	// Get it from the Request attribute map
	return context.getFacesContext().getExternalContext().
	    getRequestMap().get(key);
    }

    /**
     *	<P> This method is responsible for setting the value of the Output to
     *	    a ServletRequest attribute.  'key' may be null, in this case, a
     *	    default name will be provided.  That name will follow the
     *	    following format:</P>
     *
     *	<P> [handler-id]:[key]</P>
     *
     *	@param	context	    The HandlerContext
     *
     *	@param	outDesc	    The IODescriptor for this Output value in
     *			    which to obtain the value
     *
     *	@param	key	    The optional 'key' to use when setting the
     *			    value into the ServletRequest attribute Map
     *
     *	@param	value	    The value to set
     */
    public void setValue(HandlerContext context, IODescriptor outDesc, String key, Object value) {
	if (key == null) {
	    // Provide a reasonably unique default
	    key = context.getHandlerDefinition().getId()+':'+outDesc.getName();
	}

	// Get it from the Request attribute map
	context.getFacesContext().getExternalContext().
	    getRequestMap().put(key, value);
    }
}
