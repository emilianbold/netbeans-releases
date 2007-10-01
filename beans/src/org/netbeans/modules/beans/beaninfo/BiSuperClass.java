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

package org.netbeans.modules.beans.beaninfo;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;

import org.netbeans.modules.beans.JMIUtils;
import org.netbeans.modules.beans.GenerateBeanException;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.netbeans.jmi.javamodel.*;

import javax.jmi.reflect.JmiException;

/** Singleton - utility class


 @author Petr Hrebejk
*/
final class BiSuperClass extends Object {


    /** Creates a ClassElement containing all methods from classElement and it's superclasses */

    static JavaClass createForClassElement( JavaClass classElement ) throws GenerateBeanException {
        assert JMIUtils.isInsideTrans();
        try {
            JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(classElement);
            JavaClass result = jmodel.getJavaClass().createJavaClass();

            result.setName( classElement.getName() );

            JavaClass ce = classElement;
            int methodsAdded = 0;           // Workaround for getMethd

            HashSet visited = new HashSet();
            while ( ce != null && visited.add(ce) && !"java.lang.Object".equals(ce.getName())) { // NOI18N
                List/*<Method>*/ methods = JMIUtils.getMethods(ce);
                for (Iterator it = methods.iterator(); it.hasNext();) {
                    Method method = (Method) it.next();
                    if ( ( method.getModifiers() & Modifier.PUBLIC ) == 0 )
                        continue;

                    if ( methodsAdded == 0 || result.getMethod( method.getName(), getParameterTypes(method), false ) == null ) {
                        Method methodToAdd = duplicateMethod(method, jmodel);
                        result.getFeatures().add(methodToAdd);
                        methodsAdded ++;
                    }
                }
            
                ce = ce.getSuperClass();
            }

            /*
            MethodElement[] methods = result.getMethods();
            for( int i = 0; i < methods.length; i++ ) 
              System.out.println ( methods[i].getName() );
            */
            return result;
        } catch (JmiException e) {
            throw new GenerateBeanException(e); 
        }
    }
    
    /**
     * makes shallow copy of method in particular extent.
     * @param orig original method
     * @param jmodel extent where the copy is created
     * @return new method
     * @throws JmiException
     */ 
    private static Method duplicateMethod(Method orig, JavaModelPackage jmodel) throws JmiException {
        Method m = jmodel.getMethod().createMethod(
                orig.getName(),
                null,
                orig.getModifiers(),
                null,
                null,
                null,
                null,
                duplicateTypeParameters(orig, jmodel),
                duplicateParameters(orig, jmodel),
                duplicateExceptionNames(orig, jmodel),
                null,
                orig.getDimCount()
                );
        m.setType(orig.getType());
        return m;
    }

    private static List duplicateTypeParameters(Method orig, JavaModelPackage jmodel) {
        List tparams = orig.getTypeParameters();
        if (tparams == null) {
            return null;
        }
        
        List duplicates = new ArrayList(tparams.size());
        for (Iterator it = tparams.iterator(); it.hasNext();) {
            TypeParameter typeParameter = (TypeParameter) it.next();
            TypeParameter duplicate = duplicateTypeParameter(typeParameter, jmodel);
            duplicates.add(duplicate);
        }
        return duplicates;
    }

    private static TypeParameter duplicateTypeParameter(TypeParameter typeParameter, JavaModelPackage jmodel) {
        TypeParameter duplicate = jmodel.getTypeParameter().createTypeParameter();
        duplicate.setName(typeParameter.getName());
        duplicate.setSuperClass(typeParameter.getSuperClass());
        List/*<JavaClass>*/ bounds = typeParameter.getInterfaces();
        List/*<JavaClass>*/ copyOfBounds = new ArrayList(bounds.size());
        for (Iterator it = bounds.iterator(); it.hasNext();) {
            Object bound = it.next();
            if (bound instanceof TypeParameter) {
                copyOfBounds.add(duplicateTypeParameter((TypeParameter) bound, jmodel));
            } else {
                copyOfBounds.add(bound);
            }
        }
        duplicate.getInterfaces().addAll(copyOfBounds);
        return duplicate;
    }

    /**
     * makes shallow copy of parameters in particular extent.
     * @param orig method with original list of parameters
     * @param jmodel extent where the copy is created
     * @return list of parameters
     * @throws JmiException
     */ 
    private static List duplicateParameters(Method orig, JavaModelPackage jmodel) throws JmiException {
        List parameters = orig.getParameters();
        if (parameters == null) {
            return null;
        }
        List duplicates = new ArrayList(parameters.size());
        for (Iterator it = parameters.iterator(); it.hasNext();) {
            Parameter parameter = (Parameter) it.next();
            Parameter duplicate = jmodel.getParameter().createParameter(
                    parameter.getName(),
                    null,
                    parameter.isFinal(),
                    null,
                    parameter.getDimCount(), 
                    parameter.isVarArg()
            );
            duplicate.setType(parameter.getType());
            duplicates.add(duplicate);
        }
        return duplicates;
    }
    
    private static List/*<Type>*/ getParameterTypes(Method method) throws JmiException {
        List/*<Parameter>*/ params = method.getParameters();
        if (params == null) {
            return Collections.EMPTY_LIST;
        }
        List types = new ArrayList(params.size());
        for (Iterator it = params.iterator(); it.hasNext();) {
            Parameter param = (Parameter) it.next();
            types.add(param.getType());
        }
        return types;
    }

    private static List/*<MultipartId>*/ duplicateExceptionNames(Method orig, JavaModelPackage jmodel) {
        List/*<JavaClass>*/ exIds = orig.getExceptions();
        
        List/*<MultipartId>*/ duplicates = new ArrayList/*<MultipartId>*/(exIds.size());
        for (Iterator it = exIds.iterator(); it.hasNext(); ) {
            JavaClass ex = (JavaClass) it.next();
            MultipartId duplicate = jmodel.getMultipartId().createMultipartId(ex.getName(), null, null);
            duplicates.add(duplicate);
        }
        return duplicates;
    }

}
