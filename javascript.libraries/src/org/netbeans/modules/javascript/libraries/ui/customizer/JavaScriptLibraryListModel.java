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

package org.netbeans.modules.javascript.libraries.ui.customizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.event.TableModelListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.javascript.libraries.util.JSLibraryProjectUtils;
import org.netbeans.modules.javascript.libraries.util.JSLibraryData;
import org.openide.util.NbBundle;

/**
 *
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
class JavaScriptLibraryListModel extends AbstractListModel {
    private List<LibraryRow> libraryData;
    private List<TableModelListener> listeners;    
    private final Project project;
    
    public JavaScriptLibraryListModel(Project project) {
        this.libraryData = new ArrayList<LibraryRow>();
        this.listeners = new ArrayList<TableModelListener>();
        this.project = project;
        
        init();
    }

    private void init() {
        Set<JSLibraryData> data = JSLibraryProjectUtils.getJSLibraryData(project);
        for (JSLibraryData entry : data) {
            libraryData.add(new LibraryRow(entry.getLibraryName(), entry.getLibraryLocation()));
        }
    }

    public int getSize() {
        return libraryData.size();
    }

    public Object getElementAt(int index) {
        return libraryData.get(index).getDisplayName();
    }

    public String getLibraryNameAt(int rowIndex) {
        return libraryData.get(rowIndex).getLibraryName();
    }
    
    public String getLibraryLocationAt(int rowIndex) {
        return libraryData.get(rowIndex).getDisplayLocation();
    }
    
    public void appendLibrary(String name, String location) {
        libraryData.add(new LibraryRow(name, location));
        
        int index = libraryData.size()-1;
        fireIntervalAdded(this, index, index);
    }
    
    public void removeLibrary(int index) {
        libraryData.remove(index);
        fireIntervalRemoved(this, index, index);
    }
    
    public void removeLibrary(String libraryName) {
        for (int i = 0; i < libraryData.size(); i++) {
            if (libraryData.get(i).getLibraryName().equals(libraryName)) {
                removeLibrary(i);
                return;
            }
        }
    }
    
    public void changeLibrary(int index, String name, String location) {
        LibraryRow changedRow = libraryData.get(index);
        name = (name != null) ? name : changedRow.getLibraryName();
        location = (location != null) ? location : changedRow.getLibraryLocation();
        
        libraryData.set(index, new LibraryRow(name, location));
        fireContentsChanged(this, index, index);
    }
    
    private final class LibraryRow {
        private final String libraryName;
        private final String libraryLocation;
        private final String displayName;
        private final String displayLocation;
        
        public LibraryRow(String libName, String libLocation) {
            assert libName != null;
            
            this.libraryName = libName;
            this.libraryLocation = libLocation;
            
            LibraryManager manager = JSLibraryProjectUtils.getLibraryManager(project);
            Library library = manager.getLibrary(libName);
            
            if (library == null) {
                this.displayName = getDefaultDisplayName();
            } else if (this.libraryLocation == null) {
                this.displayName = NbBundle.getMessage(JavaScriptLibraryListModel.class, 
                        "CustomizerJSLibraries_NoLocation", library.getDisplayName());
            } else if (JSLibraryProjectUtils.isLibraryFolderEmpty(project, library, this.libraryLocation)) {
                this.displayName = NbBundle.getMessage(JavaScriptLibraryListModel.class, 
                        "CustomizerJSLibraries_NoFilesForLibrary", library.getDisplayName());
            } else {
                this.displayName = library.getDisplayName();
            }
            
            this.displayLocation = (libraryLocation != null) ? libraryLocation :
                NbBundle.getMessage(JavaScriptLibraryListModel.class, "CustomizerJSLibraries_MissingLocation");
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDisplayLocation() {
            return displayLocation;
        }
        
        public String getLibraryLocation() {
            return libraryLocation;
        }

        public String getLibraryName() {
            return libraryName;
        }
        
        private String getDefaultDisplayName() {
            return NbBundle.getMessage(JavaScriptLibraryListModel.class, "CustomizerJSLibraries_MissingReference", libraryName);
        }
    }
}
