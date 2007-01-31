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

package  org.netbeans.modules.cnd.makewizard;

import java.io.File;
import javax.swing.JFileChooser;
import org.netbeans.modules.cnd.api.utils.IpeFileSystemView;

/**
 * The DirectoryChooserPanel extends the FileChooserPanel but makes the
 * JFileChooser a directory chooser rather than a file chooser.
 */

public abstract class DirectoryChooserPanel extends FileChooserPanel {

    /** Serial version number */
    static final long serialVersionUID = -8477214279063965753L;

    public DirectoryChooserPanel(MakefileWizard wd) {
	super(wd);
	init();
	fc = new JFileChooser();
	fc.setApproveButtonText(getString("BTN_Approve"));		// NOI18N
	fc.setDialogTitle(getString("TITLE_DirChooser"));		// NOI18N
	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	fc.setFileSystemView(new IpeFileSystemView(fc.getFileSystemView()));
	fc.setCurrentDirectory(
			new File(System.getProperty("user.dir")));	// NOI18N
    }
}
