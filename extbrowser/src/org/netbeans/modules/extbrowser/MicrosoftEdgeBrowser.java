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

package org.netbeans.modules.extbrowser;

import java.io.File;
import java.util.logging.Level;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;

/**
 * @author Jan Stola
 */
public class MicrosoftEdgeBrowser extends ExtWebBrowser {

    /**
     * Determines whether the browser should be visible or not.
     * 
     * @return {@code true} when the OS is Windows, returns {@code false} otherwise.
     */
    public static Boolean isHidden() {
        String osName = System.getProperty("os.name"); // NOI18N
        return !osName.startsWith("Windows 8") // JDK bug 8081573 // NOI18N
                && !"Windows 10".equals(osName); // NOI18N
    }

    private static final long serialVersionUID = 4333320552804224866L;

    public MicrosoftEdgeBrowser() {
        super(PrivateBrowserFamilyId.UNKNOWN);
    }

    @Override
    public String getName () {
        if (name == null) {
            name = NbBundle.getMessage(MicrosoftEdgeBrowser.class, "CTL_MicrosoftEdgeBrowserName"); // NOI18N
        }
        return name;
    }
    
    /**
     * Returns a new instance of BrowserImpl implementation.
     * @throws UnsupportedOperationException when method is called and OS is not Windows 10.
     * @return browserImpl implementation of browser.
     */
    @Override
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        ExtBrowserImpl impl = null;

        if (isHidden()) {
            throw new UnsupportedOperationException(NbBundle.getMessage(MicrosoftEdgeBrowser.class, "MSG_CannotUseBrowser")); // NOI18N
        } else {
            impl = new NbDdeBrowserImpl(this);
        }

        return impl;
    }
        
    /**
     * Default command for browser execution.
     *
     * @return process descriptor that allows to start browser.
     */
    @Override
    protected NbProcessDescriptor defaultBrowserExecutable () {
        String command = "cmd"; // NOI18N
        String params = "/C start shell:AppsFolder\\" + getAppUserModelId() + " {" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}"; // NOI18N
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "{0} MicrosoftEdge: defaultBrowserExecutable: {1}, {2}", new Object[] { System.currentTimeMillis(), params, command });
        }
        return new NbProcessDescriptor(command, params);
    }

    private String appUserModelId;
    private String getAppUserModelId() {
        if (appUserModelId == null) {
            // Hack that attepmts to get the correct hash in appUserModelId
            // Hopefully, the appUserModelId will be hash-less in the final version.
            // So, this hack will not be needed then.
            File folder = new File("C:\\Windows\\SystemApps"); // NOI18N
            String id = "Microsoft.MicrosoftEdge_8wekyb3d8bbwe"; // NOI18N
            for (File file : folder.listFiles()) {
                String fileName = file.getName();
                if (fileName.startsWith("Microsoft.MicrosoftEdge")) { // NOI18N
                    id = fileName;
                }
            }
            appUserModelId = id + "!MicrosoftEdge"; // NOI18N
        }
        return appUserModelId;
    }
    
    private void readObject (java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }

}
