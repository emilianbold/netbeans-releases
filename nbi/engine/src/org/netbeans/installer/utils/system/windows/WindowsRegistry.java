/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.utils.system.windows;

/**
 *
 * @author Dmitry Lipin
 */
public class WindowsRegistry {
    private static WindowsRegistry registry = new WindowsRegistry();
    
    
    /** Private constructor
     *
     */
    private WindowsRegistry() {
    }
    
    /**
     * Get instance of Win32Registry.
     * @return Win32Registry
     */
    public static WindowsRegistry getInstance() {
        return registry;
    };
    
    /** Checks if the specified value exists in the registry.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param value The specified value
     * @return <i>true</i> if the specified value exists, <i>false</i> otherwise
     */
    public native boolean isValueExists(int registrySection, String key, String value);
    
    /** Checks if the specified key exists in the registry.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @return <i>true</i> if the specified key exists, <i>false</i> otherwise
     */
    public native boolean isKeyExists(int registrySection, String key) ;
    
    /** Checks if the specified value exists in the registry.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param value The specified value
     * @return <i>true</i> if the specified value exists, <i>false</i> otherwise
     */
    public native boolean isKeyEmpty(int registrySection, String key) ;
    
    /** Delete the specified key exists in the registry.
     * Note that if the key contains subkeys then it would not be deleted.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @return <i>true</i> if the specified key was deleted, <i>false</i> otherwise
     */
    public boolean deleteKey(int registrySection, String key) {
        String keyString = key;
        int index = keyString.lastIndexOf("\\");
        if(index==keyString.length()) {
            keyString = keyString.substring(0,index - 1);
        }
        index = keyString.lastIndexOf("\\");
        
        String parent = key.substring(0,index);
        String child = key.substring(index + 1);
        
        return deleteKey(registrySection,parent,child);
    }
    
    /** Delete the specified key exists in the registry.
     * @param registrySection The section of the registry
     * @param parentKey The specified parent key
     * @param childKey The specified child key
     * @return <i>true</i> if the specified key was deleted, <i>false</i> otherwise
     */
    public native boolean deleteKey(int registrySection, String parentKey, String childKey);
    
    /** Delete the specified value exists in the registry.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param value The specified value
     * @return <i>true</i> if the specified value was deleted, <i>false</i> otherwise
     */
    public native boolean deleteValue(int registrySection, String key,String value);
    
    /** Returns the type of the value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param value The specified value
     * @return The possible values are:<br>
     *
     * <code>REG_NONE</code><br>
     * <code>REG_SZ</code><br>
     * <code>REG_EXPAND_SZ</code><br>
     * <code>REG_BINARY</code><br>
     * <code>REG_DWORD</code>=<code>REG_DWORD_LITTLE_ENDIAN</code><br>
     * <code>REG_DWORD_BIG_ENDIAN</code><br>
     * <code>REG_LINK</code><br>
     * <code>REG_MULTI_SZ</code><br>
     * <code>REG_RESOURCE_LIST</code><br>
     * <code>REG_FULL_RESOURCE_DESCRIPTOR</code><br>
     * <code>REG_RESOURCE_REQUIREMENTS_LIST</code><br>
     * <code>REG_QWORD</code>=<code>REG_QWORD_LITTLE_ENDIAN</code>
     */
    public native int getValueType(int registrySection,String key,String value);
    
    /** Get the number of the subkeys of the specified key.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @return If the key doesn`t exist or can`t be accessed then return -1.
     * <br>Otherwise return the number of subkeys
     */
    public native int getSubKeysNumber(int registrySection, String key);
    
    /** Get the number of the values of the specified key.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @return If the key doesn`t exist or can`t be accessed then return -1.
     * <br>Otherwise return the number of values
     */
    public native int getValuesInKeyNumber(int registrySection, String key);
    
    /** Get the array of values names of the specified key.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @return If the key doesn`t exist or can`t be accessed then return <i>null</i>
     * <br>Otherwise return the array of value names
     */
    public native String[] getValueNames  (int registrySection, String key);
    
    /** Get the array of subkey names of the specified key.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @return If the key doesn`t exist or can`t be accessed then return <i>null</i>
     * <br>Otherwise return the array of subkey names
     */
    public native String[] getSubkeyNames(int registrySection, String key);
    
