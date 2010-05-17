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

import com.sun.javacard.AID;
import com.sun.javacard.Portability;

public final class AppletXmlAppletEntry implements FileModelEntry {
    String displayName;
    final String clazz;
    AID aid;
    int order;

    public AppletXmlAppletEntry(String displayName, String clazz, AID aid, int order) {
        this.displayName = displayName;
        this.clazz = clazz;
        this.aid = aid;
        this.order = order;
    }

    public AppletXmlAppletEntry() {
        this (null, null, null, 0);
    }

    public String getProblem() {
        if (displayName == null || displayName.trim().length() == 0) {
            return Portability.getString("MSG_NO_DISPLAY_NAME", clazz); //NOI18N
        }
        if (aid == null) {
            Portability.getString("MSG_NO_AID", clazz); //NOI18N
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (AppletXmlAppletEntry.class != obj.getClass()) {
            return false;
        }
        final AppletXmlAppletEntry other = (AppletXmlAppletEntry) obj;
        /*
         //XXX fixme
        String dn = this.displayName;
        String odn = other.displayName;
        boolean result = (dn == null) == (odn == null);
        if (result && dn != null) {
            result = !dn.equals(odn);
            if (!result) {
                return false;
            }
        }
        if ((this.clazz == null) ? (other.clazz != null) : !this.clazz.equals(other.clazz)) {
            return false;
        }
        if (this.aid != other.aid && (this.aid == null || !this.aid.equals(other.aid))) {
            return false;
        }
        return true;
         */
        return toXml().equals(other.toXml());
    }

    public String toXml() {
        StringBuilder sb = new StringBuilder("    <applet>\n"); //NOI18N
        sb.append("        <display-name>"); //NOI18N
        sb.append(displayName);
        sb.append("</display-name>\n"); //NOI18N
        sb.append("        <applet-class>"); //NOI18N
        sb.append(clazz);
        sb.append("</applet-class>\n"); //NOI18N
        sb.append("        <applet-AID>"); //NOI18N
        sb.append(aid);
        sb.append("</applet-AID>\n"); //NOI18N
        sb.append("    </applet>\n\n"); //NOI18N
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.displayName != null ? this.displayName.hashCode() : 0);
        hash = 13 * hash + (this.clazz != null ? this.clazz.hashCode() : 0);
        hash = 13 * hash + (this.aid != null ? this.aid.hashCode() : 0);
        return hash;
    }

    public int getOrder() {
        return order;
    }

    public int compareTo(FileModelEntry o) {
        assert o == null || o.getClass() == getClass();
        return o == null ? 0 : getOrder() - o.getOrder();
    }

    public String getClassname() {
        return clazz;
    }

    public AID getAID() {
        return aid;
    }

    public void setAID(AID aid) {
        this.aid = aid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
