/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.nav;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.Index.SearchScope;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.IndexSearcher;
import org.netbeans.modules.gsf.api.IndexSearcher.Descriptor;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.NbUtilities;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class PHPTypeSearcher implements IndexSearcher {

    public Set<? extends Descriptor> getTypes(Index gsfIndex, String textForQuery, NameKind kind, EnumSet<SearchScope> scope, Helper helper) {
        PHPIndex index = PHPIndex.get(gsfIndex);
        
        if (index == null) {
            return Collections.emptySet();
        }
        
        //#132529: consider after this is solved:
//        if (kind == NameKind.CASE_INSENSITIVE_PREFIX) {
//            textForQuery = textForQuery.toLowerCase();
//        }
        
        int doubleColon = textForQuery.indexOf("::");
        Set<PHPTypeDescriptor> result = new HashSet<PHPTypeDescriptor>();
        
        if (doubleColon != (-1)) {
            String className = textForQuery.substring(0, doubleColon);
            String rest = textForQuery.substring(doubleColon + 2);
            
            for (IndexedClass clazz : index.getClasses(null, className, kind)) {
                for (IndexedFunction func : index.getMethods(null, clazz.getName(), rest, kind, PHPIndex.ANY_ATTR)) {
                    result.add(new PHPTypeDescriptor(func, clazz, helper));
                }
            }
        } else {
            for (IndexedElement el : index.getFunctions(null, textForQuery, kind)) {
                result.add(new PHPTypeDescriptor(el, helper));
            }
            for (IndexedElement el : index.getClasses(null, textForQuery, kind)) {
                result.add(new PHPTypeDescriptor(el, helper));
            }
            for (IndexedElement el : index.getConstants(null, textForQuery, kind)) {
                result.add(new PHPTypeDescriptor(el, helper));
            }
        }

        return result;
    }

    public Set<? extends Descriptor> getSymbols(Index gsfIndex, String textForQuery, NameKind kind, EnumSet<SearchScope> scope, Helper helper) {
        return getTypes(gsfIndex, textForQuery, kind, scope, helper);
    }

    private class PHPTypeDescriptor extends Descriptor {
        private final IndexedElement element;
        private final IndexedElement enclosingClass;
        private String projectName;
        private Icon projectIcon;
        private final Helper helper;
        
        public PHPTypeDescriptor(IndexedElement element, Helper helper) {
            this(element, null, helper);
        }
        
        public PHPTypeDescriptor(IndexedElement element, IndexedElement enclosingClass, Helper helper) {
            this.element = element;
            this.enclosingClass = enclosingClass;
            this.helper = helper;
        }

        public Icon getIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            return helper.getIcon(element);
        }

        public String getTypeName() {
            return element.getName();
        }

        public String getProjectName() {
            if (projectName == null) {
                initProjectInfo();
            }
            return projectName;
        }

        private void initProjectInfo() {
            FileObject fo = element.getFileObject();
            if (fo != null) {
                Project p = FileOwnerQuery.getOwner(fo);
                if (p != null) {
                    ProjectInformation pi = ProjectUtils.getInformation(p);
                    projectName = pi.getDisplayName();
                    projectIcon = pi.getIcon();
                }
            } else {
                Logger.getLogger(PHPTypeSearcher.class.getName()).fine("No fileobject for " + element.toString() + " with fileurl=" + element.getFilenameUrl());
            }
            if (projectName == null) {
                projectName = "";
            }
        }
        
        public Icon getProjectIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            return projectIcon;
        }

        public FileObject getFileObject() {
            return element.getFileObject();
        }

        public void open() {
            NbUtilities.open(element.getFileObject(), element.getOffset(), element.getName());
        }

        public String getContextName() {
            StringBuilder sb = new StringBuilder();
            boolean s = false;
            if (enclosingClass != null) {
                sb.append(enclosingClass.getName());
                s = true;
            }
            FileObject file = getFileObject();
            if (file != null) {
                if (s) {
                    sb.append(" in ");
                }
                sb.append(FileUtil.getFileDisplayName(file));
            }
            if (sb.length() > 0) {
                return sb.toString();
            }
            return null;
        }

        public ElementHandle getElement() {
            return element;
        }

        public int getOffset() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getSimpleName() {
            return element.getName();
        }

        public String getOuterName() {
            return null;
        }
    }
}
