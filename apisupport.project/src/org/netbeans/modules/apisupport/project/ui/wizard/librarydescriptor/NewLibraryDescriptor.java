/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.librarydescriptor;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;

/**
 * Wizard <em>J2SE Library Descriptor</em> for registering
 * libraries for end users.
 *
 * @author Radek Matous
 */
public class NewLibraryDescriptor extends BasicWizardIterator {
    private static final long serialVersionUID = 1L;
    NewLibraryDescriptor.DataModel data = null;    
    
    public static NewLibraryDescriptor createIterator() {
        return new NewLibraryDescriptor();
    }
    
    public Set instantiate() throws IOException {
        assert data != null;
        CreatedModifiedFiles fileOperations = data.getCreatedModifiedFiles();
        if (fileOperations != null) {            
            fileOperations.run();
        }
        //TODO: if this returns empty list, it will never get to the list of recent items in New action popup
        return new HashSet();
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
        private Library library = null;
        private String packageName = null;
        private String libraryName = null;
        private String libraryDisplayName = null;        
        
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

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
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

        public String getLibraryDisplayName() {
            return libraryDisplayName;
        }

        public void setLibraryDisplayName(String libraryDisplayName) {
            this.libraryDisplayName = libraryDisplayName;
        }

    }    
}
