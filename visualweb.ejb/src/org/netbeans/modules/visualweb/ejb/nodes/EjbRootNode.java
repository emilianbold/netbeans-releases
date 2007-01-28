/*
 * EjbNode.java
 *
 * Created on April 30, 2004, 3:46 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;

import org.netbeans.modules.visualweb.ejb.actions.AddEjbGroupAction;
import org.netbeans.modules.visualweb.ejb.actions.ExportAllEjbDataSourcesAction;
import org.netbeans.modules.visualweb.ejb.actions.ImportEjbDataSourceAction;
import java.awt.Image;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * This is the parent node for the Enterprise Java Beans data source.
 * The only valid action on this node is to add a new ejb group 
 * which is handled by the AddEjbGroupAction.java
 *
 * @author cao
 */

public class EjbRootNode extends AbstractNode {
    
    public EjbRootNode() {
        super( new EjbRootNodeChildren() );
        
        // Set FeatureDescriptor stuff:
        setName( NbBundle.getMessage(EjbRootNode.class, "ENTERPRISE_JAVA_BEANS") );
        setDisplayName( NbBundle.getMessage(EjbRootNode.class, "ENTERPRISE_JAVA_BEANS") );
        setShortDescription( NbBundle.getMessage(EjbRootNode.class, "ENTERPRISE_JAVA_BEANS") );
    }
    
    public Image getIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/ejb_modul_project.png");
    }
    
    public Image getOpenedIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/ejb_modul_project.png");
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get( AddEjbGroupAction.class ),
            SystemAction.get( ImportEjbDataSourceAction.class ),
            SystemAction.get( ExportAllEjbDataSourcesAction.class )
        };
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(AddEjbGroupAction.class);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("projrave_ui_elements_server_nav_ejb_node");
    }
    
    protected EjbRootNodeChildren getEjbRootNodeChildren() {
        return (EjbRootNodeChildren)getChildren();
    }
}
