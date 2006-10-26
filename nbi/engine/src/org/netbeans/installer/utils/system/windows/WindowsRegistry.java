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

import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;

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
    private void logStartJNI(Object args[]) {
        LogManager.getInstance().log(ErrorLevel.DEBUG,"[JNI] [" +
                new Exception().getStackTrace()[1].getMethodName() +
                "] Starting with args: {" +
                StringUtils.getInstance().asString(args,"; ") + "}");
    }
    private void logEndJNI() {
        LogManager.getInstance().log(ErrorLevel.DEBUG,"[JNI] [" +
                new Exception().getStackTrace()[1].getMethodName() +
                "] ... end");
    }
    private void logErrJNI(Error ex) {
        LogManager.getInstance().log(ErrorLevel.DEBUG,"[JNI] [" +
                new Exception().getStackTrace()[1].getMethodName() +
                "] !! Error !!");
        LogManager.getInstance().log(ErrorLevel.DEBUG, ex);
    }
    /** Checks if the specified value exists in the registry.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param value The specified value
     * @return <i>true</i> if the specified value exists, <i>false</i> otherwise
     */
    public boolean isValueExists(int registrySection, String key, String value) {
        logStartJNI(new Object [] {registrySection,key,value});
        boolean result = false;
        try {
            result = isValueExists0(registrySection,key,value);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native boolean isValueExists0(int registrySection, String key, String value);
    
    /** Checks if the specified key exists in the registry.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @return <i>true</i> if the specified key exists, <i>false</i> otherwise
     */
    public boolean isKeyExists(int registrySection, String key) {
        logStartJNI(new Object [] {registrySection,key});
        boolean result = false;
        try {
            result = isKeyExists0(registrySection,key);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native boolean isKeyExists0(int registrySection, String key) ;
    
    /** Checks if the specified value exists in the registry.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param value The specified value
     * @return <i>true</i> if the specified value exists, <i>false</i> otherwise
     */
    public boolean isKeyEmpty(int registrySection, String key) {
        logStartJNI(new Object [] {registrySection,key});
        boolean result = false;
        try {
            result = isKeyEmpty0(registrySection,key);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
            throw ex;
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native boolean isKeyEmpty0(int registrySection, String key) ;
    private String getParentKey(String key) {
        String keyString = key;
        String parent = null;
        if(keyString==null) {
            return null;
        }
        int index = keyString.lastIndexOf("\\");
         if(index!=-1) {
            if(index==keyString.length()) {
                keyString = keyString.substring(0,index - 1);
            }
            index = keyString.lastIndexOf("\\");
            parent = key.substring(0,index);            
        } else {
            parent = null;            
        }
        return parent;
    }
    private String getChildKey(String key) {
        String keyString = key;        
        String child = null;
        if(keyString==null) {
            return null;
        }
        int index = keyString.lastIndexOf("\\");
        if(index!=-1) {
            if(index==keyString.length()) {
                keyString = keyString.substring(0,index - 1);
            }
            index = keyString.lastIndexOf("\\");            
            child = key.substring(index + 1);
        } else {
            
            child = key;
        }
        return child;
    }
    /** Delete the specified key exists in the registry.
     * Note that if the key contains subkeys then it would not be deleted.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @return <i>true</i> if the specified key was deleted, <i>false</i> otherwise
     */
    public boolean deleteKey(int registrySection, String key) {        
        return (key == null) ? 
            false :
            deleteKey(registrySection,getParentKey(key),getChildKey(key));
    }
    
    /** Delete the specified key exists in the registry.
     * @param registrySection The section of the registry
     * @param parentKey The specified parent key
     * @param childKey The specified child key
     * @return <i>true</i> if the specified key was deleted, <i>false</i> otherwise
     */
    public boolean deleteKey(int registrySection, String parentKey, String childKey) {
        logStartJNI(new Object [] {registrySection,parentKey,childKey});
        boolean result = false;
        try {
            result = deleteKey0(registrySection,parentKey,childKey);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native boolean deleteKey0(int registrySection, String parentKey, String childKey);
    
    /** Delete the specified value exists in the registry.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param value The specified value
     * @return <i>true</i> if the specified value was deleted, <i>false</i> otherwise
     */
    public boolean deleteValue(int registrySection, String key,String value) {
        logStartJNI(new Object [] {registrySection,key,value});
        boolean result = false;
        try {
            result = deleteValue0(registrySection,key,value);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native boolean deleteValue0(int registrySection, String key,String value);
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
    public int getValueType(int registrySection,String key,String value) {
        logStartJNI(new Object [] {registrySection,key,value});
        int result = REG_NONE;
        try {
            result = getValueType0(registrySection,key,value);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native int getValueType0(int registrySection,String key,String value);
    
    /** Get the number of the subkeys of the specified key.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @return If the key doesn`t exist or can`t be accessed then return -1.
     * <br>Otherwise return the number of subkeys
     */
    public int getSubKeysNumber(int registrySection, String key) {
        logStartJNI(new Object [] {registrySection,key});
        int result = -1;
        try {
            result = getSubKeysNumber0(registrySection,key);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native int getSubKeysNumber0(int registrySection, String key);
    
    /** Get the number of the values of the specified key.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @return If the key doesn`t exist or can`t be accessed then return -1.
     * <br>Otherwise return the number of values
     */
    public int getValuesInKeyNumber(int registrySection, String key) {
        logStartJNI(new Object [] {registrySection,key});
        int result = -1;
        try {
            result = getValuesInKeyNumber0(registrySection,key);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native int getValuesInKeyNumber0(int registrySection, String key);
    
    /** Get the array of values names of the specified key.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @return If the key doesn`t exist or can`t be accessed then return <i>null</i>
     * <br>Otherwise return the array of value names
     */
    public String[] getValueNames  (int registrySection, String key) {
        logStartJNI(new Object [] {registrySection,key});
        String [] result = null;
        try {
            result = getValueNames0(registrySection,key);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native String[] getValueNames0  (int registrySection, String key);
    
    /** Get the array of subkey names of the specified key.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @return If the key doesn`t exist or can`t be accessed then return <i>null</i>
     * <br>Otherwise return the array of subkey names
     */
    public String[] getSubkeyNames(int registrySection, String key) {
        logStartJNI(new Object [] {registrySection,key});
        String [] result = null;
        try {
            result = getSubkeyNames0(registrySection,key);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native String[] getSubkeyNames0(int registrySection, String key);
    
    
    /** Create the new key in the registry.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @return <i>true</i> if the key was successfully created,
     * <br> <i>false</i> otherwise
     */
    public boolean createKey(int registrySection, String key) {
        return (key==null) ? false :
            createKey(registrySection,getParentKey(key),getChildKey(key));
    }
    /** Create the new key in the registry.
     * @param registrySection The section of the registry
     * @param parent key The specified parent key
     * @param parent key The specified child key
     * @return <i>true</i> if the key was successfully created,
     * <br> <i>false</i> otherwise
     */
    public boolean createKey(int registrySection, String parent, String child) {
        logStartJNI(new Object [] {registrySection,parent, child});
        boolean result = false;
        try {
            result = createKey0(registrySection,parent,child);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native boolean createKey0(int registrySection, String parent, String child);
    
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
    public boolean setStringValue(int registrySection, String key, String valueName, String value, boolean expandable) {
        logStartJNI(new Object [] {registrySection, key, valueName, value, expandable});
        boolean result = false;
        try {
            result = setStringValue0(registrySection, key, valueName, value, expandable);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native boolean setStringValue0(int registrySection, String key, String valueName, String value, boolean expandable);
    
    
    /** Set REG_DWORD value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @param value The specified value of the <code>valueName</code>
     * @return <i>true</i> if the value was successfully set
     * <br> <i>false</i> otherwise
     */
    public boolean set32BitValue(int registrySection, String key, String valueName, int value) {
        logStartJNI(new Object [] {registrySection, key, valueName, value});
        boolean result = false;
        try {
            result = set32BitValue0(registrySection, key, valueName, value);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native boolean set32BitValue0(int registrySection, String key, String valueName, int value);
    /** Set REG_MULTI_SZ value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @param value The specified value of the <code>valueName</code>
     * @return <i>true</i> if the value was successfully set
     * <br> <i>false</i> otherwise
     */
    public boolean setMultiStringValue(int registrySection, String key, String valueName, String [] value) {
        logStartJNI(new Object [] {registrySection, key, valueName, value});
        boolean result = false;
        try {
            result = setMultiStringValue0(registrySection, key, valueName, value);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native boolean setMultiStringValue0(int registrySection, String key, String valueName, String [] value);
    
    
    /** Set binary (REG_BINARY) value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @param value The specified value of the <code>valueName</code>
     * @return <i>true</i> if the value was successfully set
     * <br> <i>false</i> otherwise
     */
    public boolean setBinaryValue(int registrySection, String key, String valueName, byte [] value) {
        logStartJNI(new Object [] {registrySection, key, valueName, value});
        boolean result = false;
        try {
            result = setBinaryValue0(registrySection, key, valueName, value);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native boolean setBinaryValue0(int registrySection, String key, String valueName, byte [] value);
    
    
    /** Get string value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @param expandable
     *      If <code>expandable</code> is <i>true</i> and
     *      the type of the value is REG_EXPAND_SZ the value would be expanded
     * @return The value of the valueName, <i>null</i> in case of any error
     */
    public String getStringValue(int registrySection, String key, String valueName, boolean expand) {
        logStartJNI(new Object [] {registrySection, key, valueName, expand});
        String result = null;
        try {
            result = getStringValue0(registrySection, key, valueName, expand);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native String getStringValue0(int registrySection, String key, String valueName, boolean expand);
    
    /** Get integer value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @return The value of the valueName, <i>-1</i> in case of any error
     */
    public int get32BitValue(int registrySection, String key, String valueName) {
        logStartJNI(new Object [] {registrySection, key, valueName});
        int result = -1;
        try {
            result = get32BitValue0(registrySection, key, valueName);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private  native int    get32BitValue0(int registrySection, String key, String valueName);
    
    /** Get the array of strings of the specified value
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @return The multri-string value of the valueName, <i>null</i> in case of any error
     */
    public String[] getMultiStringValue(int registrySection, String key, String valueName) {
        logStartJNI(new Object [] {registrySection, key, valueName});
        String [] result = null;
        try {
            result = getMultiStringValue0(registrySection, key, valueName);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native String[] getMultiStringValue0(int registrySection, String key, String valueName);
    /** Get binary value.
     * @param registrySection The section of the registry
     * @param key The specified key
     * @param valueName The specified value
     * @return The binary value of the valueName, <i>null</i> in case of any error
     */
    
    public byte  [] getBinaryValue(int registrySection, String key, String valueName) {
        logStartJNI(new Object [] {registrySection, key, valueName});
        byte [] result = null;
        try {
            result = getBinaryValue0(registrySection, key, valueName);
        } catch(UnsatisfiedLinkError ex) {
            logErrJNI(ex);
        } finally {
            logEndJNI();
        }
        return result;
    }
    private native byte  [] getBinaryValue0(int registrySection, String key, String valueName);
    
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
