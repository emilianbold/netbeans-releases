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

import java.beans.PropertyEditor;
import java.util.*;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

import javax.enterprise.deploy.spi.DeploymentManager;

/**
 *
 * @author  Petr Pisl
 */

public class TomcatInstanceNode extends AbstractNode implements Node.Cookie {
    
    private static String  ICON_BASE = "org/netbeans/modules/tomcat5/resources/tomcat5"; // NOI18N
    
    protected static final String PROPERTY_TOMCAT_HOME = "tomcat_home"; //NOI18N
    protected static final String PROPERTY_TOMCAT_BASE = "tomcat_base"; //NOI18N

    protected static final String DEBUGGER="debugger"; //NOI18N
    protected static final String DEBUGGER_PORT = "debugger_port"; //NOI18N
    protected static final String DEBUGGING_TYPE = "debugging_type"; //NOI18N
    protected static final String SERVER_PORT= "server_port";//NOI18N
    protected static final String ADMIN_PORT= "admin_port";//NOI18N
    protected static final String CLASSIC = "classic"; //NOI18N
    protected static final String NAME_FOR_SHARED_MEMORY_ACCESS = "name_for_shared_memory_access"; //NOI18N
    private static final String DEFAULT_NAME_FOR_SHARED_MEMORY_ACCESS = "tomcat_shared_memory_id"; //NOI18N
    
    private Lookup lkp;
    
    /** Creates a new instance of TomcatInstanceNode 
      @param lookup will contain DeploymentFactory, DeploymentManager, Management objects. 
     */
    public TomcatInstanceNode(Children children, Lookup lookup) {
        super(children);
        this.getChildren().add(new Node[]{new WebModuleNode(new Children.Map())});
        lkp = lookup;
        setIconBase(ICON_BASE);

        getCookieSet().add(this);
    }
    
    private int iPort = 0;
    
    public String getDisplayName(){
        Integer port = getServerPort();
        String portStr = "";
        System.out.println("getDisplayName() port: "+  getServerPort());
        if (port != null) { 
            portStr = port.toString();
        }
        return NbBundle.getMessage(TomcatInstanceNode.class, "LBL_TomcatInstanceNode",  // NOI18N
            new Object []{portStr});
    }
    
    private Integer getServerPort () {
        DeploymentManager m = (DeploymentManager)lkp.lookup(DeploymentManager.class);
        if (m instanceof TomcatManager){
            Integer port = ((TomcatManager)m).getServerPort();
            if (port != null && port.intValue() != iPort){
                iPort = port.intValue();
                setDisplayName(NbBundle.getMessage(TomcatInstanceNode.class, "LBL_TomcatInstanceNode",  // NOI18N
                    new Object []{"" + iPort}));
            }
            return port;
        };
        return new Integer(8080);
    }
    
    public javax.swing.Action[] getActions(boolean context) {
        return new SystemAction[] {
                   SystemAction.get (AccessLogAction.class),
                   SystemAction.get (ContextLogAction.class)
               };        
    }

    DeploymentManager getDeploymentManager() {
        return (DeploymentManager)lkp.lookup(DeploymentManager.class);
    }

    private String getHome() {
        DeploymentManager m = (DeploymentManager)lkp.lookup(DeploymentManager.class);
        if (m instanceof TomcatManager){
            return ((TomcatManager)m).getCatalinaHome();
        }
        return "";
    }
    
    private String getBase() {
        DeploymentManager m = (DeploymentManager)lkp.lookup(DeploymentManager.class);
        if (m instanceof TomcatManager) {
            if (((TomcatManager)m).getCatalinaBase() != null) {
                return ((TomcatManager)m).getCatalinaBase();
            } else {
                return ((TomcatManager)m).getCatalinaHome();
            }
        }
        return "";
    }

