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
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

/**
 *
 * @author  Adam Sotona, David Kaspar
 */
public class KeyAliasCellRenderer extends DefaultListCellRenderer {
    
    private static final Image key = ImageUtilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/key.gif"); //NOI18N
    private static final Icon ICON_OPENED = new ImageIcon(ImageUtilities.mergeImages(key, ImageUtilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/unlockedBadge.gif"), 0, 0)); //NOI18N
    private static final Icon ICON_CLOSED = new ImageIcon(ImageUtilities.mergeImages(key, ImageUtilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/lockedBadge.gif"), 0, 0)); //NOI18N
    private static final Icon ICON_INVALID = new ImageIcon(ImageUtilities.mergeImages(key, ImageUtilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/invalidBadge.gif"), 0, 0)); //NOI18N
    
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
