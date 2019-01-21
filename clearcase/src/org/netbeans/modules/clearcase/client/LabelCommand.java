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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.clearcase.client;

import org.netbeans.modules.clearcase.ClearcaseException;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import org.netbeans.modules.clearcase.Clearcase;

/**
 * The Label command.
 * 
 * 
 */
public class LabelCommand extends FilesCommand {
    
    private final boolean recurse;
    private final boolean replace;
    private final boolean follow;
    private final String  version;
    private final String  comment;
    private final String  label;
    
    private File          addMessageFile;    
    
    /**
     * Creates a MkLabel command.
     * 
     * @param files
     * @param replace
     * @param recurse
     * @param follow
     * @param listeners
     */
    public LabelCommand(File [] files, String label, String comment, boolean replace, boolean recurse, boolean follow, String version, NotificationListener... listeners) {
        super(files, listeners);
        this.label = label;
        this.replace = replace;
        this.follow = follow;
        this.recurse = recurse;
        this.version = version;
        this.comment = comment;
    }

    public void prepareCommand(Arguments arguments) throws ClearcaseException {
        arguments.add("mklabel");
        if (recurse) {
            arguments.add("-recurse");
        }
        if (replace) {
            arguments.add("-replace");
        }
        if (follow) {
            arguments.add("-follow");
        }
        if (comment == null) {
            arguments.add("-ncomment");
        } else {
            try {
                addMessageFile = File.createTempFile("clearcase-", ".txt");
                addMessageFile.deleteOnExit();
                FileWriter fw = new FileWriter(addMessageFile);
                fw.write(comment);
                fw.close();
                arguments.add("-cfile");
                arguments.add(addMessageFile);
            } catch (IOException e) {
                arguments.add("-comment");
                arguments.add(comment);
            }
        }
        arguments.add(label);
        addPNames(arguments);
    }

    public void commandFinished() {
        super.commandFinished();
        if (addMessageFile != null) addMessageFile.delete();
    }    
    
    protected final void addPNames(Arguments arguments) {
        String [] paths = computeRelativePaths();
        for (String path : paths) {
            arguments.add(path + ((version != null && !version.trim().equals("")) ? Clearcase.getInstance().getExtendedNamingSymbol() + version : "") );
        }
    }
    
}
