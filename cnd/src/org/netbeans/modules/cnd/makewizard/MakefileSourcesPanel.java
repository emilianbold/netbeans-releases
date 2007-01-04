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
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import org.netbeans.modules.cnd.makewizard.EnterItemsPanel.ErrorInfo;

/**
 * Create the Sources panel in the Makefile wizard.
 */

public class MakefileSourcesPanel extends EnterItemsPanel {

    /** Save the source filter rather than doing repeated lookups */
    private String srcFilter;

    /** Serial version number */
    static final long serialVersionUID = -6961895016031819992L;

    private boolean initialized;

    /** Store the target key */
    private int		key;

    /**
     * Constructor for the Makefile sources panel.
     */
    public MakefileSourcesPanel(MakefileWizard wd) {
	super(wd);
	String subtitle = new String(getString("LBL_MakefileSourcesPanel")); //NOI18N
	setSubTitle(subtitle);
	this.getAccessibleContext().setAccessibleDescription(subtitle);
	initialized = false;
    }


    /** Defer widget creation until the panel needs to be displayed */
    private void create() {
	int flags;
	String msg;
	if (getMakefileData().getMakefileType() == MakefileData.COMPLEX_MAKEFILE_TYPE) {
	    flags = EXPAND_DIRS | MSP_FILTER | DYNAMIC_DEFAULT_BUTTONS | DYNAMIC_LAST_BUTTON | ITEMS_REQUIRED;
	    msg = getString("LBL_SourceNamesComplex"); // NOI18N
	}
	else {
	    flags = EXPAND_DIRS | MSP_FILTER | DYNAMIC_DEFAULT_BUTTONS | ITEMS_REQUIRED;
	    msg = getString("LBL_SourceNamesSimple"); // NOI18N
	}
	create(msg, getString("MNEM_SourceNames").charAt(0), flags); // NOI18N
    }

    /** Set the label for the Source List */
    protected String getListLabel() {
	return getString("LBL_SourceList");				//NOI18N
    }

    /** Set the mnemonic for the Source List */
    protected char getListMnemonic() {
	return getString("MNEM_SourceList").charAt(0);			//NOI18N
    }


    /** Get the title and message for the error dialog */
    protected ErrorInfo getErrorInfo() {
	return new ErrorInfo(getString("DLG_NoFilesError"),		//NOI18N
			getString("MSG_NoFilesMatched"));		//NOI18N
    }


    /** Validate the source files */
    public void validateData(ArrayList msgs, int key) {
	TargetData target = (TargetData) getMakefileData().getTarget(key);

	String[] slist = target.getSourcesList();
	if (slist == null) {
	    warn(msgs, WARN_NO_SRC_FILES, target.getName());
	} else {
	    String cwd = getMakefileData().getBaseDirectory(MakefileData.EXPAND);
	    ArrayList dne = new ArrayList();
	    int absCount = 0;
	    int hdrCount = 0;
	    int i;

	    for (i = 0; i < slist.length; i++) {
		String srcFile = slist[i].toString();

		if (srcFile.startsWith("/")) {				//NOI18N
		    absCount++;
		}

		if (srcFile.endsWith(".h")) {				//NOI18N
		    hdrCount++;
		}

		File file;
		if (srcFile.startsWith(File.separator)) {
		    file = new File(srcFile);
		} else {
		    file = new File(cwd, srcFile);
		}
		if (!file.exists()) {
		    dne.add(new StringBuffer("\t").    			//NOI18N
				append(file.getPath()).append("\n"));	//NOI18N
		}
	    }

	    if (absCount > 0) {
		warn(msgs, WARN_ABSPATH_SRC_COUNT, target.getName(),
				new Integer(absCount).toString());
	    }

	    if (hdrCount > 0) {
		warn(msgs, WARN_HDR_SRC_COUNT, target.getName(),
				new Integer(hdrCount).toString());
	    }

	    if (dne.size() > 0) {
		if (dne.size() < MAX_ITEMS_TO_SHOW) {
		    warn(msgs, WARN_DNE_FILES, target.getName());
		    for (i = 0; i < dne.size(); i++) {
			msgs.add(dne.get(i));
		    }
		    msgs.add(new String("\n"));				//NOI18N
		} else {
		    warn(msgs, WARN_DNE_COUNT, target.getName(),
					new Integer(dne.size()).toString());
		}
	    }
	}
    }


    /** Create the widgets if not initialized. Also initialize the text field */
    public void addNotify() {
	TargetData target = getMakefileData().getCurrentTarget();
	key = target.getKey();

	if (!initialized) {
	    create();
	    srcFilter = getString("DFLT_SourceFilter");			//NOI18N
	    initialized = true;
	}

	// Initialize the text field
	getEntryText().setText(srcFilter);
	
	// Initialize the list. First, remove any from the JList. Then, add any
	// entries from the target into the JList.
	DefaultListModel model = (DefaultListModel) getList().getModel();
	model.removeAllElements();
	String[] slist = target.getSourcesList();
	if (slist != null) {
	    for (int i = 0; i < slist.length; i++) {
		model.addElement(slist[i]);
	    }
	}

	super.addNotify();
    }


    /** Get the data from the panel and update the target */
    public void removeNotify() {
	super.removeNotify();

	TargetData target = getMakefileData().getTarget(key);

	String[] slist = getListItems();
	target.setSourcesList(slist);
    }
}
