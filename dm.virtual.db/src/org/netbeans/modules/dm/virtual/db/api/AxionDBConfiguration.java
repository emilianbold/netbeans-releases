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
package org.netbeans.modules.dm.virtual.db.api;

import java.io.File;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class AxionDBConfiguration {

    private static final AxionDBConfiguration DEFAULT = new AxionDBConfiguration();
    private org.netbeans.modules.dm.virtual.db.ui.AxionDBConfiguration deligate = new org.netbeans.modules.dm.virtual.db.ui.AxionDBConfiguration();
    public static final String PROP_DB_LOC = org.netbeans.modules.dm.virtual.db.ui.AxionDBConfiguration.PROP_DB_LOC;
    public static final String PROP_DRIVER_LOC = org.netbeans.modules.dm.virtual.db.ui.AxionDBConfiguration.PROP_DRIVER_LOC;

    public static AxionDBConfiguration getDefault() {
        return DEFAULT;
    }

    public String displayName() {
        return deligate.displayName();
    }

    public String getDriver() {
        return deligate.getDriver();

    }

    public void setDriver(String driver) {
        deligate.setDriver(driver);
    }

    //axiondb.jar location
    public String getLocation() {
        return deligate.getLocation();
    }

    public void setLocation(String location) {
        deligate.setLocation(location);
    }

    public static File getConfigFile() {
        return org.netbeans.modules.dm.virtual.db.ui.AxionDBConfiguration.getConfigFile();
    }
}
