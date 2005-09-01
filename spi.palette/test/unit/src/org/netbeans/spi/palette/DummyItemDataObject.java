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

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;


/**
 *
 * @author sa154850
 */
public class DummyItemDataObject extends MultiDataObject {
    
    /** Creates a new instance of DummyItemDataObject */
    DummyItemDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
    }

    protected org.openide.nodes.Node createNodeDelegate() {

        org.openide.nodes.Node retValue;
        
        retValue = super.createNodeDelegate();
        return retValue;
    }
    
}
