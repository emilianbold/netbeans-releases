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

import org.openide.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import java.beans.*;

import java.util.*;


/**
 *
 * @author  todd
 */
public class MessageNode extends AbstractNode {
    /**
     *
     *
     */
    public MessageNode(String message) {
        super(Children.LEAF);
        setDisplayName(message);
        setIconBase("org/openide/resources/actions/empty"); // NOI18N
    }

    /**
     *
     *
     */
    public SystemAction[] getActions() {
        return new SystemAction[0];
    }
}
