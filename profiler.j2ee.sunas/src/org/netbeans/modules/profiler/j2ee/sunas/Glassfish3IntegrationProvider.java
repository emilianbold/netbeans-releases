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

package org.netbeans.modules.profiler.j2ee.sunas;

import java.util.StringTokenizer;
import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Jaroslav Bachorik
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.attach.spi.IntegrationProvider.class)
public class Glassfish3IntegrationProvider extends SunASAutoIntegrationProvider {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final String APP_SERVER_CONSOLE_TITLE = NbBundle.getMessage(this.getClass(),
                                                                        "GlassFishV3IntegraionProvider_ProfiledGlassFish3ConsoleString"); // NOI18N
    private final String APP_SERVER_TITLE = NbBundle.getMessage(this.getClass(), "GlassFishV3IntegraionProvider_GlassFishV3String"); // NOI18N

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String getTitle() {
        return APP_SERVER_TITLE; // NOI18N
    }

    protected int getAttachWizardPriority() {
        return 20;
    }

    protected int getMagicNumber() {
        return 30;
    }

    protected String getWinConsoleString() {
        return APP_SERVER_CONSOLE_TITLE; // NOI18N
    }

    protected String getWinSpecificCommandLineArgs(String targetOS, boolean isRemote, int portNumber) {
        return "\"-agentpath:" + IntegrationUtils.getNativeLibrariesPath(targetOS, getTargetJava(), isRemote)
               + IntegrationUtils.getDirectorySeparator(targetOS) + IntegrationUtils.getProfilerAgentLibraryFile(targetOS) + "=" //NOI18N
               + "\\\"" + IntegrationUtils.getLibsDir(targetOS, isRemote) + "\\\"" + "," + portNumber + "\""; //NOI18N
    }

    @Override
    protected String getConfigDir(String targetOS) {
        return "glassfish" + IntegrationUtils.getDirectorySeparator(targetOS) + super.getConfigDir(targetOS);
    }

    @Override
    protected String getDomainsDirPath(String separator) {
        return "glassfish" + separator + super.getDomainsDirPath(separator);
    }

    @Override
    protected boolean usesXMLDeclaration() {
        return false;
    }

    @Override
    protected void insertJvmOptions(Document domainScriptDocument, Element profilerElement, String optionsString) {
        StringTokenizer tk = new StringTokenizer(optionsString);
        
        while (tk.hasMoreTokens()) {
            String option = tk.nextToken();
            if (option.trim().isEmpty()) continue;
            
            insertJvmOptionsElement(domainScriptDocument, profilerElement, option);
        }
    }

    @Override
    protected String getJvmOptionsElementText(String options) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer tk = new StringTokenizer(options);
        
        while (tk.hasMoreTokens()) {
            String option = tk.nextToken();
            if (option.trim().isEmpty()) continue;
            
            sb.append(createJvmOptionsElementText(option));
        }
        
        return sb.toString();
    }
    
    
}
