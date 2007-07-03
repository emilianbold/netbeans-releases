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

package org.netbeans.modules.refactoring.ruby.ui.tree;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class TreeElementFactoryImpl implements TreeElementFactoryImplementation {

    public Map<Object, TreeElement> map = new WeakHashMap<Object,TreeElement>();
    public static TreeElementFactoryImpl instance;
    {
        instance = this;
    }
    
    public TreeElement getTreeElement(Object o) {
        TreeElement result = null;
        if (o instanceof SourceGroup) {
            result = map.get(((SourceGroup)o).getRootFolder());
        } else {
            result = map.get(o);
        }
        if (result!= null)
            return result;
        if (o instanceof FileObject) {
            FileObject fo = (FileObject) o;
            if (fo.isFolder()) {
                SourceGroup sg = FolderTreeElement.getSourceGroup(fo);
                if (sg!=null && fo.equals(sg.getRootFolder())) 
                    result = new SourceGroupTreeElement(sg);
                else 
                    result = new FolderTreeElement(fo);
            } else {
                result = new FileTreeElement(fo);
            }
        } else if (o instanceof SourceGroup) {
            result = new SourceGroupTreeElement((SourceGroup)o);
        } else if (o instanceof ElementGrip) {
            result = new ElementGripTreeElement((ElementGrip) o);
        }
        else if (o instanceof Project) {
            result = new ProjectTreeElement((Project) o);
        } else if (o instanceof RefactoringElement) {
            ElementGrip grip = ((RefactoringElement) o).getLookup().lookup(ElementGrip.class);
            if (grip!=null) {
                result = new RefactoringTreeElement((RefactoringElement) o);
            } 
        }
        if (result != null) {
            if (o instanceof SourceGroup) {
                map.put(((SourceGroup)o).getRootFolder(), result);
            } else {
                map.put(o, result);
            }
        }
        return result;
    }

    public void cleanUp() {
        map.clear();
        ElementGripFactory.getDefault().cleanUp();
    }
}
