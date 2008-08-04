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
public class JSHttpProgress implements JSHttpMessage {

    private final String id;
    private final String timeStamp;
    private final int current;
    private final int max;
    private final int total;
    private final int maxTotal;
    private final Map<String, String> headerData;
    private final String status;
    private final String mimeType;
    private final String responseText;
    private final String category;

    public JSHttpProgress(HttpMessage message) {
        id = message.getId();
        assert id != null;
        timeStamp = message.getTimeStamp();
        current = Integer.parseInt(message.getChildValue("current"));
        max = Integer.parseInt(message.getChildValue("max"));
        total = Integer.parseInt(message.getChildValue("total"));
        maxTotal = Integer.parseInt(message.getChildValue("maxTotal"));

        headerData = Collections.<String,String>unmodifiableMap(message.getHeader());
        status = message.getChildValue("status");
        mimeType = message.getChildValue("mimeType");
        responseText = message.getResponseText();
        category = message.getChildValue("category");
    }

    public String getCategory() {
        return category;
    }

    public String getResponseText() {
        return responseText;
    }

    public final static Type getType() {
        return Type.PROGRESS;
    }

    public String getId() {
        return id;
    }


    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     * @return the current
     */
    public int getCurrent() {
        return current;
    }

    /**
     * @return the max
     */
    public int getMax() {
        return max;
    }

    /**
     * @return the total
     */
    public int getTotal() {
        return total;
    }

    /**
     * @return the maxTotal
     */
    public int getMaxTotal() {
        return maxTotal;
    }

    public Map<String,String> getHeader() {
        return Collections.unmodifiableMap(headerData);
    }

    /**
     * @return the mimeType
     */
    public String getStatus() {
        return status;
    }


    public String getMimeType() {
        return mimeType;
    }
}
