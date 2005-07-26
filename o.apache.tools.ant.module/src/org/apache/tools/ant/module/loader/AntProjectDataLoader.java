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

package org.apache.tools.ant.module.loader;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Recognizes Ant project files according to XML signature.
 */
public class AntProjectDataLoader extends UniFileLoader {

    public static final String REQUIRED_MIME = "text/x-ant+xml"; // NOI18N
    private static final String KNOWN_ANT_FILE = "org.apache.tools.ant.module.loader.AntProjectDataLoader.KNOWN_ANT_FILE"; // NOI18N
    private static final String KNOWN_ANT_FILE_OLD = "org.apache.tools.ant.module.AntProjectDataLoader.KNOWN_ANT_FILE"; // NOI18N
    private static final String KNOWN_ANT_FILENAME = "build.xml"; // NOI18N

    private static final long serialVersionUID = 3642056255958054115L;

    public AntProjectDataLoader () {
        super ("org.apache.tools.ant.module.loader.AntProjectDataObject"); // NOI18N
    }

    protected String defaultDisplayName () {
        return NbBundle.getMessage (AntProjectDataLoader.class, "LBL_loader_name");
    }

    protected void initialize () {
        super.initialize ();
        getExtensions().addMimeType(REQUIRED_MIME);
    }

    protected FileObject findPrimaryFile(FileObject fo) {
        FileObject prim = super.findPrimaryFile(fo);
        if (prim == null && fo.getNameExt().equals(KNOWN_ANT_FILENAME)) {
            // XXX hack for #43871.
            // Does not set the MIME type correctly, but at least should be
            // possible to run targets, etc.
            prim = fo;
        }
        return prim;
    }

    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new AntProjectDataObject(primaryFile, this);
    }

    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions"; // NOI18N
    }

}
