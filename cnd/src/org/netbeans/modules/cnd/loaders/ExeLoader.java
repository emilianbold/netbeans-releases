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

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.MIMENames;

/**
 *  Recognizes EXE files (Windows, Linux, and Solaris executables, shared objects and
 *  core files).
 */
public class ExeLoader extends UniFileLoader {

    /** Serial version number */
    static final long serialVersionUID = -602486606840357846L;

    /** store the popup menu actions here */
    protected static SystemAction[] standardActions;

    /** Single depth cache of last MIME type */
    private static String lastMime;
    
    /** Single depth cache of FileObjects */
    private static FileObject lastFo;

    private static final String KNOWN_EXEFILE_TYPE =
	    "org.netbeans.modules.cnd.ExeLoader.KNOWN_EXEFILE_TYPE";	//NOI18N

    private static ExeLoader DEFAULT = null;

    public ExeLoader() {
	super("org.netbeans.modules.cnd.loaders.ExeObject");            //NOI18N
	DEFAULT = this;
    }

    public ExeLoader(Class recognizedClass) {
	super(recognizedClass);
	DEFAULT = this;
    }

    public ExeLoader(String representationClassName) {
	super(representationClassName);
	DEFAULT = this;
    }

    /** Keeps track of number of added actions. */
    private static int additionalActions = 0;

    // This method only inserts after the execute action
    /*
    public static synchronized void addAction(SystemAction a) {
	if (a != null) {
            SystemAction execAction = SystemAction.get(RunDialogAction.class);
	    SystemAction[] currentActions = DEFAULT.getActions();
	    int numActions = currentActions.length + 1;
	    if (additionalActions == 0)
		numActions++; // allow for adding a separator
	    SystemAction[] newActions = new SystemAction[numActions];
	    Boolean insertNewAction = Boolean.TRUE;
	    int j = 0;
	    
	    for (int i = 0; i < currentActions.length; i++) {
		newActions[j] = currentActions[i]; // save current action
		if (currentActions[i] != null) {
		    if (currentActions[i].equals(execAction) && 
			insertNewAction == Boolean.TRUE) {
			newActions[++j] = currentActions[++i]; // save null sep
			newActions[++j] = a;                // insert new action
			if (additionalActions == 0) { 
			    // only add an ending separator once,
			    // after the first action is added!
			    newActions[++j] = null;
			}
			additionalActions++;
			insertNewAction = Boolean.FALSE;
		    }
                }
		j++;
	    } // end for loop
	    // Make sure we refresh the action list
	    DEFAULT.setActions(newActions);
	} // end if (a != null)
    }
    */

    // Inserts action at top of action list
    public static synchronized void addAction(SystemAction a) {
	if (a != null) {
	    SystemAction[] currentActions = DEFAULT.getActions();
	    int numActions = currentActions.length + 1;
	    SystemAction[] newActions = new SystemAction[numActions];

	    newActions[0] = a;
	    for (int i = 0; i < currentActions.length; i++) {
		newActions[i+1] = currentActions[i];
	    }
	    // Make sure we refresh the action list
	    DEFAULT.setActions(newActions);
	} 
    }

    // Inserts action after a sepcified action
    public static synchronized void addAction(SystemAction a, SystemAction after) {
	if (a != null) {
	    SystemAction[] currentActions = DEFAULT.getActions();
	    SystemAction[] newActions = new SystemAction[currentActions.length + 1];

	    int j = 0;
	    for (int i = 0; i < currentActions.length; i++) {
		newActions[j++] = currentActions[i];
		if (currentActions[i] == after) {
		    newActions[j++] = a;                // insert new action
		}
	    }
	    // Make sure we refresh the action list
	    DEFAULT.setActions(newActions);
	} 
    }

    // Inserts action at a sepcified index
    public static synchronized void addAction(SystemAction a, int index) {
	if (a != null) {
	    SystemAction[] currentActions = DEFAULT.getActions();
	    if (currentActions.length >= index) {
		SystemAction[] newActions = new SystemAction[currentActions.length + 1];

		int j = 0;
		for (int i = 0; i < currentActions.length; i++) {
		    if (i == index) {
			newActions[j++] = a;                // insert new action
		    }
		    newActions[j++] = currentActions[i];
		}
		// Make sure we refresh the action list
		DEFAULT.setActions(newActions);
	    }
	} 
    }

    public static synchronized void removeAction(SystemAction a) {
	if (a != null) {
	    SystemAction[] currentActions = DEFAULT.getActions();
	    int numActions = currentActions.length - 1;
	    if (--additionalActions == 0)
		numActions--;             // will be removing unneeded separator
	    SystemAction[] newActions = new SystemAction[numActions];
	    int j = 0;
	    
	    for (int i = 0; i < numActions; i++) {
		if (currentActions[j] != null) {
                    if (currentActions[j].equals(a)) {
			j++;                          // skip this action
			if (additionalActions == 0) { // if no additnl actions
			    j++;                      // don't insert null sep
			}
		    }
		}
		newActions[i] = currentActions[j];    // save the action
		j++;
            }
	    // Make sure we refresh the action list
	    DEFAULT.setActions(newActions);
	}
    }

