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

package org.netbeans.lib.collab.xmpp;

import java.text.DateFormat;
import java.util.*;
import java.io.*;

import org.netbeans.lib.collab.*;
import org.netbeans.lib.collab.xmpp.*;
import org.netbeans.lib.collab.xmpp.jso.iface.x.event.MessageEventExtension;
import org.netbeans.lib.collab.util.*;
import java.util.regex.*;

// imports from the jso library
import org.jabberstudio.jso.*;
import org.jabberstudio.jso.io.XMLImporter;
import org.jabberstudio.jso.x.xdata.*;
import org.jabberstudio.jso.x.info.DelayedExtension;
import org.netbeans.lib.collab.xmpp.jso.iface.x.muc.*;
import org.netbeans.lib.collab.xmpp.jso.iface.x.amp.*;
import org.jabberstudio.jso.format.DateTimeProfileFormat;





/**
 * An XMPP message has only one part.  multiple <body/> elements
 * may only be used to send the message stanza in multiple languages
 * While this API allows for multipart messages, i.e messages with
 * attachments, file transfer is handled using the OOB mechanism in
 * XMPP.
 *
 * An XMPP message contains by definition only text.  We use it to
 * provide regular human text as well as structured xml-ized
 * content.
 *
 * 
 */
public class XMPPMessage extends XMPPMessagePart implements InviteMessage, ReadOnlyMessage {
    
    Set _recipients = new HashSet();
    // HashMap _headers = new HashMap();
    
    String _contentType;
    String _originator;
    //This is required only for InviteMessage in MUC rooms
    private JID _associatedConferenceJID;
    private static DateTimeProfileFormat _dateFormat 
                                         = DateTimeProfileFormat.getInstance(
                                                         DateTimeProfileFormat.DATETIME);

    
   
    // global marks used to avoid duplicate message IDs
    static long lastTime = 0;
    static int lastTimeIndex = 0;
    static Object timeLock = new Object();
    
    public static final String CONTENT_TYPE = "content-type";
    public static final String CONTENT_NAME = "content-name";
    public static final String CONTENT_ENCODING = "content-encoding";
    //public static final String SIZE = "size";
    public static final String SID_LIST = "sid_list";
    public static final String SUBJECT = "subject";
    public static final String TIMESTAMP = "timestamp";
    public static final String POLL_TYPE = "application/x-iim-poll";
    public static final String POLL_REPLY_TYPE = "application/x-iim-poll-reply";
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";
    public static final String BINARY_CONTENT_TYPE = "application/octet-stream";
    public static final String XML_CONTENT_TYPE = "text/xml";
    public static final String SOAP_CONTENT_TYPE = "application/soap+xml";

    public static final String XHTML_TYPE = "text/html";
    //final static String ID_ORIGINATOR = "originator";
    public static final String XHTML_NAMESPACE = "http://jabber.org/protocol/xhtml-im";
    public static final NSI NSI_XHTML = new NSI("html", XHTML_NAMESPACE);
    public static final NSI NSI_XHTML_BODY = new NSI("body","http://www.w3.org/1999/xhtml");
    private static final NSI NSI_DELAY = new NSI("delay", "sun:xmpp:delay");
    
    XMPPSession _session;
    StreamDataFactory _sdf;

    org.jabberstudio.jso.Packet _xmppMessage;
    List _parts = new ArrayList();
    boolean isNewMsg = true;
    
    Hashtable _headers = new Hashtable();
    List _rules = new ArrayList();
    
    /** Creates a new instance of XMPPMessage */
    public XMPPMessage() {
    }
    
    /**
     * parse incoming message
     */
    public XMPPMessage(InputStream in) {
    }
    
    /**
     * XMPPSession s, the session of the user
     * JID originator - the message sender
     */
    
    public XMPPMessage(XMPPSession s, JID originator) throws CollaborationException {
        _session = s;
        _sdf = s.getDataFactory();
        // _originator = originator;
        _xmppMessage = (org.jabberstudio.jso.Message)_sdf.createPacketNode(
                                      s.MESSAGE_NAME,                                     
                                      org.jabberstudio.jso.Message.class);        
        setOriginator(originator.toString());
        String uniqueID = s.nextID("message");
        _xmppMessage.setID(uniqueID);
        _xmppMessage.setType(org.jabberstudio.jso.Message.NORMAL);
        
        // for the time being use the same string for
        // both id and thread
        ((org.jabberstudio.jso.Message)_xmppMessage).setThread(uniqueID);
        /*
          MessageEventExtension msgEvtExt = (MessageEventExtension)sdf.createExtensionNode(MessageEventExtension.NAME);
          msgEvtExt.addEvent(MessageEventExtension.OFFLINE);
          msgEvtExt.addEvent(MessageEventExtension.DELIVERED);
          msgEvtExt.addEvent(MessageEventExtension.DISPLAYED);
          msgEvtExt.addEvent(MessageEventExtension.COMPOSING);
          _xmppMessage.add(msgEvtExt);
         */
    }
    
