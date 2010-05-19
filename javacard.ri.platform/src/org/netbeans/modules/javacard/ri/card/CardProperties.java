/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.ri.card;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.spi.DeclarableCapabilities;
import org.netbeans.modules.javacard.spi.ICardCapability;
import org.netbeans.modules.javacard.spi.JavacardDeviceKeyNames;
import org.netbeans.modules.javacard.spi.JavacardPlatformKeyNames;
import org.netbeans.modules.javacard.spi.capabilities.CapabilitiesProvider;
import org.netbeans.modules.propdos.ObservableProperties;
import org.netbeans.modules.propdos.PropertiesAdapter;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbCollections;

/**
 * Bean for card properties which reads and writes to a Properties object
 *
 * @author Tim Boudreau
 */
final class CardProperties implements ICardCapability, CapabilitiesProvider {
    public static final String DEBUG = "debug";
    public static final String SUSPEND = "suspend";
    private static final String DEFAULT_RAM_SIZE = "24K"; //NOI18N
    private static final String DEFAULT_E2P_SIZE = "128K"; //NOI18N
    private static final String DEFULT_COR_SIZE = "2K"; //NOI18N
    private static final String DEFAULT_LOGGER_LEVEL = "none"; //NOI18N
    private static final String DEFAULT_HTTP_PORT = "8019"; //NOI18N
    private static final String DEFAULT_PROXY_TO_EMULATOR_PORT = "7019"; //NOI18N
    private static final String DEFAULT_PROXY_TO_IDE_PORT = "7020"; //NOI18N
    private static final String DEFAULT_CONTACTED_PORT = "9025"; //NOI18N
    private static final String DEFAULT_CONTACTED_PROTOCOL = "T=1"; //NOI18N
    private static final String DEFAULT_CONTACTLESS_PORT = "9026"; //NOI18N
    private static final String DEFAULT_HOST = "127.0.0.1"; //NOI18N
    private static final String DEFAULT_CAPABILITIES = "START,STOP,RESUME," + //NOI18N
            "DEBUG,EPROM_FILE,CLEAR_EPROM,CONTENTS,CUSTOMIZER,INTERCEPTOR," + //NOI18N
            "PORTS,URL,DELETE"; //NOI18N
    private static final String E2P_FILE_DEF = "${javacard.device.eeprom.folder}" + //NOI18N
            "${file.separator}${javacard.device.name}.eprom"; //NOI18N
    static final String DEFAULT_DEBUG_PROXY_COMMAND_LINE = "${java.home}/bin/java " +
            "-classpath ${javacard.debug.proxy.classpath} " + //NOI18N
            "{{{-Djc.home=${javacard.ri.home}}}} " + //NOI18N
            "com.sun.javacard.debugproxy.Main " + //NOI18N
            "{{{debug}}} " + //NOI18N
            "--listen ${javacard.device.proxy2idePort} " + //NOI18N
            "--remote ${javacard.device.host}:${javacard.device.proxy2cjcrePort} " + //NOI18N
            "--classpath ${class.path}"; //NOI18N
    static final String NEW_RUN_COMMAND_LINE =
            "${" + JavacardPlatformKeyNames.PLATFORM_EMULATOR_PATH +"} " + //NOI18N
            "-debug ${" + DEBUG + "} " + //NOI18N
            "-suspend ${" + SUSPEND + "} " + //NOI18N
            "-ramsize ${" + JavacardDeviceKeyNames.DEVICE_RAMSIZE + "} " + //NOI18N
            "-e2psize ${" + JavacardDeviceKeyNames.DEVICE_E2PSIZE + "} " + //NOI18N
            "-corsize ${" + JavacardDeviceKeyNames.DEVICE_CORSIZE + "} " + //NOI18N
            "-e2pfile " + E2P_FILE_DEF + " " + //NOI18N
            "-loggerlevel ${" + JavacardDeviceKeyNames.DEVICE_LOGGERLEVEL + "} " + //NOI18N
            "-httpport ${" + JavacardDeviceKeyNames.DEVICE_HTTPPORT + "} " + //NOI18N
            "-contactedport ${" + JavacardDeviceKeyNames.DEVICE_CONTACTEDPORT +"} " + //NOI18N
            "-contactedprotocol ${" + JavacardDeviceKeyNames.DEVICE_CONTACTEDPROTOCOL + "} " + //NOI18N
            "-contactlessport ${" + JavacardDeviceKeyNames.DEVICE_CONTACTLESSPORT + "}"; //NOI18N

    static final String DEFAULT_RESUME_COMMAND_LINE = "${javacard.emulator} " + //NOI18N
            "-debug ${debug} " +
            "-suspend ${suspend} " +
            "-resume " + //NOI18N
            "-e2pfile " + E2P_FILE_DEF; //NOI18N

    private PropertiesAdapter props;
    private static final Logger LOGGER = Logger.getLogger(CardProperties.class.getPackage().getName());
    CardProperties(PropertiesAdapter props) {
        this.props = props;
    }

