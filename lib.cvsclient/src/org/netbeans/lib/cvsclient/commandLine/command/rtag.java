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

import java.io.File;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.tag.*;
import org.netbeans.lib.cvsclient.commandLine.*;

/**
 * Tag repository files.
 *
 * @author  Martin Entlicher
 */
public class rtag extends AbstractCommandProvider {

    public String getName() {
        return "rtag"; // NOI18N
    }

    public String[] getSynonyms() {
        return new String[] { "rt" }; // NOI18N
    }

    public Command createCommand(String[] args, int index, GlobalOptions gopt, String workDir) {
        RtagCommand command = new RtagCommand();
        command.setBuilder(null);
        final String getOptString = command.getOptString();
        GetOpt go = new GetOpt(args, getOptString);
        int ch = -1;
        go.optIndexSet(index);
        boolean usagePrint = false;
        String arg;
        while ((ch = go.getopt()) != go.optEOF) {
            boolean ok = command.setCVSCommand((char)ch, go.optArgGet());
            if (!ok) {
                usagePrint = true;
            }
        }
        if (usagePrint) {
            throw new IllegalArgumentException(getUsage());
        }
        int argIndex = go.optIndexGet();
        // test if we have been passed the tag name
        if (argIndex < args.length) {
            command.setTag(args[argIndex]);
        } else {
            throw new IllegalArgumentException(getUsage());
        }
        int modulesArgsIndex = argIndex + 1;
        // test if we have been passed any module arguments
        if (modulesArgsIndex < args.length) {
            String[] modulesArgs = new String[args.length - modulesArgsIndex];
            // send the arguments as absolute paths
            for (int i = modulesArgsIndex; i < args.length; i++) {
                modulesArgs[i - modulesArgsIndex] = args[i];
            }
            command.setModules(modulesArgs);
        }
        return command;
        
    }
    
}
