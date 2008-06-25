/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.modules.cnd.api.remote.ServerList;
import org.openide.util.Lookup;

/**
 *
 * @author gordonp
 */
public class DevelopmentHostConfiguration extends IntConfiguration {
    
    private static ServerList serverList = null;
    
    private StringConfiguration developmentHostName;

    public DevelopmentHostConfiguration() {
        super((IntConfiguration) null, _getDefaultServerIndex(), _getServerNames(), null);
        developmentHostName = new StringConfiguration(null, _getServerNames()[_getDefaultServerIndex()]);
    }
    
    // developmentHostName
    public StringConfiguration getDevelopmentHostName() {
        return developmentHostName;
    }
    
    public void setDevelopmentHostName(StringConfiguration developmentHostName) {
        this.developmentHostName = developmentHostName;
    }
    
    public String[] getServerNames() {
        return _getServerNames();
    }
    
    public int getDefaultServerIndex() {
        return _getDefaultServerIndex();
    }
    
    public String getDisplayName() {
        return developmentHostName.getValue();
    }
    
    @Override
    public void setValue(String v) {
        super.setValue(v);
    }
    
    private static int _getDefaultServerIndex() {
        if (getServerList() != null) {
            return serverList.getDefaultServerIndex();
        }
        return 0;
    }
    
    @Override
    public Object clone() {
        DevelopmentHostConfiguration clone = new DevelopmentHostConfiguration();
        clone.setDevelopmentHostName((StringConfiguration) getDevelopmentHostName().clone());
        return clone;
    }
    
    private static String[] _getServerNames() {
        if (getServerList() != null) {
            return serverList.getServerNames();
        }
        return new String[] { "localhost" }; // NOI18N
    }
    
    private static ServerList getServerList() {
        if (Boolean.getBoolean("cnd.remote.enable")) // DEBUG
        if (serverList == null) {
            serverList = (ServerList) Lookup.getDefault().lookup(ServerList.class);
        }
        return serverList;
    }
}
