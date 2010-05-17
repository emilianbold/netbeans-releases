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
package org.netbeans.modules.vmd.midpnb.components.commands;


import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.CodeNamePresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Karol Harezlak
 */

public final class WaitScreenFailureCommandCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#WaitScreenFailureCommand"); // NOI18N

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(CommandCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public void postInitialize(DesignComponent component) {
        component.writeProperty(CommandCD.PROP_LABEL, MidpTypes.createStringValue ("Failure")); // NOI18N
        component.writeProperty(CommandCD.PROP_TYPE, MidpTypes.createIntegerValue (CommandCD.VALUE_OK));
        component.writeProperty(CommandCD.PROP_PRIORITY, MidpTypes.createIntegerValue (0));
        component.writeProperty(CommandCD.PROP_ORDINARY, MidpTypes.createBooleanValue (false));
        component.writeProperty (ClassCD.PROP_INSTANCE_NAME, PropertyValue.createNull ());
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return null;
    }

    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        presenters.clear();
        super.gatherPresenters(presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // general
            InfoPresenter.createStatic("FAILURE_COMMAND", "Command", "WaitScreen.FAILURE_COMMAND", CommandCD.ICON_PATH), // NOI18N
            // code
            new CodeReferencePresenter() {
                protected String generateAccessCode() { return generateDirectAccessCode (); }
                protected String generateDirectAccessCode() { return "WaitScreen.FAILURE_COMMAND"; } // NOI18N
                protected String generateTypeCode() { return null; }
            },
            CodeNamePresenter.fixed (),
            // delete
            DeletePresenter.createIndeliblePresenter()
        );
    }

}
