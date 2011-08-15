/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
import java.awt.Image;
import java.io.CharConversionException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Jiri Rechtacek
 */
public class UnitDetails extends DetailsPanel {

    private final Logger err = Logger.getLogger("org.netbeans.modules.autoupdate.ui.UnitDetails");
    private RequestProcessor.Task unitDetailsTask = null;
    static final RequestProcessor UNIT_DETAILS_PROCESSOR = new RequestProcessor("unit-details-processor", 1, true);

    /** Creates a new instance of UnitDetails */
    public UnitDetails() {
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UnitTable.class, "ACN_UnitDetails")); // NOI18N
    }

    public void setUnit(Unit u) {
        setUnit(u, null);
    }

    public void setUnit(final Unit u, Action action) {
        if (unitDetailsTask != null && !unitDetailsTask.isFinished()) {
            unitDetailsTask.cancel();
        }

        if (u == null) {
            getDetails().setText("<i>" + getBundle("UnitDetails_Category_NoDescription") + "</i>"); // NOI18N
            setTitle(null);
        } else {
            try {
                setTitle(XMLUtil.toElementContent(u.getDisplayName()));
            } catch (CharConversionException e) {
                err.log(Level.INFO, null, e);
                return;
            }
            setActionListener(action);
            setUnitText(u, getUnitText(u, false));

            if (u instanceof Unit.Update) {
                unitDetailsTask = UNIT_DETAILS_PROCESSOR.post(new Runnable() {

                    public void run() {
                        final StringBuilder text = getUnitText(u, true);
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                setUnitText(u, text);
                            }
                        });
                    }
                });
            }
        }
    }

    private void buildUnitText(Unit u, StringBuilder text, boolean collectDependencies) {
        if (u instanceof Unit.Available) {
            Unit.Available u1 = (Unit.Available) u;
            Image c = u1.getSourceIcon();
            Object url = c.getProperty("url", null);
            String categoryName = u1.getSourceDescription();
            text.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
            if (url instanceof URL) {
                text.append("<td><img src=\"" + url + "\"></img></td>");
            }
            text.append("<td></td>");
            text.append("<td>&nbsp;&nbsp;</td>");
            text.append("<td><b>" + categoryName + "</b></td>");
            text.append("</tr></table><br>");
        }

        if (Utilities.modulesOnly() || Utilities.showExtendedDescription()) {
            text.append("<b>" + getBundle("UnitDetails_Plugin_CodeName") + "</b>" + u.updateUnit.getCodeName()); // NOI18N
            text.append("<br>");

        }
        String desc = null;
        if (u instanceof Unit.Update) {
            Unit.Update uu = ((Unit.Update) u);
            text.append("<b>" + getBundle("UnitDetails_Plugin_InstalledVersion") + "</b>" + uu.getInstalledVersion() + "<br>"); // NOI18N
            text.append("<b>" + getBundle("UnitDetails_Plugin_AvailableVersion") + "</b>" + uu.getAvailableVersion() + "<br>"); // NOI18N
            desc = getDependencies(uu, collectDependencies);
        } else {
            text.append("<b>" + getBundle("UnitDetails_Plugin_Version") + "</b>" + u.getDisplayVersion() + "<br>"); // NOI18N
        }
        if (u.getAuthor() != null && u.getAuthor().length() > 0) {
            text.append("<b>" + getBundle("UnitDetails_Plugin_Author") + "</b>" + u.getAuthor() + "<br>"); // NOI18N
        }
        if (u.getDisplayDate() != null && u.getDisplayDate().length() > 0) {
            text.append("<b>" + getBundle("UnitDetails_Plugin_Date") + "</b>" + u.getDisplayDate() + "<br>"); // NOI18N
        }
        text.append("<b>" + getBundle("UnitDetails_Plugin_Source") + "</b>" + u.getSource() + "<br>"); // NOI18N

        if (u.getHomepage() != null && u.getHomepage().length() > 0) {
            text.append("<b>" + getBundle("UnitDetails_Plugin_Homepage") + "</b><a href=\"" + u.getHomepage() + "\">" + u.getHomepage() + "</a><br>"); // NOI18N
        }

        if (u.getNotification() != null && u.getNotification().length() > 0) {
            text.append("<br><h3>" + getBundle("UnitDetails_Plugin_Notification") + "</h3>"); // NOI18N
            text.append("<font color=\"red\">"); // NOI18N
            text.append(u.getNotification());
            text.append("</font><br>");  // NOI18N
        }

        if (u.getDescription() != null && u.getDescription().length() > 0) {
            text.append("<br><h3>" + getBundle("UnitDetails_Plugin_Description") + "</h3>"); // NOI18N
            String description = u.getDescription();
            if(description.toLowerCase().startsWith("<html>")) {
                text.append(description.substring(6));
            } else {
                text.append(description);
            }
        }
        if (desc != null && desc.length() > 0) {
            text.append("<br><br><h4>" + getBundle("Unit_InternalUpdates_Title") + "</h4>"); // NOI18N
            text.append(desc);
        }
    }

    private void setUnitText(Unit u, StringBuilder text) {
        getDetails().setText(text.toString());
        setUnitHighlighing(u);
    }

    private StringBuilder getUnitText(Unit u, boolean collectDependencies) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; ; i++) {
            try {
                buildUnitText(u, text, collectDependencies);
            } catch (IllegalStateException ex) {
                if (i > 100) {
                    throw ex;
                }
                Unit.log.log(Level.INFO, "Can't compute getUnitText for " + u, ex); // NOI18N
                continue;
            }
            break;
        }
        return text;
    }

    private void setUnitHighlighing(Unit u) {
        //TODO - use some color from UI palette instead of the hardcoded one,
        // if possible, to make it custom (or native) L&F friendly.
        final Color highlightColor = Color.YELLOW;
        final ColorHighlighter highlighter = new ColorHighlighter(getDetails(), highlightColor);

        int idx = highlighter.highlight(u.getFilter());
        getDetails().setCaretPosition(idx > 0 ? idx : 0);
    }

    private String getDependencies(Unit.Update uu, boolean collectDependencies) {
        if (!collectDependencies) {
            return "<i>" + getBundle("UnitDetails_Plugin_Collecting_Dependencies") + "</i><br>";
        }

        Unit u = uu;
        Set<UpdateElement> internalUpdates = new HashSet<UpdateElement>();
        if (!(u instanceof Unit.InternalUpdate)) {
            OperationContainer<InstallSupport> container = OperationContainer.createForUpdate();

            try {
                container.add(u.updateUnit, uu.getRelevantElement());
            } catch (IllegalArgumentException ex) {
                Exceptions.attachMessage(ex, "Unit: " + u);
                Exceptions.attachMessage(ex, "Unit.updateUnit: " + u.updateUnit);
                Exceptions.attachMessage(ex, "Unit.getRelevantElement(): " + uu.getRelevantElement());
                throw ex;
            }
            Set<UpdateElement> required = new LinkedHashSet<UpdateElement>();
            List <OperationInfo<InstallSupport>> infos = container.listAll();

            for (OperationInfo<InstallSupport> info : infos) {
                Set<UpdateElement> reqs  = info.getRequiredElements();
                
                for (UpdateElement req : reqs) {
                    if (req.getUpdateUnit().getInstalled() != null && !req.getUpdateUnit().isPending()) {
                        required.add(req);                        
                    } else {
                        //OperationContainer.createForInstall().
                    }
                }                
            }
            for (OperationInfo<InstallSupport> i : infos) {
                if (!i.getUpdateUnit().equals(u.updateUnit) && !i.getUpdateUnit().isPending()) {
                    required.add(i.getUpdateElement());
                }
            }

            if (required.size() != 0) {
                List<UpdateElement> visibleRequirements = new ArrayList<UpdateElement>();
                for (UpdateElement ue : required) {
                    if (ue.getUpdateUnit().getType().equals(UpdateManager.TYPE.KIT_MODULE)) {
                        visibleRequirements.add(ue);
                    }
                }
                OperationContainer<InstallSupport> containerForVisibleUpdate = OperationContainer.createForUpdate();
                OperationContainer<InstallSupport> containerForVisibleInstall = OperationContainer.createForInstall();
                List<OperationInfo<InstallSupport>> infoList = new ArrayList<OperationInfo<InstallSupport>>();
                for (UpdateElement ue : visibleRequirements) {
                    if (containerForVisibleUpdate.canBeAdded(ue.getUpdateUnit(), ue)) {
                        infoList.add(containerForVisibleUpdate.add(ue));
                    } else if (containerForVisibleInstall.canBeAdded(ue.getUpdateUnit(), ue)) {
                        infoList.add(containerForVisibleInstall.add(ue));
                    }
                }
                List<UpdateElement> requiredElementsCoveredByVisible = new ArrayList<UpdateElement>();
                for (OperationInfo<InstallSupport> i : infoList) {
                    Set<UpdateElement> visibleRequired = i.getRequiredElements();
                    for (UpdateElement r : visibleRequired) {
                        if (!requiredElementsCoveredByVisible.contains(r)) {
                            requiredElementsCoveredByVisible.add(r);
                        }
                    }
                }


                for (UpdateElement ue : required) {
                    if (!requiredElementsCoveredByVisible.contains(ue) &&
                            !ue.getUpdateUnit().getType().equals(UpdateManager.TYPE.KIT_MODULE)) {
                        internalUpdates.add(ue);
                    }
                }
            }
        } else {
            Unit.InternalUpdate iu = (Unit.InternalUpdate) u;
            
            OperationContainer<InstallSupport> updContainer = OperationContainer.createForUpdate();
            for (UpdateUnit inv : iu.getUpdateUnits()) {
                updContainer.add(inv.getAvailableUpdates().get(0));
            }
            for (OperationInfo<InstallSupport> info : updContainer.listAll()) {
                internalUpdates.add(info.getUpdateElement());
                for (UpdateElement r : info.getRequiredElements()) {
                    if (r.getUpdateUnit().getInstalled() != null && !r.getUpdateUnit().isPending()) {
                        internalUpdates.add(r);
                    }

                }
            }
            /*
             *
            OperationContainer<InstallSupport> reiContainer = OperationContainer.createForInternalUpdate();
            reiContainer.add(iu.getRelevantElement());

            for (OperationInfo<InstallSupport> info : reiContainer.listAll()) {
                if (!info.getUpdateElement().equals(iu.updateUnit.getInstalled())) {
                    internalUpdates.add(info.getUpdateElement());
                }
                for (UpdateElement r : info.getRequiredElements()) {
                    if (r.getUpdateUnit().getInstalled() != null && !r.getUpdateUnit().isPending()) {
                        internalUpdates.add(r);
                    }
                }
            }
             * 
             */
        }
        StringBuilder desc = new StringBuilder();
        try {
        
        Set <UpdateElement> sorted = new TreeSet <UpdateElement> (new Comparator<UpdateElement> () {

                public int compare(UpdateElement o1, UpdateElement o2) {
                    return o1.getDisplayName().compareTo(o2.getDisplayName());
                }
            
        });
        sorted.addAll(internalUpdates);

        for (UpdateElement ue : sorted) {
            appendInternalUpdates(desc, ue);
        }
        } catch (Exception e) {
            err.log(Level.INFO, "Exception", e);
        }
        return desc.toString();
    }

    private void appendInternalUpdates(StringBuilder desc, UpdateElement ue) {
        desc.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        desc.append(ue.getDisplayName());
        if (ue.getUpdateUnit().getInstalled() != null) {
            desc.append(" [" + ue.getUpdateUnit().getInstalled().getSpecificationVersion() + "->");
        } else {
            desc.append(" <span color=\"red\">" + getBundle("UnitDetails_New_Internal_Update_Mark") + "</span> [");
        }

        desc.append(ue.getUpdateUnit().getAvailableUpdates().get(0).getSpecificationVersion());
        desc.append("]<br>");
    }



    private static String getBundle(String key) {
        return NbBundle.getMessage(UnitDetails.class, key);
    }
}
