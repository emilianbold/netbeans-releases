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

import com.sun.rave.web.ui.component.ImageHyperlink;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import com.sun.rave.web.ui.renderer.ImageHyperlinkRenderer;
import javax.faces.component.UICommand;

/**
 * A delegating renderer for {@link org.netbeans.modules.visualweb.web.ui.component.ImageHyperlink} that
 * sets a default text property when the property is null.
 *
 * @author gjmurphy
 */
public class ImageHyperlinkDesignTimeRenderer extends ActionSourceDesignTimeRenderer {

    public ImageHyperlinkDesignTimeRenderer() {
        super(new ImageHyperlinkRenderer());
    }

    protected String getShadowText() {
        return DesignMessageUtil.getMessage(ImageHyperlinkDesignTimeRenderer.class, "imageHyperlink.label");
    }

    protected boolean needsShadowText(UICommand component) {
        ImageHyperlink imageHyperlink = (ImageHyperlink) component;
        return imageHyperlink.getValue() == null && imageHyperlink.getIcon() == null
                && imageHyperlink.getImageURL() == null && imageHyperlink.getChildCount() == 0;
    }

}
