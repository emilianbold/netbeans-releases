/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ruby.rubyproject.templates;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.ruby.rubyproject.RubyProject;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Wizard to create a new Java file.
 * Based on org.netbeans.modules.ruby.rubyproject.templates.*.
 */
public class NewRubyFileWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private Type type;

    private static final long serialVersionUID = 1L;
    
    private final transient Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    
    static enum Type {
        FILE, CLASS, MODULE, TEST, SPEC
    }
    
    NewRubyFileWizardIterator() {
        this(Type.FILE);
    }
    
    NewRubyFileWizardIterator(final Type type) {
        this.type = type;
    }
    
    public static NewRubyFileWizardIterator classWizard() {
        return new NewRubyFileWizardIterator(Type.CLASS);
    }

    public static NewRubyFileWizardIterator moduleWizard() {
        return new NewRubyFileWizardIterator(Type.MODULE);
    }

    public static NewRubyFileWizardIterator testWizard() {
        return new NewRubyFileWizardIterator(Type.TEST);
    }

    public static NewRubyFileWizardIterator specWizard() {
        return new NewRubyFileWizardIterator(Type.SPEC);
    }

    private WizardDescriptor.Panel[] createPanels (WizardDescriptor wizardDescriptor) {
        // Ask for Ruby folders
        Project project = Templates.getProject( wizardDescriptor );
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(RubyProject.SOURCES_TYPE_RUBY);
        assert groups != null : "Cannot return null from Sources.getSourceGroups: " + sources;
        if (groups.length == 0) {
            groups = sources.getSourceGroups( Sources.TYPE_GENERIC ); 
            return new WizardDescriptor.Panel[] {            
                Templates.createSimpleTargetChooser( project, groups ),
            };
        } else {
            return new WizardDescriptor.Panel[] {
                new RubyTargetChooserPanel( project, groups, null, this.type)
            };
        }
    }
    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }
    
    public Set/*<FileObject>*/ instantiate () throws IOException {
        FileObject dir = Templates.getTargetFolder( wiz );
        String targetName = Templates.getTargetName( wiz );
        
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wiz );
        
        FileObject createdFile = null;
        DataObject dTemplate = DataObject.find( template );

        // Work around #109569
        @SuppressWarnings("unchecked")
        Map<String,Object> props = new HashMap(wiz.getProperties());
        props.remove("project"); // NOI18N

        DataObject dobj = dTemplate.createFromTemplate( df, targetName, props);
        createdFile = dobj.getPrimaryFile();
        
        return Collections.singleton( createdFile );
    }
    
        
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels( wiz );
        // Make sure list of steps is accurate.
        String[] beforeSteps = null;
        Object prop = wiz.getProperty (WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        if (prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps (beforeSteps, panels);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }
    public void uninitialize (WizardDescriptor wiz) {
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        //return "" + (index + 1) + " of " + panels.length;
        return ""; // NOI18N
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
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
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    public final void addChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        ChangeListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }
     
    
}
