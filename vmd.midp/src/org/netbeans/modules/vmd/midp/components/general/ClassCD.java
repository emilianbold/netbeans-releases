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
package org.netbeans.modules.vmd.midp.components.general;

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.actions.GoToSourcePresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorInstanceName;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.codegen.ModelUpdatePresenter;
import org.netbeans.modules.vmd.midp.components.general.ClassCode.CodeClassComponentDependencyPresenter;

/**
 * @author David Kaspar
 */

public final class ClassCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#Class"); // NOI18N

    public static final String PROP_INSTANCE_NAME = "instanceName"; // NOI18N
    public static final String PROP_LAZY_INIT = "lazyInit";  // NOI18N
    public static final String PROP_CODE_GENERATED = "codeGenerated";  // NOI18N

    static {
        MidpTypes.registerIcon (TYPEID, null); // TODO - use an empty icon here
    }

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (null, TYPEID, false, true);
    }

    @Override
    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_INSTANCE_NAME, 
                InstanceNameResolver.createFromSuggested (component,
                ClassCode.getSuggestedMainName (component.getType ())));
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
       return Arrays.asList(
            new PropertyDescriptor (PROP_INSTANCE_NAME,
                MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull (),
                false, false, Versionable.FOREVER),
            new PropertyDescriptor (PROP_LAZY_INIT, MidpTypes.TYPEID_BOOLEAN, 
            MidpTypes.createBooleanValue (Boolean.TRUE), false, false,
                Versionable.FOREVER),
            new PropertyDescriptor (PROP_CODE_GENERATED,
                MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue (Boolean.FALSE),
                false, false, Versionable.FOREVER)
       );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES)
                .addProperty(NbBundle.getMessage(ClassCD.class, "DISP_Class_Instance_Name"),
                    PropertyEditorInstanceName.createInstance(TYPEID), PROP_INSTANCE_NAME) // NOI18N
                    .addProperty(NbBundle.getMessage(ClassCD.class,
                "DISP_Class_Is_Lazy_Initialized"), PropertyEditorBooleanUC.createInstance(),
                    PROP_LAZY_INIT); // NOI18N
                
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        MidpActionsSupport.addCommonClassActionsPresenters (presenters, true,
                true, true, true, true);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            ClassSupport.createInfoPresenter (),
            // general
            new GoToSourcePresenter () {
                protected boolean matches (GuardedSection section) {
                    boolean lazyInit = MidpTypes.getBoolean (getComponent ().readProperty (PROP_LAZY_INIT));
                    return MultiGuardedSection.matches(section, lazyInit ? getComponent().getComponentID() + "-getter" : getComponent ().getDocument ().getRootComponent ().getComponentID () + "-initialize", 1); // NOI18N
                }
            },
            // properties
            createPropertiesPresenter(),
            // codegen
            new ClassCode.ClassCodeReferencePresenter (),
            new ClassCode.CodeLazyInitPresenter (),
            new ClassCode.CodeClassNamePresenter (),
            new ClassCode.GeneratedCodePresenter(),
            new CodeClassComponentDependencyPresenter()
        );
    }

}
