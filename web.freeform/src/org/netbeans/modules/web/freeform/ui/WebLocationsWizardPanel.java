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

package org.netbeans.modules.web.freeform.ui;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.netbeans.modules.java.freeform.spi.support.NewJavaFreeformProjectSupport;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author  Radko Najman
 */
public class WebLocationsWizardPanel implements WizardDescriptor.Panel {

    private WebLocationsPanel component;
    private WizardDescriptor wizardDescriptor;
    private File baseFolder;

    public WebLocationsWizardPanel() {
        getComponent().setName(NbBundle.getMessage(NewWebFreeformProjectWizardIterator.class, "TXT_NewWebFreeformProjectWizardIterator_WebSources")); // NOI18N
    }

    public Component getComponent() {
        if (component == null) {
            component = new WebLocationsPanel();
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx( WebLocationsWizardPanel.class );
    }

    public boolean isValid() {
        getComponent();
        return true;
    }

    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); // NOI18N

        //guess webapps well-known locations and preset them
        File baseFolder = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_PROJECT_LOCATION);
        File nbProjectFolder = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_PROJECT_FOLDER);
        final String webPages;
        final String srcPackages;
        if(baseFolder.equals(this.baseFolder)) {
            webPages = component.getWebPagesLocation().getAbsolutePath();
            srcPackages = component.getSrcPackagesLocation().getAbsolutePath();
        } else {
            this.baseFolder = baseFolder;
            FileObject fo = FileUtil.toFileObject(baseFolder);
            if (fo != null) {
                webPages = guessDocBase(fo);
                srcPackages = guessJavaRoot(fo);
            } else {
                webPages = ""; // NOI18N
                srcPackages = ""; // NOI18N
            }
        }
        component.setFolders(baseFolder, nbProjectFolder);
        component.setWebPages(webPages);
        component.setSrcPackages(srcPackages);
    }

    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        wizardDescriptor.putProperty(NewWebFreeformProjectWizardIterator.PROP_WEB_WEBMODULES, component.getWebModules());
        
        List l = component.getJavaSrcFolder();
        wizardDescriptor.putProperty(NewJavaFreeformProjectSupport.PROP_EXTRA_JAVA_SOURCE_FOLDERS, l);
        
        wizardDescriptor.putProperty(NewWebFreeformProjectWizardIterator.PROP_WEB_SOURCE_FOLDERS, component.getWebSrcFolder());
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
    }

    private String guessDocBase (FileObject dir) {
        Enumeration ch = dir.getChildren (true);
        while (ch.hasMoreElements ()) {
            FileObject f = (FileObject) ch.nextElement ();
            if (f.isFolder () && f.getName ().equals ("WEB-INF")) { // NOI18N
                final FileObject webXmlFleObject = f.getFileObject ("web.xml"); // NOI18N
                if (webXmlFleObject!= null && webXmlFleObject.isData ()) { 
                    return FileUtil.toFile(f.getParent()).getAbsolutePath();
                }
            }
        }
        return ""; // NOI18N
    }

    private String guessJavaRoot (FileObject dir) {
        Enumeration ch = dir.getChildren (true);
        try {
            while (ch.hasMoreElements ()) {
                FileObject f = (FileObject) ch.nextElement ();
                if (f.getExt ().equals ("java")) { // NOI18N
                    String pckg = guessPackageName (f);
                    String pkgPath = f.getParent ().getPath ();
                    if (pckg != null && pkgPath.endsWith (pckg.replace ('.', '/'))) {
                        String rootName = pkgPath.substring (0, pkgPath.length () - pckg.length ());
                        return FileUtil.toFile(f.getFileSystem().findResource(rootName)).getAbsolutePath();
                    }
                }
            }
        } catch (FileStateInvalidException fsie) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, fsie);
        }
        return ""; // NOI18N
    }

    private String guessPackageName(FileObject f) {
        java.io.Reader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(f.getInputStream (), "utf-8")); //NOI18N
            boolean noPackage = false;
            for (;;) {
                String line = ((BufferedReader) r).readLine();
                if (line == null) {
                    if (noPackage)
                        return "";
                    else
                        break;
                }
                line = line.trim();
                //try to find package
                if (line.trim().startsWith("package")) { // NOI18N
                    int idx = line.indexOf(";");  // NOI18N
                    if (idx >= 0)
                        return line.substring("package".length(), idx).trim(); // NOI18N
                }
                //an easy check if it is class
                if (line.indexOf("class") != -1)
                    noPackage = true;
            }
        } catch (java.io.IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        } finally {
            try {
                if (r != null)
                    r.close ();
            } catch (java.io.IOException ioe) {
                // ignore this
            }
        }
        
        return null;
    }
}
