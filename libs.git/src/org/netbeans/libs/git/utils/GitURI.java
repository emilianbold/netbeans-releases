/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.libs.git.utils;

import java.net.URISyntaxException;
import org.eclipse.jgit.transport.URIish;

/**
 *
 * @author Tomas Stupka
 */
public class GitURI {
    private URIish uri;
    
    private GitURI() {}
    
    // a private constructor with URIish uri causes:
    // cannot access org.eclipse.jgit.transport.URIish 
    // class file for org.eclipse.jgit.transport.URIish not found
    private GitURI create(URIish uri) {
        GitURI u = new GitURI();
        u.uri = uri;
        return u;
    }
    
    public GitURI(String uri) throws URISyntaxException {
        this.uri = new URIish(uri);
    }

    public String toPrivateString() {
        return uri.toPrivateString();
    }

    public GitURI setUser(String n) {
        return create(uri.setUser(n));
    }

    public GitURI setScheme(String n) {
        return create(uri.setScheme(n));
    }

    public GitURI setPort(int n) {
        return create(uri.setPort(n));
    }

    public GitURI setPath(String n) {
        return create(uri.setPath(n));
    }

    public GitURI setPass(String n) {
        return create(uri.setPass(n));
    }

    public GitURI setHost(String n) {
        return create(uri.setHost(n));
    }

    public boolean isRemote() {
        return uri.isRemote();
    }

    public String getUser() {
        return uri.getUser();
    }

    public String getScheme() {
        return uri.getScheme();
    }

    public int getPort() {
        return uri.getPort();
    }

    public String getPath() {
        return uri.getPath();
    }

    public String getPass() {
        return uri.getPass();
    }

    public String getHost() {
        return uri.getHost();
    }
    
}
