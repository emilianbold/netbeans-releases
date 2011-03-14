/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common.wizards;

import java.io.File;
import org.openide.util.NbBundle;

/**
 *
 * @author vkraemer
 */
public enum ServerDetails {
    
    GLASSFISH_SERVER_3 (NbBundle.getMessage(ServerDetails.class,
                    "STR_3_SERVER_NAME", new Object[]{}), // NOI18N "GlassFish Server 3.0",
            "deployer:gfv3ee6",
                new String[]{"lib" + File.separator + "schemas" + File.separator // NOI18N
                    + "web-app_3_0.xsd"}, // NOI18N
                new String[]{"lib" + File.separator + "dtds" + File.separator  // NOI18N
                    + "glassfish-web-app_3_0-1.dtd"}, // NOI18N
                "http://download.java.net/glassfish/" + // NOI18N
                    "3.0.1/release/glassfish-3.0.1-ml.zip?nbretriever=fallback", // NOI18N
                "http://serverplugins.netbeans.org/glassfishv3/post69v3.txt" // NOI18N
            ),
    GLASSFISH_SERVER_3_1 (NbBundle.getMessage(ServerDetails.class,
                    "STR_31_SERVER_NAME", new Object[]{}), // NOI18N "GlassFish Server 3.1",
            "deployer:gfv3ee6wc",
                new String[]{"lib" + File.separator + "dtds" + File.separator  // NOI18N
                    + "glassfish-web-app_3_0-1.dtd"}, // NOI18N
                new String[0],
                "http://download.java.net/glassfish/" + // NOI18N
                    "3.1/release/glassfish-3.1.zip?nbretriever=fallback", // NOI18N
                "http://serverplugins.netbeans.org/glassfishv3/post69v3-1.txt" // NOI18N
            );
    
    String displayName;
    String uriFragment;
    public String indirectUrl;
    public String directUrl;
    String[] requiredFiles;
    String[] excludedFiles;
    

    ServerDetails(String displayName, String uriFragment, 
            String[] requiredFiles, String[] excludedFiles, 
            String directUrl, String indirectUrl) {
            this.displayName = displayName;
            this.uriFragment = uriFragment;
            this.indirectUrl = indirectUrl;
            this.directUrl = directUrl;
            this.requiredFiles = requiredFiles;
            this.excludedFiles = excludedFiles;
    }
    
    @Override 
    public String toString() {
        return displayName;
    }

}
