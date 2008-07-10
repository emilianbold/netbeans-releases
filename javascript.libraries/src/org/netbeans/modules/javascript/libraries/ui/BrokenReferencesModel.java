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

package org.netbeans.modules.javascript.libraries.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.javascript.libraries.spi.ProjectJSLibraryManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
final class BrokenReferencesModel extends AbstractListModel {
    private enum Cause { MISSING_REFERENCE, NON_JAVASCRIPT_REFERENCE };
    private final Project project;
    private final List<ListItem> referenceItems;
    
    public BrokenReferencesModel(Project project) {
        this.project = project;
        
        this.referenceItems = new ArrayList<ListItem>();
        refreshList();
    }
    
    public int getSize() {
        return referenceItems.size();
    }

    public Object getElementAt(int index) {
        String bundleKey = "";
        ListItem item = referenceItems.get(index);
        
        switch(item.getCause()) {
            case MISSING_REFERENCE:
                bundleKey = "LBL_BrokenReferenceCustomizer_BrokenLibrary";
                break;
            case NON_JAVASCRIPT_REFERENCE:
                bundleKey = "LBL_BrokenReferenceCustomizer_MismatchLibrary";
                break;
        }
        
        return NbBundle.getMessage(BrokenReferencesModel.class, bundleKey, item.getLibraryName());
    }

    public String getLibraryNameAt(int index) {
        return referenceItems.get(index).getLibraryName();
    }
    
    public String getDescriptionFor(int index) {
        String bundleKey = "";
        ListItem item = referenceItems.get(index);
        
        switch(item.getCause()) {
            case MISSING_REFERENCE:
                bundleKey = "BrokenReferencesCustomizer_BrokenLibraryDesc";
                break;
            case NON_JAVASCRIPT_REFERENCE:
                bundleKey = "BrokenReferencesCustomizer_MismatchLibraryDesc";
                break;
        }
        
        return NbBundle.getMessage(BrokenReferencesModel.class, bundleKey, item.getLibraryName());
    }
    
//    
//    public void removeElementAt(int index) {
//        referenceItems.remove(index);
//        this.fireIntervalRemoved(this, index, index);
//    }
    
    public List<String> refreshList() {
        List<String> resolvedRefs = new ArrayList<String>();
        for (ListItem item : referenceItems) {
            resolvedRefs.add(item.getLibraryName());
        }
        
        referenceItems.clear();
        Set<String> libNames = ProjectJSLibraryManager.getJSLibraryNames(project);
        for (String libName : libNames) {
            Library library = LibraryManager.getDefault().getLibrary(libName);
            if (library == null) {
                referenceItems.add(new ListItem(libName, Cause.MISSING_REFERENCE));
            } else if (!library.getType().equals("javascript")) { // NOI18N
                referenceItems.add(new ListItem(libName, Cause.NON_JAVASCRIPT_REFERENCE));
            }
        }
        
        this.fireContentsChanged(this, 0, getSize());
        for (int i = resolvedRefs.size()-1; i >= 0; i--) {
            boolean found = false;
            for (int j = 0; j < referenceItems.size(); j++) {
                if (referenceItems.get(j).getLibraryName().equals(resolvedRefs.get(i))) {
                    found = true;
                    break;
                }
            }
            
            if (found) {
                resolvedRefs.remove(i);
            }
        }
        
        return resolvedRefs;
    }
    
    private static final class ListItem {
        private final String libraryName;
        private final Cause cause;
        
        public ListItem(String libraryName, Cause cause) {
            this.libraryName = libraryName;
            this.cause = cause;
        }
        
        public String getLibraryName() {
            return libraryName;
        }
        
        public Cause getCause() {
            return cause;
        }
    }
    
}
