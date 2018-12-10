/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.oberthur;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Callback invoked by the ConnectionWatchdog owned by a Card instance.
 * The poll() method will update the state of the card based on whether
 * it can connect to its URL.
 *
 */
final class StateUpdaterCallback implements org.netbeans.modules.javacard.spi.ConnectionWatchdog.Callback<CardImpl> {

    private static final Logger LOGGER = Logger.getLogger(StateUpdaterCallback.class.getName());
    public void poll(CardImpl card) throws Exception {
        URL url = card.getPollUrl();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, this + " update card status"); //NOI18N
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(200);
            conn.setUseCaches(false);
            conn.setReadTimeout(200);
            conn.setRequestMethod("GET"); //NOI18N
            conn.setDoOutput(false);
            int code = conn.getResponseCode();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, this + " got response code " + //NOI18N
                        code);
            }
            card.setConnected(code == HttpURLConnection.HTTP_OK);
        } catch (IOException ex) {
            LOGGER.log(
                    Level.FINEST, "Could not connect to " + url, ex); //NOI18N
            card.setConnected(false);
        }
    }
}
