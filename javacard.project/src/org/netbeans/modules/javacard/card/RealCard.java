/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.card;

import org.netbeans.modules.javacard.api.JavacardPlatform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Anki R Nelaturu
 */
final class RealCard extends SunJavaCardServer {

    private static String CARD_MANAGER_APPLICATION = "/gemalto/admin";

    // Settings
    private String cardURL;
    private String cardHome;

    public RealCard(JavacardPlatform platform, String id, String displayName, String userName, String password, String cardURL,
            String cardHome) {
        super(platform, id, userName, password);
        this.cardURL = cardURL;
        this.cardHome = cardHome;
    }

    public String getCardHome() {
        return cardHome;
    }

    public void setCardHome(String cardHome) {
        this.cardHome = cardHome;
    }

    public String getCardURL() {
        return cardURL;
    }

    public void setCardURL(String cardURL) {
        this.cardURL = cardURL;
    }

    @Override
    public Set<Integer> getPortsInUse() {
        try {
            URL url = new URL("http://" + cardURL); //NOI18N
            int port = url.getPort();
            return Collections.singleton(port);
        } catch (MalformedURLException e) {
            Logger.getLogger(RealCard.class.getName()).log(Level.INFO, null, e);
        }
        return Collections.emptySet();
    }

    @Override
    public Set<Integer> getPortsInActiveUse() {
        return isRunning() ? getPortsInUse() : Collections.<Integer>emptySet();
    }

    @Override
    public final String getServerURL() {
        return "http://" + getCardURL(); //NOI18N
    }

    @Override
    public final String getCardManagerURL() {
        return getServerURL() + CARD_MANAGER_APPLICATION;
    }

    @Override
    public File getProcessDir() {
        return new File(getCardHome(), "bin");
    }

    @Override
    public String[] getStartCommandLine(boolean forDebug) {
        return new String[]{
            new File(getCardHome(), "bin/pcscmon.exe").getAbsolutePath()
        };
    }

    @Override
    public String[] getResumeCommandLine() {
        return null;
    }

    @Override
    public String[] getDebugProxyCommandLine(Object... args) {
        return new String[0];
    }
}
