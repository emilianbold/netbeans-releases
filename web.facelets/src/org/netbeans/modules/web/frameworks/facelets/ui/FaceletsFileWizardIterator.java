package org.netbeans.modules.web.frameworks.facelets.ui;

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.frameworks.facelets.FaceletsUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;

public final class FaceletsFileWizardIterator implements TemplateWizard.Iterator {
    
    private int index;
    private transient WizardDescriptor.Panel[] panels;
    
    private transient SourceGroup[] sourceGroups;
    
    /** Creates a new instance of TemplateClientIterator */
    public FaceletsFileWizardIterator () {
    }
    
    public Set instantiate(TemplateWizard wiz) throws IOException {
        final org.openide.filesystems.FileObject dir = Templates.getTargetFolder( wiz );
        final String targetName =  Templates.getTargetName(wiz);
        final DataFolder df = DataFolder.findFolder( dir );

        FaceletsUtils.setFaceletsFramework(dir);
        DataObject result = DataObject.find(Templates.getTemplate(wiz)).createFromTemplate(df, targetName);

        return Collections.singleton(result);
    }
    
    public void initialize(TemplateWizard wiz) {
        index = 0;
        Project project = Templates.getProject( wiz );
        panels = createPanels(project, wiz);
        
        // Creating steps.
        Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
        
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            FileObject documentBase = wm.getDocumentBase();
            FileObject targetFolder = Templates.getTargetFolder(wiz);
            String relativePath = (targetFolder == null) ? null : FileUtil.getRelativePath(documentBase, targetFolder);
            if (relativePath == null) {
                Templates.setTargetFolder(wiz, documentBase);
            }
        }
    }
    
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    public String name() {
        return NbBundle.getMessage(TemplateIterator.class, "TITLE_x_of_y",
                new Integer(index + 1), new Integer(panels.length));
    }
    
    public void previousPanel() {
        if (! hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    
    public void nextPanel() {
        if (! hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    public void addChangeListener(ChangeListener l) {
    }
    
    public void removeChangeListener(ChangeListener l) {
    }
    
    protected WizardDescriptor.Panel[] createPanels(Project project, TemplateWizard wiz) {
        Sources sources = (Sources) project.getLookup().lookup(org.netbeans.api.project.Sources.class);
        SourceGroup[] sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        // creates simple wizard panel with bottom panel
        WizardDescriptor.Panel firstPanel = Templates.createSimpleTargetChooser(project,sourceGroups);
        
        return new WizardDescriptor.Panel[] {
            firstPanel
        };
    }
    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }
    
}
