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
package org.netbeans.modules.xml.schema.wizard;

//java imports
import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.UndoableEditListener;

//netbeans imports
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.xam.ui.ProjectConstants;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.TemplateWizard;
import org.netbeans.spi.project.ui.templates.support.Templates;


/**
 * Schema wizard iterator. This guy is responsible for showing appropriate GUI
 * panels to the user, collecting inputs from those panels and based on those
 * collected inputs, will instantiate schema from templates.
 *
 * See layer.xml for template declaration.
 *
 * Read http://performance.netbeans.org/howto/dialogs/wizard-panels.html.
 * 
 * @author  Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SchemaWizardIterator extends Object implements TemplateWizard.Iterator {
        
    private static final long serialVersionUID = 1L;
    private int index;
    private final Set<ChangeListener> changeListeners = new HashSet<ChangeListener>();
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor.Panel schemaPanel;
    private SchemaAdditionalInfoGUI schemaGUI;
        
    /**
     * You should define what panels you want to use here:
     */
    protected WizardDescriptor.Panel[] createPanels (Project project,
						     final TemplateWizard wizard) {
        Sources sources = ProjectUtils.getSources(project);
	List<SourceGroup> roots = new ArrayList<SourceGroup>();
	SourceGroup[] javaRoots = 
	    sources.getSourceGroups(ProjectConstants.JAVA_SOURCES_TYPE);
	roots.addAll(Arrays.asList(javaRoots));
	if (roots.isEmpty()) {
	    SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
	    roots.addAll(Arrays.asList(sourceGroups));
	}
        schemaPanel = new SchemaAdditionalInfoPanel();
	schemaGUI = (SchemaAdditionalInfoGUI) schemaPanel.getComponent();
	DataFolder folder = DataFolder.findFolder(roots.get(0).getRootFolder());
	DataFolder projectFolder = 
	    DataFolder.findFolder(project.getProjectDirectory());
	try {
	    if (wizard.getTargetFolder().equals(projectFolder)) {
		wizard.setTargetFolder(folder);
	    }
	} catch (IOException ioe) {
	    wizard.setTargetFolder(folder);
	}
        WizardDescriptor.Panel panel = 
            Templates.createSimpleTargetChooser(project, 
		roots.toArray(new SourceGroup[roots.size()]), schemaPanel);
	schemaGUI.setParentPanel(panel);
	return new WizardDescriptor.Panel[] {panel};
    }
    
    /**
     * Initialization of the wizard iterator.
     */
    public void initialize(TemplateWizard wizard) {
        index = 0;
        Project project = Templates.getProject( wizard );
        panels = createPanels (project, wizard);
        
        // Creating steps.
        Object prop = wizard.getProperty ("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps (beforeSteps, panels);
        
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
                jc.putClientProperty ("WizardPanel_contentSelectedIndex", Integer.valueOf(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty ("WizardPanel_contentData", steps); // NOI18N
            }
        }        
    }

    /**
     * Cleanup.
     */
    public void uninitialize (TemplateWizard wiz) {
        panels = null;
    }

    /**
     * This is where, the schema gets instantiated from the template.
     */
    public Set instantiate (TemplateWizard wizard) throws IOException {
        String tns = schemaGUI.getTargetNamespace();
        
        FileObject dir = Templates.getTargetFolder( wizard );        
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wizard );        
        DataObject dTemplate = DataObject.find( template );                
        DataObject dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wizard )  );
        if (dobj == null)
            return Collections.emptySet();
            
        EditCookie edit = (EditCookie) dobj.getCookie (EditCookie.class);
        if (edit != null) {
            EditorCookie editorCookie = (EditorCookie)dobj.getCookie(EditorCookie.class);
            BaseDocument doc = (BaseDocument)editorCookie.openDocument();
            UndoableEditListener[] listeners = doc.getUndoableEditListeners();
            for(UndoableEditListener l : listeners) {
                doc.removeUndoableEditListener(l);
            }            
            if(tns.length() == 0) tns = SchemaAdditionalInfoGUI.DEFAULT_TARGET_NAMESPACE;
            replaceInDocument(doc, "#TARGET_NAMESPACE", tns); //NOI18N

            SaveCookie save = (SaveCookie)dobj.getCookie(SaveCookie.class);
            if (save!=null) save.save();
            for(UndoableEditListener l : listeners) {
                doc.addUndoableEditListener(l);
            }            
        }
        return Collections.singleton(dobj.getPrimaryFile());
    }
                
    /**
     *
     */
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     *
     *
     */
    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }
    
    /**
     *
     */
    public void fireStateChanged() {
        ChangeEvent event = new ChangeEvent(this);

        Iterator<ChangeListener> i = changeListeners.iterator();
        while (i.hasNext()) {
            try {
                i.next().stateChanged(event);
            } catch (Exception e) {
                //Debug.debugNotify(e);
            }
        }
    }
    
    /**
     *
     */
    public String name () {
        return NbBundle.getMessage(SchemaWizardIterator.class, "TITLE_x_of_y",
            Integer.valueOf(index + 1), Integer.valueOf(panels.length));
    }
    
    /**
     *
     */
    public boolean hasNext () {
        return index < panels.length - 1;
    }
    
    /**
     *
     */
    public boolean hasPrevious () {
        return index > 0;
    }
    
    /**
     *
     */
    public void nextPanel () {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }
    
    /**
     *
     */
    public void previousPanel () {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }
    
    /**
     * Returns the current panel.
     */
    public WizardDescriptor.Panel current () {
        return panels[index];
    }

    /**
     * Create steps.
     */
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        //assert panels != null;
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
    
    /**
     * Utility method to replace text in document.
     */
    private void replaceInDocument(javax.swing.text.Document document, String replaceFrom, String replaceTo) {
        javax.swing.text.AbstractDocument doc = (javax.swing.text.AbstractDocument)document;
        int len = replaceFrom.length();
        try {
            String content = doc.getText(0,doc.getLength());
            int index = content.lastIndexOf(replaceFrom);
            while (index>=0) {
                doc.replace(index,len,replaceTo,null);
                content=content.substring(0,index);
                index = content.lastIndexOf(replaceFrom);
            }
        } catch (javax.swing.text.BadLocationException ex){}
    }

}
