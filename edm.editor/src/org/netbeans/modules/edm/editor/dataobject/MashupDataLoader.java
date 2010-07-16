/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */

package org.netbeans.modules.edm.editor.dataobject;

import java.io.IOException;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PasteAction;
import org.openide.actions.RenameAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import java.util.logging.Logger;

public class MashupDataLoader extends UniFileLoader {
    
    public static final String REQUIRED_MIME = "text/x-mashup+xml";
    
    private static final long serialVersionUID = 1L;
    private static final Logger mLogger = Logger.getLogger(MashupDataLoader.class.getName());
    
    public MashupDataLoader() {
        super("org.netbeans.modules.edm.editor.dataobject.MashupDataObject");
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(MashupDataLoader.class, "LBL_Mashup_loader_name");
    }
    
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new MashupDataObject(primaryFile, this);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    protected SystemAction[] defaultActions() {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            null,
            SystemAction.get(CutAction.class),
            SystemAction.get(CopyAction.class),
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class)
        };
    }    
    
    /*@Override
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }*/    
}
