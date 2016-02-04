/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.versioning.spi;

import java.io.File;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;

/**
 *
 * @author Ondrej Vrabec
 */
public interface VCSProvider {

    /**
     * The type of VCS handled by a particular implementation
     */
    enum Type {
        GIT,
        SVN
    }
    
    /**
     * Determines the VCS Type handled by the implementation.
     * 
     * @return 
     */
    public Type getType ();

    /**
     * Returns a name representing a VCS System.
     * 
     * @return 
     */
    public String getDisplayName ();
    
    /**
     * Determines whether the functionality to get sources from a remote url is 
     * provided - e.g. <code>git clone</code>
     * 
     * @param url
     * @return 
     * @throws java.net.MalformedURLException 
     */
    public boolean providesSources(String url) throws MalformedURLException;
    
    /**
     * Passes over to the particular VCS (and its UI eventually) 
     * to get the sources from a remote url. <br/>
     * Note that this operation is expected to block as long as it takes
     * 
     * @param repositoryUrl
     * @param passwdAuth
     * @return in case the operation succeeded then <code>File</code> representing the folder with the local checkout/clone,
     * otherwise <code>null</code>
     * @throws java.net.MalformedURLException
     */
    public File getSources (String repositoryUrl, PasswordAuthentication passwdAuth) throws MalformedURLException;

    /**
     * Determines whether the functionality to open a History view for files 
     * from the given local clone/checkout is available.
     * 
     * @param workdir
     * @return 
     */
    public boolean providesOpenHistory(File workdir);
    
    /**
     * Opens a history view for the given local directory 
     * (which will be a checkout/clone previously made from the Team Server) and commit id. <br/>
     * Note that this operation is expected to block for any necessary time.
     * 
     * @param workdir
     * @param commitId
     * @return <code>true</code> in case the operation succeeded, otherwise <code>false</code>
     */
    public boolean openHistory (File workdir, String commitId);
    
    /**
     * Opens a history view for the given local directory 
     * (which will be a checkout/clone previously made from the Team Server) and commit id. <br/>
     * Note that this operation is expected to block for any necessary time.
     * 
     * @param workdir
     * @param commitIdFrom
     * @param commitIdTo
     * @return <code>true</code> in case the operation succeeded, otherwise <code>false</code>
     */
    public boolean openHistory (File workdir, String commitIdFrom, String commitIdTo);
    
    /**
     * Opens a history view for the given local directory 
     * (which will be a checkout/clone previously made from the Team Server) and commit id. <br/>
     * Note that this operation is expected to block for any necessary time.
     * 
     * @param workdir
     * @param branch
     * @return <code>true</code> in case the operation succeeded, otherwise <code>false</code>
     */
    public boolean openHistoryBranch (File workdir, String branch);

    /**
     * Determines whether the functionality to make a local repository 
     * initialization is available. 
     * 
     * @param repositoryUrl
     * @return 
     * @throws java.net.MalformedURLException 
     */
    public boolean providesLocalInit(String repositoryUrl) throws MalformedURLException;
    
    /**
     * Initializes a local repository in the given local folder, where 
     * repositoryUrl is the remote address to which the newly created local repository or checkout relates. 
     * Called after a new server project was created. The provided <code>repositoryUrl</code> is given 
     * from the newly created remote project. The <code>localFolder</code> is going to contain the 
     * local files which are meant to be commited/pushed.
     * <p>
     * Note that the given local folder might already contain files. In such a case 
     * it is expected that after the operation the local files are ready for commit/push from the IDE.
     * <br/>
     * Also note that this operation is expected to block as long as it takes.
     * <p/>
     * e.g. - in case of git - do git init and set the repositoryUrl as a remote.
     * 
     * @param localFolder
     * @param repositoryUrl
     * @param credentials
     * @return 
     * @throws java.net.MalformedURLException 
     */
    public boolean localInit (File localFolder, String repositoryUrl, PasswordAuthentication credentials) throws MalformedURLException;
    
    
}
