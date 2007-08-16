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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.loaders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.netbeans.editor.BaseDocument;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.MIMENames;

/**
 *  Recognizes single files in the Repository as being of a certain type.
 */
public class ShellDataLoader extends CndAbstractDataLoader {

    private static ShellDataLoader instance = null;

    /** Serial version number */
    static final long serialVersionUID = -7173746465817543299L;

    /** The suffix list for shell files */
    private static final String[] shellExtensions =
	    {"bash", "csh", "ksh", "sh", "zsh", "bat", "cmd"}; // NOI18N


    /**
     *  Default constructor
     */
    public ShellDataLoader() {
	super("org.netbeans.modules.cnd.loaders.ShellDataObject"); // NOI18N
	instance = this;
	createExtentions(shellExtensions);
    }
    
    public static ShellDataLoader getInstance() {
	return instance;
    }
    
    protected String actionsContext () {
        return "Loaders/text/x-shell/Actions/"; // NOI18N
    }

    protected String getMimeType() {
        return MIMENames.SHELL_MIME_TYPE;
    }

    /** Override because we don't have secondary files */
    protected FileObject findSecondaryFile(FileObject fo){
	return null;
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
	return new ShellFormat(obj, primaryFile);
    }
  

    /** Call the method we use to find the primary file */
    protected FileObject findPrimaryFile(FileObject fo) {
	if (fo.isFolder()) {
	    return null;
	}

        /* First, look for an extension */
        if (getExtensions().isRegistered(fo)) {
            return fo;
        }
        
        /*
         * Now let the mime resolver do it. This could cause bytes to be read in
         * the mime resolver.
         */
	String mime = fo.getMIMEType();
	if (mime != null && mime.equals(MIMENames.SHELL_MIME_TYPE)) {
	    return fo;
	}

	return null;
    }

    // Inner class: Substitute important template parameters...
    public class ShellFormat extends CndFormat {
        
	public ShellFormat(MultiDataObject obj, FileObject primaryFile) {
	    super(obj, primaryFile);
	}

        // This method was taken fom base class to replace "new line" string.
        // Shell scripts shouldn't contains "\r"
        // API doesn't provide method to replace platform dependant "new line" string.
        public FileObject createFromTemplate(FileObject f, String name) throws IOException {
            String ext = getFile().getExt();
            if (name == null) {
                name = FileUtil.findFreeFileName(f, getFile().getName(), ext);
            }
            FileObject fo = f.createData(name, ext);
            java.text.Format frm = createFormat(f, name, ext);
            BufferedReader r = new BufferedReader(new InputStreamReader(getFile().getInputStream()));
            try {
                FileLock lock = fo.lock();
                try {
                    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(fo.getOutputStream(lock)));
                    try {
                        String current;
                        while ((current = r.readLine()) != null) {
                            w.write(frm.format(current));
                            w.write(BaseDocument.LS_LF);
                        }
                    } finally {
                        w.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            } finally {
                r.close();
            }
            FileUtil.copyAttributes(getFile(), fo);
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
