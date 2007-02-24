/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/**
 *
 */
package org.netbeans.modules.uml.project.ui;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.cookies.InstanceCookie;

import org.netbeans.modules.uml.core.support.umlsupport.Log;



/**
 * @author alagud
 *
 */
public class MDREventProcessor
{
    Vector<IMDRListener> listener = new Vector<IMDRListener>();
    private static MDREventProcessor instance = null;
    
    private MDREventProcessor()
    {
        Log.out("MDREventProcessor instance Created");
    }
    
    public static MDREventProcessor getInstance()
    {
        if(instance==null)
            instance = new MDREventProcessor();
        
        return instance;
    }
    public void fireChanged(SourceGroup[] oldValue , SourceGroup[] newValue)
    {
        FileSystem system = Repository.getDefault().getDefaultFileSystem();
        
        try
        {
            if (system != null)
            {
                FileObject lookupDir = system.findResource("uml/source-roots-listeners");                
                if (lookupDir != null)
                {
                    FileObject[] children = lookupDir.getChildren();                    
                    for (FileObject curObj : children)
                    {
                        try
                        {
                            DataObject dObj = DataObject.find(curObj);                            
                            if (dObj != null)
                            {
                                InstanceCookie cookie = (InstanceCookie)dObj
                                        .getCookie(InstanceCookie.class);
                                
                                if (cookie != null)
                                {
                                    Object obj = cookie.instanceCreate();
                                    
                                    if (obj instanceof IMDRListener)
                                    {
                                        IMDRListener listener = (IMDRListener)obj;
                                        if(oldValue!=null)
                                        {
                                            listener.removeJMI(oldValue);
                                        }
                                        
                                        if(newValue!=null)
                                        {
                                            listener.registerJMI(newValue);
                                        }
                                    }
                                }
                            } // dObj != null
                        }
                        
                        catch (ClassNotFoundException e)
                        {
                            // Unable to create the instance for some reason.  So the
                            // do not worry about adding the instance to the list.
                        }
                    } // for-each FileObject
                } // if lookupDir != null
            } // if system != null
        }        
        catch (DataObjectNotFoundException e)
        {
            // Basically Bail at this time.
        }        
        catch (IOException ioE)
        {
            
        }
    }

}
