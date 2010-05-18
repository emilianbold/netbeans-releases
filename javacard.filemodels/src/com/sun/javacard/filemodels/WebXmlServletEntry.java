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
package com.sun.javacard.filemodels;

import com.sun.javacard.Portability;

public final class WebXmlServletEntry implements FileModelEntry {

    private final String name;
    private final String clazz;
    private final int order;
    private String mapping;

    public WebXmlServletEntry(String name, String clazz, String mapping, int order) {
        this.name = name;
        this.clazz = clazz;
        this.mapping = mapping;
        this.order = order;
    }

    @Override
    public String toString() {
        return "Servlet name=" + name + " class=" + clazz + " mapping=" + mapping; //NOI18N
    }

    public String getProblem() {
        String key = null;
        if (name == null || name.trim().length() == 0) {
            key = "PROBLEM_NO_NAME"; //NOI18N
        } else if (name != null && name.trim().indexOf(' ') >= 0) { //NOI18N
            key = "PROBLEM_NAME_CONTAINS_SPACES"; //NOI18N
        } else if (mapping == null || mapping.trim().length() == 0) {
            key = "PROBLEM_MAPPING_NOT_SET"; //NOI18N
        } else if (mapping != null && mapping.trim().indexOf(' ') >= 0) { //NOI18N
            key = "PROBLEM_MAPPING_CONTAINS_SPACES"; //NOI18N
        } else if (mapping != null && !mapping.startsWith("/")) { //NOI18N
            key = "PROBLEM_MAPPING_DOESNT_START_WITH_SLASH"; //NOI18N
        }
        if (key != null) {
            return Portability.getString(key, trimmedClassName());
        }
        return null;
    }

    private String trimmedClassName() {
        if (clazz == null) return "???";
        int ix = clazz.lastIndexOf(".");
        if (ix != clazz.length() - 1) {
            return clazz.substring(ix + 1);
        } else {
            return clazz;
        }
    }

    public String toXml() {
        StringBuilder sb = new StringBuilder("\n    <servlet>\n         <servlet-name>"); //NOI18N
        sb.append(name);
        sb.append("</servlet-name>\n"); //NOI18N
        sb.append("         <servlet-class>"); //NOI18N
        sb.append(clazz);
        sb.append("</servlet-class>\n    </servlet>\n"); //NOI18N
        sb.append("    <servlet-mapping>\n        <servlet-name>"); //NOI18N
        sb.append(name);
        sb.append("</servlet-name>\n        <url-pattern>"); //NOI18N
        sb.append(mapping);
        sb.append("</url-pattern>\n    </servlet-mapping>\n\n"); //NOI18N
        return sb.toString();
    }

    public int getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WebXmlServletEntry other = (WebXmlServletEntry) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.clazz == null) ? (other.clazz != null) : !this.clazz.equals(other.clazz)) {
            return false;
        }
        if ((this.mapping == null) ? (other.mapping != null) : !this.mapping.equals(other.mapping)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 47 * hash + (this.clazz != null ? this.clazz.hashCode() : 0);
        hash = 47 * hash + (this.mapping != null ? this.mapping.hashCode() : 0);
        return hash;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return clazz;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public int compareTo(FileModelEntry o) {
        assert o == null || o.getClass() == getClass();
        return getOrder() - o.getOrder();
    }
}
