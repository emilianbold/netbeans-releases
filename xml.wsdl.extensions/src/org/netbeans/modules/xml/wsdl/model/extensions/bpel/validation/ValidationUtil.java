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
package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.Attribute;

public final class ValidationUtil {
    private static final String SCHEMA_COMPONENT_ATTRIBUTE_BASE = "base"; // NOI18N
    private static final String SCHEMA_COMPONENT_ATTRIBUTE_NAME = "name"; // NOI18N
    private static final String SCHEMA_COMPONENT_ATTRIBUTE_TYPE = "type"; // NOI18N

    public static Collection<GlobalSimpleType> BUILT_IN_SIMPLE_TYPES = SchemaModelFactory.getDefault().getPrimitiveTypesModel().getSchema().getSimpleTypes();

    private ValidationUtil() {}
    
    // vlv 
    public static Component getBasedSimpleType(Component component) {
      if ( !(component instanceof SchemaComponent)) {
        return component;
      }
      SchemaComponent type = getBuiltInSimpleType((SchemaComponent) component);

      if (type == null) {
        return component;
      }
      return type;
    }

    public static String getTypeName(SchemaComponent component) {
      if (component == null) {
        return "n/a"; // NOI18N
      }
      if (component instanceof Named) {
        return ((Named) component).getName();
      }
      return component.toString();
    }

    public static Attribute attributeName() {
        return new GenericExtensibilityElement.StringAttribute(SCHEMA_COMPONENT_ATTRIBUTE_NAME);
    }
    
    public static Attribute attributeType() {
        return new GenericExtensibilityElement.StringAttribute(SCHEMA_COMPONENT_ATTRIBUTE_TYPE);
    }
    
    public static GlobalSimpleType getBuiltInSimpleType(SchemaComponent schemaComponent) {
//System.out.println();
//System.out.println();
//System.out.println("--------------------");
//System.out.println("BUILT IN SIMPLE TYPE: " + getTypeName(schemaComponent));
        if (schemaComponent == null) {
            return null;
        }
        if (isBuiltInSimpleType(schemaComponent)) {
//System.out.println("  !!!!! SIMPLE !!!!!!!!");
            return (GlobalSimpleType) schemaComponent;
        }
        String baseTypeName = schemaComponent.getAnyAttribute(new QName(SCHEMA_COMPONENT_ATTRIBUTE_BASE));
        GlobalSimpleType globalSimpleType = null;

//System.out.println("baseTypeName: " + baseTypeName);
        // # 130281
        Collection<GlobalSimpleType> schemaSimpleTypes = schemaComponent.getModel().getSchema().getSimpleTypes();
        List<GlobalSimpleType> simpleTypes = new LinkedList<GlobalSimpleType>();

        simpleTypes.addAll(schemaSimpleTypes);
        simpleTypes.addAll(BUILT_IN_SIMPLE_TYPES);

//System.out.println("Simple Types:");
//System.out.println("" + simpleTypes);
//System.out.println();

        if (baseTypeName != null) {
            baseTypeName = ignoreNamespace(baseTypeName);
            globalSimpleType = findGlobalSimpleType(baseTypeName, simpleTypes);

            if (globalSimpleType != null) {
                // # 130281
                return getBuiltInSimpleType(globalSimpleType);
            }
        }
//System.out.println("getSchemaComponentTypeName: " + getSchemaComponentTypeName(schemaComponent));
        // # 130281
        globalSimpleType = findGlobalSimpleType(getSchemaComponentTypeName(schemaComponent), simpleTypes);
//System.out.println("globalSimpleType: " + globalSimpleType);

        if (globalSimpleType != null) {
            for (SchemaComponent childComponent : globalSimpleType.getChildren()) {
                globalSimpleType = getBuiltInSimpleType(childComponent);
               
                if (globalSimpleType != null) {
                    return globalSimpleType;
                }
            }
            return null;
        }
//System.out.println();
        // # 130281
        for (SchemaComponent child : schemaComponent.getChildren()) {
//System.out.println("  child: " + child.getClass().getName());
          if (child instanceof SimpleContent) {
//System.out.println("        getLocalDefinition: " + ((SimpleContent) child).getLocalDefinition());
              globalSimpleType = getBuiltInSimpleType(((SimpleContent) child).getLocalDefinition());

              if (globalSimpleType != null) {
                  return globalSimpleType;
              }
          }
        }
        return null;
    }

    public static boolean isBuiltInSimpleType(SchemaComponent component) {
        if ( !(component instanceof GlobalSimpleType)) {
            return false;
        }
        return findGlobalSimpleType(getSchemaComponentTypeName(component), BUILT_IN_SIMPLE_TYPES) != null;
    }

    public static GlobalSimpleType findGlobalSimpleType(String typeName, Collection<GlobalSimpleType> globalSimpleTypes) {
        if (typeName != null && globalSimpleTypes != null) {
            for (GlobalSimpleType globalSimpleType : globalSimpleTypes) {
                if (ignoreNamespace(globalSimpleType.toString()).equals(ignoreNamespace(typeName))) {
                    return globalSimpleType;
                }
            }
        }
        return null;
    }

    private static String getSchemaComponentTypeName(SchemaComponent schemaComponent) {
        String typeName = null;

        if ((schemaComponent instanceof SimpleType) || (schemaComponent instanceof ComplexType)) {
            typeName = schemaComponent.getAttribute(attributeName());
        }
        else {
            NamedComponentReference<? extends GlobalType> typeRef = getSchemaComponentTypeRef(schemaComponent);
        
            if (typeRef != null) {
                typeName = typeRef.get().getName();
            }
            else {
                typeName = ((SchemaComponent) schemaComponent).getAttribute(attributeType());
            }
        }
        return typeName;
    }
    
    private static NamedComponentReference<? extends GlobalType> getSchemaComponentTypeRef(SchemaComponent schemaComponent) {
        NamedComponentReference<? extends GlobalType> typeRef = null;
        try {
            typeRef = ((TypeContainer) schemaComponent).getType();
        } 
        catch (Exception e) {
        }
        return typeRef;
    }
    
    public static String ignoreNamespace(String dataWithNamespace) {
        if (dataWithNamespace == null) {
          return null;
        }
        int index = dataWithNamespace.indexOf(":");

        if ((index > -1) && (index < dataWithNamespace.length() - 1)) {
            return dataWithNamespace.substring(index + 1);
        }
        return dataWithNamespace;
    }
}
