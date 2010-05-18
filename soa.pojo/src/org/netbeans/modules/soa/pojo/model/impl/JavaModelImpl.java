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

package org.netbeans.modules.soa.pojo.model.impl;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.soa.pojo.model.api.JavaModel;

/**
 * Java Model implementation
 * @author sgenipudi
 */
public class JavaModelImpl extends TreePathScanner<Void, Void> implements JavaModel{
    private CompilationController cc;
    private TypeElement classElem;
    
    public void setCompilationController(CompilationController cc) {
        this.cc = cc;
    }

    public List<TypeElement> getEnclosedTypes() {
        return Collections.unmodifiableList(ElementFilter.typesIn(classElem.getEnclosedElements()));
    }

    public TypeElement getEnclosedTypeByType(String type) {
        for (TypeElement t : getEnclosedTypes()) {
            if (t.getSimpleName().contentEquals(type)) {
                return t;
            }
        }
        return null;
    }
    
    public String getQualifiedName() {
       return  classElem.getQualifiedName().toString();
    }

    public List<ExecutableElement> getMethods() {
        return Collections.unmodifiableList(ElementFilter.methodsIn(classElem.getEnclosedElements()));
    }

    public List<ExecutableElement> getMethodsByName(String name) {
        List<ExecutableElement> result = new ArrayList<ExecutableElement> ();
        for (ExecutableElement e : getMethods()) {
            if (e.getSimpleName().contentEquals(name)) {
                result.add(e);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<VariableElement> getVariables() {
        return Collections.unmodifiableList(ElementFilter.fieldsIn(classElem.getEnclosedElements()));
    }

    public List<VariableElement> getVariablesByName(String name) {
        List<VariableElement> result = new ArrayList<VariableElement> ();
        for (VariableElement e : getVariables()) {
            if (e.getSimpleName().contentEquals(name)) {
                result.add(e);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public boolean isContainerManaged() {
        Types types = cc.getTypes();
        Elements elements = cc.getElements();
        TypeMirror annoType = elements.getTypeElement("javax.ejb.TransactionManagement").asType();
        for (AnnotationMirror anno : classElem.getAnnotationMirrors()) {
            if (types.isSameType(anno.getAnnotationType(), annoType)) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> map = elements.getElementValuesWithDefaults(anno);
                for (AnnotationValue annoValue : map.values()) {
                    if (annoValue != null && annoValue.getValue().toString().equals("BEAN")) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public CompilationInfo getCompilationInfo() {
        return cc;
    }


    @Override
    public Void visitClass(ClassTree t, Void v) {
        // only visit outter-most class, do not call super.visitClass() method
        Element elem = cc.getTrees().getElement(getCurrentPath());
        if (elem != null) {
            classElem = (TypeElement) elem;
        }
        return null;
    }

}
