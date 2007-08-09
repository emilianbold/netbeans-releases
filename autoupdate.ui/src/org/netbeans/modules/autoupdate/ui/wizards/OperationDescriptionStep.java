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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.ui.Containers;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Rechtacek
 */
public class OperationDescriptionStep implements WizardDescriptor.Panel<WizardDescriptor> {
    private static final String HEAD = "OperationDescriptionStep_Header_Head";
    private static final String CONTENT = "OperationDescriptionStep_Header_Content";
    private static final String TABLE_TITLE_INSTALL = "OperationDescriptionStep_TableInstall_Title";
    private static final String TABLE_TITLE_UPDATE = "OperationDescriptionStep_TableUpdate_Title";
    private static final String HEAD_UNINSTALL = "OperationDescriptionStep_HeaderUninstall_Head";
    private static final String CONTENT_UNINSTALL = "OperationDescriptionStep_HeaderUninstall_Content";
    private static final String TABLE_TITLE_UNINSTALL = "OperationDescriptionStep_TableUninstall_Title";
    private static final String HEAD_ACTIVATE = "OperationDescriptionStep_HeaderActivate_Head";
    private static final String CONTENT_ACTIVATE = "OperationDescriptionStep_HeaderActivate_Content";
    private static final String TABLE_TITLE_ACTIVATE = "OperationDescriptionStepActivate_Table_Title";
    private static final String HEAD_DEACTIVATE = "OperationDescriptionStep_HeaderDeativate_Head";
    private static final String CONTENT_DEACTIVATE = "OperationDescriptionStep_HeaderDeativate_Content";
    private static final String TABLE_TITLE_DEACTIVATE = "OperationDescriptionStep_TableDeativate_Title";
    private static final String DEPENDENCIES_TITLE_INSTALL = "DependenciesResolutionStep_Table_Title";
    private static final String DEPENDENCIES_TITLE_UPDATE = "DependenciesResolutionStep_Table_Title";
    private static final String DEPENDENCIES_TITLE_UNINSTALL = "UninstallDependenciesResolutionStep_Table_Title";
    private static final String DEPENDENCIES_TITLE_ACTIVATE = "OperationDescriptionStep_TableInstall_Title";
    private static final String DEPENDENCIES_TITLE_DEACTIVATE = "UninstallDependenciesResolutionStep_Table_Title";
    private static final String TABLE_TITLE_BROKEN = "OperationDescriptionStep_BrokenDependencies_Title";
    private static final String TITLE_BROKEN_DEPENDENCIES = "OperationDescriptionStep_BrokenDependencies";
    private PanelBodyContainer component;
    private OperationWizardModel model = null;
    private boolean readyToGo = false;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    private RequestProcessor.Task lazyDependingTask = null;
    
    /** Creates a new instance of OperationDescriptionStep */
    public OperationDescriptionStep (OperationWizardModel model) {
        this.model = model;
    }
    
