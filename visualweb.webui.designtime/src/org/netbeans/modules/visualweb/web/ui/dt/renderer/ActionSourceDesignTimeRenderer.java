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

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;

/**
 * A delegating renderer for components that extend
 * {@link org.netbeans.modules.visualweb.web.ui.component.UICommand}, which does two things. If
 * the value property is not set, temporarily sets the value and style to
 * cause the component to be rendered with a default "shadow label". If the
 * value property is bound, but the value is null or the empty string,
 * temporarily sets the value to a "dummy string" that is keyed to the type
 * of the value.
 *
 * @author gjmurphy
 */
public abstract class ActionSourceDesignTimeRenderer extends AbstractDesignTimeRenderer {

    protected static String STYLE_CLASS_PROP = "styleClass"; //NOI18N
    protected static String VALUE_PROP = "value"; //NOI18N

    boolean isTextSet;
    boolean isStyleSet;

    public ActionSourceDesignTimeRenderer(Renderer renderer) {
        super(renderer);
    }

    /**
     * Returns a display string to set as the component's value if shadowed
     * text is required.
     */
    protected abstract String getShadowText();

    /**
     * Determines if shadowed text is required. Default implementation is
     * simply to check for a null value.
     */
    protected boolean needsShadowText(UICommand component) {
        return component.getValue() == null;
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (UICommand.class.isAssignableFrom(component.getClass())) {
            ValueBinding valueBinding = component.getValueBinding(VALUE_PROP); //NOI18N
            UICommand command = (UICommand) component;
            Object value = command.getValue();
            if (valueBinding != null && (value == null || value.toString().length() == 0)) {
                Object dummyValue = getDummyData(context, valueBinding);
                command.setValue(dummyValue);
                isTextSet = true;
            } else if (needsShadowText(command)) {
                command.setValue(getShadowText());
                String styleClass = (String) component.getAttributes().get(STYLE_CLASS_PROP);
                component.getAttributes().put(STYLE_CLASS_PROP, addStyleClass(styleClass, UNINITITIALIZED_STYLE_CLASS));
                isTextSet = true;
                isStyleSet = true;
            }
        }
        super.encodeBegin(context, component);
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        super.encodeEnd(context, component);
        if (isTextSet) {
            UICommand command = (UICommand)component;
            command.setValue(null);
            isTextSet = false;
        }
        if (isStyleSet) {
            String styleClass = (String) component.getAttributes().get(STYLE_CLASS_PROP);
            component.getAttributes().put(STYLE_CLASS_PROP, removeStyleClass(styleClass, UNINITITIALIZED_STYLE_CLASS));
            isStyleSet = false;
        }
    }

}
