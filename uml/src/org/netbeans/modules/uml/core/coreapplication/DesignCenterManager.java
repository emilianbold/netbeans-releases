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
 * Created on Mar 4, 2004
 *
 */
package org.netbeans.modules.uml.core.coreapplication;


//import org.netbeans.modules.uml.core.addinframework.AddInManagerImpl;
//import org.netbeans.modules.uml.core.addinframework.IAddIn;
//import org.netbeans.modules.uml.core.addinframework.IAddInDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * @author sumitabhk
 *
 */
public class DesignCenterManager /*extends AddInManagerImpl*/ implements IDesignCenterManager
{
   private HashMap < String, IDesignCenterSupport > mAddins = new HashMap < String, IDesignCenterSupport >();
   
	public DesignCenterManager()
	{
		initialize();
	}

	/**
	 *
	 * Initialize the design center manager.  This will set up where its registry information
	 * is to be found as well as load any of its registered addins.
	 *
	 * @return HRESULT
	 *
	 */
	public void initialize()
	{
      
      IDesignCenterSupport[] addins = getDesignCenterAddins();
      for(IDesignCenterSupport curAddin : addins)
      {
         String progID = curAddin.getID();
         mAddins.put(progID, curAddin);
      }
      
	}

   public IDesignCenterSupport[] getAddIns()
   {
      IDesignCenterSupport[] retVal = null;
      
      Collection < IDesignCenterSupport > addins = mAddins.values();
      if(addins.size() > 0)
      {
         retVal = new IDesignCenterSupport[addins.size()];
         addins.toArray(retVal);
      }
      else
      {
         retVal = new IDesignCenterSupport[0];
      }
      
      return retVal;
   }
   
	/**
	 * The design center manager knows about the addins in the design center (requirements, patterns, macros).
	 * This routine retrieves a particular addin based on the prog id passed in.
	 * 
	 *
	 * @param sProgID[in]		The prog id of the addin to get
	 * @param pAddIn[out]		The found add in
	 *
	 * @return HRESULT
	 *
	 */
//	public IAddIn getDesignCenterAddIn(String sProgID) 
   public IDesignCenterSupport getDesignCenterAddIn(String sProgID) 
	{
		IDesignCenterSupport pIn = mAddins.get(sProgID);
		if (pIn != null && pIn instanceof IDesignCenterSupport)
		{
			return (IDesignCenterSupport)pIn;
		}
		return null;
	}
   
   ////////////////////////////////////////////////////////////////////
   // Protected methods
   
   /**
	 * Retrieve the design center addins added by other modules.
	 *
	 * @param actions The action collection to add the actions to.
	 */
	protected IDesignCenterSupport[] getDesignCenterAddins()
	{
		return getAddinsFromRegistry("UML/designcenter"); // NOI18N

	}
   
   /**
	 * The registry information that is retrieved from layer files to build
	 * the list of design center addins supported by this node.
	 *
	 * @param path The registry path that is used for the lookup.
	 * @return The list of addins in the path.  null will be used if when
	 *         seperators can be placed.
	 */
	protected IDesignCenterSupport[] getAddinsFromRegistry(String path)
	{
		ArrayList < IDesignCenterSupport > addins = new ArrayList < IDesignCenterSupport >();
		try
		{
                        org.openide.filesystems.FileObject lookupDir = FileUtil.getConfigFile(path);
                        if(lookupDir != null)
                        {
                                org.openide.filesystems.FileObject[] children = lookupDir.getChildren();

                                for(FileObject curObj : children)
                                {
                                        try
                                        {
                                                DataObject dObj = DataObject.find(curObj);
                                                if(dObj != null)
                                                {
                                                        InstanceCookie cookie = (InstanceCookie)dObj.getCookie(InstanceCookie.class);
                                                        if(cookie != null)
                                                        {
                                                                Object obj = cookie.instanceCreate();
                                                                if(obj instanceof IDesignCenterSupport)
                                                                {
                      //String id = (String)curObj.getAttribute("id");
                                                                        addins.add((IDesignCenterSupport)obj);
                                                                }
                                                        }
                                                }
                                        }
                                        catch(ClassNotFoundException e)
                                        {
                                                // Unable to create the instance for some reason.  So the
                                                // do not worry about adding the instance to the list.
             e.printStackTrace();
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

		IDesignCenterSupport[] retVal = new IDesignCenterSupport[addins.size()];
		addins.toArray(retVal);
		return retVal;
	}

}



