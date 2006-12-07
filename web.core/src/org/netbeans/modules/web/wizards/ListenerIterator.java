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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import java.text.MessageFormat;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.web.core.Util;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.WizardDescriptor;
import org.openide.loaders.*;
import org.openide.util.NbBundle;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.openide.DialogDisplayer;

import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;

/** A template wizard iterator (sequence of panels).
 * Used to fill in the second and subsequent panels in the New wizard.
 * Associate this to a template inside a layer using the
 * Sequence of Panels extra property.
 * Create one or more panels from template as needed too.
 *
 * @author  mk115033
 */
public class ListenerIterator implements TemplateWizard.Iterator {

    //                                    CHANGEME vvv
    //private static final long serialVersionUID = ...L;

    // You should define what panels you want to use here:
    private ListenerPanel panel;
    protected WizardDescriptor.Panel[] createPanels (TemplateWizard wizard) {
        Project project = Templates.getProject( wiz );
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        panel = new ListenerPanel(wizard);
        
        WizardDescriptor.Panel packageChooserPanel;
        if (sourceGroups.length == 0)
            packageChooserPanel = Templates.createSimpleTargetChooser(project, sourceGroups, panel);
        else
            packageChooserPanel = JavaTemplates.createPackageChooser(project, sourceGroups, panel);

        return new WizardDescriptor.Panel[] {
            // Assuming you want to keep the default 2nd panel:
            packageChooserPanel
        };
    }

    public Set instantiate (TemplateWizard wiz) throws IOException/*, IllegalStateException*/ {
        // Here is the default plain behavior. Simply takes the selected
        // template (you need to have included the standard second panel
        // in createPanels(), or at least set the properties targetName and
        // targetFolder correctly), instantiates it in the provided
        // position, and returns the result.
        // More advanced wizards can create multiple objects from template
        // (return them all in the result of this method), populate file
        // contents on the fly, etc.
       
        FileObject folder = Templates.getTargetFolder( wiz );
        DataFolder targetFolder = DataFolder.findFolder( folder );
        
        ClassPath classPath = ClassPath.getClassPath(folder,ClassPath.SOURCE);
        String listenerName = wiz.getTargetName();
        DataObject result=null;
        
        if (classPath!=null) { //NOI18N
            DataObject template = wiz.getTemplate ();
            if (listenerName==null) {
                // Default name.
                result = template.createFromTemplate (targetFolder);
            } else {
                result = template.createFromTemplate (targetFolder, listenerName);
            }
            String className = classPath.getResourceName(result.getPrimaryFile(),'.',false);
            if (result!=null && panel.createElementInDD()){
                FileObject webAppFo=DeployData.getWebAppFor(folder);
                WebApp webApp=null;
                if (webAppFo!=null) {
                    webApp = DDProvider.getDefault().getDDRoot(webAppFo);
                }
                if (webApp!=null) {     
                    Listener[] oldListeners = webApp.getListener();
                    boolean found=false;
                    for (int i=0;i<oldListeners.length;i++) {
                        if (className.equals(oldListeners[i].getListenerClass())) {
                            found=true;
                            break;
                        }
                    }
                    if (!found) {
                        try {
                            Listener listener = (Listener)webApp.createBean("Listener");//NOI18N
                            listener.setListenerClass(className);
                            StringBuffer desc= new StringBuffer();
                            int i=0;
                            if (panel.isContextListener()) {
                                desc.append("ServletContextListener"); //NOI18N
                                i++;
                            }
                            if (panel.isContextAttrListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("ServletContextAttributeListener"); //NOI18N
                                i++;
                            }
                            if (panel.isSessionListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("HttpSessionListener"); //NOI18N
                                i++;
                            }
                            if (panel.isSessionAttrListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("HttpSessionAttributeListener"); //NOI18N
                            }
                            if (panel.isRequestListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("RequestListener"); //NOI18N
                                i++;
                            }
                            if (panel.isRequestAttrListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("RequestAttributeListener"); //NOI18N
                            }
                            listener.setDescription(desc.toString());
                            webApp.addListener(listener);
                            webApp.write(webAppFo);
                        } catch (ClassNotFoundException ex) {//Shouldn happen since
                        }
                    }
                }
            }
            if (result!=null) {
                JavaSource clazz = JavaSource.forFileObject(result.getPrimaryFile());
                if (clazz!=null) {
                    ListenerGenerator gen = new ListenerGenerator(
                        panel.isContextListener(),
                        panel.isContextAttrListener(),
                        panel.isSessionListener(),
                        panel.isSessionAttrListener(),
                        panel.isRequestListener(),
                        panel.isRequestAttrListener());
                    try {
                        gen.generate(clazz);
                    } catch (IOException ex){
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    }
                }
            }
        } else {
            String mes = MessageFormat.format (
                    NbBundle.getMessage (ListenerIterator.class, "TXT_wrongFolderForClass"),
                    new Object [] {"Servlet Listener"}); //NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);                         
        }
        return Collections.singleton (result);
    }

    // --- The rest probably does not need to be touched. ---

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wiz;

    private static final long serialVersionUID = -7586964579556513549L;
    
    // You can keep a reference to the TemplateWizard which can
    // provide various kinds of useful information such as
    // the currently selected target name.
    // Also the panels will receive wiz as their "settings" object.
    public void initialize (TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels (wiz);
        
        // Creating steps.
        Object prop = wiz.getProperty ("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = Utilities.createSteps (beforeSteps, panels);
        
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent ();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName ();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty ("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    public void uninitialize (TemplateWizard wiz) {
        this.wiz = null;
        panels = null;
    }

    // --- WizardDescriptor.Iterator METHODS: ---
    // Note that this is very similar to WizardDescriptor.Iterator, but with a
    // few more options for customization. If you e.g. want to make panels appear
    // or disappear dynamically, go ahead.

    public String name () {
        return NbBundle.getMessage(ListenerIterator.class, "TITLE_x_of_y",
            new Integer (index + 1), new Integer (panels.length));
    }

    public boolean hasNext () {
        return index < panels.length - 1;
    }
    public boolean hasPrevious () {
        return index > 0;
    }
    public void nextPanel () {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }
    public void previousPanel () {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }
    public WizardDescriptor.Panel current () {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener (ChangeListener l) {}
    public final void removeChangeListener (ChangeListener l) {}
    // If something changes dynamically (besides moving between panels),
    // e.g. the number of panels changes in response to user input, then
    // uncomment the following and call when needed:
    // fireChangeEvent ();
}
