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

import org.openide.nodes.AbstractNode;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import com.sun.collablet.Conversation;

/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class ParticipantsNode extends AbstractNode {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final String ICON_BASE = "org/netbeans/modules/collab/ui/resources/group_png"; // NOI18N
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {  };

    /**
     *
     *
     */
    public ParticipantsNode(Conversation conversation) {
        super(createChildren(conversation));

        setName(NbBundle.getBundle(ParticipantsNode.class).getString("LBL_ParticipantsNode_DisplayName")); // NOI18N
        setIconBase(ICON_BASE);
        systemActions = DEFAULT_ACTIONS;
    }

    /**
     *
     *
     */
    protected static ParticipantsNodeChildren createChildren(Conversation conversation) {
        return new ParticipantsNodeChildren(conversation);
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ParticipantsNode.class);
    }

    /**
     *
     *
     */
    public boolean canCut() {
        return false;
    }

    /**
     *
     *
     */
    public boolean canCopy() {
        return false;
    }

    /**
     *
     *
     */
    public boolean canDestroy() {
        return false;
    }

    /**
     *
     *
     */
    public boolean canRename() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
}
