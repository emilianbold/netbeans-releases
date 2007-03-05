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
import com.sun.rave.web.ui.util.RenderingUtilities;
import java.beans.Beans;

import java.io.IOException;
import java.net.URL;

import javax.faces.component.UIComponent;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * <p>This class is responsible for rendering the {@link Anchor} component for the
 * HTML Render Kit.</p> <p> The {@link Anchor} component can be used as an anchor</p>
 */
public class AnchorRenderer extends AbstractRenderer {


      // -------------------------------------------------------- Renderer Methods


      /**
       * <p>Render the start of an anchor (Anchor) tag.</p>
       * @param context <code>FacesContext</code> for the current request
       * @param component <code>UIComponent</code> to be rendered
       * @param writer <code>ResponseWriter</code> to which the element
       * start should be rendered
       * @exception IOException if an input/output error occurs
       */
      protected void renderStart(FacesContext context, UIComponent component,
      ResponseWriter writer) throws IOException {

          Anchor anchor = (Anchor) component;          
          writer.startElement("a", anchor); //NOI18N
      
      }
      /**
       * <p>Render the attributes for an anchor tag.  The onclick attribute will contain
       * extra javascript that will appropriately submit the form if the URL field is
       * not set.</p>
       * @param context <code>FacesContext</code> for the current request
       * @param component <code>UIComponent</code> to be rendered
       * @param writer <code>ResponseWriter</code> to which the element
       * attributes should be rendered
       * @exception IOException if an input/output error occurs
       */
      protected void renderAttributes(FacesContext context, UIComponent component,
      ResponseWriter writer) throws IOException {

          // Set up local variables we will need
          Anchor anchor = (Anchor) component;
          String id = anchor.getId();
          
          // Render core and pass through attributes as necessary
          // NOTE - id is being rendered "as is" instead of the normal convention
          // that we render the client id.
          writer.writeAttribute("id", id, "id"); //NOI8N
          String style = anchor.getStyle();
          String styleClass = anchor.getStyleClass();
          if (styleClass != null) {
                RenderingUtilities.renderStyleClass(context, writer, component, null);
          }
          if (style != null) {
              writer.writeAttribute("style", style, null);
          }
  
          // XHTML requires that this been the same as the id and it may
          // removed.
          writer.writeAttribute("name", id, null); //NO18N

    }
      
      /**
       * <p>Close off the anchor tag.</p>
       * @param context <code>FacesContext</code> for the current request
       * @param component <code>UIComponent</code> to be rendered
       * @param writer <code>ResponseWriter</code> to which the element
       * end should be rendered
       * @exception IOException if an input/output error occurs
       */
      protected void renderEnd(FacesContext context, UIComponent component,
      ResponseWriter writer) throws IOException {
          // End the appropriate element

          Anchor anchor = (Anchor) component;
          writer.endElement("a"); //NOI18N

      }
            
      // --------------------------------------------------------- Private Methods
      
}
