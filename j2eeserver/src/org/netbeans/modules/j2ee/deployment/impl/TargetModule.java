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
    
    private static final long serialVersionUID = 69446832504L;

    private final String id;
    private final String instanceUrl;
    private final String targetName;
    private final long timestamp;
    private final String contentDirectory;
    private final String contextRoot;
    private transient TargetModuleID delegate;
    private static final TargetModuleID[] EMPTY_TMID_ARRAY = new TargetModuleID[0];
    
    /** Creates a new instance of TargetModule */
    public TargetModule(String id, String url, long timestamp, String contentDir, String contextRoot, TargetModuleID delegate) {
        this(id, url, delegate.getTarget().getName(), timestamp, contentDir, contextRoot);
        this.delegate = delegate;
    }
    
    public TargetModule(String url, long timestamp, String contentDir, String contextRoot, TargetModuleID delegate) {
        this(delegate.toString(), url, delegate.getTarget().getName(), timestamp, contentDir, contextRoot);
        this.delegate = delegate;
    }

    public TargetModule(String id, String url, String targetName, long timestamp, String contentDir, String contextRoot) {
        if (id == null || url == null || targetName == null || timestamp <= 0) {
            java.util.List args = Arrays.asList(new Object[] { id, url, targetName, new Long(timestamp)});
            throw new IllegalArgumentException(NbBundle.getMessage(TargetModule.class, "MSG_BadTargetModuleAttributes", args));
        }
        this.id = id;
        this.instanceUrl = url;
        this.targetName = targetName;
        this.timestamp = timestamp;
        this.contentDirectory = contentDir;
        this.contextRoot = contextRoot;
    }

    /* wrapper for map/set operation only */
    public TargetModule(TargetModuleID delegate) {
        this("bogus", 1, null, null,delegate);
    }
    
    public String getId() { return id; }
    public String getInstanceUrl() { return instanceUrl; }
    public String getTargetName() { return targetName; }
    public long getTimestamp() { return timestamp; }
    //public void setTimestamp(long ts) { this.timestamp = ts; }
    public String getContentDirectory() {
        return contentDirectory;
    }
    public String getContextRoot() {
        return contextRoot;
    }
    
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
        if (targetModules == null) return new TargetModuleID[0];
        TargetModuleID [] ret = new TargetModuleID[targetModules.length];
        for (int i=0; i<ret.length; i++) {
            ret[i] = targetModules[i].delegate();
        }
        return ret;
    }

    public static Target[] toTarget(TargetModule[] targetModules) {
        if (targetModules == null) return new Target[0];
        Target[] ret = new Target[targetModules.length];
        for (int i=0; i<ret.length; i++) {
            ret[i] = targetModules[i].delegate().getTarget();
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
            return id; //issue 37930
        else
            return delegate.toString();
    }
    public int hashCode() {
        return id.hashCode();
    }
    public boolean equals(Object obj) {
        if (obj instanceof TargetModuleID) {
            return id.equals(((TargetModuleID)obj).toString());
        }
        return false;
    }
}
