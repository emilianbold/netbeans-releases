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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * CssFileLoader.java
 *
 * Created on December 8, 2004, 10:06 PM
 */

package org.netbeans.modules.css.loader;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Data loader that recognizes and loads the CSS files
 * @author Winston Prakash
 * @version 1.0
 */
public class CssFileLoader extends UniFileLoader{

    public static final String CSS_MIME_TYPE = "text/x-css"; //NOI18N

    /** Creates a new instance of CssFileLoader */
    public CssFileLoader() {
        super(org.netbeans.modules.css.loader.CssDataObject.class.getName());
    }

    /** Get the default display name of this loader.
     * @return default display name
     */
    protected String defaultDisplayName() {
        return NbBundle.getMessage(CssFileLoader.class, "CssLoaderName"); // NOI18N
    }

    /**
     * Initialize shared state of this shared class (SharedClassObject)
     */
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(CSS_MIME_TYPE);
    }

    protected FileObject findPrimaryFile(FileObject fo) {
        if (fo.isFolder()) {
            return null;
        }

        FileObject primaryFile = super.findPrimaryFile(fo);
        if (primaryFile == null) {
            return null;
        }
        return primaryFile;
    }

    /**
     * Create the data object for a given primary file.
     * @return  data object for the file
     */
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, java.io.IOException {
        return new CssDataObject(primaryFile, this);
    }

    protected String actionsContext() {
        return "Loaders/" + CSS_MIME_TYPE + "/Actions";
    }
}
