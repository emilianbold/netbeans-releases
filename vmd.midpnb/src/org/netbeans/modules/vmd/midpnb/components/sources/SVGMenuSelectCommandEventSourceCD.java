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
package org.netbeans.modules.vmd.midpnb.components.sources;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Anton Chechel
 */
public class SVGMenuSelectCommandEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#SVGMenuSelectCommandEventSource"); // NOI18N

    public static final String PROP_SHOW_SELECT_COMMAND = "showSelectCommand"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (CommandEventSourceCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
                new PropertyDescriptor (PROP_SHOW_SELECT_COMMAND, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue (false), false, false, MidpVersionable.MIDP_2)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter ()
            .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_PROPERTIES)
            .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES) // TODO - its is not a code property
                .addProperty (NbBundle.getMessage(SVGMenuSelectCommandEventSourceCD.class, "DISP_SVGMenuSelectCommandEventSource_ShowSelectCommand"), // NOI18N
                    PropertyEditorBooleanUC.createInstance(NbBundle.getMessage(SVGMenuSelectCommandEventSourceCD.class,
                        "LBL_SVGMenuSelectCommandEventSource_ShowSelectCommand")), PROP_SHOW_SELECT_COMMAND); // NOI18N
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, InfoPresenter.class);
        DocumentSupport.removePresentersOfClass (presenters, CommandEventSourceCD.CommandEventSourceFlowPinPresenter.class);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.createStatic ("SVGMenu.SELECT_COMMAND", "Command", CommandCD.ICON_PATH), // NOI18N
            // flow
            new CommandEventSourceCD.CommandEventSourceFlowPinPresenter () {
                @Override
                protected DesignComponent getComponentForAttachingPin () {
                    if (! MidpTypes.getBoolean (getComponent ().readProperty (PROP_SHOW_SELECT_COMMAND)))
                        return null;
                    return super.getComponentForAttachingPin ();
                }
            },
            // properties
            createPropertiesPresenter (),
            // delete
            DeletePresenter.createUserIndeliblePresenter ()
        
        );
    }

}
