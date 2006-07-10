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

import java.beans.*;
import java.io.IOException;

import org.openide.windows.CloneableOpenSupport;

import com.sun.collablet.Conversation;

/**
 *
 * @author  todd
 */
public class PublicConversationOpenSupportEnv extends Object implements CloneableOpenSupport.Env,
    PropertyChangeListener {
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
    public PublicConversationOpenSupportEnv() {
        super();

        //this.conversation=conversation;
        // TODO: Should this be a weak listener?
        //conversation.addPropertyChangeListener(this);
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
        //return getConversation().isValid();
        return true;
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
