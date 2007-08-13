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

package org.netbeans.modules.tomcat5.nodes;

import java.awt.Component;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.nodes.actions.AdminConsoleAction;
import org.netbeans.modules.tomcat5.nodes.actions.ServerLogAction;
import org.netbeans.modules.tomcat5.nodes.actions.TerminateAction;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.tomcat5.customizer.Customizer;
import org.netbeans.modules.tomcat5.nodes.actions.SharedContextLogAction;
import org.netbeans.modules.tomcat5.nodes.actions.EditServerXmlAction;
import org.netbeans.modules.tomcat5.nodes.actions.OpenServerOutputAction;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.cookies.EditorCookie;
import org.openide.util.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 *
 * @author  Petr Pisl
 */

public class TomcatInstanceNode extends AbstractNode implements Node.Cookie {
    
    private TomcatManager tm;
    
    /** Creates a new instance of TomcatInstanceNode 
      @param lookup will contain DeploymentFactory, DeploymentManager, Management objects. 
     */
    public TomcatInstanceNode(Children children, Lookup lookup) {
        super(children);
        tm = (TomcatManager)lookup.lookup(TomcatManager.class);
        setIconBaseWithExtension("org/netbeans/modules/tomcat5/resources/tomcat.png"); // NOI18N
        getCookieSet().add(this);
    }
    
    public String getShortDescription() {
        return NbBundle.getMessage(
                    TomcatInstanceNode.class, 
                    "LBL_TomcatInstanceNode", 
                    String.valueOf(tm.getCurrentServerPort()));
    }
    
    public boolean hasCustomizer() {
        return true;
    }
    
    public Component getCustomizer() {
        return new Customizer(tm);
    }
    
    /** Return the TomcatManager instance this node represents. */
    public TomcatManager getTomcatManager() {
        return tm;
    }

    public javax.swing.Action[] getActions(boolean context) {
        java.util.List actions = new LinkedList();
        // terminate does not work on Windows, see issue #63157
        if (!Utilities.isWindows()) {
            actions.add(null);
            actions.add(SystemAction.get(TerminateAction.class));
        }
        actions.add(null);
        actions.add(SystemAction.get(EditServerXmlAction.class));
        actions.add(SystemAction.get(AdminConsoleAction.class));
        if (tm.isTomcat50()) {
            actions.add(SystemAction.get(SharedContextLogAction.class));
        }
        if (tm.isTomcat55()) {
            actions.add(SystemAction.get(ServerLogAction.class));
        }
        actions.add(SystemAction.get(OpenServerOutputAction.class));
        return (SystemAction[])actions.toArray(new SystemAction[actions.size()]);
    }
        
    private FileObject getTomcatConf() {
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
                Logger.getLogger(TomcatInstanceNode.class.getName()).log(Level.INFO, null, ex);
            }
            if (dataObject != null) {
                EditorCookie editorCookie = (EditorCookie)dataObject.getCookie(EditorCookie.class);
                if (editorCookie != null) {
                    editorCookie.open();
                } else {
                    Logger.getLogger(TomcatInstanceNode.class.getName()).log(Level.INFO, "Cannot find EditorCookie."); // NOI18N
                }
            }
        }
    }

    /**
     * Open the server log (output).
     */
    public void openServerLog() {
        tm.logManager().openServerLog();
    }
    
    /**
     * Can be the server log (output) displayed?
     *
     * @return <code>true</code> if the server log can be displayed, <code>false</code>
     *         otherwise.
     */
    public boolean hasServerLog() {
        return tm.logManager().hasServerLog();
    }
}
