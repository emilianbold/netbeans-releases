/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.nodes;

import java.awt.Component;
import java.beans.PropertyEditor;
import java.io.File;
import java.util.LinkedList;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.*;
import org.netbeans.modules.tomcat5.util.TomcatInstallUtil;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.ide.Customizer;
import org.netbeans.modules.tomcat5.nodes.actions.SharedContextLogAction;
import org.netbeans.modules.tomcat5.nodes.actions.EditServerXmlAction;
import org.netbeans.modules.tomcat5.ide.MonitorSupport;
import org.netbeans.modules.tomcat5.nodes.actions.OpenServerOutputAction;
import org.openide.actions.PropertiesAction;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.cookies.EditorCookie;
import org.openide.util.Utilities;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author  Petr Pisl
 */

public class TomcatInstanceNode extends AbstractNode implements Node.Cookie {
    
    private static String  ICON_BASE = "org/netbeans/modules/tomcat5/resources/tomcat5instance"; // NOI18N
    
    protected static final String PROPERTY_TOMCAT_HOME = "tomcat_home"; //NOI18N
    protected static final String PROPERTY_TOMCAT_BASE = "tomcat_base"; //NOI18N

    protected static final String DEBUGGER="debugger"; //NOI18N
    protected static final String DEBUGGER_PORT = "debugger_port"; //NOI18N
    protected static final String DEBUGGING_TYPE = "debugging_type"; //NOI18N
    protected static final String DISPLAY_NAME= "display_name";//NOI18N    
    protected static final String SERVER_PORT= TomcatManager.SERVER_PORT;//NOI18N
    protected static final String ADMIN_PORT= "admin_port";//NOI18N
    protected static final String MONITOR_ENABLED= "monitor_enabled";//NOI18N
    protected static final String OPEN_CONTEXT_LOG_ON_RUN_ENABLED= "open_context_log_on_run_enabled";//NOI18N
    protected static final String SECURITY_STARTUP_OPTION= "security_startup_option"; //NOI18N
    protected static final String FORCE_STOP_OPTION= "force_stop_option"; //NOI18N
    protected static final String CLASSIC = "classic"; //NOI18N
    protected static final String USER_NAME = "user_name"; //NOI18N
    protected static final String PASSWORD = "password"; //NOI18N
    protected static final String NAME_FOR_SHARED_MEMORY_ACCESS = "name_for_shared_memory_access"; //NOI18N
    private static final String DEFAULT_NAME_FOR_SHARED_MEMORY_ACCESS = "tomcat_shared_memory_id"; //NOI18N        
    
    private static final int MIN_PORT_NUMBER = 0;
    private static final int MAX_PORT_NUMBER = 65535;
    
    private Lookup lkp;
    
    /** Creates a new instance of TomcatInstanceNode 
      @param lookup will contain DeploymentFactory, DeploymentManager, Management objects. 
     */
    public TomcatInstanceNode(Children children, Lookup lookup) {
        super(children);
        //this.getChildren().add(new Node[]{new WebModuleNode(new Children.Map())});
        lkp = lookup;
        setIconBase(ICON_BASE);
        getCookieSet().add(this);
    }
    
    private int iPort = 0;
    
    
    public String getDisplayName(){
        Integer port = getServerPort();
        String portStr = "";
        if (port != null) { 
            portStr = port.toString();
        }
        return NbBundle.getMessage(TomcatInstanceNode.class, "LBL_TomcatInstanceNode",  // NOI18N
            new Object []{portStr});
    }
    
    public boolean hasCustomizer() {
        return true;
    }
    
    public Component getCustomizer() {
        return new Customizer(getTomcatManager());
    }
    
    /** Returns the TomcatManager for this node, or null if TomcatManager was not found - which 
     * should never happen.
     */
    public TomcatManager getTomcatManager() {
        DeploymentManager m = getDeploymentManager();
        return (m instanceof TomcatManager) ? (TomcatManager)m : null;
    }
    
    private Integer getServerPort () {
        TomcatManager m = getTomcatManager();
        if (m != null){
            Integer port = m.getServerPort();
            if (port != null && port.intValue() != iPort){
                iPort = port.intValue();
                setDisplayName(NbBundle.getMessage(TomcatInstanceNode.class, "LBL_TomcatInstanceNode",  // NOI18N
                    new Object []{"" + iPort}));
            }
            return port;
        };
        return TomcatManager.DEFAULT_SERVER_PORT;
    }

