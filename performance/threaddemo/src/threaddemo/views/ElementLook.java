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

package threaddemo.views;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.spi.looks.Look;
import org.openide.actions.DeleteAction;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.w3c.dom.*;
import org.w3c.dom.events.*;

// XXX could have other operations - insert etc.

/**
 * Look for a DOM Document or one of its elements.
 * @author Jesse Glick
 */
public class ElementLook extends Look implements EventListener {
    
    public ElementLook() {
        super("ElementLook");
    }
    
    public String getDisplayName() {
        return "DOM Elements";
    }
    
    protected void attachTo(Object o) {
        assert o instanceof Element;
        EventTarget et = (EventTarget)o;
        // Node{Inserted,Removed} is fired *before* the change. This is better;
        // fired *after* it has taken effect.
        et.addEventListener("DOMSubtreeModified", this, false);
    }
    
    protected void detachFrom(Object o) {
        EventTarget et = (EventTarget)o;
        et.removeEventListener("DOMSubtreeModified", this, false);
    }
    
    public void handleEvent(Event evt) {
        try {
            Element parent = (Element)evt.getCurrentTarget();
            System.err.println("ElementLook: event on " + parent.getTagName() + ": " + evt + "; co=" + getChildObjects(parent, null));//XXX
            fireChange(parent, Look.GET_CHILD_OBJECTS);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
    
    public String getName(Object o, Lookup env) {
        return ((Element)o).getTagName();
    }
    
    public String getDisplayName(Object o, Lookup env) {
        return "<" + ((Element)o).getTagName() + ">";
    }
    
    public boolean isLeaf(Object o, Lookup env) {
        NodeList nl = ((Element)o).getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                return false;
            }
        }
        return true;
    }
    
    public List getChildObjects(Object o, Lookup env) {
        //System.err.println("ElementLook: gCO on " + ((Element)o).getTagName());
        NodeList nl = ((Element)o).getChildNodes();
        List l = new ArrayList(Math.max(nl.getLength(), 1));
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                l.add(n);
            }
        }
        return l;
    }
    
    public boolean canDestroy(Object o, Lookup env) {
        Element e = (Element)o;
        if (e.getParentNode() instanceof Document) {
            return false;
        } else {
            return true;
        }
    }
    
    public void destroy(Object o, Lookup env) {
        Element e = (Element)o;
        e.getParentNode().removeChild(e);
    }
    
    public Action[] getActions(Object o, Lookup env) {
        return new Action[] {
            SystemAction.get(DeleteAction.class),
        };
    }
    
}
