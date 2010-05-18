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
package org.netbeans.modules.visualweb.faces.dt.renderer;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.el.EvaluationException;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;

/**
 * Base class for design-time renderers that may delegate most rendering
 * operations to their corresponding run-time renderer.
 *
 * @author gjmurphy
 */
public class AbstractDesignTimeRenderer extends Renderer {

    protected static String UNINITITIALIZED_STYLE_CLASS = "rave-uninitialized-text";
    protected static String BORDER_STYLE_CLASS = "rave-design-border";

    // Delagatee renderer
    protected Renderer renderer;

    public AbstractDesignTimeRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
            throws ConverterException {
        return this.renderer.getConvertedValue(context, component, submittedValue);
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        this.renderer.encodeEnd(context, component);
    }

    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        this.renderer.encodeChildren(context, component);
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        this.renderer.encodeBegin(context, component);
    }

    public void decode(FacesContext context, UIComponent component) {
        this.renderer.decode(context, component);
    }

    public String convertClientId(FacesContext context, String clientId) {
        return this.renderer.convertClientId(context, clientId);
    }

    public boolean getRendersChildren() {
        return this.renderer.getRendersChildren();
    }

    protected static String addStyleClass(String value, String styleClass) {
        if (value == null) {
            return styleClass;
        } else if (value.indexOf(styleClass) >= 0) {
            return value;
        } else {
            return value + " " + styleClass;
        }
    }

    protected static String removeStyleClass(String value, String styleClass) {
        if (value == null || value.indexOf(styleClass) == -1)
            return value;
        int i = value.indexOf(styleClass);
        while (i > 0 && Character.isSpaceChar(value.charAt(i)))
            i--;
        return value.substring(0, i) + value.substring(i + styleClass.length());
    }

    protected static Object getDummyData(FacesContext context, ValueBinding vb) {
        Class type = null;
        try {
            type = vb.getType(context);
        } catch (EvaluationException e) {
            // FIXME - workaround for [6371691] that should be reconsidered,
            // as swallowing exceptions is not a good general practice
            type = Object.class;
        }
        return getDummyData(type);
    }

    protected static Object getDummyData(Class clazz) {
        if (clazz.equals(Object.class))
            return null;
        return com.sun.data.provider.impl.AbstractDataProvider.getFakeData(clazz);
    }

}
