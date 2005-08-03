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

import org.w3c.dom.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;


/**
 *
 *
 * @author Todd Fast, todd.fast@sun.com
 */
public abstract class MOXCCollablet extends Object implements Collablet {
    ////////////////////////////////////////////////////////////////////////////
    // Instance members
    ////////////////////////////////////////////////////////////////////////////
    private Conversation conversation;
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private Document messageTemplate;

    /**
     *
     *
     */
    public MOXCCollablet() {
        super();
    }

    /**
     *
     *
     */
    public MOXCCollablet(Conversation conversation) {
        super();
        this.conversation = conversation;
    }

    /**
     *
     *
     */
    public Conversation getConversation() {
        return conversation;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Collablet methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public final boolean acceptMessage(CollabMessage message) {
        return true;
    }

    /**
     * Tries to determine if the message is a MOXC message
     *
     */
    public boolean handleMessage(CollabMessage message)
    throws CollabException {
        try {
            MOXCMessage moxcMessage = new MOXCMessage(this, message);

            if (moxcMessage.isSelfOrigin()) {
                return true;
            }

            // See if namespace matches
            if (Arrays.asList(getNamespaces()).contains(moxcMessage.getMessageNamespace())) {
                return handleMOXCMessage(moxcMessage);
            } else {
                return false;
            }
        } catch (InvalidMessageException e) {
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // MOXC-specific methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the message namespace(s) this channel understands
     *
     */
    public abstract String[] getNamespaces();

    /**
     * Called when a namespace match has been determined and the channel should
     * handle this message.  This method will only be called if the message
     * matches one of the namespaces declared by this channel.
     *
     */
    public abstract boolean handleMOXCMessage(MOXCMessage message)
    throws CollabException;

    ////////////////////////////////////////////////////////////////////////////
    // Message helper methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void sendMOXCMessage(Element element) throws CollabException {
        Document doc = createMOXCMessage(element);

        StringWriter writer = new StringWriter();

        try {
            TransformerFactory xformFactory = TransformerFactory.newInstance();
            Transformer transform = xformFactory.newTransformer();
            Source input = new DOMSource(doc);

            Result output = new StreamResult(writer);
            transform.transform(input, output);
        } catch (Exception e) {
            throw new CollabException(e, "Could not serialize XML document");
        }

        String message = writer.toString();

        CollabMessage collabMessage = getConversation().createMessage();
        collabMessage.setContent(message);

        //Debug.out.println("Sending:\n"+message);
        // Set a header to ensure the chat channel doesn't pick this up
        collabMessage.setHeader("x-channel", element.getNamespaceURI()); // NOI18N

        getConversation().sendMessage(collabMessage);
    }

    /**
     *
     *
     */
    private Document createMOXCMessage(Element element)
    throws CollabException {
        Document doc = createMOXCEnvelope(element.getNamespaceURI());

        NodeList bodyNodes = doc.getElementsByTagNameNS(MOXCConstants.SOAP_URI, "Body"); // NOI18N

        if ((bodyNodes == null) || (bodyNodes.getLength() == 0)) {
            throw new InvalidMessageException(
                "The message envelope did not contain the required " + // NOI18N
                "SOAP Body element"
            ); // NOI18N
        }

        Element body = (Element) bodyNodes.item(0);

        // Append the elements into the Body element
        body.appendChild(doc.importNode(element, true));

        return doc;
    }

    /**
     *
     *
     */
    private Document createMOXCEnvelope(String namespace)
    throws CollabException {
        Document doc = (Document) getMessageTemplate().cloneNode(true);

        setElementContent(
            doc, MOXCConstants.MOXC_URI, "sender",
            getConversation().getCollabSession().getUserPrincipal().getIdentifier()
        );
        setElementContent(doc, MOXCConstants.MOXC_URI, "conversation", getConversation().getIdentifier());
        setElementContent(doc, MOXCConstants.MOXC_URI, "channel", namespace);
        setElementContent(doc, MOXCConstants.MOXC_URI, "instance", "*");

        return doc;
    }

    /**
     *
     *
     */
    private void setElementContent(Document doc, String namespace, String localName, String value)
    throws CollabException {
        NodeList nodes = doc.getElementsByTagNameNS(namespace, localName);

        if ((nodes == null) || (nodes.getLength() == 0)) {
            throw new InvalidMessageException(
                "The document did not contain the required " + // NOI18N
                "element {" + namespace + "}" + localName
            ); // NOI18N
        }

        Element element = (Element) nodes.item(0);
        element.setNodeValue(value);
    }

    /**
     *
     *
     */
    private Document getMessageTemplate() throws CollabException {
        if (messageTemplate == null) {
            try {
                InputStream templateStream = getClass().getResourceAsStream("/com/sun/collablet/moxc/message.xml");
                assert templateStream != null : "Couldn't load message template";

                if (templateStream == null) {
                    throw new IOException("Couldn't load message template: " + "inputStream = " + templateStream);
                }

                // TODO: Cache the builder & use buffered stream
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                messageTemplate = factory.newDocumentBuilder().parse(templateStream);
            } catch (Exception e) {
                throw new CollabException(e, "Could not initialize MOXC message template");
            }
        }

        return messageTemplate;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Property change support
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected PropertyChangeSupport getChangeSupport() {
        return changeSupport;
    }

    /**
     *
     *
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().addPropertyChangeListener(listener);
    }

    /**
     *
     *
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().removePropertyChangeListener(listener);
    }
}
