/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 * Creates a multi-view TopComponent from <code>AbstractBrandingPanel</code>s.
 *
 * @see AbstractBrandingPanel
 * 
 * @author S. Aubrecht
 */
public class BrandingEditor {

    private static final Map<Project, BrandingEditor> project2editor = new WeakHashMap<Project, BrandingEditor>(10);

    /**
     * Open branding editor for given suite project.
     * @param suite
     */
    public static void open( SuiteProject suite ) {
        SuiteProperties properties = new SuiteProperties(suite, suite.getHelper(), suite.getEvaluator(), SuiteUtils.getSubProjects(suite));
        BasicBrandingModel model = new BasicBrandingModel(properties);
        open( properties.getProjectDisplayName() + " - Branding", suite, model, true );
    }

    /**
     * Open branding editor for given generic project.
     * @param displayName Branding editor's display name.
     * @param p Project to be branded.
     * @param model Branding model.
     * @param contextAvailable True if the given project knows which platform
     * app it belongs and the platform jars/projects are available, false otherwise.
     */
    public static void open( String displayName, Project p, BasicBrandingModel model, boolean contextAvailable ) {
        synchronized( project2editor ) {
            BrandingEditor editor = project2editor.get(p);
            if( null == editor ) {
                editor = new BrandingEditor(displayName, p, model, contextAvailable);
                project2editor.put(p, editor);
            }
            editor.open();
        }
    }


    private final Project project;
    private final String title;
    private final BasicBrandingModel model;
    private TopComponent tc;
    private final Action saveAction;
    private final InstanceContent content = new InstanceContent();
    private final AbstractBrandingPanel[] panels;
    private boolean isModified = false;
    private boolean isValid = true;
    private Set<JLabel> errorLabels = new HashSet<JLabel>(10);
    private final boolean contextAvailable;

    private BrandingEditor( String title, Project p, final BasicBrandingModel model, boolean contextAvailable ) {
        this.project = p;
        this.title = title;
        this.contextAvailable = contextAvailable;
        final OpenProjects projects = OpenProjects.getDefault();
        projects.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if( OpenProjects.PROPERTY_OPEN_PROJECTS.equals( evt.getPropertyName() ) ) {
                    if( !projects.isProjectOpen(project) && null != tc ) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if( null != tc )
                                    tc.close();
                            }
                        });
                        projects.removePropertyChangeListener(this);
                    }
                }
            }
        });
        this.model = model;
        panels = new AbstractBrandingPanel[] {
            new BasicBrandingPanel(model),
            new SplashBrandingPanel(model),
            new WindowSystemBrandingPanel(model)
        };
        //TODO restrict the functionality of generic resource bundle editor when platform context isn't available
        saveAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSave();
            }
        };
        saveAction.putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/apisupport/project/ui/resources/save.png", true)); //NOI18N
        saveAction.setEnabled(false);
    }

    void setModified() {
        isModified = true;
        saveAction.setEnabled(isModified && isValid);
    }

    boolean isModified() {
        return isModified;
    }

    Action getSaveAction() {
        return saveAction;
    }


    private void open() {
        if( null == tc ) {
            tc = createWindow();
        }
        tc.open();
        tc.requestActive();
    }

    private void doSave() {
        for( AbstractBrandingPanel panel : panels ) {
            panel.store();
        }
        try {
            model.store();
        } catch( IOException ioE ) {
            Exceptions.printStackTrace(ioE);
        }

        isModified = false;
        saveAction.setEnabled(false);
    }

    private TopComponent createWindow() {
        Lookup lkp = new AbstractLookup(content);
        for( AbstractBrandingPanel panel : panels ) {
            panel.init(this, lkp);
        }
        MultiViewDescription[] tabs = new MultiViewDescription[panels.length];
        System.arraycopy(panels, 0, tabs, 0, panels.length);
        TopComponent res = MultiViewFactory.createMultiView(tabs, tabs[0], createCloseHandler());
        res.setDisplayName(title);
        return res;
    }

    Image getIcon() {
        Image res = null;
        ProjectInformation pi = project.getLookup().lookup(ProjectInformation.class);
        if( null != pi ) {
            res = ImageUtilities.icon2Image(pi.getIcon());
        }
        return res;
    }

    JLabel createErrorLabel() {
        JLabel res = new JLabel();
        res.setVisible(false);
        res.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/apisupport/project/ui/resources/error.gif", true)); //NOI18N
        errorLabels.add( res );
        return res;
    }

    void onBrandingValidation() {
        isValid = true;
        String errMessage = null;
        for( AbstractBrandingPanel panel : panels ) {
            isValid &= panel.isBrandingValid();
            String msg = panel.getErrorMessage();
            if( null != msg && null == errMessage )
                errMessage = msg;
        }
        for( JLabel lbl : errorLabels ) {
            lbl.setText(errMessage);
            lbl.setVisible(!isValid);
        }
        saveAction.setEnabled(isModified && isValid);
    }

    CloseOperationHandler createCloseHandler() {
        return new CloseOperationHandler() {
            @Override
            public boolean resolveCloseOperation(CloseOperationState[] elements) {
                if( isModified ) {
                    Object response = DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Confirmation(NbBundle.getMessage(BrandingEditor.class,
                            "Ask_SaveBrandingChanges"), tc.getDisplayName())); //NOI18N
                    if( response == NotifyDescriptor.NO_OPTION ) {
                        synchronized( project2editor ) {
                            project2editor.remove(project);
                        }
                        return true;
                    }
                    if( response == NotifyDescriptor.CANCEL_OPTION )
                        return false;
                    doSave();
                }
                return true;
            }
        };
    }
}