    Properties toProperties() {
        return props.asProperties();
    }

    public String getContactedPort() {
        return "" + getAsInt(JavacardDeviceKeyNames.DEVICE_CONTACTEDPORT, DEFAULT_CONTACTED_PORT); //NOI18N
    }

    public void setContactedPort(String contactedPort) {
        setAsInt (JavacardDeviceKeyNames.DEVICE_CONTACTEDPORT, contactedPort);
    }

    public String getContactedProtocol() {
        return getProp(JavacardDeviceKeyNames.DEVICE_CONTACTEDPROTOCOL, DEFAULT_CONTACTED_PROTOCOL);
    }

    public void setContactedProtocol(String contactedProtocol) {
        setProp(JavacardDeviceKeyNames.DEVICE_CONTACTEDPROTOCOL, contactedProtocol);
    }

    public String getContactlessPort() {
        return "" + getAsInt(JavacardDeviceKeyNames.DEVICE_CONTACTLESSPORT, DEFAULT_CONTACTLESS_PORT);
    }

    public void setContactlessPort(String contactlessPort) {
        setAsInt (JavacardDeviceKeyNames.DEVICE_CONTACTLESSPORT, contactlessPort);
    }

    public String getCorSize() {
        return getProp(JavacardDeviceKeyNames.DEVICE_CORSIZE, DEFULT_COR_SIZE);
    }

    public void setCorSize(String corSize) {
        setProp (JavacardDeviceKeyNames.DEVICE_CORSIZE, corSize);
    }

    public String getE2pSize() {
        return getProp(JavacardDeviceKeyNames.DEVICE_E2PSIZE, DEFAULT_E2P_SIZE);
    }

    public void setE2pSize(String e2pSize) {
        setProp (JavacardDeviceKeyNames.DEVICE_E2PSIZE, e2pSize);
    }

    public String getHttpPort() {
        return getProp(JavacardDeviceKeyNames.DEVICE_HTTPPORT, DEFAULT_HTTP_PORT);
    }

    public void setHttpPort(String httpPort) {
        setAsInt(JavacardDeviceKeyNames.DEVICE_HTTPPORT, httpPort);
    }

    public String getLoggerLevel() {
        return getProp(JavacardDeviceKeyNames.DEVICE_LOGGERLEVEL, DEFAULT_LOGGER_LEVEL);
    }

    public void setLoggerLevel(String loggerLevel) {
        setProp(JavacardDeviceKeyNames.DEVICE_LOGGERLEVEL, loggerLevel);
    }

    public boolean isSuspend() {
        String val = getProp(JavacardDeviceKeyNames.DEVICE_SUSPEND_THREADS_ON_STARTUP, "false"); //NOI18N
        return val == null ? false : Boolean.parseBoolean(val);
    }

    public void setSuspend(boolean suspend) {
        setProp (JavacardDeviceKeyNames.DEVICE_SUSPEND_THREADS_ON_STARTUP, "" + suspend); //NOI18N
    }

    public String getProxy2cjcrePort() {
        return getAsInt(JavacardDeviceKeyNames.DEVICE_PROXY2CJCREPORT, DEFAULT_PROXY_TO_EMULATOR_PORT) + ""; //NOI18N
    }

    public void setProxy2cjcrePort(String proxy2cjcrePort) {
        setAsInt (JavacardDeviceKeyNames.DEVICE_PROXY2CJCREPORT, proxy2cjcrePort);
    }

    public String getProxy2idePort() {
        return getAsInt(JavacardDeviceKeyNames.DEVICE_PROXY2IDEPORT, DEFAULT_PROXY_TO_IDE_PORT) + ""; //NOI18N
    }

    public void setProxy2idePort(String proxy2idePort) {
        setAsInt (JavacardDeviceKeyNames.DEVICE_PROXY2IDEPORT, proxy2idePort);
    }

    public String getRamSize() {
        return getProp(JavacardDeviceKeyNames.DEVICE_RAMSIZE, DEFAULT_RAM_SIZE);
    }

    public void setRamSize(String ramSize) {
        setProp (JavacardDeviceKeyNames.DEVICE_RAMSIZE, ramSize);
    }

    public boolean isRemote() {
        String val = getProp(JavacardDeviceKeyNames.DEVICE_IS_REMOTE, "true"); //NOI18N
        return val == null ? false : Boolean.parseBoolean(val);
    }

    public void setRemote(boolean remote) {
        setProp (JavacardDeviceKeyNames.DEVICE_IS_REMOTE, "" + remote); //NOI18N
    }

    public String getHost() {
        return getProp(JavacardDeviceKeyNames.DEVICE_HOST, DEFAULT_HOST);
    }

    public void setHost(String host) {
        setProp (JavacardDeviceKeyNames.DEVICE_HOST, host);
    }

