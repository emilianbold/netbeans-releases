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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.grailsproject.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.netbeans.api.project.Project;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import javax.swing.filechooser.FileFilter;

public class AddLibraryAction extends AbstractAction {

    Project prj;
    Logger LOG = Logger.getLogger(AddLibraryAction.class.getName());
            
    public AddLibraryAction (Project prj, String desc){
        super (desc);
        this.prj = prj;
    }

    @Override
    public boolean isEnabled(){
            return true;
        }
            
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new LibFilter());

        if (chooser.showDialog(null, "choose lib") == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            assert file != null;

            FileObject fo = FileUtil.toFileObject(file);
            assert fo != null;

            FileObject target = prj.getProjectDirectory().getFileObject("lib");
            assert target != null;

            try {
                FileUtil.copyFile(fo, target, fo.getName());
            // LOG.log(Level.WARNING, "Added file to project: " + file.getName());
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Problem adding file: " + file.getName());
            }
        }
    }
    
    class LibFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            } else if ( f.getName().endsWith(".jar")) {
                return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "Java libraries";
        }
    }
}


