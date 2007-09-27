/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.loaders;

import java.io.IOException;
import java.util.Enumeration;

import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

import org.netbeans.modules.cnd.MIMENames;

/** Recognizes single files in the Repository as being of a certain type */
public class MakefileDataLoader extends UniFileLoader {

    /** Serial version number */
    static final long serialVersionUID = -7148711275717543299L;

    /** Mark a file as a Makefile */
    public static final String PROP_MAKEFILE_TYPE = "MAKEFILE_TYPE";	// NOI18N
    
    /** the list of well known extensions which are <b>not</b> Makefiles */
    private ExtensionList wellKnownExtensionsList;
    
    /** list of suffixes which are <b>not</b> Makefiles and not cnd file types */
    private String otherWellKnownExtensionsList[] = {
        "java", "gif", "png", "xml", "properties", "html", "instance", "settings" // NOI18N
    };
    
    private static MakefileDataLoader instance = null;

    public MakefileDataLoader() {
	super("org.netbeans.modules.cnd.loaders.MakefileDataObject"); // NOI18N
        init();
    }
    
    /** Do various initializations */
    private void init() {
        Enumeration en;
        
        instance = this;
        
        /* initialize the extensions list */
        ExtensionList extensionsList = new ExtensionList();
        extensionsList.addExtension("mk"); // NOI18N
        setExtensions(extensionsList);
        
        /* initialize the well known extensions list */
        wellKnownExtensionsList = new ExtensionList();

        /* add C extensions */
        en = CDataLoader.getInstance().getExtensions().extensions();
        while (en.hasMoreElements()) {
            wellKnownExtensionsList.addExtension((String) en.nextElement());
        }
        
        /* add C++ extensions */
        en = CCDataLoader.getInstance().getExtensions().extensions();
        while (en.hasMoreElements()) {
            wellKnownExtensionsList.addExtension((String) en.nextElement());
        }
        
        /* add C/C++ Header extensions */
        en = HDataLoader.getInstance().getExtensions().extensions();
        while (en.hasMoreElements()) {
            wellKnownExtensionsList.addExtension((String) en.nextElement());
        }
        
        /* Add various miscelaneous suffixes to the list */
        for (int i = 0; i < otherWellKnownExtensionsList.length; i++) {
            wellKnownExtensionsList.addExtension(otherWellKnownExtensionsList[i]);
        }
    }

    public static MakefileDataLoader getInstance(){
        if (instance == null) {
            instance = SharedClassObject.findObject(MakefileDataLoader.class, true);
        }
        return instance;
    }
    
    protected String actionsContext () {
        return "Loaders/text/x-make/Actions/"; // NOI18N
    }

    /** set the default display name */
    protected String defaultDisplayName() {
	return NbBundle.getMessage(MakefileDataLoader.class,
			    "PROP_MakefileDataLoader_Name"); // NOI18N
    }

    /** Create the DataObject */
    protected MultiDataObject createMultiObject(FileObject primaryFile)
                throws DataObjectExistsException, IOException {
	return new MakefileDataObject(primaryFile, this);
    }
  

    /**
     *  Create the primary Entry in the MultiDataObject.
     */
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj,
			    FileObject primaryFile) {
	return new CndAbstractDataLoader.CndFormat(obj, primaryFile);
    }

    /** Find the primary file */
    protected FileObject findPrimaryFile(FileObject fo) {
        
        if (fo.isFolder()) {
            return null;
        }

	/*
	 * Some Makefiles don't follow standard Makefile naming conventions.
	 * If they have a PROP_MAKEFILE_TYPE property we still recognise them
	 * as a Makefile.
	 */
	if (fo.getAttribute(PROP_MAKEFILE_TYPE) != null) {
	    return fo;
	}

	if (wellKnownExtensionsList.isRegistered(fo)) {
            return null;
        }

	// Check if its an SCCS file. Ignore it (return NULL) if it is
	FileObject parent = fo.getParent();
	if (parent != null && parent.getName().equals("SCCS")) {    // NOI18N
		return null;
	}

	// Check for various (somewhat) standard Makefile names.
	String name = fo.getName().toLowerCase();
	if (name.startsWith("makefile") || name.endsWith("makefile") ||name.startsWith("gnumakefile")) { // NOI18N
	    return fo;
	}
        
	return super.findPrimaryFile(fo);
    }

    protected String getMimeType(){
        return MIMENames.MAKEFILE_MIME_TYPE;
    }
}

