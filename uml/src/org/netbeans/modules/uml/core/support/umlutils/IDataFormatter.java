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
