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
package org.netbeans.modules.visualweb.web.ui.dt.renderer;

import com.sun.rave.web.ui.component.Alert;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import com.sun.rave.web.ui.renderer.AlertRenderer;
import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * A delegating renderer for {@link org.netbeans.modules.visualweb.web.ui.component.Alert} that
 * sets a default summary property when the property is null.
 *
 * @author gjmurphy
 */
public class AlertDesignTimeRenderer extends AbstractDesignTimeRenderer {

    boolean isTextDefaulted;

    public AlertDesignTimeRenderer() {
        super(new AlertRenderer());
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (component instanceof Alert) {
            Alert alert = (Alert)component;
            if (alert.getSummary() == null || alert.getSummary().length() == 0) {
                alert.setSummary(DesignMessageUtil.getMessage(AlertDesignTimeRenderer.class, "alert.summary"));
                alert.setStyleClass(addStyleClass(alert.getStyleClass(), UNINITITIALIZED_STYLE_CLASS));
                isTextDefaulted = true;
            }
        }
        super.encodeBegin(context, component);
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        super.encodeEnd(context, component);
        if (component instanceof Alert && isTextDefaulted) {
            Alert alert = (Alert)component;
            alert.setSummary(null);
            alert.setStyleClass(removeStyleClass(alert.getStyleClass(), UNINITITIALIZED_STYLE_CLASS));
            isTextDefaulted = false;
        }
    }

}
