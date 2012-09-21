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
                try {
                    messageTemplate = factory.newDocumentBuilder().parse(templateStream);
                } finally {
                    templateStream.close();
                }
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
