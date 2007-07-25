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

package org.netbeans.modules.uml.core.support.umlutils;

import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.dom4j.Attribute;
import org.dom4j.Node;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingErrorDialog;
import org.openide.util.NbPreferences;

public class FormatterHelper
{
   public FormatterHelper( Node pNode )
   {
	if (pNode != null)
	{
		m_cpNode = pNode;
	}
   }
   public FormatterHelper( IElement pElement )
   {
   	if (pElement != null)
   	{
   		m_cpElement = pElement;
   		Node pNode = pElement.getNode();
   		m_cpNode = pNode;
   	}
   }
   public FormatterHelper( Object pDispatch )
   {
   	if (pDispatch != null)
   	{
   		if (pDispatch instanceof IElement)
   		{
   			IElement pElement = (IElement)pDispatch;
			m_cpElement = pElement;
			if (pElement != null)
			{
				Node pNode = pElement.getNode();
				m_cpNode = pNode;
			}
   		}
   		else if (pDispatch instanceof Node)
   		{
			m_cpNode = (Node)pDispatch;
   		}
   	}
   }

   public Node getNode()
   {
		return m_cpNode;
   }
   public IElement getElement()
   {
	return m_cpElement;
   }

   /// Determine the language element associated with this element
   public ILanguage getLanguage()
   {
	if( m_cpLanguage == null)
	{
	   ILanguage cpLanguageElement = null;
	   if( useElementLanguage() )
	   {
		  m_cpLanguage = getActiveLanguageOfNode();
	   }
	   else
	   {
	   	  m_cpLanguage = getActiveLanguageOfProjectNode();
	   }

	   if (m_cpLanguage == null)
	   {
		  m_cpLanguage = getDefaultLanguage();
	   }
	}
	return m_cpLanguage;
   }
   
   /**
	* Retrieves a model elements associated language.  If the model element
	* is associated to more than one language then the first language is the 
	* active language.
	*
	* @param pElement [in] The element being processed.
	* @param pVal [out] The active language for the element.
	*/
   public String getLanguageName()
   {
		if( m_bsLanguage == null || m_bsLanguage.length() == 0)
		{
		   m_bsLanguage = getLanguage().getName();
		}
		return m_bsLanguage;
   }

   /// Determine the default language element
   public ILanguage getDefaultLanguage()
   {
	if (m_cpDefaultLanguage == null)
	{
	   ILanguageManager pManager = getLanguageManager();
	   if( pManager != null)
	   {
			//m_cpDefaultLanguage = pManager.getLanguage("UML");
		  	// changing this for jUML
		  	m_cpDefaultLanguage = pManager.getLanguage("Java");
	   }
	}
	return m_cpDefaultLanguage;
   }

   /**
	* Attempt to get the formatted description using XSLT
	*/
   public String getElementsXSLTFile()
   {
	    String bsType = getElementType();
		String bsFormatFile = getLanguage().getFormatStringFile(bsType);
	    if( bsFormatFile.length() <= 0 )
	    {
		   bsFormatFile = getDefaultLanguage().getFormatStringFile( bsType );
	
		   if( bsFormatFile.length() <= 0 )
		   {
			 // Now QI for the NamedElement, attempting to use the NamedElement.xsl file at all costs
			 IElement pElement = getElement();
			 if (pElement instanceof INamedElement)
			 {
				bsFormatFile = getDefaultLanguage().getFormatStringFile("NamedElement");
			 }
		  }
	   }
	   return bsFormatFile;
   }

   /// Key used to store the XSLT format string
   public String getFormatterKey()
   {
		String bsKey = getElementType();
		return bsKey;
   }

