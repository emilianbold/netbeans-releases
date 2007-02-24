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



package org.netbeans.modules.uml.ui.swing.drawingarea;

/**
 * A convience class to allow the method verifyDataDeletion on IDiagramEngine
 * to specify how to handle diagram interactions.  The user can cancel the
 * operation or affect the model.
 *
 * @author Trey Spiva
 */
public class DataVerificationResults
{
   /** Specifies if the action was canceled by the user. */
   private boolean m_CancelAction = false;

   /** Specifies if the model element should be affected by the change. */
   private boolean m_AffectModelElement = false;
   
   public DataVerificationResults()
   {
      
   }
   
   public DataVerificationResults(boolean cancel, boolean affect)
   {
      setCancelAction(cancel);
      setAffectModelElement(affect);
   }
   
   /**
    * Determines if the user canceled the action.
    * 
    * @return <code>true</code> if the user canceled the action.
    */
   public boolean isCancelAction()
   {
      return m_CancelAction;
   }

   /**
    * Sets whether or not the user canceled the action.
    * 
    * @param b <code>true</code> if the user canceled the action.
    */
   public void setCancelAction(boolean b)
   {
      m_CancelAction = b;
   }

   /**
    * Determines if the user want to change the data model.
    * 
    * @return <code>true</code> if the user wants the action to affect the model. 
    */
   public boolean isAffectModelElement()
   {
      return m_AffectModelElement;
   }

   /**
    * Set whether or not the user want to change the data model.
    * 
    * @param b <code>true</code> if the user wants the action to affect the model. 
    */
   public void setAffectModelElement(boolean b)
   {
      m_AffectModelElement = b;
   }

}
