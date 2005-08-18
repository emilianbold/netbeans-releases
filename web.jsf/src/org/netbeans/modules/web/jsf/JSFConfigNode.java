/*
 * StrutsConfigNode.java
 *
 * Created on May 9, 2005, 1:35 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.web.jsf;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;

/**
 *
 * @author Petr Pisl
 */

public class JSFConfigNode extends DataNode {
    
    public static final String ICON_BASE =  "org/netbeans/modules/web/jsf/resources/JSFConfigIcon.gif";
    
    /** Creates a new instance of StrutsConfigNode */
    public JSFConfigNode (final JSFConfigDataObject dataObject) {
	super(dataObject,Children.LEAF);
        setIconBaseWithExtension(ICON_BASE);
    }
    
    // test to see if we can use DeleteAction
    public boolean canDestroy() {
	return true;
    }
}
