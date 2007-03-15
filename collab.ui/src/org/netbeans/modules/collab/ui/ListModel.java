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
package org.netbeans.modules.collab.ui;

import java.util.Vector;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import org.openide.util.*;

import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.Conversation;



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
        list.addFocusListener(new ListFocusListener());
    }

    public ListModel(JList list, Vector v, boolean show_server, boolean show_uid, boolean show_desc) {
        this.v = v;
        this.list = list;
        this.show_server = show_server;
        this.show_uid = show_uid;
        this.show_desc = show_desc;
        list.addFocusListener(new ListFocusListener());
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
            return new ImageIcon(Utilities.loadImage("org/netbeans/modules/collab/ui/resources/user_png.gif")); // NOI18N
        } else if (object instanceof String) // hack 
         {
            return new ImageIcon(Utilities.loadImage("org/netbeans/modules/collab/ui/resources/conversation_png.gif")); // NOI18N
        }

        return new ImageIcon(Utilities.loadImage("org/netbeans/modules/collab/ui/resources/empty.gif")); // NOI18N
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////
 
    /**
     *
     *
     */
    class ListFocusListener implements FocusListener {
 
        Color backgroundColor = list.getBackground();
        Color foregroundColor = list.getForeground();
 
        Color selectionBackgroundColor = list.getSelectionBackground();
        Color selectionForegroundColor = list.getSelectionForeground();
 
        public void focusGained(FocusEvent evt) {
         
            list.setSelectionBackground(selectionBackgroundColor);
            list.setSelectionForeground(selectionForegroundColor);
            
            int size  = list.getModel().getSize();
            int index = list.getSelectedIndex();
 
            if( 0 < size ){
                if( index < 0 ){
                    list.setSelectedIndex(size - 1);
                }
            }
            
        }
         
        public void focusLost(FocusEvent e) {
            list.setSelectionBackground(backgroundColor);
            list.setSelectionForeground(foregroundColor);
        }        
    }
}
