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
 * Created on Dec 17, 2003
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import java.io.File;

import org.netbeans.modules.uml.core.eventframework.EventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ErrorEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityProperties;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParserSettings;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ParseFacility;

/**
 * @author Aztec
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class UMLParser  extends ParseFacility implements IUMLParser
{
	
	private IEventDispatchController   m_DispController = null;
	private IUMLParserEventDispatcher  m_ParserDispatcher = null;
	private IParserBootstrap           m_ParserBootStrap = null;
	
    public UMLParser()
    {
        initializeUMLParserDispatcher();
    }
    
	public IEventDispatchController getEventDispatchController()
	{
		IEventDispatchController pVal = null;
		try
		{
			if( m_DispController == null)
			{
				m_DispController = new EventDispatchController();
			}
			pVal = m_DispController;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return pVal;
	}
    
	public IUMLParserEventDispatcher getUMLParserDispatcher()
	{
		IUMLParserEventDispatcher pVal = null;
		try
		{      
			IEventDispatchController  cont = getEventDispatchController();
			if( cont != null)
			{
				IEventDispatcher  pDisp =  cont.retrieveDispatcher("UMLParserDispatcher");

				if( pDisp instanceof IUMLParserEventDispatcher)
				{
					pVal = (IUMLParserEventDispatcher)pDisp;
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return pVal;
	}
    
	public void processOperationFromFile(String filename, IREOperation pOperation, 
											 IOpParserOptions pOptions)
	{
		ILanguageParser  pParser = retrieveOpParserForFile(filename, pOptions, pOperation);
		if((pParser != null) && (m_ParserDispatcher != null))
		{      
		   pParser.parseOperation(filename, pOperation);
		}
	}
	
	public void processStreamByType(String stream, String langName, int type)
	{
	   if(m_ParserDispatcher != null)
	   {
		  try
		  {
		  		m_ParserDispatcher.fireBeginParse("", null);
         	 	ILanguageParser pParser = retrieveParserForLang(langName);   
         	 	if(pParser != null)
         	 	{  
         	 		pParser.processStreamByType(stream, type);               
         	 	}
         	 	m_ParserDispatcher.fireEndParse("", null);
		  }
		  catch(Exception e)
		  {
			 e.printStackTrace();
		  }
	   }  
	}

	/**
	 * Executes the facility on the specified file.
	 * 
	 * @param filename [in] The file to be processed.
	 * @throws PF_E_FILE_NOT_EXIST - if the file does not exist.
	 */
	public void processStreamFromFile(String filename)
	{
		if(m_ParserDispatcher != null)
		{
			try
			{
				m_ParserDispatcher.fireBeginParse(filename, null);
				if(!(new File(filename).exists()))
				{
					IErrorEvent  pEvent = new ErrorEvent();
					if(pEvent != null)
					{
						String msg = PFMessages.getString(
						        "IDS_E_FILE_NOT_EXIST",
                                new Object[] { filename } );
						pEvent.setErrorMessage(msg);
						m_ParserDispatcher.fireError(filename, pEvent, null);
					}
				}
				else
				{
					ILanguageParser  pParser = retrieveParserForFile(filename);
					if(pParser != null)
					{  
						pParser.parseFile(filename);               
					}
				}
				m_ParserDispatcher.fireEndParse(filename, null);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}  

	}

	public void processStreamFromFile(String filename, ILanguageParserSettings 
									   					pSettings)
	{
		if(m_ParserDispatcher != null)
		{
			try
			{
				m_ParserDispatcher.fireBeginParse(filename, null);
				if(!(new File(filename).exists()))
				{
					IErrorEvent  pEvent = new ErrorEvent();
					if(pEvent != null)
					{
                        String msg = PFMessages.getString(
                                "IDS_E_FILE_NOT_EXIST",
                                new Object[] { filename } );
						pEvent.setErrorMessage(msg);
						m_ParserDispatcher.fireError(filename, pEvent, null);
					}
				}
				else
				{
					ILanguageParser pParser = retrieveParserForFile(filename);   
					if(pParser != null)
					{
//						TODO: Aztec
//						ILanguageSettingsParser pSettingsParser = pParser;
//						if((pSettingsParser != null) && (pSettings != null))
//						{
//							pSettingsParser.parseFileWithSettings(filename, pSettings);	
//						}
//						else
						{
							pParser.parseFile(filename);               
						}
					}
				}
				m_ParserDispatcher.fireEndParse(filename, null);
			}
			catch(Exception e)
			{
			
		  }
 	   }  
	}


	public void initializeUMLParserDispatcher()
	{
		try
		{
			m_ParserDispatcher= new UMLParserEventDispatcher();
		  	IEventDispatchController controller = getEventDispatchController();
		  	if( controller != null)
		  	{
		  		controller.addDispatcher("UMLParserDispatcher", m_ParserDispatcher );
		  	}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public void deinitializeUMLParserDispatcher()
	{
		try
		{
			if( m_DispController != null)
			{
				IEventDispatcher  disp =  m_DispController.removeDispatcher("UMLParserDispatcher");
			}
			m_DispController   = null;
			m_ParserDispatcher = null;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public ILanguageParser retrieveParserForFile(String filename)
	{
		ILanguageParser pParser = null;
		try
		{
			initializeBootStrap();
			if(m_ParserBootStrap != null)
			{
				IFacilityProperties  pProperties = getProperties();
				pParser = m_ParserBootStrap.initializeParserForFile(pProperties, filename, 
													   	  "Default");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return pParser;
	}

	public ILanguageParser retrieveParserForLang(String langName)
	{
		ILanguageParser pParser = null;
		try
		{
			initializeBootStrap();
			if(m_ParserBootStrap != null)
			{
				IFacilityProperties pProperties = getProperties();
				pParser = m_ParserBootStrap.initializeParser(pProperties, 
															langName, "Default");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	   return pParser;
	}

	/**
	 * Retreives the parser and initializes the parser ports.  The ports initialization 
	 * is defined by the facilities properties.
	 *
	 * @param filename [in] The file to be parsed.
	 * @param pParser [out] The initialized parser.
	 */
	public ILanguageParser retrieveOpParserForFile(String filename, 
														IOpParserOptions pOptions, 
														IREOperation pOperation)
	{
		ILanguageParser pParser = null;
		try
		{
			initializeBootStrap();
			if(m_ParserBootStrap != null)
			{
				IFacilityProperties  pProperties = getProperties();
				if(pOptions != null)
				{
					pOptions.setOperation(pOperation);
				}
				pParser  = m_ParserBootStrap.initializeOperationParserForFile(
																	pProperties, 
																	filename, 
																	"Default", 
																	pOptions                                                                 
																	);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
 	   return pParser;
	}

//	   ----------------------------------------------------------------------------
//	   InitializeBootStrap
//	   ----------------------------------------------------------------------------

	/**
	 * Initializes the bootstrap mechanism.  The bootstrap class used to
	 * initilaize parser is defined by the <I>BootStrap</I> property.
	 */
	public void initializeBootStrap()
	{
	   try
	   {
		  if(m_ParserBootStrap == null)
		  {
		
			 String value = getPropertyValue("BootStrap");
    		 if(value != null && value.length() > 0)
			 {
				IParserBootstrap pBootStrap = (IParserBootstrap)Class.forName(value).newInstance();
				if(pBootStrap != null)
				{
				   m_ParserBootStrap = pBootStrap;
				}
			 }
		  }
	   	}
	   	catch(Exception e)
	   	{
	   		e.printStackTrace();
	   	}
	}
}


