/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.enterprise.modules.db;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.modules.*;
import org.openide.TopManager;
import org.openide.util.NbBundle;

/**
* DB module.
* @author Slavek Psenicka
*/
public class DatabaseModule extends ModuleInstall 
{
	private ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");
	
	public void installed() 
	{
		TopManager tm = TopManager.getDefault();
   		System.out.println("Installing database module");
		
		try {
			FileSystem rfs = tm.getRepository().getDefaultFileSystem();
    		FileObject rootFolder = rfs.getRoot();
    		FileObject databaseFileObject = rootFolder.getFileObject("Database");
    		System.out.println("databaseFileObject = "+databaseFileObject);
    		if (databaseFileObject == null) {
    			databaseFileObject = rootFolder.createFolder("Database");
    			FileObject adaptorsFileObject = databaseFileObject.createFolder("Adaptors");
	    		System.out.println("adaptorsFileObject created = "+adaptorsFileObject);
    			InstanceDataObject.create(DataFolder.findFolder(adaptorsFileObject), "DefaultAdaptor", com.netbeans.ddl.adaptors.DefaultAdaptor.class);
    	    }
		} catch (LinkageError ex) {
			String msg = MessageFormat.format(bundle.getString("FMT_CLASSNOTFOUND"), new String[] {ex.getMessage()});
			if (tm != null) tm.notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
		} catch (Exception ex) {
			String msg = MessageFormat.format(bundle.getString("FMT_EXCEPTIONINSTALL"), new String[] {ex.getMessage()});
			if (tm != null) tm.notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
		}
	}
}

/*
* <<Log>>
*  6    Gandalf   1.5         10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
*        changed to class + some methods added
*  5    Gandalf   1.4         9/27/99  Slavek Psenicka new Database/Adaptors 
*       folder
*  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
*  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
* $
*/
