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
 * Created on Jun 18, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.controls.projecttree;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * 
 * @author Trey Spiva
 */
public class ProjectTreeDiagramNode extends ProjectTreeNode
   implements ITreeDiagram
{
   private IProxyDiagram m_Diagram = null;
   private String        m_DiagramType = "Diagram";
   
   public ProjectTreeDiagramNode(IProxyDiagram diagram)
   {
      setDiagram(diagram);
      setFilename(diagram.getFilename());
      setDiagramType(diagram.getDiagramKindName());
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram#getDiagram()
    */
   public IProxyDiagram getDiagram()
   {
      // TODO Auto-generated method stub
      return m_Diagram;
   }
   
   /**
    * Set the diagram that is wrapped by the node.
    * 
    * @param diagram The diagram.
    */
   public void setDiagram(IProxyDiagram diagram)
   {
      // TODO Auto-generated method stub
      m_Diagram = diagram;
   }
   
   public void setDiagramType(String name)
   {
      m_DiagramType = name;
   }
   
   public String getDiagramType()
   {
      return m_DiagramType;
   }
      
   //**************************************************
   // Helper Methods
   //**************************************************
   
   /**
    * @param string
    */
   protected void setFilename(String value)
   {
      IProjectTreeItem data = getData();
      
      if(data != null)
      {
         String filename = StringUtilities.ensureExtension(value, FileExtensions.DIAGRAM_LAYOUT_EXT);
         if(filename.length() > 0)
         {
            data.setDescription(filename);
         }
      }
   }
   
   public boolean equals(Object obj)
   {
      boolean retVal = false;
      
      if(obj instanceof ITreeDiagram)
      {
         ITreeDiagram diagram = (ITreeDiagram)obj;
         
         String testDescription = diagram.getData().getDescription();
         String myDescription   = getData().getDescription();
         
         retVal = myDescription.equals(testDescription);
      }
      else if(obj instanceof String)
      {
         String myDescription   = getData().getDescription();         
         retVal = myDescription.equals((String)obj);
      }
      else if(obj instanceof IDrawingAreaControl)
      {
         IDrawingAreaControl control = (IDrawingAreaControl)obj;
         
         String testDescription = control.getFilename();
         String myDescription   = getData().getDescription();

         retVal = myDescription.equals(testDescription);
      }
      else if(obj instanceof IProxyDiagram)
      {
         IProxyDiagram control = (IProxyDiagram)obj;
   
         String testDescription = control.getFilename();
         String myDescription   = getData().getDescription();

         retVal = myDescription.equals(testDescription);
      }
      else if(obj instanceof IDiagram)
      {
         IDiagram control = (IDiagram)obj;

         String testDescription = control.getFilename();
         String myDescription   = getData().getDescription();

         retVal = myDescription.equals(testDescription);
      }
      else
      {
         retVal = super.equals(obj);
      }
      return retVal;
   }
   
   public String getType()
   {
	  return m_DiagramType;
   }
   
   public String getDisplayedName()
   {
	  String retVal = "";
	  IProjectTreeItem item = getData();
	  if(item != null)
	  {
		IProxyDiagram dia = item.getDiagram();
		if (dia != null)
		{
			//its an unopen diagram
			retVal = dia.getNameWithAlias();
		}
	  }

	  return retVal;
   }
}
