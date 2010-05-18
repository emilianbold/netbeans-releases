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
package org.netbeans.modules.visualweb.web.ui.dt.renderer;

import com.sun.rave.web.ui.component.Breadcrumbs;
import com.sun.rave.web.ui.component.Hyperlink;
import com.sun.rave.web.ui.component.ImageHyperlink;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import com.sun.rave.web.ui.renderer.BreadcrumbsRenderer;
import java.io.IOException;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

/**
 * A delegating renderer for {@link org.netbeans.modules.visualweb.web.ui.component.Breadcrumbs}. If
 * the breadcrumbs has no children hyperlinks, then a minimual block of markup
 * is output. If the breadcrummbs has children, any of them are missing both
 * text and content will have their text temporarily set to the display name
 * of the hyperlink component. If the breadcrumbs is bound to an array or list
 * of hyperlinks, and design-time evaluation of the bound property returns an
 * empty value, a default set of dummy hyperlinks is temporarily added to the
 * component while it renders.
 *
 * @author gjmurphy
 */
public class BreadcrumbsDesignTimeRenderer extends AbstractDesignTimeRenderer {

    static int DUMMY_PAGES_SET = 1;
    static int COMPONENT_SHADOWED = 2;
    static int LINK_CHILDREN_SHADOWED = 3;

    int rendererStatus;

    public BreadcrumbsDesignTimeRenderer() {
        super(new BreadcrumbsRenderer());
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        rendererStatus = 0;
        if (component instanceof Breadcrumbs) {
            if(component.getValueBinding("pages") != null) {
                ValueBinding pagesBinding = component.getValueBinding("pages");
                Object value = pagesBinding.getValue(context);
                if (value != null && value instanceof Hyperlink[] && ((Hyperlink[])value).length == 0) {
                    ((Breadcrumbs) component).setPages(getDummyHyperlinks());
                    rendererStatus = DUMMY_PAGES_SET;
                }
            } else if(component.getChildCount() == 0) {
                ResponseWriter writer = context.getResponseWriter();
                writer.startElement("div", component);
                String style = ((Breadcrumbs) component).getStyle();
                writer.writeAttribute("style", style, "style");
                writer.startElement("span", component);
                writer.writeAttribute("class", super.UNINITITIALIZED_STYLE_CLASS, "class");
                String label = DesignMessageUtil.getMessage(BreadcrumbsDesignTimeRenderer.class, "breadcrumbs.label");
                char[] chars = label.toCharArray();
                writer.writeText(chars, 0, chars.length);
                writer.endElement("span");
                writer.endElement("div");
                rendererStatus = COMPONENT_SHADOWED;
            } else {
                List children = component.getChildren();
                int i = children.size() - 1;
                if (Hyperlink.class.isAssignableFrom(children.get(i).getClass())) {
                    Hyperlink link = (Hyperlink)children.get(i);
                    if (link.getText() == null && link.getChildCount() == 0) {
                        if (link instanceof ImageHyperlink)
                            link.setText(DesignMessageUtil.getMessage(BreadcrumbsDesignTimeRenderer.class,
                                    "imageHyperlink.label"));
                        else
                            link.setText(DesignMessageUtil.getMessage(BreadcrumbsDesignTimeRenderer.class,
                                    "hyperlink.label"));
                        link.setStyleClass(addStyleClass(link.getStyleClass(), UNINITITIALIZED_STYLE_CLASS));
                        rendererStatus = LINK_CHILDREN_SHADOWED;
                    }
                }
            }
        }
        if (rendererStatus != COMPONENT_SHADOWED) {
            super.encodeBegin(context, component);
        }
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        if (rendererStatus != COMPONENT_SHADOWED) {
            super.encodeEnd(context, component);
        }
        if (rendererStatus == DUMMY_PAGES_SET) {
            ((Breadcrumbs) component).setPages(null);
        } else if (rendererStatus == LINK_CHILDREN_SHADOWED) {
            List children = component.getChildren();
            int i = children.size() - 1;
            Hyperlink link = (Hyperlink)children.get(i);
            link.setText(null);
            link.setStyleClass(removeStyleClass(link.getStyleClass(), UNINITITIALIZED_STYLE_CLASS));
        }
        rendererStatus = 0;
    }
    
    static Hyperlink[] dummyHyperlinks;
    
    static Hyperlink[] getDummyHyperlinks() {
        if (dummyHyperlinks == null) {
            Hyperlink dummyHyperlink = new Hyperlink();
            String dummyText = getDummyData(String.class).toString();
            dummyHyperlink.setText(dummyText);
            dummyHyperlinks = new Hyperlink[]{dummyHyperlink, dummyHyperlink, dummyHyperlink};
        }
        return dummyHyperlinks;
    }
    
}
