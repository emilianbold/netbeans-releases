/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midp.components.databinding;

import org.netbeans.modules.vmd.midp.components.*;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderComponentPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.components.categories.DatabindingCategoryCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.inspector.controllers.InspectorPositionControllerSupport;


/**
 *
 * @author Karol Harezlak
 */
public class DataSetAbstractCD extends ComponentDescriptor {
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/dataset_16.gif"; // NOI18N
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "#DataSetAbstract"); //NOI18N
    
    public static final String PROP_READ_ONLY = "readOnly"; //NOI18N
    
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
   
    @Override
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ClassCD.TYPEID, TYPEID, false, true);
    }

    @Override
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    @Override
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_READ_ONLY, MidpTypes.TYPEID_BOOLEAN, PropertyValue.createNull(), false, false, MidpVersionable.MIDP_2)
        );
    }

    @Override
    protected List<? extends Presenter> createPresenters() {
        
        return Arrays.asList(
                // code
                MidpCodePresenterSupport.createAddImportPresenter("org.netbeans.microedition.databinding.DataBinder"), //NOI18N
                //inspector
                new InspectorFolderComponentPresenter(true),
                InspectorPositionPresenter.create(InspectorPositionControllerSupport.createHierarchical(DatabindingCategoryCD.TYPEID)));   
    }
}
