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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.ui.utils.PathPanel;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.openide.util.NbBundle;


public class AddExternalItemAction extends AbstractAction {
    private Project project;

    public AddExternalItemAction(Project project) {
	putValue(NAME, NbBundle.getBundle(getClass()).getString("CTL_AddExternalItem")); //NOI18N
	this.project = project;
    }

    public void actionPerformed(ActionEvent e) {
	ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class );
	ConfigurationDescriptor projectDescriptor = pdp.getConfigurationDescriptor();
	MakeConfigurationDescriptor makeProjectDescriptor = (MakeConfigurationDescriptor)projectDescriptor;

	String seed = null;
	if (FileChooser.getCurrectChooserFile() != null) {
	    seed = FileChooser.getCurrectChooserFile().getPath();
	}
	if (seed == null) {
	    seed = makeProjectDescriptor.getBaseDir();
	}
	FileChooser fileChooser = new FileChooser(NbBundle.getBundle(getClass()).getString("LBL_FileChooserTitle"), NbBundle.getBundle(getClass()).getString("LBL_SelectButton"), FileChooser.FILES_AND_DIRECTORIES, null, seed, true);
	PathPanel pathPanel = new PathPanel();
	fileChooser.setAccessory(pathPanel);
	fileChooser.setMultiSelectionEnabled(true);
	int ret = fileChooser.showOpenDialog(null); // FIXUP
	if (ret == FileChooser.CANCEL_OPTION)
	    return;

	File[] files = fileChooser.getSelectedFiles();
	Item[] items = new Item[files.length];
	for (int i = 0; i < files.length; i++) {
	    String itemPath;
	    if (pathPanel.getMode() == PathPanel.REL_OR_ABS)
		itemPath = IpeUtils.toAbsoluteOrRelativePath(makeProjectDescriptor.getBaseDir(), files[i].getPath());
	    else if (pathPanel.getMode() == PathPanel.REL)
		itemPath = IpeUtils.toRelativePath(makeProjectDescriptor.getBaseDir(), files[i].getPath());
	    else
		itemPath = files[i].getPath();
	    itemPath = FilePathAdaptor.mapToRemote(itemPath);
	    itemPath = FilePathAdaptor.normalize(itemPath);
            Item item = makeProjectDescriptor.getExternalItemFolder().findItemByPath(itemPath);
	    if (item != null) {
                items[i] = item;
	    }
            else {
                makeProjectDescriptor.getExternalItemFolder().addItem(items[i] = new Item(itemPath));
            }
	}
	MakeLogicalViewProvider.setVisible(project, items);
    }
}
