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

package org.netbeans.lib.collab.util;

import java.util.*;
import java.net.*;
import java.io.*;
import java.lang.reflect.*;

/**
 *
 *
 * @param
 *
 */
public class PlatformUtil {
    static private boolean is95 = false;
    static private boolean is98 = false;
    static private boolean isNT = false;
    static private boolean is2000 = false;
    static private boolean is2003 = false;
    static private boolean isXP   = false;
    static private boolean isos2 = false;
    static private boolean isunix = false;
    static private boolean isSun = false;
    static private boolean isLinux = false;
    static private boolean ismac = false;
    static private boolean isHpux = false;
    
    static private boolean is1_1 = false;
    static private boolean is1_2 = false;
    static private boolean is1_3 = false;
    static private boolean is1_4 = false;
    static private boolean is1_5 = false;
    static private boolean isJava2 = true;
    
    static private boolean isJview = false;
    static public boolean isJview40 = false;
    
    final static private String WIN95_ID =  "windows 95";
    final static private String WIN98_ID =  "windows 98";
    final static private String WINNT_ID =  "windows nt";
    final static private String WIN2K_ID =  "windows 2000";
    final static private String WIN3K_ID =  "windows 2003";
    final static private String WINXP_ID =  "windows xp";
    final static private String OS2_ID =    "os/2";
    final static private String MAC_ID =    "mac os";
    final static private String UNIX_ID =   "unix";
    final static private String SUN_ID =    "SunOS";
    final static private String LINUX_ID =  "Linux";
    final static private String HPUX_ID =  "HP-UX";
    
    
    /**
     * @param
     */
    static {
        String t = getOSName();
        //System.out.println("<< OPERATING SYSTEM >> " + t);
        if (t.equalsIgnoreCase(WIN95_ID)) {
            if (getOSVersion().equals("4.0"))
                is95 = true;
            else
                is98 = true;
        } else if (t.equalsIgnoreCase(WIN98_ID)) {
            is98 = true;
        } else if (t.equalsIgnoreCase(WINNT_ID)){
            if (getOSVersion().equals("5.0"))
                is2000 = true;
            else
                isNT = true;
        } else if (t.equalsIgnoreCase(WIN2K_ID)) {
            is2000 = true;
            }else if (t.equalsIgnoreCase(WIN3K_ID)) {
            	is2003 = true;
            
        } else if (t.equalsIgnoreCase(WINXP_ID)) {
            isXP = true;
        } else if (t.equalsIgnoreCase(OS2_ID)) {
            isos2 = true;
        } else if (t.equalsIgnoreCase(SUN_ID)) {
            isSun = true;
        } else if (t.equalsIgnoreCase(HPUX_ID)) {
            isHpux = true;
        } else if (t.toLowerCase().startsWith(MAC_ID)) {
            ismac = true;
        } else if (t.equalsIgnoreCase(LINUX_ID)) {
            isLinux = true;
        }
        
        t = getJavaVersion();
        if (t.startsWith("1.5")) {
            is1_5 = true;
        } else if (t.startsWith("1.4")) {
            is1_4 = true;
        } else if (t.startsWith("1.3")) {
            is1_3 = true;
        } else if (t.startsWith("1.2")) {
            is1_2 = true;
        } else if (t.startsWith("1.1"))  {
            isJava2 = false;
            is1_1 = true;
        }
        
        t = System.getProperty("java.vendor");
        if (t.startsWith("Microsoft")) {
            isJview = true;
            getMsVersion();
        }
        t = null;
    }
    
    
    
    
    /**
     * If on MS platform, get the JView Version
     */
    final private static String getMsVersion(){
        StringBuffer ret = new StringBuffer();
        
        try {
            Class c = Class.forName("com.ms.util.SystemVersionManager");
            Method m = c.getDeclaredMethod("getVMVersion", null);
            Object o = null;
            Properties p = (Properties)m.invoke(o, new Object[] {  });
            Enumeration e = p.keys();
            while(e.hasMoreElements()){
                String key = (String)e.nextElement();
                ret.append("MS ");
                ret.append(key);
                ret.append(" :: ");
                String tmp = (String)p.getProperty(key);
                if(tmp.indexOf("3240") >= 0){
                    isJview40 = true;
                }
                ret.append(tmp);
                ret.append("\n");
            }
            e = null;
        } catch (Exception e) {
            System.out.println("getMsVersion:"+e);
        }
        
        
        return ret.toString();
    }
    
    
    
