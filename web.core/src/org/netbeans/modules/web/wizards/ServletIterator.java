/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

import org.netbeans.api.project.Project;

import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.core.Util;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;


/** A template wizard iterator for new servlets, filters and
 * listeners. 
 *
 * @author radim.kubacki@sun.com
 * @author ana.von.klopp@sun.com
 * @author milan.kuchtiak@sun.com
 */

public class ServletIterator implements TemplateWizard.Iterator {
    
    private static final long serialVersionUID = -4147344271705652643L;

    private static final boolean debug = false; 

    private transient FileType fileType; 
    private transient Evaluator evaluator = null; 
    private transient DeployData deployData = null; 

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wizard;
    private transient ServletPanel servletPanel;
    private transient WizardDescriptor.Panel customPanel;

    private ServletIterator(FileType fileType) { 
	this.fileType = fileType; 
    } 

    public static ServletIterator createServletIterator() { 
	return new ServletIterator(FileType.SERVLET); 
    } 

    public static ServletIterator createFilterIterator() { 
	return new ServletIterator(FileType.FILTER); 
    }

    public void initialize (TemplateWizard wizard) {
        
        this.wizard = wizard;
        index = 0;

	if(fileType.equals(FileType.SERVLET) ||
	   fileType.equals(FileType.FILTER)) {
	    deployData = new ServletData(fileType);
	    evaluator = new TargetEvaluator(fileType, deployData); 
	}
            Project project = Templates.getProject( wizard );
            DataFolder targetFolder=null;
            try {
                targetFolder = wizard.getTargetFolder();
            } catch (IOException ex) {
                targetFolder = DataFolder.findFolder(project.getProjectDirectory());
            }
	    evaluator.setInitialFolder(targetFolder,project); 
        
	if(fileType == FileType.SERVLET) { 
	    panels = new WizardDescriptor.Panel[] {
                //wizard.targetChooser (),
                createPackageChooserPanel(wizard,null),
		ServletPanel.createServletPanel((TargetEvaluator)evaluator, wizard) 
	    };
	}
	else if(fileType == FileType.FILTER) {
            customPanel = new WrapperSelection(wizard);
	    panels = new WizardDescriptor.Panel[] {
                //wizard.targetChooser (),
                createPackageChooserPanel(wizard,customPanel),
		servletPanel=ServletPanel.createServletPanel((TargetEvaluator)evaluator, wizard), 
		ServletPanel.createFilterPanel((TargetEvaluator)evaluator, wizard)
	    };
	}
        
        // Creating steps.
        Object prop = wizard.getProperty ("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = Utilities.createSteps (beforeSteps, panels);
        
        for (int i = 0; i < panels.length; i++) { 
            JComponent jc = (JComponent)panels[i].getComponent ();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = jc.getName ();
            }
	    jc.putClientProperty ("WizardPanel_contentSelectedIndex", // NOI18N
				  new Integer (i)); 
	    jc.putClientProperty ("WizardPanel_contentData", steps); // NOI18N
	}	
    }

    private WizardDescriptor.Panel createPackageChooserPanel(TemplateWizard wizard, WizardDescriptor.Panel customPanel) {
        Project project = Templates.getProject(wizard);
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        if (customPanel==null)
            return JavaTemplates.createPackageChooser(project,sourceGroups);
        else
            return JavaTemplates.createPackageChooser(project,sourceGroups,customPanel);
    }
        
