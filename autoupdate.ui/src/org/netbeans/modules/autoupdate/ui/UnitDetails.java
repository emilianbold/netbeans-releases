/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.ui;

import java.awt.Color;
import java.io.CharConversionException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Jiri Rechtacek
 */
public class UnitDetails extends DetailsPanel{
    private final Logger err = Logger.getLogger ("org.netbeans.modules.autoupdate.ui.UnitDetails");
    
    /** Creates a new instance of UnitDetails */
    public UnitDetails() {
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UnitTable.class, "ACN_UnitDetails")); // NOI18N
    }

    public void setUnit(Unit u) {
        setUnit(u, null);
    }
    
    public void setUnit(Unit u, Action action) {
        if (u == null) {
            getDetails ().setText ("<i>" + getBundle ("UnitDetails_Category_NoDescription") + "</i>"); // NOI18N
            setTitle(null);
        } else {
            String text;
            try {
                text = XMLUtil.toElementContent(u.getDisplayName()); // NOI18N
                setTitle(text);text = "";//NOI18N
                setActionListener(action);
                if (u instanceof Unit.Available) {
                    Unit.Available u1 = (Unit.Available)u;
                    CATEGORY c = u1.getSourceCategory();
                    String categoryName = Utilities.getCategoryName(c);
                    URL icon = Utilities.getCategoryIcon(c);
                    text += "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>";
                    text += "<td><img src=\""+ icon.toExternalForm() +"\"></img></td>";
                    text += "<td>&nbsp;&nbsp;</td>";
                    text += "<td><b>"+ categoryName+ "</b></td>";
                    text += "</tr></table><br>";
                }
                                
                if (Utilities.modulesOnly () || Utilities.showExtendedDescription ()) {
                    text += "<b>" + getBundle ("UnitDetails_Plugin_CodeName") + "</b>" + u.updateUnit.getCodeName (); // NOI18N
                    text += "<br>";

                }
                if (u instanceof Unit.Update) {
                    Unit.Update uu = ((Unit.Update) u);
                    text += "<b>" + getBundle ("UnitDetails_Plugin_InstalledVersion") + "</b>" + uu.getInstalledVersion () + "<br>"; // NOI18N
                    text += "<b>" + getBundle ("UnitDetails_Plugin_AvailableVersion") + "</b>" + uu.getAvailableVersion () + "<br>"; // NOI18N
                } else {
                    text += "<b>" + getBundle ("UnitDetails_Plugin_Version") + "</b>" + u.getDisplayVersion() + "<br>"; // NOI18N
                }
                if (u.getAuthor () != null && u.getAuthor ().length () > 0) {
                    text += "<b>" + getBundle ("UnitDetails_Plugin_Author") + "</b>" + u.getAuthor () + "<br>"; // NOI18N
                }
                if (u.getDisplayDate () != null && u.getDisplayDate ().length () > 0) {
                    text += "<b>" + getBundle ("UnitDetails_Plugin_Date") + "</b>" + u.getDisplayDate () + "<br>"; // NOI18N
                }
                text += "<b>" + getBundle ("UnitDetails_Plugin_Source") + "</b>" + u.getSource() + "<br>"; // NOI18N

                if (u.getHomepage() != null && u.getHomepage().length() > 0) {
                    text += "<b>" + getBundle ("UnitDetails_Plugin_Homepage") + "</b><a href=\"" + u.getHomepage() + "\">" + u.getHomepage() + "</a><br>"; // NOI18N
                }
                                
                if (u.getNotification() != null && u.getNotification().length () > 0) {
                    text += "<br><h3>" + getBundle ("UnitDetails_Plugin_Notification") + "</h3>"; // NOI18N
                    text += "<font color=\"red\">"; // NOI18N
                    text += u.getNotification ();
                    text += "</font><br>";  // NOI18N
                }
                
                if (u.getDescription() != null && u.getDescription().length () > 0) {
                    text += "<br><h3>" + getBundle ("UnitDetails_Plugin_Description") + "</h3>"; // NOI18N
                    text += u.getDescription ();
                }
            } catch (CharConversionException e) {
                err.log (Level.INFO, null, e);
                return;
            }
            //TODO - use some color from UI palette instead of the hardcoded one,
            // if possible, to make it custom (or native) L&F friendly.
            final Color highlightColor  = Color.YELLOW;
            
            final ColorHighlighter highlighter = new ColorHighlighter(getDetails(), highlightColor);
            
            getDetails().setText(text);
            int idx = highlighter.highlight(u.getFilter());
            getDetails().setCaretPosition(idx > 0 ? idx : 0);
        }
    }
    
    private static String getBundle (String key) {
        return NbBundle.getMessage (UnitDetails.class, key);
    }
    
}
