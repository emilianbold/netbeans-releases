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

import org.netbeans.lib.collab.*;
import org.netbeans.lib.collab.xmpp.jso.iface.x.amp.*;

public class XMPPMessageProcessingRule implements MessageProcessingRule {

    private AMPRule _rule;
    private Condition[] _conditions;
    private Action _action;

    XMPPMessageProcessingRule(AMPRule rule)
    {
        _rule = rule;
    }

    XMPPMessageProcessingRule(Condition[] c, Action a) {
        _conditions = c;
        _action = a;
    }

    /**
     * get the condition set contained in this rule
     * @return array of Condition objects
     */
    public Condition[] getConditions()
    {
        if (_conditions == null) {
            Object o = _rule.getConditionValue();
            if (o != null) {
                _conditions = new Condition[1];
                _conditions[0] = getCondition(o);
            }
        }
        return _conditions;
    }

    /**
     * get this rule's action
     * @return action for this rule.
     */
    public Action getAction() {
        if (_action == null) {
            _action = getAction(_rule.getAction());
        }
        return _action;
    }

    public boolean equals(Object o) {
        return _rule.equals(((XMPPMessageProcessingRule)o)._rule);
    }


    static Action getAction(AMPRule.Action action) {
        Action a = null;
        if (AMPRule.DROP.equals(action)) a = DROP;
        else if (AMPRule.DEFER.equals(action)) a = DEFER;
        else if (AMPRule.NOTIFY.equals(action)) a = NOTIFY;
        else if (AMPRule.ERROR.equals(action)) a = ERROR;
        else if (AMPRule.ALERT.equals(action)) a = ALERT;
        return a;

    }

    static Condition getCondition(AMPRule.Disposition disp) {
        Condition a = null;
        if (AMPRule.DIRECT.equals(disp)) a = DIRECT;
        else if (AMPRule.STORED.equals(disp)) a = STORED;
        else if (AMPRule.FORWARD.equals(disp)) a = FORWARD;
        else if (AMPRule.GATEWAY.equals(disp)) a = GATEWAY;
        else if (AMPRule.NONE.equals(disp)) a = NONE;
        return a;
    }

    static Condition getCondition(AMPRule.ResourceMatcher matcher) {
        Condition a = ANY;
        if (AMPRule.EXACT.equals(matcher)) a = EQUALS;
        else if (AMPRule.OTHER.equals(matcher)) a = NOT;
        return a;
    }

    static Condition getCondition(java.util.Date date) {
        return new ExpirationCondition(date);
    }

    static Condition getCondition(Object o) {
        if (o instanceof AMPRule.Disposition) {
            return getCondition((AMPRule.Disposition)o);
        }
        if (o instanceof AMPRule.ResourceMatcher) {
            return getCondition((AMPRule.ResourceMatcher)o);
        }
        if (o instanceof java.util.Date) {
            return getCondition((java.util.Date)o);
        }
        return null;
    }

    public String toString() { return _rule.toString(); }

    protected AMPRule getJSORule() { return _rule; }
}