    public XMPPMessage(XMPPSession s, String originator) throws CollaborationException {
        this(s,JID.valueOf(originator));
    }
    
    public XMPPMessage(XMPPSession s, JID recipient, JID originator) throws CollaborationException {
        this(s,originator);
        if (recipient != null) addRecipient(recipient.toString());
    }
    
    public XMPPMessage(XMPPSession s, String recipient, String originator) throws CollaborationException {
        this(s,JID.valueOf(recipient),JID.valueOf(originator));
    }
    
    public XMPPMessage(XMPPSession s, org.jabberstudio.jso.Packet m) throws CollaborationException {
        this(s.getDataFactory(), s, m);
    }

    public XMPPMessage(StreamDataFactory sdf, org.jabberstudio.jso.Packet m) throws CollaborationException 
    {
        this(sdf, null, m);
    }

    XMPPMessage(StreamDataFactory sdf, XMPPSession s, org.jabberstudio.jso.Packet m) throws CollaborationException     
    {        
        _session = s;
        _sdf = sdf;
        _xmppMessage = m;
        JID from = m.getFrom();
        JID to = m.getTo();
        if (from != null) {
            setOriginator(from.toString());
        }
        if (to != null) {
            addRecipient(m.getTo().toString());
        }
        isNewMsg = false;        
    }
    
    public XMPPMessage copy() throws CollaborationException {
        XMPPMessage m = new XMPPMessage(_sdf, _session, (Packet)_xmppMessage.copy());
        m.setHeaders((Hashtable)_headers.clone());
        m.setPartList(new ArrayList(_parts));
        m.setRecipientSet(new HashSet(_recipients));
        return m;
    }
     
    public void addPart(MessagePart messagePart) throws CollaborationException {
        if ((_parts.size() == 0) && (isNewMsg)) {
            setContent(messagePart.getContent());
            setContentType(messagePart.getContentType());
            Hashtable ht = ((XMPPMessagePart)messagePart).getContents();
            for (Enumeration e = ht.keys(); e.hasMoreElements();) {
                String type = (String)e.nextElement();
                setContent((String)ht.get(type),type);
            }
            //first part has been assigned so make isNewMsg false
            isNewMsg = false;
        } else {
            _parts.add(messagePart);
        }
        /*if (this != messagePart) 
            throw new CollaborationException("Multi-part messages not allowed");
        return;*/
    }
    
    public void addRecipient(String recipient) throws CollaborationException 
    {
        String fqid = recipient;
        if (_session != null) fqid = StringUtility.appendDomainToAddress(recipient, _session.getPrincipal().getDomainName());
        _xmppMessage.setTo(_sdf.createJID(fqid));
        
        // does xmpp support specifying multiple recipients for a message
        _recipients.add(fqid);
    }
    
    public java.util.Date getExpirationDate() {
        return null;
    }
    
    public String getHeader(String header) 
    {        
        if ((header.equalsIgnoreCase(SUBJECT)) &&
            (_xmppMessage instanceof org.jabberstudio.jso.Message)) {
            return ((org.jabberstudio.jso.Message)_xmppMessage).getSubject();
        }
        
        if (header.equalsIgnoreCase(CONTENT_TYPE)) {
            return getContentType();
        }
        
        if (header.equalsIgnoreCase(TIMESTAMP))
        {
            long timeInMill = getTime();
            String fullDateTime = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL).format(new Date(timeInMill));
            return fullDateTime;
        }
        
