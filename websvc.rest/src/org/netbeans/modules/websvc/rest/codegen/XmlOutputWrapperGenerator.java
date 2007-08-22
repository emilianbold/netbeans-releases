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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.rest.codegen;

import com.sun.source.tree.ClassTree;
import java.io.IOException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.websvc.rest.codegen.model.GenericResourceBean;
import org.netbeans.modules.websvc.rest.codegen.model.WsdlResourceBean;
import org.netbeans.modules.websvc.rest.support.AbstractTask;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.filesystems.FileObject;

/**
 * Generator for JAXB XmlRoot JAXB output class annotated with XmlType.
 * 
 * @author nam
 */
public class XmlOutputWrapperGenerator {
    private FileObject destDir;
    private String className;
    private String packageName;
    private String wrapElementName;
    private String[] jaxbOutputClassNames;
    private JavaSource wrapperJS;
    
    public XmlOutputWrapperGenerator(FileObject destDir, String className, String packageName, String[] jaxbOutputClassNames) {
        if (destDir == null || className == null || jaxbOutputClassNames == null || jaxbOutputClassNames.length == 0) {
            throw new IllegalArgumentException();
        }
        this.destDir = destDir;
        this.className = className;
        this.packageName = packageName;
        this.jaxbOutputClassNames = jaxbOutputClassNames;
        wrapElementName = Util.lowerFirstChar(className);
        if (wrapElementName.endsWith(EntityResourcesGenerator.CONVERTER_SUFFIX)) {
            wrapElementName = wrapElementName.substring(0,wrapElementName.lastIndexOf(EntityResourcesGenerator.CONVERTER_SUFFIX));
        }
    }

    public FileObject generate() throws IOException {
        FileObject wrapperFO = destDir.getFileObject(className, Constants.JAVA_EXT);
        if (wrapperFO != null) {
            return wrapperFO;
        }
        
        wrapperJS = JavaSourceHelper.createJavaSource(destDir, 
                packageName, className);
                
        ModificationResult result = wrapperJS.runModificationTask(new AbstractTask<WorkingCopy>() {
                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);
                    
                    JavaSourceHelper.addImports(copy, new String[] { 
                        Constants.XML_ELEMENT, Constants.XML_ROOT_ELEMENT });
                    
                    JavaSourceHelper.addClassAnnotation(copy,
                            new String[] {Constants.XML_ROOT_ELEMENT_ANNOTATION},
                            new Object[] {
                        JavaSourceHelper.createAssignmentTree(copy, "name", wrapElementName) });
                    
                    ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                    ClassTree modifiedTree = tree;

                    for (int i=0; i<jaxbOutputClassNames.length; i++) {
                        String jaxbOutputClassName = jaxbOutputClassNames[i];
                        String fieldName = getSimpleName(jaxbOutputClassName);
                        fieldName = Util.lowerFirstChar(fieldName);
                        modifiedTree = addField(copy, modifiedTree, fieldName, jaxbOutputClassName);
                        modifiedTree = addGetMethod(copy, modifiedTree, fieldName, jaxbOutputClassName);
                        modifiedTree = addSetMethod(copy, modifiedTree, fieldName, jaxbOutputClassName);
                    }
                    
                    copy.rewrite(tree, modifiedTree);
                }
        });
        result.commit();
        return wrapperJS.getFileObjects().iterator().next();
    }
    
    private String getSimpleName(String qualifiedName) {
        String name = qualifiedName;
        int i = name.lastIndexOf('.');
        if (i > -1)
            name = name.substring(i+1, name.length());
        return name;
    }

    private ClassTree addField(WorkingCopy copy, ClassTree tree, String fieldName, String jaxbOutputClassName) {
        return JavaSourceHelper.addField(copy, tree, Constants.PRIVATE, null, null, fieldName, jaxbOutputClassName);
    }
    
    private ClassTree addGetMethod(WorkingCopy copy, ClassTree tree, String fieldName, String jaxbOutputClassName) {
        String[] annotations = new String[] { Constants.XML_ELEMENT_ANNOTATION };
        String methodName = "get" + Util.upperFirstChar(fieldName); //NOI18N
        String bodyText = "{ return "+fieldName+"; }"; //NOI18N
        String comment = "Get method for wrapped element \n"+ //NOI18N
                "@return an instance of " + jaxbOutputClassName; //NOI18N
        return JavaSourceHelper.addMethod(copy, tree, 
                Constants.PUBLIC, annotations, null,
                methodName, jaxbOutputClassName, null, null, null, null,
                bodyText, comment);      //NOI18N
    }
    
    private ClassTree addSetMethod(WorkingCopy copy, ClassTree tree, String fieldName, String jaxbOutputClassName) {
        String methodName = "set" + Util.upperFirstChar(fieldName); //NOI18N
        String[] parameters = new String[] { fieldName };
        Object[] paramTypes = new Object[] { jaxbOutputClassName };
        String bodyText = "{ this.$FIELD$ = $FIELD$; }".replace("$FIELD$", fieldName); //NOI18N
        String comment = "Set method for wrapped element of type " + jaxbOutputClassName; //NOI18N
        return JavaSourceHelper.addMethod(copy, tree, 
                Constants.PUBLIC, null, null,
                methodName, Constants.VOID, parameters, paramTypes, null, null,
                bodyText, comment);      //NOI18N
    }
}
