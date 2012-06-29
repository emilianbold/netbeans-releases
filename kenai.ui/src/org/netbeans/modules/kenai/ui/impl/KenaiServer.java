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
package org.netbeans.modules.kenai.ui.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.ui.dashboard.DashboardImpl;
import org.netbeans.modules.team.ui.spi.LoginPanelSupport;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamServerProvider;
import org.openide.util.WeakListeners;

/**
 *
 * @author Ondrej Vrabec
 */
public class KenaiServer implements TeamServer {
    private static final Map<Kenai, KenaiServer> serverMap = new WeakHashMap<Kenai, KenaiServer>(3);
    private final Kenai kenai;
    private PropertyChangeListener l;
    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    private KenaiServer (Kenai kenai) {
        this.kenai = kenai;
        kenai.addPropertyChangeListener(WeakListeners.propertyChange(l=new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (Kenai.PROP_LOGIN.equals(pce.getPropertyName()))  {
                    propertyChangeSupport.firePropertyChange(PROP_LOGIN, pce.getOldValue(), pce.getNewValue());
                }
            }
        }, kenai));
    }

    public static KenaiServer forKenai (Kenai kenai) {
        KenaiServer serverUi;
        synchronized (serverMap) {
            serverUi = serverMap.get(kenai);
            if (serverUi == null) {
                serverUi = new KenaiServer(kenai);
                serverMap.put(kenai, serverUi);
            }
        }
        return serverUi;
    }
    
    @Override
    public URL getUrl () {
        return kenai.getUrl();
    }

    @Override
    public Status getStatus () {
        return kenai.getStatus() != Kenai.Status.OFFLINE ? Status.ONLINE : Status.OFFLINE;
    }

    @Override
    public void logout () {
        kenai.logout();
    }

    @Override
    public String getDisplayName () {
        return kenai.getName();
    }

    @Override
    public Icon getIcon () {
        return kenai.getIcon();
    }

    @Override
    public void addPropertyChangeListener (PropertyChangeListener propertyChange) {
        propertyChangeSupport.addPropertyChangeListener(propertyChange);
    }

    @Override
    public void removePropertyChangeListener (PropertyChangeListener propertyChange) {
        propertyChangeSupport.removePropertyChangeListener(propertyChange);
    }

    @Override
    public TeamServerProvider getProvider () {
        return TeamServerProviderImpl.getDefault();
    }

    public Kenai getKenai () {
        return kenai;
    }

    @Override
    public LoginPanelSupport createLoginSupport () {
        return new LoginPanelSupportImpl(kenai);
    }

    @Override
    public JComponent getDashboardComponent () {
        DashboardImpl dashboard = DashboardImpl.getInstance();
        dashboard.setKenai(kenai);
        return dashboard.getComponent();
    }
    
}
