/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.nodes;

import org.netbeans.modules.wlm.model.api.Group;
import org.netbeans.modules.wlm.model.api.TAction;
import org.netbeans.modules.wlm.model.api.TAssignment;
import org.netbeans.modules.wlm.model.api.TChangeVariables;
import org.netbeans.modules.wlm.model.api.TCopy;
import org.netbeans.modules.wlm.model.api.TPriority;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.TTimeout;
import org.netbeans.modules.wlm.model.api.TTitle;
import org.netbeans.modules.wlm.model.api.User;
import org.netbeans.modules.wlm.model.api.VariableInit;
import org.netbeans.modules.wlm.model.api.WLMComponent;

/**
 *
 * @author anjeleevich
 */
public enum WLMNodeType {
    TASK(0),
    INIT_VARIABLES(1),
    COPY(2),
    TITLE(3),
    PRIORITY(4),
    ASSIGNMENT(5),
    USERS(6),
    USER(7),
    GROUPS(8),
    GROUP(9),
    TIMEOUTS(10),
    TIMEOUT(11),
    ACTIONS(12),
    ACTION(13),
    CHANGE_VARIABLES(14),
    LOCAL_NOTIFICATIONS(15),
    LOCAL_NOTIFICATION(16),
    ESCALATIONS(17),
    ESCALATION(18),
    NOTIFICATIONS(19),
    NOTIFICATION(20),
    NOTIFICATION_EMAILS(21),
    EMAIL_ADDRESS(22),
    NOTIFICATION_SUBJECT(23),
    NOTIFICATUIN_BODY(24),
    IMPORTS(25),
    IMPORT(26),
    KEYWORDS(27),
    KEYWORD(28);

    public final long mask;

    WLMNodeType(int offset) {
        mask = 1l << offset;
    }

    public static WLMNodeType getNodeTypeFor(WLMComponent component) {
        return null;
    }
}
