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

import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.points.CallPointCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventHandlerEdgePresenter;
import org.netbeans.modules.vmd.midp.general.AbstractEventHandlerCreatorPresenter;
import org.openide.util.NbBundle;

import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */

public final class CallPointEventHandlerCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#CallPointEventHandler"); // NOI18N

    public static final String PROP_CALL_POINT = "callPoint"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventHandlerCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList(
            new PropertyDescriptor (CallPointEventHandlerCD.PROP_CALL_POINT, CallPointCD.TYPEID, PropertyValue.createNull (), false, false, MidpVersionable.MIDP)
        );
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.createStatic (NbBundle.getMessage(CallPointEventHandlerCD.class, "NAME_CallPointEventHandler"), NbBundle.getMessage(CallPointEventHandlerCD.class, "TYPE_Action"), CallPointCD.ICON_PATH), // NOI18N
            // flow
            new FlowEventHandlerEdgePresenter () {
                protected DesignComponent getTargetComponent () {
                    return getComponent ().readProperty (CallPointEventHandlerCD.PROP_CALL_POINT).getComponent ();
                }
            },
            // code
            new CodeMultiGuardedLevelPresenter() {
                protected void generateMultiGuardedSectionCode (MultiGuardedSection section) {
                    DesignComponent callPointComponent = getComponent ().readProperty (PROP_CALL_POINT).getComponent ();
                    MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), callPointComponent.readProperty (CallPointCD.PROP_ACCESS_CODE));
                    section.getWriter ().write ("\n"); // NOI18N
                }
            },
                
            // delete
            DeleteDependencyPresenter.createDependentOnPropertyPresenter (PROP_CALL_POINT)
        );
    }

    public static AbstractEventHandlerCreatorPresenter createCallPointEventHandlerCreatorPresenter () {
        return new AbstractEventHandlerCreatorPresenter() {
            public DesignComponent createReuseEventHandler (DesignComponent eventSource, DesignComponent currentEventHandler, DesignComponent targetComponent) {
                if (currentEventHandler == null || ! getComponent ().getDocument ().getDescriptorRegistry ().isInHierarchy (CallPointEventHandlerCD.TYPEID, currentEventHandler.getType ()))
                    currentEventHandler = getComponent ().getDocument ().createComponent (CallPointEventHandlerCD.TYPEID);
                currentEventHandler.writeProperty (CallPointEventHandlerCD.PROP_CALL_POINT, PropertyValue.createComponentReference (targetComponent));
                return currentEventHandler;
            }
        };
    }

}
