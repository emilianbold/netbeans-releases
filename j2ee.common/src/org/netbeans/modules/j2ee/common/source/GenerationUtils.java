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
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.*;
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

    private final WorkingCopy copy;

    public GenerationUtils(WorkingCopy copy) throws IOException, IllegalStateException {
        super(copy);
        this.copy = copy;
    }

    // static methods

    /**
     * Creates a new regular Java class based on the default template.
     *
     * <p>This method does not need to be called in javac context, but if
     * it is, that transaction needs to be enclosed in an
     * {@link FileSystem#runAtomicAction atomic action},
     * otherwise a deadlock can occur, like in issue 77500.</p>
     *
     * @param  targetFolder the folder / package for entity. Must not be null.
     * @param  targetName the name of entity. Must not be null or empty.
     * @return FileObject for the new Java class
     */
    // TODO review Javadoc above
    public static FileObject createClass(FileObject targetFolder, String targetName, final String javadoc) throws IOException{
        FileObject classFO = createDataObjectFromTemplate(CLASS_TEMPLATE, targetFolder, targetName).getPrimaryFile();
        JavaSource javaSource = JavaSource.forFileObject(classFO);

        final boolean[] commit = { false };
        ModificationResult modification = javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                GenerationUtils genUtils = new GenerationUtils(copy);
                commit[0] = genUtils.ensureDefaultConstructor();
                // if (javadoc != null) {
                //     setJavadoc(copy, mainType, javadoc);
                // }
            }
        });
        if (commit[0]) {
            modification.commit();
        }

        return classFO;
    }

    /**
     * Creates a data object from a given template name.
     *
     * @see #CLASS_TEMPLATE
     * @see #INTERFACE_TEMPLATE
     */
    private static DataObject createDataObjectFromTemplate(String template, FileObject targetFolder, String targetName) throws IOException {
        if (null == targetFolder || null == targetName || "".equals(targetName.trim())) { // NOI18N
            throw new IllegalArgumentException("Target folder and target name must be given."); // NOI18N
        }

        FileSystem dfs = Repository.getDefault().getDefaultFileSystem();
        FileObject fo = dfs.findResource(template);
        DataObject dob = DataObject.find(fo);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);
        return dob.createFromTemplate(dataFolder, targetName);
    }

    // instance methods

    /**
     * Ensures the main type element contains a concrete (not synthetic, that is)
     * default constructor.
     */
    public boolean ensureDefaultConstructor() throws IOException {
        ExecutableElement constructor = getDefaultConstructor();
        boolean modified = false;
        if (constructor != null) {
            if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                // TODO make constructor public
                modified = true;
            }
        } else {
            copy.toPhase(Phase.RESOLVED);

            TreeMaker make = copy.getTreeMaker();
            ClassTree oldClazz = copy.getTrees().getTree(mainTypeElement);
            MethodTree method = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                    mainTypeElement.getSimpleName(),
                    make.PrimitiveType(TypeKind.VOID),
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    "{ }", // NOI18N
                    null
            );
            ClassTree clazz = make.addClassMember(oldClazz, method);
            copy.rewrite(oldClazz, clazz);
            modified = true;
        }
        return modified;
    }

    /**
     * Returns the non-synthetic constructor of the main type element.
     */
    private ExecutableElement getDefaultConstructor() throws IOException {
        copy.toPhase(Phase.ELEMENTS_RESOLVED);

        ElementUtilities elementUtils = copy.getElementUtilities();
        for (Element element : mainTypeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement)element; // XXX is casting allowed after getKind()?
                if (constructor.getParameters().size() == 0 && !elementUtils.isSyntetic(constructor)) {
                    return constructor;
                }
            }
        }
        return null;
    }
}
