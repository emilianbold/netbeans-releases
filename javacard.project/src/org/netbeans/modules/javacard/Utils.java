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
package org.netbeans.modules.javacard;

import com.sun.javacard.AID;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javacard.api.JavacardPlatform;
import org.netbeans.modules.javacard.constants.CommonSystemFilesystemPaths;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.netbeans.modules.javacard.platform.BrokenJavacardPlatform;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.*;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public final class Utils {
    private static final String RID_PREFERENCES_KEY = "RID"; //NOI18N

    public static DataObject findDeviceForPlatform(String platform, String device) {
        FileObject deviceFolder = sfsFolderForDeviceConfigsForPlatformNamed(platform, false);
        if (deviceFolder != null) {
            FileObject child = deviceFolder.getFileObject(device, JCConstants.JAVACARD_DEVICE_FILE_EXTENSION);
            if (child != null) {
                try {
                    return DataObject.find(child);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }

    public static FileObject folderForInvalidDeviceConfigsForPlatformNamed(String platformName, String invalidDeviceName) {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        try {
            FileSystem sfs = FileUtil.getConfigRoot().getFileSystem();
            MultiFileSystem mfs = new MultiFileSystem(new FileSystem[]{sfs, fs});
            String rootPath = CommonSystemFilesystemPaths.SFS_DEVICE_CONFIGS_ROOT;
            FileObject fld = FileUtil.createFolder(fs.getRoot(), rootPath);
            fld = FileUtil.createFolder(fld, platformName);
            FileUtil.createData(fld, invalidDeviceName +
                    '.' + JCConstants.JAVACARD_DEVICE_FILE_EXTENSION);
            return mfs.getRoot().getFileObject(fld.getPath());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

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

    public static FileObject sfsFolderForDeviceEepromsForPlatformNamed(String name, boolean create) {
        String rootPath = CommonSystemFilesystemPaths.SFS_DEVICE_EEPROMS_ROOT;
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

    public static File eepromFileForDevice(JavacardPlatform platform, String deviceName, boolean create) {
        String realName = platform.getSystemName();
        if (realName == null) {
            for (FileObject fo : sfsFolderForRegisteredJavaPlatforms().getChildren()) {
                try {
                    DataObject dob = DataObject.find(fo);
                    JavacardPlatform impl = dob.getLookup().lookup(JavacardPlatform.class);
                    if (impl != null && impl.equals(platform)) {
                        realName = dob.getName();
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        if (realName == null) {
            realName = platform.getDisplayName();
        }
        return eepromFileForDevice(realName, deviceName, create);
    }

    public static FileObject sfsFolderForRegisteredJavaPlatforms() {
        FileObject fld = FileUtil.getConfigFile(CommonSystemFilesystemPaths.SFS_JAVA_PLATFORMS_FOLDER); //NOI18N
        return fld;
    }

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

    public static DataObject findPlatformDataObjectNamed(String name) {
        if (name == null || "".equals(name)) {
            return null;
        }
        for (FileObject fo : sfsFolderForRegisteredJavaPlatforms().getChildren()) {
            if (name.equals(fo.getName())) {
                try {
                    return DataObject.find(fo);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return createFakeJavacardPlatform(name);
    }

    public static DataObject createFakeJavacardPlatform(String name) {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        MultiFileSystem mfs;
        try {
            mfs = new MultiFileSystem(new FileSystem[]{FileUtil.getConfigRoot().getFileSystem(), fs});
            FileObject fo = FileUtil.createData(fs.getRoot(),
                    CommonSystemFilesystemPaths.SFS_JAVA_PLATFORMS_FOLDER + '/' +
                    name + '.' + JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION);
            fo = mfs.getRoot().getFileObject(fo.getPath());
            return DataObject.find(fo);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public static JavacardPlatform findPlatformNamed(String name) {
        DataObject dob = findPlatformDataObjectNamed(name);
        JavacardPlatform result = null;
        if (dob != null) {
            result = dob.getLookup().lookup(JavacardPlatform.class);
        }
        if (result == null) {
            result = new BrokenJavacardPlatform(name);
        }
        return result;
    }

    /**
     * Finds an appropriate directory to root a filechooser on when looking
     * for a runtime.  The JFileChooser default is the user home dir,
     * which is almost certainly not what we want on windows.
     *
     * @return A directory to start with when searching for a runtime
     */
    public static File getJcdkSearchRoot() {
        File defaultDir = null;
        if (Utilities.isWindows()) {
            for (File file : File.listRoots()) {
                if (!FileSystemView.getFileSystemView().isFloppyDrive(file)) {
                    defaultDir = file;
                    File programFiles = new File(defaultDir, "Program Files"); //NOI18N
                    if (programFiles.exists() && programFiles.isDirectory()) {
                        defaultDir = programFiles;
                    }
                    break;
                }
            }
            if (defaultDir == null) {
                defaultDir = FileSystemView.getFileSystemView().getHomeDirectory();
            }
        } else {
            defaultDir = FileSystemView.getFileSystemView().getRoots()[0];
        }
        return defaultDir;
    }

    private static String stripPeriods(String pkg) {
        char[] c = pkg.toCharArray();
        StringBuilder res = new StringBuilder();
        for (char ch : c) {
            if (ch != '.') {
                res.append(ch);
            }
        }
        return res.toString();
    }

    private Utils() {
    }

    public static FileObject findBuildXml(Project project) {
        return project.getProjectDirectory().getFileObject(
                GeneratedFilesHelper.BUILD_XML_PATH);
    }

    public static URL getBuildXslTemplate() {
        FileObject file = FileUtil.getConfigFile(CommonSystemFilesystemPaths.SFS_PATH_TO_BUILD_XSL);
        Parameters.notNull(CommonSystemFilesystemPaths.SFS_PATH_TO_BUILD_XSL + " missing " + //NOI18N
                "from system filesystem", file); //NOI18N
        return URLMapper.findURL(file, URLMapper.INTERNAL);
    }

    public static URL getBuildImplXslTemplate() {
        FileObject file = FileUtil.getConfigFile(CommonSystemFilesystemPaths.SFS_PATH_TO_BUILD_IMPL_XSL);
        Parameters.notNull(CommonSystemFilesystemPaths.SFS_PATH_TO_BUILD_IMPL_XSL + " missing " + //NOI18N
                "from system filesystem", file); //NOI18N
        return URLMapper.findURL(file, URLMapper.INTERNAL);
    }

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

    public static boolean saveWebResource(String address, String localFileName) throws Exception {
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        boolean done = false;
        try {
            URL url = new URL(address);
            out = new BufferedOutputStream(
                    new FileOutputStream(localFileName));
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[10240]; // 10K buffer

            int numRead;
            long numWritten = 0;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
                numWritten += numRead;
            }
            done = true;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                //
            }
        }
        return done;
    }
    private static final int FIXED_RID_LENGTH = 10;
    private static final int MAX_PIX_LENGTH = 22;
    public static final String AID_AUTHORITY = "//aid/";

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

    public static void checkAID(String aid) throws Exception {
        // AID must start with //aid/

        if (!aid.startsWith(AID_AUTHORITY)) {
            throw new Exception("Invalid AID '" + aid + "'. does not start with //aid/");
        }

        String aidSubStr = aid.substring(AID_AUTHORITY.length());
        int lastIndexofSlash = aidSubStr.lastIndexOf('/');

        if (lastIndexofSlash == -1) {
            throw new Exception("Missing PIX in " + aid);
        }

        if (lastIndexofSlash != aidSubStr.indexOf('/')) {
            throw new Exception("Invalid AID " + aid);
        }

        String RID = aidSubStr.substring(0, lastIndexofSlash);
        String PIX = aidSubStr.substring(lastIndexofSlash + 1);

        // verify the RID
        if (RID.length() != FIXED_RID_LENGTH) {
            // RID length must be exactly 10
            throw new Exception("Invalid RID " + RID + " in " + aid);
        }

        // verify that it actually is a hexadecimal number
        if (!isValidHexadecimalNumber(RID)) {
            throw new Exception("RID is not a valid hexadecimal number in " + aid);
        }

        // verify the PIX
        if (PIX.equals("-")) {
            // no need to do any further verification. Return
        }

        if (PIX.length() == 0) {
            throw new Exception("Empty PIX in " + aid);
        }

        if (PIX.startsWith("-")) {
            throw new Exception("PIX starts with a \"-\" in  " + aid);
        }

        if ((PIX.length() % 2 != 0) || PIX.length() > MAX_PIX_LENGTH) {
            throw new Exception("Invalid PIX length in  " + aid);
        }

        // verify that it actually is a hexadecimal number
        if (!isValidHexadecimalNumber(PIX)) {
            throw new Exception("PIX is not a valid hexadecimal number in " + aid);
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

    public static byte[] getDefaultRID() {
        synchronized (AID.class) {
            if (defaultRID != null) {
                return defaultRID;
            }
        }
        String rid = NbPreferences.forModule(AID.class).get(RID_PREFERENCES_KEY, null);
        if (rid == null) {
            Random r = new Random(System.currentTimeMillis());
            byte[] result = new byte[5];
            r.nextBytes(result);
            String hex = Utils.getStringForByteArray(result);
            NbPreferences.forModule(AID.class).put(RID_PREFERENCES_KEY, hex);
            synchronized (AID.class) {
                defaultRID = result;
            }
            return result;
        }
        byte[] b = new byte[rid.length() / 2];
        Utils.getByteArrayForString(rid, b, 0);
        synchronized (AID.class) {
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
            throw new IllegalArgumentException(NbBundle.getMessage(AID.class,
                    "Must_be_exactly_5_bytes", rid)); //NOI18N
        }
        byte[] b = new byte[rid.length() / 2];
        try {
            Utils.getByteArrayForString(rid, b, 0);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(NbBundle.getMessage(AID.class,
                    "Invalid_hexadecimal_number")); //NOI18N
        }
        NbPreferences.forModule(AID.class).put(RID_PREFERENCES_KEY, rid);
        synchronized (AID.class) {
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
        byte[] RID = getDefaultRID();
        return AID.generatePackageAid(getDefaultRID(), generateRandomPackageName());
    }
}
