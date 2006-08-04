/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

/**
 *
 * @author  Adam Sotona
 */
public class KeystoreCellRenderer extends DefaultListCellRenderer {
    
    private static final Image keystore = Utilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/keystore.gif"); //NOI18N
    private static final Icon ICON_OPENED = new ImageIcon(Utilities.mergeImages(keystore, Utilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/unlockedBadge.gif"), 0, 0)); //NOI18N
    private static final Icon ICON_CLOSED = new ImageIcon(Utilities.mergeImages(keystore, Utilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/lockedBadge.gif"), 0, 0)); //NOI18N
    private static final Icon ICON_INVALID = new ImageIcon(Utilities.mergeImages(keystore, Utilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/invalidBadge.gif"), 0, 0)); //NOI18N
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
