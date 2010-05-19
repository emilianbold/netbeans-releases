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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * 
 * @author rico
 */
public enum WLMQNames {
    TASK(createWLMQName("task")), // NOI18N
    IMPORT(createWLMQName("import")), // NOI18N
    ASSIGNMENT(createWLMQName("assignment")), // NOI18N
    EXCLUDED(createWLMQName("excluded")), // NOI18N
    USER(createWLMQName("user")), // NOI18N
    GROUP(createWLMQName("group")), // NOI18N
    TIMEOUT(createWLMQName("timeout")), // NOI18N
    ESCALATION(createWLMQName("escalation")), // NOI18N
    DURATION(createWLMQName("duration")), // NOI18N
    DEADLINE(createWLMQName("deadline")), // NOI18N
    NOTIFICATION(createWLMQName("notification")), // NOI18N
    ACTION(createWLMQName("action")), // NOI18N
    TITLE(createWLMQName("title")), // NOI18N
    PRIORITY(createWLMQName("priority")), // NOI18N
    INIT(createWLMQName("init")), // NOI18N
    COPY(createWLMQName("copy")), // NOI18N
    FROM(createWLMQName("from")), // NOI18N
    TO(createWLMQName("to")), // NOI18N
    LOCAL_NOTIFICATION(createWLMQName("localNotification")), // NOI18N
    VARIABLE_INIT(createWLMQName("variable-init")), // NOI18N
    CHANGE_VARIABLES(createWLMQName("changeVariables")), // NOI18N
    MESSAGE(createWLMQName("message")), // NOI18N
    SUBJECT(createWLMQName("subject")), // NOI18N
    BODY(createWLMQName("body")), // NOI18N
    EMAIL(createWLMQName("email")), // NOI18N
    EMAIL_ADDRESS(createWLMQName("address")), // NOI18N
    KEYWORDS(createWLMQName("keywords")), // NOI18N
    KEYWORD(createWLMQName("keyword")); // NOI18N
    
    
    public static final String WLM_NS_URI = "http://jbi.com.sun/wfse";

    public static final String WLM_PREFIX = "wlm";

    public static QName createWLMQName(String localName) {
        return new QName(WLM_NS_URI, localName, WLM_PREFIX);
    }

    WLMQNames(QName name) {
        qName = name;
    }

    QName getQName() {
        return qName;
    }

    private static Set<QName> qnames = null;

    public static synchronized Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (WLMQNames wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return Collections.unmodifiableSet(qnames);
    }

    private final QName qName;
}
