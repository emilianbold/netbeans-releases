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

package org.netbeans.modules.j2ee.common.source;

import com.sun.source.tree.*;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author Andrei Badea
 */
public final class GenerationUtils extends SourceUtils {

    /**
     * The templates for regular Java class and interface.
     */
    static final String CLASS_TEMPLATE = "Templates/Classes/Class.java"; // NOI18N
    static final String INTERFACE_TEMPLATE = "Templates/Classes/Interface.java"; // NOI18N
    
    // <editor-fold desc="Constructors and factory methods">
    
    private GenerationUtils(WorkingCopy copy, TypeElement typeElement) {
        super(copy, typeElement);
    }

    public static GenerationUtils newInstance(WorkingCopy copy, TypeElement typeElement) {
        if (copy == null) {
            throw new IllegalArgumentException("The copy argument cannot be null"); // NOI18N
        }
        if (typeElement == null) {
            throw new IllegalArgumentException("The mainTypeElement argument cannot be null"); // NOI18N
        }
        return new GenerationUtils(copy, typeElement);
    }
    
    public static GenerationUtils newInstance(WorkingCopy copy) throws IOException {
        if (copy == null) {
            throw new IllegalArgumentException("The copy argument cannot be null"); // NOI18N
        }
        TypeElement topLevelTypeElement = findPublicTopLevelTypeElement(copy);
        if (topLevelTypeElement != null) {
            return newInstance(copy, topLevelTypeElement);
        }
        return null;
    }
    
    // </editor-fold>
    
    // <editor-fold desc="Public static methods">
    
    /**
     * Creates a new Java class based on the default template for classes.
     *
     * @param  targetFolder the folder the new class should be created in.
     *         Must not be null.
     * @param  targetName the name of the new class. Must not be null or empty.
     * @return the FileObject for the new Java class
     */
    public static FileObject createClass(FileObject targetFolder, String targetName, final String javadoc) throws IOException{
        if (targetFolder == null) {
            throw new IllegalArgumentException("The targetFolder argument cannot be null"); // NOI18N
        }
        if (targetName == null) {
            throw new IllegalArgumentException("The targetName argument cannot be null"); // NOI18N
        }
        if (targetName.length() == 0) {
            throw new IllegalArgumentException("The targetName argument cannot be an empty string"); // NOI18N
        }
        
        FileObject classFO = createDataObjectFromTemplate(CLASS_TEMPLATE, targetFolder, targetName).getPrimaryFile();
        JavaSource javaSource = JavaSource.forFileObject(classFO);

        final boolean[] commit = { false };
        ModificationResult modification = javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                commit[0] = genUtils.ensureDefaultConstructor();
                // if (javadoc != null) {
                //     genUtils.setJavadoc(copy, mainType, javadoc);
                // }
            }
        });
        if (commit[0]) {
            modification.commit();
        }

        return classFO;
    }
    
    /**
     * Creates a new Java class based on the default template for classes.
     *
     * @param  targetFolder the folder the new class should be created in.
     *         Must not be null.
     * @param  targetName the name of the new class. Must not be null or empty.
     * @return the FileObject for the new Java class
     */
    public static FileObject createInterface(FileObject targetFolder, String targetName, final String javadoc) throws IOException{
        if (targetFolder == null) {
            throw new IllegalArgumentException("The targetFolder argument cannot be null"); // NOI18N
        }
        if (targetName == null) {
            throw new IllegalArgumentException("The targetName argument cannot be null"); // NOI18N
        }
        if (targetName.length() == 0) {
            throw new IllegalArgumentException("The targetName argument cannot be an empty string"); // NOI18N
        }
        
        FileObject classFO = createDataObjectFromTemplate(INTERFACE_TEMPLATE, targetFolder, targetName).getPrimaryFile();
        JavaSource javaSource = JavaSource.forFileObject(classFO);

        // final boolean[] commit = { false };
        // ModificationResult modification = javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
        //     public void run(WorkingCopy copy) throws IOException {
        //         GenerationUtils genUtils = GenerationUtils.newInstance(copy);
        //         if (javadoc != null) {
        //             genUtils.setJavadoc(copy, mainType, javadoc);
        //         }
        //     }
        // });
        // if (commit[0]) {
        //     modification.commit();
        // }

        return classFO;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Non-public static methods">

    /**
     * Creates a data object from a given template path in the system
     * file system.
     * 
     * @return the <code>DataObject</code> of the newly created file.
     * @throws IOException if an error occured while creating the file.
     */
    private static DataObject createDataObjectFromTemplate(String template, FileObject targetFolder, String targetName) throws IOException {
        assert template != null;
        assert targetFolder != null;
        assert targetName != null && targetName.trim().length() >  0;
        
        FileSystem dfs = Repository.getDefault().getDefaultFileSystem();
        FileObject fo = dfs.findResource(template);
        DataObject dob = DataObject.find(fo);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);
        return dob.createFromTemplate(dataFolder, targetName);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Non-public methods">

    /**
     * Ensures the main type element contains a concrete (that is, not synthetic)
     * default constructor.
     * 
     * @return true if the working copy was modified, false otherwise
     */
    private boolean ensureDefaultConstructor() throws IOException {
        ExecutableElement constructor = getDefaultConstructor();
        boolean modified = false;
        if (constructor != null) {
            if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                // TODO make constructor public
                modified = true;
            }
            
            System.out.println(constructor.getReturnType().getKind());
            
        } else {
            getWorkingCopy().toPhase(Phase.RESOLVED);

            TreeMaker make = getWorkingCopy().getTreeMaker();
            ClassTree oldClazz = getWorkingCopy().getTrees().getTree(getTypeElement());
            MethodTree method = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                    "<init>", // NOI18N
                    null,
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    "{ }", // NOI18N
                    null
            );
            ClassTree clazz = make.addClassMember(oldClazz, method);
            getWorkingCopy().rewrite(oldClazz, clazz);
            modified = true;
        }
        return modified;
    }
    
    /**
     * Returns the working copy this instance works with.
     * 
     * @return the working copy this instance works with; never null.
     */
    private WorkingCopy getWorkingCopy() {
        return (WorkingCopy)getCompilationController();
    }
    
    // </editor-fold>
}
