/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.midpnb.components.items;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter.IconType;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter.NameType;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter.Resolver;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGMenuElementEventSourceCD;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import java.awt.*;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGComponentCD;

/**
 *
 * @author Anton Chechel
 */
public class ItemSupport {
    
    private static final Resolver SVG_MENU_ELEMENT_RESOLVER = new ElementResolver(SVGMenuElementEventSourceCD.PROP_STRING, SVGMenuElementEventSourceCD.ICON_PATH);
    //private static final Resolver SVG_FORM_ELEMENT_RESOLVER = new ElementResolver(SVGComponentCD.PROP_ID, "");
    
    public static Resolver createSVGMenuElementInfoResolver () {
        return SVG_MENU_ELEMENT_RESOLVER;
    }
   
//    public static Resolver createSVGFormElementInfoResolver () {
//        return SVG_FORM_ELEMENT_RESOLVER;
//    }
   
    private static class ElementResolver implements InfoPresenter.Resolver {

        private String propertyName;
        private Image icon;
        
        private ElementResolver(String propertyName, String iconResource) {
            this.propertyName = propertyName;
            this.icon = ImageUtilities.loadImage (iconResource);
        }
        
        public DesignEventFilter getEventFilter(DesignComponent component) {
            return new DesignEventFilter ().addComponentFilter (component, false);
        }
        
        public String getDisplayName(DesignComponent component, NameType nameType) {
            switch (nameType) {
                case PRIMARY:
                    return resolveName (component);
                case SECONDARY:
                    return NbBundle.getMessage(ItemSupport.class, "TYPE_Element"); // NOI18N
                case TERTIARY:
                    return null;
                default:
                    throw Debug.illegalState ();
            }
        }
        
        private String resolveName (DesignComponent component) {
            return MidpValueSupport.getHumanReadableString(component.readProperty(propertyName));
        }
        
        public boolean isEditable(DesignComponent component) {
            return true;
        }
        
        public String getEditableName(DesignComponent component) {
            PropertyValue value = component.readProperty(propertyName);
            if (value.getKind() == PropertyValue.Kind.VALUE) {
                return MidpTypes.getString(value);
            } else {
                return ""; // NOI18N
            }
        }
        
        public void setEditableName(DesignComponent component, String enteredName) {
            assert enteredName != null;
            component.writeProperty(propertyName, MidpTypes.createStringValue(enteredName));
        }
        
        public Image getIcon(DesignComponent component, IconType iconType) {
            return iconType == IconType.COLOR_16x16 ? icon : null;
        }

    }
    
}
