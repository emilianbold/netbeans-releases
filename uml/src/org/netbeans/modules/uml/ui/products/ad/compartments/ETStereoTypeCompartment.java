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



package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;

import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import com.tomsawyer.editor.TSEFont;
import com.tomsawyer.editor.TSEImage;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

/**
 * @author Embarcadero Technologies Inc.
 *
 * 
 */
public class ETStereoTypeCompartment extends ETNameCompartment implements IADStereotypeCompartment
{

   private static final String STEREOTYPE_CONFIG_FILE = "StereotypeIcons.etc";
   public static String SHOWSTEREOTYPEASICONS_STRING = "ShowStereotypesAsIcons";
   private static Document m_Document = null;
   private boolean m_showStereotypesAsIcons = true;

   private static HashMap stereoTypeIconMap;

   private static final int ICON_SIZE = 15;

   private ArrayList m_iconStereoTypesList = new ArrayList();
   private ArrayList m_textStereoTypesList = new ArrayList();
   private String m_oldStereotypeText = null;
   private TSEFont m_defaultTextFont = new TSEFont("Arial-italic-12");

   public ETStereoTypeCompartment()
   {
      super();
      this.init();
   }

   public ETStereoTypeCompartment(IDrawEngine pDrawEngine)
   {
      super(pDrawEngine);
      this.init();
   }

   private void init()
   {
      this.setFontString("Arial-italic-12");
      this.initResources();
      this.setReadOnly(true);

   }

   private static HashMap getStereoTypeIcons()
   {
      if (ETStereoTypeCompartment.stereoTypeIconMap == null)
      {

         IConfigManager conman = ProductRetriever.retrieveProduct().getConfigManager();
         m_Document = XMLManip.getDOMDocument(conman.getDefaultConfigLocation() + STEREOTYPE_CONFIG_FILE);

         if (m_Document != null)
         {
            ETStereoTypeCompartment.stereoTypeIconMap = new HashMap();

            Element root = m_Document.getRootElement();
            Element icons = (Element)XMLManip.selectSingleNode(root, "Icons");
            for (Iterator i = icons.elementIterator("Icon"); i.hasNext();)
            {
               Element stereotypeIconElement = (Element)i.next();
               Image stereotypeImage = TSEImage.loadImage(ETStereoTypeCompartment.class, XMLManip.getAttributeValue(stereotypeIconElement, "Filename"));
               stereoTypeIconMap.put(XMLManip.getAttributeValue(stereotypeIconElement, "Stereotype"), stereotypeImage);
            }
         }
         return ETStereoTypeCompartment.stereoTypeIconMap;

      }
      else
      {
         return ETStereoTypeCompartment.stereoTypeIconMap;
      }
   }

   public void initResources()
   {
      this.setName(" ");
      // First setup our defaults in case the colors/fonts are not in the 
      // configuration file
      setResourceID("stereotype", Color.BLACK);

      // Now call the base class so it can setup any string ids we haven't already set
      super.initResources();
   }

   /**
    * Calculates the "best" size for this compartment.  The calculation sets 
    * the member variable m_szCachedOptimumSize, which represents the "best" 
    * size of the compartment at 100%.
    *
    * @param pDrawInfo The draw info used to perform the calculation.
    * @param bAt100Pct pMinSize is either in current zoom or 100% based on this 
    *                  flag.  If bAt100Pct then it's at 100%
    * @return The optimum size of the compartment.
    */
   public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   {
		IETSize retVal = null;
		TSEGraphics graphics = getGraphics(pDrawInfo);

      String staticText = getStereotypeText();
      
      if ((staticText != null) && (staticText.length() > 0))
      {
         initStereotypeLists(staticText);
         setName(staticText);
      }

		retVal = super.calculateOptimumSize(pDrawInfo, false);
                
                m_showStereotypesAsIcons = showStereotypeIcons();

		if (m_showStereotypesAsIcons)
		{			
			if (m_textStereoTypesList.size() == 0)
			{
				retVal = new ETSize(0,0);
			}

			if (m_iconStereoTypesList.size() > 0)
			{
				// Now expand the size of the stereotype icons
				retVal.setWidth(retVal.getWidth() + (ICON_SIZE * m_iconStereoTypesList.size()) + 1);
				retVal.setHeight(retVal.getHeight() + ICON_SIZE + 1);
			}
		}else if (m_textStereoTypesList.size() == 0 && m_iconStereoTypesList.size() == 0){
			retVal = new ETSize(0,0);	
		}
      
		// Make sure only 1 to 1 ratio is the set in the internal size,
		if (retVal != null)
		{
			internalSetOptimumSize(retVal.getWidth(), retVal.getHeight());
		}		
      
		// Now Scale the device units
		return bAt100Pct ? getOptimumSize(bAt100Pct) : this.scaleSize(this.m_cachedOptimumSize, graphics != null ? graphics.getTSTransform() : this.getTransform());

   }

