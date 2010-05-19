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

package org.netbeans.modules.soa.ldap.browser;

import java.awt.Component;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 *
 * @author anjeleevich
 */
public class IconPool {
    private static final Map<String, Icon> ICONS_MAP 
            = new HashMap<String, Icon>();

    private static final Map<String, Icon> ICONS_CACHE
            = new HashMap<String, Icon>();

    private static final Object SYNC = new Object();

    /**
     * @param objectClasses - ordered array of lowcase strings
     * @return if null, then use default icon
     */
    public static Icon getIcon(String[] objectClasses) {
        synchronized (SYNC) {
            if (objectClasses == null || objectClasses.length == 0) {
                return null;
            }

            String key = makeKey(objectClasses);
            Icon icon = ICONS_CACHE.get(key);

            if (icon == null) {
                for (String objectClass : objectClasses) {
                    icon = ICONS_MAP.get(objectClass);
                    if (icon != null) {
                        break;
                    }
                }
                if (icon == null) {
                    icon = DEFAULT_ICON;
                }
                
                ICONS_CACHE.put(key, icon);
            }

            return icon;
        }
    }

    private static String makeKey(String[] objectClasses) {
        StringBuilder builder = new StringBuilder();
        for (String objectClass : objectClasses) {
            builder.append(objectClass);
            builder.append(" ");
        }
        return builder.toString();
    }

    public static Icon loadImageIcon(String fileName) {
        return new ImageIcon(ImageUtilities.loadImage(
                "org/netbeans/modules/soa/ldap/resources/" // NOI18N
                + fileName + ".png")); // NOI18N
    }

    public static final Icon FOLDER_ICON = loadImageIcon("folder");
    public static final Icon LEAF_ICON = loadImageIcon("leaf");

    static {
        Icon userIcon = loadImageIcon("user"); // NOI18N
        Icon groupIcon = loadImageIcon("group"); // NOI18N
        Icon ouIcon = loadImageIcon("ou"); // NOI18N
        Icon ppolIcon = loadImageIcon("ppol"); // NOI18N

        ICONS_MAP.put("person", userIcon); // NOI18N
        ICONS_MAP.put("user", userIcon); // NOI18N
        ICONS_MAP.put("organizationalunit", ouIcon); // NOI18N
        ICONS_MAP.put("groupofurls", groupIcon); // NOI18N
        ICONS_MAP.put("group", groupIcon); // NOI18N
        ICONS_MAP.put("ds-virtual-static-group", groupIcon); // NOI18N
        ICONS_MAP.put("groupofuniquenames", groupIcon); // NOI18N
        ICONS_MAP.put("passwordpolicy", ppolIcon); // NOI18N
    }

    public static final Icon DEFAULT_ICON = new Icon() {
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        public int getIconWidth() {
            return 16;
        }

        public int getIconHeight() {
            return 16;
        }
    };
}