    // Create a property sheet:
    protected Sheet createSheet () {
	Sheet sheet = super.createSheet ();
        Sheet.Set ssProp = sheet.get (Sheet.PROPERTIES);
        if (ssProp == null) {
	    ssProp = Sheet.createPropertiesSet ();
            sheet.put (ssProp);
	}
        
        // This is workaround for displaying properties in Properties cathegory more times
        /*Sheet.Set ssProp = new Sheet.Set ();
        ssProp.setName("_xxproperties");      // NOI18N
        ssProp.setDisplayName("Properties");  // NOI18N
        sheet.put (ssProp);
        */
        
        Node.Property p;
        p = new PropertySupport.ReadWrite (
                   SERVER_PORT,
                   Integer.TYPE,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_serverPort"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_serverPort")   // NOI18N
               ) {
                   public Object getValue () {
                       // TODO 
                       return getServerPort();
                   }
                   
                   public void setValue (Object val){
                       // TODO
                   }
               };    
        ssProp.put(p);  
        p = new PropertySupport.ReadWrite (
                   ADMIN_PORT,
                   Integer.TYPE,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_adminPort"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_adminPort")   // NOI18N
               ) {
                   public Object getValue () {
                       // TODO 
                       return new Integer(0);
                   }
                   
                   public void setValue (Object val){
                       // TODO
                   }
               };    
        ssProp.put(p);  
        p = new PropertySupport.ReadOnly(
                   PROPERTY_TOMCAT_HOME,
                   String.class,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_tomcatHome"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_tomcatHome")   // NOI18N
               ) {
                   public Object getValue () {
                       // TODO
                       return getHome();
                   }
               };    
        ssProp.put(p);
        p = new PropertySupport.ReadOnly(
                   PROPERTY_TOMCAT_BASE,
                   String.class,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_tomcatBase"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_tomcatBase")   // NOI18N
               ) {
                   public Object getValue () {
                       //TODO
                       return getBase();
                   }
               };    
        ssProp.put(p);
        
        
        Sheet.Set ssDebug = new Sheet.Set ();
        ssDebug.setName(DEBUGGER);
        ssDebug.setDisplayName(NbBundle.getMessage (TomcatInstanceNode.class, "PROP_debuggerSetName"));  // NOI18N
        ssDebug.setShortDescription(NbBundle.getMessage (TomcatInstanceNode.class, "HINT_debuggerSetName"));  // NOI18N
        
        ssDebug.setValue("helpID", "tomcat_node_ssProp");// NOI18N

        p = new PropertySupport.ReadWrite (
                   DEBUGGER_PORT,
                   Integer.TYPE,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_debuggerPort"),  // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_debuggerPort")  // NOI18N
               ) {
                   public Object getValue () {
                       // TODO obtain debugger port
                       return new Integer(11555);  // NOI18N
                   }
                   
                   public void setValue (Object val){
                       // TODO set debugger port
                   }                   
               };      
        ssDebug.put(p);
        
        p = new PropertySupport.ReadWrite (
                   CLASSIC,
                   Boolean.TYPE,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_classic"),  // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_classic")  // NOI18N
               ) {
                   public Object getValue () {
                       //TODO we need obtain this value somewhere
                       return Boolean.TRUE;
                   }
                   
                   public void setValue (Object val){
                       //TODO we need store this value somewhere
                       
                   }                   
               };      
               
        ssDebug.put(p);
        
        if (org.openide.util.Utilities.isWindows()) {
            p = new PropertySupport.ReadWrite (
                       DEBUGGING_TYPE,
                       String.class,
                       NbBundle.getMessage (TomcatInstanceNode.class, "PROP_debuggingType"),   // NOI18N
                       NbBundle.getMessage (TomcatInstanceNode.class, "HINT_debuggingType")  // NOI18N
                   ) {
                       public Object getValue () {
                           //TODO we need obtain this value
                           return "socket";   // NOI18N
                       }

                       public void setValue (Object val){
                            // TODO we need store 
                       }

                       public PropertyEditor getPropertyEditor(){
                           return new DebuggingTypeEditor();
                       }
                   };
            ssDebug.put(p);        
            p = new PropertySupport.ReadWrite (
                       NAME_FOR_SHARED_MEMORY_ACCESS,
                       String.class,
                       NbBundle.getMessage (TomcatInstanceNode.class, "PROP_nameForSharedMemoryAccess"),  // NOI18N
                       NbBundle.getMessage (TomcatInstanceNode.class, "HINT_nameForSharedMemoryAccess")  // NOI18N
                   ) {
                       public Object getValue () {
                           //TODO we need obtain this value
                           return DEFAULT_NAME_FOR_SHARED_MEMORY_ACCESS;
                       }

                       public void setValue (Object val){
                           //TODO we need store
                       }
                   };
            ssDebug.put(p);  
        }
        sheet.put(ssDebug);
        
        return sheet;
    }
    
    class WebModuleNode extends AbstractNode{
         public WebModuleNode(Children children) {
            super(children);
            setIconBase(ICON_BASE);
            setDisplayName("Web Modules");
         }
    }
}
