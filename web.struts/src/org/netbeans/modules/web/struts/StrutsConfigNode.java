/*
 * StrutsConfigNode.java
 *
 * Created on May 9, 2005, 1:35 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.web.struts;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;

/**
 *
 * @author Petr Pisl
 */

public class StrutsConfigNode extends DataNode {
    
    public static final String ICON_BASE =  "org/netbeans/modules/web/struts/resources/StrutsConfigIcon.gif";
    
    /** Creates a new instance of StrutsConfigNode */
    public StrutsConfigNode (final StrutsConfigDataObject dataObject) {
	super(dataObject,Children.LEAF);
        setIconBaseWithExtension(ICON_BASE);
    }
    
    // test to see if we can use DeleteAction
    public boolean canDestroy() {
	return true;
    }
}
