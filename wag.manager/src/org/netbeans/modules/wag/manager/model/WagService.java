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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wag.manager.model;

import java.util.List;

/**
 *
 * @author peterliu
 */
public class WagService implements Comparable<WagService> {

    private String displayName;
    private String callableName;
    private String uuid;
    private String description;
    private String url;
    private List<WagServiceParameter> parameters;

    public WagService() {

    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setCallableName(String name) {
        this.callableName = name;
    }

    public String getCallableName() {
        return callableName;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getDescription() {
        return description;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setParameters(List<WagServiceParameter> params) {
        this.parameters = params;
    }

    public List<WagServiceParameter> getParameters() {
        return parameters;
    }
   
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WagService) {
            return uuid.equals(((WagService) obj).getUuid());
        }

        return false;
    }

    @Override
    public String toString() {
        return callableName + "(" + getParamString() + ")";
    }

    private String getParamString() {
        String paramStr = "";

        int index = 0;
        for (WagServiceParameter p : parameters) {
            if (index++ > 0) {
                paramStr += ", ";
            }

            paramStr += p.getName();
        }

        return paramStr;
    }

    public int compareTo(WagService svc) {
        return displayName.compareTo(svc.getDisplayName());
    }
}
