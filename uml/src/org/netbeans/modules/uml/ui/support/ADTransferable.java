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
 * Created on Jun 26, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * The Transferable implmentation that specifies the transfer data for a 
 * project tree drag and drop operation.
 * 
 * To research the Java Drag and Drop functionallity check out the 
 * <a href="http://java.sun.com/docs/books/tutorial/uiswing/misc/dnd.html"> Drag
 * and Drop Tutorial</a>.
 *   
 * @author Trey Spiva
 */
public class ADTransferable implements Transferable
{
   ArrayList < IElement >             m_ModelElements = null;
   ArrayList < IPresentationElement > m_PresentationElements = null;
   ArrayList < String >               m_DiagramLocations = null;
   ArrayList < String >               m_GenericElements = null;
   String                             m_TransferOperation = "";
   
   public final static DataFlavor ADDataFlavor = new DataFlavor(ADTransferData.class, 
                                                                "ADTransfer Data");
   private DataFlavor[] m_SupportedFlavors = { DataFlavor.stringFlavor, 
                                               ADDataFlavor };
   
   /** 
    * Create a new transferable object.  The ADTransferable transerable know
    * how to handle drag and drop objects for Embarcadero's Describe application.
    * There are two data flavors supported by ADTransferable.  
    * 
    * The StringFlavor is string representation of the XML document that 
    * descibes the items that are beig dragged. 
    * 
    * @param operation The name of the opeation that is to occur.
    */
   public ADTransferable(String operation)
   {
      setTransferOperation(operation);
   }
   
   /**
    * Add a Model element to this drag and drop operation.
    *
    * @param element The element to add.
    */
   public void addModelElement(IElement element)
   {
      if(m_ModelElements == null)
      {
         m_ModelElements = new ArrayList < IElement >();
      }
      
      if(m_ModelElements.contains(element) == false)
      {
         m_ModelElements.add(element);
      }
   }
   
   public void addModelAndPresentationElements(ETList < IPresentationElement > pPEs)
   {

      int count = 0;

      if (pPEs != null)
      {
         count = pPEs.size();
      }

      if (count > 0)
      {

         // Now get the elements and place them onto the clip string
         ETList < IElement > pElements = new ETArrayList < IElement > ();

         for (int i = 0; i < count; i++)
         {
            IPresentationElement pThisSelected = pPEs.get(i);

            if (pThisSelected != null)
            {
               IElement pElement = pThisSelected.getFirstSubject();

               if (pElement != null)
               {
                  pElements.add(pElement);
                  // pElements.AddIfNotInList(pElement);
               }
               // Add the presentation element to the list
               addPresentationElement(pThisSelected);
            }
         }

         // Get the number of model elements
         count = pElements.size();

         for (int x = 0; x < count; x++)
         {
            IElement pThisElement = pElements.get(x);

            if (pThisElement != null)
            {
               // Add the model element to the list
               addModelElement(pThisElement);
            }
         }
      }
   }
   /**
    * Add a Presentation element to this drag and drop operation.
    *
    * @param element The element to add.
    */
   public void addPresentationElement(IPresentationElement element)
   {
      if(m_PresentationElements == null)
      {
         m_PresentationElements = new ArrayList < IPresentationElement >();
      }
   
      if(m_PresentationElements.contains(element) == false)
      {
         m_PresentationElements.add(element);
      }
   }
      
   /**
    * Add a Presentation element to this drag and drop operation.
    *
    * @param location The fully qualified path to the diagram's .etld file
    */
   public void addDiagramLocation(String location)
   {
      if(m_DiagramLocations == null)
      {
         m_DiagramLocations = new ArrayList < String >();
      }

      if(m_DiagramLocations.contains(location) == false)
      {
         m_DiagramLocations.add(location);
      }
   }
    
   /**
    * Add a Presentation element to this drag and drop operation.
    *
    * @param location The fully qualified path to the diagram's .etld file
    */
   public void addGenericElement(String genericString)
   {
      if(m_GenericElements == null)
      {
         m_GenericElements = new ArrayList < String >();
      }

      if(m_GenericElements.contains(genericString) == false)
      {
         m_GenericElements.add(genericString);
      }
   }  
   
   /* (non-Javadoc)
    * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
    */
   public DataFlavor[] getTransferDataFlavors()
   {  
      return m_SupportedFlavors;
   }

   /* (non-Javadoc)
    * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
    */
   public boolean isDataFlavorSupported(DataFlavor flavor)
   {
      boolean retVal = false;
      for (int index = 0; index < m_SupportedFlavors.length; index++)
      {
         if(m_SupportedFlavors[index].equals(flavor) == true)
         {
            retVal = true;
            break;
         }
      }
      return retVal;
   }

   /* (non-Javadoc)
    * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
    */
   public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException
   {
      Object retVal = null;
      
      if(flavor.equals(DataFlavor.stringFlavor) == true)
      {
         retVal = getXMLRepresentation();
      }
      else if(flavor.equals(ADDataFlavor) == true)
      {
         return new ADTransferData();
      }
      else
      {
         throw new UnsupportedFlavorException(flavor);
      }
      
      return retVal;
   }

   /**
    * Rretrieves the transfer operation being processed.
    * @return The name of the operation.
    */
   public String getTransferOperation()
   {
      return m_TransferOperation;
   }

   /**
    * Sets the transfer operation that is being processed.
    * @param string The name of the operation.
    */
   public void setTransferOperation(String string)
   {
      m_TransferOperation = string;
   }
      
