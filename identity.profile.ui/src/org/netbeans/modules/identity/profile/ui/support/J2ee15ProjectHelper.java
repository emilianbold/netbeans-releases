/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.identity.profile.ui.support;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author PeterLiu
 */
public class J2ee15ProjectHelper extends J2eeProjectHelper {
    
    private static final String SERVICE_SUFFIX = "Service";     //NOI18N
    
    private static final String WEB_FOLDER = "web";     //NOI18N
      
    private static final String JAVA_EXT = "java";       //NOI18N

    private static final String CONF_FOLDER = "conf";       //NOI18N
 
    private static final String TEST_FOLDER = "test";       //NOI18N
    
    private String portComponentName;
    private String serviceDescriptionName;
    private List<String> serviceRefNames;
    private List<ServiceRef> serviceRefs;
    
    
    /** Creates a new instance of J2ee15ProjectHelper */
    protected J2ee15ProjectHelper(Node node, JaxWsModel model) {
        super(node, model);
    }
    
    
//    public String getPortComponentName() {
//        if (portComponentName == null) {
//            JavaSource source = JavaSource.forFileObject(getJavaSource());
//            
//            if (source != null)
//                portComponentName = getClassName(source);
//        }
//        
//        //System.out.println("J2ee15ProjectHelper.portComponentName = " + portComponentName);
//        return portComponentName;
//    }
//    
//    public String getServiceDescriptionName() {
//        if (serviceDescriptionName == null) {
//            JavaSource source = JavaSource.forFileObject(getJavaSource());
//            
//            if (source != null) {
//                serviceDescriptionName = getServiceName(source);
//                
//                if (serviceDescriptionName == null) {
//                    serviceDescriptionName = getPortComponentName() + SERVICE_SUFFIX;
//                }
//            }
//        }
//        
//        //System.out.println("J2ee15ProjectHelper.serviceDescriptionName = " + serviceDescriptionName);
//        return serviceDescriptionName;
//    }
    
    public List<String> getAllServiceRefNames() {
        if (serviceRefNames == null) {
            serviceRefNames = new ArrayList<String>();
            serviceRefs = new ArrayList<ServiceRef>();
            List<ServiceRef> refs = getServiceRefsFromSources();
            String wsdlUri = getClient().getWsdlUrl();
            
            for (ServiceRef ref : refs) {
                if (ref.getWsdlLocation().equals(wsdlUri)) {
                    serviceRefNames.add(ref.getName());
                    serviceRefs.add(ref);
                }
            }
        }
        
        return serviceRefNames;
    }
  
    private String getClassName(JavaSource source) {
        final String[] className = new String[1];
        
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    ClassTree tree = getTopLevelClassTree(controller);
                    className[0] = tree.getSimpleName().toString();
                }
            }, true);
        } catch (IOException ex) {
            
        }
        
        return className[0];
    }
    
    private String getServiceName(JavaSource source) {
        final String[] serviceName = new String[1];
        
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement classElement = getTopLevelClassElement(controller);
                    
                    if (classElement == null) {
                        //System.out.println("Cannot resolve class!");
                    } else {
                        List<? extends AnnotationMirror> annotations =
                                controller.getElements().getAllAnnotationMirrors(classElement);
                        
                        for (AnnotationMirror annotation : annotations) {
                            if (annotation.toString().startsWith("@javax.jws.WebService")) {    //NOI18N
                                
                                Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
                                
                                for (ExecutableElement key : values.keySet()) {
                                    if (key.getSimpleName().toString().equals("serviceName")) { //NOI18N
                                        String name = values.get(key).toString();
                                        serviceName[0] =  name.replace("\"", "");               //NOI18N
                                        
                                        return;
                                    }
                                    
                                }
                                break;
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            
        }
        
        return serviceName[0];
    }
    
    private List<ServiceRef> getServiceRefsFromSources() {
        FileObject[] sourceRoots = getProvider().getSourceRoots();
        List<ServiceRef> refs = new ArrayList<ServiceRef>();
        
        for (FileObject root : sourceRoots) {
            String name = root.getName();
            
            if (name.equals(CONF_FOLDER) || 
                    name.equals(WEB_FOLDER) || 
                    name.equals(TEST_FOLDER)) {  
                continue;
            }
            
            Enumeration<? extends FileObject> dataFiles = root.getData(true);
            
            while (dataFiles.hasMoreElements()) {
                FileObject fobj = dataFiles.nextElement();
                
                if (fobj.getExt().equals(JAVA_EXT)) {    
                    //System.out.println("source fobj = " + fobj);
                    JavaSource source = JavaSource.forFileObject(fobj);
                    
                    refs.addAll(getServiceRefsFromSource(source));
                }
            }
        }
        
        return refs;
    }
    
    private List<ServiceRef> getServiceRefsFromSource(JavaSource source) {
        final List<ServiceRef> refs = new ArrayList<ServiceRef>();
        
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    
                    TypeElement classElement = getTopLevelClassElement(controller);
                    List<VariableElement> fields = ElementFilter.fieldsIn(classElement.getEnclosedElements());
                    
                    for (VariableElement field : fields) {
                        List<? extends AnnotationMirror> annotations = field.getAnnotationMirrors();
                        
                        for (AnnotationMirror annotation : annotations) {
                            if (annotation.toString().startsWith("@javax.xml.ws.WebServiceRef")) {    //NOI18N
                                Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
                                
                                for (ExecutableElement key : values.keySet()) {
                                    if (key.getSimpleName().toString().equals("wsdlLocation")) {        //NOI18N
                                        String wsdlLocation = values.get(key).toString().replace("\"", "");     //NOI18N
                                        String refName = classElement.getQualifiedName().toString() + "/" +     //NOI18N
                                                field.getSimpleName().toString();
                                        String className = classElement.getSimpleName().toString();
                                        
                                        refs.add(new ServiceRef(refName, wsdlLocation, className));
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            
        }
        
        return refs;
    }
    
    private ClassTree getTopLevelClassTree(CompilationController controller) {
        String className = controller.getFileObject().getName();
        
        List<? extends Tree> decls = controller.getCompilationUnit().getTypeDecls();
        
        for (Tree decl : decls) {
            if (decl.getKind() != Tree.Kind.CLASS) {
                continue;
            }
            
            ClassTree classTree = (ClassTree) decl;
            
            if (classTree.getSimpleName().contentEquals(className) &&
                    classTree.getModifiers().getFlags().contains(Modifier.PUBLIC))
                return classTree;
        }
        
        return null;
    }
    
    private TypeElement getTopLevelClassElement(CompilationController controller) {
        ClassTree classTree = getTopLevelClassTree(controller);
        Trees trees = controller.getTrees();
        TreePath path = trees.getPath(controller.getCompilationUnit(), classTree);
        
        return (TypeElement) trees.getElement(path);
    }
    
    private static class ServiceRef {
        private String name;
        private String wsdlLocation;
        private String className;
        
        public ServiceRef(String name, String wsdlLocation, String className) {
            this.name = name;
            this.wsdlLocation = wsdlLocation;
            this.className = className;
        }
        
        public String getName() {
            return name;
        }
        
        public String getWsdlLocation() {
            return wsdlLocation;
        }
        
        public String getClassName() {
            return className;
        }
    }
}

