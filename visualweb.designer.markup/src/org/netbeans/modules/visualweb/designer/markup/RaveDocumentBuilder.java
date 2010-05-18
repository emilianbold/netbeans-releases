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
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000-2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Sun Microsystems, Inc.,
 * http://www.sun.com.  For more information on the Apache Software
 * Foundation, please see <http://www.apache.org/>.
 */

// BEGIN RAVE MODIFICATIONS
package org.netbeans.modules.visualweb.designer.markup;
//package org.apache.xerces.jaxp;
import org.apache.xerces.jaxp.*;
// END RAVE MODIFICATIONS

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.dom.DOMMessageFormatter;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Shamelessly COPIED from org.apache.xerces.jaxp.DocumentBuilderImpl . I did that because
 * I need to access the domParser field and change its document class name property,
 * and couldn't find a way to do that with the standard provided DocumentBuilderImpl
 * (Couldn't use reflection to break into the package private getDOMParser field, and
 * it also doesn't have an accessible constructor I could use to subclass it.)
 * 
 * @author Rajiv Mordani
 * @author Edwin Goei
 */
public class RaveDocumentBuilder extends DocumentBuilder
        implements JAXPConstants
{
    private EntityResolver er = null;
    private ErrorHandler eh = null;
    private DOMParser domParser = null;

    public RaveDocumentBuilder(DocumentBuilderFactory dbf, Hashtable dbfAttrs, boolean sourceDocument)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
// BEGIN RAVE MODIFICATINS
        //domParser = new DOMParser();
        domParser = new RaveDomParser(sourceDocument);
// END RAVE MODIFICATINS

// BEGIN RAVE MODIFICATINS
        /* I don't care about validation (couldn't just leave the check in since
           DefaultValidationErrorHandler is package private and don't feel like copying it)

        // If validating, provide a default ErrorHandler that prints
        // validation errors with a warning telling the user to set an
        // ErrorHandler
        if (dbf.isValidating()) {
            setErrorHandler(new DefaultValidationErrorHandler());
        }
        */
// END RAVE MODIFICATIONS

        domParser.setFeature(Constants.SAX_FEATURE_PREFIX +
                             Constants.VALIDATION_FEATURE, dbf.isValidating());

        // "namespaceAware" == SAX Namespaces feature
        domParser.setFeature(Constants.SAX_FEATURE_PREFIX +
                             Constants.NAMESPACES_FEATURE,
                             dbf.isNamespaceAware());

        // Set various parameters obtained from DocumentBuilderFactory
        domParser.setFeature(Constants.XERCES_FEATURE_PREFIX +
                             Constants.INCLUDE_IGNORABLE_WHITESPACE,
                             !dbf.isIgnoringElementContentWhitespace());
        domParser.setFeature(Constants.XERCES_FEATURE_PREFIX +
                             Constants.CREATE_ENTITY_REF_NODES_FEATURE,
                             !dbf.isExpandEntityReferences());
        domParser.setFeature(Constants.XERCES_FEATURE_PREFIX +
                             Constants.INCLUDE_COMMENTS_FEATURE,
                             !dbf.isIgnoringComments());
        domParser.setFeature(Constants.XERCES_FEATURE_PREFIX +
                             Constants.CREATE_CDATA_NODES_FEATURE,
                             !dbf.isCoalescing());

        setDocumentBuilderFactoryAttributes(dbfAttrs);
    }

    /**
     * Set any DocumentBuilderFactory attributes of our underlying DOMParser
     *
     * Note: code does not handle possible conflicts between DOMParser
     * attribute names and JAXP specific attribute names,
     * eg. DocumentBuilderFactory.setValidating()
     */
    private void setDocumentBuilderFactoryAttributes(Hashtable dbfAttrs)
        throws SAXNotSupportedException, SAXNotRecognizedException
    {
        if (dbfAttrs == null) {
            // Nothing to do
            return;
        }

        for (Enumeration e = dbfAttrs.keys(); e.hasMoreElements();) {
            String name = (String)e.nextElement();
            Object val = dbfAttrs.get(name);
            if (val instanceof Boolean) {
                // Assume feature
                domParser.setFeature(name, ((Boolean)val).booleanValue());
            } else {
                // Assume property
                if (JAXP_SCHEMA_LANGUAGE.equals(name)) {
                    // JAXP 1.2 support
                    //None of the properties will take effect till the setValidating(true) has been called                                        
                    if ( W3C_XML_SCHEMA.equals(val) ) {
                        if( isValidating() ) {
                            domParser.setFeature(
                                Constants.XERCES_FEATURE_PREFIX +
                                Constants.SCHEMA_VALIDATION_FEATURE, true);
                            // this should allow us not to emit DTD errors, as expected by the 
                            // spec when schema validation is enabled
                            domParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
                        }
                    }
        		} else if(JAXP_SCHEMA_SOURCE.equals(name)){
               		if( isValidating() ) {
						String value=(String)dbfAttrs.get(JAXP_SCHEMA_LANGUAGE);
						if(value !=null && W3C_XML_SCHEMA.equals(value)){
            				domParser.setProperty(name, val);
						}else{
                            throw new IllegalArgumentException(
                                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, 
                                "jaxp-order-not-supported",
                                new Object[] {JAXP_SCHEMA_LANGUAGE, JAXP_SCHEMA_SOURCE}));
						}
					}
            	} else {
                    // Let Xerces code handle the property
                    domParser.setProperty(name, val);
				}
			}
		}
	}

    /**
     * Non-preferred: use the getDOMImplementation() method instead of this
     * one to get a DOM Level 2 DOMImplementation object and then use DOM
     * Level 2 methods to create a DOM Document object.
     */
    public Document newDocument() {
        return new org.apache.xerces.dom.DocumentImpl();
    }

    public DOMImplementation getDOMImplementation() {
        return DOMImplementationImpl.getDOMImplementation();
    }

    public Document parse(InputSource is) throws SAXException, IOException {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        boolean reset = false;
        try {
            // XXX #6472104 ClassLoader may be set to the project's ClassLoader earlier in the
            // call stack when this is being called.
            // XXX Using this module classloader as context for 'better performance',
            // but to be kosher, the system classloader should be there (the one retrieved from global lookup).
            ClassLoader thisClassLoader = this.getClass().getClassLoader();
            if (oldContextClassLoader != thisClassLoader) {
                Thread.currentThread().setContextClassLoader(thisClassLoader);
                reset = true;
            }

            if (is == null) {
                throw new IllegalArgumentException(
                    DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, 
                    "jaxp-null-input-source", null));
            }

            if (er != null) {
                domParser.setEntityResolver(er);
            }

            if (eh != null) {
                domParser.setErrorHandler(eh);      
            }

            domParser.parse(is);
            return domParser.getDocument();
        } finally {
            if (reset) {
                Thread.currentThread().setContextClassLoader(oldContextClassLoader);
            }
        }
    }

    public boolean isNamespaceAware() {
        try {
            return domParser.getFeature(Constants.SAX_FEATURE_PREFIX +
                                        Constants.NAMESPACES_FEATURE);
        } catch (SAXException x) {
            throw new IllegalStateException(x.getMessage());
        }
    }

    public boolean isValidating() {
        try {
            return domParser.getFeature(Constants.SAX_FEATURE_PREFIX +
                                        Constants.VALIDATION_FEATURE);
        } catch (SAXException x) {
            throw new IllegalStateException(x.getMessage());
        }
    }

    public void setEntityResolver(org.xml.sax.EntityResolver er) {
        this.er = er;
    }

    public void setErrorHandler(org.xml.sax.ErrorHandler eh) {
        // If app passes in a ErrorHandler of null, then ignore all errors
        // and warnings
        this.eh = (eh == null) ? new DefaultHandler() : eh;
    }

    // package private
    DOMParser getDOMParser() {
        return domParser;
    }
}