   //**************************************************
   // Helper Methods
   //**************************************************
   
   /**
    * Generates the raw XML data structure that represents the supplied elements.
    * 
    * @return A String object that contains the data.
    */
   protected Object getXMLRepresentation()
   {
      String retVal = "";
      
      if(getTransferOperation().length() > 0)
      {
         // Create an XML Document to hold the information for
         // what is being dragged.
         Document doc = XMLManip.getDOMDocument();
         if((doc != null) && (doc.getRootElement() != null))
         {
            Element root = doc.getRootElement();
            Element topE = DocumentHelper.createElement(getTransferOperation());
            if(topE != null)
            {
               root.add(topE);
               if((copyModelElements(topE) == true) |
                  (copyPresentationElements(topE) == true) |
                  (copyDiagramLocations(topE) == true) |
                  (copyGenericElements(topE) == true))
               {
                  retVal = topE.asXML();
               }
            } 
         }
      }
      
      return retVal;
   }

   /**
    * Copies the generic elementst to the XML data stream.
    * 
    * @param owner The DOM element that own the information.
    */
   private boolean copyGenericElements(Element owner)
   {
      boolean retVal = false;
      
      if((m_GenericElements != null) && (m_GenericElements.size() > 0))
      {
         retVal = true;
         for (Iterator < String > iter = m_GenericElements.iterator(); iter.hasNext();)
         {
            String value = iter.next();
            Element curElement = DocumentHelper.createElement("GENERICELEMENT");
            if(curElement != null)
            {
               owner.add(curElement);
               curElement.setAttributeValue("DESCSTRING", value);   
            }     
         }   
      }
      
      return retVal;
      
   }

   /**
    * Copy the diagram locations to the DOM element.
    * 
    * @param owner The DOM element that own the information.
    */
   private boolean copyDiagramLocations(Element owner)
   {
      boolean retVal = false;
      
      if((m_DiagramLocations != null) && (m_DiagramLocations.size() > 0))
      {
         retVal = true;
         for (Iterator < String > iter = m_DiagramLocations.iterator(); iter.hasNext();)
         {
            String loc = iter.next();
            Element curElement = DocumentHelper.createElement("DIAGRAM");
            if(curElement != null)
            {
               owner.add(curElement);
               curElement.setAttributeValue("LOCATION", loc);   
            }  
         }   
      }
      
      return retVal;
      
   }

   /**
    * Copy the presentation elements to the DOM element.
    * 
    * @param owner The DOM element that own the information.
    */
   private boolean copyPresentationElements(Element owner)
   {
      boolean retVal = false;
      
      if((m_PresentationElements != null) && (m_PresentationElements.size() > 0))
      {
         retVal = true;
         for (Iterator < IPresentationElement > iter = m_PresentationElements.iterator(); iter.hasNext();)
         {
            IPresentationElement element = iter.next();
            Element curElement = DocumentHelper.createElement("PRESENTATIONELEMENT");
            if(curElement != null)
            {
               owner.add(curElement);
               curElement.setAttributeValue("TOPLEVELID", element.getTopLevelId());
               curElement.setAttributeValue("XMIID", element.getXMIID());   
            }         
         }   
      }
      
      return retVal;
      
   }

   /**
    * Copy the model elements to the DOM element.
    * 
    * @param owner The DOM element that own the information.
    */
   private boolean copyModelElements(Element owner)
   {
      boolean retVal = false;
      
      if((m_ModelElements != null)  && (m_ModelElements.size() > 0))
      {
         retVal = true;
         for (Iterator < IElement > iter = m_ModelElements.iterator(); iter.hasNext();)
         {
            IElement element = iter.next();
            
            Element curElement = DocumentHelper.createElement("MODELELEMENT");
            if(curElement != null)
            {
               owner.add(curElement);
               curElement.setAttributeValue("TOPLEVELID", element.getTopLevelId());
               curElement.setAttributeValue("XMIID", element.getXMIID());   
            }
         }   
      }
      
      return retVal;
   }   
   
   /**
    * The data for the "ADTransfer Data" DataFlavor.
    * 
    * @author Trey Spiva
    */
   public class ADTransferData 
   {
      /**
       * Checks if all the data on the DataFlavor are model elements.
       * @return <code>true</code> if there are only model elements in the 
       *         DataFlavor.
       */
      public boolean isAllElementsFeatures()
      {
         boolean retVal = false;
                  
         ArrayList < IElement > list = getModelElements();
         
         if(list.size() > 0)
         {
            retVal = true;
            for (Iterator < IElement > iter = list.iterator(); 
                 (iter.hasNext() == true) && (retVal == true);)
            {
               IElement element = iter.next();
               if(!(element instanceof IFeature))
               {
                  retVal = false;
               }
            }
         }
         
         return retVal;
      }
      
      /**
       * Retrieve the model elements that are being transfered.
       */
      public ArrayList < IElement > getModelElements()
      {
         return m_ModelElements;
      }
      
      /**
       * Retrieve the presentation elements that are being transfered.
       */
      public ArrayList < IPresentationElement > getPresentationElements()
      {
         return m_PresentationElements;
      }
      
      /**
       * Retrieve the location of the diagrams that are being transfered.
       */
      public ArrayList < String > getDiagramLocations()
      {
         return m_DiagramLocations;
      }
      
      /**
       * Retrieve the generic elements that are being transfered.
       */
      public ArrayList < String > getGenericElements()
      {
         return m_GenericElements;
      }
   }
}
