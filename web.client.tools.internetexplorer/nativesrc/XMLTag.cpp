/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *      jdeva <deva@neteans.org>
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
#include "stdafx.h"
#include "XMLTag.h"
#include <msxml2.h>

IXMLDOMDocument *XMLTag::m_pXMLDOM;

/**
 * createXMLDOM creates the xml representation of a XML tree
 *
 * pXMLTag - (in) the xml node being added
 * pXMLDOMNode - (out) TBD (smart pointer to an <IXMLDOMNode>)
 * return value: TBD (XMLTag)
 */
IXMLDOMElement *XMLTag::createXMLDOM(XMLTag *pXMLTag, IXMLDOMNode *pXMLDOMNode) {
    USES_CONVERSION;
    CComPtr<IXMLDOMElement> spXMLDOMElement;

    HRESULT hr = m_pXMLDOM->createElement(T2BSTR(pXMLTag->name.c_str()), &spXMLDOMElement);
    map<tstring, tstring>::iterator iter = pXMLTag->attributes.begin();
    while(iter != pXMLTag->attributes.end()) {
        CComVariant var = T2BSTR(iter->second.c_str());
        spXMLDOMElement->setAttribute(T2BSTR(iter->first.c_str()), var);
        ++iter;
    }

    if(pXMLTag->childTags.size() > 0) {
        // node with child tags
        list<XMLTag *>::iterator tagIter = pXMLTag->childTags.begin();
        while(tagIter != pXMLTag->childTags.end()) {
            createXMLDOM(*tagIter, spXMLDOMElement);
            ++tagIter;
        }
    }
    else if (pXMLTag->value.length() > 0) {
        // node with a text value rather than child tags. Handle by
        // creating a Text (rather than element) dom node for just the text
        // and add as a child tag to the tag it belongs to
        // <foo>bar</foo> for example would result in an IXMLDOMElement node
        // named foo with a child IXMLDOMText node named bar.
        CComPtr<IXMLDOMText> spXMLDOMText;
        HRESULT hr = m_pXMLDOM->createTextNode(
            T2BSTR(pXMLTag->value.c_str()), &spXMLDOMText);
        spXMLDOMText->put_text(T2BSTR(pXMLTag->value.c_str()));

        CComPtr<IXMLDOMNode> spNewXMLDOMa;
        spXMLDOMElement->appendChild(spXMLDOMText, &spNewXMLDOMa);
    }

    CComPtr<IXMLDOMNode> spNewXMLDOM;
    pXMLDOMNode->appendChild(spXMLDOMElement, &spNewXMLDOM);
    return spXMLDOMElement.Detach();
}

XMLTag::~XMLTag() {
    list<XMLTag *>::iterator tagIter = childTags.begin();
    while(tagIter != childTags.end()) {
        delete *tagIter;
        ++tagIter;
    }
    attributes.clear();
    childTags.clear();
}

tstring XMLTag::toString() {
    HRESULT hr;
    if(m_pXMLDOM == NULL) {
        hr = CoCreateInstance(__uuidof(DOMDocument30), NULL, CLSCTX_INPROC_SERVER, 
                                __uuidof(IXMLDOMDocument), (void**)&m_pXMLDOM);
        CComPtr<IXMLDOMProcessingInstruction> spProcessingInstruction;
        hr = m_pXMLDOM->createProcessingInstruction(L"xml", L"version='1.0'", 
                                                    &spProcessingInstruction);
        CComPtr<IXMLDOMNode> spNewChild;
        m_pXMLDOM->appendChild(spProcessingInstruction, &spNewChild);
    }

    //create XML response
    CComPtr<IXMLDOMElement> spXMLDOMElement = createXMLDOM(this, m_pXMLDOM);
    CComBSTR xmlBstrString;
    m_pXMLDOM->get_xml(&xmlBstrString);

    //Cleanup for next use
    CComPtr<IXMLDOMNode> spOldChild;
    m_pXMLDOM->removeChild(spXMLDOMElement, &spOldChild);

    return (TCHAR *)(xmlBstrString);
}

XMLTag &XMLTag::addChildTag(const tstring name) {
    XMLTag *pXMLlTag = new XMLTag(name);
    childTags.push_back(pXMLlTag);
    return *pXMLlTag;
}

/**
 * Adds a child tag to an XML Tag, with a text value
 * eg: parameters name=bar, value=baz for existing element foo would result
 * in XML such as: <foo><bar>baz</bar></foo>
 */
XMLTag &XMLTag::addChildTag(const tstring name, const tstring value) {
    XMLTag *pXMLlTag = new XMLTag(name, value);
    childTags.push_back(pXMLlTag);
    return *pXMLlTag;
}

/**
 * Adds a child xml tag to this xml tag. The text value here is
 * specified as an int for programmer convenience
 * eg: parameters name=bar, value=12 for existing element foo would result
 * in XML such as: <foo><bar>12</bar></foo>
 */
XMLTag &XMLTag::addChildTag(const tstring name, int value) {
    TCHAR buffer[32];
    _itot_s(value, buffer, 32, 10);
    return addChildTag(name, buffer);
}