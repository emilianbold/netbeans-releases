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

package org.netbeans.lib.collab;

/**
 * This class allows the application to define rules governing
 * how messages should be processed by the service.  In the
 * common case, messages are delivered to the recipient(s) if
 * online and may be stored for future delivery.  This class
 * provide the user with more control over what happens.
 * Specifically, it allows the user to define a condition based
 * on time and message disposition and associate a specific
 * action should this condition be matched by the message.
 * Actions include discarding the message, deferring it, or 
 * generating some form of notification back to the sender.
 * 
 * @since version 0.1
 * 
 */
public interface MessageProcessingRule {

    /**
     * This class defines an action that the service can take on 
     * a message in transit 
     */
    public static class Action {
        int value;
        Action(int v) { value = v; }
        public boolean equals(Object o) {
            if (o instanceof Action) {
                return ((Action)o).value == this.value;
            } else {
                return false;
            }
        }
    }

    /**
     * base condition class
     */
    public static class Condition
    {
        int value;
	int type;
        private Condition(int type, int status) { 
	    this.value = status; 
	    this.type = type;
	}
        private Condition(int type) { 
	    this.type = type;
	}
	/**
	 * tests equality with another object
	 * @return true if the provided object is a Condition of the 
	 * same type and value; false otherwise.
	 */ 
        public boolean equals(Object o) {
            if (o instanceof Condition) {
                return (((Condition)o).value == this.value &&
			((Condition)o).type == this.type);
            } else {
                return false;
            }
        }
    }

    /**
     * A message disposition describes how the service plans to
     * dispose of a message.  This class allows the user to overwrite
     * the service behavior based on what the service would do.
     */
    public static final class DispositionCondition extends Condition {
        private DispositionCondition(int status) { super(1, status); }
	/**
	 * return the MessageStatus value for this Disposition
	 * @return value as defined in MessageStatus
	 */
	public int getMessageStatus() {
	    return value;
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
    public static final class SessionCondition extends Condition {
        private SessionCondition(int status) { super(2, status); }
    }

    /**
     * expiration condition
     */
    public static final class ExpirationCondition extends Condition
    {
	private java.util.Date date;
	/**
	 * @param date date at which the message expires
	 */
	public ExpirationCondition(java.util.Date date) {
	    super(3);
	    this.date = date;
	}
	/**
	 * get expiration date
	 * @return expiration date
	 */
	public java.util.Date getDate() { return date; }

	/**
	 * tests equality with another object
	 * @return true if the provided object is a ExpirationCondition
	 * instance and the dates are the same; false otherwise.
	 */ 
        public boolean equals(Object o) {
            if (o instanceof ExpirationCondition) {
                return (((ExpirationCondition)o).getDate().equals(date));
            } else {
                return false;
            }
        }
    }

    /**
     * discard the message silently
     */
    public static final Action DROP   = new Action(0);

    /**
     * notifies the sender if the condition is matched
     * The notification is provided through the
     * MessageStatusListener object provided when sending the
     * message
     */
    public static final Action NOTIFY = new Action(1);

    /**
     * notifies the sender if the condition is matched
     * The notification is provided through the
     * MessageStatusListener object provided when sending the
     * message
     */
    public static final Action ALERT  = new Action(2);

    /**
     * notifies the sender if the condition is matched
     * The notification is provided through the
     * MessageStatusListener object provided when sending the
     * message
     */
    public static final Action ERROR  = new Action(3);

    /**
     * store the message for future delivery.  This action
     * should be only in combination with a expiration-based
     * DROP rule.
     */
    public static final Action DEFER  = new Action(4);


    /**
     * message to be delivered directly to online user
     */
    public static final DispositionCondition DIRECT   = new DispositionCondition(MessageStatus.DELIVERED);

    /**
     * message to be stored for future delivery
     */
    public static final DispositionCondition STORED   = new DispositionCondition(MessageStatus.DELAYED);

    /**
     * message to be forwarded to another XMPP domain
     */
    public static final DispositionCondition FORWARD  = new DispositionCondition(MessageStatus.FORWARDED);

    /**
     * message to be forwarded to another messaging 
     * system.
     */
    public static final DispositionCondition GATEWAY  = new DispositionCondition(MessageStatus.GATEWAYED);

    /**
     * message to be discarded
     */
    public static final DispositionCondition NONE     = new DispositionCondition(MessageStatus.FAILED);


    /**
     * deliver to any of the recipients session.  Note that if
     * the destination address specifies a session, this session will be tried 
     * first.
     */
    public static final SessionCondition ANY    = new SessionCondition(0);

    /**
     * deliver to any session except for the one specified by the
     * recipient address
     */
    public static final SessionCondition NOT    = new SessionCondition(1);

    /**
     * deliver only to session matched by the recipient address
     */
    public static final SessionCondition EQUALS = new SessionCondition(2);

    /**
     * get the condition set contained in this rule
     * @return array of Condition objects
     */
    public Condition[] getConditions() throws IllegalArgumentException;

    /**
     * get this rule's action
     * @return action for this rule.
     */
    public Action getAction();

}

