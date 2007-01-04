/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.loaders;

import java.io.IOException;
import java.io.File;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.MIMENames;
import org.netbeans.modules.cnd.actions.MakeAction;
import org.netbeans.modules.cnd.actions.MakeCleanAction;
import org.netbeans.modules.cnd.actions.MakeTargetAction;


/**
 *  Recognizes single files in the Repository as being of a certain type.
 */
public class MakefileDataLoader extends MultiFileLoader {

    /** Serial version number */
    static final long serialVersionUID = -7148711275717543299L;

    /** Count of files we've recognized. Only incremented for Unix files. */
    private static int count = 0;

    /** Mark a file as a Makefile */
    public static final String PROP_MAKEFILE_TYPE = "MAKEFILE_TYPE";	//NOI18N

    /** store the popup menu actions here */
    protected static SystemAction[] standardActions;
    
    /** a list of well known file extensions */
    protected static String extensionsList[] = 
                               { "cc", "c", "cpp", "h", "java",      //NOI18N
                                 "c++", "gif", "xml", "ser",         //NOI18N
                                 "html", "instance", "settings",     //NOI18N
                                 "f", "f90", "f95", "for", };        //NOI18N

    /**
     *  Default constructor
     */
    public MakefileDataLoader() {
	super("org.netbeans.modules.cnd.loaders.MakefileDataObject");   //NOI18N
    }
    
    public MakefileDataLoader(String recognizedClassName) {
	super(recognizedClassName);
    }
  
    public MakefileDataLoader(Class recognizedClass) {
	super(recognizedClass);
    }
  

    /**
     *  Defer creating the SystemAction array until its actually needed.
     */
    protected SystemAction[] createDefaultActions() {
	return new SystemAction[] {
	    SystemAction.get(OpenAction.class),
	    SystemAction.get(FileSystemAction.class),
	    null,
	    SystemAction.get(MakeAction.class),
	    SystemAction.get(MakeCleanAction.class),
	    SystemAction.get(MakeTargetAction.class),
	    //SystemAction.get(RunTargetsAction.class),
	    null,
	    SystemAction.get(CutAction.class),
	    SystemAction.get(CopyAction.class),
	    SystemAction.get(PasteAction.class),
	    null,
	    SystemAction.get(DeleteAction.class),
	    SystemAction.get(RenameAction.class),
	    null,
	    SystemAction.get(SaveAsTemplateAction.class),
	    null,
	    SystemAction.get(ToolsAction.class),
	    SystemAction.get(PropertiesAction.class),
	};
    }


    /**
     *  Return the SystemAction[]s array. Create it and store it if needed.
     *
     *  @return The SystemAction[] array
     */
    protected SystemAction[] defaultActions() {
    
	if (standardActions != null) {
	    return standardActions;
	} else {
	    synchronized(getClass()) {
		if (standardActions == null) {
		    standardActions = createDefaultActions();
		}
	    }
	}

	return standardActions;
    }


    /** set the default display name */
    protected String defaultDisplayName() {
	return NbBundle.getMessage(MakefileDataLoader.class,
			    "PROP_MakefileDataLoader_Name"); // NOI18N
    }
  

    /**
     *  Create the DataObject.
     */
    protected MultiDataObject createMultiObject(FileObject primaryFile)
	throws DataObjectExistsException, IOException {
	return new MakefileDataObject(primaryFile, this);
    }
  

