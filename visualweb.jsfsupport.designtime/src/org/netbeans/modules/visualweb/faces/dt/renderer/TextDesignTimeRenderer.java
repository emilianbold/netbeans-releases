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

import com.sun.faces.renderkit.html_basic.TextRenderer;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * A delegating renderer for {@link javax.faces.component.UIOutput}. If there
 * is no value, a "shadow" value is set.
 *
 * @author gjmurphy
 */

public class TextDesignTimeRenderer extends AbstractDesignTimeRenderer {

    static ComponentBundle bundle = ComponentBundle.getBundle(TextDesignTimeRenderer.class);

    public TextDesignTimeRenderer() {
        super(new TextRenderer());
    }

    boolean shadowValueSet;

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        shadowValueSet = false;
        if (component instanceof UIOutput) {
            Object value = ((UIOutput) component).getValue();
            if (!(component instanceof UIInput) && value == null) {
                Map attributesMap = component.getAttributes();
                String styleClass = (String) attributesMap.get("styleClass"); //NOI18N
                attributesMap.put("styleClass", addStyleClass(styleClass, UNINITITIALIZED_STYLE_CLASS)); //NOI18N
                ((UIOutput) component).setValue(bundle.getMessage("Text.default.label")); //NOI18N
                shadowValueSet = true;
            }
        }
        this.renderer.encodeBegin(context, component);
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        this.renderer.encodeEnd(context, component);
        if (shadowValueSet) {
            Map attributesMap = component.getAttributes();
            String styleClass = removeStyleClass((String) attributesMap.get("styleClass"), UNINITITIALIZED_STYLE_CLASS); //NOI18N
            if (styleClass == null || styleClass.length() == 0)
                attributesMap.put("styleClass", null); //NOI18N
            else
                attributesMap.put("styleClass", styleClass); //NOI18N
            ((UIOutput) component).setValue(null);
        }
    }

}
