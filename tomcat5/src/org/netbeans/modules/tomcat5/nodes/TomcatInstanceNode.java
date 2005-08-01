/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.nodes;

import java.awt.Component;
import java.awt.Image;
import java.util.LinkedList;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.*;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.tomcat5.customizer.Customizer;
import org.netbeans.modules.tomcat5.nodes.actions.SharedContextLogAction;
import org.netbeans.modules.tomcat5.nodes.actions.EditServerXmlAction;
import org.netbeans.modules.tomcat5.nodes.actions.OpenServerOutputAction;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.cookies.EditorCookie;
import org.openide.util.Utilities;
import org.openide.ErrorManager;


/**
 *
 * @author  Petr Pisl
 */

public class TomcatInstanceNode extends AbstractNode implements Node.Cookie {
    
    private static String ICON_BASE = "org/netbeans/modules/tomcat5/resources/tomcat5instance.png"; // NOI18N
    
    private Lookup lkp;
    
    /** Creates a new instance of TomcatInstanceNode 
      @param lookup will contain DeploymentFactory, DeploymentManager, Management objects. 
     */
    public TomcatInstanceNode(Children children, Lookup lookup) {
        super(children);
        //this.getChildren().add(new Node[]{new WebModuleNode(new Children.Map())});
        lkp = lookup;
        getCookieSet().add(this);
    }
    
    private int iPort = 0;
    
    public String getDisplayName(){
        int port = getTomcatManager().getCurrentServerPort();        
        return NbBundle.getMessage(TomcatInstanceNode.class, "LBL_TomcatInstanceNode", Integer.toString(port));
    }
    
    
    public Image getIcon(int type) {
        return Utilities.loadImage(ICON_BASE);
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    public boolean hasCustomizer() {
        return true;
    }
    
    public Component getCustomizer() {
        return new Customizer(getTomcatManager());
    }
    
    /** Returns the TomcatManager instance for this node. */
    public TomcatManager getTomcatManager() {
        DeploymentManager dm = (DeploymentManager)lkp.lookup(DeploymentManager.class);
        assert dm instanceof TomcatManager : 
            "Illegal deployment manager instance: " + dm.getClass().getName(); // NOI18N
        return (TomcatManager)dm;
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
        return (SystemAction[])actions.toArray(new SystemAction[actions.size()]);
    }
        
    private FileObject getTomcatConf() {
        TomcatManager tm = getTomcatManager();
        tm.ensureCatalinaBaseReady(); // generated the catalina base folder if empty
        TomcatProperties tp = tm.getTomcatProperties();
        return FileUtil.toFileObject(tp.getServerXml());
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
