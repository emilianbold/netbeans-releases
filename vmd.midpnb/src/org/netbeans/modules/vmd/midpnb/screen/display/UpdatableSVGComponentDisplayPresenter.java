/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.screen.display;

import javax.microedition.m2g.SVGImage;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGComponentCD;

/**
 *
 * @author akorostelev
 */
public abstract class UpdatableSVGComponentDisplayPresenter extends SVGComponentDisplayPresenter{

    protected static final String TRAIT_X = "x";                 // NOI18N
    protected static final String TRAIT_Y = "y";                 // NOI18N
    protected static final String TRAIT_VISIBILITY = "visibility";        // NOI18N
    protected static final String TRAIT_FILL = "fill";              // NOI18N
    protected static final String TRAIT_TEXT = "#text";             // NOI18N

    protected static final String TR_VALUE_VISIBLE = "visible";           // NOI18N
    protected static final String TR_VALUE_HIDDEN = "hidden";            // NOI18N
    protected static final String TR_VALUE_INHERIT = "inherit";           // NOI18N
    
    protected static final String DASH = "_";                 // NOI18N

    /**
     * Reloads svg component. Is used to show current property values.
     * @param svgImage current SVG Image
     * @param svgComponent currect svg component
     * @param componentId currect svg component id
     */
    protected abstract void reloadSVGComponent(SVGImage svgImage,
            DesignComponent svgComponent, String componentId);

    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);

        DesignComponent svgComponent = getSvgComponent();
        if (svgComponent == null) {
            return;
        }
        String id = (String) svgComponent.readProperty(SVGComponentCD.PROP_ID).getPrimitiveValue();
        if (id == null) {
            return;
        }
        SVGImage svgImage = getSvgImage();
        if (svgImage == null) {
            return;
        }
        reloadSVGComponent(svgImage, svgComponent, id);
    }
}
