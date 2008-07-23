/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
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
        this.project = project;
        this.target = target;
        this.it = it;
        it.initialize(this);
        setPanelsAndSettings(it, this);
        setTitle(NbBundle.getMessage(ShortcutWizard.class, "TITLE_wizard"));
        putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
        putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
        putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
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
        final FileObject actionsBuild = FileUtil.createFolder(Repository.getDefault().getDefaultFileSystem().getRoot(), "Actions/Build"); // NOI18N
        actionsBuild.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                // First create Actions/Build/*.xml, so it appears in action pool:
                String fname = FileUtil.findFreeFileName(actionsBuild, getTargetBaseName(), "xml") + ".xml"; // NOI18N
                FileObject shortcut = actionsBuild.createData(fname); // NOI18N
                FileLock lock = shortcut.lock();
                try {
                    OutputStream os = shortcut.getOutputStream(lock);
                    try {
                        os.write(getContents().getBytes("UTF-8")); // NOI18N
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
                // Now create *.shadow links to it from appropriate places in GUI:
                DataObject shortcutFO = DataObject.find(shortcut);
                if (it.showing(PROP_SHOW_MENU)) {
                    DataShadow.create((DataFolder) getProperty(PROP_FOLDER_MENU), shortcutFO);
                }
                if (it.showing(PROP_SHOW_TOOL)) {
                    DataShadow.create((DataFolder) getProperty(PROP_FOLDER_TOOL), shortcutFO);
                }
                if (it.showing(PROP_SHOW_KEYB)) {
                    FileObject currentKeymapDir = Repository.getDefault().getDefaultFileSystem().findResource("Shortcuts"); // NOI18N
                    String stroke = Utilities.keyToString((KeyStroke) getProperty(PROP_STROKE));
                    DataShadow.create(DataFolder.findFolder(currentKeymapDir), stroke, shortcutFO);
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
