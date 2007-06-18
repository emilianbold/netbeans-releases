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

package org.netbeans.modules.web.jsf.refactoring;


import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class JSFRefactoringUtils {

    private JSFRefactoringUtils() {
    }
    
    //TODO this is copy from org.netbeans.modules.refactoring.java.RetoucheUtils
    //Probably this methods will be moved to an api 
    public static String getPackageName(FileObject folder) {
        assert folder.isFolder() : "argument must be folder";  //NOI18N
        return ClassPath.getClassPath(
                folder, ClassPath.SOURCE)
                .getResourceName(folder, '.', false);
    }
    
    //TODO this is copy from org.netbeans.modules.refactoring.java.RetoucheUtils
    //Probably this methods will be moved to an api 
    public static String getPackageName(URL url) {
        File file = null;
        try {
            file = FileUtil.normalizeFile(new File(url.toURI()));
        } catch (URISyntaxException uRISyntaxException) {
            throw new IllegalArgumentException("Cannot create package name for url " + url);  //NOI18N
        }
        String suffix = "";
        
        do {
            FileObject fileObject = FileUtil.toFileObject(file);
            if (fileObject != null) {
                if ("".equals(suffix))
                    return getPackageName(fileObject);
                String prefix = getPackageName(fileObject);
                return prefix + ("".equals(prefix)?"":".") + suffix;
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix;
            }
            suffix = URLDecoder.decode(file.getPath().substring(file.getPath().lastIndexOf(File.separatorChar)+1)) + suffix;
            file = file.getParentFile();
        } while (file!=null);
        throw new IllegalArgumentException("Cannot create package name for url " + url);  //NOI18N
    }

    
    public static boolean containsRenamingPackage(String oldFQCN, String oldPackage, boolean renameSubpackages){
        boolean contains = false;
        if (!renameSubpackages){
            if (oldFQCN.startsWith(oldPackage)
                    && oldFQCN.substring(oldPackage.length()+1).indexOf('.') < 0
                    && oldFQCN.substring(oldPackage.length()).charAt(0) == '.'){
                contains = true;
            }
        }
        else {
            if (oldFQCN.startsWith(oldPackage) 
                    && oldFQCN.substring(oldPackage.length()).charAt(0) == '.'){
                contains = true;
            }
        }
        return contains;
    }
    
    public static void renamePackage(AbstractRefactoring refactoring, RefactoringElementsBag refactoringElements, 
            FileObject folder, String oldFQPN, String newFQPN, boolean recursive){
        WebModule webModule = WebModule.getWebModule(folder);
        if (webModule != null){
            List <Occurrences.OccurrenceItem> items = Occurrences.getPackageOccurrences(webModule, oldFQPN, newFQPN, recursive);
            Modifications modification = new Modifications();
            for (Occurrences.OccurrenceItem item : items) {
                Modifications.Difference difference = new Modifications.Difference(
                                Modifications.Difference.Kind.CHANGE, item.getChangePosition().getBegin(),
                                item.getChangePosition().getEnd(), item.getOldValue(), item.getNewValue(), item.getRenamePackageMessage());
                modification.addDifference(item.getFacesConfig(), difference);
                refactoringElements.add(refactoring, new DiffElement.ChangeFQCNElement(difference, item, modification));
            }
        }
    }
    
}
