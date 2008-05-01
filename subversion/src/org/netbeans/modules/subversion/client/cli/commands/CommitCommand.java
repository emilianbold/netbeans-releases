/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.cli.commands;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.cli.Parser.Line;
import org.netbeans.modules.subversion.client.cli.SvnCommand;

/**
 *
 * @author Tomas Stupka
 */
public class CommitCommand extends SvnCommand {

    private final boolean keepLocks;
    private final boolean recursive;
    private final String message;
    private File[] files;
    private long revision;

    public CommitCommand(File[] files, boolean keepLocks, boolean recursive, String message) {
        this.keepLocks = keepLocks;
        this.recursive = recursive;
        this.message = message;
        this.files = files;
    }
       
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("commit");
        if (keepLocks) {
            arguments.add("--no-unlock");                                
        }
        if(!recursive) {
            arguments.add("-N");
        }
        arguments.addMessage(message);	        
        arguments.addFileArguments(files);
        setCommandWorkingDirectory(files);
    }

    public long getRevision() {
        return revision;
    }

    @Override
    protected void notify(Line line) {
        if(line.getRevision() != -1) {
            if(revision != -1) {
                try {
                    Subversion.LOG.warning("Revision notified more times : " + revision + ", " + line.getRevision() + " for command " + getStringCommand());
                } catch (IOException ex) {
                    // should not happen
                }
            }
            revision = line.getRevision();            
        }
    }        
    
    @Override
    public void errorText(String line) {
        super.errorText(line);
//        XXX if ("".equals(line))
//        return SVNRevision.SVN_INVALID_REVNUM;
//        if (line.startsWith("svn: Attempted to lock an already-locked dir")) {
//                for (int i = 0; i < 50; i++) {
//                        try {
//                                notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(parents));
//                                _cmd.checkin(paths, comment, recurse, keepLocks);
//                                return _cmd.getRevision();
//                        } catch (CmdLineException e1) {
//                                try {
//                                        Thread.sleep(100);
//                                } catch (InterruptedException e2) {
//                                        //do nothing if interrupted
//                                }
//                        }
//                }
//        }
//			throw SVNClientException.wrapException(e);
    }
    
}
