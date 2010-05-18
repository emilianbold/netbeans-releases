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
package org.netbeans.modules.vmd.midp.components.resources;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorImage;
import org.openide.util.NbBundle;
import java.util.Arrays;
import java.util.List;


/**
 * @author David Kaspar
 */

public final class ImageCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Image"); // NOI18N

    public static final String PROP_RESOURCE_PATH = "resourcePath";  // NOI18N
    // TODO - should be inlined to all CDs directly and should not be here
    public static final String PROP_IMAGE = "image";  // NOI18N

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor (ResourceCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList (
                new PropertyDescriptor(PROP_RESOURCE_PATH, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), false, true, MidpVersionable.MIDP)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty(NbBundle.getMessage(ImageCD.class, "DISP_Image_Resource_Path"), PropertyEditorImage.createInstance(), PROP_RESOURCE_PATH); // NOI18N
    }

    private static Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
                .addParameters (new ImageResourceParameter ())
                .addParameters (new NullImageResourceParameter ())
                .addSetters (MidpSetter.createFactoryMethod (TYPEID, null, "Image", "createImage", MidpVersionable.MIDP).addParameters (ImageResourceParameter.PARAM_RESOURCE_PATH).addExceptions ("java.io.IOException")) // NOI18N
                .addSetters (MidpSetter.createFactoryMethod (TYPEID, null, "Image", "createImage", MidpVersionable.MIDP).addParameters (NullImageResourceParameter.PARAM_NULL_RESOURCE_PATH)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter ()
        );
    }

    private static class ImageResourceParameter extends MidpParameter {

        static final String PARAM_RESOURCE_PATH = "resourcePath"; // NOI18N

        protected ImageResourceParameter () {
            super (PARAM_RESOURCE_PATH);
        }

        @Override
        public boolean isRequiredToBeSet (DesignComponent component) {
            return component.readProperty (PROP_RESOURCE_PATH).getKind () != PropertyValue.Kind.NULL;
        }

    }

    private static class NullImageResourceParameter extends MidpParameter {

        static final String PARAM_NULL_RESOURCE_PATH = "nullResourcePath"; // NOI18N

        protected NullImageResourceParameter () {
            super (PARAM_NULL_RESOURCE_PATH);
        }

        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            section.getWriter ().write ("1, 1"); // NOI18N
        }

        @Override
        public boolean isRequiredToBeSet (DesignComponent component) {
            return component.readProperty (PROP_RESOURCE_PATH).getKind () == PropertyValue.Kind.NULL;
        }

    }

}
