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

package org.netbeans.modules.versioning.util;

import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import javax.swing.Icon;
import javax.swing.JLabel;

/**
 *
 * @author Tomas Stupka
 */
public abstract class VCSKenaiSupport {

    /**
     * Returns an instance of PasswordAuthentication holding the actuall
     * Kenai credentials or null if user not logged in.
     *
     * @return
     */
    public abstract PasswordAuthentication getPasswordAuthentication();

    /**
     * Returns an instance of PasswordAuthentication holding the actuall
     * Kenai credentials or forces a login if forceLogin is true
     *
     * @param forceLogin opens a login dialog is user not logged in
     * @return
     */
    public abstract PasswordAuthentication getPasswordAuthentication(boolean forceLogin);

    /**
     * Returns true if the given url belongs to a Kenai project, otherwise false.
     * 
     * @param url
     * @return
     */
    public abstract boolean isKenai(String url);

    /**
     * Opens the kenai login dialog.
     * @return true if login successfull, otherwise false
     */
    public abstract boolean showLogin();

    /**
     * Returns true if user is logged into kenai
     * @return true if user is logged into kenai
     */
    public abstract boolean isLogged ();
    
    public abstract KenaiUser forName(final String user);

    public abstract boolean isUserOnline(String user);

    public abstract class KenaiUser {

        public abstract boolean isOnline();

        public abstract void addPropertyChangeListener(PropertyChangeListener listener);

        public abstract void removePropertyChangeListener(PropertyChangeListener listener);

        public abstract Icon getIcon();

        public abstract JLabel createUserWidget();

        public abstract String getUser();

        public abstract void startChat();
    }
    
}
