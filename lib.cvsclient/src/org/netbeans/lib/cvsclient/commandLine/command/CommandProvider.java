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

package org.netbeans.lib.cvsclient.commandLine.command;

import java.io.PrintStream;
import java.util.ResourceBundle;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.GlobalOptions;

/**
 * The provider of CVS commands.
 * The implementation of this interface knows how to create a CVS command
 * from an array of arguments.
 *
 * @author  Martin Entlicher
 */
public interface CommandProvider {

    /**
     * Get the name of this command.
     * The default implementation returns the name of the implementing class.
     */
    public String getName();
    
    /**
     * Get the list of synonyms of names of this command.
     */
    public abstract String[] getSynonyms();
    
    /**
     * Create the CVS command from an array of arguments.
     * @param args The array of arguments passed to the command.
     * @param index The index in the array where the command's arguments start.
     * @param workDir The working directory.
     * @return The implementation of the {@link org.netbeans.lib.cvsclient.command.Command}
     *         class, which have set the passed arguments.
     */
    public abstract Command createCommand(String[] args, int index, GlobalOptions gopt, String workDir);
    
    /**
     * Get a short string describibg the usage of the command.
     */
    public String getUsage();
    
    /**
     * Print a short help description (one-line only) for this command to the
     * provided print stream.
     * @param out The print stream.
     */
    public void printShortDescription(PrintStream out);
    
    /**
     * Print a long help description (multi-line with all supported switches
     * and their description) of this command to the provided print stream.
     * @param out The print stream.
     */
    public void printLongDescription(PrintStream out);
    
}
