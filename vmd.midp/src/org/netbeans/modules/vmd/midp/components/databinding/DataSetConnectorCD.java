/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;

/**
 *
 * @author Karol Harezlak
 */
public class DataSetConnectorCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "#DataSetConnector"); //NOI18N

    public static final String PROP_COMPONENT_ID = "referencedComponent"; //NOI18N
    public static final String PROP_EXPRESSION_READ = "expressionRead"; //NOI18N
    public static final String PROP_EXPRESSION_WRITE = "expressionWrite"; // NOI18N
    public static final String PROP_UPDATE_COMMAND = "updateCommand"; //NOI18N
    public static final String PROP_BINDED_PROPERTY = "property";//NOI18N
    public static final String PROP_NEXT_COMMAND = "nextCommand"; //NOI18N
    public static final String PROP_PREVIOUS_COMMAND = "previousCommand"; //NOI18N
    //public static final String PROP_INDEX_NAME = "indexName"; //NOI18N //TODO Remove!!
    public static final String PROP_INDEX = "index"; //NOI18N

    
    @Override
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(null, TYPEID, true, true);
    }

    @Override
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_COMPONENT_ID, MidpTypes.TYPEID_LONG, PropertyValue.createNull(), true, false, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_EXPRESSION_READ, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, false, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_EXPRESSION_WRITE, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, false, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_UPDATE_COMMAND, CommandCD.TYPEID, PropertyValue.createNull(), true, false, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_BINDED_PROPERTY, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, false, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_NEXT_COMMAND, CommandCD.TYPEID, PropertyValue.createNull(), true, false, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_PREVIOUS_COMMAND, CommandCD.TYPEID, PropertyValue.createNull(), true, false, MidpVersionable.MIDP_2),
                //new PropertyDescriptor(PROP_INDEX_NAME, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, false, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_INDEX, IndexableDataSetIndexCD.TYPEID, PropertyValue.createNull(), true, false, MidpVersionable.MIDP_2) 
        );
    }

    @Override
    protected List<? extends Presenter> createPresenters() {
        return null;
    }
}
