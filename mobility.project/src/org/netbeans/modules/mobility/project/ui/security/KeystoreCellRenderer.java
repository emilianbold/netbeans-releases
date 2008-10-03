/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * KeystoreCellRenderer.java
 *
 * Created on June 1, 2004
 */
package org.netbeans.modules.mobility.project.ui.security;

import java.awt.Component;
import java.awt.Image;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import org.netbeans.modules.mobility.project.security.KeyStoreRepository;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

/**
 *
 * @author  Adam Sotona
 */
public class KeystoreCellRenderer extends DefaultListCellRenderer {
    
    private static final Image keystore = ImageUtilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/keystore.gif"); //NOI18N
    private static final Icon ICON_OPENED = new ImageIcon(ImageUtilities.mergeImages(keystore, ImageUtilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/unlockedBadge.gif"), 0, 0)); //NOI18N
    private static final Icon ICON_CLOSED = new ImageIcon(ImageUtilities.mergeImages(keystore, ImageUtilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/lockedBadge.gif"), 0, 0)); //NOI18N
    private static final Icon ICON_INVALID = new ImageIcon(ImageUtilities.mergeImages(keystore, ImageUtilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/invalidBadge.gif"), 0, 0)); //NOI18N
    public static final String buildinKeystoreString = NbBundle.getMessage(KeystoreCellRenderer.class, "NAME_BuildInKeyStore"); // NOI18N
        
    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        if (value instanceof KeyStoreRepository.KeyStoreBean) {
            final KeyStoreRepository.KeyStoreBean keystore = (KeyStoreRepository.KeyStoreBean)value;
            super.getListCellRendererComponent(list, KeyStoreRepository.isDefaultKeystore(keystore) ? buildinKeystoreString : keystore.getKeyStoreFile().getName(), index, isSelected, cellHasFocus);
            setIcon(keystore.isValid() ? (keystore.isOpened() ? ICON_OPENED : ICON_CLOSED) : ICON_INVALID);
        } else {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setIcon(null);
        }
        return this;
    }
    
}