    public Set instantiate(TemplateWizard wizard) throws IOException {

	if(debug) log("::instantiate()"); 
	// Create the target folder. The next piece is independent of
	// the type of file we create, and it should be moved to the
	// evaluator class instead. The exact same process
	// should be used when checking if the directory is valid from
	// the wizard itself. 

	// ------------------------- FROM HERE -------------------------
        
        FileObject dir = Templates.getTargetFolder( wizard );
        DataFolder df = DataFolder.findFolder( dir );
        /*
        ClassPath classPath = ClassPath.getClassPath(fo,ClassPath.SOURCE);
        if (classPath==null || !"src".equals(classPath.findOwnerRoot(fo).getName())) {//NOI18N
            String mes = java.text.MessageFormat.format (
                    NbBundle.getMessage (ServletIterator.class, "TXT_wrongFolderForClass"),
                    new Object [] {FileType.SERVLET.equals(fileType)?"Servlet":"Filter"}); //NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
            org.openide.DialogDisplayer.getDefault().notify(desc);
            return null;
        }
        */
        FileObject template = Templates.getTemplate( wizard );
        if (FileType.FILTER.equals(fileType) && ((WrapperSelection)customPanel).isWrapper()) {
            template = Templates.getTemplate( wizard );
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("AdvancedFilter","java"); //NOI18N
        }
        
        DataObject dTemplate = DataObject.find( template );                
        DataObject dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wizard )  );

	if(debug) log("\topened file"); //NOI18N

	// If the user does not want to add the file to the
	// deployment descriptor, return
	if(!deployData.makeEntry()) { 
	    return Collections.singleton(dobj);
	} 

	TargetEvaluator te = (TargetEvaluator)evaluator; 

	if(debug) log("\tcreate dd entries"); //NOI18N
	
	// Check if the user has entered any configuration data
        /*
	if(index == 0) { 
	    if(debug) log("\tUser finished on first panel"); //NOI18N
            System.out.println("folderName="+wizard.getTargetFolder().getName());
	    if(deployData.getClassName().length() == 0) {
		deployData.setClassName(te.getClassName());
                System.out.println("1");
            }
            
	    if(deployData instanceof ServletData) { 
		if(((ServletData)deployData).getName().length() == 0) 
		    ((ServletData)deployData).setName(te.getFileName());
		if(!((ServletData)deployData).isNameUnique()) 
		    ((ServletData)deployData).setName(te.getClassName()); 
		((ServletData)deployData).parseUrlMappingString("/" + //NOI18N
							    te.getFileName()); 
	    }
	}
        */
	deployData.createDDEntries(); 

	if(debug) log("\tURI param"); //NOI18N

	//If any of the mappings don't contain wild cards, we give it
	//to the servlet as an execution parameter. 
	
	// PENDING - this hangs so commenting out for now... 
        /*
	if(fileType == FileType.SERVLET) { 

	    if(debug) log("\tAttempt to set the execution parameter"); //NOI18N

	    String[] mappings = ((ServletData)deployData).getUrlMappings(); 
	    char[] c; 
	    boolean bad; 

	    for(int i=0; i<mappings.length; ++i) {
		
		bad = false; 

		c = mappings[i].toCharArray(); 
		if(debug) log("\tChecking " + mappings[i]); //NOI18N
		for(int j=0; j<c.length; ++j) { 
		    if(c[j] == '*' || c[j] == '?') { 
			if(debug) log("\t unsuitable");//NOI18N
			bad = true; 
			break; 
		    }
		} 
		if(bad) continue; 
		if(debug) log("\t...suitable");//NOI18N
		try { 
		    ((ServletDataObject)(result.getCookie(ServletDataObject.class))).setURIParameter(mappings[i]); 
		}
		catch(Exception ex) {
		    if(debug) log("\tFailed"); 
		    ex.printStackTrace(); 
		}
		break; 
	    }
	    if(debug) log("\tdone URI param"); //NOI18N
	}*/
        return Collections.singleton(dobj);
    } 

    private void log(String s) { 
	System.out.println("ServletIterator" + s); 
    } 
    

    public void uninitialize (TemplateWizard wizard) {
        this.wizard = null;
        panels = null;
    }

    // --- WizardDescriptor.Iterator METHODS: ---
    
    public String name () {
        return NbBundle.getMessage (ServletIterator.class, "TITLE_x_of_y",
        new Integer (index + 1), new Integer (panels.length));
    }
    
    // If the user has elected to place the file in a regular
    // directory (not a web module) then we don't show the DD info
    // panel. 
    public boolean hasNext () {
	if(debug) log("::hasNext()"); //NOI18N
	if(debug) log("\tindex is " + index); //NOI18N
	return index < panels.length - 1 && deployData.hasDD() && deployData.isAddToDD();
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
    
    // PENDING - Ann suggests updating the available panels based on
    // changes. Here is what should happen: 
    // 1. If target is directory, disable DD panels
    // 2. If target is web module but the user does not want to make a
    //    DD entry, disable second DD panel for Filters. 

    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener (ChangeListener l) {}
    public final void removeChangeListener (ChangeListener l) {}
    // If something changes dynamically (besides moving between panels),
    // e.g. the number of panels changes in response to user input, then
    // uncomment the following and call when needed:
    // fireChangeEvent ();
    /*
    private transient Set listeners = new HashSet (1); // Set<ChangeListener>
    public final void addChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.add (l);
        }
    }
    public final void removeChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.remove (l);
        }
    }
    protected final void fireChangeEvent () {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet (listeners).iterator ();
        }
        ChangeEvent ev = new ChangeEvent (this);
        while (it.hasNext ()) {
            ((ChangeListener) it.next ()).stateChanged (ev);
        }
    }
    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject ();
        listeners = new HashSet (1);
    }
     */

}