    /** Create the new key in the registry.
     * @param registrySection The section of the registry
     * @param key The specified parent key
     * @return <i>true</i> if the key was successfully created,
     * <br> <i>false</i> otherwise
     */
    public native boolean createKey(int registrySection, String parent, String child);
    
    /** Set string value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @param value The specified value of the <code>valueName</code>
     * @param expandable
     *      If <code>expandable</code> is <i>true</i> then the type would be
     *       <code>REG_EXPAND_SZ</code> or <code>REG_SZ</code> otherwise
     * @return <i>true</i> if the value was successfully set
     * <br> <i>false</i> otherwise
     */
    public native boolean setStringValue(int registrySection, String key, String valueName, String value, boolean expandable);
    
    
    /** Set REG_DWORD value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @param value The specified value of the <code>valueName</code>
     * @return <i>true</i> if the value was successfully set
     * <br> <i>false</i> otherwise
     */
    public native boolean set32BitValue  (int registrySection, String key, String valueName, int value);
    
    /** Set REG_MULTI_SZ value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @param value The specified value of the <code>valueName</code>
     * @return <i>true</i> if the value was successfully set
     * <br> <i>false</i> otherwise
     */
    public native boolean setMultiStringValue(int registrySection, String key, String valueName, String [] value);
    
    
    /** Set binary (REG_BINARY) value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @param value The specified value of the <code>valueName</code>
     * @return <i>true</i> if the value was successfully set
     * <br> <i>false</i> otherwise
     */
    public native boolean setBinaryValue(int registrySection, String key, String valueName, byte [] value);
    
    
    
    /** Get string value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @param expandable
     *      If <code>expandable</code> is <i>true</i> and
     *      the type of the value is REG_EXPAND_SZ the value would be expanded
     * @return The value of the valueName, <i>null</i> in case of any error
     */
    public native String   getStringValue(int registrySection, String key, String valueName, boolean expand);
    
    /** Get integer value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @return The value of the valueName, <i>-1</i> in case of any error
     */
    public native int      get32BitValue(int registrySection, String key, String valueName);
    
    /** Get the array of strings of the specified value
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @return The multri-string value of the valueName, <i>null</i> in case of any error
     */
    public native String[] getMultiStringValue(int registrySection, String key, String valueName);
    
    /** Get binary value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @return The binary value of the valueName, <i>null</i> in case of any error
     */
    
    public native byte  [] getBinaryValue(int registrySection, String key, String valueName);
    
    public boolean setStringValue(int registrySection, String key, String valueName, String value) {
        return setStringValue(registrySection, key, valueName, value, false);
    }
    
    public String getStringValue(int registrySection, String key, String valueName) {
        return getStringValue(registrySection, key, valueName, false);
    }
    
    public static final int HKEY_CLASSES_ROOT        = 0;
    public static final int HKEY_CURRENT_USER        = 1;
    public static final int HKEY_LOCAL_MACHINE       = 2;
    public static final int HKEY_USERS               = 3;
    public static final int HKEY_CURRENT_CONFIG      = 4;
    
    public static final int HKEY_DYN_DATA            = 5;
    public static final int HKEY_PERFORMANCE_DATA    = 6;
    public static final int HKEY_PERFORMANCE_NLSTEXT = 7;
    public static final int HKEY_PERFORMANCE_TEXT    = 8;
    
    public static final String WR_SEPARATOR = "\\";
    
    public static final int REG_NONE                       = 0;
    public static final int REG_SZ                         = 1;
    public static final int REG_EXPAND_SZ                  = 2;
    public static final int REG_BINARY                     = 3;
    public static final int REG_DWORD_LITTLE_ENDIAN        = 4;
    public static final int REG_DWORD                      = 4;
    public static final int REG_DWORD_BIG_ENDIAN           = 5;
    public static final int REG_LINK                       = 6;
    public static final int REG_MULTI_SZ                   = 7;
    public static final int REG_RESOURCE_LIST              = 8;
    public static final int REG_FULL_RESOURCE_DESCRIPTOR   = 9;
    public static final int REG_RESOURCE_REQUIREMENTS_LIST = 10;
    public static final int REG_QWORD_LITTLE_ENDIAN        = 11;
    public static final int REG_QWORD                      = 11;
}
