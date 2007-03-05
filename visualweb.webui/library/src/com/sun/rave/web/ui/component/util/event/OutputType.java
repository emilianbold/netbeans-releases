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
 *  <P>	This interface provides an abstraction for different locations for
 *	storing output from a handler.  Implementations may store values in
 *	Session, request attributes, databases, etc.</P>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public interface OutputType {

    /**
     *	<P> This method is responsible for retrieving the value of the Output
     *	    from the destination that was specified by handler.  'key' may be
     *	    null.  In cases where it is not needed, it can be ignored.  If it
     *	    is needed, the implementation may either provide a default or
     *	    throw an exception.</P>
     *
     *	@param	context	    The HandlerContext
     *
     *	@param	outDesc	    The IODescriptor for this Output value in
     *			    which to obtain the value
     *
     *	@param	key	    The optional 'key' to use when retrieving the
     *			    value from the OutputType
     *
     *	@return The requested value.
     */
    public Object getValue(HandlerContext context, IODescriptor outDesc, String key);

    /**
     *	<P> This method is responsible for setting the value of the Output
     *	    to the destination that was specified by handler.  'key' may be
     *	    null.  In cases where it is not needed, it can be ignored.  If it
     *	    is needed, the implementation may either provide a default or
     *	    throw an exception.</P>
     *
     *	@param	context	    The HandlerContext
     *
     *	@param	outDesc	    The IODescriptor for this Output value in
     *			    which to obtain the value
     *
     *	@param	key	    The optional 'key' to use when setting the
     *			    value from the OutputType
     *
     *	@param	value	    The value to set
     */
    public void setValue(HandlerContext context, IODescriptor outDesc, String key, Object value);
}
