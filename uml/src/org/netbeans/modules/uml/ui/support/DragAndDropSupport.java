/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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


//	 Date:: Aug 11, 2003 1:47:18 PM												

package org.netbeans.modules.uml.ui.support;

import org.dom4j.Node;
import org.dom4j.Document;
import org.dom4j.dom.DOMDocument;
import org.dom4j.Element;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;

import java.util.Iterator;
import java.util.List;

/**
 * This class assists in dragging and dropping model/presentation elements.
 */

public class DragAndDropSupport
{
   private final static String MAINELEMENT = "Describe";

   // Here's the source of the DND operation
   private final static String SOURCE = "Source";
   private final static String NAME = "Name";
   private final static String FILENAME = "Filename";
   
   private final static String MODELELEMENT = "ModelElement";
   private final static String PRESENTATIONELEMENT = "PresentationElement";
   private final static String TOPLEVELID = "TopLevelID";
   private final static String XMIID = "XMIID";
   private final static String DIAGRAM = "Diagram";
   private final static String LOCATION = "Location";
   private final static String GENERICELEMENT = "GenericElement";
   private final static String DESCSTRING = "DescString";
   private final static String STEREOTYPE = "Stereotype";
   
   private static String m_sOperation;

   private static ETList < IElement > m_pModelElements = null;
   private static ETList < IPresentationElement > m_pPresentationElements = null;
   private static ETList < String > m_DiagramLocations = new ETArrayList < String > ();
   private static ETList < String > m_GenericElements = new ETArrayList < String > ();

   public DragAndDropSupport()
   {
      super();
      init();
   }

   /// clears any internal members, call before re-using this object
   public static void init()
   {
      m_sOperation = "";
      m_pModelElements = null;
      m_pPresentationElements = null;
      m_DiagramLocations.clear();
      m_GenericElements.clear();
   }

   /// Create a data source and put this data on it
   public static Object /*COleDataSource*/
   createDataSource(String pString)
   {

      /*
         COleDataSource* pDataSource = 0;
         HRESULT hr = S_OK;
         try
         {
            if (pString.GetLength())
            {
               USES_CONVERSION;
      
      	      CSharedFile globFile;
      
               CString sData = CreateRawText( pString );
               
               char *buffer = new char[sData.GetLength() + 1]; 
      #ifdef _UNICODE
               strcpy(buffer,W2A(sData));
      #else
               strcpy(buffer,sData);
      #endif
               globFile.Write(buffer, sData.GetLength() + 1);
      
      
               DWORD dwLen = globFile.GetLength();
               HGLOBAL hMem = globFile.Detach();
               if (!hMem)
                 return NULL;
      
               hMem = ::GlobalReAlloc(hMem, dwLen, GMEM_MOVEABLE | GMEM_DDESHARE | GMEM_ZEROINIT);
               if (hMem)
               {
                  pDataSource = new COleDataSource();
      
                  if (pDataSource)
                  {
                     // Call CacheGlobalData to put data onto this data source
      	            pDataSource->CacheGlobalData (CF_TEXT, hMem);
                  }
               }
               delete [] buffer;
            }
         }
         catch( _com_error& err )
         {
            hr = COMErrorManager::ReportError( err );
         }
         return pDataSource;
      
       */
      return null;
   }

   /// Given this data object, this gets the text off it.
   public static String getGlobalTextData(Object /*COleDataObject*/
   pDataObject)
   {
      /*
         CString foundString;
      
         HRESULT hr = S_OK;
         try
         {
            if (pDataObject && pDataObject->IsDataAvailable(CF_TEXT))
            {
               // Get the text from the COleDataObject
               HGLOBAL hmem = pDataObject->GetGlobalData(CF_TEXT);
               CMemFile sf((BYTE*) ::GlobalLock(hmem), ::GlobalSize(hmem));
      
               // CF_TEXT is ANSI text, so we need to allocate a char* buffer
               // to hold this.
               TCHAR* szBuffer = new TCHAR[::GlobalSize(hmem)];
               if (szBuffer)
               {
                  sf.Read(szBuffer, ::GlobalSize(hmem));
                  ::GlobalUnlock(hmem);
            
                  // Now store in generic TCHAR form so we no longer have to deal with
                  // ANSI/UNICODE problems
                  foundString = szBuffer;
      
                  delete [] szBuffer;
               }
            }
         }
         catch( _com_error& err )
         {
            hr = COMErrorManager::ReportError( err );
         }
      
         return foundString;
      
       */
      return "";
   }

