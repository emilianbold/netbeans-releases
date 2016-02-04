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
package org.netbeans.modules.odcs.git;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import org.netbeans.modules.git.api.Git;
import org.netbeans.modules.odcs.versioning.spi.VCSProvider;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Ondrej Vrabec
 */
@ServiceProvider(service=VCSProvider.class)
public class GitApiProviderImpl implements VCSProvider {

    @Override
    public Type getType() {
        return Type.GIT;
    }
    
    @Override
    public boolean providesSources(String url) {
        return true;
    }
    
    @Override
    public File getSources (String repositoryUrl, PasswordAuthentication passwdAuth) {
        File cloneDest = null;
        try {
            if (passwdAuth != null) {
                cloneDest = Git.cloneRepository(repositoryUrl, passwdAuth.getUserName(), passwdAuth.getPassword()); 
            } else {
                cloneDest = Git.cloneRepository(repositoryUrl, null, null);
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
        return cloneDest;
    }

    @Override
    public String getDisplayName () {
        return "Git"; //NOI18N
    }

    @Override
    public boolean providesOpenHistory(File workdir) {
        return Git.isOwner(workdir);
    }
    
    @Override
    public boolean openHistory (final File workdir, final String commitId) {
        assert Git.isOwner(workdir);
        if(!Git.isOwner(workdir)) {
            return false;
        }
        Git.openSearchHistory(workdir, commitId);
        return true;
    }
    
    @Override
    public boolean openHistory (final File workdir, final String commitIdFrom, final String commitIdTo) {
        assert Git.isOwner(workdir);
        if(!Git.isOwner(workdir)) {
            return false;
        }
        Git.openSearchHistory(workdir, commitIdFrom, commitIdTo);
        return true;
    }

    @Override
    public boolean openHistoryBranch(File workdir, String branch) {
        assert Git.isOwner(workdir);
        if(!Git.isOwner(workdir)) {
            return false;
        }
        Git.openSearchHistoryBranch(workdir, branch);
        return true;
    }
    
    @Override
    public boolean providesLocalInit(String repositoryUrl) {
        return true;
    }
    
    @Override
    public boolean localInit(File localFolder, String repositoryUrl, PasswordAuthentication credentials) throws MalformedURLException {
        try {
            Git.initializeRepository(localFolder, repositoryUrl, credentials);
            return true;
        } catch (URISyntaxException ex) {
            throw new MalformedURLException(repositoryUrl);
        } 
    }
    
}
