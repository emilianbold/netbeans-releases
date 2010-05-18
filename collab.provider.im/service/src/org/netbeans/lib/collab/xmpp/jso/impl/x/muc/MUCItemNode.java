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

package org.netbeans.lib.collab.xmpp.jso.impl.x.muc;

import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;
import org.jabberstudio.jso.StreamNode;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.util.Utilities;
import net.outer_planes.jso.ElementNode;
import net.outer_planes.jso.ExtensionNode;
import org.netbeans.lib.collab.xmpp.jso.iface.x.muc.MUCItem;
import org.netbeans.lib.collab.xmpp.jso.iface.x.muc.MUCRole;
import org.netbeans.lib.collab.xmpp.jso.iface.x.muc.Affiliation;
import org.jabberstudio.jso.JID;

/**
 *
 * 
 * @author Rahul Shah
 * 
 */
public class MUCItemNode extends ElementNode implements MUCItem{
    
    //"Constants"
    public static final NSI     ATTRNAME_JID = new NSI("jid", null);
    public static final NSI     ATTRNAME_NICK = new NSI("nick", null);
    public static final NSI     ATTRNAME_AFFILIATION = new NSI("affiliation", null);
    public static final NSI     ATTRNAME_ROLE = new NSI("role", null);
    
    public static final NSI     ELEMNAME_REASON = new NSI("reason", null);
    public static final NSI     ELEMNAME_ACTOR = new NSI("actor", null);
	public static final NSI     ATTRNAME_NAME       = new NSI("jid", null);
    
    /** Creates a new instance of MUCItemNode */
    public MUCItemNode(StreamDataFactory sdf, String namespace) {
        super(sdf, new NSI(MUCItem.NAME, namespace));
    }
    
    protected MUCItemNode(StreamElement parent, MUCItemNode base) {
        super(parent, base);
    }
    
    public void setRole(org.netbeans.lib.collab.xmpp.jso.iface.x.muc.MUCRole.Type type) throws IllegalArgumentException {
        setAttributeObject(ATTRNAME_ROLE, type);
    }
    
    public org.netbeans.lib.collab.xmpp.jso.iface.x.muc.Affiliation.Type getAffiliation() {
        Object val = getAttributeObject(ATTRNAME_AFFILIATION);
        org.netbeans.lib.collab.xmpp.jso.iface.x.muc.Affiliation.Type type = null;

        if (val instanceof org.netbeans.lib.collab.xmpp.jso.iface.x.muc.Affiliation.Type) {
            type = (org.netbeans.lib.collab.xmpp.jso.iface.x.muc.Affiliation.Type)val;
        } else if (val != null) {
            String  temp = val.toString();
            if (Utilities.equateStrings(temp, Affiliation.ADMIN.toString()))
                type = Affiliation.ADMIN;
            else if (Utilities.equateStrings(temp, Affiliation.MEMBER.toString()))
                type = Affiliation.MEMBER;
            else if (Utilities.equateStrings(temp, Affiliation.NONE.toString()))
                type = Affiliation.NONE;
            else if (Utilities.equateStrings(temp, Affiliation.OUTCAST.toString()))
                type = Affiliation.OUTCAST;
            else if (Utilities.equateStrings(temp, Affiliation.OWNER.toString()))
                type = Affiliation.OWNER;

            if (type != null)
                setAttributeObject(ATTRNAME_AFFILIATION, type);
        }
        return type;
    }
    
    public JID getJID() {
        String val = getAttributeValue(ATTRNAME_JID);
        return ((val != null) ? new JID(val) : null);
    }
    
    public String getNick() {
        String val = getAttributeValue(ATTRNAME_NICK);
        return ((val != null) ? val : null);
    }
    
    public String getReason() {
        StreamElement res = getFirstElement(ELEMNAME_REASON);
        return ((res != null) ? res.normalizeTrimText() : null);
    }

    public StreamElement getActorElement() {
	return getFirstElement(ELEMNAME_ACTOR);
    }

    public JID getActor() {
	StreamElement   act = getActorElement();
	String          jid = null;
	
	if (act != null)
	    jid = act.getAttributeValue(ATTRNAME_JID);
	return ((jid != null) ? new JID(jid) : null);
    }
	
    public void setActor(JID jid) {
        StreamElement   act = getActorElement();

        if (act == null)
            act = addElement(ELEMNAME_ACTOR);

        act.setAttributeValue(ATTRNAME_JID, (jid != null) ? jid.toString() : "");
    }

    public void removeActor() {
        StreamElement   act = getActorElement();

        if (act != null)
            act.detach();
    }

    public org.netbeans.lib.collab.xmpp.jso.iface.x.muc.MUCRole.Type getRole() {
        Object val = getAttributeObject(ATTRNAME_ROLE);
        org.netbeans.lib.collab.xmpp.jso.iface.x.muc.MUCRole.Type type = null;

        if (val instanceof org.netbeans.lib.collab.xmpp.jso.iface.x.muc.MUCRole.Type) {
            type = (org.netbeans.lib.collab.xmpp.jso.iface.x.muc.MUCRole.Type)val;
        } else if (val != null) {
            String  temp = val.toString();
            if (Utilities.equateStrings(temp, MUCRole.MODERATOR.toString()))
                type = MUCRole.MODERATOR;
            else if (Utilities.equateStrings(temp, MUCRole.NONE.toString()))
                type = MUCRole.NONE;
            else if (Utilities.equateStrings(temp, MUCRole.PARTICIPANT.toString()))
                type = MUCRole.PARTICIPANT;
            else if (Utilities.equateStrings(temp, MUCRole.VISITOR.toString()))
                type = MUCRole.VISITOR;

            if (type != null)
                setAttributeObject(ATTRNAME_ROLE, type);
        }
        return type;
    }
    
    public void setAffiliation(org.netbeans.lib.collab.xmpp.jso.iface.x.muc.Affiliation.Type type) throws IllegalArgumentException {
        setAttributeObject(ATTRNAME_AFFILIATION, type);
    }
    
    public void setJID(JID jid) throws IllegalArgumentException {
        if (jid == null)
            throw new IllegalArgumentException("jid cannot be null");
        setAttributeValue(ATTRNAME_JID, jid.toString());
    }
    
    public void setNick(String nick) throws IllegalArgumentException {
        if ((nick == null) || (nick.equals("")))
            throw new IllegalArgumentException("nick cannot be null or \"\"");
        setAttributeValue(ATTRNAME_NICK, nick);
    }
    
    public void setReason(String reason) throws IllegalArgumentException {
        StreamElement res = getFirstElement(ELEMNAME_REASON);
        if (res == null)
            res = addElement(ELEMNAME_REASON);
        res.addText(reason);
    }
 
    public StreamObject copy(StreamElement parent) {
        return new MUCItemNode(parent, this);
    }
}