   /// Translates the xmlid's in the data object to model and presentation elements.
   public static int getElementsOnClipboard(
        String sStringFromDataObject,
        IApplication pApplication,
        String sOperation,
        ETList < IElement > pModelElements,
        ETList < IPresentationElement > pPresentationElements,
        ETList < String > diagramLocations) 
   {
       int numElements = 0;
       diagramLocations.clear();
       if (sStringFromDataObject.length() > 0 && pApplication  != null && sOperation.length() > 0) 
       {
           // Create our return lists
           ETList<IElement> pTempElements = new ETArrayList<IElement>();
           
           // See if we have an XML DRAGGEDITEMS format
           Document pDoc = XMLManip.loadXML(sStringFromDataObject);
           
           Node draggedElements = pDoc.selectSingleNode(sOperation);
           
           if (draggedElements!=null) 
           {
               // get the model elements
               int nCount = extractElementsFromNode( draggedElements,
                    pApplication,
                    "MODELELEMENT",
                    pModelElements );
               
               // get the presentation elements
               nCount = extractElementsFromNode( draggedElements,
                    pApplication,
                    "PRESENTATIONELEMENT",
                    pTempElements );
               
               if(pPresentationElements != null) 
               {
                   int count = pTempElements.getCount();
                   
                   for (int i = 0 ; i < count ; i++) 
                   {
                       IElement pTempElement = (IElement)pTempElements.item(i);
                       
                       IPresentationElement pPE = null;
                       if(pTempElement instanceof IPresentationElement)
                           pPE = (IPresentationElement)pTempElement;
                       
                       if (pPE != null) 
                       {
                           pPresentationElements.add(pPE);
                       }
                   }
               }
               
               // Get the diagrams
               extractStringListFromNode( draggedElements, "DIAGRAM", "LOCATION", diagramLocations );
               
           }
           
           if(pModelElements != null && pPresentationElements != null) 
           {
               // Get the number of elements in the list as a return item
               numElements = pModelElements.getCount()
               + pPresentationElements.getCount()
               + diagramLocations.size();
           }
       }
       return numElements;
   }

   /// Translates the xmlid's in the data object to model and presentation elements.
   public static long getElementsOnClipboard(
   /*COleDataObject*/
   Object pDataObject, IApplication pApplication, String sOperation, ETList < IElement > pModelElements, ETList < IPresentationElement > pPresentationElements, ETList < String > diagramLocations)
   {

      String tempString = getText(pDataObject);
      return getElementsOnClipboard(tempString, pApplication, sOperation, pModelElements, pPresentationElements, diagramLocations);
   }

   /// Are all these model elements features?
   public static boolean allAreFeatures(ETList < IElement > pElements)
   {
      boolean bAllAreFeatures = false;

      //      long count = 0;
      //      if (pElements)
      //      {
      //         _VH(pElements - > get_Count(& count));
      //      }
      //
      //      if (count)
      //      {
      //         # pragma warning(disable : 4310)bAllAreFeatures = true;
      //         # pragma warning(default :
      //            4310)
      //            }
      //
      //      for (long i = 0; i < count; i++)
      //      {
      //         CComPtr < IElement > pThisElement;
      //
      //         _VH(pElements - > Item(i, & pThisElement));
      //
      //         CComQIPtr < IFeature > pThisFeature(pThisElement);
      //         if (pThisFeature == 0)
      //         {
      //            bAllAreFeatures = false;
      //            break;
      //         }
      //      }

      return bAllAreFeatures;
   }

   /// Returns the text on this data object.  Used for dropping onto the drawing area
   public static String getText(/*COleDataObject*/
   Object pDataObject)
   {

      return "";
      /*
       * 
         CString returnString;
         HRESULT hr = S_OK;
         try
         {
      
            if (pDataObject && pDataObject->IsDataAvailable(CF_TEXT))
            {
               // Get the text from the COleDataObject
               HGLOBAL hmem = pDataObject->GetGlobalData(CF_TEXT);
               CMemFile sf((BYTE*) ::GlobalLock(hmem), ::GlobalSize(hmem));
      
               // CF_TEXT is ANSI text, so we need to allocate a char* buffer
               // to hold this.
               char* szBuffer = new char[::GlobalSize(hmem)];
               if (!szBuffer)
                  return returnString;
            
               sf.Read(szBuffer, ::GlobalSize(hmem));
               ::GlobalUnlock(hmem);
            
               // Now store in generic TCHAR form so we no longer have to deal with
               // ANSI/UNICODE problems
               returnString = szBuffer;
               delete [] szBuffer;
            }
         }
         catch( _com_error& err )
         {
            hr = COMErrorManager::ReportError( err );
         }
      
         return returnString;
      
       */
   }

