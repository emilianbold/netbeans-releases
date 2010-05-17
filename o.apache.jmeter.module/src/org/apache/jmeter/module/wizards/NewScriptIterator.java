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
    Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
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
        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
        // Step name (actually the whole list for reference).
        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
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
