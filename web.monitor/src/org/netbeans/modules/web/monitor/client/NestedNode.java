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

package org.netbeans.modules.web.monitor.client; 

import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;
import java.util.StringTokenizer; 
import java.text.DateFormat; 
import java.util.Date; 
import org.netbeans.modules.web.monitor.server.Constants;

public class NestedNode extends AbstractNode {

    private String resource = null;
    private String method = null;
    private int[] index;
    private final static boolean debug = false;
     
    public NestedNode(String resource, String method, int[] index) {
		
	super(Children.LEAF);
	this.resource = resource;
	this.method = method;
	this.index = index;
	setProperties();
	if(debug) System.out.println(this.toString());
    }


    public NestedNode(String resource, 
		      String method, 
		      Children ch, 
		      int[] index) { 
	super(ch);
	this.resource = resource;
	this.method = method;
	this.index = index;
	setProperties();
	if(debug) System.out.println(this.toString());
    }

    public String getLongName() {
	return getName();
    }
    
    public String getResource() { 
	return resource;
    }

    public int[] getIndex() { 
	return index;
    }

    /* Getter for set of actions that should be present in the
     * popup menu of this node. This set is used in construction of
     * menu returned from getContextMenu and specially when a menu for
     * more nodes is constructed.
     *
     * @return array of system actions that should be in popup menu
     */

    protected SystemAction[] createActions () {

	return new SystemAction[] {
	};
    }

    /** Can this node be copied?
     * @return <code>true</code> in the default implementation
     */
    public boolean canCopy () {
	return false;
    }

    /** Can this node be cut?
     * @return <code>false</code> in the default implementation
     */
    public boolean canCut () {
	return false;
    }

    private void setProperties() {

	// Get icon
	if(method.equals(Constants.Http.GET))
	    setIconBase("org/netbeans/modules/web/monitor/client/icons/get"); //NOI18N
	// Post icon
	else if(method.equals(Constants.Http.POST))
	    setIconBase("org/netbeans/modules/web/monitor/client/icons/post"); //NOI18N
	// Other 
	else 
	setIconBase("org/netbeans/modules/web/monitor/client/icons/other"); //NOI18N
	
	setNameString();
    }
    
    public void setNameString() {
	
	String name = null;
	if(resource.equals("/")) name = resource;  //NOI18N
	else {
	    StringTokenizer st = new StringTokenizer(resource,"/");  //NOI18N
	    while(st.hasMoreTokens()) name = st.nextToken();
	}
	setName(name); 
    }

    public String toString() {
	StringBuffer buf = new StringBuffer("NestedNode: ");  //NOI18N
	buf.append(this.getName());
	buf.append(", resource=");  //NOI18N
	buf.append(resource); 
	buf.append(", index="); //NOI18N
	for(int i=0; i<index.length; ++i) {
	    buf.append(index[i]);
	    buf.append(","); //NOI18N
	}
	return buf.toString();
    }
} // NestedNode