    private Boolean getClassic() {
        TomcatManager m = getTomcatManager();
        if (m != null){
            return  m.getClassic();
        };
        return TomcatManager.DEFAULT_CLASSIC;
    }

    private String getDebugType() {
        TomcatManager m = getTomcatManager();
        if (m != null){
            return NbBundle.getMessage(DebuggingTypeEditor.class, m.getDebugType());
        };
        return null;
    }

    private String getSharedMemory() {
        TomcatManager m = getTomcatManager();
        if (m != null){
            return  m.getSharedMemory();
        };
        return DEFAULT_NAME_FOR_SHARED_MEMORY_ACCESS;
    }

    private Integer getDebugPort () {
        TomcatManager m = getTomcatManager();
        if (m != null){
            return  m.getDebugPort();
        };
        return TomcatManager.DEFAULT_DEBUG_PORT;
    }
    
    private Integer getAdminPort () {
        TomcatManager m = getTomcatManager();
        if (m != null){
            return m.getAdminPort();
        };
        return TomcatManager.DEFAULT_ADMIN_PORT;
    }

    private void setClassic (Boolean classic) {
        TomcatManager m = getTomcatManager();
        if (m != null){
            m.setClassic(classic);
        };
    }

    private void setSharedMemory (String str) {
        TomcatManager m = getTomcatManager();
        if (m != null){
            m.setSharedMemory(str);
        };
    }

    private void setDebugType (String str) {
        DeploymentManager m = getDeploymentManager();
        if (m instanceof TomcatManager){
            ((TomcatManager)m).setDebugType(str);
        };
    }

    private void setDebugPort (Integer port) {
        TomcatManager m = getTomcatManager();
        if (m != null && validatePortNumber(port.intValue())){
            m.setDebugPort(port);
        };
    }

    public javax.swing.Action[] getActions(boolean context) {
        TomcatManager tm = getTomcatManager();
        java.util.List actions = new LinkedList();
        actions.add(null);
        actions.add(SystemAction.get(EditServerXmlAction.class));
        if (tm != null && tm.isTomcat50()) {
            actions.add(SystemAction.get(SharedContextLogAction.class));
        }
        actions.add(SystemAction.get(OpenServerOutputAction.class));
        actions.add(null);
        actions.add(SystemAction.get(PropertiesAction.class));
        return (SystemAction[])actions.toArray(new SystemAction[actions.size()]);
    }

    DeploymentManager getDeploymentManager() {
        return (DeploymentManager)lkp.lookup(DeploymentManager.class);
    }
    
    private String getHome() {
        TomcatManager m = getTomcatManager();
        if (m != null) {
            File homeDir = m.getCatalinaHomeDir();
            if (homeDir != null) return homeDir.getAbsolutePath();
        }
        return ""; // NOI18N
    }
    
    private String getBase() {
        TomcatManager m = getTomcatManager();
        if (m != null){
            if (m.getCatalinaBase() != null) {
                return m.getCatalinaBaseDir().getAbsolutePath();
            } else {
                return m.getCatalinaHomeDir().getAbsolutePath();
            }
        }
        return ""; // NOI18N
    }

    /** Getter for property userName.
     * @return Value of property userName.
     *
     */
    private String getUserName() {
        DeploymentManager m = getDeploymentManager();
        if (m instanceof TomcatManager){
            return ((TomcatManager)m).getUsername();
        };
        return ""; //NOI18N
    }    
    
    /** Setter for property userName.
     * @param userName New value of property userName.
     *
     */
    private void setUserName(String userName) {
        DeploymentManager m = getDeploymentManager();
        if (m instanceof TomcatManager){
            ((TomcatManager)m).setUsername(userName);
        };
    }
    
    /** Getter for property password.
     * @return Value of property password.
     *
     */
    private String getPassword() {
        return NbBundle.getMessage(TomcatInstanceNode.class, "LBL_password");
    }
    
    /** Setter for property password.
     * @param password New value of property password.
     *
     */
    private void setPassword(String password) {
        DeploymentManager m = getDeploymentManager();
        if (m instanceof TomcatManager){
            ((TomcatManager)m).setPassword(password);
        };
    }
    
