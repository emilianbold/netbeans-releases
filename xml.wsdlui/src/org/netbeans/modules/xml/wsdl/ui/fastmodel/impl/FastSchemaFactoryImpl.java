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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Created on Mar 10, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.fastmodel.impl;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.xml.wsdl.ui.fastmodel.FastSchema;
import org.netbeans.modules.xml.wsdl.ui.fastmodel.FastSchemaFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;



/**
 * @author radval
 *
 * A factory which parses wsdl fast. 
 * Just parse some attributes from wsdl and ignore rests.
 */
public class FastSchemaFactoryImpl extends FastSchemaFactory {
    
    private boolean mParseImports = false;
    
    
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    @Override
    public FastSchema newFastSchema(InputStream in, boolean parseImports) {
        this.mParseImports = parseImports;
        FastSchema def = new FastSchemaImpl();
        try {
            SAXParserFactory fac = SAXParserFactory.newInstance();
            SAXParser parser = fac.newSAXParser();
            FastWSDLDefinitionsHandler handler = new FastWSDLDefinitionsHandler(def);
            parser.parse(in, handler);
            
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Failed to parse schema", ex);
            def.setParseErrorMessage(ex.getMessage());
        }
        
        return def;
    }
    
    @Override
    public FastSchema newFastSchema(String defFileUrl) {
        return newFastSchema(defFileUrl, false);
    }
    
    @Override
    public FastSchema newFastSchema(String defFileUrl, 
            boolean parseImports) {
        File file = new File(defFileUrl);
        return newFastSchema(file, parseImports);
    }
    
    @Override
    public FastSchema newFastSchema(File file) {
        return newFastSchema(file, false);
    }
    
    @Override
    public FastSchema newFastSchema(File file, boolean parseImports) {
        this.mParseImports = parseImports;
        FastSchema def = new FastSchemaImpl();
        
        
        
        try {
            SAXParserFactory fac = SAXParserFactory.newInstance();
            SAXParser parser = fac.newSAXParser();
            FastWSDLDefinitionsHandler handler = new FastWSDLDefinitionsHandler(def);
            parser.parse(file, handler);
            
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Failed to parse "+ file.getAbsolutePath(), ex);
            def.setParseErrorMessage(ex.getMessage());
        }
        
        return def;
    }
    
    public class FastWSDLDefinitionsHandler extends DefaultHandler {
        
        private String targetNamespace;
        
        private FastSchema mDef;
        
        public FastWSDLDefinitionsHandler(FastSchema def) {
            this.mDef = def;
        }
        
        public String getTargetNamespace() {
            return targetNamespace;
        }
        
        @Override
        public void startElement (String uri, 
                String localName,
                String qName, 
                Attributes attributes)
        throws SAXException
        {
            if(qName.endsWith("schema")) {
                for(int i = 0 ; i < attributes.getLength(); i++) {
                    String attrQName = attributes.getQName(i); 
                    if(attrQName.endsWith("targetNamespace")) {
                        targetNamespace = attributes.getValue(i);
                        mDef.setTargetNamespace(targetNamespace);
                        break;
                    }
                }
            } 
            
            
        }
        
        @Override
        public void fatalError (SAXParseException e)
        throws SAXException
        {
            
        }
        
        @Override
        public void error (SAXParseException e)
        throws SAXException
        {
            
        }
        
    }
}
