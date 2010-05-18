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

/*
 * WindowsRegistryReader.java
 *
 * Created on August 24, 2005, 2:49 PM
 *
 */
package org.netbeans.modules.mobility.cldcplatform.wizard;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.netbeans.spi.mobility.cldcplatform.CustomCLDCPlatformConfigurator;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author Adam Sotona
 */
public class WindowsRegistryReader {
    
    private static String vendorsList = "Sun|Nokia|Motorola|Siemens|Ericsson|Samsung|Vodafone|Sprint"; //NOI18N
    
    public static final int HKEY_CURRENT_USER = 0x80000001;
    public static final int HKEY_LOCAL_MACHINE = 0x80000002;
    
    public static Set<File> traverseRegistry() {
        final Set<File> files = new HashSet<File>();
        if (ok && Utilities.isWindows()) {
            traverseRegistry(HKEY_LOCAL_MACHINE, files);
            traverseRegistry(HKEY_CURRENT_USER, files);
        }
        return files;
    }
    
    private WindowsRegistryReader() {
        //To avoid instantiation
    }
    
    private static void traverseRegistry(final int hKey, final Set<File> files) {
        final StringBuffer sb = new StringBuffer(vendorsList);
        for ( CustomCLDCPlatformConfigurator cfg : Lookup.getDefault().lookup(new Lookup.Template<CustomCLDCPlatformConfigurator>(CustomCLDCPlatformConfigurator.class)).allInstances() ) {
            final String vend = cfg.getRegistryProviderName();
            if (vend != null) sb.append('|').append(vend);
        }
        final Pattern vendors = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
        try {
            final String children[] = WindowsRegistryReader.getChildren(hKey, "Software");//NOI18N
            if (children != null) for (int i=0; i<children.length; i++) if (children[i] != null && vendors.matcher(children[i]).find()) traverseRegistry(hKey, "Software\\"+children[i], files);
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) throw (ThreadDeath)t; //do not block ThreadDeath other Throwable ignore
        }
    }
    
    
    private static void traverseRegistry(final int hKey, final String key, final Set<File> files) {
        if (key.length() > 256) return;
        try {
            final String keys[] = WindowsRegistryReader.getKeys(hKey, key);
            if (keys != null) for (int i=0; i<keys.length; i++) if (keys[i] != null) try {
                final String value = WindowsRegistryReader.get(hKey, key, keys[i]);
                File f;
                if (value != null && value.length() > 3 && (f = new File(value)).isDirectory() && f.isAbsolute()) files.add(f);
            } catch (Exception ex) {}
            final String children[] = WindowsRegistryReader.getChildren(hKey, key);
            if (children != null) for (int i=0; i<children.length; i++) if (children[i] != null) traverseRegistry(hKey, key + '\\' + children[i], files);
        } catch (Exception e) {}
    }
    
    public static String get(final int hKey, final String subKey, final String javaName) {
        final int nativeHandle = WindowsRegOpenKey(hKey, toByteArray(subKey), KEY_QUERY_VALUE)[NATIVE_HANDLE];
        if (nativeHandle == 0) return null;
        try {
            final Object resultObject =  WindowsRegQueryValueEx(nativeHandle, toByteArray(javaName));
            return resultObject == null ? null : toJavaValueString((byte[]) resultObject);
        } finally {
            WindowsRegCloseKey(nativeHandle);
        }
    }
    
    public static String[] getKeys(final int hKey, final String subKey) {
        final int nativeHandle = WindowsRegOpenKey(hKey, toByteArray(subKey), KEY_QUERY_VALUE)[NATIVE_HANDLE];
        if (nativeHandle == 0) return null;
        try {
            final int[] result =  WindowsRegQueryInfoKey(nativeHandle);
            if (result[ERROR_CODE] != 0) return null;
            final int maxValueNameLength = result[MAX_VALUE_NAME_LENGTH];
            final int valuesNumber = result[VALUES_NUMBER];
            if (valuesNumber == 0) return new String[0];
            String[] valueNames = new String[valuesNumber];
            for (int i = 0; i < valuesNumber; i++) valueNames[i] = byteArrayToString(WindowsRegEnumValue(nativeHandle, i, maxValueNameLength+1));
            return valueNames;
        } finally {
            WindowsRegCloseKey(nativeHandle);
        }
    }
    
    public static String[] getChildren(final int hKey, final String subKey) {
        final int nativeHandle = WindowsRegOpenKey(hKey, toByteArray(subKey), KEY_QUERY_VALUE | KEY_ENUMERATE_SUB_KEYS)[NATIVE_HANDLE];
        if (nativeHandle == 0) return null;
        try {
            final int[] result =  WindowsRegQueryInfoKey(nativeHandle);
            if (result[ERROR_CODE] != 0) return null;
            final int maxKeyLength = result[MAX_KEY_LENGTH];
            final int subKeysNumber = result[SUBKEYS_NUMBER];
            if (subKeysNumber == 0) return new String[0];
            String[] children = new String[subKeysNumber];
            for (int i = 0; i < subKeysNumber; i++) children[i] = byteArrayToString(WindowsRegEnumKeyEx(nativeHandle, i, maxKeyLength+1));
            return children;
        } finally {
            WindowsRegCloseKey(nativeHandle);
        }
    }
    
    /* Constants used to interpret returns of native functions    */
    private static final int NATIVE_HANDLE = 0;
    private static final int ERROR_CODE = 1;
    private static final int SUBKEYS_NUMBER = 0;
    private static final int VALUES_NUMBER = 2;
    private static final int MAX_KEY_LENGTH = 3;
    private static final int MAX_VALUE_NAME_LENGTH = 4;
    
    /* Windows security masks */
    private static final int KEY_QUERY_VALUE = 1;
    private static final int KEY_ENUMERATE_SUB_KEYS = 8;
    
    
    private static Class WindowsPreferences;
    private static boolean ok = true;
    
    private static Method findMethod(final String name, final Class[] arguments) {
        if (ok) try {
            synchronized (vendorsList) {
                if (WindowsPreferences == null) WindowsPreferences = Class.forName("java.util.prefs.WindowsPreferences"); //NOI18N
            }
            final Method m = WindowsPreferences.getDeclaredMethod(name, arguments);
            m.setAccessible(true);
            return m;
        } catch (Exception e) {
            ok = false;
        }
        return null;
    }
    
    private static Method WindowsRegOpenKey = findMethod("WindowsRegOpenKey", new Class[]{int.class, byte[].class, int.class}); //NOI18N
    
    private static Method WindowsRegCloseKey = findMethod("WindowsRegCloseKey", new Class[]{int.class}); //NOI18N
    
    private static Method WindowsRegQueryValueEx = findMethod("WindowsRegQueryValueEx", new Class[]{int.class, byte[].class}); //NOI18N
    
    private static Method WindowsRegQueryInfoKey = findMethod("WindowsRegQueryInfoKey", new Class[]{int.class}); //NOI18N
    
    private static Method WindowsRegEnumKeyEx = findMethod("WindowsRegEnumKeyEx", new Class[]{int.class, int.class, int.class}); //NOI18N
    
    private static Method WindowsRegEnumValue = findMethod("WindowsRegEnumValue", new Class[]{int.class, int.class, int.class}); //NOI18N
    
    private static int[] WindowsRegOpenKey(final int hKey, final byte[] subKey, final int securityMask) {
        try {
            return (int[]) WindowsRegOpenKey.invoke(null, new Object[] {new Integer(hKey), subKey, new Integer(securityMask)});
        } catch (Exception e) {
            throw new UnsupportedOperationException(e.getLocalizedMessage());
        }
    }
    
    private static int WindowsRegCloseKey(final int hKey) {
        try {
            return ((Integer)WindowsRegCloseKey.invoke(null, new Object[] {new Integer(hKey)})).intValue();
        } catch (Exception e) {
            throw new UnsupportedOperationException(e.getLocalizedMessage());
        }
    }
    
    private static byte[] WindowsRegQueryValueEx(final int hKey, final byte[] valueName) {
        try {
            return (byte[]) WindowsRegQueryValueEx.invoke(null, new Object[] {new Integer(hKey), valueName});
        } catch (Exception e) {
            throw new UnsupportedOperationException(e.getLocalizedMessage());
        }
    }
    
    private static int[] WindowsRegQueryInfoKey(final int hKey) {
        try {
            return (int[]) WindowsRegQueryInfoKey.invoke(null, new Object[] {new Integer(hKey)});
        } catch (Exception e) {
            throw new UnsupportedOperationException(e.getLocalizedMessage());
        }
    }
    
    private static byte[] WindowsRegEnumKeyEx(final int hKey, final int subKeyIndex, final int maxKeyLength) {
        try {
            return (byte[]) WindowsRegEnumKeyEx.invoke(null, new Object[] {new Integer(hKey), new Integer(subKeyIndex), new Integer(maxKeyLength)});
        } catch (Exception e) {
            throw new UnsupportedOperationException(e.getLocalizedMessage());
        }
    }
    
    private static byte[] WindowsRegEnumValue(final int hKey, final int valueIndex, final int maxValueNameLength) {
        try {
            return (byte[]) WindowsRegEnumValue.invoke(null, new Object[] {new Integer(hKey), new Integer(valueIndex), new Integer(maxValueNameLength)});
        } catch (Exception e) {
            throw new UnsupportedOperationException(e.getLocalizedMessage());
        }
    }
    
    
    private static String toJavaValueString(final byte[] windowsNameArray) {
        final String windowsName = byteArrayToString(windowsNameArray);
        final StringBuffer javaName = new StringBuffer();
        char ch;
        for (int i = 0; i < windowsName.length(); i++){
            if ((ch = windowsName.charAt(i)) == '/') {
                char next = ' ';
                
                if (windowsName.length() > i + 1 &&
                        (next = windowsName.charAt(i + 1)) == 'u') {
                    if (windowsName.length() < i + 6){
                        break;
                    } 
                    ch = (char)Integer.parseInt
                            (windowsName.substring(i + 2, i + 6), 16);
                    i += 5;
                } else
                    if ((windowsName.length() > i + 1) &&
                        ((windowsName.charAt(i+1)) >= 'A') && (next <= 'Z')) {
                    ch = next;
                    i++;
                    } else  if ((windowsName.length() > i + 1) &&
                        (next == '/')) {
                    ch = '\\';
                    i++;
                    }
            } else if (ch == '\\') {
                ch = '/';
            }
            javaName.append(ch);
        }
        return javaName.toString();
    }
    
    private static byte[] toByteArray(final String str) {
        byte[] result = new byte[str.length()+1];
        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        result[str.length()] = 0;
        return result;
    }
    
    private static String byteArrayToString(final byte[] array) {
        if (array == null) return null;
        return new String(array, 0, array.length - 1);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) throws Exception {
        System.out.println(traverseRegistry());
    }
    
}
