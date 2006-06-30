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
 */

package org.netbeans.modules.j2ee.deployment.profiler.api;

import java.util.Arrays;
import java.util.List;
import org.netbeans.api.java.platform.JavaPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Settings that will be used for profiled server startup.
 *
 * @author sherold
 */
public final class ProfilerServerSettings {

    private JavaPlatform    javaPlatform;
    private String[]        jvmArgs;
    private String[]        env;

    /**
     * Creates new ProfilerServerSettings.
     *
     * @param javaPlatform Java platform used to run profiled server.
     * @param jvmArgs      array of extra JVM arguments used for starting profiled 
     *                     server.
     * @param env          array of <code>name=value</code> pairs of extra variables 
     *                     to be set for profiled server environment.
     */
    public ProfilerServerSettings(JavaPlatform javaPlatform, String[] jvmArgs, String[] env) {
        if (javaPlatform == null) {
            throw new NullPointerException("The javaPlatform argument must not be null.");  // NOI18N
        }
        if (jvmArgs == null) {
            throw new NullPointerException("The jvmArgs argument must not be null.");       // NOI18N
        }
        if (env == null) {
            throw new NullPointerException("The env argument must not be null.");           // NOI18N
        }
        this.javaPlatform   = javaPlatform;
        this.jvmArgs        = jvmArgs;
        this.env            = env;
    }

    /**
     * Gets the Java platform that will be used for starting the profiled server.
     *
     * @return Java platform that will be used for starting the profiled server.
     */
    public JavaPlatform getJavaPlatform() {
        return javaPlatform;
    }

    /**
     * Gets the extra arguments that will be used for starting the profiled server.
     *
     * @return array of extra arguments that will be used for starting the profiled 
     *         server
     */
    public String[] getJvmArgs() {
        return jvmArgs;
    }

    /**
     * Gets extra variables that will be set for profiled server environment.
     *
     * @return array of <code>name=value</code> pairs describing extra variables 
     *         that will be set for profiled server environment
     */
    public String[] getEnv() {
        return env;
    }
    
    /**
     * Tests this ProfilerServerSettings for equality with the given object.
     *
     * @param o The object to be compared with this ProfilerServerSettings.
     *
     * @return  <code>true</code> if the other ProfilerServerSettings instance 
     *          defines the same settings; false otherwise.
     */
    public boolean equals(Object o) {
        if (o == this) {
	    return true;
	}
        if (!(o instanceof ProfilerServerSettings)) {
            return false;
        }
        ProfilerServerSettings other = (ProfilerServerSettings)o;
        FileObject javaHome = (FileObject)javaPlatform.getInstallFolders().iterator().next();
        FileObject otherJavaHome = (FileObject)other.javaPlatform.getInstallFolders().iterator().next();
        if (!FileUtil.toFile(javaHome).equals(FileUtil.toFile(otherJavaHome))) {
            return false;
        }
        if (jvmArgs.length != other.jvmArgs.length) {
            return false;
        }
        List jvmArgsList = Arrays.asList(jvmArgs);
        for (int i = 0; i < other.jvmArgs.length; i++) {
            if (!jvmArgsList.contains(other.jvmArgs[i])) {
                return false;
            }
        }
        if (env.length != other.env.length) {
            return false;
        }
        List envList = Arrays.asList(env);
        for (int i = 0; i < other.env.length; i++) {
            if (!envList.contains(other.env[i])) {
                return false;
            }
        }
        return true;
    }
    
    public int hashCode() {
        int result = 17;
        FileObject javaHome = (FileObject)javaPlatform.getInstallFolders().iterator().next();
        result = 37 * result + FileUtil.toFile(javaHome).hashCode();        
        String[] jvmArgsTmp = (String[])jvmArgs.clone();
        Arrays.sort(jvmArgsTmp);
        for (int i = 0; i < jvmArgsTmp.length; i++) {
            result = 37 * result + jvmArgsTmp[i].hashCode();
        }
        String[] envTmp = (String[])env.clone();
        Arrays.sort(envTmp);
        for (int i = 0; i < envTmp.length; i++) {
            result = 37 * result + envTmp[i].hashCode();
        }
        return result;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ProfilerServerSettings [\n");   //  NOI18N
        buffer.append("  javaPlatform: " + javaPlatform.getDisplayName() + "\n"); //  NOI18N
        buffer.append("  jvmarg:   ");  //  NOI18N      //  NOI18N
        for (int i = 0; i < jvmArgs.length; i++) {
            buffer.append(jvmArgs[i] + " ");    //  NOI18N
        }
        buffer.append("\n");                    //  NOI18N
        buffer.append("  env:      ");          //  NOI18N
        for (int i = 0; i < env.length; i++) {  
            buffer.append(env[i] + " ");        //  NOI18N
        }
        buffer.append("]");                     //  NOI18N
        return buffer.toString();
    }
}
