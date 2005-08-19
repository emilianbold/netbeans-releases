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
