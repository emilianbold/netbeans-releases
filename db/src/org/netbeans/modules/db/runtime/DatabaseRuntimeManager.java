/*
 * DatabaseRuntimeManager.java
 *
 * Created on June 22, 2004, 10:28 AM
 */

package org.netbeans.modules.db.runtime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.RootNodeInfo;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;

/**
 *
 * @author  nn136682
 */
public class DatabaseRuntimeManager {
    
    private static DatabaseRuntimeManager manager = null;
    private Map runtimes = null;
    
    /** Creates a new instance of DatabaseRuntimeManager */
    public DatabaseRuntimeManager() {
    }
    
    static public DatabaseRuntimeManager getDefault() {
        
        if (manager == null)
            manager = new DatabaseRuntimeManager();
        return manager;
    }
    
    private Map getRuntimesMap() {
        if (runtimes == null) {
            runtimes = new HashMap();
        }
        return runtimes;
    }
    
    /**
     * Register the runtime.
     *
     * Note: Each databaser server supporting module need to do registration for all its runtimes
     * in ModuleInstall.restored. The registration is not persisted.
     */
    public void register(String jdbcDriverClassName, DatabaseRuntime runtime) {
        getRuntimesMap().put(jdbcDriverClassName, runtime);
    }
    
    /**
     * Unregister the runtime.
     */
    public void unregister(String jdbcDriverClassName, DatabaseRuntime runtime) {
        DatabaseRuntime current = getRuntime(jdbcDriverClassName);
        if (current != null && current.equals(runtime)) {
            getRuntimesMap().remove(jdbcDriverClassName);
        }
    }

    public DatabaseRuntime getRuntime(String jdbcDriverClassName) {
        return (DatabaseRuntime) getRuntimesMap().get(jdbcDriverClassName);
    }
    
    public DatabaseRuntime[] getRuntimes() {
        return (DatabaseRuntime[]) getRuntimesMap().values().toArray(new DatabaseRuntime[getRuntimesMap().size()]);
    }
    
    /**
     * Create a database connection object without tryign to connect.
     */
    public void createConnection(String driver, String url, String user, String password, String schema) {
        RootNodeInfo ninf = getRootNodeInfo();
        if (ninf == null)
            return;
        DatabaseConnection conn = new DatabaseConnection(driver, url, user, password);
        conn.setSchema(schema);
        conn.setRememberPassword(true);
        ninf.addDatabaseConnection(conn);
    }
    
    public void createOrReplaceConnection(String driver, String url, String user, String password, String schema) {
        removeConnection(url);
        createConnection(driver, url, user, password, schema);
    }
    
    public void removeConnection(String url) {
        RootNodeInfo ninf = getRootNodeInfo();
        if (ninf == null || url == null)
            return;

        try {
            for (Iterator i = ninf.getChildren().iterator(); i.hasNext();) {
                Object dni = i.next();
                if (! (dni instanceof ConnectionNodeInfo))
                continue;
            
                ConnectionNodeInfo cni = (ConnectionNodeInfo) dni;
                if (url.equalsIgnoreCase(cni.getURL())) {
                    cni.disconnect();
                    cni.delete();
                    cni.refreshChildren();
                    break;
                }
            }
        } catch(Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    public static RootNodeInfo getRootNodeInfo() {
        try {
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("UI/Runtime"); //NOI18N
            DataFolder df = (DataFolder) DataObject.find(fo);
            Node environment = df.getNodeDelegate();
            Node n = environment.getChildren().findChild("Databases"); //NOI18N
            RootNodeInfo ret = (RootNodeInfo) n.getCookie(RootNodeInfo.class);
            return ret; 
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
    }
}