    /**
     *
     *
     * @return program working directory
     */
    static final public String getUserDir(){    return System.getProperty("user.dir");}
    
    
    /**
     * Provides home directory for the system account running this
     * jvm instance.  Equivalent to getting the value of the 
     * user.home JVM variable.
     *
     * @return home directory
     */
    static final public String getHomeDir()
    {    
	return System.getProperty("user.home");
    }
    
    
    /**
     *
     *
     * @return operating system name.
     */
    static final public String getOSName(){
	return System.getProperty("os.name");
    }
    
    static final public String getNoSpaceOSName()
    {
	if (isWin()) return "Windows";
	if (isMac()) return "MacOS";
	return System.getProperty("os.name");
    }

    
    /**
     *
     *
     * @param
     */
    static final public String getOSArch() 
    {
	String arch = System.getProperty("os.arch");
        if (isLinux()) {
            // mismatch between build system (x86) and os.arch (i386);
	    if (arch.endsWith("86")) arch = "x86";
        } else if (isSun()) {
	    if (arch.endsWith("86")) arch = "i386";	    
	}
	return arch;
    }
    
    
    /**
     *
     *
     * @param
     */
    static final public String getOSVersion(){  return System.getProperty("os.version");}
    
    
    /**
     *
     *
     * @param
     */
    static final public String getJavaVersion(){return System.getProperty("java.version");}
    
    
    /**
     *
     *
     * @param
     */
    static final public String getUserName(){   return System.getProperty("user.name");}
    
    
    
    /**
     *
     *
     * @param
     */
    static final public String getVMVersion(){
        String ret = System.getProperty("java.vm.version");
        if(ret == null)
            return getMsVersion();
        else
            return ret;
    }
    
    
    
    /**
     *
     *
     * @param
     */
    static final public String getVMInfo(){
        String ret = System.getProperty("java.vm.info");
        if(ret == null)
            return getMsVersion();
        else
            return ret;
    }
    
    
    /**
     *
     *
     * @param
     */
    static final public boolean isWin(){   return is95 || is98 || isNT || is2000 || isXP || is2003 ;}
    
    
    /**
     *
     *
     * @param
     */
    static final public boolean is98(){    return is98;}
    
    
    /**
     *
     *
     * @param
     */
    static final public boolean is95(){    return is95;}
    
    
    /**
     *
     *
     * @param
     */
    static final public boolean isNT(){    return isNT;}
    
    
    /**
     *
     *
     * @param
     */
    static final public boolean is2000(){  return is2000;}
    
    /**
     *
     *
     * @param
     */
    static final public boolean is2003(){ return is2003;}
    
    
    /**
     *
     *
     * @param
     */
    static final public boolean isOS2(){   return isos2;}
    
    
    /**
     *
     *
     * @param
     */
    static final public boolean isMac(){   return ismac;}
    
    
    /**
     *
     *
     * @param
     */
    static final public boolean isSun(){  return isSun;}
    
    /**
     *
     *
     * @param
     */
    static final public boolean isHpux(){  return isHpux;}

    /**
     *
     *
     * @param
     */
    static final public boolean isLinux(){  return isLinux;}
    
    /**
     *
     *
     * @param
     */
    static final public boolean isUnix(){ return (isSun || isLinux || isHpux);}
    
    
    /**
     *
     *
     * @param
     */
    static final public boolean isJava2(){
        return (isJava2);
    }
    
    
    /**
     *
     *
     * @param
     */
    static final public boolean isJava1(){ return is1_1;}
    
    
    /**
     *
     *
     * @param
     */
    static final public boolean isJview() { return isJview; }
    
