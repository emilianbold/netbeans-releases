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
package org.netbeans.modules.collab.channel.filesharing;

import org.openide.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.w3c.dom.*;

import java.io.*;

import java.net.*;

import java.util.*;

import javax.xml.parsers.*;

import org.netbeans.modules.collab.channel.filesharing.mdc.CollabEvent;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventProcessor;
import org.netbeans.modules.collab.channel.filesharing.mdc.MDChannelEventNotifier;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.CollabNotifierConfig;


/**
 * Filesharing EventNotifier
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class FilesharingEventNotifier extends MDChannelEventNotifier {
    /**
     * constructor
     *
     */
    public FilesharingEventNotifier(CollabNotifierConfig notifierConfig, EventProcessor eventProcessor) {
        super(notifierConfig, eventProcessor);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event Handler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * exec
     *
     * @param        ce  (collab event)
     */
    public void notify(CollabEvent ce) {
        super.notify(ce);
    }
}