   /// fills a collection of IElements from a DOM fragment
   public static int extractElementsFromNode(Node pNode,
        IApplication pApplication, String sNodeID, ETList < IElement > pElements) 
   {
       int nCount = 0;
       if( pNode != null && sNodeID.length()>0 && pElements != null && pApplication != null) 
       {
           IElementLocator pElementLocator = new ElementLocator();
           if (pElementLocator == null)
               return 0;
           
           Node node = pNode;
           ETList<IElement> elements = pElements;
           
           List modelElementsNodeList = node.selectNodes( sNodeID );
           
           // Get the model elements
           int numModelElements = 0;
           if (modelElementsNodeList != null)
               numModelElements=modelElementsNodeList.size();
           
           for (int i = 0 ; i < numModelElements ; i++) 
           {
               Node pModelElementNode = (Node)modelElementsNodeList.get(i);
               
               if (pModelElementNode!=null) 
               {
                   Node pModelElementXMIID = XMLManip.getAttribute(pModelElementNode,"XMIID");
                   
                   // Usually an IProject, except when datatypes
                   // are being dropped --- ie never.
                   Node pTopLevelXMIID = XMLManip.getAttribute(pModelElementNode,"TOPLEVELID");
                   
                   if (pModelElementXMIID != null && pTopLevelXMIID != null) 
                   {
                       String sXMIID = pModelElementXMIID.getText();
                       String sTopLevelXMIID = pTopLevelXMIID.getText();
                       
                       // Use the element locator to find the model element
                       IProject pProject = pApplication.getProjectByID(sTopLevelXMIID);
                       if (pProject != null) 
                       {
                           IElement pFoundModelElement = pElementLocator.findElementByID(pProject, sXMIID);
                           
                           if (pFoundModelElement!= null) 
                           {
                               elements.add(pFoundModelElement);
                               nCount++;
                           }
                       }
                   }
               }
           }
       }
       return nCount;
   }

   /// fills a collection of strings from a DOM fragment
   public static long extractStringListFromNode( Node pNode, String sNodeID,
           String sAttrID, ETList < String > sList) 
   {
       int nRetVal = 0;
       if( pNode != null && sList != null && sNodeID.length() > 0 
           && sAttrID.length() > 0 ) 
       {
           Node node = pNode;
           
           List nodeList = node.selectNodes( sNodeID );
           
           // Get the diagrams
           int nCount =0;
           if (nodeList != null)
               nodeList.size();
           
           for (int i = 0 ; i < nCount ; i++) 
           {
               Node childNode = (Node)nodeList.get(i);
               
               if (childNode!=null) 
               {
                   Node pAttr = XMLManip.getAttribute(childNode,sAttrID);
                   
                   if (pAttr != null) 
                   {
                       String sLocation = pAttr.getText();
                       
                       if (sLocation.length()>0) 
                       {
                           sList.add(sLocation);
                           nRetVal++;
                       }
                   }
               }
           }
       }
       return nRetVal;
   }

