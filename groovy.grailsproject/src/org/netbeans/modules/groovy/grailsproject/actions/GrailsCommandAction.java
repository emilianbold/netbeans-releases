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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.Presenter;

public class GrailsCommandAction extends AbstractAction implements Presenter.Popup {

    private final JMenu grailsCommandMenu = new JMenu("Grails");
    private final Project project;
    
    public GrailsCommandAction (Project project){
        this.project = project;
        
        grailsCommandMenu.add(new CreateWarFileAction(project));
        grailsCommandMenu.add(new GrailsTargetAction(project, "Compile", "compile"));
        grailsCommandMenu.add(new GrailsTargetAction(project, "Statistics", "stats"));
        grailsCommandMenu.add(new GrailsTargetAction(project, "Upgrade", "upgrade"));
        
        List<String> cmdlist = getCustomScripts();
        
        if (!cmdlist.isEmpty()){
            grailsCommandMenu.addSeparator();
            
            for (String cmd : cmdlist) {
                grailsCommandMenu.add(new GrailsTargetAction(project, cmd, cmd));
            }
        }
        
    }
    
    List<String> getCustomScripts(){
        List<String> cmdlist = new ArrayList<String>();
        
        FileObject prjDir = project.getProjectDirectory();
        assert prjDir != null;
        
        // we have to be a little bit more forgiving,
        // if there is no scripts subdir. See # 133036
        FileObject scriptsDir = prjDir.getFileObject("scripts");
        
        if (scriptsDir == null)
            return cmdlist;
        
        for (Enumeration e = scriptsDir.getChildren(false); e.hasMoreElements();) {
                    FileObject fo = (FileObject) e.nextElement();
                    if (fo != null) {
                        cmdlist.add(fo.getName());
                    }
        }
        
        return cmdlist;
    }
    
    
    
    public void actionPerformed(ActionEvent e) {
        return;
    }

    public JMenuItem getPopupPresenter() {
        
        return grailsCommandMenu;
    }

}
