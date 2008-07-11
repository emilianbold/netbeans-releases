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
import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;

/**
 *
 * @author Karol Harezlak
 */
public class ContactsDataSetCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.databinding.pim.ContactsDataSet"); //NOI18N

    @Override
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(IndexableDataAbstractSetCD.TYPEID, TYPEID, true, true);
    }

    @Override
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    @Override
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return null;
    }
    
    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter().addParameters(MidpParameter.create()).addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP));

    }

    @Override
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            createSetterPresenter()
        );
    }
    
    //Runtime parameters
//    public static final String NAME                 = "name";
//    public static final String ADDRESS              = "address";
//    public static final String EMAIL                = "email";
//    public static final String FORMATTED_NAME       = "formatted_name";
//    public static final String NICKNAME             = "nickname";
//    public static final String NOTE                 = "note";
//    public static final String ORGANIZATION         = "organization";
//    public static final String TELEPHONE            = "telephone";
//    public static final String TITLE                = "title";
//    public static final String UID                  = "uid";
//    public static final String URL                  = "url";
//    public static final String BIRTHDAY             = "birthday";
//    public static final String REVISION             = "revision";
//    public static final String PHOTO                = "photo";
//    public static final String PUBLIC_KEY           = "public_key";    
//    public static final String PHOTO_URL            = "photo_url";
//    public static final String PUBLIC_KEY_STRING    = "public_key_string";        
//    public static final String CLASS                = "class";
    
}
