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

import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.modules.languages.LanguagesManagerImpl.LanguagesManagerListener;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

import java.io.IOException;
import java.util.Iterator;

public class LanguagesDataLoader extends UniFileLoader {

    private static final long serialVersionUID = 1L;

    public LanguagesDataLoader() {
        super("org.netbeans.modules.languages.dataobject.LanguagesDataObject");
    }

    protected String defaultDisplayName() {
        return NbBundle.getMessage(LanguagesDataLoader.class, "LBL_mf_loader_name");
    }

    protected void initialize() {
        super.initialize();
        Iterator it = LanguagesManager.getDefault ().getSupportedMimeTypes ().
            iterator ();
        while (it.hasNext ()) {
            String mimeType = (String) it.next ();
            if (mimeType.equals ("text/xml")) continue;
            getExtensions().addMimeType (mimeType);
        }
        ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
            addLanguagesManagerListener (new LanguagesManagerListener () {

            public void languageAdded (String mimeType) {
                getExtensions().addMimeType (mimeType);
            }

            public void languageRemoved (String mimeType) {
                getExtensions().removeMimeType (mimeType);
            }

            public void languageChanged (String mimeType) {
            }
        });
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new LanguagesDataObject(primaryFile, this);
    }

    protected String actionsContext() {
        return "Loaders/Languages/Actions";
    }
}
