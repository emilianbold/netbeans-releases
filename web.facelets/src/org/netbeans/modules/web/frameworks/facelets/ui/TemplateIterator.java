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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-200? Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.frameworks.facelets.ui;

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
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

/**
 *
 * @author Petr Pisl
 */
public class TemplateIterator implements TemplateWizard.Iterator {
    
    private int index;
    private transient WizardDescriptor.Panel[] panels;
    
    private TemplatePanel templatePanel;
    private transient SourceGroup[] sourceGroups;
    
    /** Creates a new instance of TemplateIterator */
    public TemplateIterator() {
    }
    
    public Set instantiate(TemplateWizard wiz) throws IOException {
        final org.openide.filesystems.FileObject dir = Templates.getTargetFolder( wiz );
        final String targetName =  Templates.getTargetName(wiz);
        final DataFolder df = DataFolder.findFolder( dir );
        final FileObject docBase = WebModule.getWebModule(df.getPrimaryFile()).getDocumentBase();
        
        df.getPrimaryFile().getFileSystem().runAtomicAction(new FileSystem.AtomicAction(){
            public void run() throws IOException {
                InputStream is;        
                FileObject target = df.getPrimaryFile().createData(targetName, "xhtml");
                
                FileObject cssFolder = docBase.getFileObject("css");
                if (cssFolder == null)
                    cssFolder = docBase.createFolder("css");
                // name of the layout file
                String layoutName = templatePanel.getLayoutFileName();
                FileObject cssFile = cssFolder.getFileObject(layoutName, "css");
                if (cssFile == null){
                    cssFile = cssFolder.createData(layoutName, "css");
                    is = templatePanel.getLayoutCSS();
                    FaceletsUtils.createFile(cssFile, FaceletsUtils.readResource(is, "UTF-8"), "UTF-8");
                }
                String layoutPath = FaceletsUtils.getRelativePath(target, cssFile);
                cssFile = cssFolder.getFileObject("default", "css");
                if (cssFile == null){
                    cssFile = cssFolder.createData("default", "css");
                    is = templatePanel.getDefaultCSS();
                    FaceletsUtils.createFile(cssFile, FaceletsUtils.readResource(is, "UTF-8"), "UTF-8");
                }
                String defatulPath =FaceletsUtils.getRelativePath(target, cssFile);
                
                is = templatePanel.getTemplate();
                String content = FaceletsUtils.readResource(is, "UTF-8");
                
                HashMap args = new HashMap();
                args.put("LAYOUT_CSS_PATH", layoutPath);
                args.put("DEFAULT_CSS_PATH", defatulPath);
                MapFormat formater = new MapFormat(args);
                formater.setLeftBrace("__");
                formater.setRightBrace("__");
                formater.setExactMatch(false);
                
                content = formater.format(content);
                
                FaceletsUtils.createFile(target, content, "UTF-8");
            }
        });
        
        FileObject target = df.getPrimaryFile().getFileObject(targetName, "xhtml");
        return Collections.singleton(DataObject.find(target));
    }
    
    public void initialize(TemplateWizard wiz) {
        //this.wiz = wiz;
        index = 0;
        Project project = Templates.getProject( wiz );
        panels = createPanels(project);
        
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
    
    protected WizardDescriptor.Panel[] createPanels(Project project) {
        Sources sources = (Sources) project.getLookup().lookup(org.netbeans.api.project.Sources.class);
        SourceGroup[] sourceGroups1 = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        SourceGroup[] sourceGroups;
        if (sourceGroups1.length < 2)
            sourceGroups = new SourceGroup[]{sourceGroups1[0], sourceGroups1[0]};
        else
            sourceGroups = sourceGroups1;
        
        templatePanel=new TemplatePanel();
        // creates simple wizard panel with bottom panel
        WizardDescriptor.Panel firstPanel = Templates.createSimpleTargetChooser(project,sourceGroups,templatePanel);
        JComponent c = (JComponent)firstPanel.getComponent();
        
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
