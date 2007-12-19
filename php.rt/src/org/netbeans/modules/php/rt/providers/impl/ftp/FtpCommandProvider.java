/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.rt.providers.impl.ftp;

import org.netbeans.api.project.Project;
import org.netbeans.modules.php.rt.providers.impl.AbstractCommandProvider;
import org.netbeans.modules.php.rt.providers.impl.actions.DebugCommandImpl;
import org.netbeans.modules.php.rt.providers.impl.actions.RunCommand;
import org.netbeans.modules.php.rt.providers.impl.actions.RunSingleCommand;
import org.netbeans.modules.php.rt.providers.impl.ftp.actions.DownloadFilesCommandImpl;
import org.netbeans.modules.php.rt.providers.impl.ftp.actions.UploadFilesCommandImpl;
import org.netbeans.modules.php.rt.spi.providers.Command;

/**
 *
 * @author avk
 */
public class FtpCommandProvider extends AbstractCommandProvider {

    FtpCommandProvider( FtpServerProvider provider ){
        myProvider = provider;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.CommandProvider#getCommands()
     */
    public Command[] getCommands(Project project) {
        if (isInvokedForProject() || isInvokedForSrcRoot()){
            return getProjectCommands(project);
        } else {
            return getObjectCommands(project);
        }
    }
    
    private Command[] getProjectCommands( Project project ) {
        return new Command[]{
                new RunCommand( project , myProvider ),
                new UploadFilesCommandImpl( project, myProvider ),
                new DownloadFilesCommandImpl( project, myProvider),
                new DebugCommandImpl( project , myProvider )
        };
    }

    private Command[] getObjectCommands(Project project) {
        return new Command[]{
                new RunSingleCommand( project , myProvider ),
                new UploadFilesCommandImpl(project, myProvider),
                new DownloadFilesCommandImpl( project, myProvider),
                new DebugCommandImpl( project , myProvider )
        };
    }

    private FtpServerProvider myProvider;
    
}