    public Set<Integer> getPorts() {
        Set<Integer> result = new HashSet<Integer>();
        result.add(Integer.parseInt(getContactedPort()));
        result.add(Integer.parseInt(getContactlessPort()));
        result.add (Integer.parseInt(getHttpPort()));
        result.add (Integer.parseInt(getProxy2cjcrePort()));
        result.add (Integer.parseInt(getProxy2idePort()));
        return result;
    }

    private void setProp (String key, String value) {
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.log(Level.FINE, "Set javacard device property \'{0}\' to \'{1}\'",
                    new Object[]{key, value}); //NOI18N
        props.asProperties().setProperty(key, value);
    }

    private void setProp (String key, int value) {
        setProp (key, "" + value); //NOI18N
    }

    private void setAsInt (String key, String value) throws NumberFormatException {
        setProp (key, Integer.parseInt(value.trim()));
    }

    private String getProp(String key, String defawlt) {
        String result = props.asProperties().getProperty(key);
        if (result == null) {
            Logger.getLogger(CardProperties.class.getName()).log(Level.SEVERE, 
                    "Property {0} not found in {1}",
                    new Object[]{key, props}); //NOI18N
            result = defawlt;
        }
        return result;
    }

    private int getAsInt(String key, String defawlt) {
        String val = getProp(key, defawlt).trim();
        int result;
        try {
            result = Integer.parseInt(val);
            if (result > 65535 || result < 0) {
                throw new NumberFormatException("Illegal port value for " + //NOI18N
                        key + ": " + val); //NOI18N
            }
        } catch (NumberFormatException nfe) {
            Logger.getLogger(CardProperties.class.getName()).log(Level.SEVERE, 
                    "Property {0} has non integer value in {1}",
                    new Object[]{key, props}); //NOI18N
            result = Integer.parseInt(defawlt);
        }
        return result;
    }

    public Set<Class <? extends ICardCapability>> getSupportedCapabilityTypes() {
        String val = getProp(JavacardDeviceKeyNames.DEVICE_CAPABILITIES, DEFAULT_CAPABILITIES);
        Set <? extends DeclarableCapabilities> caps = DeclarableCapabilities.forString(val);
        return DeclarableCapabilities.types(caps);
    }

    public String[] getDebugProxyCommandLine(Properties platformProps, String classpathClosure) {
        Map<String,String> projectInfo = prepPlatformProps(platformProps, true);
        projectInfo.put ("class.path", classpathClosure); //NOi18N
        String[] result = Utils.shellSplit (evaluated(projectInfo,
                JavacardDeviceKeyNames.DEVICE_DEBUG_PROXY_COMMAND_LINE,
                DEFAULT_DEBUG_PROXY_COMMAND_LINE));
        return result;
    }

    public String[] getRunCommandLine(Properties platformProps, boolean forDebug, boolean suspend, boolean resume) {
        Map<String,String> platform = prepPlatformProps(platformProps, true);
        platform.put (DEBUG, forDebug ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
        platform.put (SUSPEND, suspend ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
        String key = !resume ? JavacardDeviceKeyNames.DEVICE_RUN_COMMAND_LINE : JavacardDeviceKeyNames.DEVICE_RESUME_COMMAND_LINE;
        String defawlt = !resume ? NEW_RUN_COMMAND_LINE : DEFAULT_RESUME_COMMAND_LINE;
        String cmdline = evaluated (platform, key, defawlt);
        String[] result = Utils.shellSplit (cmdline);
        return result;
    }

    private Map<String,String> prepPlatformProps(Properties platformProps, boolean addEpromPath) {
        Map<String,String> m = new HashMap<String, String>();
        m.putAll(NbCollections.checkedMapByCopy(platformProps,
                String.class, String.class, true));
        if (addEpromPath) {
            String dataObjectName = platformProps.getProperty(JavacardPlatformKeyNames.PLATFORM_ID);
            if (dataObjectName != null) {
                FileObject eepromFolder = Utils.sfsFolderForDeviceEepromsForPlatformNamed(
                        dataObjectName, true);
                assert eepromFolder != null;
                File f = FileUtil.toFile(eepromFolder);
                String path = f == null ? eepromFolder.getPath() : f.getAbsolutePath(); //null in unit tests
                m.put(JavacardDeviceKeyNames.DEVICE_EPROM_FOLDER, path);
            }
        }
        m.put("file.separator", File.separator); //NOI18N
        m.put("java.io.tmpdir", System.getProperty("java.io.tmpdir")); //NOI18N
        m.put("java.home", System.getProperty("java.home")); //NOI18N
        return m;
    }

    private String evaluated(Map<String,String> pre, String key, String defawlt) {
        ObservableProperties p = props.asProperties();
        String value = p.getProperty(key);
        value = value == null ? defawlt : value;
        Map<String,String> m = prepPlatformProps(p, false);
        PropertyProvider prov = PropertyUtils.fixedPropertyProvider(m);
        PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(
                PropertyUtils.fixedPropertyProvider(pre), prov);
        return eval.evaluate(value);
    }
}
