/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.client.tools.javascript.debugger.impl;

import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.web.client.tools.common.dbgp.HttpMessage;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessage;

/**
 *
 * @author jdeva
 */
public class JSHttpRequest implements JSHttpMessage {

    private final String id;
    private final MethodType method;
    private final String timeStamp;
    private final String urlParams;
    private final Map<String, String> headerData;
    private final String postText;
    private final String url;
    private final boolean loadTriggeredByUser;

    public JSHttpRequest(HttpMessage message) {
        id = message.getId();
        assert id != null;
        method = JSFactory.getHttpMessageMethodType(message.getMethodType());
        timeStamp = message.getTimeStamp();
        headerData = Collections.<String,String>unmodifiableMap(message.getHeader());
        urlParams = message.getUrlParams();
        postText = message.getChildValue("postText");
        url = message.getUrl();
        loadTriggeredByUser = message.isLoadTriggerByUser();
    }

    public boolean isLoadTriggeredByUser () {
        return loadTriggeredByUser;
    }

    public String getPostText() {
        return postText;
    }


    public String getUrlParams() {
        return urlParams.toString();
    }

    public MethodType getMethod() {
        return method;
    }

    public final static Type getType() {
        return Type.REQUEST;
    }

    public String getId() {
        return id;
    }

    public Map<String,String> getHeader() {
        return Collections.unmodifiableMap(headerData);
    }

    public String getTimeStamp() {
        return timeStamp;
    }


    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return url;
    }

}
