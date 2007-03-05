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

import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import com.sun.rave.web.ui.component.Page;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.MessageUtil;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 * <p>Renderer for a {@link Page} component.</p>
 */

public class PageRenderer extends Renderer {

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

       return;
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        
        if(!(component instanceof Page)) {
            Object[] params = { component.toString(),
                    this.getClass().getName(),
                    Page.class.getName() };
                    String message = MessageUtil.getMessage
                            ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
                            "Renderer.component", params);              //NOI18N
                    throw new FacesException(message);
        }
        
        Page page = (Page)component;
        
        ResponseWriter writer = context.getResponseWriter();
        
        if (!RenderingUtilities.isPortlet(context)) {
            //write xml header
            writer.write("<?xml version=\"1.0\"?>");
            writer.write("\n");
            //write the doctype stuff
            if (page.isXhtml()) {
                if (page.isFrame()) {
                    //xhtml transitional frames
                    writer.write("<!DOCTYPE html PUBLIC \"" +
                            "-//W3C//DTD XHTML 1.0 Frameset//EN\" " +
                            "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">");
                } else {
                    //html transitional
                    writer.write("<!DOCTYPE html PUBLIC \"" +
                            "-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
                            "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
                }
            } else {
                if (page.isFrame()) {
                    //html transitional frames
                    writer.write("<!DOCTYPE html PUBLIC \"" +
                            "-//W3C//DTD HTML 4.01 Frameset//EN\" " +
                            "\"http://www.w3.org/TR/html4/frameset.dtd\">");
                } else {
                    //html transitional
                    writer.write("<!DOCTYPE html PUBLIC \"" +
                            "-//W3C//DTD HTML 4.01 Transitional//EN\" " +
                            "\"http://www.w3.org/TR/html4/loose.dtd\">");
                }
            }
            writer.write("\n");           
        }      
    }

    public boolean getRendersChildren() {
        return false; 
    }
}
