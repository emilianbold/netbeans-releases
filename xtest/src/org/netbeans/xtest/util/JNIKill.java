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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
 * JNIKill.java
 *
 * Created on April 5, 2002, 5:11 PM
 */

package org.netbeans.xtest.util;

import java.io.File;


/**
 *
 * @author  mb115822
 */
public class JNIKill  {


    private static boolean libraryLoaded = false;

    /*
     * this static string contains pairs of supported platforms
     * and names of native libraries implementating kill functions
     */
    private static final String [][] SUPPORTED_PLATFORMS = {
        {"Linux,i386","lib.jnikill.linux.i386.so"},
        {"Linux,x86","lib.jnikill.linux.i386.so"},
        {"Mac_OS_X,ppc","lib.jnikill.macosx.ppc.dylib"},
        {"SunOS,sparc","lib.jnikill.solaris.sparc.so"},
        {"SunOS,x86","lib.jnikill.solaris.x86.so"},
        {"Windows_Vista,x86","lib.jnikill.win32.x86.dll"},
        {"Windows_NT,x86","lib.jnikill.win32.x86.dll"},
        {"Windows_2000,x86","lib.jnikill.win32.x86.dll"},
        {"Windows_XP,x86","lib.jnikill.win32.x86.dll"},
        {"Windows_95,x86","lib.jnikill.win32.x86.dll"},
        {"Windows_98,x86","lib.jnikill.win32.x86.dll"},
        {"Windows_Me,x86","lib.jnikill.win32.x86.dll"},
        {"Windows_2003,x86","lib.jnikill.win32.x86.dll"},
        {"Windows_2003,amd64","lib.jnikill.amd64.x86.dll"}
    };
    
    private static String getPlatform() {
        String platform=System.getProperty("os.name","")+","+
                        System.getProperty("os.arch","");
        return platform.replace(' ','_');
    }
    
    // get home of xtest
    private static String getXTestHome() {
        return System.getProperty("xtest.home","");
    }
    
    // where is the native library stored
    private static String getLibraryFilename(String libraryName) {
        return getXTestHome()+File.separator+"lib"+File.separator+libraryName;
    }
    
    private static final String LIBRARY_SYSTEM_PROPERTY = "xtest.jnikill.library"    ;
    
    private static void setLibraryLoaded() {
        System.setProperty(LIBRARY_SYSTEM_PROPERTY,"loaded");
    }
    
    private static boolean isLibraryLoaded() {
        return System.getProperty(LIBRARY_SYSTEM_PROPERTY) != null;
    }
    
    /** Loads JNI library based on which platform the code is executed. */
    private static void loadJNILibrary() throws UnsatisfiedLinkError {
        if (isLibraryLoaded()) {
            System.out.println("JNI kill library already loaded");
        } else {
            String currentPlatform = getPlatform();
            System.out.println("Current platform="+currentPlatform);
            for (int i=0;i<SUPPORTED_PLATFORMS.length;i++) {
                if (currentPlatform.equalsIgnoreCase(SUPPORTED_PLATFORMS[i][0])) {
                    // we have it - let's load the library
                    try {
                        loadJNILibrary(SUPPORTED_PLATFORMS[i][1]);
                    } catch (UnsatisfiedLinkError ule) {
                        ule.printStackTrace();
                    }
                    if(isLibraryLoaded()) {
                        return;
                    }
                }
            }
            // not possible to load library anyway
            throw new UnsatisfiedLinkError("JNIKill: Problem while trying to load JNI kill library.");
        }
    }
    
    /** Load library and set flag if it succeeds. */
    private static void loadJNILibrary(String libraryName) {
        String libraryFilename = getLibraryFilename(libraryName);
        Runtime.getRuntime().load(libraryFilename);
        System.out.println("Loading library: "+libraryName);
        System.out.println("Loading library from: "+libraryFilename);
        JNIKill.setLibraryLoaded();
    }
    
    // initialize native libraries !!!
    public JNIKill() {
        loadJNILibrary();
        //System.out.println("JNIKill ready");
    }
    
    // kill myself
    public boolean suicide() {
        return killProcess(getMyPID());
    }
    
    /*
     * Native methods declaration
     */
    
    // native functions for killing given process
    public native boolean killProcess(long pid);
    
    // native function for gettin pid of this process
    public native long getMyPID();
    
    // native function creates thread performing thread dump by signals
    public native boolean startDumpThread();
    
    // native function performs immediate thread dump
    public native boolean dumpMe();
    
    // native function requesting thread dump on JVM with given pid
    public native boolean requestDump(long pid);
    
}
