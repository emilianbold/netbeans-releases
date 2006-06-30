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

package org.netbeans.modules.dbschema.migration.archiver.deserializer;

import java.lang.*;
import javax.xml.parsers.*;

import org.xml.sax.helpers.*;
import org.xml.sax.*;

public  class BaseXMLDeserializer extends java.lang.Object
    implements XMLDeserializer,  org.xml.sax.DocumentHandler, org.xml.sax.DTDHandler, org.xml.sax.ErrorHandler
{

    // Fields
    protected  java.lang.Object InitialObject;
    protected  org.xml.sax.Parser Parser;
    public org.xml.sax.Locator TheLocator;
    protected  org.xml.sax.InputSource TheSource;
    public java.lang.StringBuffer TheCharacters;

    // Constructors
    public  BaseXMLDeserializer()
    {
        this.TheCharacters = new StringBuffer();
    } /*Constructor-End*/

    // Methods
    public   void notationDecl(String name, String publicId, String systemId) throws org.xml.sax.SAXException
    {
        
    } /*Method-END*/
    public   void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws org.xml.sax.SAXException
    {
        
    } /*Method-End*/
    public   void processingInstruction(String target, String data) throws org.xml.sax.SAXException
    {
        
    } /*Method-Enc*/
    public   void setDocumentLocator(org.xml.sax.Locator locator)
    {
        this.TheLocator = locator;
    } /*Method-End*/
    public   void ignorableWhitespace(char[] ch, int start, int length) throws org.xml.sax.SAXException
    {
        
    } /*Method-End*/
    public   void endElement(java.lang.String name) throws org.xml.sax.SAXException
    {
        this.TheCharacters.delete(0,this.TheCharacters.length());
    } /*Method-End*/

    public   void endDocument()    throws org.xml.sax.SAXException
    {
        this.freeResources();
    } /*Method-End*/
    public   void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException
    {
        this.TheCharacters.append(ch,start,length);
    } /*Method-End*/

    public   void startElement(java.lang.String name, org.xml.sax.AttributeList atts) throws org.xml.sax.SAXException
    {
        this.TheCharacters.delete(0,this.TheCharacters.length());
    } /*Method-End*/

    public   void startDocument() throws org.xml.sax.SAXException
    {
        this.freeResources();
    } /*Method-End*/

    public   void fatalError(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException
    {
        this.commonErrorProcessor(exception);
    } /*Method-End*/

    public   void warning(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException
    {
        
    } /*Method-End*/

    public   void error(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException
    {
        this.commonErrorProcessor(exception);
    } /*Method-End*/

    public   void setInitialObject(java.lang.Object obj)
    {
        this.InitialObject = obj;
    } /*Method-End*/

    public   void freeResources()
    {
        this.TheCharacters.delete(0,this.TheCharacters.length());
    } /*Method-End*/

    public  java.lang.String  getCharacters()
    {
        // trim escaped newline character and newline characters
        
        int lOffset = 0;
                
        while (lOffset < this.TheCharacters.length())
        {
            if (lOffset + 2 < this.TheCharacters.length() &&
                this.TheCharacters.substring(lOffset, lOffset + 2).equals("\\n"))
            {
               this.TheCharacters.delete(lOffset, lOffset + 2);    
            }
            else if (this.TheCharacters.charAt(lOffset) == '\n')
            {
               this.TheCharacters.deleteCharAt(lOffset);
            }
            lOffset++;
        }
             
        return this.TheCharacters.toString();
    } /*Method-End*/

    public  int  Begin() throws org.xml.sax.SAXException {
        try {
            if (this.Parser == null) {
                org.xml.sax.Parser parser;
                SAXParserFactory factory;

                factory = SAXParserFactory.newInstance();
                factory.setValidating(false); // asi validate=false
                factory.setNamespaceAware(false);

                this.Parser = factory.newSAXParser().getParser();
                
                this.Parser.setDocumentHandler(this);
                this.Parser.setDTDHandler(this);
                this.Parser.setErrorHandler(this);
            }
        } catch (ParserConfigurationException e1) {
            SAXException classError = new SAXException(e1.getMessage());
            throw classError;
        }
        
        return 1;
    } /*Method-End*/

    public   void commonErrorProcessor(org.xml.sax.SAXParseException error) throws org.xml.sax.SAXException
    {        
        throw(error);
    } /*Method-End*/

    public  java.lang.Object  XlateObject() throws org.xml.sax.SAXException, java.io.IOException
    {
        this.Begin();
        
        this.Parser.parse(this.TheSource);
        
        return InitialObject;
    } /*Method-End*/

    public   void setSource(org.xml.sax.InputSource source)
    {
        this.TheSource = source;
    } /*Method-End*/

    public  java.lang.Object  XlateObject(java.io.InputStream stream) throws org.xml.sax.SAXException, java.io.IOException
    {
        this.Begin();
        
        InputSource is = new InputSource(stream);
        is.setSystemId("archiverNoID");
        this.setSource(is);
        
        this.Parser.parse(this.TheSource);
        
        return InitialObject;
    } /*Method-End*/
    
    public void DumpStatus()
    {
        // This method is a debug method to dump status information about this object
        
        System.out.println("Dump Status from class BaseXMLSerializer");
        System.out.println("The initial object is an instance of " + this.InitialObject.getClass().getName());
        System.out.println("The initial object state " + this.InitialObject.toString());
        System.out.println("The current stream dump is " + this.TheCharacters);
        System.out.println("Dump Status from class BaseXMLSerializer - END");
        
    }
    
    
}  // end of class
