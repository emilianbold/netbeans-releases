/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jayme C. Edwards, Jesse Glick
 */
 
package org.apache.tools.ant.module.loader;

import java.io.*;

import org.openide.*;
import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.SafeException;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.nodes.RunTargetsAction;

/** Recognizes single files in the Repository as being of Ant Project type.
 */
public class AntProjectDataLoader extends UniFileLoader {
    private static final String REQUIRED_MIME = "text/x-ant+xml"; // NOI18N
    private static final String KNOWN_ANT_FILE = "org.apache.tools.ant.module.loader.AntProjectDataLoader.KNOWN_ANT_FILE"; // NOI18N
    private static final String KNOWN_ANT_FILE_OLD = "org.apache.tools.ant.module.AntProjectDataLoader.KNOWN_ANT_FILE"; // NOI18N

    private static final long serialVersionUID = 3642056255958054115L;

    public AntProjectDataLoader () {
        super ("org.apache.tools.ant.module.loader.AntProjectDataObject"); // NOI18N
    }

    protected String defaultDisplayName () {
        return NbBundle.getMessage (AntProjectDataLoader.class, "LBL_loader_name");
    }

    protected void initialize () {
        super.initialize ();
        // #9582: use declarative MIME types.
        getExtensions().addMimeType(REQUIRED_MIME);
    }

    protected SystemAction[] defaultActions () {
        return new SystemAction[] {
            SystemAction.get (OpenAction.class),
            SystemAction.get (FileSystemAction.class),
            null,
            SystemAction.get (OpenLocalExplorerAction.class),
            null,
            SystemAction.get (ExecuteAction.class),
            SystemAction.get (RunTargetsAction.class),
            null,
            SystemAction.get (CutAction.class),
            SystemAction.get (CopyAction.class),
            SystemAction.get (PasteAction.class),
            null,
            SystemAction.get (ReorderAction.class),
            null,
            SystemAction.get (DeleteAction.class),
            SystemAction.get (RenameAction.class),
            null,
            SystemAction.get (NewAction.class),
            null,
            SystemAction.get (SaveAsTemplateAction.class),
            null,
            SystemAction.get (ToolsAction.class),
            SystemAction.get (PropertiesAction.class),
        };
    }

    // BuildProjectAction etc. were removed. Ignore SafeException.
    public void readExternal (ObjectInput oi) throws IOException, ClassNotFoundException {
        try {
            super.readExternal (oi);
        } catch (SafeException se) {
            AntModule.err.annotate(se, ErrorManager.UNKNOWN, "Reading AntProjectDataLoader: resetting action list to default", null, null, null); // NOI18N
            AntModule.err.notify(ErrorManager.INFORMATIONAL, se);
        }
        ExtensionList xl = getExtensions();
        if (xl.isRegistered(".xml")) { // NOI18N
            AntModule.err.log("#15547: correcting old Ant object type extension list to be MIME format");
            if (xl.mimeTypes().hasMoreElements()) {
                AntModule.err.log("WARNING: old extension list had some MIME types in it, will be kept...");
            }
            xl.removeExtension("xml"); // NOI18N
            if (xl.extensions().hasMoreElements()) {
                AntModule.err.log("WARNING: old extension list had non-.xml extensions in it, will not be converted...");
                setExtensions(xl = new ExtensionList());
            }
            xl.addMimeType(REQUIRED_MIME);
        }
    }
  
    /** Determines whether a given file should be handled by this loader.
     * @param fo the file object to interrogate
     * @return the fileojbect if we will handle it otherwise null
     */
    protected FileObject findPrimaryFile (FileObject fo) {
        FileObject fo2 = super.findPrimaryFile (fo);
        if (fo2 == null) {
            // Incorrect extension or contents.
            return null;
        } else {
            // Ours. Clear any old-style file attributes first.
            clearAttrs(fo2);
            return fo2;
        }
    }

    /** Delete old, no-longer-used marker attributes when possible.
     * @param fo the file object to unremember about
     */
    private static void clearAttrs(FileObject fo) {
        if (fo.getAttribute(KNOWN_ANT_FILE) == null && fo.getAttribute(KNOWN_ANT_FILE_OLD) == null) {
            // Already fine, no need to do anything.
            // Trying to uselessly clear the attr can cause empty .nbattrs to be written etc.
            return;
        }
        if (fo.isReadOnly ()) {
            // Don't even try.
            return;
        }
        try {
            FileSystem fs = fo.getFileSystem ();
            if (! fs.isValid ()) {
                // Unmounted FS; maybe a layer, for example. Skip it.
                return;
            }
            if (fs == TopManager.getDefault ().getRepository ().getDefaultFileSystem ()) {
                // SystemFileSystem. Skip it. We do not want .nbattrs
                // being written all over the user's system folder just because
                // there happen to be some XML files there.
                return;
            }
        } catch (FileStateInvalidException fsie) {
            // Bogus file object, skip it.
            AntModule.err.notify (ErrorManager.INFORMATIONAL, fsie);
            return;
        }
        try {
            fo.setAttribute (KNOWN_ANT_FILE, null);
            fo.setAttribute (KNOWN_ANT_FILE_OLD, null);
        } catch (IOException ioe) {
            AntModule.err.notify (ErrorManager.INFORMATIONAL, ioe);
        }
    }

    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new AntProjectDataObject(primaryFile, this);
    }

}
