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
package org.netbeans.test.editor.app.core.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Vector;
import org.netbeans.test.editor.app.gui.actions.TreeNewType;
import org.netbeans.test.editor.app.gui.actions.TreeNodeAction;

/**
 *
 * @author  jlahoda
 * @version
 */
public class ActionRegistry extends Object {

    HashMap actions;
    HashMap newTypes;
    HashMap newClasses;
    private static ActionRegistry actionRegistry = null;
    private static Object synchronizeTo = new Object();
    
    /** Creates new ActionRegistry */
    private ActionRegistry() {
        actions = new HashMap();
        newTypes=new HashMap();
        newClasses=new HashMap();
    }
    
    public static ActionRegistry getDefault() {
        if (actionRegistry == null) {
            synchronized (synchronizeTo) {
                if (actionRegistry == null) {
                    actionRegistry = new ActionRegistry();
                }
            }
        }
        return actionRegistry;
    }
    
    public static void clear() {
        actionRegistry = null;
    }
    
    public void addAction(Class cookie, TreeNodeAction action) {
        Vector v;
        v=(Vector)(actions.get(cookie));
        if (v == null) {
            v=new Vector();
            v.add(action);
            actions.put(cookie, v);
        } else {
            v.add(action);
        }
    }
    
    public Vector getActions(Class cookie) {
        return (Vector)actions.get(cookie);
    }
    
    public Vector getActions(Collection cookieSet) {
        Vector result = new Vector();
        Iterator keys = actions.keySet().iterator();
        
        while (keys.hasNext()) {
            Class key = (Class) keys.next();
            Object o;
            for (Iterator it=cookieSet.iterator();it.hasNext();) {
                o=it.next();
                if (o.equals(key)) {
                    result.addAll(getActions(key));
                    break;
                }
            }
        }
        return result;
    }
    
    //new types
    public void registerNewType(Class newClass,TreeNewType type) {
        newClasses.put(newClass,type);
    }
    
    public TreeNewType getRegisteredNewType(Class newClass) {
        return (TreeNewType)(newClasses.get(newClass));
    }
    
    public void addRegisteredNewType(Class node,Class newClass) {
        addNewType(node,getRegisteredNewType(newClass));
    }
    
    public void addNewTypes(Class node,Vector types) {
        newTypes.put(node, types);
    }
    
    public void addNewType(Class node,TreeNewType type) {
        if (newTypes.get(node) != null) {
            ((Vector)(newTypes.get(node))).add(type);
        } else {
            Vector v=new Vector();
            addNewTypes(node,v);
            v.add(type);
        }
    }
    
    public Vector getNewTypes(Class node) {
        return (Vector)(newTypes.get(node));
    }
}
