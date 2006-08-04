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
 * KeyAliasCellRenderer.java
 *
 * Created on June 1, 2004
 */
package org.netbeans.modules.mobility.project.ui.security;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.text.DateFormat;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import org.netbeans.modules.mobility.project.security.KeyStoreRepository;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

/**
 *
 * @author  Adam Sotona, David Kaspar
 */
public class KeyAliasCellRenderer extends DefaultListCellRenderer {
    
    private static final Image key = Utilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/key.gif"); //NOI18N
    private static final Icon ICON_OPENED = new ImageIcon(Utilities.mergeImages(key, Utilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/unlockedBadge.gif"), 0, 0)); //NOI18N
    private static final Icon ICON_CLOSED = new ImageIcon(Utilities.mergeImages(key, Utilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/lockedBadge.gif"), 0, 0)); //NOI18N
    private static final Icon ICON_INVALID = new ImageIcon(Utilities.mergeImages(key, Utilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/invalidBadge.gif"), 0, 0)); //NOI18N
    
    public static final String notAvailableString = NbBundle.getMessage(KeyAliasCellRenderer.class, "LBL_NotAvailable"); // NOI18N
    public static final String invalidKeyAliasString = NbBundle.getMessage(KeyAliasCellRenderer.class, "LBL_InvalidKeyAlias"); // NOI18N
    public static final String keyAliasNotUnlockString = NbBundle.getMessage(KeyAliasCellRenderer.class, "LBL_KeyAliasNotUnlock"); // NOI18N
    public static final String subjectString = NbBundle.getMessage(KeyAliasCellRenderer.class, "LBL_Subject"); // NOI18N
    public static final String issuerString = NbBundle.getMessage(KeyAliasCellRenderer.class, "LBL_Issuer"); // NOI18N
    public static final String validString = NbBundle.getMessage(KeyAliasCellRenderer.class, "LBL_Valid"); // NOI18N
    
    public static final DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
    
    private boolean showDetails = false;
    
    public void setShowDetails(final boolean showDetails) {
        this.showDetails = showDetails;
        firePropertyChange("text", null, null); // NOI18N
    }
    
    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        if (value instanceof KeyStoreRepository.KeyStoreBean.KeyAliasBean) {
            setVerticalAlignment(TOP);
            final KeyStoreRepository.KeyStoreBean.KeyAliasBean alias = (KeyStoreRepository.KeyStoreBean.KeyAliasBean)value;
            super.getListCellRendererComponent(list, showDetails ? getHtmlFormattedText(alias, isSelected ? list.getSelectionForeground() : list.getForeground()) : alias.getAlias(), index, isSelected, cellHasFocus);
            setIcon(alias.isValid() ? (alias.isOpened() ? ICON_OPENED : ICON_CLOSED) : ICON_INVALID);
        } else {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setIcon(null);
        }
        return this;
    }
    
    public static String getHtmlFormattedText(final KeyStoreRepository.KeyStoreBean.KeyAliasBean alias) {
        return getHtmlFormattedText(alias, null);
    }
    
    public static String getHtmlFormattedText(final KeyStoreRepository.KeyStoreBean.KeyAliasBean alias, final Color color) {
        if (alias == null)
            return notAvailableString;
        final String head = color == null ? "<html><b>" : ("<html><font color=\"#" + Integer.toHexString(color.getRGB() & 0xffffff) + "\"><b>") ; //NOI18N
        if (! alias.isValid())
            return head + alias.getAlias() + "</b><br>" + invalidKeyAliasString; // NOI18N
        if (! alias.isOpened())
            return head + alias.getAlias() + "</b><br>" + keyAliasNotUnlockString; // NOI18N
        String form1=null;
        String form2=null;
        synchronized (format)
        {
            form1=format.format(alias.getNotBefore());
            form2=format.format(alias.getNotAfter());
        }
                
        return head + alias.getAlias() + "</b><br>" + // NOI18N
                "<i>" + subjectString + ":</i> " + alias.getSubjectName() + "<br>" + // NOI18N
                "<i>" + issuerString + ":</i> " + alias.getIssuerName() + "<br>" + // NOI18N
                "<i>" + validString + ":</i> " + form1 + " - " + form2; // NOI18N
    }
    
}
