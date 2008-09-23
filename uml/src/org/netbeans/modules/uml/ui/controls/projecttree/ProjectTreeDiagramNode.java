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
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;

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

        if (data != null)
        {
//            String filename = StringUtilities.ensureExtension(value, FileExtensions.DIAGRAM_LAYOUT_EXT);
//            if (filename.length() > 0)
            if (value.length() > 0)
            {
                data.setDescription(value);
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
      // TODO: meteora
//      else if(obj instanceof IDrawingAreaControl)
//      {
//         IDrawingAreaControl control = (IDrawingAreaControl)obj;
//         
//         String testDescription = control.getFilename();
//         String myDescription   = getData().getDescription();
//
//         retVal = myDescription.equals(testDescription);
//      }
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
