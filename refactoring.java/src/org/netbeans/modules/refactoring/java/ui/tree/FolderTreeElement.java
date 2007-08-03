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

package org.netbeans.modules.refactoring.java.ui.tree;

import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.swing.Icon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class FolderTreeElement implements TreeElement {
    
    private FileObject fo;
    FolderTreeElement(FileObject fo) {
        this.fo = fo;
    }
        
    
    public TreeElement getParent(boolean isLogical) {
        if (isLogical) {
            SourceGroup sg = getSourceGroup(fo);
            if (sg!=null) {
                return TreeElementFactory.getTreeElement(sg);
            } else {
                return null;
            }
        } else {
            Project p = FileOwnerQuery.getOwner(fo);
            if (p!=null) {
                return TreeElementFactory.getTreeElement(p);
            } else {
                return null;
            }
        }
    }

    public Icon getIcon() {
        return UiUtils.getElementIcon(ElementKind.PACKAGE, null);
    }

    public String getText(boolean isLogical) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp==null) {
            return fo.getPath();
        } else {
            if (getJavaSourceGroup(fo)!=null) {
                String name = cp.getResourceName(fo).replace('/','.');
                if ("".equals(name))
                    return NbBundle.getMessage(UiUtils.class, "LBL_DefaultPackage_PDU");
                return name;
            } else {
                return fo.getPath();
            }
        }
    }

    static SourceGroup getSourceGroup(FileObject file) {
        Project prj = FileOwnerQuery.getOwner(file);
        if (prj == null)
            return null;
        Sources src = ProjectUtils.getSources(prj);
        //TODO: needs to be generified
        SourceGroup[] javagroups = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup[] xmlgroups = src.getSourceGroups("xml");//NOI18N
        
        SourceGroup[] allgroups =  new SourceGroup[javagroups.length + xmlgroups.length];

        if (allgroups.length < 1) {
            // Unknown project group
            Logger.getLogger(FolderTreeElement.class.getName()).severe("Cannot find SourceGroup for " + file.getPath());
            return null;
       }
        System.arraycopy(javagroups,0,allgroups,0,javagroups.length);
        System.arraycopy(xmlgroups,0,allgroups,allgroups.length-1,xmlgroups.length);
        for(int i=0; i<allgroups.length; i++) {
            if (allgroups[i].getRootFolder().equals(file) || FileUtil.isParentOf(allgroups[i].getRootFolder(), file))
                return allgroups[i];
        }
        return null;
    }
    
    private static SourceGroup getJavaSourceGroup(FileObject file) {
        Project prj = FileOwnerQuery.getOwner(file);
        if (prj == null)
            return null;
        Sources src = ProjectUtils.getSources(prj);
        SourceGroup[] javagroups = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        for(int i=0; i<javagroups.length; i++) {
            if (javagroups[i].getRootFolder().equals(file) || FileUtil.isParentOf(javagroups[i].getRootFolder(), file))
                return javagroups[i];
        }
        return null;
    }
    

    public Object getUserObject() {
        return fo;
    }
}
