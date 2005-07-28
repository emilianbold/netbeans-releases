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
package org.netbeans.modules.collab.ui;

import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.Conversation;

import org.openide.util.*;

import java.net.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.netbeans.modules.collab.*;


/**
 * Common List Model object for rendering Users, Groups, Rooms, and Topics
 * @param
 */
public final class ListModel extends AbstractListModel {
    private JList list;
    private Vector v;
    private boolean show_server;
    private boolean show_uid;
    private boolean show_desc;
    private Icon icon;

    public ListModel(JList list, Vector v, boolean show_server) {
        this.v = v;
        this.list = list;
        this.show_server = show_server;
    }

    public ListModel(JList list, Vector v, boolean show_server, boolean show_uid, boolean show_desc) {
        this.v = v;
        this.list = list;
        this.show_server = show_server;
        this.show_uid = show_uid;
        this.show_desc = show_desc;
    }

    /**
     * Size of list
     */
    final public int getSize() {
        return v.size();
    }

    /**
     * Return object at given index
     * @param int index
     */
    final public Object getElementAt(int index) {
        return v.elementAt(index);
    }

    /**
     *
     */
    public Object getIndexId(int index) {
        return getElementAt(index);
    }

    /**
     * update the list model
     */
    final public void changed() {
        fireContentsChanged(list, 0, 0);
    }

    /**
     * update the list model
     * Some views that use this model recieve a new reference to a vector, so
     * the new vector should be passed in here
     * @param Vector v
     */
    final public void changed(Vector v) {
        this.v = v;
        fireContentsChanged(list, 0, 0);
    }

    /**
     * Return display string for given object
     * @param Object object
     */
    final public String getName(Object object) {
        Object o = v.elementAt(v.indexOf(object));

        StringBuffer ret = new StringBuffer();

        if (o instanceof CollabPrincipal) {
            CollabPrincipal nu = (CollabPrincipal) o;

            if (show_server) {
                ret.append(nu.getDisplayName());
                ret.append("@");

                //  ret.append(nu.getDomainName());
            } else {
                ret.append(nu.getDisplayName());
            }

            if (show_uid) {
                ret.append(" <");
                ret.append(nu.getIdentifier());
                ret.append(">");
            }

            //4527299 - append additional attributes
            if (show_desc) {
                // if(!(nu instanceof CollaborationGroup)){
                //    ret = appendDesc(ret, nu);
                // }
            }
        } else if (o instanceof Conversation) {
            Conversation c = (Conversation) o;
            ret.append(c.getIdentifier());
        } else if (o instanceof String) {
            ret.append((String) o);
        } else {
            String name = null;

            if (o != null) {
                name = o.toString();
            } else {
                return "";
            }
        }

        return ret.toString();
    }

    /**
     * Return correct icon to render based on object type
     * @param Object object
     */
    final public Icon getIcon(Object object) {
        if (object instanceof CollabPrincipal) {
            return new ImageIcon(Utilities.loadImage("/org/netbeans/modules/collab/ui/resources/user_png.gif")); // NOI18N
        } else if (object instanceof String) // hack 
         {
            return new ImageIcon(Utilities.loadImage("/org/netbeans/modules/collab/ui/resources/conversation_png.gif")); // NOI18N
        }

        return new ImageIcon(Utilities.loadImage("/org/netbeans/modules/collab/ui/resources/empty.gif")); // NOI18N
    }
}
