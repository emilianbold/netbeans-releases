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

package org.netbeans.modules.j2me.cdc.project.bdj;

// IMPORTANT! You need to compile this class against ant.jar.
// The easiest way to do this is to add ${ant.core.lib} to your project's classpath.
// For example, for a plain Java project with no other dependencies, set in project.properties:
// javac.classpath=${ant.core.lib}

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.openide.util.Exceptions;

/**
 * @author suchys
 */
public class BdjBuildBdjo extends Task {
    
    private File bdjoFile; 
    private String orgId;
    private String appId;
    private String mainClass;


    public @Override void execute() throws BuildException {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(bdjoFile);
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            pw.println("<bdjo>");
            pw.println("    <appCacheInfo>");
            pw.println("        <entries>");
            pw.println("            <language>*.*</language>");
            pw.println("            <name>00000</name>");
            pw.println("            <type>1</type>");
            pw.println("        </entries>");
            pw.println("    </appCacheInfo>");
            pw.println("    <applicationManagementTable>");
            pw.println("        <applications>");
            pw.println("            <applicationDescriptor>");
            pw.println("                <baseDirectory>00000</baseDirectory>");
            pw.println("                <binding>TITLE_BOUND_DISC_BOUND</binding>");
            pw.println("                <classpathExtension>/00000</classpathExtension>");
            pw.println("                <iconFlags>0x0</iconFlags>");
            pw.println("                <iconLocator></iconLocator>");
            pw.println("                <initialClassName>" + mainClass + "</initialClassName>");
            pw.println("                <priority>1</priority>");
            pw.println("                <profiles>");
            pw.println("                    <majorVersion>1</majorVersion>");
            pw.println("                    <microVersion>0</microVersion>");
            pw.println("                    <minorVersion>0</minorVersion>");
            pw.println("                    <profile>1</profile>");
            pw.println("                </profiles>");
            pw.println("                <visibility>V_11</visibility>");
            pw.println("            </applicationDescriptor>");
            pw.println("            <applicationId>0x" + appId + "</applicationId>");
            pw.println("            <controlCode>0x1</controlCode>");
            pw.println("            <organizationId>0x" + orgId + "</organizationId>");
            pw.println("            <type>0x1</type>");
            pw.println("        </applications>");
            pw.println("    </applicationManagementTable>");
            pw.println("    <fileAccessInfo>.</fileAccessInfo>");
            pw.println("    <keyInterestTable>0xffe00000</keyInterestTable>");
            pw.println("    <tableOfAccessiblePlayLists>");
            pw.println("        <accessToAllFlag>true</accessToAllFlag>");
            pw.println("        <autostartFirstPlayListFlag>false</autostartFirstPlayListFlag>");
            //pw.println("        <playListFileNames>00001</playListFileNames>");
            pw.println("    </tableOfAccessiblePlayLists>");
            pw.println("    <terminalInfo>");
            pw.println("       <defaultFontFile>*****</defaultFontFile>");
            pw.println("        <initialHaviConfig>HD_1920_1080</initialHaviConfig>");
            pw.println("        <menuCallMask>false</menuCallMask>");
            pw.println("        <titleSearchMask>false</titleSearchMask>");
            pw.println("    </terminalInfo>");
            pw.println("    <version>V_0200</version>");
            pw.println("</bdjo>");
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            pw.close();
        }
    }

    public File getBdjoFile() {
        return bdjoFile;
    }

    public void setBdjoFile(File permFile) {
        this.bdjoFile = permFile;
    }
    
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }
}
