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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.apache.jmeter.module.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik
 */
public class NewScriptIterator implements TemplateWizard.Iterator {

  private transient int index;
  private transient WizardDescriptor.Panel[] panels;
  private transient TemplateWizard wiz;

  private WizardDescriptor.Panel folderPanel;

  public static NewScriptIterator createInstance() {
    return new NewScriptIterator();
  }
  
  public void initialize(TemplateWizard wiz) {
    this.wiz = wiz;
    index = 0;
    Project project = Templates.getProject(wiz);
    panels = createPanels(project);

    // Creating steps.
    Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
    String[] beforeSteps = null;
    if (prop != null && prop instanceof String[]) {
      beforeSteps = (String[]) prop;
    }
    String[] steps = Utilities.createSteps(beforeSteps, panels);

    for (int i = 0; i < panels.length; i++) {
      Component c = panels[i].getComponent();
      if (steps[i] == null) {
        // Default step name to component name of panel.
        // Mainly useful for getting the name of the target
        // chooser to appear in the list of steps.
        steps[i] = c.getName();
      }
      if (c instanceof JComponent) {
        // assume Swing components
        JComponent jc = (JComponent) c;
        // Step #.
        jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
        // Step name (actually the whole list for reference).
        jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
      }
    }
  }

  public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
    org.openide.filesystems.FileObject dir = Templates.getTargetFolder(wiz);
    DataFolder df = DataFolder.findFolder(dir);
    FileObject template = Templates.getTemplate(wiz);
//    FileObject templateParent = template.getParent();
//    TargetChooserPanel panel = (TargetChooserPanel) folderPanel;
    DataObject dTemplate = DataObject.find(template);
    DataObject dobj = dTemplate.createFromTemplate(df, Templates.getTargetName(wiz));

    return Collections.singleton(dobj);
  }

  public void uninitialize(TemplateWizard wiz) {
    this.wiz = null;
    panels = null;
  }

  public void addChangeListener(ChangeListener l) {}

  public Panel<WizardDescriptor> current() {
    return panels[index];
  }

  public boolean hasNext() {
    return index < panels.length - 1;
  }

  public boolean hasPrevious() {
    return index > 0;
  }

  public String name() {
    return NbBundle.getMessage(NewScriptIterator.class, "TITLE_x_of_y",
            new Integer (index + 1), new Integer (panels.length));
  }

  public void nextPanel() {
    if (! hasNext ()) throw new NoSuchElementException ();
        index++;
  }

  public void previousPanel() {
    if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
  }

  public void removeChangeListener(ChangeListener l) {}

  // You should define what panels you want to use here:
  protected WizardDescriptor.Panel[] createPanels(Project project) {

    folderPanel = new TargetChooserPanel(project);
    return new WizardDescriptor.Panel[]{folderPanel};
  }
}
