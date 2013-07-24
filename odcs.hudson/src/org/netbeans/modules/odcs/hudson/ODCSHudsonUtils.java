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
package org.netbeans.modules.odcs.hudson;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSFactory;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;

/**
 *
 * @author jhavlin
 */
public final class ODCSHudsonUtils {

    private static Map<Icon, Icon> CENTERED_ICON_CACHE =
            new WeakHashMap<Icon, Icon>();

    private ODCSHudsonUtils() {
    }

    public static ODCSClient getClient(
            ProjectHandle<ODCSProject> projectHandle) {

        if (projectHandle != null) {
            ODCSProject teamProject = projectHandle.getTeamProject();
            if (teamProject != null) {
                ODCSServer server = teamProject.getServer();
                if (server != null && server.getUrl() != null
                        && server.getPasswordAuthentication() != null) {
                    return ODCSFactory.getInstance().createClient(
                            server.getUrl().toString(),
                            server.getPasswordAuthentication());
                }
            }
        }
        return null;
    }

    /**
     * Center Hudson status icon.
     *
     * Standard Hudson icons have the circle in them biased towards bottom right
     * corner. This method creates another icon with the same image, but in the
     * center of the area.
     */
    public static Icon centerIcon(Icon icon) {
        Icon cachedIcon = CENTERED_ICON_CACHE.get(icon);
        if (cachedIcon != null) {
            return cachedIcon;
        } else {
            Image img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics g = img.getGraphics();
            if (g instanceof Graphics2D) {
                Graphics2D g2d = (Graphics2D) g;
                icon.paintIcon(null, g2d, -3, -2);
            }
            Icon centeredIcon = new ImageIcon(img);
            CENTERED_ICON_CACHE.put(icon, centeredIcon);
            return centeredIcon;
        }
    }
}
