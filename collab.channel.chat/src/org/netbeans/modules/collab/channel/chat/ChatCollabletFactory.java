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
package org.netbeans.modules.collab.channel.chat;

import com.sun.collablet.*;

import org.openide.options.*;
import org.openide.util.*;

import java.util.*;

import javax.swing.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class ChatCollabletFactory extends Object implements CollabletFactory {
    /**
     *
     *
     */
    public ChatCollabletFactory() {
        super();
    }

    /**
     *
     *
     */
    public String getIdentifier() {
        return "chat"; // NOI18N
    }

    /**
     *
     *
     */
    public String getDisplayName() {
        return NbBundle.getMessage(ChatCollabletFactory.class, "LBL_ChatCollabletFactory_DisplayName");
    }

    /**
     *
     *
     */
    public Collablet createInstance(Conversation conversation) {
        return new ChatChannelImpl(conversation);
    }
}
