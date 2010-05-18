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
package org.netbeans.modules.soa.pojo.util;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;

/**
 *
 * @author sgenipudi
 */
public class MethodExistenceCheckUtil {

    private String mSearchMethodName = null;
    private String methodArgument = null;
    private boolean bAlreadyContainsPOJO = false;
    private boolean bAlreadyContainsOperation = false;
    private boolean bAlreadyContainsMethod = false;
    private static int methodIndex = 1;
    private Map<String, ExecutableElement> mMapMethSignToExecElem = new HashMap<String, ExecutableElement>();
    

    static {
        methodIndex++;
    }

    public MethodExistenceCheckUtil(JavaSource javaSource, String searchMethod, String methodArgument, boolean bReadAllMethods) {
        this.mSearchMethodName = null;
        this.methodArgument = null;
        if ( bReadAllMethods) {
            try {
                javaSource.runUserActionTask(new CancellableTask<CompilationController>() {

                    public void cancel() {
                    }

                    public void run(CompilationController parameter) throws IOException {
                        parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                        new MemberVisitor(parameter).scan(parameter.getCompilationUnit(), null);
                    }
                }, true);
            } catch (Exception e1) {
            }
        }

        this.mSearchMethodName = searchMethod;
        this.methodArgument = methodArgument;

        try {
            javaSource.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(CompilationController parameter) throws IOException {
                    parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                    new MemberVisitor(parameter).scan(parameter.getCompilationUnit(), null);
                }
            }, true);
        } catch (Exception e1) {
        }

    }

    /**
     *  Private class implements TreePathScanner to prescan the code 
     *  for existence of Annotation.
     */
    private class MemberVisitor extends TreePathScanner<Void, Void> {
        //Compilation Info.
        private CompilationInfo info;

        /**
         * Constructor
         * @param info
         */
        public MemberVisitor(CompilationInfo info) {
            this.info = info;
        }

        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());

            if (el == null) {
                System.err.println("Cannot resolve class!");
            } else {
                TypeElement te = (TypeElement) el;
                System.err.println("Resolved class: " + te.getQualifiedName().toString());
                //XXX: only as an example, uses toString on element, which should be used only for debugging
                System.err.println("enclosed methods: " + ElementFilter.methodsIn(te.getEnclosedElements()));
                List<ExecutableElement> methodList = ElementFilter.methodsIn(te.getEnclosedElements());

                System.err.println("enclosed types: " + ElementFilter.typesIn(te.getEnclosedElements()));
                boolean methodIsStatic = false;
                try {
                    Annotation ann = te.getAnnotation((Class<org.glassfish.openesb.pojose.api.annotation.POJO>) Class.forName(GeneratorUtil.POJO_QUAL_CLASS_ANNOTATION));
                    if (ann != null) {
                        bAlreadyContainsPOJO = true;
                    } else {
                        ann = te.getAnnotation((Class<org.glassfish.openesb.pojose.api.annotation.Provider>) Class.forName(GeneratorUtil.PROVIDER_QUAL_CLASS_ANNOTATION));
                        if (ann != null) {
                            bAlreadyContainsPOJO = true;
                        }
                    }

                    if (methodList != null && methodList.size() > 0) {
                        for (ExecutableElement method : methodList) {
                            methodIsStatic = false;
                            if (mSearchMethodName == null) {
                                Set<Modifier> methodModifierSet = method.getModifiers();
                                if ( methodModifierSet != null) {
                                    Iterator<Modifier> modItr = methodModifierSet.iterator();
                                    if ( modItr != null ) {

                                        while( modItr.hasNext()) {
                                            Modifier methodMod = modItr.next();
                                            //Check if the method is static. If so skip it.
                                            if ( methodMod.equals(Modifier.STATIC)) {
                                                   methodIsStatic = true;
                                                   break;
                                            }
                                        }
                                    }
                                }
                                if ( methodIsStatic) {
                                    continue;
                                }
                                mMapMethSignToExecElem.put(method.toString(), method);
                            } else {
                                Annotation opnAnn = method.getAnnotation((Class<org.glassfish.openesb.pojose.api.annotation.Operation>) Class.forName(GeneratorUtil.POJO_QUAL_OPERATION_ANNOTATION_CLASS));
                                if (opnAnn != null) {
                                    bAlreadyContainsOperation = true;
                                    break;
                                }
                                if (method.getSimpleName().contentEquals(mSearchMethodName)) {
                                    List<? extends VariableElement> paramElems = method.getParameters();
                                    if (methodArgument != null) {
                                        if (paramElems != null && paramElems.size() > 0) {
                                            Set<TypeElement> typeElemSet = ElementFilter.typesIn(Collections.singleton(paramElems.get(0)));
                                            if (typeElemSet.iterator().next().getQualifiedName().contentEquals(methodArgument)) {
                                                bAlreadyContainsMethod = true;
                                                break;
                                            }
                                        }
                                    } else {
                                        bAlreadyContainsMethod = true;
                                    }
                                }
                            }




                        }
                    }

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

            }
            return null;
        }
    }

    public boolean containsMethod() {
        return this.bAlreadyContainsMethod;
    }

    public boolean containsPOJO() {
        return this.bAlreadyContainsPOJO;
    }

    public boolean containsOperation() {
        return this.bAlreadyContainsOperation;
    }

    public Map<String, ExecutableElement> getMethodList() {
       return mMapMethSignToExecElem;

    }

    public String recommendedMethodName(String defaultMethodName) {
        return (this.mSearchMethodName != null ? this.mSearchMethodName + System.currentTimeMillis() + this.methodIndex : defaultMethodName);
    }
}
