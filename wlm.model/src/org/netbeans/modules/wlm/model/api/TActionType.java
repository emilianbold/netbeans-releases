/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.api;

import org.netbeans.modules.wlm.model.api.TActionType;

/**
 * <p>
 * Java class for actionTypeAttribute.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name=&quot;actionTypeAttribute&quot;&gt;
 *   &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;&gt;
 *     &lt;enumeration value=&quot;onTaskCreated&quot;/&gt;
 *     &lt;enumeration value=&quot;onTaskClaimed&quot;/&gt;
 *     &lt;enumeration value=&quot;onTaskOutputSet&quot;/&gt;
 *     &lt;enumeration value=&quot;onTaskReassigned&quot;/&gt;
 *     &lt;enumeration value=&quot;onTaskRevoked&quot;/&gt;
 *     &lt;enumeration value=&quot;onTaskTimedOut&quot;/&gt;
 *     &lt;enumeration value=&quot;onTaskEscalated&quot;/&gt;
 *     &lt;enumeration value=&quot;onTaskCompleted&quot;/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
public enum TActionType {

    ASSIGNED("Assigned"),
    CLAIMED("Claimed"),
    COMPLETED("Completed"),
    EXPIRED("Expired"),
    ESCALATED("Escalated"),
    ABORTED("Aborted");

    // Issue #160126 (Remove 'UserDefined')
    // Temporaly disabled. Runtime does not support it
    // USER_DEFINED("UserDefined");
    
    
    private final String value;

    TActionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TActionType fromValue(String v) {
        for (TActionType c : TActionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }
}
