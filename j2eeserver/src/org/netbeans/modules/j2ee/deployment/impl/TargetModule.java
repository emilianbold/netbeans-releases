/*
 * TargetModule.java
 *
 * Created on October 6, 2003, 11:12 AM
 */

package org.netbeans.modules.j2ee.deployment.impl;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import java.util.List;
import java.util.Arrays;

/**
 *
 * @author  nn136682
 */
public class TargetModule implements TargetModuleID, java.io.Serializable {
    
    private final String id;
    private final String instanceUrl;
    private final String targetName;
    private long timestamp;
    private transient TargetModuleID delegate;
    
    private static final TargetModuleID[] EMPTY_TMID_ARRAY = new TargetModuleID[0];
    
    /** Creates a new instance of TargetModule */
    public TargetModule(String id, String instanceUrl, String targetName, long timestamp, TargetModuleID delegate) {
        this(id, instanceUrl, targetName, timestamp);
        this.delegate = delegate;
    }
    public TargetModule(String id, String instanceUrl, String targetName, long timestamp) {
        if (id == null || instanceUrl == null || targetName == null || timestamp <= 0) {
            java.util.List args = Arrays.asList(new Object[] { id, instanceUrl, targetName, new Long(timestamp)});
            throw new IllegalArgumentException(NbBundle.getMessage(TargetModule.class, "MSG_BadTargetModuleAttributes", args));
        }
        this.id = id;
        this.instanceUrl = instanceUrl;
        this.targetName = targetName;
        this.timestamp = timestamp;
    }
    public TargetModule(String id, ServerString target, long timestamp) {
        this(id, target.getUrl(), target.getTargets()[0], timestamp);
    }

    public String getId() { return id; }
    public String getInstanceUrl() { return instanceUrl; }
    public String getTargetName() { return targetName; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long ts) { this.timestamp = ts; }
    
    public static class List implements java.io.Serializable {
        private TargetModule [] targetModules;
        public List(TargetModule[] targetModules) {
            this.targetModules = targetModules;
        }
        public TargetModule[] getTargetModules() {
            return targetModules;
        }
    }

    public Target findTarget() {
        ServerInstance instance = ServerRegistry.getInstance().getServerInstance(instanceUrl);
        return instance.getServerTarget(targetName).getTarget();
    }
    
    //Delegate to TargetModuleID
    public void initDelegate(ModuleType type) {
        if (delegate == null) {
            ServerInstance instance = ServerRegistry.getInstance().getServerInstance(instanceUrl);
            DeploymentManager dm = instance.getDeploymentManager();
            Target target = findTarget();

            try {
                TargetModuleID[] tmIDs = dm.getAvailableModules(type, new Target[] {target});
                for (int i=0; i<tmIDs.length; i++) {
                    if (id.equals(tmIDs[i].toString())) {
                        delegate = tmIDs[i];
                        break;
                    }
                }
            } catch (Exception e) {
                //PENDING: log error
                e.printStackTrace();
            }
        }
    }
    
    public void initDelegate(TargetModuleID delegate) { 
        this.delegate = delegate; 
    }
    
    public static TargetModuleID[] toTargetModuleID(TargetModule[] targetModules) {
        if (targetModules == null) return null;
        TargetModuleID [] ret = new TargetModuleID[targetModules.length];
        for (int i=0; i<ret.length; i++) {
            ret[i] = targetModules[i].delegate();
        }
        return ret;
    }
    
    public TargetModuleID delegate() {
        if (delegate == null) {
            throw new IllegalStateException("Delegate is not set yet"); //NOI18N
        }
        return delegate;
    }
    public javax.enterprise.deploy.spi.TargetModuleID[] getChildTargetModuleID() {
        return delegate().getChildTargetModuleID();
    }
    public String getModuleID() {
        return delegate().getModuleID();
    }
    public javax.enterprise.deploy.spi.TargetModuleID getParentTargetModuleID() {
        return delegate().getParentTargetModuleID();
    }
    public javax.enterprise.deploy.spi.Target getTarget() {
        return delegate().getTarget();
    }
    public String getWebURL() {
        return delegate().getWebURL();
    }
    public String toString() {
        if (delegate == null)
            return super.toString();
        else
            return delegate.toString();
    }
}
