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

package org.netbeans.modules.j2ee.ddloaders.ejb;

import java.io.IOException;
//import java.util.Vector;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;

import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.ddloaders.multiview.*;

/** Recognizes deployment descriptors of ejb modules.
 *
 * @author Ludovic Champenois
 */
public class EjbJarDataLoader extends UniFileLoader {
    
    private static final long serialVersionUID = 8616780278674213L;
    private static final String REQUIRED_MIME_PREFIX_1 = "text/x-dd-ejbjar2.0"; // NOI18N
    private static final String REQUIRED_MIME_PREFIX_2 = "text/x-dd-ejbjar2.1"; // NOI18N


    public EjbJarDataLoader () {
        super ("org.netbeans.modules.j2ee.ddloaders.ejb.EjbJarDataObject");  // NOI18N
    }


    protected String defaultDisplayName () {
        return NbBundle.getMessage (EjbJarDataLoader.class, "LBL_loaderName");
    }
    
    protected SystemAction[] defaultActions () {
        return new SystemAction[] {
            SystemAction.get (OpenAction.class),
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
//DataLoader initialization now being done on EjbJar project open
//This is to take care of IS49655
/*    
    protected FileObject findPrimaryFile(FileObject fo) {
        if (fo.getPath().endsWith("src/conf/ejb-jar.xml")){ ///
            return fo;///
        }///
        
        String mimeType = fo.getMIMEType();
        if (mimeType==null) return null;
        else return (mimeType.startsWith(REQUIRED_MIME_PREFIX)?fo:null);
    }
*/
///
    
    protected void initialize () {
         super.initialize ();
         getExtensions().addMimeType(REQUIRED_MIME_PREFIX_1);
         getExtensions().addMimeType(REQUIRED_MIME_PREFIX_2);
     }

    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new EjbJarMultiViewDataObject(primaryFile, this);
    }

}