    /**
     *
     *
     * @param
     */
    static final public boolean isJDK1_4() { return is1_4;}
    
    /**
     *
     *
     * @param
     */
    static final public boolean isJDK1_5() { return is1_5;}
    
    /**
     *
     *
     * @param
     */
    static final public boolean isJDK1_3() { return is1_3;}
    
    
    /**
     *
     *
     * @param
     */
    static final public boolean isJDK1_2() { return is1_2; }
    
    
    /**
     * @param
     */
    static final public boolean isJDK1_1() { return is1_1; }
    
    
    /**
     * This was added as a fix for win98 platforms -
     * Some of the platforms locked up completely when creating a new socket
     * Sun insisted problem was fixed, but was still occurring here
     * This seemed to stop the problem
     */
    final static public void loadWinSock(){
        if(isJview) return;
        try{
            System.out.println("%% LOAD WINSOCK");
            System.loadLibrary("wsock32");
            getLocalHost();
        }catch(Throwable e) {
            System.out.println("loadWinSock:"+e);
        }
    }
    
    
    
    /**
     *
     *
     * @param
     */
    final static public String viewSystemInfo(){
        StringBuffer s = new StringBuffer("System Properties!! \n");
        s.append("\n VM Version ");
        s.append(getVMVersion());
        s.append("\n VM Info ");
        s.append(getVMInfo());
        s.append("\n");
        Enumeration e = System.getProperties().keys();
        String tmp = "";
        while(e.hasMoreElements()){
            tmp = (String)e.nextElement();
            s.append(tmp + " = " +System.getProperty(tmp)+"\n");
        }
        tmp = null;
        e = null;
        return s.toString();
    }
    
    
    
    
    /**
     *
     *
     * @param
     */
    final static public String getLocalHost(){
        try{
            System.out.println(".*. Getting Local Host .*.");
            InetAddress addr = InetAddress.getLocalHost();
            return addr.toString();
        }catch(Exception e){
            System.out.println("getLocalHost:"+e);
        }
        return "";
    }
    
    
    
    /**
     *
     *
     * @param
     */
    //For Debugging -- forces system to be viewed as Windows
    final static public void forceWin(){
        is98 = true;
        isos2 = false;
    }
    
    
    
    /**
     *
     *
     * @param
     */
    //For Debugging -- forces system to be viewed as OS2
    final static public void forceOS2(){
        isos2 = true;
        is98 = false;
        isNT = false;
        is95 = false;
        ismac = false;
        isunix = false;
    }
    
    
    
    /**
     *
     *
     * @param
     */
    //For Debugging -- forces system to be viewed as MAC
    final static public void forceMac(){
        isos2 = false;
        is98 = false;
        isNT = false;
        is95 = false;
        ismac = true;
        isunix = false;
    }
    
    
    
    /**
     *
     *
     * @param
     */
    //For Debugging -- forces system to be viewed as UNIX
    final static public void forceUnix(boolean b){
        isos2 = false;
        is98 = false;
        isNT = false;
        is95 = false;
        ismac = false;
        isunix = true;
    }
    
    
    
    /**
     *
     *
     * @param
     */
    //For Debugging -- forces system to view JDK as version 2
    final static public void forceJava2(boolean b){
        is1_2 = b;
        is1_1 = !b;
    }
    
    
    
    /**
     *
     *
     * @param
     */
    //For Debugging -- forces system to view JDK as NOT version 2
    final static public void forceJava1(boolean b){
        is1_1 = b;
        is1_2 = !b;
    }
    
    // renameTo method fails on Windows if the dest file already exists
    // Hence fake the creation of a temp file on this platform
    final static public File getTempFile(File f) {
        if (isWin()) {
            return f;
        } else {
            return new File(f.getAbsolutePath() + ".tmp");
        }
    }
    
    final static public boolean renameTempFile(File temp, File f) {
        if (!isWin()) {
            return temp.renameTo(f);
        }
        return true;
    }
    
}