    public Component getComponent() {
        if (component == null) {
            readyToGo = false;
            JPanel body;
            String tableTitle = null;
            String dependenciesTitle = null;
            String head = null;
            String content = null;
            switch (model.getOperation ()) {
            case LOCAL_DOWNLOAD :
                if (Containers.forUpdateNbms ().listAll ().isEmpty ()) {
                    tableTitle = getBundle (TABLE_TITLE_INSTALL);
                    dependenciesTitle = getBundle (DEPENDENCIES_TITLE_INSTALL);
                } else {
                    tableTitle = getBundle (TABLE_TITLE_UPDATE);
                    dependenciesTitle = getBundle (DEPENDENCIES_TITLE_UPDATE);
                }
                head = getBundle (HEAD);
                content = getBundle (CONTENT);
                break;
            case INSTALL :
                tableTitle = getBundle (TABLE_TITLE_INSTALL);
                dependenciesTitle = getBundle (DEPENDENCIES_TITLE_INSTALL);
                head = getBundle (HEAD);
                content = getBundle (CONTENT);
                break;
            case UPDATE :
                tableTitle = getBundle (TABLE_TITLE_UPDATE);
                dependenciesTitle = getBundle (DEPENDENCIES_TITLE_UPDATE);
                head = getBundle (HEAD);
                content = getBundle (CONTENT);
                break;
            case UNINSTALL :
                tableTitle = getBundle (TABLE_TITLE_UNINSTALL);
                dependenciesTitle = getBundle (DEPENDENCIES_TITLE_UNINSTALL);
                head = getBundle (HEAD_UNINSTALL);
                content = getBundle (CONTENT_UNINSTALL);
                break;
            case ENABLE :
                tableTitle = getBundle (TABLE_TITLE_ACTIVATE);
                dependenciesTitle = getBundle (DEPENDENCIES_TITLE_ACTIVATE);
                head = getBundle (HEAD_ACTIVATE);
                content = getBundle (CONTENT_ACTIVATE);
                break;
            case DISABLE :
                tableTitle = getBundle (TABLE_TITLE_DEACTIVATE);
                dependenciesTitle = getBundle (DEPENDENCIES_TITLE_DEACTIVATE);
                head = getBundle (HEAD_DEACTIVATE);
                content = getBundle (CONTENT_DEACTIVATE);
                break;
            }
            body = new OperationDescriptionPanel (tableTitle,
                    preparePluginsForShow (OperationWizardModel.getVisibleUpdateElements (model.getPrimaryUpdateElements (), false), model.getCustomHandledComponents ()),
                        getBundle ("OperationDescriptionStep_PleaseWait"), // NOI18N
                        "",
                        true);
            component = new PanelBodyContainer (head, content, body);
            component.setPreferredSize (OperationWizardModel.PREFFERED_DIMENSION);
            component.setWaitingState (true);
            appendDependingLazy (tableTitle, dependenciesTitle);
        }
        return component;
    }
    
