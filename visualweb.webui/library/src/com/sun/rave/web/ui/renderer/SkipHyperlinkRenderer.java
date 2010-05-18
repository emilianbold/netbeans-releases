/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
