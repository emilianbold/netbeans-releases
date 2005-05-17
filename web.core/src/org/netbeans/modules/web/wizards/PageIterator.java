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

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.openide.filesystems.FileObject;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.*;
import org.openide.util.NbBundle;

import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.api.java.project.JavaProjectConstants;

import org.netbeans.modules.web.taglib.TLDDataObject;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.netbeans.modules.web.taglib.model.TagFileType;


/** A template wizard iterator (sequence of panels).
 * Used to fill in the second and subsequent panels in the New wizard.
 * Associate this to a template inside a layer using the
 * Sequence of Panels extra property.
 * Create one or more panels from template as needed too.
 *
 * @author  Milan Kuchtiak
 */
public class PageIterator implements TemplateWizard.Iterator {
    public static final java.awt.Dimension PREF_SIZE = new java.awt.Dimension(560,350);

    private static final long serialVersionUID = -7586964579556513549L;
    
    private transient FileType fileType;
    private WizardDescriptor.Panel folderPanel;
    private transient SourceGroup[] sourceGroups;
    
    public static PageIterator createJspIterator() { 
	return new PageIterator(FileType.JSP); 
    } 

    public static PageIterator createTagIterator() { 
	return new PageIterator(FileType.TAG); 
    }
    
    public static PageIterator createTagLibraryIterator() { 
	return new PageIterator(FileType.TAGLIBRARY); 
    }
    
    public static PageIterator createHtmlIterator() { 
	return new PageIterator(FileType.HTML); 
    }
    
    private PageIterator(FileType fileType) { 
	this.fileType = fileType; 
    } 