   /// Creates the raw XML for the items contained by this object.  
   public static String createRawText(String sOperation)
   {

      String sXMLString = "";

      /*
       * 
      
         if( sOperation.IsEmpty() )
            return sXMLString;
      
         try
         {
            // Create an XML Document to hold the information for
            // what is being dragged.
            CComPtr < IXMLDOMDocument2 > pDoc;
      
            _VH(pDoc.CoCreateInstance( DOM_TYPE(DOMDocument) ));
            if (pDoc)
            {
               CComPtr < IXMLDOMElement > pCreatedDOMElement;
               CComPtr < IXMLDOMNode > pDraggedElements; // The toplevel element for the dragged items
               USES_CONVERSION;
      
               // Create root node
               pCreatedDOMElement = 0;
               _VH(pDoc->createElement(CComBSTR(sOperation),&pCreatedDOMElement));
               ASSERT(pCreatedDOMElement);
      
               // copy model elements
               if( m_pModelElements )
               {
                  long nCount = 0;
                  _VH( m_pModelElements->get_Count( &nCount ));
      
                  if( nCount > 0 )
                  {
                     _VH(pDoc->appendChild(pCreatedDOMElement,&pDraggedElements));
                     if (pDraggedElements)
                     {
                        for( long i = 0; i < nCount; i++ )
                        {
                           CComPtr< IElement > pModelElement;
                           _VH( m_pModelElements->Item( i, &pModelElement ));
                           if( pModelElement )
                           {
                              CComBSTR sXMIID;
                              CComBSTR sTopLevelXMIID;
      
                              _VH(pModelElement->get_XMIID(&sXMIID));
                              _VH(pModelElement->get_TopLevelID(&sTopLevelXMIID));
                              if (sXMIID.Length())
                              {
                                 // Add this item
                                 CComPtr < IXMLDOMNode > pDraggedElement;
                                 pCreatedDOMElement = 0;
                                 _VH(pDoc->createElement(CComBSTR("MODELELEMENT"),&pCreatedDOMElement));
                                 ASSERT(pCreatedDOMElement);
                                 _VH(pDraggedElements->appendChild(pCreatedDOMElement,&pDraggedElement));
                                 if (pDraggedElement)
                                 {
                                    _VH(pCreatedDOMElement->setAttribute(CComBSTR("TOPLEVELID"),CComVariant(sTopLevelXMIID)));
                                    _VH(pCreatedDOMElement->setAttribute(CComBSTR("XMIID"),CComVariant(sXMIID)));
                                 }
                              }
                           }
                        }
                     }
                  }
               }  // model elements
      
      
               /// now do the presentation elements
               if( m_pPresentationElements )
               {
                  long nCount = 0;
                  _VH( m_pPresentationElements->get_Count( &nCount ));
      
                  if( nCount > 0 )
                  {
                     if (pDraggedElements == 0)
                     {
                        _VH(pDoc->appendChild(pCreatedDOMElement,&pDraggedElements));
                     }
                     if (pDraggedElements)
                     {
                        for( long i = 0; i < nCount; i++ )
                        {
                           CComPtr< IPresentationElement > pModelElement;
                           _VH( m_pPresentationElements->Item( i, &pModelElement ));
                           if( pModelElement )
                           {
                              CComBSTR sXMIID;
                              CComBSTR sTopLevelXMIID;
      
                              _VH(pModelElement->get_XMIID(&sXMIID));
                              _VH(pModelElement->get_TopLevelID(&sTopLevelXMIID));
                              if (sXMIID.Length())
                              {
                                 // Add this item
                                 CComPtr < IXMLDOMNode > pDraggedElement;
                                 pCreatedDOMElement = 0;
                                 _VH(pDoc->createElement(CComBSTR("PRESENTATIONELEMENT"),&pCreatedDOMElement));
                                 ASSERT(pCreatedDOMElement);
                                 _VH(pDraggedElements->appendChild(pCreatedDOMElement,&pDraggedElement));
                                 if (pDraggedElement)
                                 {
                                    _VH(pCreatedDOMElement->setAttribute(CComBSTR("TOPLEVELID"),CComVariant(sTopLevelXMIID)));
                                    _VH(pCreatedDOMElement->setAttribute(CComBSTR("XMIID"),CComVariant(sXMIID)));
                                 }
                              }
                           }
                        }
                     }
                  }
               }  // presentation elements
      
      
               // Add diagram locations
               if( m_DiagramLocations.size() )
               {
                  std::vector < xstring >::iterator it = m_DiagramLocations.begin();
      
                  if ( it != m_DiagramLocations.end() )
                  {
                     if (pDraggedElements == 0)
                     {
                        _VH(pDoc->appendChild(pCreatedDOMElement,&pDraggedElements));
                     }
                     if (pDraggedElements)
                     {
                        while ( it != m_DiagramLocations.end() )
                        {
                     
                           // Add this item
                           CComPtr < IXMLDOMNode > pDraggedElement;
                           pCreatedDOMElement = 0;
                           _VH(pDoc->createElement(CComBSTR("DIAGRAM"),&pCreatedDOMElement));
                           ASSERT(pCreatedDOMElement);
                           _VH(pDraggedElements->appendChild(pCreatedDOMElement,&pDraggedElement));
                           if (pDraggedElement)
                           {
                              _VH(pCreatedDOMElement->setAttribute(CComBSTR("LOCATION"),CComVariant(CComBSTR((*it).c_str()))));
                           }
                           it++;
                        }
                     }
                  }
               }
      
               // Add Generic Elements
               if( m_GenericElements.size() )
               {
                  std::vector < xstring >::iterator it = m_GenericElements.begin();
      
                  if ( it != m_GenericElements.end() )
                  {
                     if (pDraggedElements == 0)
                     {
                        _VH(pDoc->appendChild(pCreatedDOMElement,&pDraggedElements));
                     }
                     if (pDraggedElements)
                     {
                        while ( it != m_GenericElements.end() )
                        {
                     
                           // Add this item
                           CComPtr < IXMLDOMNode > pDraggedElement;
                           pCreatedDOMElement = 0;
                           _VH(pDoc->createElement(CComBSTR("GENERICELEMENT"),&pCreatedDOMElement));
                           ASSERT(pCreatedDOMElement);
                           _VH(pDraggedElements->appendChild(pCreatedDOMElement,&pDraggedElement));
                           if (pDraggedElement)
                           {
                              _VH(pCreatedDOMElement->setAttribute(CComBSTR("DESCSTRING"),CComVariant(CComBSTR((*it).c_str()))));
                           }
                           it++;
                        }
                     }
                  }
               }
               // done building the DOM fragment, now get it's raw XML
      
               CComBSTR xmlString;
               _VH(pDoc->get_xml(&xmlString));
      
               sXMLString = xmlString;
            }
         }
         catch( _com_error& err )
         {
            COMErrorManager::ReportError( err );
         }
      
       */
      return sXMLString;
   }

