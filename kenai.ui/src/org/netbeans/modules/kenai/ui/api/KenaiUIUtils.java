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

package org.netbeans.modules.kenai.ui.api;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JLabel;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiUser;
import org.netbeans.modules.kenai.ui.dashboard.MemberNode;
import org.netbeans.modules.kenai.ui.impl.LoginUtils;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.openide.util.NbBundle;

/**
 * This class is not yet final. We be changed
 * @author Jan Becicka
 */
public final class KenaiUIUtils {

    // Usage logging
    private static Logger metricsLogger;
    private static final String USG_KENAI = "USG_KENAI"; // NOI18N
    /** To avoid logging same params more than once in a session. Expecting
     * less than 20 possible combinations at max. */
    private static Set<String> loggedParams = Collections.synchronizedSet(new HashSet<String>());

    public static String getPrefName(Kenai kenai, String name)  {
        return kenai.getUrl().getHost() + name;
    }

    public static void addDashboardListener(Kenai kenai, PropertyChangeListener propertyChangeListener) {
        KenaiServer.forKenai(kenai).getDashboard().addPropertyChangeListener(propertyChangeListener);
    }
    
    public static void removeDashboardListener(Kenai kenai, PropertyChangeListener propertyChangeListener) {
        KenaiServer.forKenai(kenai).getDashboard().removePropertyChangeListener(propertyChangeListener);
    }
    
    private KenaiUIUtils() {
    }

    /**
     * this method will be removed
     * will try to login using stored uname and password if not already logged in
     * @param force
     * @return true if logged in, false otherwise
     * @deprecated 
     */
    @Deprecated
    public static synchronized boolean tryLogin(Kenai kenai, boolean force) {
        return LoginUtils.tryLogin (kenai, force);
    }

    /**
     * @return
     */
    public static boolean showLogin() {
        return showKenaiLogin()!=null;
    }

    /**
     *
     * @return
     */
    public static Kenai showKenaiLogin() {
        for (Kenai k: KenaiManager.getDefault().getKenais()) {
            if (k.getStatus()==Kenai.Status.OFFLINE) {
                return showKenaiLogin(k);
            }
        }
        return showKenaiLogin(null);
    }

    /**
     * Invokes login dialog
     * @param kenai
     * @return true, if user was succesfully logged in
     */
    public static boolean showLogin(final Kenai kenai) {
        return showKenaiLogin(kenai) != null;
    }

    /**
     * Invokes login dialog
     * @param kenai
     * @return kenai instance, where user requested login, or null if login was
     * cancelled
     */
    public static Kenai showKenaiLogin(final Kenai kenai) {
        TeamServer server = KenaiServer.forKenai(kenai);
        server = org.netbeans.modules.team.server.api.TeamUIUtils.showLogin(server, false);
        return (server instanceof KenaiServer) ? ((KenaiServer) server).getKenai() : null;
    }

    public static JLabel createUserWidget(String user) {
        return createUserWidget(new KenaiUserUI(user));
    }

    static JLabel createUserWidget(final KenaiUserUI u) {
        final JLabel result = new JLabel(u.getUserName());
        result.setIcon(u.getIcon());
        String firstName = u.getKenaiUser().getFirstName();
        final String name = (firstName==null)?"":firstName + " " + u.getKenaiUser().getLastName(); // NOI18N
        result.setToolTipText(NbBundle.getMessage(MemberNode.class, u.getKenaiUser().isOnline()?"LBL_ONLINE_MEMBER_TOOLTIP": "LBL_OFFLINE_MEMBER_TOOLTIP", u.getUserName(), name));
        u.user.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (KenaiUser.PROP_PRESENCE.equals(evt.getPropertyName())) {
                    result.firePropertyChange(KenaiUser.PROP_PRESENCE, (Boolean) evt.getOldValue(), (Boolean) evt.getNewValue());
                    String firstName = u.getKenaiUser().getFirstName();
                    String name = (firstName==null)?"":firstName + " " + u.getKenaiUser().getLastName(); // NOI18N
                    result.setToolTipText(NbBundle.getMessage(MemberNode.class, u.getKenaiUser().isOnline()?"LBL_ONLINE_MEMBER_TOOLTIP": "LBL_OFFLINE_MEMBER_TOOLTIP", u.getUserName(), name));
                    result.repaint();
                }
            }
        });
        result.addMouseListener(new MouseAdapter() {
            private Cursor oldCursor;

            @Override
            public void mouseEntered(MouseEvent e) {
                oldCursor = result.getCursor();
                result.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                result.setCursor(oldCursor);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton()==1) {
                    u.startChat();
                }
            }

        });

        return result;
    }

    public static ProjectHandle<KenaiProject>[] getDashboardProjects(boolean onlyOpened) {
        return KenaiServer.getDashboardProjects(onlyOpened);
    }
    
    public static void logKenaiUsage(Object... parameters) {
        String paramStr = getParamString(parameters);
        if (loggedParams.add(paramStr)) {
            // not logged in this session yet
            if (metricsLogger == null) {
                metricsLogger = Logger.getLogger("org.netbeans.ui.metrics.kenai"); // NOI18N
            }
            LogRecord rec = new LogRecord(Level.INFO, USG_KENAI);
            rec.setParameters(parameters);
            rec.setLoggerName(metricsLogger.getName());
            metricsLogger.log(rec);
        }
    }

    private static String getParamString(Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return ""; // NOI18N
        }
        if (parameters.length == 1) {
            return parameters[0].toString();
        }
        StringBuilder buf = new StringBuilder();
        for (Object p : parameters) {
            buf.append(p.toString());
        }
        return buf.toString();
    }

}

