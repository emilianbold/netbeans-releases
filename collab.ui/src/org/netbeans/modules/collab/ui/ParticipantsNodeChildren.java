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

import org.openide.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;

import java.beans.*;

import java.util.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.*;


/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class ParticipantsNodeChildren extends Children.Keys implements PropertyChangeListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Collection keys;
    private Conversation conversation;

    /**
     *
     *
     */
    public ParticipantsNodeChildren(Conversation conversation) {
        super();
        this.conversation = conversation;
    }

    /**
     *
     *
     */
    public Conversation getConversation() {
        return conversation;
    }

    /**
     *
     *
     */
    protected void addNotify() {
        getConversation().addPropertyChangeListener(this);
        refreshChildren();
    }

    /**
     *
     *
     */
    protected void removeNotify() {
        _setKeys(Collections.EMPTY_SET);
        getConversation().removePropertyChangeListener(this);
    }

    /**
     *
     *
     */
    protected Node[] createNodes(Object key) {
        Node[] result = null;

        try {
            if (key instanceof Node) {
                result = new Node[] { (Node) key };
            } else {
                result = new Node[] { new ParticipantNode(getConversation(), (CollabPrincipal) key) };
            }
        } catch (Exception e) {
            Debug.debugNotify(e);
        }

        return result;
    }

    /**
     *
     *
     */
    public Collection getKeys() {
        return keys;
    }

    /**
     *
     *
     */
    public void _setKeys(Collection value) {
        keys = value;
        super.setKeys(value);
    }

    /**
     *
     *
     */
    public void refreshChildren() {
        List keys = new ArrayList();

        try {
            //			Set datasourceNames=new TreeSet(Arrays.asList(
            //				getJatoWebContextCookie().getJDBCDatasourceNames()));
            //			for (Iterator i=datasourceNames.iterator(); i.hasNext(); )
            //			{
            //				nodes.add(getJatoWebContextCookie().getJDBCDatasource(
            //					(String)i.next()));
            //			}
            // TODO: Sort participants
            CollabPrincipal[] participants = getConversation().getParticipants();

            if ((participants == null) || (participants.length == 0)) {
                keys.add(
                    new MessageNode(
                        NbBundle.getMessage(
                            ParticipantsNodeChildren.class, "LBL_ParticipantsNodeChildren_NoParticipants"
                        )
                    )
                ); // NOI18N
            } else {
                keys.addAll(Arrays.asList(participants));
            }

            _setKeys(keys);
        } catch (Exception e) {
            Debug.errorManager.notify(e);
        }
    }

    /**
     *
     *
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof Conversation) {
            String propertyName = event.getPropertyName();

            if (Conversation.PROP_PARTICIPANTS.equals(propertyName)) {
                refreshChildren();
            }
        }
    }
}
