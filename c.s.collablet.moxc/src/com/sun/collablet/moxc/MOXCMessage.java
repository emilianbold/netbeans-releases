/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                    channel = field.getTextContent();
                } else if (field.getLocalName().equals("sender")) // NOI18N
                 {
                    sender = field.getTextContent();

                    // Check if message is from self
                    selfOrigin = sender.equals(
                            getCollablet().getConversation().getCollabSession().getUserPrincipal().getIdentifier()
                        );
                } else if (field.getLocalName().equals("conversation")) // NOI18N
                 {
                    conversation = field.getTextContent();
                } else if (field.getLocalName().equals("instance")) // NOI18N
                 {
                    instance = field.getTextContent();
                } else {
                    Debug.out.println(
                        "Found unknown header field: <" + // NOI18N
                        field.getLocalName() + "> = " + // NOI18N
                        field.getTextContent()
                    );
                }

                // Save the headers
                headerMap.put(field.getLocalName(), field.getTextContent());
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
}
