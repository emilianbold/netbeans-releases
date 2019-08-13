/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.odcs.cnd.actions;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.odcs.cnd.http.HttpClientAdapter;
import org.netbeans.modules.odcs.cnd.http.HttpClientAdapterFactory;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ilia Gromov
 */
public abstract class RestAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(RestAction.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("REST request to Oracle Cloud", 3); // NOI18N

    private final String serverUrl;

    public RestAction(String serverUrl, String name) {
        super(name);
        this.serverUrl = serverUrl;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RP.submit(() -> {
            try {
                actionPerformedImpl(HttpClientAdapterFactory.get(serverUrl), e);
            } catch (Exception ex) {
                LOG.log(Level.FINE, "Exception in REST request", ex);
            }
        });
    }

    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Will be invoked not from EDT
     */
    public abstract void actionPerformedImpl(HttpClientAdapter client, ActionEvent e);

    public abstract String getRestUrl();

    protected String formatUrl(String template, String... params) {
        String result = template;

        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            result = result.replace("{" + i + "}", param); // NOI18N
        }

        return result;
    }
}
