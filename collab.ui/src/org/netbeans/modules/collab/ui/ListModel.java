/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            return new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/collab/ui/resources/user_png.gif")); // NOI18N
        } else if (object instanceof String) // hack 
         {
            return new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/collab/ui/resources/conversation_png.gif")); // NOI18N
        }

        return new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/collab/ui/resources/empty.gif")); // NOI18N
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