    // Create a property sheet:
    protected Sheet createSheet () {
	Sheet sheet = super.createSheet ();
        
        // PROPERTIES
        Sheet.Set ssProp = sheet.get (Sheet.PROPERTIES);       
        if (ssProp == null) {
	    ssProp = Sheet.createPropertiesSet ();
            sheet.put (ssProp);
	}
        Node.Property p;
        
        // DISPLAY NAME
        p = new PropertySupport.ReadWrite(
                   DISPLAY_NAME,
                   String.class,
                   NbBundle.getMessage(TomcatInstanceNode.class, "PROP_displayName"),   // NOI18N
                   NbBundle.getMessage(TomcatInstanceNode.class, "HINT_displayName")   // NOI18N
               ) {
                   public Object getValue() {
                       TomcatManager mng = getTomcatManager();
                       if (mng != null) {
                           return mng.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
                       }                       
                       return getDisplayName();
                   }
                   
                   public void setValue(Object val) {
                       TomcatManager mng = getTomcatManager();
                       if (mng != null) {
                           mng.getInstanceProperties().setProperty(InstanceProperties.DISPLAY_NAME_ATTR, (String)val);
                       }
                   }
               };
        ssProp.put(p);
        
        // SERVER PORT
        p = new PropertySupport.ReadWrite (
                   SERVER_PORT,
                   Integer.TYPE,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_serverPort"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_serverPort")   // NOI18N
               ) {
                   public Object getValue () {
                       return getServerPort();
                   }
                   
                   public void setValue (Object val){
                       TomcatManager mng = getTomcatManager();
                       if (mng!=null) {
                           if (mng.isRunning(false)) {
                               TomcatInstallUtil.notifyThatRunning(mng);
                           } else {
                               Integer newPort = (Integer)val;
                               if (setServerPort(newPort)) {
                                    mng.setServerPort(newPort);
                               }
                           }
                       }
                   }
               };
        ssProp.put(p);  
        
