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
