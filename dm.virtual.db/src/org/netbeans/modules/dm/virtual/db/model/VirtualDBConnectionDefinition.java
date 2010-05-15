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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.dm.virtual.db.model;

import org.openide.util.NbBundle;

/**
 * 
 * @author Ahimanikya Satapathy
 */
public class VirtualDBConnectionDefinition implements Cloneable, Comparable {

    public static final String AXION_DRIVER = "org.axiondb.jdbc.AxionDriver";
    private String url;
    protected String driverClass = "";
    protected String jdbcUrl = "";
    protected volatile String name;
    protected String password = "";
    protected String userName = "";

    public VirtualDBConnectionDefinition(String connName) {
        name = connName;
        userName = "sa";
        password = "sa";
        driverClass = AXION_DRIVER;
    }

    public VirtualDBConnectionDefinition(String connName, String driverName, String connUrl, String uname, String passwd) {
        name = connName;

        driverClass = driverName;
        url = connUrl;
        userName = uname;
        password = passwd;
    }

    public VirtualDBConnectionDefinition(VirtualDBConnectionDefinition connectionDefn) {
        if (connectionDefn == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(VirtualDBConnectionDefinition.class, "MSG_Null_DBConnectionDefinition"));
        }

        if (connectionDefn instanceof VirtualDBConnectionDefinition) {
            copyFrom((VirtualDBConnectionDefinition) connectionDefn);
        }
    }

    public String getDriverClass() {
        return driverClass;
    }
        
    public String getConnectionURL() {
        return url;
    }

    public void setConnectionURL(String aUrl) {
        url = aUrl;
    }

    public synchronized void copyFrom(VirtualDBConnectionDefinition source) {
        if (source == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(VirtualDBConnectionDefinition.class, "MSG_Null_sourceRef"));
        } else if (source == this) {
            return;
        }

        this.name = source.getName();
        this.driverClass = source.driverClass;
        this.url = source.url;
        this.userName = source.userName;
        this.password = source.password;
    }

    @Override
    public boolean equals(Object o) {
        // Check for reflexivity.
        if (this == o) {
            return true;
        } else if (!(o instanceof VirtualDBConnectionDefinition)) {
            return false;
        }

        boolean response = true;
        VirtualDBConnectionDefinition impl = (VirtualDBConnectionDefinition) o;
        boolean nameEqual = (name != null) ? name.equals(impl.name) : (impl.name == null);
        response &= nameEqual;
        boolean urlEqual = (url != null) ? url.equals(impl.url) : (impl.url == null);
        response &= urlEqual;
        return response;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;

        hashCode += (name != null) ? name.hashCode() : 0;
        hashCode += (url != null) ? url.hashCode() : 0;
        return hashCode;
    }

    public int compareTo(Object refObj) {
        VirtualDBConnectionDefinition defn = (VirtualDBConnectionDefinition) refObj;
        return url.compareTo(defn.url);
    }

    public synchronized String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(1000);
        final String nl = ", "; // NOI18N

        buf.append("{ name: \"").append(name).append("\"").append(nl); // NOI18N
        buf.append("jdbcUrl: \"").append(jdbcUrl).append("\"").append(nl); // NOI18N
        buf.append(" }"); // NOI18N
        return buf.toString();
    }
}

