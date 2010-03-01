/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.form;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.beans.Introspector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Position;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileObject;

/**
 *
 * Provides information about the forms java source file.
 *
 * @author Tomas Stupka
 */
public class FormJavaSource {
    
    private final FormDataObject formDataObject;	
    private List<String> fields = null;	
    private static final String[] PROPERTY_PREFIXES = new String[] {"get", // NOI18N
								    "is"}; // NOI18N
    
    public FormJavaSource(FormDataObject formDataObject) {
	this.formDataObject = formDataObject;
    }    
    
    public void refresh() {
        this.fields = Collections.<String>emptyList();
        runUserActionTask(new CancellableTask<CompilationController>() {
            @Override
            public void cancel() {
            }
            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.PARSED);
                FormJavaSource.this.fields = getFieldNames(controller);
            }
        });

    }
    
    private void runUserActionTask(CancellableTask<CompilationController> task) {
        FileObject javaFileObject = formDataObject.getPrimaryFile();		
        JavaSource js = JavaSource.forFileObject(javaFileObject);
        if (js != null) {
            try {
                js.runUserActionTask(task, true);
            } catch (IOException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void runModificationTask(CancellableTask<WorkingCopy> task) {
        FileObject javaFileObject = formDataObject.getPrimaryFile();		
        JavaSource js = JavaSource.forFileObject(javaFileObject);
        if (js != null) {
            try {
                js.runModificationTask(task).commit();
            } catch (IOException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean containsField(String name, boolean refresh) {
	if(refresh) {
	    refresh();
	}	    
	return fields != null && fields.contains(name);
    }	

    private ClassTree findClassTree(CompilationController controller) {
        String fileName = formDataObject.getPrimaryFile().getName();
        
        for (Tree t: controller.getCompilationUnit().getTypeDecls()) {
            if (t.getKind() == Tree.Kind.CLASS &&
                    fileName.equals(((ClassTree) t).getSimpleName().toString())) {
                return (ClassTree) t;
            }
        }
        return null;
    }

    private static TypeKind primitiveClassToTypeKind(Class clazz) {
        TypeKind kind = null;
        if (clazz == char.class) {
            kind = TypeKind.CHAR;
        } else if (clazz == boolean.class) {
            kind = TypeKind.BOOLEAN;
        } else if (clazz == int.class) {
            kind = TypeKind.INT;
        } else if (clazz == long.class) {
            kind = TypeKind.LONG;
        } else if (clazz == byte.class) {
            kind = TypeKind.BYTE;
        } else if (clazz == short.class) {
            kind = TypeKind.SHORT;
        } else if (clazz == float.class) {
            kind = TypeKind.FLOAT;
        } else if (clazz == double.class) {
            kind = TypeKind.DOUBLE;
        }
        return kind;
    }

    private TypeMirror clazzToTypeMirror(CompilationController controller, Class clazz) {
        TypeMirror type;
        if (clazz.isPrimitive()) {
            TypeKind kind = primitiveClassToTypeKind(clazz);
            type = controller.getTypes().getPrimitiveType(kind);
        } else if (clazz.isArray()) {
            type = clazzToTypeMirror(controller, clazz.getComponentType());
            type = controller.getTypes().getArrayType(type);
        } else {
            String returnTypeName = clazz.getCanonicalName();
            TypeElement returnTypeElm = controller.getElements().getTypeElement(returnTypeName);
            type = returnTypeElm.asType();
        }
        return type;
    }

    private List<String> findMethodsByReturnType(CompilationController controller, TypeElement celem, Class returnType) {
        List<String> methods = new ArrayList<String>();
        TypeMirror type = clazzToTypeMirror(controller, returnType);
        for (Element el: celem.getEnclosedElements()) {
            if (el.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) el;
                TypeMirror methodRT = method.getReturnType();
                if (controller.getTypes().isAssignable(type, methodRT)) {
                    methods.add(method.getSimpleName().toString());
                }
            }
        }
        return methods;
    }
    /**
     * Returns names for all methods with the specified return type
     * 
     * @param returnType return type
     * @return names of all methods with the given return type.
     */
    public String[] getMethodNames(final Class returnType) {
        final Object[] result = new Object[1];
        
        runUserActionTask(new CancellableTask<CompilationController>() {
            @Override
            public void cancel() {
            }
            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                
                ClassTree ct = findClassTree(controller);
                if (ct != null) {
                    TreePath cpath = controller.getTrees().getPath(controller.getCompilationUnit(), ct);
                    TypeElement celem = (TypeElement) controller.getTrees().getElement(cpath);
                    List<String> names = findMethodsByReturnType(controller, celem, returnType);
                    result[0] = toArray(names);
                }
                
            }
        });
        
        return result[0] == null? new String[0]: (String[]) result[0];
    }

    public String getAnnotationCode(final String methodName, final Position startPosition, final Position endPosition, final boolean removeAnnotations) {
        final StringBuilder sb = new StringBuilder();
        runModificationTask(new CancellableTask<WorkingCopy>() {
            @Override
            public void cancel() {
            }
            @Override
            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree ct = findClassTree(wc);
                int start = startPosition.getOffset();
                int end = endPosition.getOffset();
                if (ct != null) {
                    SourcePositions sp = wc.getTrees().getSourcePositions();
                    for (Tree member : ct.getMembers()) {
                        if (Tree.Kind.METHOD == member.getKind()) {
                            MethodTree method = (MethodTree)member;
                            if (methodName.equals(method.getName().toString())) {
                                long methodStart = sp.getStartPosition(wc.getCompilationUnit(), method);
                                long methodEnd = sp.getEndPosition(wc.getCompilationUnit(), method);
                                if ((methodStart <= end) && (start <= methodEnd)) {
                                    for (AnnotationTree annotation : method.getModifiers().getAnnotations()) {
                                        sb.append(annotation.toString()).append('\n');
                                    }
                                }
                                if (removeAnnotations) {
                                    ModifiersTree oldModifiers = method.getModifiers();
                                    TreeMaker make = wc.getTreeMaker();
                                    ModifiersTree newModifiers = make.Modifiers(oldModifiers.getFlags());
                                    wc.rewrite(oldModifiers, newModifiers);
                                }
                            }
                        }
                    }
                }                
            }
        });
        return (sb.length() == 0) ? null : sb.toString();
    }

    /**
     * Returns names for all methods with the specified return type which 
     * start with the prefixes "is" and "get"
     * 
     * @param returnType return type.
     * @return names of methods.
     */
    public String[] getPropertyReadMethodNames(Class returnType) {
        String[] names = getMethodNames(returnType);
        List<String> result = new ArrayList<String>(names.length);
        for (String name: names) {
            if(!FormJavaSource.extractPropertyName(name).equals("")) { // NOI18N	
                // seems to be property method
                result.add(name);
            }		    
            
        }
        
        return toArray(result);
        
    }
    
    public static String extractPropertyName(String methodName) {
	for (int i = 0; i < PROPERTY_PREFIXES.length; i++) {
	    if(methodName.startsWith(PROPERTY_PREFIXES[i]) && 
	       methodName.length() > PROPERTY_PREFIXES[i].length()) 
	    {		    
		return Introspector.decapitalize(methodName.substring(PROPERTY_PREFIXES[i].length()));		     			
	    }	
	}
	return "";  // NOI18N	
    }    

    private List<String> getFieldNames(final CompilationController controller) {
        SimpleSection variablesSection = 
            formDataObject.getFormEditorSupport().getVariablesSection();	    

        if(variablesSection==null) {
            return null;
        }

        final int genVariablesStartOffset = variablesSection.getStartPosition().getOffset();
        final int genVariablesEndOffset = variablesSection.getEndPosition().getOffset();
        
        final SourcePositions positions = controller.getTrees().getSourcePositions();
        
        TreeScanner<Void, List<String>> scan = new TreeScanner<Void, List<String>>() {
            @Override
            public Void visitClass(ClassTree node, List<String> p) {
                long startOffset = positions.getStartPosition(controller.getCompilationUnit(), node);
                long endOffset = positions.getEndPosition(controller.getCompilationUnit(), node);
                if (genVariablesStartOffset > startOffset && genVariablesEndOffset < endOffset) {
                    for (Tree tree: node.getMembers()) {
                        if (tree.getKind() == Tree.Kind.VARIABLE) {
                            testVariable((VariableTree) tree, p);
                        }
                    }
                }
                return null;
            }
            
            private void testVariable(VariableTree node, List<String> p) {
                long startOffset = positions.getStartPosition(controller.getCompilationUnit(), node);
                if (startOffset >= genVariablesEndOffset ||
                        startOffset <= genVariablesStartOffset) {
                    p.add(node.getName().toString());
                }
            }
        };
        
        List<String> fieldNames = new ArrayList<String>();
        scan.scan(controller.getCompilationUnit(), fieldNames);
        
        return fieldNames;
    }	

    private static String[] toArray(List<String> list) {
        return list.toArray(new String[list.size()]);
    }

    public static boolean isInDefaultPackage(FormModel formModel) {
        FileObject fdo = FormEditor.getFormDataObject(formModel).getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath(fdo, ClassPath.SOURCE);
        String name = cp.getResourceName(fdo);
        return name.indexOf('/') < 0;
    }

}
