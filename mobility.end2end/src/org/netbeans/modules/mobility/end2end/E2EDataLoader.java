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

/*
 * E2EDataLoader.java
 *
 * Created on June 27, 2005, 2:34 PM
 *
 */
package org.netbeans.modules.mobility.end2end;

import java.io.IOException;
import org.openide.actions.EditAction;
import org.openide.actions.FileSystemAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.SaveAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Michal Skvor
 */
public class E2EDataLoader extends UniFileLoader {
    
    /** Creates a new instance of E2EDataLoader */
    public E2EDataLoader() {
        super( "org.netbeans.modules.mobility.end2end.E2EDataObject" );  // NOI18N
        ExtensionList el = new ExtensionList();
        el.addExtension( "wsclient" );  // NOI18N
        setExtensions( el );
    }

    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage( E2EDataLoader.class, "TYPE_WSClient" ); // NOI18N
    }
    
    protected MultiDataObject createMultiObject( final FileObject primaryFile )
    throws DataObjectExistsException, IOException {
        return new E2EDataObject( primaryFile, this );
    }
    
    protected SystemAction[] defaultActions() {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(EditAction.class),
            SystemAction.get(SaveAction.class),
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(ToolsAction.class),
            SystemAction.get(PropertiesAction.class)
        };
    }
}
