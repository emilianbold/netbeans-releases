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


/*
 * DrawingAreaDropContext.java
 *
 * Created on July 1, 2004, 9:05 AM
 */

package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ADTransferable;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;

/**
 * DrawingAreaDropContext is the event context for drawing area drop events.
 *
 * @author  Trey Spiva
 */
public class DrawingAreaDropContext implements IDrawingAreaDropContext
{
   private ADTransferable.ADTransferData m_DropData = null;
   private IPresentationElement m_TargetPE = null;
   private ICompartment m_TargetCompartment = null;
   private boolean m_Canceled = false;
   private ETList < IElement > m_AdditionalElements = new ETArrayList < IElement >();
   
   /** 
    * Creates a new instance of DrawingAreaDropContext 
    *
    * @param data The DnD transfer data.
    * @param targetPE The presentation that was the target of the drop.
    * @param targetCompartment The compartment that was the target of the drop.
    */
   public DrawingAreaDropContext(ADTransferable.ADTransferData data,
                                IPresentationElement targetPE,
                                ICompartment targetCompartment)
   {
      setDropData(data);
      setPEDroppedOn(targetPE);
      setCompartmentDroppedOn(targetCompartment);
   }
   
   /**
	 * External listeners to the drop events can add model elements to add to 
    * those in the clipboard.  These will act as if they were dropped as well.
	 */
   public void addAdditionalDropElement(IElement newVal)
   {
      m_AdditionalElements.add(newVal);
   }
   
   /**
	 * Retrieves the model elemetns that External listeners to the drop events added.  
    * These will act as if they were dropped as well.
	 */
   public ETList < IElement > getAdditionalDropElements()
   {
      return m_AdditionalElements;
   }
   
   /**
	 * The compartment that is the target of the drop operation.  
    * 
    * @return The target of the drop.  <code>null</code> will be returned
    *         if a compartment element was not the target of the drop.
	*/
   public ICompartment getCompartmentDroppedOn()
   {
      return m_TargetCompartment;
   }
   
   /**
	 * The drag and drops transferable object.
	*/
   public ADTransferable.ADTransferData getDropData()
   {
      return m_DropData;
   }
   
   /**
	 * The presentation element that is the target of the drop operation.  
    * 
    * @return The target of the drop.  <code>null</code> will be returned
    *         if a presentation element was not the target of the drop.
	*/
   public IPresentationElement getPEDroppedOn()
   {
      return m_TargetPE;
   }
   
   /**
	 * Retieves whetehr or not to cancel the drag operatoin.
	*/
   public boolean getCancel()
   {
      return m_Canceled;
   }
   
   /**
	 * Set this to true in the pre to cancel the event.
	*/
   public void setCancel(boolean value)
   {
      m_Canceled = value;
   }
 
   ////////////////////////////////////////////////////////////////////////////
   // Protected methods.
   
   /**
    * Sets the drop data.
    */
   protected void setDropData(ADTransferable.ADTransferData data)
   {
      m_DropData = data;
   }
   
   /**
    * Sets the presentation element that was the target of the drop.
    */
   protected void setPEDroppedOn(IPresentationElement targetPE)
   {
      m_TargetPE = targetPE;
   }
   
   /**
    * Sets the compartment that was the target of the drop.
    */
   protected void setCompartmentDroppedOn(ICompartment targetCompartment)
   {
      m_TargetCompartment = targetCompartment;
   }
}
