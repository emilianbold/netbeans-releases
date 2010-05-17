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
package org.netbeans.lib.cvsclient.command;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.request.*;

/**
 * A class that provides common functionality for CVS commands that
 * operate upon the repository.
 *
 * @author Martin Entlicher
 */
public abstract class RepositoryCommand extends BuildableCommand {
    /**
     * The requests that are sent and processed.
     */
    protected List requests = new LinkedList();

    /**
     * The client services that are provided to this command.
     */
    protected ClientServices clientServices;

    /**
     * Whether to process recursively.
     */
    private boolean recursive = true;

    /**
     * The modules to process. These names are unexpanded and will be passed
     * to a module-expansion request.
     */
    protected final List modules = new LinkedList();
    
    /**
     * The expanded modules.
     */
    protected final List expandedModules = new LinkedList();

    /**
     * Gets the value of the recursive option.
     * @return true if recursive, false if not
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Sets the value of the recursive option.
     * @param r true if the command should recurse, false otherwise
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * Add a module to process.
     * @param module the name of the module to process
     */
    public void addModule(String module) {
        modules.add(module);
    }
    
    /**
     * Set the modules to process.
     * @param modules the names of the modules to process
     */
    public void setModules(String[] modules) {
        clearModules();
        if (modules == null) {
            return;
        }
        for (int i = 0; i < modules.length; i++) {
            String module = modules[i];
            this.modules.add(module);
        }
    }
    
    /**
     * Get the array of modules that are set to be processed.
     */
    public String[] getModules() {
        String[] mods = new String[modules.size()];
        mods = (String[])modules.toArray(mods);
        return mods;
    }
    
    /**
     * Clear the list of modules.
     */
    public void clearModules() {
        this.modules.clear();
    }
    
    /**
     * Add the argument requests. The argument requests are created using
     * the expanded set of modules passed in. Subclasses of this
     * class should call this method at the appropriate point in their
     * postExpansionExecute() method. Note that arguments are appended to the list.
     */
    protected final void addArgumentRequests() {
        if (expandedModules.size() == 0) {
            return;
        }

        for (Iterator it = expandedModules.iterator(); it.hasNext(); ) {
            final String module = (String) it.next();
            addRequest(new ArgumentRequest(module));
        }
    }

    /**
     * This is called when the server has responded to an expand-modules
     * request.
     */
    public final void moduleExpanded(ModuleExpansionEvent e) {
        expandedModules.add(e.getModule());
    }
    
    /**
     * Execute this command. This method sends the ExpandModulesRequest in order
     * to expand the modules that were set. The actual execution is performed by
     * {@link #postExpansionExecute} method.
     * @param client the client services object that provides any necessary
     * services to this command, including the ability to actually process
     * all the requests
     */
    public final void execute(ClientServices client, EventManager em)
    throws CommandException, AuthenticationException {
        
        client.ensureConnection();
        
        requests.clear();
        super.execute(client, em);
        
        clientServices = client;
        
        if (client.isFirstCommand()) {
            requests.add(new RootRequest(client.getRepository()));
        }
        for (Iterator it = modules.iterator(); it.hasNext();) {
            String module = (String)it.next();
            requests.add(new ArgumentRequest(module));
        }
        expandedModules.clear();
        requests.add(new DirectoryRequest(".", client.getRepository())); //NOI18N
        requests.add(new ExpandModulesRequest());
        try {
            client.processRequests(requests);
        }
        catch (CommandException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new CommandException(ex, ex.getLocalizedMessage());
        }
        requests.clear();
        postExpansionExecute(client, em);
    }
    
    /**
     * Execute this command
     * @param client the client services object that provides any necessary
     * services to this command, including the ability to actually process
     * all the requests
     */
    protected abstract void postExpansionExecute(ClientServices client, EventManager em)
        throws CommandException, AuthenticationException;
    
    /**
     * Adds the specified request to the request list.
     */
    protected final void addRequest(Request request) {
        requests.add(request);
    }

    /**
     * Adds the request for the current working directory.
     */
    protected final void addRequestForWorkingDirectory(ClientServices clientServices)
            throws IOException {
        addRequest(new DirectoryRequest(".", //NOI18N
                                        clientServices.getRepositoryForDirectory(getLocalDirectory())));
    }

    /**
     * If the specified value is true, add a ArgumentRequest for the specified
     * argument.
     */
    protected final void addArgumentRequest(boolean value, String argument) {
        if (!value) {
            return;
        }

        addRequest(new ArgumentRequest(argument));
    }

    /**
     * Appends the file's names to the specified buffer.
     */
    protected final void appendModuleArguments(StringBuffer buffer) {
        if (expandedModules.size() == 0) {
            return;
        }

        Iterator it = expandedModules.iterator();
        buffer.append((String) it.next());
        while (it.hasNext()) {
            buffer.append(' ');
            buffer.append((String) it.next());
        }
    }
}

