/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.app;

import java.io.IOException;
//import java.util.Vector;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;

import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.ddloaders.common.*;

/** Recognizes deployment descriptors of ejb modules.
 *
 * @author Ludovic Champenois
 */
public class EarDataLoader extends UniFileLoader {
    
    private static final long serialVersionUID = 3616780278674213886L;
    private static final String REQUIRED_MIME_PREFIX = "text/x-dd-ear"; // NOI18N


    public EarDataLoader () {
        super ("org.netbeans.modules.j2ee.ddloaders.app.EarDataObject");  // NOI18N
    }


    protected String defaultDisplayName () {
        return NbBundle.getMessage (EarDataLoader.class, "LBL_loaderName");
    }
    
    protected SystemAction[] defaultActions () {
        return new SystemAction[] {
            SystemAction.get (EditAction.class),
            SystemAction.get (FileSystemAction.class),
            null,
            SystemAction.get (CutAction.class),
            SystemAction.get (CopyAction.class),
            SystemAction.get (PasteAction.class),
            null,
            SystemAction.get (DeleteAction.class),
            null,
            SystemAction.get (org.netbeans.modules.xml.tools.actions.CheckAction.class),
            SystemAction.get (org.netbeans.modules.xml.tools.actions.ValidateAction.class),
            null,
            SystemAction.get (ToolsAction.class),
            SystemAction.get (PropertiesAction.class),
        };
    }

/// 
/*    protected FileObject findPrimaryFile(FileObject fo) {
        if (fo.getPath().endsWith("src/conf/application.xml")){
            return fo;
        }
        String mimeType = fo.getMIMEType();
        if (mimeType==null) return null;
        else return (mimeType.startsWith(REQUIRED_MIME_PREFIX)?fo:null);
    }
  */
///

    protected void initialize () {
         super.initialize ();
            getExtensions().addMimeType(REQUIRED_MIME_PREFIX);
     }

    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
	return new EarDataObject (primaryFile, this);
    }


}
