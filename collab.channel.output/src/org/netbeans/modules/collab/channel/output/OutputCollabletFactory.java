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
