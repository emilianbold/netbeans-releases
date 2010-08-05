/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.encoder.ui.basic;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JDialog;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.windows.WindowManager;

/**
 * A utility for handling progress information
 *
 * @author Jun Xu
 */
public class ProgressDialog {

    private final ProgressHandle mProgHandle;
    private final JDialog mDialog;
    private final JComponent mProgBar;
    
    private ProgressDialog(ProgressHandle handle, JDialog dialog, JComponent progBar) {
        mProgHandle = handle;
        mDialog = dialog;
        mProgBar = progBar;
    }
    
    public static ProgressDialog newInstance(String title) {
        ProgressHandle progHandle =
                ProgressHandleFactory.createHandle(title);
        JComponent progBar = ProgressHandleFactory.createProgressComponent(progHandle);
        progBar.setSize(new Dimension(400, 20));
        final JDialog dialog = new JDialog(
                WindowManager.getDefault().getMainWindow(), title, true);
        dialog.setLayout(new BorderLayout());
        dialog.add(progBar, BorderLayout.CENTER);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(400, 70);
        dialog.setLocationRelativeTo(null);
        return new ProgressDialog(progHandle, dialog, progBar);
    }
    
    public ProgressHandle getHandle() {
        return mProgHandle;
    }
    
    public JDialog getDialog() {
        return mDialog;
    }
    
    public JComponent getBar() {
        return mProgBar;
    }
}

