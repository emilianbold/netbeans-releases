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
package org.netbeans.modules.collab.channel.output;

import com.sun.collablet.Collablet;
import com.sun.collablet.CollabletFactory;
import com.sun.collablet.Conversation;

import org.openide.util.*;


public class OutputCollabletFactory extends Object implements CollabletFactory {
    /**
     *
     *
     */
    public OutputCollabletFactory() {
        super();
    }

    /**
     *
     *
     */
    public String getIdentifier() {
        return "output"; // NOI18N
    }

    /**
     *
     *
     */
    public String getDisplayName() {
        return NbBundle.getMessage(OutputCollabletFactory.class, "LBL_OutputCollabletFactory_DisplayName"); // NOI18N
    }

    /**
     *
     *
     */
    public Collablet createInstance(Conversation conversation) {
        return new OutputCollablet(conversation);
    }
}
