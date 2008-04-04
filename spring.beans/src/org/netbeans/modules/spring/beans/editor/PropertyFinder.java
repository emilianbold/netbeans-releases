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

package org.netbeans.modules.spring.beans.editor;

import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementUtilities;

/**
 * Finds all simple bean properties starting with a specified prefix on the specified type
 * 
 * XXX: Should be in a separate utils package and not in editor package. 
 * 
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class PropertyFinder {
    
    private static final String GET_PREFIX = "get"; // NOI18N
    private static final String SET_PREFIX = "set"; // NOI18N
    private static final String IS_PREFIX = "is"; // NOI18N
    
    private ElementUtilities eu;
    private TypeMirror type;
    private String propPrefix;
    private Map<String, Property> name2Prop = new HashMap<String, Property>();

    public PropertyFinder(TypeMirror type, String propertyName, ElementUtilities eu) {
        this.type = type;
        this.propPrefix = propertyName;
        this.eu = eu;
    }

    public Property[] findProperties() {
        eu.getMembers(type, new ElementUtilities.ElementAcceptor() {
            public boolean accept(Element e, TypeMirror type) {

                // only accept methods
                if (e.getKind() != ElementKind.METHOD) {
                    return false;
                }

                ExecutableElement ee = (ExecutableElement) e;
                String methodName = ee.getSimpleName().toString();
                TypeMirror retType = ee.getReturnType();
                
                // discard private and static methods
                if (ee.getModifiers().contains(Modifier.PRIVATE) || ee.getModifiers().contains(Modifier.STATIC)) {
                    return false;
                }
                
                // only accept getXXX and isXXX (for boolean)
                if (isGetter(methodName, ee, retType)) {
                    String propName = getPropertyName(methodName);
                    if(!propName.startsWith(propPrefix)) {
                        return false;
                    }
                    
                    addPropertyGetter(propName, ee);
                    return true;
                }
                
                // only accept setXXX
                if (isSetter(methodName, ee, retType)) {
                    String propName = getPropertyName(methodName);
                    if(!propName.startsWith(propPrefix)) {
                        return false;
                    }
                    
                    addPropertySetter(propName, ee);
                    return true;
                }

                return false;
            }
        });
        
        return name2Prop.values().toArray(new Property[0]);
    }
    
    private void addPropertySetter(String propName, ExecutableElement setter) {
        Property prop = getProperty(propName);
        prop.setSetter(setter);
    }
    
    private void addPropertyGetter(String propName, ExecutableElement getter) {
        Property prop = getProperty(propName);
        prop.setGetter(getter);
    }
    
    private Property getProperty(String propName) {
        Property prop = name2Prop.get(propName);
        if(prop == null) {
            prop = new Property(propName);
            name2Prop.put(propName, prop);
        }
        
        return prop;
    }
    
    private String getPropertyName(String methodName) {
        if(methodName.startsWith(GET_PREFIX) || methodName.startsWith(SET_PREFIX)) {
            return convertToPropertyName(methodName.substring(3));
        } else if(methodName.startsWith(IS_PREFIX)) {
            return convertToPropertyName(methodName.substring(2));
        }
        
        return null;
    }
    
    private String convertToPropertyName(String name) {
        char[] vals = name.toCharArray();
        vals[0] = Character.toLowerCase(vals[0]);
        return String.valueOf(vals);
    }
    
    private boolean isGetter(String methodName, ExecutableElement ee, TypeMirror retType) {
        boolean retVal = methodName.startsWith(GET_PREFIX) && methodName.length() > GET_PREFIX.length() && retType.getKind() != TypeKind.VOID;
        retVal = retVal || methodName.startsWith(IS_PREFIX) && methodName.length() > IS_PREFIX.length() && retType.getKind() == TypeKind.BOOLEAN;
        
        return retVal;
    }
    
    private boolean isSetter(String methodName, ExecutableElement ee, TypeMirror retType) {
        return methodName.startsWith(SET_PREFIX) && methodName.length() > SET_PREFIX.length() 
                && retType.getKind() == TypeKind.VOID && ee.getParameters().size() == 1;
    }
}
