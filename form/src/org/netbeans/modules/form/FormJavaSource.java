/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form;

import com.sun.source.tree.ClassTree;
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
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.form.project.ClassPathUtils;
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
            public void cancel() {
            }
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
    
    private List<String> findMethodsByReturnType(CompilationController controller, TypeElement celem, Class returnType) {
        List<String> methods = new ArrayList<String>();
        String returnTypeName = returnType.getName();
        TypeElement returnTypeElm = controller.getElements().getTypeElement(returnTypeName);
        for (Element el: celem.getEnclosedElements()) {
            if (el.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) el;
                TypeMirror methodRT = method.getReturnType();
                if (controller.getTypes().isAssignable(returnTypeElm.asType(), methodRT)) {
                    methods.add(method.getSimpleName().toString());
                }
            }
        }
        return methods;
    }
    /**
     * Returns names for all methods with the specified return type
     */
    public String[] getMethodNames(final Class returnType) {
        final Object[] result = new Object[1];
        
        runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
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

    /**
     * Returns names for all methods with the specified return type which 
     * start with the prefixes "is" and "get"
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
    
    /**
     *
     */
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
        
        TreeScanner scan = new TreeScanner<Void, List<String>>() {
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
        
        List<String> fields = new ArrayList<String>();
        scan.scan(controller.getCompilationUnit(), fields);
        
        return fields;
    }	

    private boolean isAssignableFrom(String typeName, Class returnType) {	
	Class clazz = getClassByName(typeName);	    
	return clazz!=null ? returnType.isAssignableFrom(clazz) : false;	    
    }
    
    private Class getClassByName(String className) {
	Class clazz = null;
	try {
	    clazz = ClassPathUtils.loadClass(className, formDataObject.getPrimaryFile());
	}
	catch (Exception ex) {
            // could be anything, ignore it...
            ex.printStackTrace();	    
	}
	catch (LinkageError ex) {
	    ex.printStackTrace();	    
	}
	return clazz;
    }	    
    
    private static String[] toArray(List list) {
        return (String[])list.toArray(new String[list.size()]);
    }

    public static boolean isInDefaultPackage(FormModel formModel) {
        FileObject fdo = FormEditor.getFormDataObject(formModel).getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath(fdo, ClassPath.SOURCE);
        String name = cp.getResourceName(fdo);
        return name.indexOf('/') < 0;
    }

}
