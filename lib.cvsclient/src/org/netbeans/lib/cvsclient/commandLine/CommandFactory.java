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
package org.netbeans.lib.cvsclient.commandLine;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.commandLine.command.CommandProvider;

/**
 * A factory for commands. Given a command name, and any arguments passed to
 * that command on the command line, it will return a configured Command
 * object, ready for execution.
 * @author  Robert Greig
 * @see org.netbeans.lib.cvsclient.command.Command
 */
public class CommandFactory {
    
    private static final String[] COMMAND_CLASSES = new String[] {
        "Import", "add", "annotate", "checkout", "commit", "diff", "export",
        "locbundlecheck", "log", "rannotate", "remove", "rlog", "rtag", "status",
        "tag", "update" };
    
    private static CommandFactory instance;
    
    private Map commandProvidersByNames;
    
    private CommandFactory() {
        createCommandProviders();
    }
    
    private void createCommandProviders() {
        commandProvidersByNames = new HashMap();
        String packageName = CommandFactory.class.getPackage().getName() + ".command.";
        for (int i = 0; i < COMMAND_CLASSES.length; i++) {
            Class providerClass;
            try {
                providerClass = Class.forName(packageName + COMMAND_CLASSES[i]);
                CommandProvider provider = (CommandProvider) providerClass.newInstance();
                commandProvidersByNames.put(provider.getName(), provider);
                String[] synonyms = provider.getSynonyms();
                for (int j = 0; j < synonyms.length; j++) {
                    commandProvidersByNames.put(synonyms[j], provider);
                }
            } catch (Exception e) {
                System.err.println("Creation of command '"+COMMAND_CLASSES[i]+"' failed:");
                e.printStackTrace(System.err);
                continue;
            }
        }
    }
    
    /**
     * Get the default instance of CommandFactory.
     */
    public static synchronized CommandFactory getDefault() {
        if (instance == null) {
            instance = new CommandFactory();
        }
        return instance;
    }
    
    /**
     * Create a CVS command.
     * @param commandName The name of the command to create
     * @param args The array of arguments
     * @param startingIndex The index of the first argument of the command in the array
     * @param workingDir The working directory
     */
    public Command createCommand(String commandName, String[] args,
                                 int startingIndex, GlobalOptions gopt,
                                 String workingDir) throws IllegalArgumentException {
        CommandProvider provider = (CommandProvider) commandProvidersByNames.get(commandName);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown command: '"+commandName+"'");
        }
        return provider.createCommand(args, startingIndex, gopt, workingDir);
    }
    
    /**
     * Get the provider of a command.
     * @param name The name of the command to get the provider for.
     */
    public CommandProvider getCommandProvider(String name) {
        return (CommandProvider) commandProvidersByNames.get(name);
    }
    
    /**
     * Get the array of all command providers.
     */
    public CommandProvider[] getCommandProviders() {
        Set providers = new HashSet(commandProvidersByNames.values());
        return (CommandProvider[]) providers.toArray(new CommandProvider[0]);
    }
    
    /*
    public static Command getCommand(String commandName, String[] args,
                                     int startingIndex, String workingDir)
            throws IllegalArgumentException {
        Class helper;
        try {
            helper = Class.forName("org.netbeans.lib.cvsclient.commandLine." +
                                   "command." + commandName);
        }
        catch (Exception e) {
            commandName = Character.toUpperCase(commandName.charAt(0)) + commandName.substring(1);
            try {
                helper = Class.forName("org.netbeans.lib.cvsclient.commandLine." +
                                       "command." + commandName);
            }
            catch (Exception ex) {
                System.err.println("Exception is: " + ex);
                throw new IllegalArgumentException("Unknown command " +
                                                   commandName);
            }
        }

        // the method invoked can throw an exception
        try {
            Method m = helper.getMethod("createCommand", new Class[]{
                String[].class,
                Integer.class,
                String.class});
            return (Command) m.invoke(null, new Object[] { args,
                    new Integer(startingIndex), workingDir });
        }
        catch (IllegalArgumentException e) {
            throw e;
        }
        catch (InvocationTargetException ite) {
            Throwable t = ite.getCause();
            if (t instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) t;
            } else {
                IllegalArgumentException iaex = new IllegalArgumentException(t.getMessage());
                iaex.initCause(t);
                throw iaex;
            }
        }
        catch (Exception e) {
            IllegalArgumentException iaex = new IllegalArgumentException(e.getMessage());
            iaex.initCause(e);
            throw iaex;
        }
    }
     */
}