    public static ExeLoader getDefaultExeLoader() {
	return DEFAULT;
    }
    
    /**
     *  Defer creating the SystemAction array until its actually needed.
     */
    protected SystemAction[] createDefaultActions() {
	int numActionEntries = 12;
	
	SystemAction[] act = new SystemAction[numActionEntries];

	int j = 0;
	act[j++] = null;
	act[j++] = SystemAction.get(FileSystemAction.class);
	act[j++] = null;
	act[j++] = SystemAction.get(CutAction.class);
	act[j++] = SystemAction.get(CopyAction.class);
	act[j++] = SystemAction.get(PasteAction.class);
	act[j++] = null;
	act[j++] = SystemAction.get(DeleteAction.class);
	act[j++] = SystemAction.get(RenameAction.class);
	act[j++] = null;
	act[j++] = SystemAction.get(ToolsAction.class);
	act[j++] = SystemAction.get(PropertiesAction.class);

	if (numActionEntries != j &&
	    Boolean.getBoolean("netbeans.debug.exceptions")) {  // NOI18N
	    Thread.dumpStack();
	}
	return act;
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
			    "PROP_ExeLoader_Name"); // NOI18N
    }
  
  

    protected FileObject findPrimaryFile(FileObject fo) {
	String mime;

	if (fo.isFolder()) {
	    return null;
	}

	Object o = fo.getAttribute(KNOWN_EXEFILE_TYPE);
	if (o != null) {
	    mime = o.toString();
	} else {
	    mime = fo.getMIMEType();
	    if (mime.equals("application/x-exe")) { //NOI18N
		mime = MIMENames.EXE_MIME_TYPE;
	    } else if (mime.equals("application/x-exe+dll")) { //NOI18N
		mime = MIMENames.DLL_MIME_TYPE;
	    } else if (mime.equals("application/x-executable+elf")) { //NOI18N
		mime = MIMENames.ELF_EXE_MIME_TYPE;
	    } else if (mime.equals("application/x-core+elf")) { //NOI18N
		mime = MIMENames.ELF_CORE_MIME_TYPE;
	    } else if (mime.equals("application/x-shobj+elf")) { //NOI18N
		mime = MIMENames.ELF_SHOBJ_MIME_TYPE;
	    } else if (mime.equals("application/x-object+elf")) { //NOI18N
		mime = MIMENames.ELF_OBJECT_MIME_TYPE;
	    } else if ("application/x-elf".equals(mime)) { //NOI18N
		// Fallback matching. We shouldn't see this anymore.
		String name = fo.getNameExt();
		if (name.startsWith("core") || name.endsWith(".core")) { //NOI18N
		    mime = MIMENames.ELF_CORE_MIME_TYPE;
		} else if (name.endsWith(".o")) { //NOI18N
		    mime = MIMENames.ELF_OBJECT_MIME_TYPE;
		} else if (name.endsWith(".so") || name.indexOf(".so.") >= 0) { //NOI18N
		    mime = MIMENames.ELF_SHOBJ_MIME_TYPE;
		} else {
		    mime = MIMENames.ELF_EXE_MIME_TYPE;
		}
	    }
	}

	if (MIMENames.EXE_MIME_TYPE.equals(mime) ||
                    MIMENames.DLL_MIME_TYPE.equals(mime) ||
                    MIMENames.ELF_EXE_MIME_TYPE.equals(mime) ||
		    MIMENames.ELF_CORE_MIME_TYPE.equals(mime) ||
		    MIMENames.ELF_SHOBJ_MIME_TYPE.equals(mime) ||
		    MIMENames.ELF_OBJECT_MIME_TYPE.equals(mime)) {
	    lastMime = mime;
	    lastFo = fo;

	    try {
		fo.setAttribute(KNOWN_EXEFILE_TYPE, mime);
	    } catch (IOException ex) {
		// We've figured out the mime type, which is the main thing this
		// method needed to do. Its much less important that we couldn't
		// save it. So we just ignore the exception!
	    }

	    return fo;
	} else {
	    return null;
	}
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile)
			throws DataObjectExistsException, IOException {
	String mime;

	if (lastFo.equals(primaryFile)) {
	    mime = lastMime;
	} else {
	    mime = primaryFile.getMIMEType();
	}

	if (mime.equals(MIMENames.EXE_MIME_TYPE)) {
	    return new ExeObject(primaryFile, this);
//	} else if (mime.equals(MIMENames.DLL_MIME_TYPE)) {
//	    return new ExeDllObject(primaryFile, this);
	} else if (mime.equals(MIMENames.ELF_EXE_MIME_TYPE)) {
	    return new ExeElfObject(primaryFile, this);
	} else if (mime.equals(MIMENames.ELF_CORE_MIME_TYPE)) {
	    return new CoreElfObject(primaryFile, this);
	} else if (mime.equals(MIMENames.ELF_SHOBJ_MIME_TYPE)) {
	    return new DllObject(primaryFile, this);
	} else {
	    return new OrphanedElfObject(primaryFile, this);
	}
    }
}
