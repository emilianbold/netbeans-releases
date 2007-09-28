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



package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.drawing.TSLabel;
import com.tomsawyer.editor.TSENodeLabel;
import com.tomsawyer.editor.TSEConnectorLabel;
import com.tomsawyer.editor.TSEEdgeLabel;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.ui.TSELabelUI;
import com.tomsawyer.graph.TSGraphObject;

public class LabelPresentation extends GraphPresentation implements ILabelPresentation
{
   
   /// flag indicated if the element should be deleted if not edited
   boolean m_bDeleteIfNotEdited = false;
   
   /**
    *
    */
   public LabelPresentation()
   {
      super();
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation#getETLabel()
    */
   public IETLabel getETLabel()
   {
      return getETGraphObject() instanceof IETLabel ? (IETLabel) getETGraphObject() : null;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation#getLabelView()
    */
   public TSELabelUI getLabelView()
   {
      return this.getUI() instanceof TSELabelUI ? (TSELabelUI) this.getUI() : null;
   }
   private TSLabel mLabel = null;
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation#getTSLabel()
    */
   public TSLabel getTSLabel()
   {
       return mLabel;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation#setTSLabel(com.tomsawyer.drawing.TSLabel)
    */
   public void setTSLabel(TSLabel value)
   {
       mLabel = value;
   }
   
   public IETGraphObjectUI getUI()
   {
       TSLabel label = getTSLabel();
       IETGraphObjectUI retVal = null;
       
       TSEObjectUI ui = null;
       if(label instanceof TSEEdgeLabel)
       {
           TSEEdgeLabel edgeLabel = (TSEEdgeLabel)label;
           ui = edgeLabel.getUI();           
       }
       else if(label instanceof TSENodeLabel)
       {
           TSENodeLabel edgeLabel = (TSENodeLabel)label;
           ui = edgeLabel.getUI();
       }
       else if(label instanceof TSEConnectorLabel)
       {
           TSEConnectorLabel edgeLabel = (TSEConnectorLabel)label;
           ui = edgeLabel.getUI();
       }
       
       if(ui instanceof IETGraphObjectUI)
       {
           retVal = (IETGraphObjectUI)ui;
       }
       
       return retVal;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation#invalidate()
    */
   public void invalidate()
   {
      TSLabel label = getTSLabel();
      IDrawEngine engine = getDrawEngine();
      if (label != null && engine != null)
      {
         
         IDrawingAreaControl ctrl = engine.getDrawingArea();
         
         TSEGraphWindow window = ctrl.getGraphWindow();
         if (window != null)
         {
            window.addInvalidRegion((TSEObject) label);
         }
      }
      
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation#beginEdit()
    */
   public long beginEdit()
   {
      
      IDrawEngine pDE = this.getDrawEngine();
      
      if (pDE != null)
      {
         
         int count = 0;
         
         List pCompartments = pDE.getCompartments();
         
         if (pCompartments != null)
         {
            count = pCompartments.size();
         }
         if (count > 0)
         {
            ICompartment pFirstCompartment = (ICompartment) pCompartments.get(0);
            
            if (pFirstCompartment != null)
            {
               int tempShort = 0;
               setSelected(true);
               pFirstCompartment.editCompartment(m_bDeleteIfNotEdited, tempShort, 0, -1);
               
               // always set deleteIfNotEdited back to false so subsequent edits don't accidently delete
               m_bDeleteIfNotEdited = false;
            }
         }
      }
      return 0;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation#get_DeleteIfNotEdited()
    */
   public boolean getDeleteIfNotEdited()
   {
      // TODO Auto-generated method stub
      return false;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation#put_DeleteIfNotEdited(boolean)
    */
   public long setDeleteIfNotEdited(boolean bDelete)
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation#getPresentationOwner()
    */
   public IPresentationElement getPresentationOwner()
   {
      IPresentationElement retVal = null;
      
      IETLabel etlabel = getETLabel();
      if(etlabel != null)
      {
         retVal = etlabel.getParentPresentationElement();
      }
      
      return retVal;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation#getLocation(int, int, int, int)
    */
   public long getLocation(int pWidth, int pHeight, int pXCenter, int pYCenter)
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation#moveTo(int, int, int)
    */
   public long moveTo(int x, int y, int flags)
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
   /**
    *
    * Creates the label presentation element node.
    *
    * @param doc[in] the document owner of the new node
    * @param parent[in] the parent of the new node
    * @return HRESULTs
    *
    */
   public void establishNodePresence(Document doc, Node parent)
   {
      buildNodePresence("UML:LabelPresentation", doc, parent);
   }
   
   /**
    * Gets the FIRST model element associated with this presentation element.  More generically
    * there is a many to one relationship...but in most cases a many to one relationship
    * between an IPresentationElement and IElement exists.
    *
    * @param pVal[out, retval] The first model element associated with this presentation element
    *
    * @return HRESULT
    */
   public IElement getModelElement()
   {
      return super.getModelElement();
      //		return getFirstSubject();
   }
   
   /**
    * Sets the model element associated with this presentation element.
    *
    * @param newVal[in] The model element associated with this presentation element
    *
    * @return HRESULT
    */
   public void setModelElement(IElement newVal)
   {
      
      super.setModelElement(newVal);
      //		if (newVal != null) {
      //			// Remove all the subjects
      //			ETList < IElement > pModelElements = getSubjects();
      //			long count = getSubjectCount();
      //			for (long i = 0; i < count; i++) {
      //				IElement pEle = pModelElements.get((int) i);
      //				pEle.removePresentationElement(this);
      //				removeSubject(pEle);
      //			}
      //
      //			// Add the subject
      //			addSubject(newVal);
      //
      //			// Now update the draw engine/compartment so that it's pointing to the same model element
      //			IETGraphObject graphObj = getETGraphObject();
      //			if (graphObj != null) {
      //				IDrawEngine drawEng = graphObj.getEngine();
      //				if (drawEng != null) {
      //					drawEng.initCompartments(this);
      //				}
      //			}
      //		}
   }
   
   public IETPoint getCenter()
   {
      TSLabel label = getTSLabel();
      return label != null ? new ETPoint((int)label.getCenter().getX(), (int)label.getCenter().getY()) : null;
   }
   
   public long getWidth()
   {
      TSLabel label = getTSLabel();
      return label != null ? (long)label.getWidth() : 0;
   }
   
   public long getHeight()
   {
      TSLabel label = getTSLabel();
      return label != null ? (long)label.getHeight() : 0;
   }
   
}
