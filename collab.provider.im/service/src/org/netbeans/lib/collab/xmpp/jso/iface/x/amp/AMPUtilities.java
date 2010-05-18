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

package org.netbeans.lib.collab.xmpp.jso.iface.x.amp;

import java.util.List;
import java.util.Date;


import org.jabberstudio.jso.Extension;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.util.Enumerator;
import org.jabberstudio.jso.Message;
import org.jabberstudio.jso.Packet;
import org.jabberstudio.jso.PacketError;

/**
 *
 * AMP Utilities
 *
 * @author Jacques Belissent
 */
public class AMPUtilities {

    /**
     * Obtain the standard Action matching the provided String
     * @return defined AMPRule.Action object matching argument
     */
    public static AMPRule.Action getAction(String action)
    {
        AMPRule.Action a = null;

        if (AMPRule.DROP.equals(action)) { a = AMPRule.DROP; }
        else if (AMPRule.NOTIFY.equals(action)) { a = AMPRule.NOTIFY; }
        else if (AMPRule.ALERT.equals(action)) { a = AMPRule.ALERT; }
        else if (AMPRule.ERROR.equals(action)) { a = AMPRule.ERROR; }
        else if (AMPRule.DEFER.equals(action)) { a = AMPRule.DEFER; }

        return a;
    }

    /**
     * Obtain the standard Disposition matching the provided String
     * @return defined AMPRule.Disposition matching argument
     */
    public static AMPRule.Disposition getDisposition(String disp)
    {
        AMPRule.Disposition c = null;

        if (AMPRule.DIRECT.equals(disp)) c = AMPRule.DIRECT;
        else if (AMPRule.STORED.equals(disp)) c = AMPRule.STORED;
        else if (AMPRule.FORWARD.equals(disp)) c = AMPRule.FORWARD;
        else if (AMPRule.GATEWAY.equals(disp)) c = AMPRule.GATEWAY;
        else if (AMPRule.NONE.equals(disp)) c = AMPRule.NONE;

        return c;
    }

    /**
     * Obtain the standard Resource matcher matching the provided String
     * @return defined AMPRule.ResourceMatcher matching argument
     */
    public static AMPRule.ResourceMatcher getResourceMatcher(String s)
    {
        AMPRule.ResourceMatcher c = null;

        if (AMPRule.EXACT.equals(s)) c = AMPRule.EXACT;
        else if (AMPRule.OTHER.equals(s)) c = AMPRule.OTHER;
        else c = AMPRule.ANY;
 
        return c;
    }

    /**
     * modify the current packet into a notify/error/alert packet
     * to be send back the originating end point.
     *
     * @param sdf factory to create new elements
     * @param packet incoming packet
     * @param rule rule that matched.
     * @param errorMsg error text to use in the error element
     * in the case of an error action
     */
    public static void initResponse(StreamDataFactory sdf, 
                                    Packet packet,
                                    AMPRule rule,
                                    String errorMsg) 
    {

        if (AMPRule.DROP.equals(rule.getAction())) return;

        AMPExtension amp = 
            (AMPExtension)packet.getExtension(AMPExtension.NAMESPACE);
        if (amp == null) {
            amp = (AMPExtension)sdf.createExtensionNode(AMPExtension.NAME);
            packet.add(amp);
        } else {
            amp.clearElements();
        }

        amp.setStatus(AMPRule.DEFER.equals(rule.getAction())? AMPRule.NOTIFY: rule.getAction());
        amp.setFrom(packet.getTo());
        amp.setTo(packet.getFrom());
        amp.add(rule);

        if (AMPRule.ERROR.equals(rule.getAction())) {
            PacketError pe = sdf.createPacketError(PacketError.MODIFY, PacketError.UNDEFINED_CONDITION);
            Extension amperr = sdf.createExtensionNode(AMPExtension.NAME_ERROR);
            amperr.add(rule);
            if (errorMsg != null) pe.setText(errorMsg);
            pe.add(amperr);
            packet.add(pe);
            packet.setType(Packet.ERROR);
        }

    }


}
