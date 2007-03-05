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
package com.sun.rave.web.ui.component;
import javax.faces.context.FacesContext;

/**
 * <p>Most components whose renderers write more than one HTML
 * element need to implement this interface, which exists to allow
 * for a distinction between the component ID and the ID of the
 * primary element that can recieve user input or focus. The
 * latter is needed to allow the application to maintain
 * focus, and to set the <code>for</code>attribute on labels.</p>
 */
public interface ComplexComponent {

    /**
     * Implement this method so that it returns the DOM ID of the
     * HTML element which should receive focus when the component
     * receives focus, and to which a component label should apply.
     * Usually, this is the first element that accepts input.
     * 
     * @param context The FacesContext for the request
     * @return The client id, also the JavaScript element id
     */
     public String getPrimaryElementID(FacesContext context);
     
}
