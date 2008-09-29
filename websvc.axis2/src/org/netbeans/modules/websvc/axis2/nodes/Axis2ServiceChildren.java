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
package org.netbeans.modules.websvc.axis2.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.Action;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.websvc.axis2.actions.TestOperationAction;
import org.netbeans.modules.websvc.axis2.config.model.Service;
import org.netbeans.modules.websvc.axis2.java.SourceUtils;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

public class Axis2ServiceChildren extends Children.Keys<OperationInfo> {
    private java.awt.Image cachedIcon;
    private FileChangeListener fcl;
    private Service service;
    private FileObject implClass;
    private FileObject srcRoot;
    
    public Axis2ServiceChildren(Service service, FileObject srcRoot) {
        this.service=service;
        this.srcRoot = srcRoot;
    }
    
    @Override
    protected void addNotify() {
        getImplClass();
        assert(implClass != null);
        if (fcl == null) {
            fcl = new FileChangeAdapter() {
                @Override
                public void fileChanged(FileEvent fe) {
                    updateKeys();
                }
            };
            implClass.addFileChangeListener(fcl);
        };
        updateKeys();
    }
    
    @Override
    protected void removeNotify() {
        getImplClass();
        assert(implClass != null);
        if (fcl != null) {
            implClass.removeFileChangeListener(fcl);
            fcl = null;
        }
    }
    
    @SuppressWarnings("unchecked")  
    private void updateKeys() {
        //List<OperationInfo> keys = new ArrayList<OperationInfo>();
        final List<OperationInfo>[] keys = new List[]{new ArrayList<OperationInfo>()};
        if (implClass != null) {
            JavaSource javaSource = JavaSource.forFileObject(implClass);
            if (javaSource!=null) {
                CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                        if (typeElement!=null) {
                            // find WS operations
                            // either annotated (@WebMethod) public mathods 
                            // or all public methods
                            List<ExecutableElement> publicMethods = getPublicMethods(controller, typeElement);
                            //List<ExecutableElement> webMethods = new ArrayList<ExecutableElement>();
                            List<OperationInfo> wsOperations = new ArrayList<OperationInfo>();


                            // create list of operations;                                      
                            for (ExecutableElement method:publicMethods) {
                                // web operation name
                                OperationInfo wsOperation = new OperationInfo();

                                if (wsOperation.getOperationName() == null) {
                                    wsOperation.setOperationName(method.getSimpleName().toString());
                                }

                                // return type
                                TypeMirror returnType = method.getReturnType();
                                if (returnType.getKind() == TypeKind.DECLARED) {
                                    TypeElement element = (TypeElement)((DeclaredType)returnType).asElement();
                                    wsOperation.setReturnType(element.getQualifiedName().toString());
                                } else { // for primitive type
                                    wsOperation.setReturnType(returnType.toString());
                                }                                               

                                // parameter types
                                List<? extends VariableElement> params = method.getParameters();
                                List<String> paramTypes = new ArrayList<String>();
                                List<String> paramNames = new ArrayList<String>();
                                for (VariableElement param:params) {
                                    TypeMirror type = param.asType();
                                    paramNames.add(param.getSimpleName().toString());
                                    if (type.getKind() == TypeKind.DECLARED) {
                                        TypeElement element = (TypeElement)((DeclaredType)type).asElement();
                                        paramTypes.add(element.getQualifiedName().toString());
                                    } else { // for primitive type
                                        paramTypes.add(type.toString());
                                    }
                                }
                                wsOperation.setParamTypes(paramTypes);
                                wsOperation.setParamNames(paramNames);
                                wsOperations.add(wsOperation);
                            }
                            keys[0] = wsOperations;
                        }
                    }

                    public void cancel() {}
                };
                try {
                    javaSource.runUserActionTask(task, true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        setKeys(keys[0]);
    }

    protected Node[] createNodes(OperationInfo key) {
        return new Node[] {new OperationNode(service, key)};
    }
    
    private List<ExecutableElement> getPublicMethods(CompilationController controller, TypeElement classElement) throws IOException {
        List<? extends Element> members = classElement.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn(members);
        List<ExecutableElement> publicMethods = new ArrayList<ExecutableElement>();
        for (ExecutableElement method:methods) {
            Set<Modifier> modifiers = method.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC)) {
                publicMethods.add(method);
            }
        }
        return publicMethods;
    }
        
    private FileObject getImplClass() {
        if (implClass == null) {
            implClass = srcRoot.getFileObject(service.getServiceClass().replace('.', '/')+".java"); //NOI18N
        }
        return implClass;
    }
    
    
    private class OperationNode extends AbstractNode {
        private OperationInfo operation;
        private Service service;
        private static final String OPERATION_ICON = "org/netbeans/modules/websvc/axis2/resources/wsoperation.png"; //NOI18N
        
        OperationNode(Service service, OperationInfo operation) {
            super(Children.LEAF, Lookups.fixed(service, operation));
            this.operation = operation;
            this.service = service;
        }
        
        @java.lang.Override
        public java.awt.Image getIcon(int type) {
            if (cachedIcon == null) {
                cachedIcon = ImageUtilities.loadImage(OPERATION_ICON);
            }
            return cachedIcon;
        }
        @Override
        public String getName() {
            return operation.getOperationName();
        }
        @Override
        public String getDisplayName() {
            return operation.getOperationName()+": "+getClassName(operation.getReturnType()); //NOI18N
        }
        
        private String getClassName(String fullClassName) {
            StringTokenizer tok = new StringTokenizer(fullClassName,"."); //NOI18N
            String token = ""; //NOI18N
            while (tok.hasMoreTokens()) {
                token = tok.nextToken();
            }
            return token;
        }
        // Create the popup menu:
        @Override
        public Action[] getActions(boolean context) {
            return new SystemAction[] {
                SystemAction.get(TestOperationAction.class),
                null,
                SystemAction.get(PropertiesAction.class),
            };
        }
    }

}
