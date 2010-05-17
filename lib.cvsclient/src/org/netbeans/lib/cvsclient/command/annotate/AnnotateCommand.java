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

package org.netbeans.lib.cvsclient.command.annotate;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.request.*;

/**
 * The annotate command shows all lines of the file and annotates each line with cvs-related info.
 * @author  Milos Kleint
 */
public class AnnotateCommand extends BasicCommand {
    /**
     * The event manager to use
     */
    protected EventManager eventManager;

    /**
     * Use head revision if a revision meeting criteria set by switches -r/-D
     * (tag/date) is not found.
     */
    private boolean useHeadIfNotFound;

    /**
     * equals the -D switch of command line cvs.
     */
    private String annotateByDate;

    /**
     * Equals the -r switch of command-line cvs.
     */
    private String annotateByRevision;

    /**
     * Construct a new diff command
     */
    public AnnotateCommand() {
    }

    /**
     * Create a builder for this command.
     * @param eventMan the event manager used to receive events.
     */
    public Builder createBuilder(EventManager eventMan) {
        return new AnnotateBuilder(eventMan, this);
    }

    /**
     * Execute a command
     * @param client the client services object that provides any necessary
     * services to this command, including the ability to actually process
     * all the requests.
     */
    public void execute(ClientServices client, EventManager em)
            throws CommandException, AuthenticationException {
        eventManager = em;

        client.ensureConnection();

        super.execute(client, em);

        excludeBinaryFiles(requests);

        try {
            if (useHeadIfNotFound) {
                requests.add(1, new ArgumentRequest("-f")); //NOI18N
            }
            if (annotateByDate != null && annotateByDate.length() > 0) {
                requests.add(1, new ArgumentRequest("-D")); //NOI18N
                requests.add(2, new ArgumentRequest(getAnnotateByDate()));
            }
            if (annotateByRevision != null && annotateByRevision.length() > 0) {
                requests.add(1, new ArgumentRequest("-r")); //NOI18N
                requests.add(2, new ArgumentRequest(getAnnotateByRevision()));
            }
            addRequestForWorkingDirectory(client);
            addArgumentRequests();
            addRequest(CommandRequest.ANNOTATE);
            client.processRequests(requests);
        }
        catch (CommandException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new CommandException(ex, ex.getLocalizedMessage());
        }
        finally {
            requests.clear();
        }
    }

    private void excludeBinaryFiles(java.util.List requests) {
        Iterator it = requests.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof EntryRequest) {
                EntryRequest req = (EntryRequest)obj;
                if (req.getEntry().isBinary()) {
                    it.remove();
                    if (it.hasNext()) {
                        // removes also the follwoing modified/unchanged request
                        it.next();
                        it.remove();
                    }
                }
            }
        }
    }

    /** called when server responses with "ok" or "error", (when the command finishes)
     */
    public void commandTerminated(TerminationEvent e) {
        if (builder != null) {
            builder.outputDone();
        }
    }

    /**
     * Getter for property useHeadIfNotFound.
     * @return Value of property useHeadIfNotFound.
     */
    public boolean isUseHeadIfNotFound() {
        return useHeadIfNotFound;
    }

    /**
     * Setter for property useHeadIfNotFound.
     * @param useHeadIfNotFound New value of property useHeadIfNotFound.
     */
    public void setUseHeadIfNotFound(boolean useHeadIfNotFound) {
        this.useHeadIfNotFound = useHeadIfNotFound;
    }

    /**
     * Getter for property annotateByDate.
     * @return Value of property annotateByDate.
     */
    public String getAnnotateByDate() {
        return annotateByDate;
    }

    /**
     * Setter for property annotateByDate.
     * @param annotateByDate New value of property annotateByDate.
     */
    public void setAnnotateByDate(String annotateByDate) {
        this.annotateByDate = annotateByDate;
    }

    /**
     * Getter for property annotateByRevision.
     * @return Value of property annotateByRevision.
     */
    public String getAnnotateByRevision() {
        return annotateByRevision;
    }

    /**
     * Setter for property annotateByRevision.
     * @param annotateByRevision New value of property annotateByRevision.
     */
    public void setAnnotateByRevision(String annotateByRevision) {
        this.annotateByRevision = annotateByRevision;
    }

    /**
     * This method returns how the command would looklike when typed on the command line.
     * Each command is responsible for constructing this information.
     * @returns <command's name> [<parameters>] files/dirs. Example: checkout -p CvsCommand.java
     */
    public String getCVSCommand() {
        StringBuffer toReturn = new StringBuffer("annotate "); //NOI18N
        toReturn.append(getCVSArguments());
        File[] files = getFiles();
        if (files != null) {
            for (int index = 0; index < files.length; index++) {
                toReturn.append(files[index].getName() + " "); //NOI18N
            }
        }
        return toReturn.toString();
    }

    /** takes the arguments and sets the command. To be mainly
     * used for automatic settings (like parsing the .cvsrc file)
     * @return true if the option (switch) was recognized and set
     */
    public boolean setCVSCommand(char opt, String optArg) {
        if (opt == 'R') {
            setRecursive(true);
        }
        else if (opt == 'l') {
            setRecursive(false);
        }
        else if (opt == 'r') {
            setAnnotateByRevision(optArg);
        }
        else if (opt == 'D') {
            setAnnotateByDate(optArg);
        }
        else if (opt == 'f') {
            setUseHeadIfNotFound(true);
        }
        else {
            return false;
        }
        return true;
    }

    /**
     * String returned by this method defines which options are available for this particular command
     */
    public String getOptString() {
        return "Rlr:D:f"; //NOI18N
    }

    /**
     * resets all switches in the command. After calling this method,
     * the command should have no switches defined and should behave defaultly.
     */
    public void resetCVSCommand() {
        setRecursive(true);
        setAnnotateByDate(null);
        setAnnotateByRevision(null);
        setUseHeadIfNotFound(false);
    }

    /**
     * Returns the arguments of the command in the command-line style.
     * Similar to getCVSCommand() however without the files and command's name
     */
    public String getCVSArguments() {
        StringBuffer toReturn = new StringBuffer(""); //NOI18N
        if (!isRecursive()) {
            toReturn.append("-l "); //NOI18N
        }
        if (getAnnotateByRevision() != null) {
            toReturn.append("-r "); //NOI18N
            toReturn.append(getAnnotateByRevision());
            toReturn.append(" "); //NOI18N
        }
        if (getAnnotateByDate() != null) {
            toReturn.append("-D "); //NOI18N
            toReturn.append(getAnnotateByDate());
            toReturn.append(" "); //NOI18N
        }
        if (isUseHeadIfNotFound()) {
            toReturn.append("-f "); //NOI18N
        }
        return toReturn.toString();
    }

}