   private void initStereotypeLists(String pStereoTypeText)
   {
      String staticText = this.getStereotypeText();

      if (staticText != null && (!staticText.equals(this.m_oldStereotypeText)))
      {
         this.m_oldStereotypeText = staticText;

         m_textStereoTypesList.clear();
         m_iconStereoTypesList.clear();
         HashMap stereoTypeIconMap = ETStereoTypeCompartment.getStereoTypeIcons();

         String tempString = staticText.replaceAll("<<", "");
         tempString = tempString.replaceAll(">>", "");
         tempString = tempString.trim();

         String[] stereoTypeWords = tempString.split(",");

         if (stereoTypeWords.length > 0)
         {
            for (int i = 0; i < stereoTypeWords.length; i++)
            {
               String textToken = stereoTypeWords[i];
               Image iconToken = (Image)stereoTypeIconMap.get(textToken.trim());
               if (iconToken == null)
               {
                  m_textStereoTypesList.add(textToken);
               }
               else
               {
                  m_iconStereoTypesList.add(iconToken);
               }
            }
         }
      }
   }

   public void draw(IDrawInfo pDrawInfo, IETRect pBoundingRect)
   {
      String staticText = getStereotypeText();

      if (!m_showStereotypesAsIcons)
      {
         this.setName(staticText);
         super.draw(pDrawInfo, pBoundingRect);
      }
      else
      {
         setWinClientRectangle((IETRect)pBoundingRect.clone());

         TSEGraphics graphics = pDrawInfo.getTSEGraphics();
         TSTransform transform = graphics.getTSTransform();

         Font compartmentFont = getCompartmentFont(pDrawInfo.getFontScaleFactor());
         graphics.setFont(compartmentFont);

         String stereoTypesText = null;
         if (m_textStereoTypesList.size() > 0)
         {
            stereoTypesText = "<<";
            for (Iterator textStereoTypesIterator = m_textStereoTypesList.iterator(); textStereoTypesIterator.hasNext();)
            {
               stereoTypesText = stereoTypesText + (String)textStereoTypesIterator.next() + ",";
            }
            stereoTypesText = stereoTypesText.substring(0, (stereoTypesText.length() - 1)) + ">>";

            if (stereoTypesText != null)
            {
               graphics.setColor(getCompartmentFontColor());
               graphics.drawString(
                  stereoTypesText,
                  (pBoundingRect.getIntX() + pBoundingRect.getIntWidth() / 2) - (graphics.getFontMetrics().stringWidth(stereoTypesText) / 2),
                  pBoundingRect.getIntY() + graphics.getFontMetrics().getHeight() - 2);
            }
         }

         if (m_iconStereoTypesList.size() > 0)
         {
            ImageObserver imageObserver = graphics.getGraphWindow();
            int scaledIconSize = (int) (ICON_SIZE * transform.getScaleX());
            int xPos = (pBoundingRect.getIntX() + pBoundingRect.getIntWidth()) - scaledIconSize - 2;
            int yPos;

            if (stereoTypesText != null)
            {
               yPos = pBoundingRect.getIntY() + graphics.getFontMetrics().getHeight();
            }
            else
            {
               yPos = pBoundingRect.getIntY();
            }

            for (Iterator iconStereoTypesIterator = m_iconStereoTypesList.iterator(); iconStereoTypesIterator.hasNext();)
            {
               Image stereoTypeIcon = (Image)iconStereoTypesIterator.next();
               graphics.drawImage(stereoTypeIcon, xPos, yPos, scaledIconSize, scaledIconSize, imageObserver);
               xPos = xPos - scaledIconSize - 2;
            }
         }
      }
   }

   protected boolean showStereotypeIcons()
   {
      IPreferenceManager2 prefMan = ProductHelper.getPreferenceManager();

      String sData = "";
      if (prefMan != null)
      {
         sData = prefMan.getPreferenceValue("Diagrams", "ShowStereotypeIcons");
      }
      return (sData != null && sData.equals("PSK_YES")) ? true : false;
   }

   public boolean getShowStereotypeIcons()
   {
      return m_showStereotypesAsIcons;
   }

   public void setShowStereotypeIcons(boolean pNewVal)
   {
      m_showStereotypesAsIcons = pNewVal;
   }

   /**
    * Write ourselves to archive, returns the compartment element
    *
    * @param pProductArchive [in] The archive we're saving to
    * @param pElement [in] The current element, or parent for any new attributes or elements.
    * @param pCompartmentElement [out] The created element for this compartment's information
    */
   public IProductArchiveElement writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pElement)
   {
      IProductArchiveElement retObj = super.writeToArchive(pProductArchive, pElement);
      if (retObj != null)
      {
         retObj.addAttributeBool(SHOWSTEREOTYPEASICONS_STRING, m_showStereotypesAsIcons);
      }
      return retObj;
   }

   /**
    * Update from archive
    *
    * @param pProductArchive [in] The archive we're reading from
    * @param pCompartmentElement [in] The element where this compartment's information should exist.
    */
   public void readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pParentElement)
   {
      super.readFromArchive(pProductArchive, pParentElement);
      m_showStereotypesAsIcons = pParentElement.getAttributeBool(SHOWSTEREOTYPEASICONS_STRING);
   }

   /**
    * This is the name of the drawengine used when storing and reading from the product archive.
    *
    * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
    * product archive (etlp file).
    */
   public String getCompartmentID()
   {
      return "StereotypeCompartment";
   }

   public long onGraphEvent(int nKind)
   {
      switch (nKind)
      {
         case IGraphEventKind.GEK_POST_CREATE :
            m_showStereotypesAsIcons = showStereotypeIcons();
            break;
      }

      return 0;
   }


   public String getStereotypeText()
   {
		String sStereotypeText = "";
   	
		IElement modelElement = getModelElement();
		
		if (modelElement != null){
			sStereotypeText =  super.getStereotypeText(modelElement);
		}
		
		return sStereotypeText;
   }

}
