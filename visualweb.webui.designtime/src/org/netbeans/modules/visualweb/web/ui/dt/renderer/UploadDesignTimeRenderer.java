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

import com.sun.rave.web.ui.component.Field;
import com.sun.rave.web.ui.renderer.UploadRenderer;
import javax.faces.context.FacesContext;

/**
 * A delegating renderer for the {@link org.netbeans.modules.visualweb.web.ui.component.Upload}
 * component.
 *
 * @author gjmurphy
 */
public class UploadDesignTimeRenderer extends FieldDesignTimeRenderer {

    public UploadDesignTimeRenderer() {
        super(new UploadRenderer());
   }

    protected String getShadowText(FacesContext context, Field field) {
        return field.getValueAsString(context);
    }

}
