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

import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.displayables.ListCode;
import org.netbeans.modules.vmd.midp.flow.FlowEventHandlerPinBadgePresenter;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */
public class ListEventHandlerCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#ListEventHandler"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/list_action_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/list_action_32.png"; // NOI18N

    public static final String PROP_LIST = "list"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventHandlerCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return null;
    }

    public PaletteDescriptor getPaletteDescriptor () {
        return new PaletteDescriptor (MidpPaletteProvider.CATEGORY_PROCESS_FLOW, NbBundle.getMessage(ListEventHandlerCD.class, "DISP_ListEventHandler"), NbBundle.getMessage(ListEventHandlerCD.class, "TTIP_ListEventHandler"), ICON_PATH, LARGE_ICON_PATH); // NOI18N
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
                // info
                InfoPresenter.createStatic (NbBundle.getMessage(ListEventHandlerCD.class, "NAME_ListEventHandler"), NbBundle.getMessage(ListEventHandlerCD.class, "TYPE_Action"), ICON_PATH), // NOI18N
                // flow
                new FlowEventHandlerPinBadgePresenter (Utilities.loadImage (ICON_PATH), 0) {
                    protected DesignEventFilter getEventFilter () {
                        return super.getEventFilter ().addParentFilter (getComponent (), 2, false);
                    }
                },
                // code
                new CodeMultiGuardedLevelPresenter () {
                    protected void generateMultiGuardedSectionCode (MultiGuardedSection section) {
                        DesignComponent listEventHandler = getComponent ();

                        DesignComponent commandEventSource = listEventHandler.getParentComponent ();
                        if (commandEventSource == null)
                            return;

                        DesignComponent list = commandEventSource.getParentComponent ();
                        String code = ListCode.getListActionMethodAccessCode (list);
                        if (code != null)
                            section.getWriter ().write (code + " ();\n"); // NOI18N
                    }
                }
        );
    }

}
