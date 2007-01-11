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

package org.netbeans.modules.j2ee.ejbcore;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.Trees;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * This class contains common functionality for code generation
 * @author Chris Webster
 * @author Martin Adamek
 */
public class EjbGenerationUtil {

    private static final String[] EJB_NAME_CONTEXTS = new String[] {
                EnterpriseBeans.SESSION,
                EnterpriseBeans.ENTITY,
                EnterpriseBeans.MESSAGE_DRIVEN
    };
    
    public static String getFullClassName(String pkg, String className) {
        return (pkg==null||pkg.length()==0)?className:pkg+"."+className; //NOI18N
    }
    
    public static String getBaseName(String fullClassName) {
        return fullClassName.substring(fullClassName.lastIndexOf('.')+1); //NOI18N
    }
    
    public static String[] getPackages(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<String> pkgs = new TreeSet<String>();
        for (int i = 0; i < groups.length; i++) {
            findPackages(groups[i].getRootFolder(),"", pkgs);
        }
        return pkgs.toArray(new String[pkgs.size()]);
    }
    
    private static void findPackages (FileObject root, String curPkg, Set<String> pkgs) {
        for (FileObject kid : root.getChildren()) {
	        String name = curPkg + (curPkg.length() != 0 ? "." : "") + kid.getName();
            pkgs.add (name);
	        findPackages (kid, name, pkgs);
        }
    }
    
    public static boolean isEjbNameInDD(String ejbName, EjbJar ejbJar) {
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        Object ejb = null;
        if (beans != null) {
            for (int i = 0; i < EJB_NAME_CONTEXTS.length; i++) {
                ejb = beans.findBeanByName(EJB_NAME_CONTEXTS[i], Ejb.EJB_NAME, ejbName);
                if (ejb != null) {
                    break;
                }
            }
        }
        return beans != null && ejb != null;
    }
    
    /**
     * Generates the basic part of EJB component name which is used for other
     * generations, like <component-name>LocalHome ...<br>
     * If <component-name> contains suffix "Bean", it will be removed.<br>
     * If <component-name> equals "Bean", then generated name will be "Bean"<br>
     * If component with such name already exists in EJB deployment descriptor,
     * name will be extended with number suffix: <component-name>1 ... n<br>
     *
     * @param componentName name in form: <component-name> or <component-name>Bean
     * @param dd deployment descriptor
     * @return unique name for EJB component which is used as base for other names
     */
    public static String uniqueSingleEjbName(String componentName, EjbJar ejbJar) {
        throw new UnsupportedOperationException("not implemented");
        //TODO: RETOUCHE
//        int uniquifier = 1;
//        if (!componentName.equalsIgnoreCase("Bean") && componentName.endsWith("Bean")) {
//            componentName = componentName.substring(0, componentName.length() - 4);
//        }
//        String newName = componentName;
//        while(isEjbNameInDD(newName + "Bean", dd)) {
//            newName = componentName + String.valueOf(uniquifier++);
//        }
//        return newName;
    }

    public static FileObject getPackageFileObject(SourceGroup location, String pkgName, Project project) {
        String relativePkgName = pkgName.replace('.', '/');
        FileObject fileObject = null;
        fileObject = location.getRootFolder().getFileObject(relativePkgName);
        if (fileObject != null) {
            return fileObject;
        } else {
            File rootFile = FileUtil.toFile(location.getRootFolder());
            File pkg = new File(rootFile,relativePkgName);
            pkg.mkdirs();
            fileObject = location.getRootFolder().getFileObject(relativePkgName);
        }
        return fileObject;
    }

    public static String getSelectedPackageName(FileObject targetFolder) {
        Project project = FileOwnerQuery.getOwner(targetFolder);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = FileUtil.getRelativePath(groups [i].getRootFolder(), targetFolder);
        }
        if (packageName != null) {
            packageName = packageName.replaceAll("/", ".");
        }
        return packageName+"";
    }

    //TODO: RETOUCHE move this static method somewhere, ensure it is not called from within other task
    public static void addPKGetter(final Entity entity, FileObject classFO, final boolean remote) throws IOException {
        final String pkField = entity.getPrimkeyField();
        if (pkField == null) {
            return;
        }
        JavaSource javaSource = JavaSource.forFileObject(classFO);
        ModificationResult modificationResult = javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                SourceUtils sourceUtils = SourceUtils.newInstance(workingCopy);
                TypeElement javaClass = sourceUtils.getTypeElement();
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                TypeElement returnType = workingCopy.getElements().getTypeElement(entity.getPrimKeyClass());
                ExpressionTree throwsClause = null;
                if (remote) {
                    TypeElement element = workingCopy.getElements().getTypeElement(RemoteException.class.getName());
                    throwsClause = treeMaker.QualIdent(element);
                }
                Trees trees = workingCopy.getTrees();
                MethodTree resultTree = treeMaker.Method(
                        treeMaker.Modifiers(Collections.<Modifier>emptySet()),
                        EntityMethodController.getMethodName(pkField, true),
                        trees.getTree(returnType),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        remote ? Collections.singletonList(throwsClause) : Collections.<ExpressionTree>emptyList(),
                        treeMaker.Block(Collections.<StatementTree>emptyList(), false),
                        null
                        );
                ClassTree classTree = trees.getTree(javaClass);
                ClassTree modifiedClazz = treeMaker.addClassMember(classTree, resultTree);
                workingCopy.rewrite(classTree, modifiedClazz);
            }
        });
        modificationResult.commit();
    }

}
