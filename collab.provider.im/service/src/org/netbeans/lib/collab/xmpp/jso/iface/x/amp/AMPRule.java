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

import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.util.Enumerator;

/**
 * Interface for representing AMP rules
 *
 * @author Jacques Belissent
 */
public interface AMPRule extends StreamElement {

    public static final NSI NAME = new NSI("rule", AMPExtension.NAMESPACE);

    /**
     * This class defines an action that the service can take on
     * a message in transit
     */
    public static final class Action extends Enumerator {
        private Action(String name) {
            super(name);
        }
    }

    /**
     * A message disposition describes how the service plans to
     * dispose of a message.  This class allows the user to overwrite
     * the service behavior based on what the service would do.
     */
    public static final class Disposition extends Enumerator {
        private Disposition(String name) {
            super(name);
        }
    }

    /**
     * A condition type
     */
    public static final class ConditionType extends Enumerator {
        private ConditionType(String name) {
            super(name);
        }
    }

    /**
     * A recipient may access the service using multiple sessions.
     * The service decides which session to deliver the message to
     * based on session priorities.  The session filter condition
     * allows the application
     * to overwrite this behavior by asking the service to deliver
     * to a specific named session or to deliver to any but a specific
     * named session.
     */
    public static final class ResourceMatcher extends Enumerator {
        private ResourceMatcher(String name) {
            super(name);
        }
    }

    /**
     * discard the message silently
     */
    public static final Action DROP   = new Action("drop");

    /**
     * notifies the sender if the condition is matched
     * the notification is provider through the
     * MessageProcessingListener interface
     */
    public static final Action NOTIFY = new Action("notify");

    /**
     * notifies the sender if the condition is matched
     * the notification is provider through the
     * MessageProcessingListener interface
     */
    public static final Action ALERT  = new Action("alert");

    /**
     * notifies the sender if the condition is matched
     * the notification is provider through the
     * MessageProcessingListener interface
     */
    public static final Action ERROR  = new Action("error");

    /**
     * store the message for future delivery.  This action
     * should be only in combination with a expiration-based
     * DROP rule.
     */
    public static final Action DEFER  = new Action("defer");


    /**
     * message to be delivered directly to online user
     */
    public static final Disposition DIRECT   = new Disposition("direct");

    /**
     * message to be stored for future delivery
     */
    public static final Disposition STORED   = new Disposition("stored");

    /**
     * message to be forwarded to another XMPP domain
     */
    public static final Disposition FORWARD  = new Disposition("forward");

    /**
     * message to be forwarded to another messaging 
     * system.
     */
    public static final Disposition GATEWAY  = new Disposition("gateway");

    /**
     * message to be discarded
     */
    public static final Disposition NONE     = new Disposition("none");


    /**
     * deliver to any of the recipients session.  Note that if
     * the destination address specifies a session, this session will be tried 
     * first.
     */
    public static final ResourceMatcher ANY    = new ResourceMatcher("any");

    /**
     * deliver to any session except for the one specified by the
     * recipient address
     */
    public static final ResourceMatcher OTHER = new ResourceMatcher("other");

    /**
     * deliver only to session matched by the recipient address
     */
    public static final ResourceMatcher EXACT = new ResourceMatcher("exact");



    /**
     * condition based on the disposition of the message.  (what the
     * server did with it, or would do with it.
     */
    public static final ConditionType DISPOSITION  = new ConditionType("deliver");

    /**
     * Condition based on matching sender-specified recipient resource 
     * and actual recipient resource.
     */
    public static final ConditionType RESOURCE   = new ConditionType("match-resource");

    /**
     * message expiration condition
     */
    public static final ConditionType EXPIRATION    = new ConditionType("expire-at");


    /**
     * Specify a expire-at condition.
     * @param date expiration date
     */
    public void setExpirationCondition(java.util.Date date);

    /**
     * Specify a deliver condition.
     * @param disposition what would happen of the message
     */
    public void setDispositionCondition(Disposition disposition);

    /**
     * Specify a match-resource condition.
     * @param filter session/resource filter
     */
    public void setResourceCondition(ResourceMatcher filter);


    /**
     * get the type of condition associated with this rule
     * @return condition type
     */
    public ConditionType getConditionType();


    /**
     * get the condition value.  The class of the returned object
     * depends on the condition type, which can be obtained by calling
     * getConditionType
     * <li>If the rule uses an expiration condition, the returned value
     * is an instance of java.util.Date set to the expiration Date </li>
     * <li>If the rule uses a disposition condition, the returned value
     * is an instance of AMPRule.Disposition.</li>
     * <li>If the rule uses a resource condition, the returned value
     * is an instance of AMPRule.ResourceMatcher.</li>
     * @return condition value
     */
    public Object getConditionValue();

    /**
     * set action to take when condition is matched
     * @param action what to do if the condition is matched
     * 
     * @exception ServiceUnavailableException service does not 
     * support the requested action.
     */
    public void setAction(Action action);

    /**
     * set action to take when condition is matched
     * @param action what to do if the condition is matched
     * 
     * @exception ServiceUnavailableException service does not 
     * support the requested action.
     */
    public Action getAction();

}
