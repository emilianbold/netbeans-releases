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
import org.openide.util.Utilities;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;
import java.awt.Image;
import java.util.StringTokenizer; 
import java.text.DateFormat; 
import java.util.Date;
import org.netbeans.modules.web.monitor.data.Constants;

public class NestedNode extends AbstractNode {
    
    private static final Image IMAGE_GET =
        Utilities.loadImage("org/netbeans/modules/web/monitor/client/icons/get.gif"); // NOI18N
    private static final Image IMAGE_POST =
        Utilities.loadImage("org/netbeans/modules/web/monitor/client/icons/post.gif"); // NOI18N
    private static final Image IMAGE_OTHER =
        Utilities.loadImage("org/netbeans/modules/web/monitor/client/icons/other.gif"); // NOI18N

    private static final Image INFO_BADGE =
        Utilities.loadImage("org/netbeans/modules/web/monitor/client/icons/infoBadge.gif"); // NOI18N
    private static final Image WARNING_BADGE =
        Utilities.loadImage("org/netbeans/modules/web/monitor/client/icons/warningBadge.gif"); // NOI18N
    private static final Image ERROR_BADGE =
        Utilities.loadImage("org/netbeans/modules/web/monitor/client/icons/errorBadge.gif"); // NOI18N

    private static final Image IMAGE_GET_INFO =
        Utilities.mergeImages(IMAGE_GET, INFO_BADGE, 0, 0);
    private static final Image IMAGE_POST_INFO =
        Utilities.mergeImages(IMAGE_POST, INFO_BADGE, 0, 0);
    private static final Image IMAGE_OTHER_INFO=
        Utilities.mergeImages(IMAGE_OTHER, INFO_BADGE, 0, 0);

    private static final Image IMAGE_GET_WARNING =
        Utilities.mergeImages(IMAGE_GET, WARNING_BADGE, 0, 0);
    private static final Image IMAGE_POST_WARNING =
        Utilities.mergeImages(IMAGE_POST, WARNING_BADGE, 0, 0);
    private static final Image IMAGE_OTHER_WARNING=
        Utilities.mergeImages(IMAGE_OTHER, WARNING_BADGE, 0, 0);
    
    private static final Image IMAGE_GET_ERROR =
        Utilities.mergeImages(IMAGE_GET, ERROR_BADGE, 0, 0);
    private static final Image IMAGE_POST_ERROR =
        Utilities.mergeImages(IMAGE_POST, ERROR_BADGE, 0, 0);
    private static final Image IMAGE_OTHER_ERROR =
        Utilities.mergeImages(IMAGE_OTHER, ERROR_BADGE, 0, 0);

    private String resource = null;
    private String method = null;
    private int statusCode;
    private int[] index;
    private final static boolean debug = false;
     
    public NestedNode(String resource, String method, int[] index, int statusCode) {
		
	super(Children.LEAF);
	this.resource = resource;
	this.method = method;
	this.index = index;
        this.statusCode = statusCode;
	setProperties();
	if(debug) System.out.println(this.toString());
    }


    public NestedNode(String resource, 
		      String method, 
		      Children ch, 
		      int[] index,
                      int statusCode) { 
	super(ch);
	this.resource = resource;
	this.method = method;
	this.index = index;
        this.statusCode = statusCode;
	setProperties();
	if(debug) System.out.println(this.toString());
    }

    public String getLongName() {
	return getName();
    }
    
    public Image getIcon(int type) {
	// Get icon
	if(method.equals(Constants.Http.GET)) {
            if (statusCode >= 400 || statusCode < 0) {
                return IMAGE_GET_ERROR;
            } else if (statusCode >= 300) {
                return IMAGE_GET_WARNING;
            } else if (statusCode >= 200) {
                return IMAGE_GET;
            } else {
                return IMAGE_GET_INFO;
            }
	// Post icon
        } else if(method.equals(Constants.Http.POST)) {
	    if (statusCode >= 400 || statusCode < 0) {
                return IMAGE_POST_ERROR;
            } else if (statusCode >= 300) {
                return IMAGE_POST_WARNING;
            } else if (statusCode >= 200) {
                return IMAGE_POST;
            } else {
                return IMAGE_POST_INFO;
            }
	// Other 
        } else {
	    if (statusCode >= 400 || statusCode < 0) {
                return IMAGE_OTHER_ERROR;
            } else if (statusCode >= 300) {
                return IMAGE_OTHER_WARNING;
            } else if (statusCode >= 200) {
                return IMAGE_OTHER;
            } else {
                return IMAGE_OTHER_INFO;
            }
        }
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(type);
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






