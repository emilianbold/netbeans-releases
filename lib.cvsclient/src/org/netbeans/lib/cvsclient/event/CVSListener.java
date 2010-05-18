/*****************************************************************************
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
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):

 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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

 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.event;

/**
 * This interface must be implemented by clients wishing to receive events
 * describing the results of commands.
 * @author  Robert Greig
 */
public interface CVSListener {
    /**
     * Called when the server wants to send a message to be displayed to
     * the user. The message is only for information purposes and clients
     * can choose to ignore these messages if they wish.
     * @param e the event
     */
    void messageSent(MessageEvent e);

    /**
     * Called when the server wants to send a binary message to be displayed to
     * the user. The message is only for information purposes and clients
     * can choose to ignore these messages if they wish.
     * @param e the event
     */
    void messageSent(BinaryMessageEvent e);
    
    /**
     * Called when a file has been added.
     * @param e the event
     */
    void fileAdded(FileAddedEvent e);

    /**
     * Called when a file is going to be removed.
     * @param e the event
     */
    void fileToRemove(FileToRemoveEvent e);

    /**
     * Called when a file is removed.
     * @param e the event
     */
    void fileRemoved(FileRemovedEvent e);

    /**
     * Called when a file has been updated
     * @param e the event
     */
    void fileUpdated(FileUpdatedEvent e);

    /**
     * Called when file information has been received
     */
    void fileInfoGenerated(FileInfoEvent e);

    /**
     * called when server responses with "ok" or "error", (when the command
     * finishes)
     */
    void commandTerminated(TerminationEvent e);

    /**
     * Fire a module expansion event. This is called when the servers
     * has responded to an expand-modules request.
     */
    void moduleExpanded(ModuleExpansionEvent e);
}
