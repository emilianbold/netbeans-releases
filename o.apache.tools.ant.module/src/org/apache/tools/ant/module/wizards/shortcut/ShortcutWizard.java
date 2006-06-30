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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.wizards.shortcut;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.KeyStroke;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The shortcut wizard itself.
 * @author Jesse Glick
 */
public final class ShortcutWizard extends WizardDescriptor {
    
    /**
     * Show the shortcut wizard for a given Ant target.
     * @param project the Ant script to make a target to
     * @param target the particular target in it
     */
    public static void show(AntProjectCookie project, Element target) {
        final ShortcutWizard wiz = new ShortcutWizard(project, target, new ShortcutIterator());
        DialogDisplayer.getDefault().createDialog(wiz).setVisible(true);
        if (wiz.getValue().equals(WizardDescriptor.FINISH_OPTION)) {
            try {
                wiz.finish();
            } catch (IOException ioe) {
                AntModule.err.notify(ioe);
            }
        }
    }
    
    // Attributes stored on the template wizard:
    
    /** type String */
    private static final String PROP_CONTENTS = "wizdata.contents"; // NOI18N
    /** type String */
    static final String PROP_DISPLAY_NAME = "wizdata.displayName"; // NOI18N
    /** type Boolean */
    static final String PROP_SHOW_CUST = "wizdata.show.cust"; // NOI18N
    /** type Boolean */
    static final String PROP_SHOW_MENU = "wizdata.show.menu"; // NOI18N
    /** type Boolean */
    static final String PROP_SHOW_TOOL = "wizdata.show.tool"; // NOI18N
    /** type Boolean */
    static final String PROP_SHOW_KEYB = "wizdata.show.keyb"; // NOI18N
    /** type DataFolder */
    static final String PROP_FOLDER_MENU = "wizdata.folder.menu"; // NOI18N
    /** type DataFolder */
    static final String PROP_FOLDER_TOOL = "wizdata.folder.tool"; // NOI18N
    /** type KeyStroke */
    static final String PROP_STROKE = "wizdata.stroke"; // NOI18N
    
    private final AntProjectCookie project;
    private final Element target;
    private final ShortcutIterator it;

    ShortcutWizard(AntProjectCookie project, Element target, ShortcutIterator it) {
        super(it);
        this.project = project;
        this.target = target;
        this.it = it;
        it.initialize(this);
        setTitle(NbBundle.getMessage(ShortcutWizard.class, "TITLE_wizard"));
        putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
        putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
        putProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
        String desc = target.getAttribute("description"); // NOI18n
        putProperty(PROP_DISPLAY_NAME, desc);
        // XXX deal with toolbar short desc somehow: #39985
        // Need to have another field in toolbar panel, and also patch AntActionInstance
        // to respond to Action.SHORT_DESCRIPTION, presumably as the <description>.
    }
    
    /**
     * Get the current XML contents of the shortcut.
     */
    String getContents() {
        String c = (String)getProperty(PROP_CONTENTS);
        if (c == null) {
            c = generateContents();
            putContents(c);
        }
        return c;
    }
    
    /**
     * Put the XML contents.
     */
    void putContents(String c) {
        putProperty(PROP_CONTENTS, c);
    }
    
    /**
     * Create XML contents of the shortcut to be generated, based on current data.
     */
    private String generateContents() {
        try {
            Document doc = XMLUtil.createDocument("project", null, null, null); // NOI18N
            Element pel = doc.getDocumentElement();
            String displayName = (String)getProperty(PROP_DISPLAY_NAME);
            if (displayName != null && displayName.length() > 0) {
                pel.setAttribute("name", displayName); // NOI18N
            }
            pel.setAttribute("default", "run"); // NOI18N
            Element tel = doc.createElement("target"); // NOI18N
            tel.setAttribute("name", "run"); // NOI18N
            Element ael = doc.createElement("ant"); // NOI18N
            ael.setAttribute("antfile", project.getFile().getAbsolutePath()); // NOI18N
            // #34802: let the child project decide on the basedir:
            ael.setAttribute("inheritall", "false"); // NOI18N
            ael.setAttribute("target", target.getAttribute("name")); // NOI18N
            tel.appendChild(ael);
            pel.appendChild(tel);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
            XMLUtil.write(doc, baos, "UTF-8"); // NOI18N
            return baos.toString("UTF-8"); // NOI18N
        } catch (IOException e) {
            AntModule.err.notify(e);
            return ""; // NOI18N
        }
    }

    void finish() throws IOException {
        if (it.showing(PROP_SHOW_MENU)) {
            create((DataFolder) getProperty(PROP_FOLDER_MENU), null);
        }
        if (it.showing(PROP_SHOW_TOOL)) {
            create((DataFolder) getProperty(PROP_FOLDER_TOOL), null);
        }
        if (it.showing(PROP_SHOW_KEYB)) {
            FileObject shortcutsFolder = Repository.getDefault().getDefaultFileSystem().findResource("Shortcuts"); // NOI18N
            KeyStroke stroke = (KeyStroke) getProperty(PROP_STROKE);
            create(DataFolder.findFolder(shortcutsFolder), Utilities.keyToString(stroke));
        }
    }
    
    private void create(DataFolder f, String name) throws IOException {
        assert f != null;
        final String fname;
        if (name != null) {
            fname = name + ".xml"; // NOI18N
        } else {
            fname = FileUtil.findFreeFileName(f.getPrimaryFile(), getTargetBaseName(), "xml") + ".xml"; // NOI18N
        }
        final String contents = getContents();
        final FileObject folder = f.getPrimaryFile();
        final FileObject[] shortcut = new FileObject[1];
        folder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                shortcut[0] = folder.createData(fname); // NOI18N
                FileLock lock = shortcut[0].lock();
                try {
                    OutputStream os = shortcut[0].getOutputStream(lock);
                    try {
                        os.write(contents.getBytes("UTF-8")); // NOI18N
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        });
    }
    
    String getTargetBaseName() {
        String projname = ""; // NOI18N
        Document doc = project.getDocument();
        if (doc != null) {
            projname = doc.getDocumentElement().getAttribute("name"); // NOI18N
        }
        return (projname + '-' + target.getAttribute("name")).replaceAll("[^a-zA-Z0-9_-]", "-"); // NOI18N
    }

}
