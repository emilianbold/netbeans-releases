/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.wizard;

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.wlm.model.api.Group;
import org.netbeans.modules.wlm.model.api.TAssignment;
import org.netbeans.modules.wlm.model.api.TPriority;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.TTitle;
import org.netbeans.modules.wlm.model.api.User;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.dataloader.WorklistDataObject;
import org.netbeans.modules.worklist.editor.designview.components.TextFieldEditor;
import org.netbeans.modules.worklist.util.NameGenerator;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

public final class WorklistWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private int index;
    
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        try {
        if (panels == null) {
                if(this.wizard instanceof TemplateWizard) {
                DataFolder folder = ((TemplateWizard)this.wizard).getTargetFolder();
                
                Project project = FileOwnerQuery.getOwner(folder.getPrimaryFile());
                Sources sources = (Sources) project.getLookup().lookup(org.netbeans.api.project.Sources.class);
                SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                WorklistWizardBottomPanel1 bottomPanel =new WorklistWizardBottomPanel1(folder);
                // creates simple wizard panel with bottom panel
                WizardDescriptor.Panel firstPanel = new WorklistWizardPanel1(Templates.createSimpleTargetChooser(project,sourceGroups,bottomPanel), bottomPanel);
                JComponent comp = (JComponent)firstPanel.getComponent();
                // the bottom panel should listen to changes on file name text field
                ((WorklistWizardBottomPanel1)bottomPanel).setNameTF(findFileNameField(comp, Templates.getTargetName(this.wizard)));

                panels = new WizardDescriptor.Panel[] {
                    //new WorklistWizardPanel1(folder),
                    firstPanel,
                    // new WorklistWizardPanel2()
                    //RIT need to add escalation new WorklistWizardPanel3(),
                    //RIT need to add timeout new WorklistWizardPanel4()
                };
                String[] steps = createSteps();
                for (int i = 0; i < panels.length; i++) {
                    Component c = panels[i].getComponent();
                    if (steps[i] == null) {
                        // Default step name to component name of panel. Mainly
                        // useful for getting the name of the target chooser to
                        // appear in the list of steps.
                        steps[i] = c.getName();
                    }
                    if (c instanceof JComponent) { // assume Swing components
                        JComponent jc = (JComponent) c;
                        // Sets step number of a component
                        jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                        // Sets steps names for a panel
                        jc.putClientProperty("WizardPanel_contentData", steps);
                        // Turn on subtitle creation on each step
                        jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                        // Show steps on the left side with the image on the background
                        jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                        // Turn on numbering of all steps
                        jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                    }
                }

             }
          }
        } catch(Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return panels;
    }
    
    public Set instantiate() throws IOException {
        if(this.wizard instanceof TemplateWizard) {
            TemplateWizard wiz = (TemplateWizard) this.wizard;
            FileObject dir = Templates.getTargetFolder( wiz );
            DataFolder df = DataFolder.findFolder( dir );
            FileObject template = Templates.getTemplate( wiz );
            
            DataObject dTemplate = DataObject.find( template );
            final DataObject dObj = dTemplate.createFromTemplate( df, Templates.getTargetName( wiz )  );
            //create new data object
            if (dObj != null && dObj instanceof WorklistDataObject) {
                WorklistDataObject wObj = (WorklistDataObject) dObj;
                WLMModel model = wObj.getModel();
                model.startTransaction();
                try {
                    TTask task = model.getTask();
                    //
                    String targetNS = NameGenerator.createNewTargetNamespace(wObj.getModel(), wObj);
                    task.setTargetNamespace(targetNS);

                    String taskName = (String) wiz.getProperty(WizardConstants.TASK_NAME);
                    Operation op = (Operation) wiz.getProperty(WizardConstants.TASK_OPERATION);
    //                String partnerLinkName = taskName + "PartnerLink";
                    task.setName(taskName);

                    // Init title
                    TTitle title = task.getTitle();
                    if (title == null) {
                        title = model.getFactory().createTitle(model);
                        task.setTitle(title);
                    }
                    if (taskName != null) {
                        if (taskName.indexOf('\'') >= 0) {
                            title.setContent("\"" + taskName + "\""); // NOI18N
                        } else {
                            title.setContent("'" + taskName + "'"); // NOI18N
                        }
                    } else {
                        title.setContent("'newTask'"); // NOI18N
                    }

                    // Init priority
                    TPriority priority = task.getPriority();
                    if (priority == null) {
                        priority = model.getFactory().createPriority(model);
                        task.setPriority(priority);
                    }
                    priority.setContent("'5'"); // NOI18N

    //                task.setPartnerLink(partnerLinkName);
                    task.setOperation(task.createOperationReference(op));

                    //assignment
                    List<UsersTableModel.User> users = (List<UsersTableModel.User>) wiz.getProperty(WizardConstants.TASK_USERS);
                    List<GroupsTableModel.Group> groups = (List<GroupsTableModel.Group>) wiz.getProperty(WizardConstants.TASK_GROUPS);
                    TAssignment assignment = null;
                    if(users != null) {
                        assignment = model.getFactory().createAssignment(model);
                        Iterator<UsersTableModel.User> itU = users.iterator();
                        while(itU.hasNext()) {
                            UsersTableModel.User user = itU.next();
                            User tuser = model.getFactory().createUser(model);
                            tuser.setContent(TextFieldEditor
                                    .textToXPath(user.getUserName()));
                            assignment.addUser(tuser);
                        }
                    }

                    if(groups != null) {
                        if(assignment == null) {
                            assignment = model.getFactory().createAssignment(model);
                        }

                        Iterator<GroupsTableModel.Group> itG = groups.iterator();
                        while(itG.hasNext()) {
                            GroupsTableModel.Group group = itG.next();
                            Group tgroup = model.getFactory().createGroup(model);
                            tgroup.setContent(TextFieldEditor
                                    .textToXPath(group.getGroupName()));
                            assignment.addGroup(tgroup);
                        }
                    }

                    if(assignment != null) {
                        task.setAssignment(assignment);
                    }

                    //escalation

                    //timeout
                } finally {
                    model.endTransaction();
                }

                try {
                    SaveCookie cookie = dObj.getCookie(SaveCookie.class);
                    if (cookie != null) {
                        cookie.save();
                    }
                } catch (Exception ex) {
                    Logger.getLogger(WorklistWizardIterator.class.getName())
                            .log(Level.WARNING, "Unable to save file. " // NOI18N
                            + "Exception message:" + ex.getMessage()); // NOI18N
                }

                if (wObj != null) {
                    try {
                        wObj.getWlmEditorSupport().getUndoManager()
                                .discardAllEdits();
                    } catch (Exception ex) {
                        Logger.getLogger(WorklistWizardIterator.class.getName())
                                .log(Level.WARNING,
                                "Unable to reset undo-redo manager. " // NOI18N
                                + "Exception message:" + ex.getMessage()); // NOI18N
                    }
                }

                //hack for some reason  editor is not open after
                //wizard is finished even though it has open cookie
                //so do it explicitly
                Runnable openRun = new Runnable() {
                    public void run() {
                        OpenCookie cookie = dObj.getCookie(OpenCookie.class);
                        if(cookie != null) {
                            cookie.open();
                        }
                    }
                    
                };
                
                SwingUtilities.invokeLater(openRun);
                return Collections.singleton(dObj);
            }
        }
        return Collections.EMPTY_SET;
    }
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }
    
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }
    
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }
    
    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }
    
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}
    
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
     */
    
    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData");
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        
        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }
        
        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }
    
    //from schema wizard
    private JTextField findFileNameField(Component panel, String text) {
        Collection<Component> allComponents = new ArrayList<Component>();
        getAllComponents(new Component[] {panel}, allComponents);
        for (Component c : allComponents) {
            // we assume that the first text field is the file text field
            if (c instanceof JTextField) {
                JTextField tf = (JTextField) c;
                //if (text.equals(tf.getText())) {
                return tf;
                //}
            }
        }
        return null;
    }

    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents( Component[] components, Collection<Component> allComponents ) {
        for( int i = 0; i < components.length; i++ ) {
            if( components[i] != null ) {
                allComponents.add( components[i] );
                if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
                    getAllComponents( ( ( Container )components[i] ).getComponents(), allComponents );
                }
            }
        }
    }
}
