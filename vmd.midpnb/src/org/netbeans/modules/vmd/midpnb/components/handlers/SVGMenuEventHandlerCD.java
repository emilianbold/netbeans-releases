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

package org.netbeans.modules.vmd.midpnb.components.handlers;

import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.handlers.EventHandlerCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventHandlerPinBadgePresenter;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.openide.util.ImageUtilities;
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
                new FlowEventHandlerPinBadgePresenter (ImageUtilities.loadImage (ICON_PATH), 0) {
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
