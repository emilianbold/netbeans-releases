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

package org.netbeans.modules.project.ui;

import java.awt.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.*;

import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.*;

/**
 *
 * @author  tom
 */
public class TemplatesPanel implements WizardDescriptor.Panel {
    
    private ArrayList listeners;
    private TemplatesPanelGUI panel;
    private WarmupJob warmUp;
    private boolean warmUpActive;
    private boolean needsReselect = false;   // WelcomeScreen hack, XXX Delete after WS is redesigned
        
    /** Creates a new instance of TemplatesPanel */
    public TemplatesPanel() {
    }
    
    public void readSettings (Object settings) {      
        TemplateWizard wd = (TemplateWizard) settings;
        wd.putProperty ("WizardPanel_contentSelectedIndex", new Integer (0)); // NOI18N
        wd.putProperty ("WizardPanel_contentData", new String[] { // NOI18N
                NbBundle.getBundle (TemplatesPanel.class).getString ("LBL_TemplatesPanel_Name"), // NOI18N
                NbBundle.getBundle (TemplatesPanel.class).getString ("LBL_TemplatesPanel_Dots")}); // NOI18N
        FileObject templatesFolder = (FileObject) wd.getProperty (TemplatesPanelGUI.TEMPLATES_FOLDER);
        
        // WelcomeScreen hack, XXX Delete after WS is redesigned
        String preselectedCategory = (String)wd.getProperty( "PRESELECT_CATEGORY" );        
        if ( templatesFolder != null && templatesFolder.isFolder() && 
            ( wd.getTemplate() == null || preselectedCategory != null || needsReselect ) ) {
            String selectedCategory = OpenProjectListSettings.getInstance().getLastSelectedProjectCategory ();
            String selectedTemplate = OpenProjectListSettings.getInstance().getLastSelectedProjectType ();
            TemplatesPanelGUI p = (TemplatesPanelGUI) this.getComponent();
            if (isWarmUpActive()) {
                WarmupJob wup = getWarmUp();
                wup.setTemplatesFolder (templatesFolder);
                wup.setSelectedCategory( preselectedCategory != null ? preselectedCategory : selectedCategory );
                wup.setSelectedTemplate( preselectedCategory != null ? null : selectedTemplate);
            }
            else {
                p.setTemplatesFolder(templatesFolder);
                p.setSelectedCategoryByName (preselectedCategory != null ? preselectedCategory : selectedCategory);
                p.setSelectedTemplateByName (preselectedCategory != null ? null : selectedTemplate);
            }

        }
        // bugfix #44792: project wizard title always changes
        ((WizardDescriptor)settings).putProperty ("NewProjectWizard_Title", null); // NOI18N
    }
    
