/*
 * ActionsCache.java
 *
 * Created on November 14, 2002, 3:31 PM
 */

package org.netbeans.test.editor.app.gui.tree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import org.netbeans.test.editor.app.gui.actions.TreeNewType;
import org.netbeans.test.editor.app.gui.actions.TreeNodeAction;

/**
 *
 * @author  eh103527
 */
public class ActionsCache {
    
    HashMap actions;
    
    private static ActionsCache instance;
    
    /** Creates a new instance of ActionsCache */
    private ActionsCache() {
        actions=new HashMap();
    }
    
    public static ActionsCache getDefault() {
        if (instance == null) {
            instance=new ActionsCache();
        }
        return instance;
    }
    
    public void addNodeActions(Class node,Vector acts) {
        actions.put(node, acts);
    }
    
    public void addNodeAction(Class node,TreeNodeAction action) {
        if (actions.get(node) != null) {
            ((Vector)(actions.get(node))).add(action);
        } else {
            Vector v=new Vector();
            addNodeActions(node,v);
            v.add(action);
        }
    }
    
    public Vector getActions(Class node) {
        return (Vector)(actions.get(node));
    }
    
    public TreeNodeAction getAction(Class node, Class action) {
        Vector v=(Vector)(actions.get(node));
        Object o;
        for (Iterator it=v.iterator();it.hasNext();) {
            o=it.next();
            if (o.getClass().equals(action)) {
                return (TreeNodeAction)o;
            }
        }
        return null;
    }    
}
