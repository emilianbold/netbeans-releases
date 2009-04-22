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

package org.netbeans.modules.project.ui.actions;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Arrays;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.project.ui.NoMainProjectWarning;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Actions;
import org.openide.awt.MouseUtils;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/** Invokes command on the main project.
 *
 * @author Pet Hrebejk
 */
public class MainProjectAction extends LookupSensitiveAction implements PropertyChangeListener {

    private String command;
    private ProjectActionPerformer performer;
    private String name;

    public MainProjectAction(ProjectActionPerformer performer, String name, Icon icon) {
        this( null, performer, name, icon );
    }

    public MainProjectAction(String command, String name, Icon icon) {
        this( command, null, name, icon );
    }

    public MainProjectAction(String command, ProjectActionPerformer performer, String name, Icon icon) {

        super(icon, null, new Class[] { Project.class, DataObject.class });
        this.command = command;
        this.performer = performer;
        this.name = name;

        String presenterName = "";
        if (name != null) {
            presenterName = MessageFormat.format(name, new Object[] { -1 });
        }
        setDisplayName(presenterName);
        if ( icon != null ) {
            setSmallIcon( icon );
        }

        // Start listening on open projects list to correctly enable the action
        OpenProjectList.getDefault().addPropertyChangeListener( WeakListeners.propertyChange( this, OpenProjectList.getDefault() ) );
        // XXX #47160: listen to changes in supported commands on current project, when that becomes possible
    }

    @Override
    protected boolean init() {
        boolean needsInit = super.init();
        if (needsInit) {
            refreshView(null);
        }
        return needsInit;
    }



    public void actionPerformed(Lookup context) {

        // first try to find main project
        Project p = OpenProjectList.getDefault().getMainProject();

        // then try to find some selected project
        if (p == null) {
            Project[] projects = ActionsUtil.getProjectsFromLookup(context, command);
            if (projects.length == 1) {
                p = projects[0];
            }
        }

        // then if there is only one project opened in IDE - use it
        if (p == null) {
            Project[] projects = OpenProjects.getDefault().getOpenProjects();
            if (projects.length == 1) {
                p = projects[0];
            }
        }

        // if no main project or no selected or more than one project opened,
        // then show warning and allow choose a main project
        if (p == null) {
            // show warning, if cancel then return
            if (showNoMainProjectWarning (OpenProjectList.getDefault().getOpenProjects (), 
                    getPresenterName(name, OpenProjectList.getDefault().getMainProject(), p))) {
                return ;
            }
            p = OpenProjectList.getDefault().getMainProject();
        }

        if ( command != null ) {
            ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
            if (ap != null) {
                if (Arrays.asList(ap.getSupportedActions()).contains(command)) {
                    ap.invokeAction(command, Lookup.EMPTY);
                } else {
                    // #47160: was a supported command (e.g. on a freeform project) but was then removed.
                    Toolkit.getDefaultToolkit().beep();
                    refreshView(null);
                }
            }
        }
        else {
            performer.perform( p );
        }
    }


    // Private methods ---------------------------------------------------------

    // Implementation of PropertyChangeListener --------------------------------

