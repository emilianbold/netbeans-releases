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

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.FacesException;
import com.sun.rave.web.ui.component.Body;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.MessageUtil;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Renderer for a {@link Body} component.</p>
 */
public class BodyRenderer extends AbstractRenderer {

    private static final boolean DEBUG = false;
    /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String[] stringAttributes = {"onClick", "onDblClick", "onMouseDown", "onMouseUp", "onMouseOver", "onMouseMove", "onMouseOut", "onKeyPress", "onKeyDown", "onKeyUp", "onFocus", "onBlur"}; //NOI18N
    /**
     * <p>The set of integer pass-through attributes to be rendered.</p>
     */
    private static final String[] integerAttributes = {"tabIndex"}; //NOI18N

    /**
     * <p>Render the appropriate element start, depending on whether the
     * <code>for</code> property is set or not.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component component to render.
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderStart(FacesContext context, UIComponent component, ResponseWriter writer) throws IOException {

        // Start the appropriate element
        if (RenderingUtilities.isPortlet(context)) {
            return;
        }

        if (!(component instanceof Body)) {
            Object[] params = {component.toString(), this.getClass().getName(), Body.class.getName()};
            String message = MessageUtil.getMessage("com.sun.rave.web.ui.resources.LogMessages", "Renderer.component", params); //NOI18N
            throw new FacesException(message);
        }

        writer.startElement("body", component); //NOI18N
    }

    /**
     * <p>Render the appropriate element attributes,
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component component to be rendered
     *  submitted value is to be stored
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAttributes(FacesContext context, UIComponent component, ResponseWriter writer) throws IOException {

        if (RenderingUtilities.isPortlet(context)) {
            return;
        }

        Body body = (Body) component;

        addCoreAttributes(context, component, writer, null);
        addStringAttributes(context, component, writer, stringAttributes);


        // onload is a special case;
        String onload = body.getOnLoad();
        StringBuffer sb = new StringBuffer(256);
        if (onload != null) {
            sb.append(onload);
        }
        if (body.getFocusID(context) != null && !RenderingUtilities.isPortlet(context)) {
            if (onload != null) {
                sb.append("; ");
            }
            sb.append("return ");
            sb.append(body.getJavaScriptObjectName(context));
            sb.append(".setInitialFocus();");
        }
        if (sb.length() > 0) {
            writer.writeAttribute("onload", sb.toString(), null); //NO18N
        }
        // <RAVE>
        String imageUrl = body.getImageURL();
        if (imageUrl != null && imageUrl.length() > 0) {
            String resourceUrl = context.getApplication().getViewHandler().getResourceURL(context, imageUrl);
            writer.writeAttribute("background", resourceUrl, null); //NOI18N
        }
        // </RAVE>
        // unload is a special case;
        String onUnload = body.getOnUnload();
        sb.setLength(0);
        if (onUnload != null) {
            sb.append(onUnload);
        }
        if (body.getFocusID(context) != null && !RenderingUtilities.isPortlet(context)) {
            if (onUnload != null) {
                sb.append("; ");
            }
            sb.append("return ");
            sb.append(body.getJavaScriptObjectName(context));
            sb.append(".setScrollPosition();");
        }
        if (sb.length() > 0) {
            writer.writeAttribute("onunload", sb.toString(), null); //NO18N
        }
        addIntegerAttributes(context, component, writer, integerAttributes);
        writer.write("\n"); //NOI18N
    }

    /**
     * <p>Render the appropriate element end, depending on whether the
     * <code>for</code> property is set or not.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component component to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderEnd(FacesContext context, UIComponent component, ResponseWriter writer) throws IOException {

        if (!RenderingUtilities.isPortlet(context)) {
            Body body = (Body) component;
            if (body.getFocusID(context) != null) {

                StringBuffer jsBuffer = new StringBuffer(128);
                jsBuffer.append("\n<script type=\"text/javascript\">\nvar ");
                jsBuffer.append(body.getJavaScriptObjectName(context));

                jsBuffer.append(" = new Body('");
                jsBuffer.append(body.getFocusID(context));
                jsBuffer.append("');");

                jsBuffer.append("\n</script>\n");
                writer.write(jsBuffer.toString());
            }
            writer.endElement("body"); //NOI18N
            writer.write("\n"); //NOI18N
        }
    }
}
