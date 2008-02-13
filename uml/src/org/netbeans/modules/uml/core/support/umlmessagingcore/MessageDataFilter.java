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
 * Created on Oct 8, 2003
 *
 */
package org.netbeans.modules.uml.core.support.umlmessagingcore;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 *
 */
public class MessageDataFilter implements IMessageDataFilter
{

	private IMessageService m_MessageService;
	private ETList<IMessageFacilityFilter> m_Filters;
	private String m_FileLocation; 
	
	public MessageDataFilter()
	{	
		m_Filters = new ETArrayList<IMessageFacilityFilter>();
	}
	
	/**
	 * Initializes the class by providing the xml file to store preferences in and
	 * the messenger which this filter applies to.  Eventually we need to combine the
	 * facilities found in the messenger with those in the xml file - because the messenger
	 * only has a list of what facilities have sent messages for this run, whereas the file
	 * will have messages for the last run.
	 *
	 * @param fileLocation[in] The location of the message filter preference file
	 * @param pMessenger[in] The messenger this filter applies to
	 *
	 * @todo To get all past filters I think I need to reread the file in initialize
	 */
	public void initialize( String fileLocation, IMessageService pMessenger )
	{
		m_FileLocation = fileLocation;
		m_MessageService = pMessenger;
		
		try
		{
			// Make sure the file location is absolute.  If it isn't then go
			// off of our home location
			File file = new File(m_FileLocation);
			if (file != null)
			{
				if (!file.isAbsolute())
				{
					// Get the home location from the config manager
					ICoreProduct pCoreProduct = ProductRetriever.retrieveProduct();
					if (pCoreProduct != null)
					{
						IConfigManager configMan = pCoreProduct.getConfigManager();
						if (configMan != null)
						{
							String homeLoc = configMan.getHomeLocation();
							if (homeLoc != null && homeLoc.length() > 0)
							{
								String tempLoc = new File(homeLoc, m_FileLocation).toString();
								m_FileLocation = tempLoc;
							}
						}
					}
				}
				reRead();
				save();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns a list of facility filters.  Each item in the list corresponds to a
	 * particular facility and indicates what message types should be displayed.
	 *
	 * @return The list of filters - one item for each facility
	 */
	public ETList<IMessageFacilityFilter> getFilters()
	{
		return m_Filters;
	}
	
	/**
	 * Given a particular message this routine returns a true or false
	 * indicating if the filter settings would display this message.
	 *
	 * @param pMessageData[in] The message under question.  Should this be displayed.
	 * @return whether or not the message should be displayed
	 */
	public boolean getIsDisplayed( IMessageData pMessageData )
	{
		boolean retVal = false;
		try
		{
			if (pMessageData != null)
			{
				String sFacility = pMessageData.getFacility();
				int nMessageType = pMessageData.getMessageType();
				
				retVal = getIsDisplayed(nMessageType, sFacility);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
		return retVal;
	}
	
	/**
	 * Given a particular message this routine sets the facility and message type to
	 * be displayed according to the parameter newVal.
	 *
	 * @param pMessageData[in] The message under question.  Should this be displayed
	 * @param newVal[in] Sets if it should be displayed
	 */
	public void setIsDisplayed( IMessageData pMessageData, boolean newVal )
	{		
		try
		{
			if (pMessageData != null)
			{
				String sFacility = pMessageData.getFacility();
				int nMessageType = pMessageData.getMessageType();
				
				setIsDisplayed(nMessageType, sFacility, newVal);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Given a particular message type and facility, this routine returns a true or false
	 * indicating if the filter settings would display this message.
	 *
	 * @param nMessageType[in] The message type under question.  Should this be displayed
	 * @param sFacility[in] The message type facility
	 * @return whether or not the message should be displayed
	 */
	public boolean getIsDisplayed(int nMessageType, String sFacility)
	{		
		boolean retVal;
		IMessageFacilityFilter msgFilter = getFilter(sFacility);
		if (msgFilter != null)
		{
			retVal = msgFilter.getIsDisplayed(nMessageType);
		}
		else
		{
			msgFilter = new MessageFacilityFilter();
			msgFilter.setName(sFacility);
			m_Filters.add(msgFilter);
			
			retVal = true;
		}
		return retVal;
	}
	
	/**
	 * Gets the filter corresponding to the argument facility name.
	 *
	 * @param sFacility The facility we want to search for
	 * @param pFilter The output filter
	 */
	protected IMessageFacilityFilter getFilter(String sFacility)
	{
		IMessageFacilityFilter retFilter = null;
		if (m_Filters != null && sFacility != null)
		{
			Iterator<IMessageFacilityFilter> iter = m_Filters.iterator();
			if (iter != null)
			{
				while (iter.hasNext())
				{
					IMessageFacilityFilter msgFacilityItem = iter.next();
					if (msgFacilityItem != null)
					{
						if (sFacility.equals(msgFacilityItem.getName()))
						{
							retFilter = msgFacilityItem;
							break;
						}
					}
				}
				
			}
		}
		return retFilter;
	}
	
	/**
	 * Given a particular message type and facilty, this routine sets the facility and message type to
	 * be displayed according to the parameter newVal.
	 *
	 * @param nMessageType[in] The message type under question.  Should this be displayed
	 * @param sFacility[in] The message type facility
	 * @param newVal[in] Sets if it should be displayed
	 */
	public void setIsDisplayed( int nMessageType, String sFacility, boolean newValue )
	{		
		IMessageFacilityFilter msgFilter = getFilter(sFacility);
		if (msgFilter != null)
		{
			msgFilter.setIsDisplayed(nMessageType,newValue);
		}
		else
		{
			msgFilter = new MessageFacilityFilter();
			msgFilter.setName(sFacility);
			msgFilter.setIsDisplayed(nMessageType, newValue);
			m_Filters.add(msgFilter);
		}
	}

	/**
	 * Saves the file.
	 *
	 * @see CMessageDataFilter::Initialize
	 */
	public void save()
	{
		Document pDoc = XMLManip.getDOMDocument();
		Element pCreatedDOMElement = XMLManip.createElement(pDoc, "FACILITYFILTERS");
		Iterator<IMessageFacilityFilter> iter = m_Filters.iterator();
		if (iter != null && pCreatedDOMElement != null)
		{
			while (iter.hasNext())
			{
				IMessageFacilityFilter pFilterItem = iter.next();
				if (pFilterItem != null)
				{
					Element pCreatedFacilityElement = XMLManip.createElement(pCreatedDOMElement, "FACILITY");
					if (pCreatedFacilityElement != null)
					{
						//Add the facility Name						
						XMLManip.setAttributeValue(pCreatedFacilityElement, "NAME",pFilterItem.getName());
						
						// Add the Facility Display flags
						XMLManip.setAttributeValue(pCreatedFacilityElement, "MT_CRITICAL",Boolean.toString(pFilterItem.getIsDisplayed(MsgCoreConstants.MT_CRITICAL)));				
						XMLManip.setAttributeValue(pCreatedFacilityElement, "MT_ERROR",Boolean.toString(pFilterItem.getIsDisplayed(MsgCoreConstants.MT_ERROR)));
						XMLManip.setAttributeValue(pCreatedFacilityElement, "MT_WARNING",Boolean.toString(pFilterItem.getIsDisplayed(MsgCoreConstants.MT_WARNING)));
						XMLManip.setAttributeValue(pCreatedFacilityElement, "MT_INFO",Boolean.toString(pFilterItem.getIsDisplayed(MsgCoreConstants.MT_INFO)));
						XMLManip.setAttributeValue(pCreatedFacilityElement, "MT_DEBUG",Boolean.toString(pFilterItem.getIsDisplayed(MsgCoreConstants.MT_DEBUG)));                        					
					}
				}
			}
		}
		XMLManip.save(pDoc,m_FileLocation);
	}
	
	/**
	 * Rereads the file from disk.
	 *
	 * @see CMessageDataFilter::Initialize
	 */
	public void reRead()
	{
		Document pDoc = XMLManip.getDOMDocument(m_FileLocation);
		if (pDoc != null)
		{
			Node pFilterElements = XMLManip.selectSingleNode(pDoc,"FACILITYFILTERS");
			if (pFilterElements != null)
			{
				int count = 0;
				List pFiltersNodeList = XMLManip.selectNodeList(pFilterElements,"FACILITY");
				if (pFiltersNodeList != null && (count = pFiltersNodeList.size()) > 0)
				{
					Node pFacilityNode = null;
					for (int i=0;i<count;i++)
					{
						pFacilityNode = (Node)pFiltersNodeList.get(i);
						if (pFacilityNode != null)
						{
							IMessageFacilityFilter pFilter = new MessageFacilityFilter();
							Node node;
							// Name
							pFilter.setName((node = XMLManip.getAttribute(pFacilityNode,"NAME")) != null?node.getText():null);
							
							// Facility Display flags
                            boolean flag = node != null?
                                Boolean.valueOf(node.getText()).booleanValue()
                              : false;

							pFilter.setIsDisplayed(MsgCoreConstants.MT_CRITICAL,
								(node = XMLManip.getAttribute(pFacilityNode,"MT_CRITICAL")) != null?
																flag:false);
							pFilter.setIsDisplayed(MsgCoreConstants.MT_ERROR,
								(node = XMLManip.getAttribute(pFacilityNode,"MT_ERROR")) != null?
																flag:false);
							pFilter.setIsDisplayed(MsgCoreConstants.MT_WARNING,
								(node = XMLManip.getAttribute(pFacilityNode,"MT_WARNING")) != null?
																flag:false);
							pFilter.setIsDisplayed(MsgCoreConstants.MT_INFO,
								(node = XMLManip.getAttribute(pFacilityNode,"MT_INFO")) != null?
																flag:false);
							pFilter.setIsDisplayed(MsgCoreConstants.MT_DEBUG,
								(node = XMLManip.getAttribute(pFacilityNode,"MT_DEBUG")) != null?
																flag:false);
																
							m_Filters.add(pFilter);																									
						}
					}
				}
			}
		}
	}
		
}



