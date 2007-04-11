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
        String text = "<b>Category: </b>" + unitCategory.getCategoryName() + "<br>";
        
        
        /*List<Unit> units = unitCategory.getUnits();
        String pom ="";
        for (Unit unit : units) {
            pom += unit.getDisplayName()+", ";
        }
        text = text + "<b>Units: </b>" + pom + "<br>";
         */
        getDetails().setText(text);
        getDetails().setCaretPosition(0);        
    }
    
    @SuppressWarnings ("deprecation")
    public void setUnit(Unit u) {
        if (u == null) {
            getDetails().setText("No description.");
        } else {
            String text;
            try {
                text = "<h2>" + XMLUtil.toElementContent(u.getDisplayName()) + "</h2>"; // NOI18N
                text += "<b>UpdateVersion: </b>" + u.getDisplayVersion() + "<br>";
                text += "<b>Author: </b>" + (u.getAuthor () == null ? "" : u.getAuthor ()) + "<br>";
                text += "<b>Source: </b>" + u.getSource() + "<br>";
                
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
                    text += "<br><b>CodeName: </b>" + u.updateUnit.getCodeName() + "<br>";
                    text += u.updateUnit.isAutoload () ? "<b>Autoload </b>" : "";
                    text += u.updateUnit.isEager () ? "<b>Eager </b>" : "";
                    text += u.updateUnit.isFixed () ? "<b>Fixed</b>" : "";
                    if (c.canBeAdded (u.updateUnit, elem)) {
                        List<UpdateElement> elems = Utilities.getRequiredElements(u.updateUnit, elem, c);
                        if (elems.size() > 0) {
                            String pom = "";
                            for (UpdateElement updateElement : elems) {
                                pom += updateElement.getDisplayName() + ", ";
                            }
                            
                            if (isEnabled) {
                                text += "<b>Required by: </b>" + pom + "<br>";
                            } else {
                                text += "<b>Requires: </b>" + pom + "<br>";
                            }
                        }
                        
                    }
                }
                if (u.getHomepage() != null && u.getHomepage().length() > 0) {
                    text += "<b>Homepage: </b><a href=\"" + u.getHomepage() + "\">" + u.getHomepage() + "</a><br>";
                }
                text += "<h3>Plugin Description</h3>";
                text += (u.getDescription() == null ? "" : XMLUtil.toElementContent(u.getDescription()));
            } catch (CharConversionException e) {
                err.log (Level.WARNING, null, e);
                return;
            }
            getDetails().setText(text);
            getDetails().setCaretPosition(0);
        }
    }    
}
