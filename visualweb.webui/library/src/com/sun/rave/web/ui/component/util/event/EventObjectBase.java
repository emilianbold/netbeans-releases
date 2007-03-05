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

import java.util.EventObject;

import javax.faces.component.UIComponent;


/**
 *  <p>	This class serves as the base class for <code>EventObject</code>s in
 *	this package.</p>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class EventObjectBase extends EventObject implements UIComponentHolder {

    /**
     *	<p> This constructor should not be used.</p>
     */
    private EventObjectBase() {
	super(null);
    }

    /**
     *	<p> This constructor is protected to avoid direct instantiation, one
     *	    of the sub-classes of this class should be used instead.</p>
     *
     *	@param	component   The <code>UIComponent</code> associated with this
     *			    <code>EventObject</code>.
     */
    protected EventObjectBase(UIComponent component) {
	super(component);
    }

    /**
     *	<P> This method returns the <code>UIComponent</code> held by the
     *	    <code>Object</code> implementing this interface.</p>
     *
     *	@return The <code>UIComponent</code>.
     */
    public UIComponent getUIComponent() {
	return (UIComponent)getSource();
    }
}