        // ADMIN PORT
        p = new PropertySupport.ReadWrite (
                   ADMIN_PORT,
                   Integer.TYPE,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_adminPort"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_adminPort")   // NOI18N
               ) {
                   public Object getValue () {
                       return getAdminPort();
                   }
                   
                   public void setValue (Object val){
                       TomcatManager mng = getTomcatManager();
                       if (mng!=null) {
                           if (mng.isRunning(false)) {
                               TomcatInstallUtil.notifyThatRunning(mng);
                           } else {
                               Integer newPort = (Integer)val;
                               if (setAdminPort(newPort)) mng.setAdminPort(newPort);
                           }
                       }
                   }
               };    
        ssProp.put(p);
        
        // USERNAME
        p = new PropertySupport.ReadWrite (
                   USER_NAME,
                   String.class,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_userName"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_userName")   // NOI18N
               ) {
                   public Object getValue () {
                       return getUserName();
                   }
                   
                   public void setValue (Object val){
                       setUserName((String)val);
                   }
               };    
        ssProp.put(p);
        
        // PASSWORD
        p = new PropertySupport.ReadWrite (
                   PASSWORD,
                   String.class,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_password"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_password")   // NOI18N
               ) {
                   public Object getValue () {
                       return getPassword();
                   }
                   
                   public void setValue (Object val){
                       setPassword((String)val);
                   }
               };    
        ssProp.put(p);
        
        // TOMCAT HOME
        p = new PropertySupport.ReadOnly(
                   PROPERTY_TOMCAT_HOME,
                   String.class,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_tomcatHome"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_tomcatHome")   // NOI18N
               ) {
                   public Object getValue () {
                       return getHome();
                   }
               };    
        ssProp.put(p);
        
        // TOMCAT BASE
        p = new PropertySupport.ReadOnly(
                   PROPERTY_TOMCAT_BASE,
                   String.class,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_tomcatBase"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_tomcatBase")   // NOI18N
               ) {
                   public Object getValue () {
                       return getBase();
                   }
               };    
        ssProp.put(p);
        
        // MONITOR ENABLED
        p = new PropertySupport.ReadWrite (
        MONITOR_ENABLED,
        Boolean.TYPE,
        NbBundle.getMessage (TomcatInstanceNode.class, "PROP_monitorEnabled"),   // NOI18N
        NbBundle.getMessage (TomcatInstanceNode.class, "HINT_monitorEnabled")   // NOI18N
        ) {
            public Object getValue () {
                TomcatManager tm = getTomcatManager();
                if (tm != null) {
                    return Boolean.valueOf(MonitorSupport.getMonitorFlag(tm));
                }
                else return null;
            }
            
            public void setValue (Object val){
                TomcatManager tm = getTomcatManager();
                if (tm != null) {
                    boolean b = ((Boolean)val).booleanValue();
                    MonitorSupport.setMonitorFlag(tm, b);
                }
            }
        };
        ssProp.put(p);
        
        TomcatManager tm = getTomcatManager();
        if (tm != null && tm.isTomcat50()) {
            // OPEN CONTEXT LOG ON RUN ENABLED
            p = new PropertySupport.ReadWrite (
                OPEN_CONTEXT_LOG_ON_RUN_ENABLED,
                Boolean.TYPE,
                NbBundle.getMessage (TomcatInstanceNode.class, "PROP_openLogOnRunEnabled"),   // NOI18N
                NbBundle.getMessage (TomcatInstanceNode.class, "HINT_openLogOnRunEnabled")   // NOI18N
            ) {
                public Object getValue () {
                    TomcatManager tm = getTomcatManager();
                    if (tm != null) {
                        return Boolean.valueOf(tm.getOpenContextLogOnRun());
                    }
                    return Boolean.TRUE;
                }

                public void setValue (Object val) {
                    TomcatManager tm = getTomcatManager();
                    if (tm != null) {
                        tm.setOpenContextLogOnRun(((Boolean)val).booleanValue());
                    }
                }
            };
            ssProp.put(p);
        }
        
        // SECURITY STARTUP OPTION
        p = new PropertySupport.ReadWrite(
                   SECURITY_STARTUP_OPTION,
                   Boolean.class,
                   NbBundle.getMessage(TomcatInstanceNode.class, "PROP_securityStartupOption"),   // NOI18N
                   NbBundle.getMessage(TomcatInstanceNode.class, "HINT_securityStartupOption")   // NOI18N
               ) {
                   
                    public Object getValue () {
                        TomcatManager tm = getTomcatManager();
                        if (tm != null) {
                            return Boolean.valueOf(tm.getSecurityStartupOption());
                        }
                        return Boolean.TRUE;
                    }

                    public void setValue (Object val) {
                        TomcatManager tm = getTomcatManager();
                        if (tm != null) {
                            tm.setSecurityStartupOption(((Boolean)val).booleanValue());
                        }
                    }
                   
        };
        ssProp.put(p);
        
        // FORCE STOP OPTION - UNIX specific option
        if (Utilities.isUnix()) {
            p = new PropertySupport.ReadWrite(
                       FORCE_STOP_OPTION,
                       Boolean.class,
                       NbBundle.getMessage(TomcatInstanceNode.class, "PROP_forceStopOption"),   // NOI18N
                       NbBundle.getMessage(TomcatInstanceNode.class, "HINT_forceStopOption")   // NOI18N
                   ) {

                        public Object getValue () {
                            TomcatManager tm = getTomcatManager();
                            if (tm != null) {
                                return Boolean.valueOf(tm.getForceStopOption());
                            }
                            return Boolean.TRUE;
                        }

                        public void setValue (Object val) {
                            TomcatManager tm = getTomcatManager();
                            if (tm != null) {
                                tm.setForceStopOption(((Boolean)val).booleanValue());
                            }
                        }

            };
            ssProp.put(p);
        }
        
        // DEBUGGER
        Sheet.Set ssDebug = new Sheet.Set ();
        ssDebug.setName(DEBUGGER);
        ssDebug.setDisplayName(NbBundle.getMessage (TomcatInstanceNode.class, "PROP_debuggerSetName"));  // NOI18N
        ssDebug.setShortDescription(NbBundle.getMessage (TomcatInstanceNode.class, "HINT_debuggerSetName"));  // NOI18N
        
        // DEBUGGER PORT
        p = new PropertySupport.ReadWrite (
                   DEBUGGER_PORT,
                   Integer.TYPE,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_debuggerPort"),  // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_debuggerPort")  // NOI18N
               ) {
                   public Object getValue () {
                       return getDebugPort();
                   }
                   
                   public void setValue (Object val){
                       TomcatManager mng = getTomcatManager();
                       if (mng!=null) {
                           if (mng.isRunning(false)) {
                               TomcatInstallUtil.notifyThatRunning(mng);
                           } else {
                               setDebugPort((Integer)val);
                           }
                       }
                   }                   
               };      
        ssDebug.put(p);
        
        // CLASSIC
        p = new PropertySupport.ReadWrite (
                   CLASSIC,
                   Boolean.TYPE,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_classic"),  // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_classic")  // NOI18N
               ) {
                   public Object getValue () {
                       return getClassic();
                   }
                   
                   public void setValue (Object val){
                       TomcatManager mng = getTomcatManager();
                       if (mng!=null) {
                           if (mng.isRunning(false)) {
                               TomcatInstallUtil.notifyThatRunning(mng);
                           } else {
                               setClassic((Boolean)val);
                           }
                       }
                       
                   }                   
               };      
               
        ssDebug.put(p);
                
        if (org.openide.util.Utilities.isWindows()) {
            // DEBUGGING TYPE
            p = new PropertySupport.ReadWrite (
                       DEBUGGING_TYPE,
                       String.class,
                       NbBundle.getMessage (TomcatInstanceNode.class, "PROP_debuggingType"),   // NOI18N
                       NbBundle.getMessage (TomcatInstanceNode.class, "HINT_debuggingType")  // NOI18N
                   ) {
                       public Object getValue () {
                           return getDebugType();
                       }

                       public void setValue (Object val) {
                           TomcatManager mng = getTomcatManager();
                           if (mng!=null) {
                               if (mng.isRunning(false)) {
                                   TomcatInstallUtil.notifyThatRunning(mng);
                               } else {
                                    setDebugType((String)val);
                               }
                           }
                       }

                       public PropertyEditor getPropertyEditor(){
                           return new DebuggingTypeEditor();
                       }
                   };
            ssDebug.put(p);
            
            // NAME FOR SHARED MEMORY ACCESS
            p = new PropertySupport.ReadWrite (
                       NAME_FOR_SHARED_MEMORY_ACCESS,
                       String.class,
                       NbBundle.getMessage (TomcatInstanceNode.class, "PROP_nameForSharedMemoryAccess"),  // NOI18N
                       NbBundle.getMessage (TomcatInstanceNode.class, "HINT_nameForSharedMemoryAccess")  // NOI18N
                   ) {
                       public Object getValue () {
                           return getSharedMemory();
                       }

                       public void setValue (Object val){
                           TomcatManager mng = getTomcatManager();
                           if (mng!=null) {
                               if (mng.isRunning(false)) {
                                   TomcatInstallUtil.notifyThatRunning(mng);
                               } else {
                                   setSharedMemory((String)val);
                               }
                           }
                       }
                   };
            ssDebug.put(p);  
        }
        sheet.put(ssDebug);

        return sheet;
    }
    
    private boolean setServerPort(Integer port) {
        FileObject fo = getTomcatConf();
        if (fo==null || !validatePortNumber(port.intValue())) {
            return false;
        }
        return TomcatInstallUtil.setServerPort (port, fo);
    }
        
    private boolean setAdminPort(Integer port) {
        FileObject fo = getTomcatConf();
        if (fo==null || !validatePortNumber(port.intValue())) {
            return false;
        }
        return TomcatInstallUtil.setAdminPort (port, fo);
    }
    
    private boolean validatePortNumber(int port) {
        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
            String msg = NbBundle.getMessage(TomcatInstanceNode.class,
                    "MSG_outOfPortRange", new Integer(port), // NOI18N
                    new Integer(MIN_PORT_NUMBER), new Integer(MAX_PORT_NUMBER));
            NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                    msg, NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(notDesc);
            return false;
        } else {
            return true;
        }
    }
    
    private FileObject getTomcatConf() {
        FileObject base = getTomcatManager().getCatalinaBaseFileObject();
        if (base != null) {
            return base.getFileObject("conf/server.xml"); //NOI18N
        }
        return null;
    }
    
    /**
     * Open server.xml file in editor.
     */
    public void editServerXml() {
        FileObject fileObject = getTomcatConf();
        if (fileObject != null) {
            DataObject dataObject = null;
            try {
                dataObject = DataObject.find(fileObject);
            } catch(DataObjectNotFoundException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            if (dataObject != null) {
                EditorCookie editorCookie = (EditorCookie)dataObject.getCookie(EditorCookie.class);
                if (editorCookie != null) {
                    editorCookie.open();
                } else {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot find EditorCookie."); // NOI18N
                }
            }
        }
    }

    /**
     * Open the server log (output).
     */
    public void openServerLog() {
        getTomcatManager().logManager().openServerLog();
    }
    
    /**
     * Can be the server log (output) displayed?
     *
     * @return <code>true</code> if the server log can be displayed, <code>false</code>
     *         otherwise.
     */
    public boolean hasServerLog() {
        return getTomcatManager().logManager().hasServerLog();
    }
}