    private void appendDependingLazy (final String tableTitle, final String dependenciesTitle) {
        lazyDependingTask = RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                JPanel body = null;
                if (model.hasBrokenDependencies ()) {
                    body = new OperationDescriptionPanel ("", "",
                            prepareBrokenDependenciesForShow (model),
                            "",
                            true);
                } else {
                    body = new OperationDescriptionPanel (tableTitle,
                            preparePluginsForShow (OperationWizardModel.getVisibleUpdateElements (model.getPrimaryUpdateElements (), false), model.getCustomHandledComponents ()),
                                dependenciesTitle,
                                preparePluginsForShow (OperationWizardModel.getVisibleUpdateElements (model.getRequiredUpdateElements (), true), null),
                                ! OperationWizardModel.getVisibleUpdateElements (model.getRequiredUpdateElements (), true).isEmpty ());
                }
                final JPanel finalPanel = body;
                readyToGo = model != null && ! model.hasBrokenDependencies ();
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        component.setBody (finalPanel);
                        component.setWaitingState (false);
                        fireChange ();
                    }
                });
            }
        });
    }
    
    private String prepareBrokenDependenciesForShow (OperationWizardModel model) {
        String s = new String ();
        for (String plugin : model.getBrokenDependencies ().keySet ()) {
            s += "<h3>" + NbBundle.getMessage (OperationDescriptionStep.class, "OperationDescriptionStep_PluginHasBrokenDependencies", plugin) + "</h3>";
            SortedSet<String> sset = new TreeSet<String> (model.getBrokenDependencies ().get (plugin));
            for (String dep : sset) {
                s += "      " + tryTakeDisplayName (dep) + "<br>"; // NOI18N
            }
        }
        return s.trim ();
    }
    
    private String tryTakeDisplayName (String dep) {
        String displayName = null;
        if (dep != null && dep.startsWith ("module")) { // NOI18N
            String codeName = dep.substring (6).trim ();
            int end = codeName.indexOf ('/'); // NOI18N
            if (end == -1) {
                end = codeName.indexOf (' '); // NOI18N
            }
            if (end != -1) {
                codeName = codeName.substring (0, end);
                for (UpdateUnit u : UpdateManager.getDefault ().getUpdateUnits (Utilities.getUnitTypes ())) {
                    if (codeName.equals (u.getCodeName ())) {
                        if (u.getInstalled () != null) {
                            displayName = u.getInstalled ().getDisplayName ();
                            break;
                        } else if (u.getAvailableUpdates ().size () > 0) {
                            displayName = u.getAvailableUpdates ().get (0).getDisplayName ();
                            break;
                        }
                    }
                }
            }
            if (displayName != null) {
                displayName = NbBundle.getMessage (OperationDescriptionStep.class, "OperationDescriptionStep_PluginNameFormat", displayName, dep);
            }
        }
        return displayName == null ? dep : displayName;
    }
    
    private String preparePluginsForShow (Set<UpdateElement> plugins, Set<UpdateElement> customHandled) {
        String s = new String ();
        List<String> names = new ArrayList<String> ();
        if (plugins != null && ! plugins.isEmpty ()) {
            for (UpdateElement el : plugins) {
                names.add ("<b>"  + el.getDisplayName () + "</b> " // NOI18N
                        + NbBundle.getMessage (OperationDescriptionStep.class, "OperationDescriptionStep_PluginVersionFormat",
                        el.getSpecificationVersion ()) + "<br>"); // NOI18N
            }
            Collections.sort (names);
            for (String name : names) {
                s += name;
            }
        }
        if (customHandled != null && ! customHandled.isEmpty ()) {
            names = new ArrayList<String> ();
            for (UpdateElement el : customHandled) {
                names.add ("<b>"  + el.getDisplayName () + "</b> " // NOI18N
                        + NbBundle.getMessage (OperationDescriptionStep.class, "OperationDescriptionStep_PluginVersionFormat",
                        el.getSpecificationVersion ()) + "<br>"); // NOI18N
            }
            Collections.sort (names);
            s += "<br>" + NbBundle.getMessage (OperationDescriptionStep.class, "OperationDescriptionStep_CustomHandled_Head", customHandled.size ()) + "<br>"; // NOI18N
            for (String name : names) {
                s += name;
            }
        }
        return s.trim ();
    }

    public HelpCtx getHelp() {
        return null;
    }

    public void readSettings(WizardDescriptor wd) {
        boolean approved = ! (model instanceof InstallUnitWizardModel) || ((InstallUnitWizardModel) model).allLicensesApproved ();
        if (approved) {
            model.modifyOptionsForDoOperation (wd);
        } else {
            model.modifyOptionsForStartWizard (wd);
        }
    }

    public void storeSettings(WizardDescriptor wd) {
        if (WizardDescriptor.CANCEL_OPTION.equals (wd.getValue ()) || WizardDescriptor.CLOSED_OPTION.equals (wd.getValue ())) {
            try {
                if (lazyDependingTask != null && ! lazyDependingTask.isFinished ()) {
                    lazyDependingTask.cancel ();
                }
                model.doCleanup ();
            } catch (OperationException x) {
                Logger.getLogger (InstallUnitWizardModel.class.getName ()).log (Level.INFO, x.getMessage (), x);
            }
        }
    }

    public boolean isValid () {
        return readyToGo;
    }

    public synchronized void addChangeListener (ChangeListener l) {
        listeners.add (l);
    }

    public synchronized void removeChangeListener (ChangeListener l) {
        listeners.remove (l);
    }

    private void fireChange () {
        ChangeEvent e = new ChangeEvent (this);
        List<ChangeListener> templist;
        synchronized (this) {
            templist = new ArrayList<ChangeListener> (listeners);
        }
        for (ChangeListener l : templist) {
            l.stateChanged (e);
        }
    }

    private String getBundle (String key) {
        return NbBundle.getMessage (OperationDescriptionPanel.class, key);
    }

}
