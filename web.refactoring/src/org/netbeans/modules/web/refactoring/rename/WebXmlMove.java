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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.refactoring.rename;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Handles moving of a class.
 *
 * @author Erno Mononen
 */
public class WebXmlMove extends BaseWebXmlRename{
    
    private final MoveRefactoring move;
    private final String oldFqn;
    
    public WebXmlMove(FileObject webDD, WebApp webModel, String clazz, MoveRefactoring move) {
        super(webDD, webModel);
        this.oldFqn = clazz;
        this.move = move;
    }
    
    private String getNewFQN(){
        String newPkg = getPackageName(move.getTarget().lookup(URL.class));
        String uqn = move.getRefactoringSource().lookup(FileObject.class).getName();
        return newPkg + "." + uqn;
    }
    
    protected AbstractRefactoring getRefactoring() {
        return move;
    }
    
    protected List<RenameItem> getRenameItems() {
        return Collections.singletonList(new BaseWebXmlRename.RenameItem(getNewFQN(),oldFqn));
    }
    
    // copied from o.n.m.java.refactoring.RetoucheUtils
    private static String getPackageName(URL url) {
        File f = null;
        try {
            f = FileUtil.normalizeFile(new File(url.toURI()));
        } catch (URISyntaxException uRISyntaxException) {
            throw new IllegalArgumentException("Cannot create package name for url " + url);
        }
        String suffix = "";
        
        do {
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null) {
                if ("".equals(suffix))
                    return getPackageName(fo);
                String prefix = getPackageName(fo);
                return prefix + ("".equals(prefix)?"":".") + suffix;
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix;
            }
            suffix = URLDecoder.decode(f.getPath().substring(f.getPath().lastIndexOf(File.separatorChar)+1)) + suffix;
            f = f.getParentFile();
        } while (f!=null);
        throw new IllegalArgumentException("Cannot create package name for url " + url);
    }
    
    // copied from o.n.m.java.refactoring.RetoucheUtils
    private static String getPackageName(FileObject folder) {
        assert folder.isFolder() : "argument must be folder";
        return ClassPath.getClassPath(
                folder, ClassPath.SOURCE)
                .getResourceName(folder, '.', false);
    }
    
    
}
