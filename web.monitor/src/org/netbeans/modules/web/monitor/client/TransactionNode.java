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
import org.openide.util.Utilities;

import java.util.StringTokenizer; 
import java.text.DateFormat; 
import java.util.Date; 
import java.awt.Image;
import org.netbeans.modules.web.monitor.server.Constants;

public class TransactionNode extends AbstractNode {
    
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
    
    String id, method, uri, name = null, timestamp = null; 
    boolean current;
    private int statusCode;
    static boolean showTimeStamp = true; 

    /*
    public TransactionNode(String str, boolean current) { 
	super(Children.LEAF); 
 	StringTokenizer st = 
	    new StringTokenizer(str, Constants.Punctuation.itemSep);

	this.id = st.nextToken(); 
	this.method = st.nextToken(); 
	this.uri = st.nextToken(); 
	this.current = current; 

	setProperties();
    }

    public TransactionNode(String str, Children ch, boolean current) { 
	super(ch); 
	StringTokenizer st = 
	    new StringTokenizer(str, Constants.Punctuation.itemSep);

	this.id = st.nextToken(); 
	this.method = st.nextToken(); 
	this.uri = st.nextToken(); 
	this.current = current; 

	setProperties();
    }


    public TransactionNode(String str) { 
	this(str, true);
    }

    public TransactionNode(String id, String method, String uri) { 
	this(id, method, uri, true); 
    }

    */
    public TransactionNode(String id, String method, String uri, 
			   boolean current, int statusCode) {
	
	super(Children.LEAF);

	this.id = id;
	this.method = method;
	this.uri = uri;
	this.current = current;
        this.statusCode = statusCode;

	setProperties();
    }

    public TransactionNode(String id, String method, String uri, 
			   Children ch, boolean current, int statusCode) {
	
	super(ch);

	this.id = id;
	this.method = method;
	this.uri = uri;
	this.current = current;
        this.statusCode = statusCode;
        
	setProperties();
    }

    // This method is incomplete, URI may need to be truncated... 
    public String getLongName() {

	StringBuffer buf = new StringBuffer(method); 
	buf.append(" "); //NOI18N
	buf.append(uri);
	if(timestamp == null) setTimeStamp();
	buf.append(" "); //NOI18N
	buf.append(timestamp);

	return buf.toString();
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
    
    public String getID() { 
	return id;
    }

    public String getMethod() { 
	return method;
    }

    public String getURI() { 
	return uri;
    }

    public boolean isCurrent() { 
	return current;
    }

    public void setCurrent(boolean b) { 
	current = b;
    }

    /* Getter for set of actions that should be present in the
     * popup menu of this node. This set is used in construction of
     * menu returned from getContextMenu and specially when a menu for
     * more nodes is constructed.
     *
     * @return array of system actions that should be in popup menu
     */

    protected SystemAction[] createActions () {

	if(current) {
	    return new SystemAction[] {
		SystemAction.get(SaveAction.class),
		null,
		SystemAction.get(ReplayAction.class),
		SystemAction.get(EditReplayAction.class),
		null,
		SystemAction.get(DeleteAction.class)
	    };
	}
     
	return new SystemAction[] {
	    SystemAction.get(ReplayAction.class),
	    SystemAction.get(EditReplayAction.class),
	    null,
	    SystemAction.get(DeleteAction.class),
	};
    }


    public SystemAction[] getActions () {

	if(current) {
	    return new SystemAction[] {
		SystemAction.get(SaveAction.class),
		null,
		SystemAction.get(ReplayAction.class),
		SystemAction.get(EditReplayAction.class),
		null,
		SystemAction.get(DeleteAction.class)
	    };
	}
     
	return new SystemAction[] {
	    SystemAction.get(ReplayAction.class),
	    SystemAction.get(EditReplayAction.class),
	    null,
	    SystemAction.get(DeleteAction.class),
	};
    }

    /** Can this node be copied?
     * @return <code>true</code> in the default implementation
     */
    public boolean canCopy () {
	return true;
    }

    /** Can this node be cut?
     * @return <code>false</code> in the default implementation
     */
    public boolean canCut () {
	return false;
    }

    /** 
     * Set whether the timestamp is shown or not
     */
    public static void toggleTimeStamp() { 
	if(showTimeStamp) showTimeStamp = false; 
	else showTimeStamp = true; 
    }

    /** 
     * Is the timestamp showing
     */
    public static boolean showTimeStamp() { 
	return showTimeStamp; 
    }

    private void setProperties() {
	setNameString();
	setShortDescription(uri);
    }
    
    public void setNameString() {
	
	String name = null;
	if(uri.equals("/")) name = uri;  //NOI18N
	else {
	    StringTokenizer st = new StringTokenizer(uri,"/");  //NOI18N
	    while(st.hasMoreTokens()) name = st.nextToken();
	}
	
	StringBuffer buf = new StringBuffer(method); 
	buf.append(" "); //NOI18N
	buf.append(name);
	if(showTimeStamp) { 
	    if(timestamp == null) setTimeStamp();
	    buf.append(" "); //NOI18N
	    buf.append(timestamp);
	}
	setName(buf.toString()); 
    }

    private void setTimeStamp() {
	
	try { 
	    long ldate = Long.valueOf(id).longValue(); 
	    Date date = new Date(ldate); 

	    StringBuffer buf = new StringBuffer("["); //NOI18N
	    DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT); 
	    buf.append(df.format(date)); 
	    buf.append(" "); //NOI18N
	    df = DateFormat.getDateInstance(DateFormat.SHORT);
	    buf.append(df.format(date)); 
	    buf.append("]"); //NOI18N
	    timestamp = buf.toString();
	} 
	catch(Exception e) {} 
    } 
    
    public String toString() {
	StringBuffer buf = new StringBuffer("TransactionNode: ");  //NOI18N
	buf.append(this.getName());
	buf.append("\n");  //NOI18N
	buf.append("id=");  //NOI18N
	buf.append(id); 

	buf.append("\n");  //NOI18N
	buf.append("method=");  //NOI18N
	buf.append(method); 

	buf.append("\n");  //NOI18N
	buf.append("uri=");  //NOI18N
	buf.append(uri); 

	buf.append("\n");  //NOI18N
	buf.append("current=");  //NOI18N
	buf.append(String.valueOf(current)); 
	buf.append("\n");  //NOI18N

	return buf.toString();
    }
} // TransactionNode






