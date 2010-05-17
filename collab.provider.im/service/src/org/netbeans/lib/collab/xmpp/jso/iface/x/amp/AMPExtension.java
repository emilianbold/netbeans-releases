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
import org.jabberstudio.jso.Packet;

/**
 *
 * Interface representing an amp element in a message.
 * See JEP-0079 Advanced Message Processing
 *
 * @author Jacques Belissent
 */
public interface AMPExtension extends Extension {

    /**
     * Main AMP namespace
     */
    public static final String NAMESPACE = "http://jabber.org/protocol/amp";

    /**
     * NSI for amp elements in message packets
     */
    public static final NSI NAME = new NSI("amp", NAMESPACE);

    /**
     * AMP error namespace
     */
    public static final String NAMESPACE_ERROR = NAMESPACE + "#errors";

    /**
     * NSI for failed-rules elements in message errors
     */
    public static final NSI NAME_ERROR = new NSI("failed-rule",
						 NAMESPACE_ERROR);


    /** 
     * sets the value of the per-hop flag
     * @param b per-hop value
     */
    public void setPerHopFlag(boolean b);

    /** 
     * returns the value of the per-hop flag
     * @return per-hop value
     */
    public boolean getPerHopFlag();

    /**
     * returns the current list of rules.  (this is not a copy).
     * @return list of AMPRule objects.
     */
    public List listRules();

    /**
     * add a new rule to the amp element.  The rule is added
     * after the most recently created rule.
     * @param action action associated with the new rule
     * @return added rule.
     */
    public AMPRule addRule(AMPRule.Action action, Date expires);

    /**
     * add a new rule to the amp element.  The rule is added
     * after the most recently created rule.
     * @param action action associated with the new rule
     * @return added rule.
     */
    public AMPRule addRule(AMPRule.Action action, AMPRule.Disposition disp);

    /**
     * add a new rule to the amp element.  The rule is added
     * after the most recently created rule.
     * @param action action associated with the new rule
     * @return added rule.
     */
    public AMPRule addRule(AMPRule.Action action,
                           AMPRule.ResourceMatcher matcher);

    /** 
     * remove first rule matching the argument
     * @param rule to remove
     */
    public void removeRule(AMPRule rule);


    /**
     * Evaluate this packet given the current rule set
     * @param packet message packet to evaluate
     * @param disp what would happen to the message were it not 
     * for these rules
     * @param resource online resource the message would be delivered to
     * @param cmpDate current date (or date to compare with) or null
     * to disable the expiration check.
     * @return rule that matched the packet if any.  
     * null indicates that the ruleset did not match and that
     * normal processing should continue.
     */
    public AMPRule evaluate(Packet packet,
                            AMPRule.Disposition disp,
                            String resource,
                            Date cmpDate);


    /** 
     * return the status of this AMP extension element.  The 
     * status attribute is used in responses from the server to 
     * the client sent as a result of applying a rule.
     * @return action taken on the initial message matching this 
     * one.
     */
    public AMPRule.Action getStatus();

    /** 
     * return the from attribute value of this AMP extension element,
     * if present.  The 
     * status attribute is used in responses from the server to 
     * the client sent as a result of applying a rule.  This matches
     * the recipient  of the message initially sent and processed
     * by the server.
     * @return action taken on the initial message matching this 
     * one.
     */
    public JID getFrom();

    /** 
     * return the to attribute value of this AMP extension element,
     * if present.  The 
     * status attribute is used in responses from the server to 
     * the client sent as a result of applying a rule.
     * @return action taken on the initial message matching this 
     * one.
     */
    public JID getTo();

    /**
     * sets this rule set's to attribute
     * @param jid to attribute value
     */
    public void setTo(JID jid);

    /**
     * sets this rule set's from attribute
     * @param jid from attribute value
     */
    public void setFrom(JID jid);

    /**
     * sets this rule set's status attribute
     * @param action status attribute value
     */
    public void setStatus(AMPRule.Action action);

}
