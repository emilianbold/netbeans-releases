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

package org.openidex.search;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.HelpCtx;

/**
 *
 * @author  Marian Petras
 */
public class DummyDataObject extends MultiDataObject {
    
    public DummyDataObject(FileObject fo, MultiFileLoader loader)
                                            throws DataObjectExistsException {
        super(fo, loader);
    }

    public boolean isDeleteAllowed() {
        return false;
    }

    public boolean isCopyAllowed() {
        return false;
    }

    public boolean isMoveAllowed() {
        return false;
    }

    public boolean isRenameAllowed() {
        return false;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected DataObject handleCopy(DataFolder f) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void handleDelete() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected FileObject handleRename(String name) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected FileObject handleMove(DataFolder df) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
