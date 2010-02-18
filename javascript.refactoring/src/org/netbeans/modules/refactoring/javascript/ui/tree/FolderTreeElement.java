/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.refactoring.javascript.ui.tree;

import javax.swing.Icon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.csl.api.UiUtils;
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
//        return UiUtils.getElementIcon(ElementKind.PACKAGE, null);
        // UGH! I need a "source folder" like icon!
        return UiUtils.getElementIcon(ElementKind.MODULE, null);
    }

    public String getText(boolean isLogical) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp==null) {
            return fo.getPath();
        } else {
            if (getRubySourceGroup(fo)!=null) {
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
        //SourceGroup[] rubygroups = src.getSourceGroups(RubyProject.SOURCES_TYPE_RUBY);
        SourceGroup[] jsgroups = src.getSourceGroups(Sources.TYPE_GENERIC);
        SourceGroup[] xmlgroups = src.getSourceGroups("xml");//NOI18N

        if (jsgroups.length == 0 && xmlgroups.length == 0) {
            // Probably used as part of some non-JavaScript-related project refactoring operation (#106987)
            return null;
       }
        
        SourceGroup[] allgroups =  new SourceGroup[jsgroups.length + xmlgroups.length];
        System.arraycopy(jsgroups,0,allgroups,0,jsgroups.length);
        System.arraycopy(xmlgroups,0,allgroups,allgroups.length-1,xmlgroups.length);
        for(int i=0; i<allgroups.length; i++) {
            if (allgroups[i].getRootFolder().equals(file) || FileUtil.isParentOf(allgroups[i].getRootFolder(), file))
                return allgroups[i];
        }
        return null;
    }
    
    private static SourceGroup getRubySourceGroup(FileObject file) {
        Project prj = FileOwnerQuery.getOwner(file);
        if (prj == null)
            return null;
        Sources src = ProjectUtils.getSources(prj);
//        SourceGroup[] rubygroups = src.getSourceGroups(RubyProject.SOURCES_TYPE_RUBY);
        SourceGroup[] rubygroups = src.getSourceGroups(Sources.TYPE_GENERIC);
        
        for(int i=0; i<rubygroups.length; i++) {
            if (rubygroups[i].getRootFolder().equals(file) || FileUtil.isParentOf(rubygroups[i].getRootFolder(), file))
                return rubygroups[i];
        }
        return null;
    }
    

    public Object getUserObject() {
        return fo;
    }
}