    // You should define what panels you want to use here:
    protected WizardDescriptor.Panel[] createPanels (Project project) {
        Sources sources = (Sources) project.getLookup().lookup(org.netbeans.api.project.Sources.class);
        if (fileType.equals(FileType.JSP)) {
            sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            if (sourceGroups==null || sourceGroups.length==0)
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            folderPanel=new TargetChooserPanel(project,sourceGroups,fileType);
            return new WizardDescriptor.Panel[] {
                folderPanel
            };
        }
        else if (fileType.equals(FileType.HTML)) {
            sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            if (sourceGroups==null || sourceGroups.length==0)
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            folderPanel=new TargetChooserPanel(project,sourceGroups,fileType);
            return new WizardDescriptor.Panel[] {
                folderPanel
            };
        }
        else if (fileType.equals(FileType.TAG)) {
            sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            if (sourceGroups==null || sourceGroups.length==0)
                sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (sourceGroups==null || sourceGroups.length==0)
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            folderPanel=new TargetChooserPanel(project,sourceGroups,fileType);
            return new WizardDescriptor.Panel[] {
                folderPanel
            };
        }
        else if (fileType.equals(FileType.TAGLIBRARY)) {
            sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            if (sourceGroups==null || sourceGroups.length==0)
                sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (sourceGroups==null || sourceGroups.length==0)
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            folderPanel=new TargetChooserPanel(project,sourceGroups,fileType);
            return new WizardDescriptor.Panel[] {
                folderPanel
            };
        }
        return new WizardDescriptor.Panel[] {
            Templates.createSimpleTargetChooser(project,sourceGroups)
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
       
        org.openide.filesystems.FileObject dir = Templates.getTargetFolder( wiz );
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wiz );
        FileObject templateParent = template.getParent();
        TargetChooserPanel panel = (TargetChooserPanel)folderPanel;
        
        if (FileType.JSP.equals(fileType)) {
            if (panel.isSegment()) {
                if (panel.isXml()) {
                    template = templateParent.getFileObject("JSPFX","jspf"); //NOI18N
                } else {
                    template = templateParent.getFileObject("JSPF","jspf"); //NOI18N
                }
            } else {
                if (panel.isXml()) {
                    template = templateParent.getFileObject("JSPX","jspx"); //NOI18N
                }
            }
        } else if (FileType.TAG.equals(fileType)) {
            if (panel.isSegment()) {
                if (panel.isXml()) {
                    template = templateParent.getFileObject("TagFileFX","tagf"); //NOI18N
                } else {
                    template = templateParent.getFileObject("TagFileF","tagf"); //NOI18N
                }
            } else {
                if (panel.isXml()) {
                    template = templateParent.getFileObject("TagFileX","tagx"); //NOI18N
                }
            }
        } else if (FileType.TAGLIBRARY.equals(fileType)) {
            WebModule wm = WebModule.getWebModule (dir);
            if (wm!=null) {
                String j2eeVersion = wm.getJ2eePlatformVersion();
                if (WebModule.J2EE_13_LEVEL.equals(j2eeVersion)) {
                    template = templateParent.getFileObject("TagLibrary_1_2","tld"); //NOI18N
                }
            }
        }
        DataObject dTemplate = DataObject.find( template );                
        DataObject dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wiz )  );
        if (dobj!=null) {
            if (FileType.TAGLIBRARY.equals(fileType)) { //TLD file 
                TLDDataObject tldDO = (TLDDataObject)dobj;
                Taglib taglib = tldDO.getTaglib();
                taglib.setUri(panel.getUri());
                taglib.setShortName(panel.getPrefix());
                tldDO.write(taglib);
            } else if (FileType.TAG.equals(fileType) && panel.isTldCheckBoxSelected()) { //Write Tag File to TLD 
                FileObject tldFo = panel.getTldFileObject();
                if (tldFo!=null) {
                    if (!tldFo.canWrite()) {
                        String mes = java.text.MessageFormat.format (
                                NbBundle.getMessage (PageIterator.class, "MSG_tldRO"),
                                new Object [] {tldFo.getNameExt()});
                        org.openide.NotifyDescriptor desc = new org.openide.NotifyDescriptor.Message(mes,
                            org.openide.NotifyDescriptor.Message.ERROR_MESSAGE);
                        org.openide.DialogDisplayer.getDefault().notify(desc);
                    } else {
                        TLDDataObject tldDO = (TLDDataObject)DataObject.find(tldFo);
                        Taglib taglib=null;
                        try {
                            taglib = tldDO.getTaglib();
                        } catch (IOException ex) {
                            String mes = java.text.MessageFormat.format (
                                    NbBundle.getMessage (PageIterator.class, "MSG_tldCorrupted"),
                                    new Object [] {tldFo.getNameExt()});
                            org.openide.NotifyDescriptor desc = new org.openide.NotifyDescriptor.Message(mes,
                                org.openide.NotifyDescriptor.Message.ERROR_MESSAGE);
                            org.openide.DialogDisplayer.getDefault().notify(desc);
                        }
                        if (taglib!=null) {
                            TagFileType tag = new TagFileType();
                            tag.setName(panel.getTagName());
                            String packageName=null;
                            for (int i = 0; i < sourceGroups.length && packageName == null; i++) {
                                packageName = org.openide.filesystems.FileUtil.getRelativePath (sourceGroups [i].getRootFolder (), dobj.getPrimaryFile());
                            }
                            tag.setPath("/"+packageName); //NOI18N
                            taglib.addTagFile(tag);
                            try {
                                tldDO.write(taglib);
                            } catch (IOException ex) {
                                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.EXCEPTION,ex);
                            }
                        }
                    }
                }
            }
            
            // Do something with the result, e.g. open it:
            /*
            OpenCookie open = (OpenCookie) dobj.getCookie (OpenCookie.class);
            if (open != null) {
                open.open ();
            }
             */
        }
        return Collections.singleton(dobj);
    }

    // --- The rest probably does not need to be touched. ---
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wiz;

    // You can keep a reference to the TemplateWizard which can
    // provide various kinds of useful information such as
    // the currently selected target name.
    // Also the panels will receive wiz as their "settings" object.
    public void initialize (TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;
        Project project = Templates.getProject( wiz );
        panels = createPanels (project);
        
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
        return NbBundle.getMessage(PageIterator.class, "TITLE_x_of_y",
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
