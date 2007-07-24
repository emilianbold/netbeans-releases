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
 */
package org.netbeans.modules.vmd.midp.components.handlers;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.general.ClassSupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class EventHandlerSupport {

    private static final InfoPresenter.Resolver SWITCH_DISPLAYABLE_EVENT_HANDLER_INFO_RESOLVER = new SwitchDisplayableResolver ();

    public static InfoPresenter.Resolver getSwitchDisplayableEventHandlerInfoResolver () {
        return SWITCH_DISPLAYABLE_EVENT_HANDLER_INFO_RESOLVER;
    }

    private static class SwitchDisplayableResolver implements InfoPresenter.Resolver {

        public DesignEventFilter getEventFilter (DesignComponent component) {
            return new DesignEventFilter ().addDescentFilter (component, SwitchDisplayableEventHandlerCD.PROP_DISPLAYABLE).addDescentFilter (component, SwitchDisplayableEventHandlerCD.PROP_ALERT);
        }

        public String getDisplayName (DesignComponent component, InfoPresenter.NameType nameType) {
            switch (nameType) {
                case PRIMARY:
                    DesignComponent displayable = component.readProperty (SwitchDisplayableEventHandlerCD.PROP_DISPLAYABLE).getComponent ();
                    DesignComponent alert = component.readProperty (SwitchDisplayableEventHandlerCD.PROP_ALERT).getComponent ();
                    if (displayable == null)
                        return NbBundle.getMessage(EventHandlerSupport.class, "DISP_Handler_Clear_Display"); // NOI18N

                    String displayableName = ClassSupport.resolveDisplayName (displayable);
                    String alertName = alert != null ? ClassSupport.resolveDisplayName (alert) : null;
                    if (alertName != null)
                        return NbBundle.getMessage(EventHandlerSupport.class, "DISP_Handler_Go_to_displayable_alert", displayableName, alertName); // NOI18N
                    else
                        return NbBundle.getMessage(EventHandlerSupport.class, "DISP_Handler_Go_to_displayable", displayableName); // NOI18N
                case SECONDARY:
                    return NbBundle.getMessage(EventHandlerSupport.class, "TYPE_Action"); // NOI18N
                case TERTIARY:
                    return null;
                default:
                    throw Debug.illegalState ();
            }
        }

        public boolean isEditable (DesignComponent component) {
            return false;
        }

        public String getEditableName (DesignComponent component) {
            return null;
        }

        public void setEditableName (DesignComponent component, String enteredName) {
        }

        public Image getIcon (DesignComponent component, InfoPresenter.IconType iconType) {
            switch (iconType) {
                case COLOR_16x16:
                    return Utilities.loadImage (DisplayableCD.ICON_PATH);
                case COLOR_32x32:
                    return Utilities.loadImage (DisplayableCD.LARGE_ICON_PATH);
                default:
                    return null;
            }
        }

    }

}
