/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
