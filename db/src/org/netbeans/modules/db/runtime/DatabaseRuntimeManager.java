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
    /**
     * Create a database connection object without tryign to connect.
     */    
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
    /**
     * returns null or a DatabaseConnection oject that matches the 'name' argument
     * for example, the name can be like: jdbc:pointbase://localhost:9092/sample [pbpublic on PBPUBLIC]
     * which is the name used for the display of the connection node in the explorer
     * This api is now used in 4.1 for the app server for cmp ejb creation wizard.
     * Keep it when we change the way db module is implemented.
     * 
     */ 
    public static DatabaseConnection getDatabaseConnection(String name){
        if(name != null){
            try{
                //let's find the set of connections via node. I know, ugly, but this is what 
                // we have in the db module space: model and nodes are intermixed.
                Node dbNode;
                FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("UI/Runtime"); //NOI18N
                DataFolder df;
                try {
                    df = (DataFolder) DataObject.find(fo);
                } catch (org.openide.loaders.DataObjectNotFoundException exc) {
                    return null;
                }
                dbNode = df.getNodeDelegate().getChildren().findChild("Databases"); //NOI18N
                String waitNode = org.openide.util.NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("WaitNode"); //NOI18N
                Node[] n = dbNode.getChildren().getNodes();
                while (n.length == 1 && waitNode.equals(n[0].getName())) {
                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException e) {
                        //PENDING
                    }
                    n = dbNode.getChildren().getNodes();
                }
                
                
                Node.Cookie cookie;
                ConnectionNodeInfo info;
                //lookup all the connections by name:
                for (int i = 0; i < n.length; i++) {
                    cookie = n[i].getCookie(ConnectionNodeInfo.class);
                    if (cookie != null) {
                        info = (ConnectionNodeInfo) cookie;
                        if (n[i].getDisplayName().equals(name)){
                            // we found the right one
                            return (DatabaseConnection) (info. getDatabaseConnection());
                        }
                    }
                }
                
                

            }catch(Exception ex){
                // Connection could not be found
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return null;
    }
}
