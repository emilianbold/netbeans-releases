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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;

/**
 *  Recognizes .h header files and create .h data objects for them
 *
 *  This data loader recognizes .h header data files, creates a data object for
 *  each file, and sets up an appropriate action menus for .h file objects.
 *
 */

public final class HDataLoader extends UniFileLoader {
    private static HDataLoader instance = null;

    /** Serial version number */
    static final long serialVersionUID = -2924582006340980748L;

    /** store the popup menu actions here */
    protected static SystemAction[] standardActions;

    /** The suffix list for C/C++ header files */
    private static final String[] hdrExtensions =
				{ "h", "H", "hpp", "hxx" };		//NOI18N

    public HDataLoader() {
       super("org.netbeans.modules.cnd.loaders.HDataObject");           //NOI18N
       instance = this;
       createExtentions();
    }

    public HDataLoader(Class recognizedObject) {
	super(recognizedObject);
       instance = this;
	createExtentions();
    }

    public static HDataLoader getInstance(){
        return instance;
    }

    public String[] suffixes() {
        ArrayList res = new ArrayList();
        ExtensionList list = getExtensions();
        for (Enumeration e = list.extensions(); e != null &&  e.hasMoreElements();) {
            String ex = (String) e.nextElement();
            res.add(ex);
        }
        return (String[])res.toArray(new String[res.size()]);
    }

    private void createExtentions() {
	// These extensions MUST match the ones in the editor kits...
	ExtensionList extensions = new ExtensionList();
	for (int i = 0; i < hdrExtensions.length; i++) {
	    extensions.addExtension(hdrExtensions[i]);
	}
	setExtensions(extensions);
    }
    
    public boolean resolveMimeType(String ext){
        ExtensionList extensions = getExtensions();
        for (Enumeration e = extensions.extensions(); e != null &&  e.hasMoreElements();) {
            String ex = (String) e.nextElement();
            if (ex != null && ex.equals(ext))
                return true;
        }
        return false;
    }

    /**
     *  Defer creating the SystemAction array until its actually needed.
     */
    protected SystemAction[] createDefaultActions() {
	return new SystemAction[] {
	    SystemAction.get(OpenAction.class),
	    SystemAction.get(FileSystemAction.class),
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
	return NbBundle.getMessage(HDataLoader.class, "PROP_HDataLoader_Name"); // NOI18N
    }


    /**
     *  Recognize a header file.
     */
    protected FileObject findPrimaryFile(FileObject fo) {

	// Never recognize folders...
	if (fo.isFolder()) {
	    return null;
	}

	// Check if its an SCCS file. Ignore it (return NULL) if it is
	FileObject parent = fo.getParent();
	if (parent != null && parent.getName().equals("SCCS")) { // NOI18N
		return null;
	}
	FileObject res = super.findPrimaryFile(fo);
        if (res == null){
            if (detectCPPByComment(fo)){
                res = fo;
            }
        }
        return res;
    }

    public static boolean detectCPPByComment(FileObject fo){
        boolean ret = false;
       InputStreamReader isr = null;
       BufferedReader br = null;
        try {
            if (fo.canRead() && fo.getExt().length()==0){
                isr = new InputStreamReader(fo.getInputStream());
                br = new BufferedReader(isr);
                String line = null;
                try {
                    line = br.readLine();
                } catch (IOException ex) {
                }
                if(line != null){
                    if (line.startsWith("//") && line.indexOf("-*- C++ -*-")>0){
                        ret = true;
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (br != null){
                try {
                    br.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (isr != null){
                try {
                    isr.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return ret;
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile)
	throws DataObjectExistsException, IOException {
	return new HDataObject(primaryFile, this);
    }
  
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj,
			    FileObject primaryFile) {
	return new CCFSrcLoader.CCFFormat(obj, primaryFile);
    }
}

