/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.xml.text.syntax.dom;

import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import javax.swing.text.BadLocationException;

public class DocumentImpl extends AbstractNode implements org.w3c.dom.Document {

    SyntaxElement syntax;

    DocumentImpl(SyntaxElement element) {
        syntax = element;
    }

    public org.w3c.dom.Attr createAttribute(String str) throws org.w3c.dom.DOMException {
        return null;
    }

    public org.w3c.dom.Element getElementById(String str) {
        return null;
    }
    
    public String getVersion() {
        throw new UOException();
    }
    
    public org.w3c.dom.Element createElement(String str) throws org.w3c.dom.DOMException {
        return null;
    }
    
    public boolean getStrictErrorChecking() {
        throw new UOException();
    }
    
    public org.w3c.dom.DOMImplementation getImplementation() {
        return new DOMImplementationImpl();
    }
    
    public org.w3c.dom.Element createElementNS(String str, String str1) throws org.w3c.dom.DOMException {
        throw new UOException();
    }
    
    public org.w3c.dom.DocumentFragment createDocumentFragment() {
        return null;
    }
    
    public org.w3c.dom.NodeList getElementsByTagNameNS(String str, String str1) {
        return NodeListImpl.EMPTY;  //???
    }
    
    public void setVersion(String str) {
        throw new UOException();
    }
    
    public org.w3c.dom.Attr createAttributeNS(String str, String str1) throws org.w3c.dom.DOMException {
        return null;
    }
    
    public void setStrictErrorChecking(boolean param) {
        throw new UOException();
    }
    
    public void setEncoding(String str) {
        throw new UOException();
    }
    
    public org.w3c.dom.ProcessingInstruction createProcessingInstruction(String str, String str1) throws org.w3c.dom.DOMException {
        return null;
    }
    
    public org.w3c.dom.NodeList getElementsByTagName(String str) {
        return NodeListImpl.EMPTY;  //???
    }
    
    public org.w3c.dom.Element getDocumentElement() {
        return null;  //!!! parse for it
    }
    
    public org.w3c.dom.DocumentType getDoctype() {
//        try {
            //SyntaxElement e = syntax.support.getElementChain(0);
            //!!! locate declaration and return wrapper
//            return new DocumentTypeImpl(null, null, 0);
//        } catch (BadLocationException ex) {
            return null;
//        }
    }
    
    public org.w3c.dom.CDATASection createCDATASection(String str) throws org.w3c.dom.DOMException {
        return null;
    }
    
    public org.w3c.dom.EntityReference createEntityReference(String str) throws org.w3c.dom.DOMException {
        return null;
    }
    
    public boolean getStandalone() {
        throw new UOException();
    }
    
    public short getNodeType() {
        return Node.DOCUMENT_NODE;
    }
    
    public org.w3c.dom.Node adoptNode(org.w3c.dom.Node node) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Text createTextNode(String str) {
        return null;
    }
    
    public String getEncoding() {
        throw new UOException();
    }
    
    public org.w3c.dom.Comment createComment(String str) {
        return null;
    }
    
    public void setStandalone(boolean param) {
        throw new UOException();
    }
    
    public org.w3c.dom.Node importNode(org.w3c.dom.Node node, boolean param) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
}

