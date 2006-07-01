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
