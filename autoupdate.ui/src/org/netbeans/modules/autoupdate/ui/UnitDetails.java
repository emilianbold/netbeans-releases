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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.ui;

import java.io.CharConversionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
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
                //text = "<h3>" + u.annotateDisplayName(u.annotate(XMLUtil.toElementContent(u.getDisplayName()))) + "</h3>"; // NOI18N
                text = u.annotate(XMLUtil.toElementContent(u.getDisplayName())); // NOI18N
                setTitle(text);text = "";//NOI18N
                setActionListener(action);
                if (Utilities.modulesOnly () || Utilities.showExtendedDescription ()) {
                    text += "<b>" + getBundle ("UnitDetails_Plugin_CodeName") + "</b>" + u.annotate (u.updateUnit.getCodeName ()); // NOI18N
                    text += "<br>";

                }
                text += "<b>" + getBundle ("UnitDetails_Plugin_Version") + "</b>" + u.annotate(u.getDisplayVersion()) + "<br>"; // NOI18N
                if (u.getAuthor () != null && u.getAuthor ().length () > 0) {
                    text += "<b>" + getBundle ("UnitDetails_Plugin_Author") + "</b>" + u.annotate(u.getAuthor ()) + "<br>"; // NOI18N
                }
                if (u.getDisplayDate () != null && u.getDisplayDate ().length () > 0) {
                    text += "<b>" + getBundle ("UnitDetails_Plugin_Date") + "</b>" + u.annotate(u.getDisplayDate ()) + "<br>"; // NOI18N
                }
                text += "<b>" + getBundle ("UnitDetails_Plugin_Source") + "</b>" + u.annotate(u.getSource()) + "<br>"; // NOI18N

                if (u.getHomepage() != null && u.getHomepage().length() > 0) {
                    text += "<b>" + getBundle ("UnitDetails_Plugin_Homepage") + "</b><a href=\"" + u.getHomepage() + "\">" + u.annotate(u.getHomepage()) + "</a><br>"; // NOI18N
                }
                if (u.getDescription() != null && u.getDescription().length () > 0) {
                    text += "<h4>" + getBundle ("UnitDetails_Plugin_Description") + "</h4>"; // NOI18N
                    text += (u.getDescription() == null ? "" : u.annotate(u.getDescription ()));
                }
            } catch (CharConversionException e) {
                err.log (Level.WARNING, null, e);
                return;
            }
            getDetails().setText(text);
            Document d = getDetails().getDocument();
            int idx = -1;
            try {
                idx = u.findCaretPosition(d.getText(0, d.getLength()));
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            getDetails().setCaretPosition(idx > 0 ? idx : 0);
        }
    }
    
    private static String getBundle (String key) {
        return NbBundle.getMessage (UnitDetails.class, key);
    }
    
}
