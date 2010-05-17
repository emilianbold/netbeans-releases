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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.lib.collab.xmpp.httpbind;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Mridul Muralidharan
 */
public class XMLParserHelper {
    
    private static DocumentBuilderFactory _domFactory;
    private static final String UTF_8 = "UTF-8"; // NOI18N
    private static final String BODY_TAG = "body"; // NOI18N
    
    private static final String SID_ATTRIBUTE = "sid"; // NOI18N
    private static final String TO_ATTRIBUTE = "to"; // NOI18N
    private static final String RID_ATTRIBUTE = "rid"; // NOI18N
    
    private static final String JAXP_SCHEMA_LANGUAGE =
            "http://java.sun.com/xml/jaxp/properties/schemaLanguage"; // NOI18N

    private static final String W3C_XML_SCHEMA =
            "http://www.w3.org/2001/XMLSchema"; // NOI18N

    static {
        _domFactory = DocumentBuilderFactory.newInstance();
        _domFactory.setValidating(false);
        _domFactory.setNamespaceAware(false);
        _domFactory.setIgnoringElementContentWhitespace(true);
        _domFactory.setIgnoringComments(true);
        _domFactory.setCoalescing(true);
    }
    
    public static org.w3c.dom.Element parseXMLInput(String input){

        org.w3c.dom.Element xmlDoc = null;
        
        try{
            StringReader reader = new StringReader(input);
            InputSource inputSource = new InputSource(reader);

            DocumentBuilder builder = _domFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(inputSource);

            xmlDoc = doc.getDocumentElement();

            // Kick this out when we move to using the schema
            if (!xmlDoc.getTagName().equals(BODY_TAG)){
                xmlDoc = null;
            }
        }
        catch(ParserConfigurationException pcEx){
            xmlDoc = null;
        }
        catch(UnsupportedEncodingException ueEx){
            xmlDoc = null;
        }
        catch(SAXException saxEx){
            xmlDoc = null;
        }
        catch(IOException ioEx){
            xmlDoc = null;
        }
        
        return xmlDoc;
    }

    public static int getIntAttribute(org.w3c.dom.Element bodyElement , String attr){
        String attrib = bodyElement.getAttribute(attr);
        int retval = -1;

        if (null != attrib && 0 != attrib.trim().length()){
            try{
                retval = Integer.parseInt(attrib);
            }
            catch(NumberFormatException nfEx){
                retval = -1;
            }
        }

        return retval;
    }

    public static long getLongAttribute(org.w3c.dom.Element bodyElement , String attr){
        String attrib = bodyElement.getAttribute(attr);
        long retval = -1L;

        if (null != attrib && 0 != attrib.trim().length()){
            try{
                retval = Long.parseLong(attrib);
            }
            catch(NumberFormatException nfEx){
                retval = -1L;
            }
        }
        
        return retval;
    }

    public static String getStringAttribute(org.w3c.dom.Element bodyElement , String attr){
        return bodyElement.getAttribute(attr);
    }
    
    private static Transformer xslTransformer = null;
    
    protected static Transformer initialiseTransformer() throws 
            TransformerConfigurationException{

        if (null == xslTransformer){
            xslTransformer = TransformerFactory.newInstance().newTransformer();
            xslTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // NOI18N
        }
        return xslTransformer;
    }
    
    public static String nodeToString(Node node){
        
        try{
            Transformer transformer = initialiseTransformer();
            DOMSource src = new DOMSource(node);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamResult res = new StreamResult(baos);
            transformer.transform(src , res);
            return new String(baos.toByteArray());
        }
        // Remove the debug statements below
        catch(TransformerConfigurationException tcEx){
            tcEx.printStackTrace();
        }catch(TransformerException tEx){
            tEx.printStackTrace();
        }catch(Exception ex){
            ex.printStackTrace();
            System.err.println("node : " + node); // NOI18N
            System.err.println("nodeToString for NodeName : " + node.getNodeName()); // NOI18N
        }
        
        return null;
    }
}
