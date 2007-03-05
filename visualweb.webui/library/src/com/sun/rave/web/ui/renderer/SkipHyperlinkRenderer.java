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
package com.sun.rave.web.ui.renderer;


import com.sun.rave.web.ui.component.Anchor;
import com.sun.rave.web.ui.component.ImageHyperlink;
import com.sun.rave.web.ui.component.SkipHyperlink;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.util.RenderingUtilities;
import java.io.IOException;
import javax.faces.component.UIComponent;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;


/**
 * <p>This class is responsible for rendering the {@link SkipHyperlink}
 * component.</p>
 */
public class SkipHyperlinkRenderer extends AbstractRenderer {

    /** Creates a new instance of AlertRenderer */
    public SkipHyperlinkRenderer() {
        // default constructor
    }

    public boolean getRendersChildren() {
      return true;
    }

    public  void encodeChildren(FacesContext context, UIComponent component)
        throws IOException {
	super.encodeChildren(context, component);
    }

    protected void renderStart(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
	//intentionally left blank
	
	if (context == null || component == null || writer == null) {
            throw new NullPointerException();
        }
	
	SkipHyperlink link = (SkipHyperlink) component;
	ImageHyperlink imglink = new ImageHyperlink();
	String id = link.getId() + "_skipHyperlinkId"; //NOI18N

	imglink.setId(id);
	imglink.setUrl("#" + id);
	imglink.setIcon(ThemeImages.DOT);
	imglink.setAlt(link.getDescription());
	RenderingUtilities.renderComponent(imglink, context);
    }
         
    protected void renderEnd(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {

	SkipHyperlink link = (SkipHyperlink) component;

	writer.startElement("div", link); //NOI18N
	
	Anchor anchor = new Anchor();
	String id = link.getId() + "_skipHyperlinkId"; //NOI18N	
	anchor.setId(id);
	RenderingUtilities.renderComponent(anchor, context);

	// Close the span, div
	writer.endElement("div"); //NOI18N	
    }
}
