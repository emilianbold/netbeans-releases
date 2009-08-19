/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.bridge.kenai;

import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import javax.swing.Icon;
import javax.swing.JLabel;
import org.netbeans.modules.bugtracking.util.KenaiUtil;
import org.netbeans.modules.versioning.util.VCSKenaiSupport;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.util.VCSKenaiSupport.class)
public class VCSKenaiSupportImpl extends VCSKenaiSupport {

    @Override
    public boolean isKenai(String url) {
        return KenaiUtil.isKenai(url);
    }
    
    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return getPasswordAuthentication(true);
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication(boolean forceLogin) {
        return KenaiUtil.getPasswordAuthentication(forceLogin);
    }

    @Override
    public boolean showLogin() {
        return KenaiUtil.showLogin();
    }

    @Override
    public boolean isLogged () {
        return KenaiUtil.isLoggedIn();
    }

    @Override
    public KenaiUser forName(String user) {
        org.netbeans.modules.kenai.ui.spi.KenaiUser kenaiUser =
                org.netbeans.modules.kenai.ui.spi.KenaiUser.forName(user);
        if(kenaiUser == null) {
            return null;
        } else {
            return new KenaiUserImpl(kenaiUser);
}
    }

    @Override
    public boolean isUserOnline(String user) {
        return org.netbeans.modules.kenai.ui.spi.KenaiUser.isOnline(user);
    }

    private class KenaiUserImpl extends KenaiUser {
        org.netbeans.modules.kenai.ui.spi.KenaiUser delegate;

        public KenaiUserImpl(org.netbeans.modules.kenai.ui.spi.KenaiUser delegate) {
            this.delegate = delegate;
        }

        @Override
        public void startChat() {
            delegate.startChat();
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            delegate.removePropertyChangeListener(listener);
        }

        @Override
        public boolean isOnline() {
            return delegate.isOnline();
        }

        @Override
        public String getUser() {
            return delegate.getUser();
        }

        @Override
        public Icon getIcon() {
            return delegate.getIcon();
        }

        @Override
        public JLabel createUserWidget() {
            return delegate.createUserWidget();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            delegate.addPropertyChangeListener(listener);
        }

    }
}
