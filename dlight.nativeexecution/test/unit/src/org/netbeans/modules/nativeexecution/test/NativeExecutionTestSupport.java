/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.nativeexecution.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;

/**
 *
 * @author vk155633
 */
public class NativeExecutionTestSupport {

    private static ExecutionEnvironment defaultTestExecutionEnvironment;
    private static RcFile rcFile;
    private static final Map<String, ExecutionEnvironment> spec2env = new LinkedHashMap<String, ExecutionEnvironment>();
    private static final Map<ExecutionEnvironment, String> env2spec = new LinkedHashMap<ExecutionEnvironment, String>();

    private NativeExecutionTestSupport() {
    }

    public static synchronized RcFile getRcFile() throws IOException, RcFile.FormatException {
        if (rcFile == null) {
            String rcFileName = System.getProperty("cnd.remote.rcfile"); // NOI18N
            if (rcFileName == null) {
                String homePath = System.getProperty("user.home");
                if (homePath != null) {
                    File homeDir = new File(homePath);
                    rcFile = new RcFile(new File(homeDir, ".cndtestrc"));
                }
            } else {
                rcFile = new RcFile(new File(rcFileName));
            }
        }
        return rcFile;
    }

    /**
     * Gets old-style default test execution environment -
     * i.e. the one that is set via -J-Dcnd.remote.testuserinfo
     * or CND_REMOTE_TESTUSERINFO environment variable
     */
    public static ExecutionEnvironment getDefaultTestExecutionEnvironment(boolean connect) throws IOException, CancellationException {
        synchronized(NativeExecutionBaseTestCase.class) {
            if (defaultTestExecutionEnvironment == null) {
                String ui = System.getProperty("cnd.remote.testuserinfo"); // NOI18N
                char[] passwd = null;
                if( ui == null ) {
                    ui = System.getenv("CND_REMOTE_TESTUSERINFO"); // NOI18N
                }
                if (ui != null) {
                    int m = ui.indexOf(':');
                    if (m>-1) {
                        int n = ui.indexOf('@');
                        String strPwd = ui.substring(m+1, n);
                        String remoteHKey = ui.substring(0,m) + ui.substring(n);
                        defaultTestExecutionEnvironment = ExecutionEnvironmentFactory.fromUniqueID(remoteHKey);
                        passwd = strPwd.toCharArray();                        
                    } else {
                        String remoteHKey = ui;
                        defaultTestExecutionEnvironment = ExecutionEnvironmentFactory.fromUniqueID(remoteHKey);
                    }
                } else {
                    defaultTestExecutionEnvironment = ExecutionEnvironmentFactory.createNew(System.getProperty("user.name"), "127.0.0.1"); // NOI18N
                }
                if (defaultTestExecutionEnvironment != null) {
                    if(passwd != null && passwd.length > 0) {
                        PasswordManager.getInstance().storePassword(defaultTestExecutionEnvironment, passwd, false);
                    }
                    
                    if (connect) {
                        ConnectionManager.getInstance().connectTo(defaultTestExecutionEnvironment);
                    } 
                }
            }
        }
        return defaultTestExecutionEnvironment;
    }

    public static ExecutionEnvironment getTestExecutionEnvironment(String mspec) throws IOException {
        ExecutionEnvironment result = null;

        if (mspec == null) {
            return null;
        }

        String rcFileName = System.getProperty("cnd.remote.testuserinfo.rcfile"); // NOI18N
        File userInfoFile = null;

        if (rcFileName == null) {
            String homePath = System.getProperty("user.home");
            if (homePath != null) {
                File homeDir = new File(homePath);
                userInfoFile = new File(homeDir, ".testuserinfo");
            }
        } else {
            userInfoFile = new File(rcFileName);
        }

        if (userInfoFile == null || ! userInfoFile.exists()) {
            return null;
        }

        BufferedReader rcReader = new BufferedReader(new FileReader(userInfoFile));
        String str;
        Pattern infoPattern = Pattern.compile("^([^#].*)[ \t]+(.*)"); // NOI18N
        Pattern pwdPattern = Pattern.compile("([^:]+):(.*)@(.*)"); // NOI18N
        char[] passwd = null;

        while ((str = rcReader.readLine()) != null) {
            Matcher m = infoPattern.matcher(str);
            String spec = null;
            String loginInfo;

            if (m.matches()) {
                spec = m.group(1).trim();
                loginInfo = m.group(2).trim();
            } else {
                continue;
            }

            if (mspec.equals(spec)) {
                m = pwdPattern.matcher(loginInfo);
                String remoteHKey = null;

                if (m.matches()) {
                    passwd = m.group(2).toCharArray();
                    remoteHKey = m.group(1) + "@" + m.group(3); // NOI18N                    
                } else {
                    remoteHKey = loginInfo;
                }

                result = ExecutionEnvironmentFactory.fromUniqueID(remoteHKey);
                break;
            }
        }

        if (result != null) {
            if (passwd != null) {
                PasswordManager.getInstance().put(result, passwd);
            }
            //ConnectionManager.getInstance().connectTo(result, passwd, false);
        }

        spec2env.put(mspec, result);
        env2spec.put(result, mspec);
        return result;
    }

    /**
     * Gets an MSpec string, which was used for getting the given environent
     * (i.e. it's an inverse of getTestExecutionEnvironment(String))
     */
    public static String getMspec(ExecutionEnvironment execEnv) {
        return env2spec.get(execEnv);
    }

    public static boolean getBoolean(String condSection, String condKey) {
        return getBoolean(condSection, condKey, false);
    }

    public static boolean getBoolean(String condSection, String condKey, boolean defaultValue) {
        try {
            String value = getRcFile().get(condSection, condKey);
            return (value == null) ? defaultValue : Boolean.parseBoolean(value);
        } catch (FileNotFoundException ex) {
            // silently: just no file => condition is false, that's it
            return defaultValue;
        } catch (IOException ex) {
            return defaultValue;
        } catch (RcFile.FormatException ex) {
            return defaultValue;
        }
    }
}
