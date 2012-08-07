/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.android;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.cordova.ProcessUtils;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Becicka
 */
public class AndroidPlatform {
    
    private static AndroidPlatform instance;
    
    private static String ANDROID_SDK_ROOT_PREF = "android.sdk.home";
    public static final String TYPE = "android";

    private AndroidPlatform() {
    }

    public static synchronized AndroidPlatform getDefault() {
        if (instance == null) {
            instance = new AndroidPlatform();
        }
        return instance;
    }
    
//    public void createProject(File dir, String targetId, String projectName, String activityName, String packageName) throws IOException {
//        ProcessBuilder pb = ProcessBuilder.getLocal();
//        pb.setExecutable(getSdkLocation() + "/tools/android");
//        pb.setArguments(
//        Arrays.asList(
//                "create", "project", 
//                "--target", targetId,
//                "--name", projectName,
//                "--path", dir.getPath(),
//                "--activity", activityName,
//                "--package", packageName
//                ));
//        pb.setWorkingDirectory(dir.getParentFile().getAbsolutePath());
//        try {
//            Process call = pb.call();
//            call.waitFor();
//            InputStreamReader inputStreamReader = new InputStreamReader(new BufferedInputStream(call.getErrorStream()));
//            if (call.exitValue() != 0) {
//                StringBuilder error = new StringBuilder();
//                char[] ch = new char[1];
//                while (inputStreamReader.ready()) {
//                    inputStreamReader.read(ch);
//                    error.append(ch);
//                }
//                throw new IOException(error.toString());
//            }
//        } catch (InterruptedException ex) {
//            throw new IOException(ex);
//        }
//    }
    
    public Collection<AVD> getAVDs() throws IOException {
        assert !SwingUtilities.isEventDispatchThread();
        String avdString = ProcessUtils.callProcess(getSdkLocation() + "/tools/android", "list", "avd");
        return AVD.parse(avdString);
    }
    

    public List<Target> getTargets() throws IOException {
        //assert !SwingUtilities.isEventDispatchThread();
        String avdString = ProcessUtils.callProcess(getSdkLocation() + "/tools/android", "list", "targets");
        return Target.parse(avdString);
    }
    
    private final HashSet<String> targets = new HashSet<String>(Arrays.asList(new String[]{
            "android-7",
            "android-8",
            "android-9",
            "android-10",
            "android-11",
            "android-12",
            "android-13",
            "android-14",
            "android-15",
            "android-16"}));
    
    
    public String getPrefferedTarget() {
        try {
            final List<Target> targets1 = getTargets();
            for (Target t: targets1) {
                if (targets.contains(t.getName())) {
                    return t.getName();
                }
            }
            return targets1.get(targets.size()-1).getName();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    
    public Collection<Device> getDevices() throws IOException {
        //assert !SwingUtilities.isEventDispatchThread();
        String avdString = ProcessUtils.callProcess(getSdkLocation() + "/platform-tools/adb", "devices");
        Collection<Device> devices = Device.parse(avdString);
        if (devices.isEmpty()) {
            //maybe adb is just down. try to restart adb
            ProcessUtils.callProcess(getSdkLocation() + "/platform-tools/adb", "kill-server");
            ProcessUtils.callProcess(getSdkLocation() + "/platform-tools/adb", "start-server");
        }
        avdString = ProcessUtils.callProcess(getSdkLocation() + "/platform-tools/adb", "devices");
        devices = Device.parse(avdString);
        return devices;
    }
    
    
    public ExecutorTask buildProject(File dir, String... targets) throws IOException {
        File build = new File(dir.getAbsolutePath() + File.separator + "build.xml");
        FileObject buildFo = FileUtil.toFileObject(build);
        return ActionUtils.runTarget(buildFo, targets, null);
    }
    
    /**
     * Deletes dir and all subdirectories/files!
     * @param dir
     * @throws IOException 
     */
    public void cleanProject(File dir) throws IOException {
        FileUtil.toFileObject(dir).delete();
    }

    public String getSdkLocation() {
        return NbPreferences.forModule(AndroidPlatform.class).get(ANDROID_SDK_ROOT_PREF, null);
    }

    public void setSdkLocation(String sdkLocation) {
        NbPreferences.forModule(AndroidPlatform.class).put(ANDROID_SDK_ROOT_PREF, sdkLocation);
    }
    
    public boolean waitEmulatorReady(int timeout) {
        try {
            return RequestProcessor.getDefault().invokeAny(Collections.singleton(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        return waitEmulatorReady();
                    }
                }), timeout, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        } catch (TimeoutException ex) {
        }
        return false;
        
    }
    
    private boolean waitEmulatorReady() {
        try {
            String value;
            for(;;) {
                value = ProcessUtils.callProcess(getSdkLocation() + "/platform-tools/adb", "-e", "wait-for-device", "shell", "getprop", "init.svc.bootanim");
                if ("stopped".equals(value.trim())) {
                    return true;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } 
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
        
    }

    public void manageAVDs() {
        assert !SwingUtilities.isEventDispatchThread();
        try {
            ProcessUtils.callProcess(getSdkLocation() + "/tools/android", "avd");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    
}

