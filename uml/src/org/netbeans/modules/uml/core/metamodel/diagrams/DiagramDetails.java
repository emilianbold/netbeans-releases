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

package org.netbeans.modules.uml.core.metamodel.diagrams;

import java.util.ArrayList;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.ui.controls.drawingarea.ModelElementXMIIDPair;
import org.netbeans.modules.uml.ui.support.diagramsupport.DiagramTypesManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDiagramTypesManager;


/**
 *
 * @author Trey Spiva
 */
public class DiagramDetails implements IDiagramKind
{
	private String     m_Name            = "";
	private int        m_DiagramType     = 0;
	private String     m_DiagramTypeName = "";
	private String     m_NamespaceXMIID  = "";
	private String     m_DiagramAlias    = "";
	private String     m_DiagramXMIID    = "";
	private String     m_ToplevelXMIID   = "";
	private INamespace m_Namespace       = null;
   private ArrayList < String > m_AssociatedDiagrams = null;
   private ArrayList < ModelElementXMIIDPair > m_AssociatedElements = null;
   private long        m_DateModified    = 0;
      
   /**
    * Retrieves the name of the diagram.
    * 
    * @return The name.
    */
   public String getDiagramAlias()
   {
      return m_DiagramAlias;
   }

	/**
	 * Sets the name of the diagram.
	 * @param value The name.
	 */
	public void setDiagramAlias(String value)
	{
		m_DiagramAlias = value;
	}
		
   /**
    * Returns the type of the daigram.  The valid values for 
    * the daigram type is specified in the interface IDiagramKind.
    * 
    * @return The diagram type.
    * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind
    */
   public int getDiagramType()
   {
   	return m_DiagramType;
   }
   
	/**
	 * Sets the type of the daigram.  The valid values for 
    * the daigram type is specified in the interface IDiagramKind.
    * 
	 * @param vlue The diagram type.
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind
	 */
	public void setDiagramType(int value)
	{
		m_DiagramType = value;
	}
   /**
    * Gets the name of the diagram.
    * 
    * @return The name of the diagrm
    */
   public String getName()
   {
      return m_Name;
   }

   /**
    * Sets the name of the diagram.
    * 
    * @parm value The name of the diagrm
    */
   public void setName(String value)
   {
      m_Name = value;
   }

   /**
    * Retrieves the XMI ID for of the namespace that contains the diagram.
    * 
    * @return The id.
    */
   public String getNamespaceXMIID()
   {
      String retVal = m_NamespaceXMIID;

      if((retVal.length() <= 0) && (m_Namespace != null))
      {
         retVal = m_Namespace.getXMIID();
      }
      
      return retVal;
   }

   /**
    * Sets the XMI ID for of the namespace that contains the diagram.
    * 
    * @param value
    */
   public void setNamespaceXMIID(String value)
   {
      m_NamespaceXMIID = value;
   }
   
   /**
    * Retrieves the diagrams XMI ID.
    * 
    * @return the id.
    */
   public String getDiagramXMIID()
   {
      return m_DiagramXMIID;
   }

   /**
    * Sets the diagrams XMIID.
    * 
    * @param value the id.
    */
   public void setDiagramXMIID(String value)
   {
      m_DiagramXMIID = value;
   }

   /**
    * Retrieves the XMI ID of the top level component.
    * 
    * @return the ID.
    */
   public String getToplevelXMIID()
   {
      return m_ToplevelXMIID;
   }

   /**
    * Sets the XMI ID of the top level component
    * @param valuethe ID.
    */
   public void setToplevelXMIID(String value)
   {
      m_ToplevelXMIID = value;
   }

   /**
    * Retrieves the type name of the diagram.
    * 
    * @return The diagram type.
    */
   public String getDiagramTypeName()
   {
      return m_DiagramTypeName;
   }

   /**
    * Sets the type name of the diagram.
    * 
    * @param value The diagram type.
    */
   public void setDiagramTypeName(String value)
   {
      m_DiagramTypeName = value;
      
      IDiagramTypesManager manager = DiagramTypesManager.instance();
      setDiagramType(manager.getDiagramKind(value));
   }

   /**
    * @param space
    */
   public void setNamespace(INamespace space)
   {
      m_Namespace = space;
   }

   /**
    * @param space
    */
   public INamespace getNamespace()
   {
      return m_Namespace;
   }
   
   /**
    * @param object
    */
   public void setAssociatedDiagrams(ArrayList < String > diagrams)
   {
      m_AssociatedDiagrams = diagrams;
      
   }

   /**
    * @param object
    */
   public ArrayList < String > getAssociatedDiagrams()
   {
      return m_AssociatedDiagrams;   
   }
      
   /**
    * @param object
    */
   public void setAssociatedElements(ArrayList < ModelElementXMIIDPair > elements)
   {
      m_AssociatedElements = elements;
      
   }
   
   /**
    * @param object
    */
   public ArrayList < ModelElementXMIIDPair > getAssociatedElements()
   {
      return m_AssociatedElements;
   
   }
   /**
    * Retrieves the file date that was used to retrieve the diagram details.
    * 
    * @return The last date modified.
    */
   public long getDateModified()
   {
      return m_DateModified;
   }

   /**
    * Sets the file date that was used to retrieve the diagram details.
    * 
    * @param value The file date.
    */
   public void setDateModified(long value)
   {
      m_DateModified = value;
   }

}
