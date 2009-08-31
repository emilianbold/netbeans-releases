/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.card;

import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.api.CardState;
import org.netbeans.modules.javacard.api.JavacardPlatform;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.javacard.constants.JavacardPlatformKeyNames;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.libraries.LibrariesManager;

/**
 *
 * @author Anki R Nelaturu
 */
public final class ReferenceImplementation extends SunJavaCardServer {

    private static final String CARD_MANAGER_APPLICATION = "/cardmanager";    // Settings
    private String ramSize = "24K";
    private String e2pSize = "128K";
    private String corSize = "2K";
    private String loggerLevel = "none";
    private String httpPort = "8019";
    private String proxy2cjcrePort = "7019";
    private String proxy2idePort = "7020";
    private String contactedPort = "9025";
    private String contactedProtocol = "T=1";
    private String contactlessPort = "9026";
    private File eepromFile = null;
    private boolean noSuspend = true;
    private boolean remote;
    /**
     * 
     * @param id
     * @param displayName
     * @param jcdkHome
     * @param userName
     * @param password
     * @param ramSize
     * @param e2pSize
     * @param corSize
     * @param loggerLevel
     * @param httpPort
     */
    public ReferenceImplementation(JavacardPlatform platform, String id, String displayName, 
            String userName, String password, String ramSize, String e2pSize,
            String corSize, String loggerLevel, String httpPort,
            String contactedPort, String contactedProtocol, 
            String contactlessPort, boolean secureMode, String proxy2cjcrePort, String proxy2idePort,
            boolean remote) {

        super(platform, id, userName, password);
        this.ramSize = ramSize;
        this.e2pSize = e2pSize;
        this.corSize = corSize;
        this.loggerLevel = loggerLevel;
        this.httpPort = httpPort;
        this.contactedPort = contactedPort;
        this.contactedProtocol = contactedProtocol;
        this.contactlessPort = contactlessPort;
        this.proxy2cjcrePort = proxy2cjcrePort;
        this.proxy2idePort = proxy2idePort;
        this.eepromFile = Utils.eepromFileForDevice(platform, id, true);
        this.remote = remote;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append ("[");
        sb.append("------------- Server Info ----------------"); //NOI18N
        sb.append("\nRAM Size: " + ramSize); //NOI18N
        sb.append("\nEEPROM Size: " + e2pSize); //NOI18N
        sb.append("\nCOR Size: " + corSize); //NOI18N
        sb.append("\nHTTP Port: " + httpPort); //NOI18N
        sb.append("\nContacted Port: " + contactedPort); //NOI18N
        sb.append("\nContacted Protocol: " + contactedProtocol); //NOI18N
        sb.append("\nContactless Port: " + contactlessPort); //NOI18N
        sb.append("\nDebug [proxy to cjcre] port: " + proxy2cjcrePort); //NOI18N
        sb.append("\nDebug [proxy to ide] port: " + proxy2idePort); //NOI18N
        sb.append("\n--------------------------------------------"); //NOI18N
        sb.append ("]");
        return sb.toString();
    }

