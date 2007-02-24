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

import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface IDataFormatter {

//   Perform an XSLT transform on the passed in element.
//HRESULT FormatElement([in] IElement* element, [out, retval] BSTR* format);
  public String formatElement(IElement elem);

//     Perform an XSLT transform on the passed in element using the passed in script name.
//HRESULT FormatElement2([in] IElement* element, [in] BSTR scriptName, [out, retval] BSTR* format );
  public String formatElement(IElement elem, String scriptName);

//   Formats the data elements data.  The property elements used to format the elements data and the formatted string is returned.
//HRESULT FormatElement3([in]IElement* element, [out]IPropertyElement** propElement, [out, retval] BSTR* format);
  public String formatElement(IElement elem, IPropertyElement propElem);

//   Associates an element type with an XSL stylesheet.
//HRESULT AddScript([in] BSTR elementType, [in] BSTR xslFileName);
  public void addScript(String elementType, String xslFileName);

//     Get the on file script name for the passed in key
//HRESULT GetScriptFromMap([in] BSTR key, [out, retval] BSTR* scriptName );
  public String getScriptFromMap(String key);

//     Remove the on file script name for the passed in key
//HRESULT RemoveScriptFromMap([in] BSTR key );
  public void removeScriptFromMap(String key);

//     Clears the on file script names from the map
//HRESULT ClearMap();
  public void clearMap();

// Are alias names displayed?
//HRESULT isAlias([out,retval] VARIANT_BOOL* bAlias);
  public boolean isAliasOn();

// Are alias names displayed?
//HRESULT isAlias([in] VARIANT_BOOL  bAlias);
  public void setAlias(boolean alias);

//   Perform an XSLT transform on the passed in node.
//HRESULT FormatNode([in] IXMLDOMNode* pNode, [out, retval] BSTR* format);
  public String formatNode(Node node);

//     Perform an XSLT transform on the passed in node using the passed in script name.
//HRESULT FormatNode2([in] IXMLDOMNode* pNode, [in] BSTR scriptName, [out, retval] BSTR* format );
  public String formatNode(Node node, String scriptName);

  public String formatNode(org.w3c.dom.Node node, String scriptName);

// Adds a COM object to all known processors that can be referenced within XSLT scripts.
//HRESULT AddObject([in] BSTR namespaceURI, [in] IDispatch* pDisp);
  public void addObject(String namespaceURI, Object obj);

//   Adds a COM object to a specific processor that can be referenced within XSLT scripts.
//HRESULT AddObject2([in] BSTR scriptName, [in] BSTR namespaceURI, [in] IDispatch* pDisp);
  public void addObject(String scriptName, String namespaceURI, Object obj);

//   Converts the property element's data into localized and stringified format, if appropriate.
//HRESULT ProcessEnumeration( [in,out] IPropertyElement* pData);
  public IPropertyElement processEnumeration(IPropertyElement pData);

//   Returns a property element for a modelelement, using the current language.
//HRESULT GetPropertyElement( [in] IElement* pElement, [out,retval] IPropertyElement** pVal);
  public IPropertyElement getPropertyElement(IElement elem);

//   Returns the PropertyElementManager, using the current language."), hidden]
//HRESULT GetElementManager( [out,retval] IPropertyElementManager** pVal);
  public IPropertyElementManager getElementManager();

  public String getFormatStringFile(Object pDisp);
  
  public ILanguage getActiveLanguage(Object pDisp);
  
  public IPropertyElement getPropertyElementByContext(IElement pElement, String context);
}