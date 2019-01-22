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
package org.netbeans.modules.vmd.midp.components.handlers;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.general.ClassSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import java.awt.*;

/**
 * 
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
                    return ImageUtilities.loadImage (DisplayableCD.ICON_PATH);
                case COLOR_32x32:
                    return ImageUtilities.loadImage (DisplayableCD.LARGE_ICON_PATH);
                default:
                    return null;
            }
        }

    }

}
