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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
