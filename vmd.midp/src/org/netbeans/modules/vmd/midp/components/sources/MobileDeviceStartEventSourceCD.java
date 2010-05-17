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
package org.netbeans.modules.vmd.midp.components.sources;

import org.netbeans.modules.vmd.api.codegen.CodeClassLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeNamePresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.points.MobileDeviceCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;
import org.netbeans.modules.vmd.midp.flow.FlowMobileDevicePinOrderPresenter;
import org.netbeans.modules.vmd.midp.actions.GoToSourcePresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.api.editor.guards.GuardedSection;
import org.openide.util.NbBundle;

import javax.swing.text.StyledDocument;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public final class MobileDeviceStartEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#MobileDeviceStartEventSource"); // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventSourceCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return null;
    }

    public static DesignComponent getMobileDeviceComponent (DesignComponent mobileDeviceStartEventSourceComponent) {
        return mobileDeviceStartEventSourceComponent.getParentComponent ();
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        MidpActionsSupport.addCommonActionsPresenters (presenters, false, true, false, false, false);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.createStatic (NbBundle.getMessage (MobileDeviceStartEventSourceCD.class, "DISP_MIDletStarted"), NbBundle.getMessage (MobileDeviceStartEventSourceCD.class, "DISP_Event"), MobileDeviceCD.ICON_PATH), // NOI18N
            // general
            new GoToSourcePresenter () {
                protected boolean matches (GuardedSection section) {
                    return MultiGuardedSection.matches (section, getComponent ().getComponentID () + "-startMIDlet", 0); // NOI18N
                }
            },
            // flow
            new FlowEventSourcePinPresenter () {
                protected DesignComponent getComponentForAttachingPin () {
                    return getMobileDeviceComponent (getComponent ());
                }
                protected String getDisplayName () {
                    return NbBundle.getMessage (MobileDeviceStartEventSourceCD.class, "DISP_FlowPin_MIDletStarted"); // NOI18N
                }
                protected String getOrder () {
                    return FlowMobileDevicePinOrderPresenter.CATEGORY_ID;
                }
            },
            // code
            new CodeClassLevelPresenter.Adapter() {
                @Override
                protected void generateClassBodyCode (StyledDocument document) {
                    MultiGuardedSection section = MultiGuardedSection.create (document, getComponent ().getComponentID () + "-startMIDlet"); // NOI18N

                    section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Method: startMIDlet \">\n"); // NOI18N
                    section.getWriter ().write ("/**\n * Performs an action assigned to the Mobile Device - MIDlet Started point.\n */\n"); // NOI18N
                    section.getWriter ().write ("public void startMIDlet () {\n"); // NOI18N

                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, getComponent ());

                    assert section.isGuarded ();
                    section.getWriter ().write ("}\n"); // NOI18N
                    section.getWriter ().write ("//</editor-fold>\n").commit (); // NOI18N

                    section.close ();
                }
            },
            CodeNamePresenter.fixed ("startMIDlet"), // NOI18N
            // delete
            DeletePresenter.createIndeliblePresenter (),
            DeleteDependencyPresenter.createDependentOnParentComponentPresenter ()
        
        );
    }

}