    public void propertyChange( PropertyChangeEvent evt ) {
        if ( evt.getPropertyName() == OpenProjectList.PROPERTY_MAIN_PROJECT ||
             evt.getPropertyName() == OpenProjectList.PROPERTY_OPEN_PROJECTS ) {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    refreshView(null);
                }
            });
        }
    }

    private void refreshView(Lookup context) {

        Project p = OpenProjectList.getDefault().getMainProject();
        Lookup theContext = context;

        if (p == null) {
            if (theContext == null) {
                theContext = LastActivatedWindowLookup.INSTANCE;
            }
            if (theContext != null) {
                Project[] projects = ActionsUtil.getProjectsFromLookup(theContext, command);
                if (projects.length == 1) {
                    p = projects[0];
                }
            }
        }

        if (p == null) {
            Project[] projects = OpenProjects.getDefault().getOpenProjects();
            if (projects.length == 1) {
                p = projects[0];
            }
        }

        boolean noOpenProject = OpenProjectList.getDefault ().getOpenProjects ().length == 0;

        if ( command == null ) {
            enable( performer.enable( p ) );
        }
        else {
            if ( p == null ) {
                enable(false);
            }
            else if ( ActionsUtil.commandSupported ( p, command, Lookup.EMPTY ) ) {
                enable( !noOpenProject );
            }
            else {
                enable( false );
            }
        }

        Project mainProject = OpenProjectList.getDefault().getMainProject();

        String presenterName = getPresenterName(name, mainProject, p);
        putValue("menuText", presenterName);
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));

    }

    private String getPresenterName(String name, Project mPrj, Project cPrj) {
        String toReturn = "";
        Object[] formatterArgs;
        if (mPrj == null) {
            if (cPrj == null) {
                formatterArgs = new Object[] { 0 };
            } else {
                formatterArgs = new Object[] { 1, ProjectUtils.getInformation(cPrj).getDisplayName() };
            }
        } else {
            formatterArgs = new Object[] { -1 };
        }
        if (name != null) {
            toReturn = MessageFormat.format(name, formatterArgs);
        }
        return toReturn;
    }

    private void enable(final boolean enable) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    setEnabled(enable);
                }
            });
        } else {
            setEnabled(enable);
        }
    }

    private boolean showNoMainProjectWarning(Project[] projects, String action) {
        boolean canceled;
        final JButton okButton = new JButton (NbBundle.getMessage (NoMainProjectWarning.class, "LBL_NoMainClassWarning_ChooseMainProject_OK")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (NoMainProjectWarning.class, "AD_NoMainClassWarning_ChooseMainProject_OK"));

        // no main project set => warning
        final NoMainProjectWarning panel = new NoMainProjectWarning (projects);

        Object[] options = new Object[] {
            okButton,
            DialogDescriptor.CANCEL_OPTION
        };

        panel.addChangeListener (new ChangeListener () {
           public void stateChanged (ChangeEvent e) {
               if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                   // click button and the finish dialog with selected class
                   if (panel.getSelectedProject () != null) {
                       okButton.doClick ();
                   }
               } else {
                   okButton.setEnabled (panel.getSelectedProject () != null);
               }
           }
        });

        okButton.setEnabled (panel.getSelectedProject () != null);

        DialogDescriptor desc = new DialogDescriptor (panel,
                action == null ?
                    NbBundle.getMessage(NoMainProjectWarning.class, "CTL_NoMainProjectWarning_Title") :
                    Actions.cutAmpersand(action),
            true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
        desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
        dlg.setVisible (true);
        if (desc.getValue() != options[0]) {
            canceled = true;
        } else {
            Project mainProject = panel.getSelectedProject ();
            OpenProjectList.getDefault ().setMainProject (mainProject);
            canceled = false;
        }
        dlg.dispose();

        return canceled;
    }

    @Override
    protected void refresh(Lookup context) {
        refreshView(context);
    }

    /* Backed out; see issue #105664 for discussion:
    public Component getToolbarPresenter() {
        final JButton button = DropDownButtonFactory.createDropDownButton(
                new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)), null); // image replaced anyway
        Actions.connect(button, this);
        final PropertyChangeListener[] weakPCL = {null};
        PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String prop = evt != null ? evt.getPropertyName() : null;
                if (prop == null || prop.equals(OpenProjectList.PROPERTY_MAIN_PROJECT) ||
                        prop.equals(ProjectConfigurationProvider.PROP_CONFIGURATIONS)) {
                    Mutex.EVENT.readAccess(new Runnable() {
                        public void run() {
                            JPopupMenu menu = null;
                            final Project p = OpenProjectList.getDefault().getMainProject();
                            if (p != null) {
                                ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
                                if (ap != null) {
                                    if (Arrays.asList(ap.getSupportedActions()).contains(command)) {
                                        final ProjectConfigurationProvider<?> pcp =
                                                p.getLookup().lookup(ProjectConfigurationProvider.class);
                                        if (pcp != null) {
                                            pcp.removePropertyChangeListener(weakPCL[0]);
                                            pcp.addPropertyChangeListener(weakPCL[0]);
                                            if (pcp.configurationsAffectAction(command) && pcp.getConfigurations().size() > 1) {
                                                menu = new JPopupMenu();
                                                for (final ProjectConfiguration config : pcp.getConfigurations()) {
                                                    JMenuItem item = new JMenuItem(config.getDisplayName());
                                                    menu.add(item);
                                                    item.addActionListener(new ActionListener() {
                                                        public void actionPerformed(ActionEvent e) {
                                                            p.getLookup().lookup(ActionProvider.class).invokeAction(
                                                                    command, Lookups.singleton(config));
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            button.putClientProperty(DropDownButtonFactory.PROP_DROP_DOWN_MENU, menu);
                        }
                    });
                }
            }
        };
        // avoid premature GC:
        button.putClientProperty("listener", pcl); // NOI18N
        weakPCL[0] = WeakListeners.propertyChange(pcl, null);
        OpenProjectList.getDefault().addPropertyChangeListener(weakPCL[0]);
        pcl.propertyChange(null);
        return button;
    }
    */

}
