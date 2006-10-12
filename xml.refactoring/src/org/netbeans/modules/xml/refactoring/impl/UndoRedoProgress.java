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
