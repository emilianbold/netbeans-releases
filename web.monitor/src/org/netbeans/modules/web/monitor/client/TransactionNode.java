/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.monitor.client; 

import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;
import java.util.StringTokenizer; 
import java.text.DateFormat; 
import java.util.Date; 
import org.netbeans.modules.web.monitor.server.Constants;

public class TransactionNode extends AbstractNode {

    String id, method, uri, name = null, timestamp = null; 
    boolean current;
    static boolean showTimeStamp = true; 
   
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

    public TransactionNode(String str) { 
	this(str, true);
    }

    public TransactionNode(String id, String method, String uri) { 
	this(id, method, uri, true); 
    }

    public TransactionNode(String id, String method, String uri, 
			   boolean current) {
	
	super(Children.LEAF);

	this.id = id;
	this.method = method;
	this.uri = uri;
	this.current = current;

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
		SystemAction.get(DisplayAction.class),
		SystemAction.get(SaveAction.class),
		null,
		SystemAction.get(ReplayAction.class),
		SystemAction.get(EditReplayAction.class),
		null,
		SystemAction.get(DeleteAction.class)
	    };
	}
     
	return new SystemAction[] {
	    SystemAction.get(DisplayAction.class),
	    null,
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


    /**
     * Display is the default action
     */
    public SystemAction getDefaultAction() {
	return SystemAction.get(DisplayAction.class);
    }


    private void setProperties() {
	
	// Get icon
	if(method.equals(Constants.Http.GET))
	    setIconBase("/org/netbeans/modules/web/monitor/client/icons/get"); //NOI18N
	// Post icon
	else if(method.equals(Constants.Http.POST))
	    setIconBase("/org/netbeans/modules/web/monitor/client/icons/post"); //NOI18N
	// Other 
	else 
	    setIconBase("/org/netbeans/modules/web/monitor/client/icons/other"); //NOI18N
	
	setNameString();
    }
    
    public void setNameString() {
	
	String name = null;
	if(uri.equals("/")) name = uri;
	else {
	    StringTokenizer st = new StringTokenizer(uri,"/");
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
	    DateFormat df = 
		DateFormat.getDateTimeInstance(DateFormat.SHORT, 
					       DateFormat.SHORT); 
	    StringBuffer buf = new StringBuffer("["); //NOI18N
	    buf.append(df.format(date)); 
	    buf.append("]"); //NOI18N
	    timestamp = buf.toString();
	} 
	catch(Exception e) {} 
    } 
    
    public String toString() {
	StringBuffer buf = new StringBuffer("TransactionNode: ");
	buf.append(this.getName());
	buf.append("\n");
	buf.append("id=");
	buf.append(id); 

	buf.append("\n");
	buf.append("method=");
	buf.append(method); 

	buf.append("\n");
	buf.append("uri=");
	buf.append(uri); 

	buf.append("\n");
	buf.append("current=");
	buf.append(String.valueOf(current)); 
	buf.append("\n");

	return buf.toString();
    }
} // TransactionNode






