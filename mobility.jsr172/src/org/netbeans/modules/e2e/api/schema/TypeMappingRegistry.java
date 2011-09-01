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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.e2e.api.schema;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public class TypeMappingRegistry {
    
    private SchemaHolder schemaHolder;
    private Map<QName, TypeMapping> mappings;
    
    /** Creates a new instance of TypeMappingRegistry */
    public TypeMappingRegistry(SchemaHolder schemaHolder) {
        this.schemaHolder = schemaHolder;
        mappings = new HashMap();

        for (Element e : schemaHolder.getSchemaElements()) {
            mapType(e);
        }

    }

    public TypeMapping getType(QName name) {
        return mappings.get(name);
    }

    private void mapType(Element e) {
        Type type = e.getType();
        if (Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
            mappings.put(e.getName(), new TypeMappingImpl(type, e.getMaxOccurs() > 1, e.isNillable()));
        } else if (Type.FLAVOR_SEQUENCE == type.getFlavor()) {
            if (type.getSubconstructs().isEmpty()) {
                System.err.print("void"); // NOI18N
            } else if (type.getSubconstructs().size() == 1) {
                SchemaConstruct sc = type.getSubconstructs().get(0);
                if (SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                    Element sce = (Element) sc;
                    if (Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
                        System.err.print(sce.getType().getJavaTypeName());
                    } else {
                        System.err.print(sce.getName().getLocalPart());
                    }
                }
            }
        }
    }

    private static final class TypeMappingImpl implements TypeMapping {

        private String name;
        private boolean array;
        private boolean primitive;
        private boolean nillable;
        private Type type;

        public TypeMappingImpl(Type type, boolean array, boolean nillable) {
            this.type = type;
            this.array = array;
            this.nillable = nillable;
        }

//    if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
//        System.err.print( type.getJavaTypeName());
//    } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
//        if( type.getSubconstructs().size() == 0 ) {
//            System.err.print( "void" );
//        } else if( type.getSubconstructs().size() == 1 ) {
//            SchemaConstruct sc = type.getSubconstructs().get( 0 );
//            if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
//                Element sce = (Element)sc;
//                if( Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
//                    System.err.print( sce.getType().getJavaTypeName());
//                } else {
//                    System.err.print( sce.getName().getLocalPart());
//                }
//            }
//        }
//    }
        
    }
}
