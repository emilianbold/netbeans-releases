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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramDetails;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.archivesupport.ProductArchiveImpl;

/**
 * 
 * @author Trey Spiva
 */
public class CachedDiagrams implements FileExtensions, IProductArchiveDefinitions
{
   private static CachedDiagrams m_Instance = null;
   private HashMap < String , DiagramDetails > m_CachedInfo = new HashMap< String , DiagramDetails >();
   
   private CachedDiagrams()
   {
   }
   
   public static CachedDiagrams instance()
   {
      if(m_Instance == null)
      {
         m_Instance = new CachedDiagrams();
      }
      return m_Instance;
   }
   
   public void destroy()
   {
      m_Instance = null;
   }
   
   /**
    * Returns the information for the provided sDiagramFilename.  If the CRC's are the same
    * you get the cached information, otherwise the file is cracked open for the information and
    * that is added to the cache.
    */
   public DiagramDetails getInfo(String sDiagramFilename)
   {
      DiagramDetails retVal = null;
      if(sDiagramFilename.length() > 0)
      {
         long etlCrc32  = 0;
         long etlpCrc32 = 0;
         retVal = getInfoFromCache(sDiagramFilename);
         
         if(retVal == null)
         {
            String filename = StringUtilities.ensureExtension(sDiagramFilename, 
                                                              DIAGRAM_PRESENTATION_EXT);
                                                              
            IProductArchive productArchive = new ProductArchiveImpl(filename);
            if(productArchive.isLoaded() == true)
            {
               IProductArchiveElement element = productArchive.getElement(DIAGRAMINFO_STRING);
               if(element != null)
               {  
                  retVal = new DiagramDetails();
                  retVal.setName(element.getAttributeString(DIAGRAMNAME_STRING));
                  retVal.setDiagramAlias(element.getAttributeString(DIAGRAMALIAS_STRING));
                  retVal.setDiagramTypeName(element.getAttributeString(DRAWINGKIND2_STRING));
                  retVal.setDiagramXMIID(element.getAttributeString(DIAGRAM_XMIID));
                  retVal.setToplevelXMIID(element.getAttributeString(NAMESPACE_TOPLEVELID));
                  retVal.setNamespaceXMIID(element.getAttributeString(NAMESPACE_MEID));
                  
                  File file = new File(filename);
                  retVal.setDateModified(file.lastModified());
                  
                  // The diagram kind changed from 6.0 to 6.1, upgrade here.
                  if(retVal.getDiagramTypeName().length() <= 0)
                  {
                     retVal.setDiagramTypeName(element.getAttributeString(DRAWINGKIND_STRING));
                  } 
                  
                  retVal.setAssociatedDiagrams(loadAssociatedDiagrams(productArchive));
                  retVal.setAssociatedElements(loadAssociatedElements(productArchive));
                  
                  addToCache(sDiagramFilename, retVal);
               }
            }
         }
      }
      
      return retVal;
   }
   
   /**
    * Returns the information for the diagram only if it's cached.  
    * false if it's not been cached yet or if the CRC is wrong.
    * 
    * @param filename The filename of the diagram.
    * @return The details of the diagram.
    */
   public DiagramDetails getInfoFromCache(String filename)
   {
      DiagramDetails retVal = null;
      
      if(filename.length() > 0)
      {
         String realTOMFilename = StringUtilities.ensureExtension(filename, 
                                                                  DIAGRAM_LAYOUT_EXT);
         String realPRSFilename = StringUtilities.ensureExtension(filename, 
                                                                 DIAGRAM_PRESENTATION_EXT);
                                                                 
         retVal = m_CachedInfo.get(realPRSFilename);
         
         if(retVal != null)
         {
            // Determine if the cache information is up to date.
            File file = new File(realPRSFilename);
            if(file.lastModified() != retVal.getDateModified())
            {
               retVal = null;
            }
         }
      }
      
      return retVal;
   }
   
   
   /**
    * Adds diagram information to our cache
    */
   public void addToCache(String filename, DiagramDetails details)
   {
      if((filename != null)      && 
         (filename.length() > 0) &&
         (details != null))
      {
         String realPRSFilename = StringUtilities.ensureExtension(filename, 
                                                                  DIAGRAM_PRESENTATION_EXT);
         if(m_CachedInfo.containsKey(realPRSFilename) == true)
         {
            // Whack the old one
            m_CachedInfo.remove(realPRSFilename);
         }
         m_CachedInfo.put(realPRSFilename, details);
      }
   }

   /**
    * Load associated diagrams.
    * 
    * @param pArchive The archive inforamtion.
    */
   public ArrayList < String > loadAssociatedDiagrams(IProductArchive pArchive)
   {
      ArrayList < String > retVal = new ArrayList < String >();
      
      if (pArchive != null)
      {
         IProductArchiveElement pFoundElement;
         int nKey = 1;
         
         pFoundElement = pArchive.getTableEntry(ASSOCIATED_DIAGRAMS_STRING, nKey);
         
         while (pFoundElement != null)
         {
            if (pFoundElement.isDeleted() == false)
            {
               String sXMIID = pFoundElement.getID();
               if (sXMIID.length() > 0)
               {
                  retVal.add(sXMIID);
               }
            }
            
            nKey++;
            pFoundElement = null;
            pFoundElement = pArchive.getTableEntry(ASSOCIATED_DIAGRAMS_STRING, nKey);
         }
      }
         
      return retVal;
   }
   
   /**
    * Load associated elements
    * 
    * @param pArchive The archive inforamtion.
    */
   public ArrayList < ModelElementXMIIDPair > loadAssociatedElements(IProductArchive pArchive)
   {
      ArrayList < ModelElementXMIIDPair > retVal = new ArrayList < ModelElementXMIIDPair >();
      
      if (pArchive != null)
      {
         int nKey = 1;
         IProductArchiveElement foundElement = null;
         foundElement = pArchive.getTableEntry(ASSOCIATED_ELEMENTS_STRING, nKey);
         
         while(foundElement != null)
         {
            if(foundElement.isDeleted() == false)
            {
               String sXMIID = foundElement.getID();
               String sTopLevelXMIID = foundElement.getAttributeString(TOPLEVELID_STRING);
               if((sXMIID.length() > 0) && 
                  (sTopLevelXMIID.length() > 0))
               {
                  retVal.add(new ModelElementXMIIDPair(sTopLevelXMIID, sXMIID));
               }
            }
            nKey++;
            foundElement = null;
            foundElement = pArchive.getTableEntry(ASSOCIATED_ELEMENTS_STRING, nKey);
         }
      }
      
      return retVal;
   }   
   
   public static String getNewDiagramType(int nOldDiagramTypeEnum)
   {
   		String retType = "";
   		if (nOldDiagramTypeEnum == 2)
   		{
   			retType = "Class Diagram";
   		}
   		else if (nOldDiagramTypeEnum == 6)
   		{
   			retType = "Sequence Diagram";
   		}
   		return retType;
   }
}
