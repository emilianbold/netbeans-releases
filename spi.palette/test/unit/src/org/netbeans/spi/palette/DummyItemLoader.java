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

package org.netbeans.spi.palette;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;

/**
 *
 * @author sa154850
 */
class DummyItemLoader extends UniFileLoader {
    
    static final String ITEM_EXT = "junit_palette_item"; // NOI18N

    DummyItemLoader() {
        super("org.netbeans.spi.palette.DummyItemDataObject"); // NOI18N

        ExtensionList ext = new ExtensionList();
        ext.addExtension(ITEM_EXT);
        setExtensions(ext);
    }


    protected MultiDataObject createMultiObject(FileObject primaryFile)
        throws DataObjectExistsException, IOException
    {
        return new DummyItemDataObject( primaryFile, this );
    }
    
}
