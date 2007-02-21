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

package org.netbeans.tests.j2eeserver.plugin.jsr88;

import java.io.*;
import java.util.*;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

/**
 *
 * @author  gfink
 * @author nn136682
 */
public class DepManager implements DeploymentManager {
    String name;

    /** Creates a new instance of DepFactory */
    public DepManager(String url, String user, String password) {
        name = url;
    }
    public String getName() { return name ; }
    
    public DeploymentConfiguration createConfiguration(DeployableObject deployableObject) throws InvalidModuleException {
        return new DepConfig(deployableObject,this);
    }

    public ProgressObject distribute(Target[] targets, final File file, File file2) throws java.lang.IllegalStateException {
        java.util.logging.Logger.getLogger(DepManager.class.getName()).log(java.util.logging.Level.FINEST,"Deploying " + file + " with " + file2);

        final ProgObject po = new ProgObject(this, targets, file, file2);
        Runnable r = new Runnable() {
            public void run() {
                try { Thread.sleep(200); //some latency
                } catch (Exception e) {}
                po.setStatusDistributeRunning("TestPluginDM: distributing "+ file);
                try { Thread.sleep(500); //super server starting time
                } catch (Exception e) {}
                if (getTestBehavior() == DISTRIBUTE_FAILED) {
                    po.setStatusStartFailed("TestPluginDM distribute failed");
                } else {
                    po.setStatusStartCompleted("TestPluginDM distribute finish");
                }
            }
        };
        
        (new Thread(r)).start();
        return po;
    }

    public ProgObject incrementalDeploy(final TargetModuleID target, AppChangeDescriptor desc) throws java.lang.IllegalStateException {
        System.out.println(desc);
        final ProgObject po = new ProgObject(this, new TargetModuleID[] { target });
        Runnable r = new Runnable() {
            public void run() {
                try { Thread.sleep(50); //some latency
                } catch (Exception e) {}
                po.setStatusDistributeRunning("TestPluginDM: incrementally deploying "+ target);
                try { Thread.sleep(500); //super server starting time
                } catch (Exception e) {}
                if (getTestBehavior() == DISTRIBUTE_FAILED) {
                    po.setStatusStartFailed("TestPluginDM incremental deploy failed");
                } else {
                    po.setStatusStartCompleted("TestPluginDM incremental deploy finish");
                }
            }
        };
        
        (new Thread(r)).start();
        return po;
    }
    
    public boolean hasDistributed(String id) {
        for (int i=0; i<getTargets().length; i++) {
            Targ t = (Targ) getTargets()[i];
            if (t.getTargetModuleID(id) != null)
                return true;
        }
        return false;
    }
    
    public ProgressObject distribute(Target[] target, InputStream inputStream, InputStream inputStream2) throws java.lang.IllegalStateException {
        return new ProgObject(this, target,inputStream,inputStream2);
    }
    
