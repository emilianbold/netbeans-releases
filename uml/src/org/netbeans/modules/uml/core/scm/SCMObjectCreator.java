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

/*
 * Created on Mar 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.netbeans.modules.uml.core.scm;

import org.openide.cookies.InstanceCookie;

/**
 * @author thumilank
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SCMObjectCreator {
	
	 /**
	    * The registry information that is retrieved from layer files to build
	    * the list of actions supported by this node.
	    *
	    * @param path The registry path that is used for the lookup.
	    * @return The list of actions in the path.  null will be used if when 
	    *         seperators can be placed.   
	    */
	   public static Object getInstanceFromRegistry(String path)
	   {
	      Object retVal = null;
          /*
	      org.openide.filesystems.FileSystem system = org.openide.filesystems.Repository.getDefault().getDefaultFileSystem();
	      try
	      {
	         if(system != null)
	         {
	            org.openide.filesystems.FileObject lookupDir = system.findResource(path);
	            if(lookupDir != null)
	            {
	               org.openide.filesystems.FileObject[] children = lookupDir.getChildren();
	               for(org.openide.filesystems.FileObject curObj : children)
	               {
	                  try
	                  {
	                     org.openide.loaders.DataObject dObj = org.openide.loaders.DataObject.find(curObj);
	                     if(dObj != null)
	                     {
	                        org.openide.cookies.InstanceCookie cookie = (org.openide.cookies.InstanceCookie)dObj.getCookie(org.openide.cookies.InstanceCookie.class);
	                        
	                        if(cookie != null)
	                        {
	                           Object obj = cookie.instanceCreate();
	                           retVal = obj;
//	                           if(obj instanceof Action)
//	                           {
//	                              actions.add((Action)obj);
//	                           }
//	                           else if(obj instanceof javax.swing.JSeparator)
//	                           {
//	                              actions.add(null);
//	                           }
	                        }
	                     }
	                  }
	                  catch(ClassNotFoundException e)
	                  {
	                     // Unable to create the instance for some reason.  So the 
	                     // do not worry about adding the instance to the list.
	                  }
	               }
	            }         
	         }
	      }
	      catch(org.openide.loaders.DataObjectNotFoundException e)
	      {
	         // Basically Bail at this time.
	      }
	      catch(java.io.IOException ioE)
	      {
	         
	      }      
	      */
	      return retVal;
	   }

}
