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

/*
 * $Id$
 */

package com.sun.rave.faces.renderer;


import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.ServletResponse;



/**
 * <p>Renderer for a component that dynamically sets the character encoding
 * of the current response.</p>
 */

public class EncodingRenderer extends AbstractRenderer {


    // ------------------------------------------------------- Protected Methods


    /**
     * <p>Set the character component if we have a non-null value.</p>
     */
    protected void renderStart(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {

	String value = (String) component.getAttributes().get("value");
	if (value == null) {
	    return;
	}
        Object response = context.getExternalContext().getResponse();
        if (response instanceof ServletResponse) {
            ((ServletResponse) response).setCharacterEncoding(value);
        }

    }


    // --------------------------------------------------------- Private Methods


}
