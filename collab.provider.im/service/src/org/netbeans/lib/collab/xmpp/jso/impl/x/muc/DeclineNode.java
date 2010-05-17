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
import net.outer_planes.jso.ElementNode;
import net.outer_planes.jso.ExtensionNode;
import org.netbeans.lib.collab.xmpp.jso.iface.x.muc.Decline;
import org.jabberstudio.jso.JID;

/**
 *
 *
 * @author Rahul Shah
 *
 */
public class DeclineNode extends ExtensionNode implements Decline {
    //"Constants"
    public static final NSI     ATTRNAME_TO = new NSI("to", null);
    public static final NSI     ATTRNAME_FROM = new NSI("from", null);
    
    public static final NSI     ELEMNAME_REASON = new NSI("reason", null);
    
    public DeclineNode(StreamDataFactory sdf) {
        super(sdf, Decline.NAME);
    }
    
    protected DeclineNode(StreamElement parent, DeclineNode base) {
        super(parent, base);
    }
    
    public void setTo(JID jid) throws IllegalArgumentException {
        if (jid == null)
            throw new IllegalArgumentException("jid cannot be null");
        setAttributeValue(ATTRNAME_TO, jid.toString());
    }
    
    public JID getFrom() {
        String val = getAttributeValue(ATTRNAME_FROM);
        return ((val != null) ? new JID(val) : null);
    }
    
    public String getReason() {
        StreamElement res = getFirstElement(ELEMNAME_REASON);
        return ((res != null) ? res.normalizeTrimText() : null);
    }
    
    public JID getTo() {
        String val = getAttributeValue(ATTRNAME_TO);
        return ((val != null) ? new JID(val) : null);
    }
    
    public void setFrom(JID jid) throws IllegalArgumentException {
        if (jid == null)
            throw new IllegalArgumentException("jid cannot be null");
        setAttributeValue(ATTRNAME_FROM, jid.toString());
    }
    
    public void setReason(String reason) throws IllegalArgumentException {
        StreamElement res = getFirstElement(ELEMNAME_REASON);
        if (res == null)
            res = addElement(ELEMNAME_REASON);
        res.addText(reason);
    }
    
    public StreamObject copy(StreamElement parent) {
        return new DeclineNode(parent, this);
    }
}