    /**
     * 
     * @return
     */
    @Override
    public String[] getDebugProxyCommandLine(Object... args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException ("Project should be first " +
                    "argument");
        }
        JCProject p = (JCProject) args[0];
        return new String[] {
            "cmd", //NOI18N
            "/c", //NOI18N
            "debugproxy.bat", //NOI18N
            "--listen", //NOI18N
            getProxy2idePort(), 
            "--remote", //NOI18N
            "localhost:" + getProxy2cjcrePort(), //NOI18N
            "--classpath", //NOI18N
            new File(p.getProjectDirectory().toString(), p.evaluator().getProperty("dist.bundle")).getAbsolutePath()
            + File.pathSeparator
            + p.getLookup().lookup(LibrariesManager.class).getProjectLibraryClasspath(p),
        };
    }

    private String getEmulator() {
        String exePath = getPlatform().toProperties().getProperty(
                JavacardPlatformKeyNames.PLATFORM_EMULATOR_PATH);
        if (exePath == null) {
            //XXX probably should show a dialog or otherwise configure,
            //not throw an exception - properties files can get corrupted
            throw new NullPointerException ("No value for " + //NOI18N
                    JavacardPlatformKeyNames.PLATFORM_EMULATOR_PATH + " in " + //NOI18N
                    getPlatform());
        }
        return exePath;
    }
    
    @Override
    public String[] getStartCommandLine(boolean forDebug) {
        return new String[]{
                    new File(getEmulator()).getAbsolutePath(),
                    "-ramsize", //NOI18N
                    getRamSize(),
                    "-e2psize", //NOI18N
                    getE2pSize(),
                    "-corsize", //NOI18N
                    getCorSize(),
                    forDebug?"-debugger":"", //NOI18N
                    forDebug?"-debugport":"", //NOI18N
                    forDebug?getProxy2cjcrePort():"", //NOI18N
                    "-e2pfile", //NOI18N
                    getEepromFile().getAbsolutePath(),
                    "-loggerlevel", //NOI18N
                    getLoggerLevel().toLowerCase(),
                    "-httpport", //NOI18N
                    getHttpPort(),
                    "-contactedport", //NOI18N
                    getContactedPort(),
                    "-contactedprotocol", //NOI18N
                    getContactedProtocol(),
                    "-contactlessport", //NOI18N
                    getContactlessPort(),
                    noSuspend ? "-nosuspend" : "", //NOI18N
                };
    }

    @Override
    public String[] getResumeCommandLine() {
        return new String[]{
                    new File(getEmulator()).getAbsolutePath(),
                    "-resume", //NOI18N
                    "-e2pfile", //NOI18N
                    getEepromFile().getAbsolutePath()
                };
    }

    @Override
    public File getProcessDir() {
        //XXX HARDCODING
        return new File(getJcdkHome(), "bin"); //NOI18N
    }

    /**
     * 
     * @return
     */
    public File getEepromFile() {
        return eepromFile;
    }

    /**
     * 
     * @param eepromFile
     */
    public void setEepromFile(File eepromFile) {
        this.eepromFile = eepromFile;
    }

    /**
     * @return the ramSize
     */
    public final String getRamSize() {
        return ramSize;
    }

    /**
     * @param ramSize the ramSize to set
     */
    public final void setRamSize(String ramSize) {
        this.ramSize = ramSize;
    }

    /**
     * @return the e2pSize
     */
    public final String getE2pSize() {
        return e2pSize;
    }

    @Override
    public final boolean isRemote() {
        return remote;
    }

    /**
     * @param size the e2pSize to set
     */
    public final void setE2pSize(String size) {
        e2pSize = size;
    }

    /**
     * @return the corSize
     */
    public final String getCorSize() {
        return corSize;
    }

    /**
     * @param corSize the corSize to set
     */
    public final void setCorSize(String corSize) {
        this.corSize = corSize;
    }

    /**
     * @return the loggerLevel
     */
    public final String getLoggerLevel() {
        return loggerLevel;
    }

    /**
     * @param loggerLevel the loggerLevel to set
     */
    public final void setLoggerLevel(String loggerLevel) {
        this.loggerLevel = loggerLevel;
    }

    @Override
    public final String getServerURL() {
        return "http://localhost:" + getHttpPort(); //NOI18N
    }

    @Override
    public final String getCardManagerURL() {
        return getServerURL() + CARD_MANAGER_APPLICATION;
    }

    public String getContactedPort() {
        return contactedPort;
    }

    public String getContactedProtocol() {
        return contactedProtocol;
    }

    public String getContactlessPort() {
        return contactlessPort;
    }

    public void setContactedPort(String contactedPort) {
        this.contactedPort = contactedPort;
    }

    public void setContactedProtocol(String contactedProtocol) {
        this.contactedProtocol = contactedProtocol;
    }

    /**
     * @return the httpPort
     */
    public final String getHttpPort() {
        return httpPort;
    }

    /**
     * @param httpPort the httpPort to set
     */
    public final void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    public String getProxy2cjcrePort() {
        return proxy2cjcrePort;
    }

    public void setProxy2cjcrePort(String proxy2cjcrePort) {
        this.proxy2cjcrePort = proxy2cjcrePort;
    }

    public String getProxy2idePort() {
        return proxy2idePort;
    }

    public void setProxy2idePort(String proxy2idePort) {
        this.proxy2idePort = proxy2idePort;
    }

    void setNoSuspend(boolean noSuspend) {
        this.noSuspend = noSuspend;
    }

    public Set<Integer> getPortsInUse() {
        Set<Integer> result = new HashSet<Integer>();
        result.add(Integer.parseInt(contactedPort));
        result.add(Integer.parseInt(contactlessPort));
        result.add (Integer.parseInt(httpPort));
        result.add (Integer.parseInt(proxy2cjcrePort));
        result.add (Integer.parseInt(proxy2idePort));
        return result;
    }

    public Set<Integer> getPortsInActiveUse() {
        CardState state = getState();
        if (state == CardState.RUNNING || state == CardState.RUNNING_IN_DEBUG_MODE) {
            Set<Integer> result = getPortsInUse();
            if (state != CardState.RUNNING_IN_DEBUG_MODE) {
                result.remove (Integer.parseInt(proxy2idePort));
            }
            return result;
        }
        return Collections.emptySet();
    }
}