    public void storeSettings (Object settings) {
        TemplateWizard wd = (TemplateWizard) settings;
        
        // WelcomeScreen hack, XXX Delete after WS is redesigned
        String preselectedCategory = (String)wd.getProperty( "PRESELECT_CATEGORY" );

        TemplatesPanelGUI gui = (TemplatesPanelGUI)this.getComponent();
        FileObject fo = gui.getSelectedTemplate();
        if (fo != null) {
            try {
                wd.setTemplate (DataObject.find(fo));
            } catch (DataObjectNotFoundException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        if ( preselectedCategory == null ) {

            String path = gui.getSelectedCategoryName();
            if (path != null) {
                OpenProjectListSettings.getInstance().setLastSelectedProjectCategory(path);
            }
            path = gui.getSelectedTemplateName();
            if (path != null) {
                OpenProjectListSettings.getInstance().setLastSelectedProjectType (path);
            }
            needsReselect = false;
        }
        else {
            needsReselect = true;
        }
    }
    
    public synchronized void addChangeListener(javax.swing.event.ChangeListener l) {
        if (this.listeners == null) {
            this.listeners = new ArrayList ();
        }
        this.listeners.add (l);
    }
    
    public synchronized void removeChangeListener(javax.swing.event.ChangeListener l) {
        if (this.listeners == null) {
            return;
        }
        this.listeners.remove (l);
    }
    
    public boolean isValid() {
        return ((TemplatesPanelGUI)this.getComponent()).getSelectedTemplate() != null;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx( TemplatesPanel.class );
    }
    
    public synchronized Component getComponent() {        
        if (this.panel == null) {
            TemplatesPanelGUI.Builder firer = new Builder();
            this.panel = new TemplatesPanelGUI (firer);
            Utilities.attachInitJob (panel, getWarmUp());
            this.warmUpActive = true;
            this.panel.setName (NbBundle.getBundle (TemplatesPanel.class).getString ("LBL_TemplatesPanel_Name")); // NOI18N
        }
        return this.panel;
    }


    private synchronized WarmupJob getWarmUp () {
        if (this.warmUp == null) {
            this.warmUp = new WarmupJob();
        }
        return this.warmUp;
    }

    private synchronized boolean isWarmUpActive () {
        return warmUpActive;
    }

    private static class CategoriesChildren extends Children.Keys {
        
        private DataFolder root;
                
        public CategoriesChildren (DataFolder folder) {
            this.root = folder;
        }
        
        protected void addNotify () {
            DataObject[] children = root.getChildren();
            setKeys (children);
        }
        
        protected void removeNotify () {
            this.setKeys (new Object[0]);
        }
        
        protected Node[] createNodes(Object key) {
            if (key instanceof DataObject) {
                DataObject dobj = (DataObject) key;
                if (dobj instanceof DataFolder) {
                    DataFolder folder = (DataFolder) dobj;
                    DataObject[] children = folder.getChildren ();
                    int type = children.length == 0 ? 0 : 1;   //Empty folder or File folder
                    for (int i=0; i< children.length; i++) {
                        if (children[i].getPrimaryFile ().isFolder ()) {
                            type = 2;   //Folder folder
                            break;
                        }
                    }
                    if (type == 1) {
                        return new Node[] {
                            new FilterNode (dobj.getNodeDelegate(), Children.LEAF)
                        };
                    }
                    else if (type == 2) {
                        return new Node[] {                        
                            new FilterNode (dobj.getNodeDelegate(), new CategoriesChildren ((DataFolder)dobj))
                        };
                    }
                }
            }
            return new Node[0];
        }                
    }
    
    private static class TemplateChildren extends Children.Keys {
        
        private DataFolder folder;
                
        public TemplateChildren (DataFolder folder) {
            this.folder = folder;
        }
        
        protected void addNotify () {
            this.setKeys (this.folder.getChildren ());
        }
        
        protected void removeNotify () {
            this.setKeys (new Object[0]);
        }
        
        protected Node[] createNodes(Object key) {
            if (key instanceof DataObject) {
                DataObject dobj = (DataObject) key;
                if (dobj.isTemplate()) {
                    return new Node[] {
                        new FilterNode (dobj.getNodeDelegate (), Children.LEAF)
                    };
                }
            }
            return new Node[0];
        }        
        
    }
    
    private class WarmupJob implements AsyncGUIJob {

        private FileObject templatesFolder;
        private String category;
        private String template;

        public void construct () {
            panel.warmUp (this.templatesFolder);
        }
        
        public void finished () {
            Cursor cursor = null;
            try {
                cursor = panel.getCursor();
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                panel.doFinished (this.templatesFolder, this.category, this.template);
            } finally {
                if (cursor != null) {
                    panel.setCursor (cursor);
                }
                synchronized(TemplatesPanel.this) {
                    warmUpActive = false;
                }
            }
        }

        void setTemplatesFolder (FileObject fo) {
            this.templatesFolder = fo;
        }

        void setSelectedCategory (String s) {
            this.category = s;
        }

        void setSelectedTemplate (String s) {
            this.template = s;
        }
    }
    
    private class Builder implements TemplatesPanelGUI.Builder {

        public org.openide.nodes.Children createCategoriesChildren (DataFolder folder) {
            assert folder != null : "Folder cannot be null.";  //NOI18N
            return new CategoriesChildren (folder);
        }

        public org.openide.nodes.Children createTemplatesChildren(DataFolder folder) {
            return new TemplateChildren (folder);
        }

        public char getCategoriesMnemonic() {
            return NbBundle.getMessage(TemplatesPanel.class,"MNE_Categories").charAt(0);
        }

        public String getCategoriesName() {
            return NbBundle.getMessage(TemplatesPanel.class,"CTL_Categories");
        }

        public char getTemplatesMnemonic() {
            return NbBundle.getMessage(TemplatesPanel.class,"MNE_Projects").charAt (0);
        }

        public String getTemplatesName() {
            return NbBundle.getMessage(TemplatesPanel.class,"CTL_Projects");
        }

        public void fireChange() {
            Iterator  it = null;
            synchronized (this) {
                if (listeners == null) {
                    return;
                }
                it = ((ArrayList)listeners.clone()).iterator();
            }
            ChangeEvent event = new ChangeEvent (this);
            while (it.hasNext ()) {
                ((ChangeListener)it.next()).stateChanged(event);
            }
        }

    }
}