        //return getMessageProperty(header);
        return (String)_headers.get(header);
    }
    
    /*private String getMessageProperty(String header)
    {
        Extension x = _xmppMessage.getExtension(XMPPSession.SUN_PRIVATE_NAMESPACE);
        if (x != null) {
            for (Iterator i = x.listElements("property").iterator(); i.hasNext();) {
                StreamElement propElem = (StreamElement)i.next();
                String name = propElem.getAttributeValue("name");
                if (name.equals(header)) {
                    StreamElement valueElem = (StreamElement)propElem.getFirstElement("value");
                    if (valueElem != null) {
                        return valueElem.normalizeTrimText();
                    }
                }
            }
        }
        return null;
    }*/
    
    private boolean isXMLMessage(Packet packet)
    {
        // todo make it more generic
        for (Iterator i = _xmppMessage.listElements().iterator(); i.hasNext(); ) {
            StreamElement child = (StreamElement)i.next();
            // ignore the other children and consider the first
            // remaining element to be the content
            String ns = child.getNamespaceURI();
            if (ns != null) {
                if (ns.indexOf("http://schemas.xmlsoap.org/") > 0 ||
                    ns.indexOf("http://www.w3.org/") > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getContentType() 
    {
        if (_contentType != null) return _contentType;

        //_contentType = getMessageProperty(CONTENT_TYPE);
        _contentType = (String)_headers.get(CONTENT_TYPE);
        if (_contentType != null) return _contentType;
        if (((Packet)_xmppMessage).getExtension(XHTML_NAMESPACE) != null) {
            XMPPSessionProvider.debug("Found html namespace");
            setContentType(XHTML_TYPE);

        } else if (((Packet)_xmppMessage).getExtension(XDataForm.NAMESPACE) != null) {
            XMPPSessionProvider.debug("Found xdata namespace");
            Extension e = ((Packet)_xmppMessage).getExtension(XDataForm.NAMESPACE);
            XDataForm.Type type = ((XDataForm)e).getType();
            if (type != null && type.equals(XDataForm.SUBMIT)) {
                setContentType(POLL_REPLY_TYPE);
            } else if (type != null && type.equals(XDataForm.FORM)) {
                setContentType(POLL_TYPE);
            }

        } else if (((Packet)_xmppMessage).getExtension(XMPPSession.IBB_NAMESPACE) != null) {
            XMPPSessionProvider.debug("Found ibb namespace");
            setContentType(BINARY_CONTENT_TYPE);

        } else if (isXMLMessage((Packet)_xmppMessage)) {
            XMPPSessionProvider.debug("Found xml namespace");
            setContentType(XML_CONTENT_TYPE);

        }

        return (_contentType != null) ? _contentType : DEFAULT_CONTENT_TYPE;
    }
    
    public String getMessageId() {
        return _xmppMessage.getID();
    }
    
    public String getOriginator() {
        JID from = null;
        MUCUserQuery user = (MUCUserQuery)_xmppMessage.getExtension(MUCUserQuery.NAMESPACE);
        if (user != null) {
            Iterator itr = user.listElements().iterator();
            while (itr.hasNext()) {
                Object o = itr.next();
                if (o instanceof Invite) {
                    from = ((Invite)o).getFrom();
                    break;
                }
            }
            return from.toString();
        } else {
            JID jid = _xmppMessage.getFrom();
            return (jid != null) ? jid.toString() : null;
        }
    }
    
    public MessagePart[] getParts() {
        MessagePart[] mparts = new MessagePart[_parts.size() + 1];
        mparts[0] = this;
        for(int i = 0; i < _parts.size(); i++) {
            mparts[i + 1] = (MessagePart)_parts.get(i);
        }
        return mparts;
    }

    public String[] getRecipients() throws CollaborationException {
        if ((_recipients.size() == 0) && (_xmppMessage.getTo() != null)) {
            _recipients.add(_xmppMessage.getTo().toString());
        }
        String[] recipients = new String[_recipients.size()];
        int j = 0;
        for (Iterator i = _recipients.iterator();i.hasNext();) {
            recipients[j++] = (String)i.next();
        }
        return recipients;
    }
    
    public MessagePart newPart() throws CollaborationException {
        return new XMPPMessagePart();
    }
    
    public void removePart(MessagePart messagePart) throws CollaborationException {
        //throw new CollaborationException("Multi-part messages not allowed");
        if (_parts.get(0).equals(messagePart)) {
            throw new CollaborationException("Cannot remove first message part");
        }
        _parts.remove(messagePart);
    }
    
    public void removeRecipient(String str) throws CollaborationException {
        String fqid = str;
        if (_session != null) fqid = StringUtility.appendDomainToAddress(str, _session.getPrincipal().getDomainName());
        _recipients.remove(fqid);
    }
    
    public void sendReply(org.netbeans.lib.collab.Message message) throws CollaborationException {
        try {
            org.jabberstudio.jso.Message m = (org.jabberstudio.jso.Message)((XMPPMessage)message).getXMPPMessage();
            String threadId = ((org.jabberstudio.jso.Message)_xmppMessage).getThread();
            m.setThread(threadId);
            if (getContentType() != null &&
                (getContentType().equalsIgnoreCase(POLL_REPLY_TYPE)) ||
                (getContentType().equalsIgnoreCase(POLL_TYPE))) 
            {
                String content = message.getContent();
                m.setBody(null);
                // do it this way for now
                try {
                    PollResponse p = new PollResponse(content);
                    XDataForm xdf = p.getXDataForm();
                    m.add(xdf);
                } catch (Exception e) {
                }
            }
            _session.getConnection().send(m);
        } catch(StreamException se) {
            se.printStackTrace();
            throw new CollaborationException(se.toString());
        }
    }
    
    public void sendStatus(int msgStatus) throws CollaborationException {
        if (_xmppMessage.getType() == org.jabberstudio.jso.Message.CHAT) {
            String thread = null;
            XMPPConferenceService confService = (XMPPConferenceService)_session.getConferenceService();
            if (confService != null) {
                thread = confService.getConversationThread((org.jabberstudio.jso.Message)_xmppMessage);
            }
            XMPPConference c = _session.getConference(thread);
            if (c == null) {
                XMPPSessionProvider.debug("Conference room not found " + thread);
                return;
            }
            if (!c.sendStatus(msgStatus)) {
                _xmppMessage.add(getMessageExtension(msgStatus));
                try {
                    if (_xmppMessage.getTo() != null) _session.getConnection().send(_xmppMessage);
                } catch (StreamException se) {
                    throw new CollaborationException(se.toString());
                }
                //echo the event to self
                _xmppMessage.setTo(_xmppMessage.getFrom());
                _session.processMessage((Packet)_xmppMessage);
            }
        } else if (_xmppMessage.getType() == org.jabberstudio.jso.Message.GROUPCHAT) {
            String id = _xmppMessage.getTo().toString();
            XMPPConference c = _session.getConference(id);
            if (c == null) {
                XMPPSessionProvider.debug("Conference room not found " + id);
                return;
            }
            if (!c.sendStatus(msgStatus)) {
                _xmppMessage.add(getMessageExtension(msgStatus));
                try {
                    _session.getConnection().send(_xmppMessage);
                } catch (StreamException se) {
                    throw new CollaborationException(se.toString());
                }
            }
        } else {
            sendNormalMessageEvent(msgStatus);
        }
    }
    
    private void sendNormalMessageEvent(int msgStatus) throws CollaborationException {
        //TODO:the status should be sent to all the recipients
        String[] rcpts = getRecipients();
        String recipient = null;
        if (rcpts !=null & rcpts.length > 0) {
            recipient = rcpts[0];
        } else {
            throw new CollaborationException(this + "message does not have any recipient");
        }
        try {
            // create an xmpp message with originator and recipient swapped
            org.jabberstudio.jso.Message xmppMessage = (org.jabberstudio.jso.Message)_sdf.createPacketNode(
                                      _session.MESSAGE_NAME,                                     
                                      org.jabberstudio.jso.Message.class);            
            xmppMessage.setFrom(JID.valueOf(recipient));
            xmppMessage.setTo(JID.valueOf(getOriginator()));
            xmppMessage.setType(_xmppMessage.getType());
            xmppMessage.setThread(((org.jabberstudio.jso.Message)_xmppMessage).getThread());
            xmppMessage.add(getMessageExtension(msgStatus));
            _session.getConnection().send(xmppMessage);
        } catch (JIDFormatException jfe) {
            throw new CollaborationException(jfe.toString());
        } catch (StreamException se) {
            throw new CollaborationException(se.toString());
        }
    }
    
    private StreamElement getMessageExtension(int msgStatus) {
        MessageEventExtension x = (MessageEventExtension)_sdf.createExtensionNode(MessageEventExtension.NAME);
        MessageEventExtension.EventType type = getJabberStatus(msgStatus);
        if (type != null) x.addEvent(type);
        x.setMessageID(getMessageId());
        return (StreamElement)x;
    }
    
    public void setHeader(String header, String value) throws CollaborationException {
        if ((header == null) ||  (value == null)) return;
        if (_xmppMessage instanceof org.jabberstudio.jso.Message) {
            if (header.equalsIgnoreCase(SUBJECT)) {
                ((org.jabberstudio.jso.Message)_xmppMessage).setSubject(value);
                return;
            }
        }
        //if the content-type is text/html or poll then do not add it to the properties
        if (header.equalsIgnoreCase(CONTENT_TYPE) && 
            (value.equalsIgnoreCase(XHTML_TYPE) ||
             value.equalsIgnoreCase(POLL_TYPE) ||
             value.equalsIgnoreCase(DEFAULT_CONTENT_TYPE) ||
             value.equalsIgnoreCase(BINARY_CONTENT_TYPE) ||
             value.equalsIgnoreCase(POLL_REPLY_TYPE))) 
        {
            return;
        }
        /*Extension x = _xmppMessage.getExtension(XMPPSession.SUN_PRIVATE_NAMESPACE);
        if (x == null) {
            x = _sdf.createExtensionNode(XMPPSession.SUN_PRIVATE_NAME);
            _xmppMessage.addExtension(x);
        }
        StreamElement propElem = _sdf.createElementNode(_sdf.createNSI("property", null));
        propElem.setAttributeValue("name",header);
        propElem.addElement("value").addText(value);
        x.add(propElem);        */
        _headers.put(header, value);
    }
    
    public void setOriginator(String sender) throws CollaborationException 
    {
        _originator = sender;
        if (_session != null) _originator = StringUtility.appendDomainToAddress(sender, _session.getPrincipal().getDomainName());
        // do we require this instance variable?
        _xmppMessage.setFrom(_sdf.createJID(_originator));
    }
    
    
    // override the setContent method in the XMPPMessagePart   
    public void setContent(String content) throws CollaborationException {
        ((org.jabberstudio.jso.Message)_xmppMessage).setBody(content);
    }
    
    public void setContent(String content, String contentType) throws CollaborationException {
        if (contentType.equals(XHTML_TYPE)) {

	    StreamElement htmlElement = _xmppMessage.getFirstElement(NSI_XHTML);
            if (htmlElement == null) {
		htmlElement = _sdf.createElementNode(NSI_XHTML);
	    } else {
		htmlElement.clearElements();
	    }

	    XMLImporter importer = null;
	    if (_session != null) {
		StreamContext _outCtx = 
		    _session.getConnection().getOutboundContext();
		importer = 
		    _session.getConnection().getJSO().createXMLImporter(_outCtx);
	    } else {
		importer = XMPPSession._jso.createXMLImporter(XMPPSession._jso.createStreamContext());
	    }
            StringBuffer str = new StringBuffer();
            str.append("<body xmlns='http://www.w3.org/1999/xhtml'>");
            
            String temp = _addImageEndTag(content);
            temp = _substituteBrTag(temp,"<br[\t\n\r\u0020]*>","<br/>");
            str.append(temp);
            
            str.append("</body>");
            
            StreamElement bodyElement = importer.read(str.toString());
            htmlElement.add(bodyElement);
            /*htmlElement.add(bodyElement);
            bodyElement.addText(content);*/
            _xmppMessage.add(htmlElement);
            setContentType(contentType);

        } else if (isXMLContentType(contentType)) {
            StreamContext _outCtx = _session.getConnection().getOutboundContext();
            XMLImporter importer = _session.getConnection().getJSO().createXMLImporter(_outCtx);
            StreamElement xmlElement = importer.read(content);
            _xmppMessage.add(xmlElement);
            setContentType(contentType);
            
        } else {
            ((org.jabberstudio.jso.Message)_xmppMessage).setBody(content);

        }
    }
    
    public void setContent(java.io.InputStream is, String enc) 
                throws CollaborationException 
    {
        try {
            setHeader(XMPPMessage.CONTENT_ENCODING,enc);
            InputStreamReader isr = new InputStreamReader(is, enc);
            StringBuffer buf = new StringBuffer();
            char[] c = new char[1024];
            while (isr.ready()) {
                int len = isr.read(c, 0, c.length);
                buf.append(c, 0, len);
            }
            ((org.jabberstudio.jso.Message)_xmppMessage).setBody(buf.toString());
            isr.close();
        } catch(Exception e) {
            throw new CollaborationException(e.toString());
        }
    }
    
    public ReadOnlyMessagePart[] getReadOnlyParts() {
        ReadOnlyMessagePart[] mparts = new MessagePart[_parts.size() + 1];
        mparts[0] = this;
        for(int i = 0; i < _parts.size(); i++) {
            mparts[i + 1] = (ReadOnlyMessagePart)_parts.get(i);
        }
        return mparts;
    }
    
    // Override methods of MessagePart
    public void setContentType(String contentType) {
        _contentType = contentType;
        try {
            setHeader(CONTENT_TYPE,contentType);
        } catch(CollaborationException e) {
            e.printStackTrace();
        }
    }
        

    private boolean isXMLContentType(String ct)
    {
        ct = ct.toLowerCase();
        return (ct.equals(XML_CONTENT_TYPE) ||
                ct.indexOf("/") < ct.indexOf("+xml"));
    }

    public String getContent() throws CollaborationException
    {
        String type = getContentType();
        if (type != null &&
            (type.equalsIgnoreCase(POLL_TYPE) ||
             type.equalsIgnoreCase(POLL_REPLY_TYPE))) {
            Extension ext = 
                ((org.jabberstudio.jso.Message)_xmppMessage).getExtension(XDataForm.NAMESPACE);
            if (ext == null) {
                return ((org.jabberstudio.jso.Message)_xmppMessage).getBody();
            } else {
                return ext.toString();
            }

        } else if (type != null && isXMLContentType(type)) {
            for (Iterator i = _xmppMessage.listElements().iterator(); i.hasNext(); ) {
                StreamElement child = (StreamElement)i.next();
                // ignore the other children and consider the first
                // remaining element to be the content
                if (!child.getNamespaceURI().equals(XMPPSession.SUN_PRIVATE_NAMESPACE) &&
                    !child.getLocalName().equals("subject") &&
                    !child.getLocalName().equals("body") &&
                    !child.getLocalName().equals("error") &&
                    !child.getLocalName().equals("event") &&
                    !child.getLocalName().equals("thread")) {
                    return child.toString();
                }
            }
            
        } else {
            return ((org.jabberstudio.jso.Message)_xmppMessage).getBody();
        }

        return null;
    }
    
    public String getContent(String contentType) throws CollaborationException {       
        if (contentType != null &&
            (contentType.equalsIgnoreCase(POLL_TYPE) ||
             contentType.equalsIgnoreCase(POLL_REPLY_TYPE))) {
            Extension ext = 
                ((org.jabberstudio.jso.Message)_xmppMessage).getExtension("jabber:x:data");
            if (ext == null) {
                return ((org.jabberstudio.jso.Message)_xmppMessage).getBody();
            } else {
                return ext.toString();
            }
        } else if (contentType.equals(XHTML_TYPE)) {
            StreamElement htmlElement = _xmppMessage.getFirstElement(NSI_XHTML);
            if (htmlElement != null) {
                StreamElement bodyElement = htmlElement.getFirstElement(NSI_XHTML_BODY);
                if (bodyElement != null) {
                    List list = bodyElement.listElements();
                    String temp =  _removeImageEndTag(bodyElement.toString());
                    temp = _substituteBrTag(temp,"<br[\t\n\r\u0020]*/>","<br>");
                    return _substituteBrTag(temp,"</br[\t\n\r\u0020]*>","");
                }
            }
        } 
        return null;        
    }
    
    public org.jabberstudio.jso.Packet getXMPPMessage() {
        return _xmppMessage;
    }
    
    public java.io.InputStream getInputStream() throws CollaborationException {
        String content = getContent();        
        if (content != null) 
            return new java.io.ByteArrayInputStream(content.getBytes());
        return null;
    }

    private MessageEventExtension.EventType getJabberStatus(int icapiStatus) {
        switch(icapiStatus) {
            case MessageStatus.READ:
                return MessageEventExtension.DISPLAYED;
            case MessageStatus.RECEIVED:
                return MessageEventExtension.DELIVERED;
            case MessageStatus.TYPING_ON:
                return MessageEventExtension.COMPOSING;
            default:
                return null;
                // case MessageStatus.REFUSED:
                // case MessageStatus.DELAYED:
                // case MessageStatus.FORWARDED:
        }
        
    }

    public void rsvp(boolean param) throws CollaborationException {
        rsvp(null,param);
    }
    
    public void rsvp(org.netbeans.lib.collab.Message message, boolean param) 
                throws CollaborationException 
    {
        try {
            if (_xmppMessage.getType() == org.jabberstudio.jso.Message.CHAT) {                
                String thread = null;
                XMPPConferenceService confService = (XMPPConferenceService)_session.getConferenceService();
                if (confService != null) {
                    thread = confService.getConversationThread((org.jabberstudio.jso.Message)_xmppMessage);
                }
                /*
                String thread = ((org.jabberstudio.jso.Message)_xmppMessage).getThread();
                if ((thread == null) || (thread.equals(""))) {
                    thread = _xmppMessage.getFrom().getNode();
                }
                 */
                XMPPConference c = _session.getConference(thread);
                c.sendChatInviteReply(message, param);
                return;
            }
            org.jabberstudio.jso.Message m = (org.jabberstudio.jso.Message)_sdf.createPacketNode(
                                      _session.MESSAGE_NAME,                                     
                                      org.jabberstudio.jso.Message.class);
            
            JID from = null;
            MUCUserQuery user = (MUCUserQuery)_xmppMessage.getExtension(MUCUserQuery.NAMESPACE);
            if (user == null) return;
            Iterator itr = user.listElements().iterator();
            while (itr.hasNext()) {
                Object o = itr.next();
                if (o instanceof Invite) {
                    XMPPSessionProvider.debug("Found invite");
                    from = ((Invite)o).getFrom();
                    break;
                }
            }
            
            m.setFrom(_xmppMessage.getTo());
            m.setTo(_associatedConferenceJID);
            user = (MUCUserQuery)_sdf.createElementNode(MUCUserQuery.NAME,
                                                        MUCUserQuery.class);
            
            String reason= null;
            if (message != null) {
                MessagePart[] parts = message.getParts();
                if (parts != null) {
                    reason = parts[0].getContent();
                }
            }
            if(param) {                
                Invite invite = (org.netbeans.lib.collab.xmpp.jso.iface.x.muc.Invite)_sdf.createElementNode(
                                                            Invite.NAME, Invite.class);
                if(reason != null) invite.setReason(reason);
                invite.setTo(from);
                user.add(invite);
            } else {                
                Decline decline = (org.netbeans.lib.collab.xmpp.jso.iface.x.muc.Decline)_sdf.createElementNode(
                                                            Decline.NAME, Decline.class);
                if(reason != null) decline.setReason(reason);
                decline.setTo(new JID(getOriginator()));
                user.add(decline);
            }
            m.add(user);
            _session.getConnection().send(m);
        } catch(StreamException e) {
            e.printStackTrace();
        }
    }
    
    protected void setHeaders(Hashtable ht) {
        if (ht == null) _headers = new Hashtable();
        else _headers = ht;
    }
    
    protected Hashtable getHeaders() {
        return _headers;
    }
    
    protected static String getUniqueMessageID() {
        long t = System.currentTimeMillis();
        int index = 0;
        synchronized(timeLock) {
            if (lastTime == t) {
                index = ++lastTimeIndex;
            } else {
                lastTime = t;
                lastTimeIndex = 0;
            }
        }
        String msgid = "iim." + XMPPSessionProvider.processId + "." + Long.toString(t) + "." + index;
        return msgid;
    }
    
    private String _addImageEndTag(String content) {
        Pattern p = Pattern.compile("(<img[\t\n\r\u0020][^>]*)>",Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);
        StringBuffer sb = new StringBuffer();
        int prev = 0;
        while (m.find()) {
            sb.append(content.substring(prev,m.start()));
            sb.append(m.group(1) + "/>");
            prev = m.end();
        }
        sb.append(content.substring(prev));
        return sb.toString();
    }
    
    private String _substituteBrTag(String content, String pattern, 
                                                        String replacement) {
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);
        StringBuffer out = new StringBuffer();
        int prev = 0;
        while(m.find()) {
            out.append(content.substring(prev,m.start()));
            out.append(replacement);
            prev = m.end();
        }
        out.append(content.substring(prev));
        return out.toString();
    }
    
    private String _removeImageEndTag(String content) {
        Pattern p = Pattern.compile(
                            "(<img[\t\n\r\u0020][^>]*)(?<=/)>",Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);
        StringBuffer ret = new StringBuffer();
        int prev = 0;
        while (m.find()) {
            ret.append(content.substring(prev,m.start()));
            ret.append(m.group(1));
            ret.setCharAt(ret.length() - 1,'>');
            prev = m.end();
        }
        ret.append(content.substring(prev));
        p = Pattern.compile("</img[\t\n\r\u0020]*>",Pattern.CASE_INSENSITIVE);
        content = ret.toString();
        m = p.matcher(content);
        ret.delete(0,ret.length());
        prev = 0;
        while (m.find()) {
            ret.append(content.substring(prev,m.start()));
            prev = m.end();
        }
        ret.append(content.substring(prev));
        return ret.toString();
    }
    
    public void setRecipientSet(Set s) {
        if (s!= null) _recipients = s;
    }

    public void setPartList(List l) {
        if (l != null) _parts = l;
    }
    
    public void setAssociatedConferenceJID(JID jid) {
        _associatedConferenceJID = jid;
    }


     public void setExpirationDate(Date expireAt) throws CollaborationException {
        if (!(_session.ampSupported && _session.ampCondExpireAtSupported
              && _session.ampActionDropSupported)) {
                 throw new ServiceUnavailableException("Advanced Message processing is not supported" +
                             "or Expireat condition or drop action is not supported");
        }
        StreamDataFactory sdf = _session.getDataFactory();
        Extension amp = sdf.createExtensionNode(new NSI("amp", AMPExtension.NAMESPACE));
        StreamElement rule = sdf.createElementNode(new NSI("rule", null));
        rule.setAttributeValue("condition", "expire-at");
        rule.setAttributeValue("value",
                                 _dateFormat.format(expireAt));
        amp.add(rule);
        _xmppMessage.add(amp);       
     }


    public String toString()
    {
        return _xmppMessage.toString();
    }

    public MessageProcessingRule addProcessingRule(MessageProcessingRule.Condition condition, MessageProcessingRule.Action action)
        throws ServiceUnavailableException
    {
        return addProcessingRule(-1, condition, action);
    }

    public MessageProcessingRule addProcessingRule(int index, MessageProcessingRule.Condition condition, MessageProcessingRule.Action action)
        throws ServiceUnavailableException, IndexOutOfBoundsException
    {
        if (condition == null || action == null) {
            throw new IllegalArgumentException("Condition or Action cannot be null");
        }
        if (!_session.ampSupported) {
            throw new ServiceUnavailableException("Message processing is not supported by the server");
        }
        MessageProcessingRule mpr = new XMPPMessageProcessingRule(new MessageProcessingRule.Condition[]{condition}, action);
        if (index >= 0) _rules.add(index, mpr);
        else _rules.add(mpr);
        return mpr;
    }

    public boolean removeProcessingRule(MessageProcessingRule rule)
    {
        return _rules.remove(rule);
    }

    public MessageProcessingRule removeProcessingRule(int index)
        throws IndexOutOfBoundsException
    {
        return (MessageProcessingRule)_rules.remove(index);
    }

    public java.util.Iterator processingRulesIterator() {
       
        final Iterator rulesIter = _rules.iterator();

        return new Iterator() {
            public Object next() throws NoSuchElementException {
                return rulesIter.next();
            }
            public boolean hasNext() {
                return rulesIter.hasNext();
            }
            public void remove() throws UnsupportedOperationException {
                throw new UnsupportedOperationException("Use Message.removeProcessingRule to remove rules.");
            }
        };
    }

    static AMPRule.Action getJSOAction(MessageProcessingRule.Action action)
    {
        AMPRule.Action a = null;
        if (MessageProcessingRule.DROP.equals(action)) a = AMPRule.DROP;
        else if (MessageProcessingRule.DEFER.equals(action)) a = AMPRule.DEFER;
        else if (MessageProcessingRule.NOTIFY.equals(action)) a = AMPRule.NOTIFY;
        else if (MessageProcessingRule.ERROR.equals(action)) a = AMPRule.ERROR;
        else if (MessageProcessingRule.ALERT.equals(action)) a = AMPRule.ALERT;
        return a;
    }
    static AMPRule.Disposition getJSODisposition(MessageProcessingRule.Condition disp)
    {
        AMPRule.Disposition a = null;
        if (MessageProcessingRule.DIRECT.equals(disp)) a = AMPRule.DIRECT;
        else if (MessageProcessingRule.STORED.equals(disp)) a = AMPRule.STORED;
        else if (MessageProcessingRule.FORWARD.equals(disp)) a = AMPRule.FORWARD;
        else if (MessageProcessingRule.GATEWAY.equals(disp)) a = AMPRule.GATEWAY;
        else if (MessageProcessingRule.NONE.equals(disp)) a = AMPRule.NONE;
        return a;
    }
    static AMPRule.ResourceMatcher getJSOResourceMatcher(MessageProcessingRule.Condition m)
    {
        AMPRule.ResourceMatcher a = AMPRule.ANY;
        if (MessageProcessingRule.EQUALS.equals(m)) a = AMPRule.EXACT;
        else if (MessageProcessingRule.NOT.equals(m)) a = AMPRule.OTHER;
        return a;
    }
    
    public void updateAMPExtension() {
        if (_rules.size() == 0) return;
        AMPExtension amp = (AMPExtension)_sdf.createExtensionNode(AMPExtension.NAME);
        for(Iterator itr = _rules.iterator(); itr.hasNext();) {
            MessageProcessingRule mpr = (MessageProcessingRule)itr.next();
            MessageProcessingRule.Condition[] c = mpr.getConditions();
            assert (c != null): "cannot add null conditions";
            MessageProcessingRule.Action action = mpr.getAction();
            //for now there will be only one condition
            MessageProcessingRule.Condition condition = c[0];
            AMPRule.Action a = getJSOAction(action);
            AMPRule rule  = null;
            if (condition instanceof MessageProcessingRule.ExpirationCondition) {
                rule = amp.addRule(a, ((MessageProcessingRule.ExpirationCondition)condition).getDate());
            } else if (condition instanceof MessageProcessingRule.DispositionCondition) {
                rule = amp.addRule(a, getJSODisposition(condition));
            } else if (condition instanceof MessageProcessingRule.SessionCondition) {
                rule = amp.addRule(a, getJSOResourceMatcher(condition));
            }
        }
        _xmppMessage.add(amp);
    }
    
    public void populateMessageProcessingRules(AMPExtension amp) {
        _rules.clear();
        for(Iterator itr = amp.listRules().iterator(); itr.hasNext();) {
            _rules.add(new XMPPMessageProcessingRule((AMPRule)itr.next()));
        }
        _xmppMessage.setFrom(amp.getFrom());
        _xmppMessage.setTo(amp.getTo());
        amp.detach();
    }

    public long getTime() {
	StreamElement delay = _xmppMessage.getFirstElement(NSI_DELAY);
	if (delay != null) {
	    try {
	        return _dateFormat.parse(delay.getAttributeValue("stamp")).getTime();
            } catch(Exception e) {}
	}
	delay = _xmppMessage.getFirstElement(DelayedExtension.NAME);
	if (delay != null) {
	    try {
                return ((DelayedExtension)delay).getStamp().getTime();
	    } catch(Exception e) {}
	}
	return new Date().getTime();
    }
}
