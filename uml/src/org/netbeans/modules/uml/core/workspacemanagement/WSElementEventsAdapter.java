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