   /** 
	* Transforms (using XSLT)  @a element using @a proc as the XSLProcessor
	* 
	* @param element[in] The element to format
	* @param proc[in] The XSL processor to use
	* @param format[out] The resulting XML document
	* 
	* @return HRESULT
	*/
   public String formatWithProcessor( XslTransformer proc )
   {
       String format = "";
       Node node = null;
       AliasMarker marker = null;
       try {
           if (proc != null) {
               node = getNode();
               if( node != null ) {
                   marker = new AliasMarker( node );
                   if (node instanceof org.w3c.dom.Node) {
                       DOMSource source = new DOMSource((org.w3c.dom.Node)node);
                       StringWriter sw = new StringWriter();
                       StreamResult result = new StreamResult(sw);
                       
                       //Jyothi: Deleting an element from a diagram causes DOM errors
                       //and javax.xml.transform.TransformerException: javax.xml.transform.TransformerException: org.w3c.dom.DOMException: Not supported yet
                       if (node.getDocument() == null) {
                           return format;
                       }
                       //Jyothi : end
                       XSLTHelper.pushActiveDocument(node.getDocument());
                       proc.transform(source, result);
                       if (result != null) {
                           sw = (StringWriter)result.getWriter();
                           format = sw.toString();
                       }
                   }
               }
           }
       } catch (TransformerException e) {
           //Log.stackTrace(e);
           //e.printStackTrace();
           e.printStackTrace();
       } catch(Throwable et) {
           et.printStackTrace();
       } finally {
           if(marker != null) {
               marker.clear();
           }
           if (node.getDocument() != null) { //Jyothi: wrote this condition for the same issue as above
               XSLTHelper.popActiveDocument();
           }
       }
       return format;
   }

   /**
	* Retrieves the node that defines the property definiton for the
	* specified element.
	*
	* @param pVal [out] The XML node that represent the element type.
	* @return true when the element definition was found, false if the default was used.
	*/
   public Node getProDefNode()
   {
   		Node pVal = null;

		String bsElementType = getDefinitionName();
		if( bsElementType.equals("Parameter"))
		{
			Node node = getNode();
			if( node != null )
			{
		   		String bsDirection = XMLManip.getAttributeValue(node, "direction");
		   		if( bsDirection != null && bsDirection.equals("result"))
		   		{
			  		bsElementType = "ReturnTypeParameter";
		   		}
			}
	    }

		boolean bFoundByType = true;
		pVal = getProDefNode( bsElementType );
		if( pVal == null )
		{
		 // if we didn't find it based on the element type, don't pass in an element
		 // and that will cause us to look for the unknown definition (Unknown)
		 // which right now is a spinoff of NamedElement
		 //
	   	 pVal = getProDefNode( "Unknown");
		}
		return pVal;
   }

   /**
	* Retrieves the node that defines the property definiton for the
	* specified element.
	*
	* @param pElement [in] The element being processed.
	* @param pLanguage [in] The language that is being represented.
	* @param pVal [out] The XML node that represent the element type.
	*/
   public Node getProDefNode( String propertyDefName)
   {
		Node pVal = null;
		if (propertyDefName != null && propertyDefName.length() > 0)
		{
			pVal = getLanguage().getFormatDefinition( propertyDefName );
		}
		return pVal;      
   }

   /// Retrieves the context used to retrieve a particular definition
   public String getContext()
   {
	return m_Context;
   }
   public void setContext(String newVal)
   {
		m_Context = newVal;
   }

   protected String getElementType()
   {
		if( m_bsType == null || m_bsType.length() == 0)
		{
			Node node = getNode();
			if( node != null )
			{
				 String bsNodeName = node.getName();
				 if (bsNodeName.indexOf("UML:") > -1)
				 {
					m_bsType = bsNodeName.substring(4);
				 }
				 else
				 {
				 	m_bsType = bsNodeName;
				 }
			}
		}
		return m_bsType;
   }
   /**
	* Retrieves the definition name, built by appending the
	* element type name with the context name
	* 
	* @return The final definiation name
	*/
   protected String getDefinitionName()
   {
		String name = getElementType();
		String context = getContext();
		if( context != null && context.length() > 0)
		{
		   name += "_";
		   name += context;
		}
		return name;
   }

   /// Determine (from the user preferences)
   /// wether to use the input element, or the project to get the language
   protected boolean useElementLanguage()
   {
       //kris richards - "DisplayFormatString" pref expunged. Set to "PSK_ELEMENT"
       return true ;
   }

   /// Retrieve the language manager from the project
   protected ILanguageManager getLanguageManager()
   {
   		ILanguageManager pManager = null;
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		 if( prod != null)
		 {
		   pManager = prod.getLanguageManager();
		}
		return pManager;
   }

