/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.e2e.wsdl.wsdl2java;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author luky
 */
public class ClassExplorer
{
    static
    {
        System.setProperty("netbeans.user","C:\\Documents and Settings\\luky\\.netbeans\\dev" );
    }
    
    private CompilationUnitTree compilationUnit;
    private Trees trees;
    private boolean initialized = false;
    
    private class ClassFinderTask implements CancellableTask<CompilationController>
    {
        
        private volatile boolean cancelled;
        private HashMap<ElementHandle<TypeElement>, List<ExecutableElement>> topClassMap = new HashMap<ElementHandle<TypeElement>, List<ExecutableElement>>();
        
        public void cancel()
        {
            this.cancelled = true;
        }
        
        public void run(CompilationController parameter) throws Exception
        {
            if (!initialized)
            {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                if (cancelled)
                {
                    return;
                }
                ClassExplorer.this.compilationUnit = parameter.getCompilationUnit();
                ClassExplorer.this.trees = parameter.getTrees();
                initialized = true;
            }
        }
    }
    
    public ClassExplorer(FileObject fobj)
    {
        try
        {
            JavaSource javaSource = JavaSource.forFileObject(fobj);
            ClassFinderTask task=new ClassFinderTask();
            Future fin = javaSource.runWhenScanFinished(task, true);
            //Wait till task is finished
            fin.get();
        }
        catch (Exception ex)
        {
            Logger.getLogger(ClassExplorer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public List<TypeElement> getClasses()
    {
        List<TypeElement> result = new ArrayList<TypeElement>();
        if (initialized)
        {
            List<? extends Tree> typeDecls = compilationUnit.getTypeDecls();
            if ((typeDecls == null) || typeDecls.isEmpty())
            {
                return Collections.<TypeElement>emptyList();
            }
            
            TreePath tPath=new TreePath(compilationUnit);
            for (Tree typeDecl : typeDecls)
            {
                if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind()))
                {
                    TypeElement elem=(TypeElement)trees.getElement(new TreePath(tPath,typeDecl));
                    result.add(elem);
                }
            }
        }
        return result;
    }
    
    public List<ExecutableElement> getMethods(TypeElement elem)
    {
        List<? extends Element> elems=elem.getEnclosedElements();
        List<ExecutableElement> exElems = new ArrayList<ExecutableElement>(elems.size());
        for (Element el : elems)
        {
            if (el.getKind().equals(ElementKind.METHOD) ||
                    el.getKind().equals(ElementKind.CONSTRUCTOR))
            {
                exElems.add((ExecutableElement)el);
            }
        }
        return exElems;
    }
    
    public TypeMirror getReturnType(ExecutableElement elem)
    {
        return elem.getReturnType();
    }
    
    public List<TypeMirror> getParameters(ExecutableElement elem)
    {
        List<? extends VariableElement> varElems=elem.getParameters();
        List<TypeMirror> paramList = new ArrayList<TypeMirror>(varElems.size());
        for (VariableElement vElem : varElems)
        {
            paramList.add(vElem.asType());
        }
        return paramList;
    }
    
    
    public boolean compareClasses(Set<String> classes,List<TypeElement> classElems)
    {
        if (classes.size() == classElems.size())
        {
            HashSet<String> set=new HashSet<String>();
            for (TypeElement elem : classElems)
            {
                set.add(elem.getQualifiedName().toString());
            }
            for (String className : classes)
            {
                if (!set.contains(className))
                {
                    System.err.println("Class: "+className+" not found in generated file");
                    break;
                }
            }
            return true;
        }
        return false;
    }
    
    public boolean compareMethods(Set<String> methods,List<ExecutableElement> methodElems)
    {
        if (methods.size() == methodElems.size())
        {
            HashSet<String> set=new HashSet<String>();
            for (ExecutableElement elem : methodElems)
            {
                set.add(elem.getSimpleName().toString());
            }
            for (String methodName : methods)
            {
                if (!set.contains(methodName))
                {
                    System.err.println("Method: "+methodName+" not found in generated file");
                    break;
                }
            }
            return true;
        }
        return false;
    }
    
    public boolean compareParameters(Set<String> params,List<TypeMirror> variableElems)
    {
        if (params.size() == variableElems.size())
        {
            HashSet<String> set=new HashSet<String>();
            for (TypeMirror elem : variableElems)
            {
                set.add(elem.toString());
            }
            for (String variableType : params)
            {
                if (!set.contains(variableType.toString()))
                {
                    System.err.println("Parameter: "+variableType+" not found in generated file");
                    break;
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        // TODO code application logic here
        ClassExplorer expl=new ClassExplorer(FileUtil.toFileObject(new File("C:\\test.java")));
        List<TypeElement> tel=expl.getClasses();
        for (TypeElement te : tel)
        {
            System.out.println(te.toString());
            List<ExecutableElement> mel=expl.getMethods(te);
            for (ExecutableElement me : mel)
            {
                System.out.println("\t"+me.getSimpleName().toString());
                List<TypeMirror> vel=expl.getParameters(me);
                for (TypeMirror var : vel)
                {
                    System.out.println("\t\t"+var.toString());
                }
                System.out.println("\t\treturns:"+me.getReturnType().toString());
            }
        }
        System.exit(-1);
    }
}