    /**
     *  Create the primary Entry in the MultiDataObject.
     */
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj,
			    FileObject primaryFile) {
	return new CCFSrcLoader.CCFFormat(obj, primaryFile);
    }
    

    /**
     *  Create a secondary Entry.
     */
    protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj,
					FileObject secondaryFile) {
	return new FileEntry.Numb(obj, secondaryFile);
    }


    /**
     *  Call the static method we use to find the primary file.
     */
    protected FileObject findPrimaryFile(FileObject fo) {
	return staticFindPrimaryFile(fo);
    }
    

    /**
     *  Find the primary file. This is static because we need to use this
     *  during Makefile compilation but we don't have easy access to the
     *  DataLoader object.
     */
    static public FileObject staticFindPrimaryFile(FileObject fo) {

	if (fo.isFolder()) {
	    return null;
	}

	// Get name information
	String name = fo.getName().toLowerCase();
	String ext = fo.getExt();
	String fullname = fo.getNameExt();
        
        if (isWellKnownExtension(ext)) {
            return null;
        }

	// Check if its an SCCS file. Ignore it (return NULL) if it is
	FileObject parent = fo.getParent();
	if (parent != null && parent.getName().equals("SCCS")) {    // NOI18N
		return null;
	}

	/*
	 * Some Makefiles don't follow standard Makefile naming conventions.
	 * If they have a PROP_MAKEFILE_TYPE property we still recognise them
	 * as a Makefile.
	 */
	if (fo.getAttribute(PROP_MAKEFILE_TYPE) != null) {
	    countFile();
	    return fo;
	}

	// Check for a .make.state file. This is secondary to Makefile.
	// 
	// The following code is problematic for several reasons:
	// 1) there is no one-to-one
	// mapping between makefiles and make state files. Several different makefiles
	// can use the same make state file. For instance makefiles in the same
	// directory using .KEEP.STATE. does use the same make state file '.make.state'.
	// Mapping it to one particular makefile can cause all kind of problems when you
	// remove or move the makefiles.
	// 2) the algoritme below is based on some assumtion that is not always true. 
	// For instance if you use the output directory in the name of the make state file
	// like '.make.state.myoutputdirectory', you can end up with the created output 
	// directiory 'myoutputdirectory' is being marked as a makefile and cannot be
	// accessed from the explorer (Hao's bug).
	// 3) The algorithme will pair both 'make.state' and '.make.state.Makefile' to 
	// the same makefile 'Makefile'. 
	//
	// I will disable this code for now.
	//
	/*if (fullname.startsWith(".make.state")) {			//NOI18N
	    FileObject fm = null;

	    if (fullname.length() == 11) {
		fm = findMakefile(fo, "Makefile");			//NOI18N
		if (fm == null) {
		    fm = findMakefile(fo, "makefile");			//NOI18N
		}
	    } else if (fullname.length() > 12 && fullname.charAt(11) == '.') {
		fm = findMakefile(fo, fullname.substring(12));
	    }
	    fo.setImportant(false);
	    if (fm != null) {
		fm.setImportant(true);
	    }
	    countFile();
	    return fm;
	} else */
	// Check for various (somewhat) standard Makefile names.
	if (ext.equals("mk") ||	      			        //NOI18N
		name.startsWith("makefile") ||                      //NOI18N
		name.endsWith("makefile") ||			//NOI18N
		name.startsWith("gnumakefile")) {			//NOI18N
	    countFile();
	    return fo;
	}
	return null;
    }

    /** Check a file extension to determine if it is a well known extension as
     *  defined by the list of well known extensions in this class.
     *  @param extension the file extension to verify.
     *  @return true if the extension is in the list of well known extensions.
     */
    private static boolean isWellKnownExtension(String extension) {
	if (extension != null && !extension.equals("")) {           //NOI18N
	    String ext = extension.toLowerCase();
	    for (int i = 0; i < extensionsList.length; i++) {
		if (ext.equals(extensionsList[i])) {
		    return true;
		}
	    }
	}
        return false;
    }

    /**
     *  Count Unix files we've recognised. The class which wants this data is
     *  the ElfTaster so we only count files on Unix systems. Not all Unix
     *  systems run Elf but Elf only runs on Unix.
     *  <P>
     *  We check for Unix via comparison of File.separatorChar and '/' rather
     *  than doing a string compare on os.name because its faster.
     */
    private static void countFile() {

	if (File.separatorChar == '/') {
	    count++;
	}
    }

    public static int getMakefileCount() {
	return count;
    }


    /** Find the Makefile associated with a .make.state* file */
    private static FileObject findMakefile(FileObject fo, String name) {

        if (fo == null) {
	    return null;
	}
        FileObject parent = fo.getParent();
        if (parent == null) {
	    return null;
	}

        return parent.getFileObject(name);
    }
}