   /// Uses a XPATH query to determine the langauage of the node
   protected ILanguage getActiveLanguageOfNode()
   {
   		ILanguage pLang = null;
		String query = "ancestor-or-self::UML:Element.ownedElement/UML:SourceFileArtifact/@sourcefile[1]";
		Node node = getNode();
		if( node != null )
		{
			Node pResultNode = null;
			try
			{
				pResultNode = node.selectSingleNode( query );
			}
			catch (Exception e)
			{
				//just ignore, as DOM4J can throw an exception while doing ancestor queries
			}
			 if (pResultNode != null)
			 {
				 String str = pResultNode.getText();
				 if (str.length() > 0)
				 {
				 	ILanguageManager pManager = getLanguageManager();
				    if( pManager != null)
				    {
						pLang = pManager.getLanguageForFile(str);   
					}
			     }
			}
			else
			{
			   pLang = getActiveLanguageOfProjectNode();
			}
		}
		return pLang;
   }

   /// Uses a XPATH query to determine the langauage of the node's project
   protected ILanguage getActiveLanguageOfProjectNode()
   {
		ILanguage pLang = null;
		String query = "ancestor-or-self::UML:Project/@defaultLanguage";
		Node node = getNode();
		if( node != null )
		{
		 	Node pResultNode = null;
			try
			{
				pResultNode = node.selectSingleNode( query );
			}
			catch (Exception e)
			{
				//just ignore, as DOM4J can throw an exception while doing ancestor queries
			}
		 	if (pResultNode != null)
		 	{
				String str = pResultNode.getText();
				if (str.length() > 0)
				{
					ILanguageManager pManager = getLanguageManager();
				  	if( pManager != null )
				  	{
						pLang = pManager.getLanguage(str);
				  	}
		   		}
			}
		}
		return pLang;
   }

   /**
    * Simple class that manages the setting and removing of a temporary
    * xml attribute that is used to determine whether or not
    * an element should use the aliasing feature or not
    */

   public class AliasMarker {
       /**
        *
        * If the Aliasing mode is currently on, this method marks the passed-in node
        * with the "embt__Aliased" xml attribute, setting it's value to "on". This
        * xml attribute will be removed after the node has been properly transformed.
        *
        * @param node[in] The node to potentially affect
        * @param manager[in] The preference manager
        *
        */
       public AliasMarker( Node node ) {
           if( node != null) {
               String bstrQuery = "ancestor::UML:Project";
               Node cpXMLDOMNode = null;
               try {
                   cpXMLDOMNode = node.selectSingleNode( bstrQuery );
               } catch (Exception e) {
                   //just ignore, as DOM4J can throw an exception while doing ancestor queries
               }
               if( cpXMLDOMNode != null ) {
                   if (cpXMLDOMNode instanceof org.dom4j.Element) {
                       m_Project = (org.dom4j.Element)cpXMLDOMNode;
                   }
               }
               if( m_Project != null ) {
                   
                   //kris richards - changing to NbPrefs
                   boolean bIsAliased = NbPreferences.forModule(FormatterHelper.class).getBoolean("UML_Show_Aliases", false) ;
                   
                   if( bIsAliased ) {
                       XMLManip.setAttributeValue( m_Project, "embt__Aliased", "on" );
                   }
               }
           }
       }
       
       /**
        *
        * Removes the "embt__Aliased" xml attribute set in the constructor
        * of this object
        *
        */
       public void clear() {
           if( m_Project != null) {
               // we removed the throw from around this because when executing the WebReport
               // we were throwing here
               Attribute attr = m_Project.attribute("embt__Aliased");
               if(attr != null) {
                   m_Project.remove( attr );
               }
           }
       }
       
       private org.dom4j.Element m_Project;  // project of the initializing node
   };

   private Node 		m_cpNode;
   private IElement  	m_cpElement;
   private ILanguage	m_cpLanguage;           // access via GetLanguage()
   private ILanguage 	m_cpDefaultLanguage;    // access via GetDefaultLanguage()

   private String m_bsLanguage;  // access via GetLanguageName()
   private String m_bsType;      // access via GetElementType()
   private String m_Context;     // access via GetContext();
};