    public ProgressObject distribute(Target[] target, ModuleType moduleType, InputStream inputStream, InputStream inputStream0) throws IllegalStateException {
        return distribute(target, inputStream, inputStream0);
    }
    
    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] target) throws TargetException, java.lang.IllegalStateException {
        List l = new ArrayList();
        Targ[] mytargets = (Targ[]) getTargets();
        HashSet yours = new HashSet(Arrays.asList(target));
        for (int i=0; i<mytargets.length; i++) {
            if (yours.contains(mytargets[i]))
                l.addAll(Arrays.asList((mytargets[i]).getTargetModuleIDs()));
        }
        return (TargetModuleID[]) l.toArray(new TargetModuleID[0]);
    }
    
    public Locale getCurrentLocale() {
        return Locale.getDefault();
    }
    
    public DConfigBeanVersionType getDConfigBeanVersion() {
        return DConfigBeanVersionType.V1_3; 
    }
    
    public Locale getDefaultLocale() {
        return Locale.getDefault();
    }
    
    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] target) throws TargetException, java.lang.IllegalStateException {
        return new TargetModuleID[0]; // PENDING see above.
    }
    
    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] target) throws TargetException, java.lang.IllegalStateException {
        return new TargetModuleID[0]; // PENDING see above.
    }
    
    public Locale[] getSupportedLocales() {
        return new Locale[] { Locale.getDefault() };
    }
    
    Targ[] targets;
    public Target[] getTargets() throws java.lang.IllegalStateException {
        if (targets == null) {
            targets = new Targ[] {
            new Targ("Target 1"),
            new Targ("Target 2")
        };
        }
        return targets;
    }
    
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dConfigBeanVersionType) {
        return true;
    }
    
    public boolean isLocaleSupported(Locale locale) {
        return Locale.getDefault().equals(locale);
    }
    
    public boolean isRedeploySupported() {
        return true; // PENDING use jsr88 redeploy?
    }
    
    public ProgressObject redeploy(TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2) throws java.lang.UnsupportedOperationException, java.lang.IllegalStateException {
        throw new UnsupportedOperationException();
    }
    
    Set redeployed;
    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, File file2) throws java.lang.UnsupportedOperationException, java.lang.IllegalStateException {
        final ProgObject po = new ProgObject(this, targetModuleID);
        final java.util.List targetModules = java.util.Arrays.asList(targetModuleID);
        Runnable r = new Runnable() {
            public void run() {
                try { Thread.sleep(200); //some latency
                } catch (Exception e) {}
                po.setStatusRedeployRunning("TestPluginDM: redeploy "+ targetModules);
                redeployed = new HashSet();
                try { Thread.sleep(500); //super server starting time
                } catch (Exception e) {}
                if (getTestBehavior() == REDEPLOY_FAILED) {
                    po.setStatusRedeployFailed("TestPluginDM: failed to redeploy "+targetModules);
                } else {
                    po.setStatusRedeployCompleted("TestPluginDM: done redeploy "+targetModules);
                    TargetModuleID[] result = po.getResultTargetModuleIDs();
                    for (int i=0; i<result.length; i++)
                        redeployed.add(result[i].toString());
                }
            }
        };
        
        (new Thread(r)).start();
        return po;
    }
    public boolean hasRedeployed(String id) {
        return redeployed != null && redeployed.contains(id);
    }
    
    public void release() {
    }
    
    public void setDConfigBeanVersion(DConfigBeanVersionType dConfigBeanVersionType) throws DConfigBeanVersionUnsupportedException {
    }
    
    public void setLocale(Locale locale) throws java.lang.UnsupportedOperationException {
    }
    
    public ProgressObject start(TargetModuleID[] targetModuleID) throws java.lang.IllegalStateException {
        final ProgObject po = new ProgObject(this, targetModuleID);
        final java.util.List targetModules = java.util.Arrays.asList(targetModuleID);
        Runnable r = new Runnable() {
            public void run() {
                try { Thread.sleep(200); //some latency
                } catch (Exception e) {}
                po.setStatusStartRunning("TestPluginDM: starting "+ targetModules);
                try { Thread.sleep(500); //super server starting time
                } catch (Exception e) {}
                if (getTestBehavior() == START_MODULES_FAILED) {
                    po.setStatusStartFailed("TestPluginDM: failed to start "+targetModules);
                } else {
                    po.setStatusStartCompleted("TestPluginDM: done starting "+targetModules);
                }
            }
        };
        
        (new Thread(r)).start();
        return po;
    }
    
    public ProgressObject stop(TargetModuleID[] targetModuleID) throws java.lang.IllegalStateException {
        final ProgObject po = new ProgObject(this, targetModuleID);
        final java.util.List targetModules = java.util.Arrays.asList(targetModuleID);
        Runnable r = new Runnable() {
            public void run() {
                try { Thread.sleep(200); //some latency
                } catch (Exception e) {}
                po.setStatusStopRunning("TestPluginDM: stopping "+ targetModules);
                try { Thread.sleep(500); //super server starting time
                } catch (Exception e) {}
                if (getTestBehavior() == STOP_MODULES_FAILED) {
                    po.setStatusStopFailed("TestPluginDM: failed to stop "+targetModules);
                } else {
                    po.setStatusStopCompleted("TestPluginDM: done stopping "+targetModules);
                }
            }
        };
        
        (new Thread(r)).start();
        return po;
    }
    
    public ProgressObject undeploy(TargetModuleID[] targetModuleID) throws java.lang.IllegalStateException {
        return new ProgObject(this, targetModuleID);
    }
    
    public static final int NORMAL = 0;
    public static final int START_FAILED = 1;
    public static final int STOP_FAILED = 2;
    public static final int START_MODULES_FAILED = 3;
    public static final int STOP_MODULES_FAILED = 4;
    public static final int DISTRIBUTE_FAILED = 5;
    public static final int REDEPLOY_FAILED = 6;
    
    private int testBehavior = NORMAL;
    public void setTestBehavior(int behavior) {
        testBehavior = behavior;
    }
    public int getTestBehavior() {
        return testBehavior;
    }
    public ServerProgress createServerProgress() {
        return new DepManager.TestServerProgress();
    }
    public static final int STOPPED = 0;
    public static final int STARTING = 1;
    public static final int RUNNING = 2;
    public static final int STOPPING = 3;
    public static final int FAILED = 4;
    private int state = STOPPED;
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }
    private class TestServerProgress extends ServerProgress {
        public TestServerProgress() {
            super(DepManager.this);
        }
    }
}
