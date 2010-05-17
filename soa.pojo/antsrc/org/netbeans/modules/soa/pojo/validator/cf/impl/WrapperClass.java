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

package org.netbeans.modules.soa.pojo.validator.cf.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.glassfish.openesb.pojose.core.anno.processor.ProxyClass;
import org.glassfish.openesb.pojose.core.anno.processor.ProxyMethod;
import org.glassfish.openesb.pojose.core.anno.processor.ProxyPOJOAnnotation;
import org.glassfish.openesb.pojose.core.anno.processor.ProxyProviderAnnotation;
import org.netbeans.modules.classfile.Annotation;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.Method;

/**
 *
 * @author gpatil
 */
public class WrapperClass implements ProxyClass{
    private ClassFile cf = null;
    private List<ProxyMethod> ms = null;
    private static String ANNO_POJO = "Lorg/glassfish/openesb/pojose/api/annotation/POJO;" ;  // No I18N
    private static String ANNO_Provider = "Lorg/glassfish/openesb/pojose/api/annotation/Provider;" ;  // No I18N
//    private static String ANNO_POJOResource = "Lorg/glassfish/openesb/pojose/api/annotation/POJOResource;" ;  // No I18N
//    private static String ANNO_Endpoint = "Lorg/glassfish/openesb/pojose/api/annotation/Endpoint;" ;  // No I18N
    
    public WrapperClass(ClassFile cf){
        this.cf = cf;
    }
    
    public ProxyPOJOAnnotation getAnnotationPOJO() {
        Annotation anno = cf.getAnnotation(ClassName.getClassName(ANNO_POJO));
        if (anno != null){
            return new WrapperPOJO(anno);
        }
        return null;
    }

    public ProxyProviderAnnotation getAnnotationProvider() {
        Annotation anno = cf.getAnnotation(ClassName.getClassName(ANNO_Provider));
        if (anno != null){
            return new WrapperProvider(anno);
        }
        return null;
    }

    public List<ProxyMethod> getMethods() {
        Collection<Method> mc = cf.getMethods();
        if (mc != null){
            ms = new ArrayList<ProxyMethod>();
            for (Method m: mc){
                ms.add(new WrapperMethod(m));
            }
        }
        return ms;
    }

    public String getName() {
        return this.cf.getName().getExternalName();
    }
    
    // REMOVE
    public ClassFile getCf(){
        return this.cf;
    }
}
