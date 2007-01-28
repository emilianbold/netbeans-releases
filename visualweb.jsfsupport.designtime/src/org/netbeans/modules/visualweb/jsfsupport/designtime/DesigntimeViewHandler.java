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

package org.netbeans.modules.visualweb.jsfsupport.designtime;


import java.io.IOException;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.context.FacesContext;

/**
 * Design time View ViewHandler
 * @author Winston Prakash
 * @version 1.0
 */

public class DesigntimeViewHandler extends ViewHandlerWrapper{
    private ViewHandler handler = null;

    public DesigntimeViewHandler(ViewHandler handler) {
	this.handler = handler;
    }


    /**
     * <p>Take appropriate action to save the current state information.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     *
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>context</code>
     *  is <code>null</code>
     */
    public void writeState(FacesContext context) throws IOException {
         // Do nothing. This is to avoid the Sate marker appearing in the designer
    }

    protected ViewHandler getWrapped() {
        return handler;
    }

}
