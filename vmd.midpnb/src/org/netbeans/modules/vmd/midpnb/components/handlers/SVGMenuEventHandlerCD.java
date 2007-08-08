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

package org.netbeans.modules.vmd.midpnb.components.handlers;

import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.handlers.EventHandlerCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventHandlerPinBadgePresenter;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.openide.util.Utilities;
import java.util.Arrays;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class SVGMenuEventHandlerCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#SVGMenuEventHandler"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_menu_action_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_menu_action_32.png"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventHandlerCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return null;
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
                // info
                InfoPresenter.createStatic (NbBundle.getMessage(SVGMenuEventHandlerCD.class, "DISP_Process_SVG_Menu"), NbBundle.getMessage(SVGMenuEventHandlerCD.class, "TYPE_Action"), ICON_PATH), // NOI18N
                // flow
                new FlowEventHandlerPinBadgePresenter (Utilities.loadImage (ICON_PATH), 0) {
                    @Override
                    protected DesignEventFilter getEventFilter () {
                        return super.getEventFilter ().addParentFilter (getComponent (), 2, false);
                    }
                },
                // code
                new CodeMultiGuardedLevelPresenter () {
                    protected void generateMultiGuardedSectionCode (MultiGuardedSection section) {
                        DesignComponent eventHandler = getComponent ();

                        DesignComponent commandEventSource = eventHandler.getParentComponent ();
                        if (commandEventSource == null)
                            return;

                        DesignComponent menu = commandEventSource.getParentComponent ();
                        String code = MidpCustomCodePresenterSupport.getSVGMenuActionMethodAccessCode (menu);
                        if (code != null)
                            section.getWriter ().write (code + " ();\n"); // NOI18N
                    }
                }
        
        );
    }

}
