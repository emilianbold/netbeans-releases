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

package org.netbeans.modules.compapp.casaeditor;


import org.openide.actions.*;

import org.openide.filesystems.FileObject;

import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;

import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import java.io.IOException;


/**
 *
 * @author tli
 *
 */
public class CasaDataLoader extends UniFileLoader {

    static final long serialVersionUID = 8066846305969307661L;
    
    public static final String CASA_MIME = "text/x-casa+xml"; // NOI18N

    
    public CasaDataLoader() {
        super("org.netbeans.modules.compapp.casaeditor.CasaDataObject"); // NOI18N
    }


    protected String defaultDisplayName() {
        return NbBundle.getMessage(CasaDataLoader.class, "LBL_loaderName"); // NOI18N
    }

    protected SystemAction[] defaultActions() {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(CutAction.class),
            SystemAction.get(CopyAction.class),
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class),
            null,
            SystemAction.get(ToolsAction.class),
            SystemAction.get(PropertiesAction.class),
        };
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile)
        throws DataObjectExistsException, IOException {
        return new CasaDataObject(primaryFile, this);
    }

    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(CASA_MIME);
    }
}
