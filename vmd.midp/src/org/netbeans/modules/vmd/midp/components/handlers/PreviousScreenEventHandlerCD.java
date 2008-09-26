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
package org.netbeans.modules.vmd.midp.components.handlers;

import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventHandlerPinBadgePresenter;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */
public class PreviousScreenEventHandlerCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#PreviousScreenEventHandler"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/previous_screen_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/previous_screen_32.png"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventHandlerCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return null;
    }

    @Override
    public PaletteDescriptor getPaletteDescriptor () {
        return new PaletteDescriptor (MidpPaletteProvider.CATEGORY_PROCESS_FLOW, NbBundle.getMessage(PreviousScreenEventHandlerCD.class, "DISP_PreviousScreenEventHandler"), NbBundle.getMessage(PreviousScreenEventHandlerCD.class, "TTIP_PreviousScreenEventHandler"), ICON_PATH, LARGE_ICON_PATH); // NOI18N
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.createStatic (NbBundle.getMessage(PreviousScreenEventHandlerCD.class, "NAME_PreviousScreenEventHandler"), NbBundle.getMessage(PreviousScreenEventHandlerCD.class, "TYPE_Action"), DisplayableCD.ICON_PATH), // NOI18N
            // flow
            new FlowEventHandlerPinBadgePresenter (ImageUtilities.loadImage (ICON_PATH), 0),
            // code
            new CodeMultiGuardedLevelPresenter () {
                protected void generateMultiGuardedSectionCode (MultiGuardedSection section) {
                    section.getWriter ().write ("switchToPreviousDisplayable ();\n"); // NOI18N
                }
            }
        );
    }

    // TODO - this should be used only them dropping an edge to a target node that is kind of PreviousScreenControllerCD
//    public static AbstractEventHandlerCreatorPresenter createPreviousScreenEventHandlerCreatorPresenter () {
//        return new AbstractEventHandlerCreatorPresenter() {
//            public DesignComponent createReuseEventHandler (DesignComponent eventHandler, DesignComponent eventSource, DesignComponent targetComponent) {
//                if (eventHandler == null || ! getComponent ().getDocument ().getDescriptorRegistry ().isComponentDescriptorCompatibleWithTypeID (PreviousScreenEventHandlerCD.TYPEID, eventHandler.getComponentDescriptor ()))
//                    eventHandler = getComponent ().getDocument ().createComponent (PSEventHandlerCD.TYPEID);
//                eventHandler.writeProperty (ExitMidletEventHandlerCD.PROP_MOBILE_DEVICE, PropertyValue.createComponentReference (targetComponent));
//                return eventHandler;
//            }
//        };
//    }

}
