/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.librarydescriptor;

import java.io.IOException;
import java.util.Set;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileSystem;

/**
 * Wizard <em>J2SE Library Descriptor</em> for registering
 * libraries for end users.
 *
 * @author Radek Matous
 */
final class NewLibraryDescriptor extends BasicWizardIterator {
    
    private static final long serialVersionUID = 1L;
    NewLibraryDescriptor.DataModel data;
    
    public static NewLibraryDescriptor createIterator() {
        return new NewLibraryDescriptor();
    }
    
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }
    
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new NewLibraryDescriptor.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new SelectLibraryPanel(wiz,data ),
                    new NameAndLocationPanel(wiz,data )
        };
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        
        private Library library;
        private String libraryName;
        private String libraryDisplayName;
        
        private CreatedModifiedFiles files;
        
        /** Creates a new instance of NewLibraryDescriptorData */
        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        public Library getLibrary() {
            return library;
        }
        
        public void setLibrary(Library library) {
            this.library = library;
        }
        
        public CreatedModifiedFiles getCreatedModifiedFiles() {
            return files;
        }
        
        public void setCreatedModifiedFiles(CreatedModifiedFiles files) {
            this.files = files;
        }
                        
        public String getLibraryName() {
            return libraryName;
        }
        
        public void setLibraryName(String libraryName) {
            this.libraryName = libraryName;
        }

        public boolean isValidLibraryName() {
            // XXX may need additional conditions, TBD (would need new message in that case)
            return getLibraryName() != null && 
                    getLibraryName().trim().length() != 0;
        }
        
        public String getLibraryDisplayName() {
            return libraryDisplayName;
        }
        
        public void setLibraryDisplayName(String libraryDisplayName) {
            this.libraryDisplayName = libraryDisplayName;
        }
        
        public boolean isValidLibraryDisplayName() {
            return getLibraryDisplayName() != null && 
                    getLibraryDisplayName().trim().length() != 0;
        }
        
        boolean libraryAlreadyExists() {
            FileSystem layerFs = null;
            LayerUtils.LayerHandle handle  = LayerUtils.layerForProject(getProject());
            layerFs = handle.layer(false);
            return (layerFs != null) ? (layerFs.findResource(CreatedModifiedFilesProvider.getLibraryDescriptorEntryPath(getLibraryName())) != null) : false;
        }
                        
        public NewLibraryDescriptor.DataModel cloneMe(WizardDescriptor wiz) {
            NewLibraryDescriptor.DataModel d = new NewLibraryDescriptor.DataModel(wiz);
            d.setLibrary(this.getLibrary());
            d.setPackageName(this.getPackageName());
            d.setCreatedModifiedFiles(this.getCreatedModifiedFiles());
            d.setLibraryDisplayName(this.getLibraryDisplayName());
            d.setLibraryName(this.getLibraryName());
            return d;
        }        
    }
    
}
