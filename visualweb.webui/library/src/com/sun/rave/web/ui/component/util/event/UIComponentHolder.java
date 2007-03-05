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

import javax.faces.component.UIComponent;


/**
 *  <p>	This interface defines a method for obtaining a
 *	<code>UIComponent</code>.  This is used by various
 *	<code>EventObject<code> implementations which hold
 *	<code>UIComponent</code>.  This allows event handling code to access
 *	the <code>UIComponent</code> related to the event.</p>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public interface UIComponentHolder {

    /**
     *	<P> This method returns the <code>UIComponent</code> held by the
     *	    <code>Object</code> implementing this interface.</p>
     *
     *	@return The <code>UIComponent</code>.
     */
    public UIComponent getUIComponent();
}
