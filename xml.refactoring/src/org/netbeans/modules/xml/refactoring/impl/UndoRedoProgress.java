/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

/*
 * UndoRedoProgress.java
 *
 * Created on June 16, 2006, 5:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.refactoring.impl;

import java.awt.BorderLayout;
import java.awt.Dialog;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Jeri Lockhart
 */
public class UndoRedoProgress {
    private ProgressHandle handle;
    private Dialog d;
    
    /** Creates a new instance of UndoRedoProgress */
    public UndoRedoProgress() {
    }
    
    public void start() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final String lab = NbBundle.getMessage(UndoRedoProgress.class, "LBL_RefactorProgressLabel");
                handle = ProgressHandleFactory.createHandle(lab);
                JComponent progress = ProgressHandleFactory.createProgressComponent(handle);
                JPanel component = new JPanel();
                component.setLayout(new BorderLayout());
                component.setBorder(new EmptyBorder(12,12,11,11));
                JLabel label = new JLabel(lab);
                label.setBorder(new EmptyBorder(0, 0, 6, 0));
                component.add(label, BorderLayout.NORTH);
                component.add(progress, BorderLayout.CENTER);
                DialogDescriptor desc = new DialogDescriptor(component, NbBundle.getMessage(UndoRedoProgress.class, "LBL_RefactoringInProgress"), true, new Object[]{}, null, 0, null, null);
                desc.setLeaf(true);
                d = DialogDisplayer.getDefault().createDialog(desc);
                ((JDialog) d).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                
                
                handle.start();
                handle.switchToIndeterminate();
                
                d.setVisible(true);
            }
        });
    }
   
    
    public void stop() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                handle.finish();
                d.setVisible(false);
            }
        });
    }
    
}
