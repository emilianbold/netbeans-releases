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
package com.sun.collablet.moxc;

import com.sun.collablet.*;

import org.openide.options.*;
import org.openide.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.*;

import javax.xml.parsers.*;

import org.netbeans.modules.collab.core.Debug;


/**
 * Represents a MOXC protocol message
 *
 * @author Todd Fast, todd.fast@sun.com
 */
public class MOXCMessage extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private Collablet collablet;
    private Document document;
    private Map headerElements;
    private String targetNamespace;
    private String sender;
    private String conversation;
    private String channel;
    private String instance;
    private boolean selfOrigin;
    private Element body;
    private Element messageElement;

    /**
     *
     *
     */
    public MOXCMessage(Collablet collablet, CollabMessage message)
    throws InvalidMessageException {
        super();
        this.collablet = collablet;
        parse(message);
    }

    /**
     * Extract all relevant info from the message and store it for easy access.
     *
     */
    private void parse(CollabMessage message) throws InvalidMessageException {
        try {
            // Parse the message
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            document = factory.newDocumentBuilder().parse(
                    new ByteArrayInputStream(message.getContent().getBytes("UTF-8"))
                ); // NOI18N

            // Make sure this is a SOAP message
            Element root = document.getDocumentElement();

            if (!MOXCConstants.SOAP_URI.equals(root.getNamespaceURI())) {
                throw new InvalidMessageException(
                    message,
                    "The message does not contain a SOAP " + // NOI18N
                    "1.2 Envelope: " + root.getNamespaceURI() + " != " + // NOI18N
                    MOXCConstants.SOAP_URI
                );
            }

            NodeList headerNodes = root.getElementsByTagNameNS(MOXCConstants.SOAP_URI, "Header"); // NOI18N

            if ((headerNodes == null) || (headerNodes.getLength() == 0)) {
                throw new InvalidMessageException(
                    message, "The message does not contain the required " + // NOI18N
                    "SOAP Header element"
                ); // NOI18N
            }

            Element header = (Element) headerNodes.item(0);
            NodeList messageHeaderNodes = header.getElementsByTagNameNS(MOXCConstants.MOXC_URI, "message"); // NOI18N

            if ((messageHeaderNodes == null) || (messageHeaderNodes.getLength() == 0)) {
                throw new InvalidMessageException(
                    message, "The required MOXC message element was not " + // NOI18N
                    "found in the header"
                ); // NOI18N
            }

            Map headerMap = new HashMap();

            // Get the MOXC message header info
            Element messageHeader = (Element) messageHeaderNodes.item(0);
            NodeList headerFields = messageHeader.getElementsByTagNameNS(MOXCConstants.MOXC_URI, "*"); // NOI18N

            for (int i = 0; i < headerFields.getLength(); i++) {
                Element field = (Element) headerFields.item(i);

                if (field.getLocalName().equals("channel")) // NOI18N
                 {
                    channel = getTextContent(field);
                } else if (field.getLocalName().equals("sender")) // NOI18N
                 {
                    sender = getTextContent(field);

                    // Check if message is from self
                    selfOrigin = sender.equals(
                            getCollablet().getConversation().getCollabSession().getUserPrincipal().getIdentifier()
                        );
                } else if (field.getLocalName().equals("conversation")) // NOI18N
                 {
                    conversation = getTextContent(field);
                } else if (field.getLocalName().equals("instance")) // NOI18N
                 {
                    instance = getTextContent(field);
                } else {
                    Debug.out.println(
                        "Found unknown header field: <" + // NOI18N
                        field.getLocalName() + "> = " + // NOI18N
                        getTextContent(field)
                    );
                }

                // Save the headers
                headerMap.put(field.getLocalName(), getTextContent(field));
            }

            // Store the headers in an unmodifiable form
            headerElements = Collections.unmodifiableMap(headerMap);

            // It's our message now; strip off the envelope and process the
            // contents of the body. If there is no body for some reason, 
            // just consume the message and return.
            NodeList bodyNodes = root.getElementsByTagNameNS(MOXCConstants.SOAP_URI, "Body"); // NOI18N

            if ((bodyNodes == null) || (bodyNodes.getLength() == 0)) {
                throw new InvalidMessageException(
                    message, "The message did not contain the required " + // NOI18N
                    "SOAP Body element"
                ); // NOI18N
            }

            body = (Element) bodyNodes.item(0);

            NodeList messageNodes = body.getElementsByTagName("*"); // NOI18N

            if ((messageNodes == null) || (messageNodes.getLength() == 0)) {
                throw new InvalidMessageException(
                    message, "The message did not contain a message element " + // NOI18N
                    "inside the SOAP Body element"
                ); // NOI18N
            }

            messageElement = (Element) messageNodes.item(0);

            if (messageElement == null) {
                throw new InvalidMessageException(message, "The Body element did not contain a message element");
            }

            // Store the message namespace for easy matching by collablets
            targetNamespace = messageElement.getNamespaceURI();
        } catch (Exception e) {
            if (e instanceof SAXParseException) {
                throw new InvalidMessageException(e);
            }

            if (e instanceof InvalidMessageException) {
                throw (InvalidMessageException) e;
            } else {
                Debug.logDebugException("Exception handling message", // NOI18N
                    e, true
                );
                Debug.dumpMessage(message);

                throw new InvalidMessageException(message, e, "Unknown exception parsing message: " + e); // NOI18N
            }
        }
    }

    /**
     *
     *
     */
    public Collablet getCollablet() {
        return collablet;
    }

    /**
     * Returns the XML document contained in the CollabMessage content. Note,
     * although the return value of this method is mutable, changing it will
     * have no effect on the values available from other methods of this class.
     *
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Returns the SOAP Body element contained in the message document.
     * Note, although the return value of this method is mutable, changing it
     * will have no effect on the values available from other methods of this
     * class.
     *
     */
    public Element getBodyElement() {
        return body;
    }

    /**
     * Returns the channel-specific message element contained in the message
     * document.  This element is the root of an arbitrary XML document
     * instance that forms the "real" collaboration content of the message.
     * Note, although the return value of this method is mutable, changing it
     * will have no effect on the values available from other methods of this
     * class.  However, modifications may affect the parsing of the message
     * contents by a channel; we strongly advise against changing this element
     * or any of its children.
     *
     */
    public Element getMessageElement() {
        return messageElement;
    }

    /**
     * Returns the list of all MOXC-specific header elements (qualified by the
     * MOXC namespace) in an unmodifiable map.  All keys are unqualified by
     * a namespace prefix.
     *
     */
    public Map getHeaders() {
        return headerElements;
    }

    /**
     *
     *
     */
    public String getMessageNamespace() {
        return targetNamespace;
    }

    /**
     * The ID of the message sender
     *
     */
    public String getSender() {
        return sender;
    }

    /**
     * The conversation ID this message was sent to
     *
     */
    public String getConversation() {
        return conversation;
    }

    /**
     * The URI of the channel that sent this message
     *
     */
    public String getChannel() {
        return channel;
    }

    /**
     * The instance ID of the channel that sent this message
     *
     */
    public String getInstance() {
        return instance;
    }

    /**
     * Returns true if the message originated from this session
     *
     */
    public boolean isSelfOrigin() {
        return selfOrigin;
    }
    
    private static String getTextContent(Node node) {
        StringBuffer result = new StringBuffer();
        if (! node.hasChildNodes()) return "";

        NodeList list = node.getChildNodes();
        for (int i=0; i < list.getLength(); i++) {
            Node subnode = list.item(i);
            if (subnode.getNodeType() == Node.TEXT_NODE ||
                    subnode.getNodeType() == Node.CDATA_SECTION_NODE) {
                result.append(subnode.getNodeValue());
            } else if (subnode.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                result.append(getTextContent(subnode));
            }
        }
        return result.toString();
    }
}
