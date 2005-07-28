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

import com.sun.collablet.Conversation;

import org.openide.*;
import org.openide.util.*;
import org.openide.windows.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import org.netbeans.modules.collab.*;


/**
 *
 * @author  todd
 */
public class ConversationOpenSupportEnv extends Object implements CloneableOpenSupport.Env, PropertyChangeListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private CloneableOpenSupport owner;
    private Conversation conversation;
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private VetoableChangeSupport vetoableSupport = new VetoableChangeSupport(this);

    /**
         *
         *
         */
    public ConversationOpenSupportEnv(Conversation conversation) {
        super();
        this.conversation = conversation;

        // TODO: Should this be a weak listener?
        conversation.addPropertyChangeListener(this);
    }

    /**
     *
     *
     */
    protected void registerCloneableOpenSupport(CloneableOpenSupport owner) {
        this.owner = owner;
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
    public CloneableOpenSupport findCloneableOpenSupport() {
        return owner;
    }

    /**
         *
         *
         */
    public boolean isValid() {
        return getConversation().isValid();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Modification status support
    ////////////////////////////////////////////////////////////////////////////

    /**
         *
         *
         */
    public boolean isModified() {
        return false;
    }

    /**
         *
         *
         */
    public void markModified() throws IOException {
        throw new IOException("Not modifiable");
    }

    /**
         *
         *
         */
    public void unmarkModified() {
        // Do nothing
    }

    ////////////////////////////////////////////////////////////////////////////
    // Property change support
    ////////////////////////////////////////////////////////////////////////////

    /**
         *
         *
         */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
         *
         *
         */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
         *
         *
         */
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        vetoableSupport.addVetoableChangeListener(listener);
    }

    /**
         *
         *
         */
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        vetoableSupport.removeVetoableChangeListener(listener);
    }

    /**
         *
         *
         */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof Conversation && Conversation.PROP_VALID.equals(event.getPropertyName())) {
            changeSupport.firePropertyChange(PROP_VALID, event.getOldValue(), event.getNewValue());
        }

        //		else
        //		if (event.getSource() instanceof Conversation &&
        //			(Conversation.PROP_MESSAGE.equals(event.getPropertyName()) ||
        //			 Conversation.PROP_EVENT.equals(event.getPropertyName())))
        //		{
        //			changeSupport.firePropertyChange(PROP_MODIFIED,false,true);
        //		}
    }
}
