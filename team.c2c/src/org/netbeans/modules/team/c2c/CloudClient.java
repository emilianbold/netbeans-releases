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
package org.netbeans.modules.team.c2c;

import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.service.ProfileWebServiceClient;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 *
 * @author ondra
 */
public final class CloudClient {
    private final ProfileWebServiceClient delegate;
    private static final String PROFILE_SERVICE = "alm/api"; //NOI18N
    private final AbstractWebLocation location;

    CloudClient (ProfileWebServiceClient client, AbstractWebLocation location) {
        this.delegate = client;
        this.location = location;
    }

    public Profile getCurrentProfile () throws CloudException {
        return this.<Profile>run(new Callable<Profile> () {
            @Override
            public Profile call () throws Exception {
                return delegate.getCurrentProfile();
            }
        }, PROFILE_SERVICE);
    }

    private <T> T run (Callable<T> callable, String service) throws CloudException {
        try {
            Authentication auth = null;
            AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
            if (credentials != null && !credentials.getUserName().trim().isEmpty()) {
                String password = credentials.getPassword();
                auth = new UsernamePasswordAuthenticationToken(new User(credentials.getUserName(), password, true, true, true, true, 
                    Collections.EMPTY_LIST), password);
            }
            SecurityContextHolder.getContext().setAuthentication(auth);
            try {
                delegate.setBaseUrl(location.getUrl() + service);
                return callable.call();
            } finally {
                delegate.setBaseUrl(location.getUrl());
                SecurityContextHolder.getContext().setAuthentication(null);
            }
        } catch (Exception ex) {
            throw new CloudException(ex);
        }
    }

    
}
