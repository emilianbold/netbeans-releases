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

package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * Wizard to create a new J2SE project.
 * @author Jesse Glick
 */
public class NewJ2SEProjectWizardIterator implements WizardDescriptor.InstantiatingIterator {

    static final int TYPE_APP = 0;
    static final int TYPE_LIB = 1;
    static final int TYPE_EXT = 2;

    private static final long serialVersionUID = 1L;
    
    private int type;
    
    /** Create a new wizard iterator. */
    public NewJ2SEProjectWizardIterator() {
        this(TYPE_APP);
    }
    
    public NewJ2SEProjectWizardIterator(int type) {
        this.type = type;
    }
        
    public static NewJ2SEProjectWizardIterator library() {
        return new NewJ2SEProjectWizardIterator( TYPE_LIB );
    }
    
    public static NewJ2SEProjectWizardIterator trada() {
        System.out.println("Trada");
        return new NewJ2SEProjectWizardIterator();
    }

    public static NewJ2SEProjectWizardIterator existing () {
        return new NewJ2SEProjectWizardIterator( TYPE_EXT );
    }

    private WizardDescriptor.Panel[] createPanels () {
        WizardDescriptor.Panel[] result = null;
        if (this.type == TYPE_EXT) {
            result =  new WizardDescriptor.Panel[] {
                new PanelConfigureProject( this.type ),
                new PanelSourceFolders.Panel ()
            };
        }
        else {
            result = new WizardDescriptor.Panel[] {
                new PanelConfigureProject( this.type )
            };
        }
        return result;
    }
    
    private String[] createSteps() {
        String[] result = null;
        if (this.type == TYPE_EXT) {
            result = new String[] {
                "Configure Project", // XXX I18N
                "Set Source Root",         // XXX I18N
            };
        }
        else {
            result = new String[] {
                "Configure Project", // XXX I18N
            };
        }
        return result;
    }
    
    
    public Set/*<FileObject>*/ instantiate () throws IOException {
        Set resultSet = new HashSet ();
        File dirF = (File)wiz.getProperty("projdir");        //NOI18N
        String codename = (String)wiz.getProperty("codename");        //NOI18N
        String displayName = (String)wiz.getProperty("displayName");        //NOI18N
        String mainClass = (String)wiz.getProperty("mainClass");        //NOI18N
        if (this.type == TYPE_EXT) {
            File sourceFolder = (File)wiz.getProperty("sourceRoot");        //NOI18N
            File testFolder = (File)wiz.getProperty("testRoot");            //NOI18N
            if (testFolder != null && !testFolder.exists()) {
                testFolder.mkdirs();
            }
            J2SEProjectGenerator.createProject(dirF, codename, displayName, sourceFolder, testFolder );
        }
        else {
            AntProjectHelper h = J2SEProjectGenerator.createProject (dirF, codename, displayName, mainClass );
            if (mainClass != null && mainClass.length () > 0) {
                try {
                    //String sourceRoot = "src"; //(String)j2seProperties.get (J2SEProjectProperties.SRC_DIR);
                    FileObject sourcesRoot = h.getProjectDirectory ().getFileObject ("src");        //NOI18N
                    FileObject mainClassFo = getMainClassFO (sourcesRoot, mainClass);
                    assert mainClassFo != null : "sourcesRoot: " + sourcesRoot + ", mainClass: " + mainClass;        //NOI18N
                    // Returning FileObject of main class, will be called its preferred action
                    resultSet.add (mainClassFo);
                } catch (Exception x) {
                    // XXX
                    x.printStackTrace();
                }
            }
        }
        FileObject dir = FileUtil.toFileObject(dirF);
        Project p = ProjectManager.getDefault().findProject(dir);
        
        // Returning FileObject of project diretory. 
        // Project will be open and set as main
        resultSet.add (dir);
        return resultSet;
    }
    
        
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
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
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return "" + (index + 1) + " of " + panels.length; // XXX I18N
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    // helper methods, finds mainclass's FileObject
    private FileObject getMainClassFO (FileObject sourcesRoot, String mainClass) {
        // replace '.' with '/'
        mainClass = mainClass.replace ('.', '/'); // NOI18N
        
        // ignore unvalid mainClass ???
        
        return sourcesRoot.getFileObject (mainClass, "java"); // NOI18N
    }
}
