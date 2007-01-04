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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.Date;
import java.util.Map;
import java.text.DateFormat;
import org.netbeans.editor.BaseDocument;

import org.openide.actions.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.ExtensionList;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.cnd.MIMENames;
import org.netbeans.modules.cnd.actions.ShellRunAction;
import org.netbeans.modules.cnd.settings.CppSettings;


/**
 *  Recognizes single files in the Repository as being of a certain type.
 */
public class ShellDataLoader extends UniFileLoader {

    private static ShellDataLoader instance = null;

    /** Serial version number */
    static final long serialVersionUID = -7173746465817543299L;

    /** The suffix list for shell files */
    private static final String[] shellExtensions = {"bash", "csh", "ksh", "sh", "zsh", "bat", "cmd"};	//NOI18N


    /**
     *  Default constructor
     */
    public ShellDataLoader() {
	super("org.netbeans.modules.cnd.loaders.ShellDataObject");   //NOI18N
	instance = this;
	createExtentions();
    }
    
    public ShellDataLoader(String recognizedClassName) {
	super(recognizedClassName);
	instance = this;
	createExtentions();
    }
  
    public ShellDataLoader(Class recognizedClass) {
	super(recognizedClass);
	instance = this;
	createExtentions();
    }

    public static ShellDataLoader getInstance() {
	return instance;
    }

    private void createExtentions() {
	ExtensionList extensions = new ExtensionList();
	for (int i = 0; i < shellExtensions.length; i++) {
	    extensions.addExtension(shellExtensions[i]);
	}
	setExtensions(extensions);
    }
  
    /**
     *  Return the SystemAction[]s array. Create it and store it if needed.
     *
     *  @return The SystemAction[] array
     */
    protected SystemAction[] defaultActions() {
	return new SystemAction[] {
	    SystemAction.get(OpenAction.class),
	    null,
	    SystemAction.get(ShellRunAction.class),
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


    /** set the default display name */
    protected String defaultDisplayName() {
	return NbBundle.getMessage(ShellDataLoader.class, "PROP_ShellDataLoader_Name"); // NOI18N
    }
  

    /**
     *  Create the DataObject.
     */
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
	return new ShellDataObject(primaryFile, this);
    }

    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
	//return new CCFFormat(obj, primaryFile);
	return new CCFFormat(obj, primaryFile);
    }
  

    /**
     *  Call the static method we use to find the primary file.
     */
    protected FileObject findPrimaryFile(FileObject fo) {
	if (fo.isFolder()) {
	    return null;
	}

	String mime = fo.getMIMEType();
	if (mime != null && mime.equals(MIMENames.SHELL_MIME_TYPE)) {
	    return fo;
	}

	return null;
    }

    // Inner class: Substitute important template parameters...
    public static class CCFFormat extends FileEntry.Format {
	public CCFFormat(MultiDataObject obj, FileObject primaryFile) {
	    super(obj, primaryFile);
	}
	protected java.text.Format createFormat(FileObject target, String name, String ext) {
	    Map map = ((CppSettings)CppSettings.findObject(CppSettings.class, true)).getReplaceableStringsProps();
	    map.put("NAME", name);	//NOI18N
	    map.put("DATE", DateFormat.getDateInstance	//NOI18N
		     (DateFormat.LONG).format(new Date()));
	    map.put("TIME", DateFormat.getTimeInstance	//NOI18N
		     (DateFormat.SHORT).format(new Date()));
	    //	    map.put("USER", System.getProperty("user.name"));	//NOI18N
	    map.put("NBDIR", System.getProperty("netbeans.home")); //NOI18N
	    map.put("QUOTES","\""); //NOI18N
	    map.put("EXTENSION", ext); //NOI18N
 	  
	    org.openide.util.MapFormat format = new org.openide.util.MapFormat(map);
	    
	    // Use "%<%" and "%>%" instead of "__" (which most other templates
	    // use) since "__" is used for many C++ tokens and we don't want
	    // any conflicts with valid code. For example, __FILE__ is a valid
	    // construct in Sun C++ files and the compiler will replace the
	    // current file name during compilation.
	    format.setLeftBrace("%<%");	//NOI18N
	    format.setRightBrace("%>%");    //NOI18N
	    return format;
	}

        // This method was taken fom base class to replace "new line" string.
        // Shell scripts shouldn't contains "\r"
        // API doesn't provide method to replace platform dependant "new line" string.
        public FileObject createFromTemplate (FileObject f, String name) throws IOException {
            String ext = getFile ().getExt ();
            if (name == null) {
                name = FileUtil.findFreeFileName(f, getFile ().getName (), ext);
            }
            FileObject fo = f.createData (name, ext);
            java.text.Format frm = createFormat (f, name, ext);
            BufferedReader r = new BufferedReader (new InputStreamReader (getFile ().getInputStream ()));
            try {
                FileLock lock = fo.lock ();
                try {
                    BufferedWriter w = new BufferedWriter (new OutputStreamWriter (fo.getOutputStream (lock)));
                    try {
                        String current;
                        while ((current = r.readLine ()) != null) {
                            w.write (frm.format (current));
                            w.write (BaseDocument.LS_LF);
                        }
                    } finally {
                        w.close ();
                    }
                } finally {
                    lock.releaseLock ();
                }
            } finally {
                r.close ();
            }
            FileUtil.copyAttributes (getFile (), fo);
            setTemplate(fo);
            return fo;
        }
        
        // do what package-local DataObject.setTemplate (fo, false) does
        private void setTemplate(FileObject fo) throws IOException {
            Object o = fo.getAttribute(DataObject.PROP_TEMPLATE);
            if ((o instanceof Boolean) && ((Boolean)o).booleanValue()) {
                fo.setAttribute(DataObject.PROP_TEMPLATE, null);
            }
        }
    }
}
