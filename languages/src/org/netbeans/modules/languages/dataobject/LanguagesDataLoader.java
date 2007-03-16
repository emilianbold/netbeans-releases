/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.languages.dataobject;

import org.netbeans.modules.languages.LanguagesManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle;

import java.io.IOException;
import org.openide.loaders.FileEntry;


public class LanguagesDataLoader extends MultiFileLoader {

    private static final long serialVersionUID = 1L;

    public LanguagesDataLoader() {
        super("org.netbeans.modules.languages.dataobject.LanguagesDataObject");
    }

    protected String defaultDisplayName() {
        return NbBundle.getMessage(LanguagesDataLoader.class, "LBL_mf_loader_name");
    }

    protected String actionsContext() {
        return "Loaders/Languages/Actions";
    }

    protected FileObject findPrimaryFile(FileObject fo) {
        String mimeType = fo.getMIMEType ();
        if (LanguagesManager.getDefault ().isSupported (mimeType))
            return fo;
        return null;
    }

    protected MultiDataObject createMultiObject (FileObject primaryFile) 
    throws DataObjectExistsException, IOException {
        String mimeType = primaryFile.getMIMEType ();
        if (LanguagesManager.getDefault ().isSupported (mimeType))
            return new LanguagesDataObject (primaryFile, this);
        return null;
    }

    protected Entry createPrimaryEntry (
        MultiDataObject obj,
        FileObject primaryFile
    ) {
        return new FileEntry (obj, primaryFile);
    }

    protected Entry createSecondaryEntry (
        MultiDataObject obj,
        FileObject secondaryFile
    ) {
        return new FileEntry (obj, secondaryFile);
    }
}
