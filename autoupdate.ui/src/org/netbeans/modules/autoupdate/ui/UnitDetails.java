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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.openide.modules.ModuleInfo;
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

    public void setUnitCategory(UnitCategory unitCategory) {
        String text = "<b>" + getBundle ("UnitDetails_Category") + "</b>" + unitCategory.getCategoryName() + "<br>"; // NOI18N
        
        
        getDetails().setText(text);
        getDetails().setCaretPosition(0);        
    }
    
    @SuppressWarnings ("deprecation")
    public void setUnit(Unit u) {
        if (u == null) {
            getDetails ().setText ("<i>" + getBundle ("UnitDetails_Category_NoDescription") + "</i>"); // NOI18N
        } else {
            String text;
            try {
                text = "<h3>" + u.annotate(XMLUtil.toElementContent(u.getDisplayName())) + "</h3>"; // NOI18N
                text += "<b>" + getBundle ("UnitDetails_Plugin_Version") + "</b>" + u.annotate(u.getDisplayVersion()) + "<br>"; // NOI18N
                if (u.getAuthor () != null && u.getAuthor ().length () > 0) {
                    text += "<b>" + getBundle ("UnitDetails_Plugin_Author") + "</b>" + u.annotate(u.getAuthor ()) + "<br>"; // NOI18N
                }
                text += "<b>" + getBundle ("UnitDetails_Plugin_Source") + "</b>" + u.annotate(u.getSource()) + "<br>"; // NOI18N

                // XXX: Temporary only for development
                if (u.updateUnit != null && u.updateUnit.getInstalled() != null) {
                    UpdateElement elem = u.updateUnit.getInstalled();
                    ModuleInfo m = ModuleProvider.getInstalledModules().get(elem.getCodeName());
                    boolean isEnabled = m != null && m.isEnabled();
                    OperationContainer<OperationSupport> c = null;
                    if (isEnabled) {
                        c = OperationContainer.createForDisable();
                    } else {
                        c = OperationContainer.createForEnable();
                    }
                    text += "<b>" + getBundle ("UnitDetails_Plugin_CodeName") + "</b>" + u.annotate(u.updateUnit.getCodeName()); // NOI18N
                    if (u.updateUnit.isAutoload() || u.updateUnit.isEager () || u.updateUnit.isFixed ()) {
                        text += " (";
                        text += u.updateUnit.isAutoload () ? "<i>" + getBundle ("UnitDetails_Plugin_Autoload") + "</i>" : ""; // NOI18N
                        text += u.updateUnit.isEager () ? "<i>" + getBundle ("UnitDetails_Plugin_Eager") + "</i>" : ""; // NOI18N
                        text += u.updateUnit.isFixed () ? "<i>" + getBundle ("UnitDetails_Plugin_Fixed") + "</i>" : ""; // NOI18N
                        text += ")";
                    }
                    text += "<br>";
                    if (c.canBeAdded (u.updateUnit, elem)) {
                        List<UpdateElement> elems = Utilities.getRequiredElements(u.updateUnit, elem, c);
                        if (elems.size() > 0) {
                            String pom = "";
                            for (UpdateElement updateElement : elems) {
                                pom += updateElement.getDisplayName() + ", ";
                            }
                            
                            if (isEnabled) {
                                text += "<b>" + getBundle ("UnitDetails_Plugin_RequiredBy") + "</b>" + pom + "<br>"; // NOI18N
                            } else {
                                text += "<b>" + getBundle ("UnitDetails_Plugin_Requires") + "</b>" + pom + "<br>"; // NOI18N
                            }
                        }
                        
                    }
                }
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
            getDetails().setCaretPosition(0);
        }
    }
    
    private static String getBundle (String key) {
        return NbBundle.getMessage (UnitDetails.class, key);
    }
    
}
