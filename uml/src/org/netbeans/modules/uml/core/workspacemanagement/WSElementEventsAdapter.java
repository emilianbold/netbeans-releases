/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.uml.core.workspacemanagement;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 *
 * @author Trey Spiva
 */
public class WSElementEventsAdapter implements IWSElementEventsSink
{

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementPreCreate(com.embarcadero.describe.workspacemanagement.IWSProject, java.lang.String, java.lang.String, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementPreCreate(IWSProject wsProject, String location, String Name, String data, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementCreated(com.embarcadero.describe.workspacemanagement.IWSElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementCreated(IWSElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementPreSave(com.embarcadero.describe.workspacemanagement.IWSElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementPreSave(IWSElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementSaved(com.embarcadero.describe.workspacemanagement.IWSElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementSaved(IWSElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementPreRemove(com.embarcadero.describe.workspacemanagement.IWSElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementPreRemove(IWSElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementRemoved(com.embarcadero.describe.workspacemanagement.IWSElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementRemoved(IWSElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementPreNameChanged(com.embarcadero.describe.workspacemanagement.IWSElement, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementPreNameChanged(IWSElement element, String proposedValue, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementNameChanged(com.embarcadero.describe.workspacemanagement.IWSElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementNameChanged(IWSElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementPreAliasChanged(com.embarcadero.describe.workspacemanagement.IWSElement, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementPreAliasChanged(IWSElement element, String proposedValue, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementAliasChanged(com.embarcadero.describe.workspacemanagement.IWSElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementAliasChanged(IWSElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementPreOwnerChange(com.embarcadero.describe.workspacemanagement.IWSElement, com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementPreOwnerChange(IWSElement element, IWSProject newOwner, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementOwnerChanged(com.embarcadero.describe.workspacemanagement.IWSElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementOwnerChanged(IWSElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementPreLocationChanged(com.embarcadero.describe.workspacemanagement.IWSElement, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementPreLocationChanged(IWSElement element, String proposedLocation, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementLocationChanged(com.embarcadero.describe.workspacemanagement.IWSElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementLocationChanged(IWSElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementPreDataChanged(com.embarcadero.describe.workspacemanagement.IWSElement, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementPreDataChanged(IWSElement element, String newData, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementDataChanged(com.embarcadero.describe.workspacemanagement.IWSElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementDataChanged(IWSElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementPreDocChanged(com.embarcadero.describe.workspacemanagement.IWSElement, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementPreDocChanged(IWSElement element, String doc, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSElementEventsSink#onWSElementDocChanged(com.embarcadero.describe.workspacemanagement.IWSElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSElementDocChanged(IWSElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }


}
