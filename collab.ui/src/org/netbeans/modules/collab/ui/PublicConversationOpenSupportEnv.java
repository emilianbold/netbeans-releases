/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
