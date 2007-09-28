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