   /// Adds model and presentation elements to our xml string
   public static void addModelAndPresentationElements(ETList < IPresentationElement > pPEs)
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

   /// add a model element to the list to be operated upon
   public static void addModelElement(IElement pElement)
   {

      if (m_pModelElements == null)
      {
         m_pModelElements = new ETArrayList < IElement > ();
      }

      if (m_pModelElements != null)
      {
         //m_pModelElements->AddIfNotInList(pElement));
         m_pModelElements.add(pElement);
      }
   }

   /// add a Presentation element to the list to be operated upon
   public static void addPresentationElement(IPresentationElement pElement)
   {

      if (m_pPresentationElements == null)
      {
         m_pPresentationElements = new ETArrayList < IPresentationElement > ();
      }

      if (m_pPresentationElements != null)
      {
         //_VH(m_pPresentationElements->AddIfNotInList(pElement));
         m_pPresentationElements.add(pElement);
      }

   }

   /// add a diagram location
   public static void addDiagramLocation(String location)
   {
      m_DiagramLocations.add(location);
      //		m_DiagramLocations.push_back(location);
      //		return S_OK;

   }

   /// Adds a generic string to the xml.  Used when the tree item is non of the above
   public static void addGenericElement(String sGenericString)
   {
      m_GenericElements.add(sGenericString);
      //		m_GenericElements.push_back(sGenericString);
      //		return S_OK;

   }

   /// Does this root element exist on the string in the clipboard.
   public static boolean rootElementsExistOnClipboard(/*COleDataObject*/
   Object pDataObject, String sOperation)
   {

      boolean bRetVal = false;

      //      CString strText = GetText(pDataObject);

      //      if (strText.GetLength() && sOperation.length())
      //      {
      //         // See if we have an XML DRAGGEDITEMS format
      //         CComPtr < IXMLDOMDocument2 > pDoc;
      //
      //         _VH(pDoc.CoCreateInstance(DOM_TYPE(DOMDocument)));
      //         if (pDoc)
      //         {
      //            VARIANT_BOOL bLoaded;
      //            USES_CONVERSION;
      //
      //            _VH(pDoc - > loadXML(CComBSTR(strText), & bLoaded));
      //            if (bLoaded)
      //            {
      //               CComPtr < IXMLDOMNode > draggedElements;
      //
      //               _VH(pDoc - > selectSingleNode(CComBSTR(sOperation.c_str()), & draggedElements));
      //
      //               bRetVal = (draggedElements != 0);
      //            }
      //         }
      //      }

      return bRetVal;

   }

