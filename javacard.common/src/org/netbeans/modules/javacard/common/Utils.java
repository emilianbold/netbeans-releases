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
package org.netbeans.modules.javacard.common;

import com.sun.javacard.AID;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;

/**
 * Miscellaneous utility methods which are generally useful.
 *
 * @author Tim Boudreau
 */
public final class Utils {
    private static final String RID_PREFERENCES_KEY = "RID"; //NOI18N
    private Utils() {
    }

    /**
     * Find a device file for the named card on the named platform.  The pa
     * is the system ID of a JavacardPlatform.  Looks up a folder in the
     * system filesystem where cards for a platform with the given name should
     * be registered, and returns the first child file found which has the
     * same name as the passed card.  Ordinarily there are not multiple files
     * with different extensions in the device folder;  what data object is
     * returned in that case is undefined.
     *
     * @param platform The system ID (DataObject/file name) of a JavacardPlatform
     * @param card The system ID of a card
     * @return A DataObject which may have the requested card in its lookup (this
     * method does matching by file name and does not check if there is really
     * an instance of Card present)
     * @deprecated Not all cards are DataObjects anymore
     */
    @Deprecated
    public static DataObject findDeviceForPlatform(String platform, String card) {
        FileObject deviceFolder = sfsFolderForDeviceConfigsForPlatformNamed(platform, false);
        if (deviceFolder != null) {
            for (FileObject child : deviceFolder.getChildren()) {
                if (card.equals(child.getName())) {
                    try {
                        return DataObject.find(child);
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the system filesystem folder for files that define Card objects,
     * given a platform name.
     * @param name The platform name
     * @param create Whether or not to create the folder if it does not exist
     * @return A FileObject, or null if it does not exist and !create
     */
    public static FileObject sfsFolderForDeviceConfigsForPlatformNamed(String name, boolean create) {
        String rootPath = CommonSystemFilesystemPaths.SFS_DEVICE_CONFIGS_ROOT;
        FileObject fld = FileUtil.getConfigFile(rootPath);
        if (fld == null) {
            try {
                fld = FileUtil.createFolder(FileUtil.getConfigRoot(), rootPath);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        FileObject result = fld.getFileObject(name);
        if (result == null && create) {
            try {
                result = FileUtil.createFolder(fld, name);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return result;
    }

    /**
     * Folder in the system filesystem where .eeprom files are kept for platforms
     * with a given name.
     * @param name The platform name (system ID)
     * @param create Whether or not to create the folder if it does not exist
     * @return A FileObject for the folder, or null
     */
    public static FileObject sfsFolderForDeviceEepromsForPlatformNamed(String name, boolean create) {
        Parameters.notNull("name", name); //NOI18N
        String rootPath = CommonSystemFilesystemPaths.SFS_DEVICE_EEPROMS_ROOT;
        FileObject fld = FileUtil.getConfigFile(rootPath);
        if (fld == null && create) {
            try {
                fld = FileUtil.createFolder(FileUtil.getConfigRoot(), rootPath);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        } else if (fld == null) {
            return null;
        }
        FileObject result = fld.getFileObject(name);
        if (result == null && create) {
            try {
                result = FileUtil.createFolder(fld, name);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return result;
    }

    /**
     * Attempts to locate an .eeprom file for a given device on a named platform.
     * @param platformName The name (system ID/data object name) of the platform
     * @param deviceName The name (system ID/data object name) of the card
     * @param create Create the file if it does not exist
     * @return The file, or null
     */
    public static File eepromFileForDevice(String platformName, String deviceName, boolean create) {
        FileObject fld = sfsFolderForDeviceEepromsForPlatformNamed(platformName, create);
        if (fld != null) {
            String filename = deviceName + '.' + JCConstants.EEPROM_FILE_EXTENSION;
            FileObject fo = fld.getFileObject(filename);
            if (fo == null && create) {
                try {
                    fo = FileUtil.createData(fld, filename);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (fo != null) {
                return FileUtil.toFile(fo);
            }
        }
        return null;
    }

    /**
     * Get the folder, defined by the Java Platform API, where Java Platform
     * files, including JavacardPlatforms, are registered
     * @return The folder
     */
    public static FileObject sfsFolderForRegisteredJavaPlatforms() {
        FileObject fld = FileUtil.getConfigFile(CommonSystemFilesystemPaths.SFS_JAVA_PLATFORMS_FOLDER); //NOI18N
        return fld;
    }

    public static FileObject sfsFolderForRegisteredJavaPlatforms(String expectedName) {
        FileObject fld = sfsFolderForRegisteredJavaPlatforms();
        if (expectedName != null) {
            boolean found = false;
            for (FileObject fo : fld.getChildren()) {
                found = expectedName.equals(fo.getName());
                if (found) {
                    break;
                }
            }
            if (!found) {
                FileSystem memfs = FileUtil.createMemoryFileSystem();
                try {
                    FileObject dummyFolder = FileUtil.createFolder(memfs.getRoot(), CommonSystemFilesystemPaths.SFS_JAVA_PLATFORMS_FOLDER);
                    FileObject dummyPlatform = dummyFolder.createData(expectedName, JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION);
                    MultiFileSystem mfs = new MultiFileSystem(new FileSystem[] { memfs, FileUtil.getConfigRoot().getFileSystem() });
                    fld = mfs.getRoot().getFileObject(CommonSystemFilesystemPaths.SFS_JAVA_PLATFORMS_FOLDER);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return fld;
    }
    /**
     * Locates all Javacard Platform DataObjects which use the .jcard extension.
     * @return An iterable over all such files
     */
    public static Iterable<DataObject> findAllRegisteredJavacardPlatformDataObjects() {
        FileObject fld = sfsFolderForRegisteredJavaPlatforms();
        List<DataObject> result = new LinkedList<DataObject>();
        for (FileObject fo : fld.getChildren()) {
            if (JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION.equals(fo.getExt())) {
                DataObject dob;
                try {
                    dob = DataObject.find(fo);
                    result.add(dob);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return result;
    }

    /**
     * Locates all Javacard Platform DataObjects which use the .jcard extension.
     * @return An iterable over all such files
     */
    public static Iterable<FileObject> findAllRegisteredJavacardPlatformFiles() {
        FileObject fld = sfsFolderForRegisteredJavaPlatforms();
        List<FileObject> result = new LinkedList<FileObject>();
        for (FileObject fo : fld.getChildren()) {
            if (JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION.equals(fo.getExt())) {
                result.add(fo);
            }
        }
        return result;
    }

    /**
     * Write a default APDU script to he passed ifle
     * @param file The file to write
     * @param acName The script name
     * @param aid The AID to select
     */
    public static void createAPDUScript(File file, String acName, String aid) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(("//Test script for Applet '" + acName + "'\n").getBytes());
            fos.write("\n".getBytes());
            fos.write(("powerup;\n").getBytes());
            fos.write(("// Select " + acName + " " + aid + "\n").getBytes());

            fos.write(("0x00 0xA4 0x04 0x00 " + Utils.getAIDStringWithLengthForScript(aid) + " 0x7F;\n").getBytes());
            fos.write("\n".getBytes());
            fos.write(("//Send the APDU here\n").getBytes());
            fos.write(("//0x80 0xCA 0x00 0x00 <length> <data> 0x7F;\n").getBytes());
            fos.write("\n".getBytes());
            fos.write(("powerdown;\n").getBytes());
            fos.close();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    private static final int FIXED_RID_LENGTH = 10;
    private static final int MAX_PIX_LENGTH = 22;
    public static final String AID_AUTHORITY = "//aid/"; //NOI18N

    /**
     * Gets an AID as it should be written in an APDU script
     * @param aid
     * @return
     */
    public static String getAIDStringWithLengthForScript(String aid) {
        StringBuffer sb = new StringBuffer();
        byte[] bytes = getAIDAsByteArray(aid);
        sb.append(twoDigitHexWith0x(bytes.length));

        for (byte b : bytes) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(twoDigitHexWith0x(b));
        }

        return sb.toString();
    }

    public static String getAIDStringForScript(String aid) {
        StringBuffer sb = new StringBuffer();

        for (byte b : getAIDAsByteArray(aid)) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(twoDigitHexWith0x(b));
        }

        return sb.toString();
    }

    private static String twoDigitHexWith0x(int value) {
        String str = Integer.toHexString(value);
        if (str.length() < 2) {
            str = "0" + str;
        }
        int l = str.length();
        if (l > 2) {
            str = str.substring(l - 2);
        }
        return "0x" + str;
    }

    /**
     * Checks an AID and throws an exception with a localized message if it
     * is invalid
     * @param aid The AID
     * @throws Exception an exception if the AID is illegal
     */
    public static void checkAID(String aid) throws Exception {
        // AID must start with //aid/

        if (!aid.startsWith(AID_AUTHORITY)) {
            throw new Exception("Invalid AID '" + aid + "'. does not start with //aid/"); //NOI18N
        }

        String aidSubStr = aid.substring(AID_AUTHORITY.length());
        int lastIndexofSlash = aidSubStr.lastIndexOf('/'); //NOI18N

        if (lastIndexofSlash == -1) {
            throw new Exception("Missing PIX in " + aid); //
        }

        if (lastIndexofSlash != aidSubStr.indexOf('/')) {
            throw new Exception("Invalid AID " + aid); //
        }

        String RID = aidSubStr.substring(0, lastIndexofSlash);
        String PIX = aidSubStr.substring(lastIndexofSlash + 1);

        // verify the RID
        if (RID.length() != FIXED_RID_LENGTH) {
            // RID length must be exactly 10
            throw new Exception("Invalid RID " + RID + " in " + aid); //NOI18N
        }

        // verify that it actually is a hexadecimal number
        if (!isValidHexadecimalNumber(RID)) {
            throw new Exception("RID is not a valid hexadecimal number in " + aid); //NOI18N
        }

        // verify the PIX
        if (PIX.equals("-")) {
            // no need to do any further verification. Return
        }

        if (PIX.length() == 0) {
            throw new Exception("Empty PIX in " + aid); //NOI18N
        }

        if (PIX.startsWith("-")) {
            throw new Exception("PIX starts with a \"-\" in  " + aid); //NOI18N
        }

        if ((PIX.length() % 2 != 0) || PIX.length() > MAX_PIX_LENGTH) {
            throw new Exception("Invalid PIX length in  " + aid); //NOI18N
        }

        // verify that it actually is a hexadecimal number
        if (!isValidHexadecimalNumber(PIX)) {
            throw new Exception("PIX is not a valid hexadecimal number in " + aid); //NOI18N
        }
    }

    public static boolean isValidHexadecimalNumber(String number) {
        for (int startIndex = 0; startIndex < number.length(); startIndex += 2) {
            String smallNumber = number.substring(startIndex, startIndex + 2);
            try {
                Integer.parseInt(smallNumber, 16);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public static byte[] getAIDAsByteArray(String aid) {
        String aidSubStr = aid.substring(AID_AUTHORITY.length());
        int lastIndexofSlash = aidSubStr.lastIndexOf('/');
        String RID = aidSubStr.substring(0, lastIndexofSlash);
        String PIX = aidSubStr.substring(lastIndexofSlash + 1);
        byte[] aidArray = new byte[RID.length() / 2 + PIX.length() / 2];
        // copy the RID
        int offset = getByteArrayForString(RID, aidArray, 0);
        // copy the PIX
        getByteArrayForString(PIX, aidArray, offset);
        return aidArray;
    }

    public static int getByteArrayForString(String number, byte[] outputArray, int offset) {
        int length = number.length();
        if ((length % 2 != 0)) {
            throw new NumberFormatException();
        }
        for (int startIndex = 0; startIndex < number.length(); startIndex += 2) {
            String smallNumber = number.substring(startIndex, startIndex + 2);
            outputArray[offset++] = (byte) Integer.parseInt(smallNumber, 16);
        }
        return offset;
    }

    public static String getStringForByteArray(byte[] input) {
        StringBuffer sb = new StringBuffer();
        for (byte num : input) {
            String hex = Integer.toHexString(num & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
        }
        return sb.toString().toUpperCase();
    }
    static byte[] defaultRID;

    /**
     * Get the default (randomly generated or stored in preferences and user-set
     * in the options dialog) RID for new AIDs for applets.
     * @return The default RID portion of an AID as a byte array
     */
    public static byte[] getDefaultRID() {
        synchronized (Utils.class) {
            if (defaultRID != null) {
                return defaultRID;
            }
        }
        String rid = NbPreferences.forModule(Utils.class).get(RID_PREFERENCES_KEY, null);
        if (rid == null) {
            Random r = new Random(System.currentTimeMillis());
            byte[] result = new byte[5];
            r.nextBytes(result);
            String hex = Utils.getStringForByteArray(result);
            NbPreferences.forModule(Utils.class).put(RID_PREFERENCES_KEY, hex);
            synchronized (Utils.class) {
                defaultRID = result;
            }
            return result;
        }
        byte[] b = new byte[rid.length() / 2];
        Utils.getByteArrayForString(rid, b, 0);
        synchronized (Utils.class) {
            defaultRID = b;
        }
        return b;
    }

    public static String getDefaultRIDasString() {
        byte[] b = getDefaultRID();
        return Utils.getStringForByteArray(b);
    }

    public static void setDefaultRID(String rid) {
        if (rid.trim().length() != 10) {
            throw new IllegalArgumentException(NbBundle.getMessage(Utils.class,
                    "Must_be_exactly_5_bytes", rid)); //NOI18N
        }
        byte[] b = new byte[rid.length() / 2];
        try {
            Utils.getByteArrayForString(rid, b, 0);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(NbBundle.getMessage(Utils.class,
                    "Invalid_hexadecimal_number")); //NOI18N
        }
        NbPreferences.forModule(Utils.class).put(RID_PREFERENCES_KEY, rid);
        synchronized (Utils.class) {
            defaultRID = b;
        }
    }

    public static AID generateAppletAID(byte[] RID, String fqn) {
        return AID.generateApplicationAid(getDefaultRID(), fqn);
    }

    public static AID generatePackageAid(String packageName) {
        byte[] RID = getDefaultRID();
        return AID.generatePackageAid(RID, packageName);
    }

    public static AID generateAppletAID(String packageName, String clazz) {
        byte[] RID = getDefaultRID();
        return AID.generateApplicationAid(RID, packageName, clazz);
    }

    public static AID generateInstanceAid(String packageName, String clazz) {
        byte[] RID = getDefaultRID();
        return AID.generateInstanceAid(RID, packageName, clazz);
    }

    public static AID generateRandomAppletAid(String clazz) {
        byte[] RID = getDefaultRID();
        return AID.generateApplicationAid(RID, generateRandomPackageName(), clazz);
    }

    public static String generateRandomPackageName() {
        StringBuilder sb = new StringBuilder();
        Random r = new Random (System.currentTimeMillis());
        for (int i=0; i < 4; i++) {
            int ct = r.nextInt(3) + 3;
            for (int j=0; j < ct; j++) {
                if (j == 0) sb.append ('.');
                int charVal = r.nextInt('z' - 'a') + 'a'; //NOI18N
                sb.append ((char) charVal);
            }
        }
        return sb.toString();
    }

    public static AID generateRandomPackageAid() {
        return AID.generatePackageAid(getDefaultRID(), generateRandomPackageName());
    }

    public static DataObject findPlatformDataObjectNamed(String name) {
        if (name == null || "".equals(name)) { //NOI18N
            return null;
        }
        for (FileObject fo : Utils.sfsFolderForRegisteredJavaPlatforms().getChildren()) {
            if (name.equals(fo.getName())) {
                try {
                    DataObject result = DataObject.find(fo);
                    return result;
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }

    private static final Pattern EXE_ARGS_SPLIT = Pattern.compile(
            "^(.*?)\\s+\\-(\\-*\\w.*)"); //NOI18N
    private static final Pattern EXE_NOARGS_SPLIT = Pattern.compile(
            "^(.*?)\\s(.*)$"); //NOI18N
    private static final Pattern EXE_PREFIX_SPLIT = Pattern.compile("(\\S*" + //NOI18N
            Pattern.quote(File.separator) + ".*)$"); //NOI18N
    static final Pattern ARG_VALUE_SPLIT = Pattern.compile(
            "^\\s*(\\-\\S*)\\s*?((?!\\-)\\S.*?)?(?:\\s\\-|$)"); //NOI18N

    /**
     * Split a command line in the format
     * <pre>
     * someExecutable -lineswitch aaaa -lineswitch -lineswitch bbb...
     * </pre>
     * into an array of strings suitable as individual arguments to pass into
     * Runtime.exec() or similar.  This method is used to resolve the actual
     * array of command line arguments used to create a process, based on
     * input in properties files for Java Card RI card definitions - after
     * using Ant-style substitution to replace templated values with actual
     * values, paths to executables, etc.
     * <p>
     * Any utility method which attempts to do such splitting without resorting
     * to shell quoting is necessarily imperfect.
     * A caveat with this one is that it is not possible to
     * have consecutive arguments that do not begin with a - character; they
     * will be treated as a single argument.  And the final trailing word
     * following an argument prefixed with -, if separated by a space, will
     * be treated as a separate argument.
     *
     * This is an issue for file paths containing spaces - for
     * example, if you have
     * <pre>
     * -Dfoo=C:\Program Files\NetBeans\NetBeans 6.8\Path\To\Something
     * </pre>
     * you will end up with two command-line arguments,
     * <code>-Dfoo=C:\Program Files\NetBeans\NetBeans</code> and
     * <code>6.8\Path\To\Something</code>.
     * <p>
     * To solve this issue, surround any content that must always be
     * treated as a single atomic argument should be surrounded by <code>{{{</code>
     * and <code>}}}</code>.  For example, the following command-line:
     * <blockquote><code>
     * javacard.device.debugger.cmdline=${java.home}/bin/java
     * -classpath ${javacard.debug.proxy.classpath}
     * {{{-Djc.home=${javacard.ri.home}}}} com.sun.javacard.debugproxy.Main {{{debug}}}
     * --listen ${javacard.device.proxy2idePort}
     * --remote ${javacard.device.host}:${javacard.device.proxy2cjcrePort}
     * --classpath ${class.path}
     * </code></blockquote>
     * has two escaped elements.  The first is a single line-switch(from the
     * perspective that anything that starts with a - character is a line
     * switch) which ends in a file path.  To guarantee that the text following
     * the final space character is not lopped off and treated as an argument
     * to this line-switch, we wrap it in <code>{{{</code>
     * and <code>}}}</code>.  The same is true for <code>{{{debug}}}</code> &mdash;
     * this follows something which is not a line switch (no - character), and
     * would otherwise be treated as part of the argument beginning
     * &quot;com.sun.javacard&quot;.  To guarantee that it is not lumped together
     * with the class name, we escape it as well.
     *
     * @param s A command line
     * @return The command line split into tokens, taking into account
     * things like spaces in file paths, dash characters in file paths, etc.
     */
    public static final String[] shellSplit(String s) {
        List<String> result = new ArrayList<String>();
        Matcher m = EXE_ARGS_SPLIT.matcher(s);
        String exePart;
        String argsPart;
        if (m.find()) {
            exePart = m.group(1).trim();
            if (exePart.trim().length() == 0) {
                exePart = null;
            }
            argsPart = "-" + m.group(2).trim(); //NOI18N
        } else {
            m = EXE_NOARGS_SPLIT.matcher(s);
            if (m.find()) {
                exePart = m.group(1);
                if (exePart.trim().length() == 0) {
                    exePart = null;
                }
                argsPart = m.group(2);
            } else {
                exePart = null;
                argsPart = s;
            }
        }
        if (exePart != null) {
            m = EXE_PREFIX_SPLIT.matcher(exePart);
            String exePrefix = null;
            if (m.find()) {
                exePrefix = exePart.substring(0, m.start(1));
                result.addAll(Arrays.asList(exePrefix.split("\\s+"))); //NOI18N
                result.add(m.group(1));
            } else {
                result.add(exePart.trim());
            }
        }
        if (!splitArgs(argsPart, result)) {
            result.add(argsPart);
        }
        result = restoreAtomicArguments (s, result);
        List<String> fixedUp = new ArrayList<String>();
        for (Iterator<String> i=result.iterator(); i.hasNext();) {
            //XXX figure out where these are coming from - not AtomicElementRepairer
            String str = i.next();
            if ("".equals(str) || str == null) { //NOI18N
                continue;
            }
            fixedUp.add (str.trim());
        }
        return fixedUp.toArray(new String[fixedUp.size()]);
    }

    private static List<String> restoreAtomicArguments (String s, List<String> l) {
        if (l.size() == 0) {
            return l;
        }
        int startIx = s.indexOf(AtomicElementRepairer.START_DELIMITER); //NOI18N
        if (!(startIx > 0 && s.indexOf(AtomicElementRepairer.END_DELIMITER) > startIx)) { //NOI18N
            return l;
        }
        return new AtomicElementRepairer(l).restoreAtomicItems();
    }

    /**
     * Method is only public until unit tests are moved
     * @param argsPart
     * @param result
     * @return
     */
    static boolean splitArgs(String argsPart, List<String> result) {
        Matcher m = ARG_VALUE_SPLIT.matcher(argsPart);
        int end = -1;
        boolean res = false;
        while (m.find()) {
            res = true;
            for (int i = 1; i <= m.groupCount(); i++) {
                String arg = m.group(i);
                if (arg != null) { //Yes, it can be null
                    result.add(arg);
                }
                end = m.end();
            }
        }
        if (res && end != -1 && end < argsPart.length() - 1) {
            String remainder = argsPart.substring(end - 1);
            if (!splitArgs(remainder, result)) {
                result.add(remainder);
            }
        }
        return res;
    }
}
