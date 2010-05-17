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
 *
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
 *
 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.command;

import java.io.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * All commands must extend this class. A command is essentially a
 * collection of requests that make up what is logically known as a CVS
 * command (from a user's perspective). Commands correspond to operations the
 * user can perform with CVS, for example checkout a module or perform a
 * diff on two file versions.<br>
 * Commands are automatically added as CVS event listeners. They can act
 * on particular events and perhaps fire new events.
 *
 * @author  Robert Greig
 */
public abstract class Command
        implements CVSListener, Cloneable {
    /**
     * The local directory from which the command is being run.
     * This gives us the ability to construct a full pathname for the file
     * which we are processing. The information from the responses alone is
     * not enough.
     */
    protected String localDirectory;
    
    /**
     * The global options.
     */
    private GlobalOptions globalOptions;
    
    private boolean failed = false;

    private String displayName;

    /**
     * Execute this command.
     * @param client the client services object that provides any necessary
     *               services to this command, including the ability to actually
     *               process all the requests
     * @param e      the event manager. The command can use this to fire events
     *               if necessary - for example, while parsing status responses.
     * @return Whether the execution was successfull
     */
    public void execute(ClientServices client, EventManager eventManager)
            throws CommandException, CommandAbortedException, AuthenticationException {
        setLocalDirectory(client.getLocalPath());
        this.globalOptions = client.getGlobalOptions();
    }

    /**
     * This method returns how the command would looklike when typed on the
     * command line.
     *
     * Each command is responsible for constructing this information.
     *
     * @returns <command's name> [<parameters>] files/dirs. Example: checkout -p CvsCommand.java
     */
    public abstract String getCVSCommand();

    /**
     * Returns the arguments of the command in the command-line style.
     * Similar to getCVSCommand() however without the files and command's name.
     */
    public abstract String getCVSArguments();

    /**
     * Takes the arguments and sets the command.
     * To be mainly used for automatic settings (like parsing the .cvsrc file).
     *
     * @return true if the option (switch) was recognized and set
     */
    public abstract boolean setCVSCommand(char opt, String optArg);

    /**
     * Resets all switches in the command to the default behaviour.
     * After calling this method, the command should behave defaultly.
     */
    public abstract void resetCVSCommand();

    /**
     * Returns a String that defines which options are available for this
     * particular command.
     */
    public abstract String getOptString();

    /**
     * This method just calls the Object.clone() and makes it public.
     */
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException ex) {
            return null;
        }
    }
    
    public boolean hasFailed() {
        return failed;
    }

    /**
     * Called when the server wants to send a message to be displayed to
     * the user. The message is only for information purposes and clients
     * can choose to ignore these messages if they wish.
     * @param e the event
     */
    public void messageSent(MessageEvent e) {
        if (e.isError() && (e.getSource() instanceof org.netbeans.lib.cvsclient.response.ErrorResponse)
                            || e.getSource() == this) {
            // We need to ignore ErrorMessageResponse
            failed = true;
        }
    }
    
    public void messageSent(BinaryMessageEvent e) {
        
    }

    /**
     * Called when a file has been added.
     * @param e the event
     */
    public void fileAdded(FileAddedEvent e) {
    }

    /**
     * Called when a file is going to be removed.
     * @param e the event
     */
    public void fileToRemove(FileToRemoveEvent e) {
    }

    /**
     * Called when a file is removed.
     * @param e the event
     */
    public void fileRemoved(FileRemovedEvent e) {
    }

    /**
     * Called when a file has been updated.
     * @param e the event
     */
    public void fileUpdated(FileUpdatedEvent e) {
    }

    /**
     * Called when file status information has been received.
     */
    public void fileInfoGenerated(FileInfoEvent e) {
    }

    /**
     * Called when server responses with "ok" or "error", (when the command finishes).
     */
    public void commandTerminated(TerminationEvent e) {
    }

    /**
     * This is called when the servers has responded to an expand-modules
     * request.
     */
    public void moduleExpanded(ModuleExpansionEvent e) {
    }

    /**
     * Returns the local path the command is associated with.
     */
    public final String getLocalDirectory() {
        return localDirectory;
    }

    /**
     * Returns the local path the command is associated with.
     * @deprecated Please use the getLocalDirectory() method instead.
     */
    public final String getLocalPath() {
        return localDirectory;
    }
    
    /**
     * Get the global options.
     */
    public final GlobalOptions getGlobalOptions() {
        return globalOptions;
    }
    
    /**
     * Returns the relative path of the specified file (relative to the set
     * local path).
     * Backward slashes will be replaced by forward slashes.
     */
    public final String getRelativeToLocalPathInUnixStyle(File file) {
        String filePath = file.getAbsolutePath();
        int startIndex = localDirectory.length() + 1;
        if (startIndex >= filePath.length()) {
            return "."; //NOI18N
        }
        String relativePath = filePath.substring(startIndex);
        return relativePath.replace('\\', '/');
    }

    /**
     * Sets the local directory for the command.
     */
    protected final void setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    /**
     * Returns the trimmed version of the specified String s.
     * The returned String is null if the specified String is null or contains
     * only white spaces.
     */
    protected static final String getTrimmedString(String s) {
        if (s == null) {
            return null;
        }

        s = s.trim();
        if (s.length() == 0) {
            return null;
        }

        return s;
    }

    /**
     * Defines prefered display name or <code>null</code>.
     * Localized string should highlight command purpose (use verb in gerund).
     * E.g. <code>UpdateCommand</code> used to refresh statuses should
     * be named "Refreshing Status" rather than "cvs -N update",
     * "Updating" or "Status Refresh".
     */
    public void setDisplayName(String name) {
        this.displayName = name;
    }

    /**
     * Returns localized name describing command purpose
     * or <code>null</code>.
     *
     * @see #getCVSCommand()
     */
    public String getDisplayName() {
        return displayName;
    }
}
