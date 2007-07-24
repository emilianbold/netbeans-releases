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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
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
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import java.awt.*;

/**
 *
 * @author Anton Chechel
 */
public class ItemSupport {
    
    private static final Resolver SVG_MENU_ELEMENT_RESOLVER = new ElementResolver(SVGMenuElementEventSourceCD.PROP_STRING, SVGMenuElementEventSourceCD.ICON_PATH);
    
    public static Resolver createSVGMenuElementInfoResolver () {
        return SVG_MENU_ELEMENT_RESOLVER;
    }
   
    private static class ElementResolver implements InfoPresenter.Resolver {

        private String propertyName;
        private Image icon;
        
        private ElementResolver(String propertyName, String iconResource) {
            this.propertyName = propertyName;
            this.icon = Utilities.loadImage (iconResource);
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
            if (value.getKind() == PropertyValue.Kind.VALUE)
                return MidpTypes.getString(value);
            else
                return ""; // NOI18N
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
