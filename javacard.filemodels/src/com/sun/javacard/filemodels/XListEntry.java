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

package com.sun.javacard.filemodels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Tim Boudreau
 */
public class XListEntry implements FileModelEntry {
    private int order;
    private final List<XListInstanceEntry> instances = Collections.synchronizedList(new ArrayList<XListInstanceEntry>());
    private String displayName;
    private String type;
    public XListEntry() {

    }

    public List<? extends XListInstanceEntry> getInstances() {
        return Collections.unmodifiableList(instances);
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isValid() {
        return type != null && displayName != null;
    }

    public void addInstance (XListInstanceEntry instance) {
        instances.add (instance);
    }

    public String toXml() {
        StringBuilder sb = new StringBuilder();
        sb.append ("        <name>"); //NOI18N
        sb.append (getDisplayName());
        sb.append ("</name>\n"); //NOI18N
        sb.append ("        <type>"); //NOI18N
        sb.append (getType());
        sb.append ("</type>\n"); //NOI18N
        if (!instances.isEmpty()) {
            sb.append ("        <instances>\n"); //NOI18N
            for (XListInstanceEntry e : instances) {
                sb.append("            <instance>"); //NOI18N
                sb.append(e.getContent());
                sb.append("</instance>\n"); //NOI18N
            }
            sb.append ("        </instances>\n"); //NOI18N
        }
        return sb.toString();
    }

    public int getOrder() {
        return order;
    }

    public String getProblem() {
        return null;
    }

    public int compareTo(FileModelEntry o) {
        return getOrder() - o.getOrder();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final XListEntry other = (XListEntry) obj;
        if (this.order != other.order) {
            return false;
        }
        if (this.instances != other.instances && (this.instances == null || !this.instances.equals(other.instances))) {
            return false;
        }
        if ((this.displayName == null) ? (other.displayName != null) : !this.displayName.equals(other.displayName)) {
            return false;
        }
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.order;
        hash = 29 * hash + (this.instances != null ? this.instances.hashCode() : 0);
        hash = 29 * hash + (this.displayName != null ? this.displayName.hashCode() : 0);
        hash = 29 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }
}