   /**
    * Creates the raw XML for the items contained by this object.
    *
    * @param sSource The source where this fragment is coming from
    * @return A CString containing a document fragment, if no operation name
    * was provided then the string will be empty.
    */
   public static String createXMLFragment( String sSource ) {
       String xmlString = "";
       
       if( sSource.length() == 0 )
           return xmlString;
       
       // Create an XML Document to hold the information for
       // what is being dragged.
       Document doc = new DOMDocument();
       
       // Create root node
       Element createdDOMElement = XMLManip.createElement(doc,MAINELEMENT);
       
       // The toplevel element for the dragged items
       Element draggedElements = createdDOMElement;
       
       if (draggedElements!=null) {
           // Add the source element
           createdDOMElement = XMLManip.createElement(draggedElements,SOURCE);
           if (createdDOMElement!=null) {
               XMLManip.setAttributeValue(createdDOMElement, NAME,sSource);
           }
           
           // copy model elements
           if( m_pModelElements != null ) {
               int nCount = m_pModelElements.getCount();
               
               for( int i = 0; i < nCount && createdDOMElement!=null; i++ ) {
                   IElement modelElement = m_pModelElements.item( i );
                   if( modelElement != null ) {
                       String xmiid = modelElement.getXMIID();
                       String topLevelXMIID = modelElement.getTopLevelId();
                       
                       if (xmiid.length()>0) {
                           // Add this item
                           createdDOMElement = XMLManip.createElement(draggedElements, MODELELEMENT);
                           
                           if (createdDOMElement != null) {
                               XMLManip.setAttributeValue(createdDOMElement, TOPLEVELID, topLevelXMIID);
                               XMLManip.setAttributeValue(createdDOMElement, XMIID, xmiid);
                           }
                       }
                   }
               }
           }  // model elements
           
           /// now do the presentation elements
           if( m_pPresentationElements != null) {
               int nCount = m_pPresentationElements.getCount();
               
               for( int i = 0; i < nCount; i++ ) {
                   IPresentationElement pe = m_pPresentationElements.item( i );
                   if( pe != null ) {
                       String xmiid = pe.getXMIID();
                       String topLevelXMIID = pe.getTopLevelId();
                       
                       if (xmiid.length() > 0 && topLevelXMIID.length() > 0) {
                           // Add this item
                           createdDOMElement = XMLManip.createElement(draggedElements,PRESENTATIONELEMENT);
                           if (createdDOMElement!= null) {
                               XMLManip.setAttributeValue(createdDOMElement, TOPLEVELID, topLevelXMIID);
                               XMLManip.setAttributeValue(createdDOMElement, XMIID, xmiid);
                           }
                       }
                   }
               }
           }  // presentation elements
           
           
           // Add diagram locations
           String location = null;
           if( m_DiagramLocations.size() > 0 ) {
               for (Iterator it = m_DiagramLocations.iterator() ; it.hasNext(); location = (String)it.next()) {
                   // Add this item
                   createdDOMElement = XMLManip.createElement(draggedElements,DIAGRAM);
                   if (createdDOMElement!= null) {
                       XMLManip.setAttributeValue(createdDOMElement, LOCATION, location);
                   }
               }
           }
           
           
            /* requires correct implementation of GenericElement
            // Add Generic Elements
              if( m_GenericElements.size() > 0)
              {
                for (Iterator it = m_GenericElements.iterator() ; it.hasNext()) ; String generic = it.next()))
                {
                    // Add this item
                    createdDOMElement = XMLManip.createElement(draggedElements, generic);
                    manip.CreateElement(pDraggedElements, it->m_sElementName,&pCreatedDOMNode );
                      pCreatedDOMElement = pCreatedDOMNode;
                      pCreatedDOMNode = 0;
                      if (pCreatedDOMElement)
                      {
                         std::vector< std::pair < xstring, xstring > >::iterator it2 = it->m_sAttrNameValues.begin();
             
                         for (; it2 != it->m_sAttrNameValues.end() ; ++it2)
                         {
                            manip.SetAttributeValue(pCreatedDOMElement, it2->first, CComBSTR(it2->second.c_str()));
                         }
                      }
                   }
                }
                // done building the DOM fragment, now get it's raw XML
             }
             **/
           
       }
       return doc.asXML();
   }
}
