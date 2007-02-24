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
 *
 * Created on Jun 19, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.controls.drawingarea;

/**
 *
 * @author Trey Spiva
 */
public class ModelElementXMIIDPair
{
   private String m_TopLevelID     = "";
   private String m_ModelElementID = "";

   public ModelElementXMIIDPair(String topLevelID, String modelElementID)
   {
      setTopLevelID(topLevelID);
      setModelElementID(modelElementID);
   }
   /**
    * @return
    */
   public String getModelElementID()
   {
      return m_ModelElementID;
   }

   /**
    * @return
    */
   public String getTopLevelID()
   {
      return m_TopLevelID;
   }

   /**
    * @param string
    */
   public void setModelElementID(String string)
   {
      m_ModelElementID = string;
   }

   /**
    * @param string
    */
   public void setTopLevelID(String string)
   {
      m_TopLevelID = string;
   }

}
