/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
	      try
	      {
	         if(system != null)
	         {
	            org.openide.filesystems.FileObject lookupDir = FileUtil.getConfigFile(path);
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
