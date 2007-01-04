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

public class MakefileIncludesPanel extends EnterItemsPanel {

    /** Serial version number */
    static final long serialVersionUID = -3932940292545539665L;

    private int key;
    private boolean initialized;


    /**
     * Constructor for the Makefile sources panel. Remember, most of the panel
     * is inherited from WizardDescriptor.
     */
    MakefileIncludesPanel(MakefileWizard wd) {
	super(wd);
	String subtitle = new String(getString("LBL_MakefileIncludesPanel")); //NOI18N
	setSubTitle(subtitle);
	this.getAccessibleContext().setAccessibleDescription(subtitle);
	initialized = false;
    }


    /** Defer widget creation until the panel needs to be displayed */
    private void create() {
	create(getString("LBL_IncDir"), getString("MNEM_IncDir").charAt(0),	//NOI18N
		    DIR_CHOOSER | DYNAMIC_DEFAULT_BUTTONS);
    }

    /** Set the label for the Source List */
    protected String getListLabel() {
	return getString("LBL_IncludesList");				//NOI18N
    }

    /** Set the mnemonic for the Source List */
    protected char getListMnemonic() {
	return getString("MNEM_IncludesList").charAt(0);			//NOI18N
    }

    /** Validate the include directories */
    public void validateData(ArrayList msgs, int key) {
	TargetData target = (TargetData) getMakefileData().getTarget(key);

	String[] ilist = target.getIncludesList();
	if (ilist == null) {
	    warn(msgs, WARN_NO_INC_DIRS, target.getName());
	} else {
	    String cwd = getMakefileData().getBaseDirectory(MakefileData.EXPAND);
	    ArrayList dne = new ArrayList();
	    ArrayList notDir = new ArrayList();
	    int i;

	    for (i = 0; i < ilist.length; i++) {
		String incDir = ilist[i].toString();

		File dir;
		if (incDir.startsWith(File.separator)) {
		    dir = new File(incDir);
		} else {
		    dir = new File(cwd, incDir);
		}
		if (dir != null) {
		    if (!dir.exists()) {
			dne.add(new StringBuffer("\t").			//NOI18N
				append(dir.getPath()).append("\n"));	//NOI18N
		    } else if (!dir.isDirectory()) {
			notDir.add(new StringBuffer("\t").		//NOI18N
				append(dir.getPath()).append("\n"));	//NOI18N
		    }
		}
	    }

	    if (dne.size() > 0) {
		if (dne.size() < MAX_ITEMS_TO_SHOW) {
		    warn(msgs, WARN_DNE_INCDIR, target.getName());
		    for (i = 0; i < dne.size(); i++) {
			msgs.add(dne.get(i));
		    }
		    msgs.add(new String("\n"));				//NOI18N
		} else {
		    warn(msgs, WARN_DNE_COUNT, target.getName(),
					new Integer(dne.size()).toString());
		}
	    }

	    if (notDir.size() > 0) {
		if (notDir.size() < MAX_ITEMS_TO_SHOW) {
		    warn(msgs, WARN_INC_NOT_DIR, target.getName());
		    for (i = 0; i < notDir.size(); i++) {
			msgs.add(notDir.get(i));
		    }
		    msgs.add(new String("\n"));				//NOI18N
		} else {
		    warn(msgs, WARN_INC_NOT_DIR_COUNT, target.getName(),
					new Integer(notDir.size()).toString());
		}
	    }
	}
    }


    /** Get the title and message for the error dialog */
    protected ErrorInfo getErrorInfo() {
	return new ErrorInfo(getString("DLG_MIP_EmptyRE"),		//NOI18N
			getString("MSG_NoFilesMatched"));		//NOI18N
    }


    /**
     *  Check the input and remove any invalid syntax. If the text starts with
     *  any option other than -I ignore completely and return null.
     *
     *  @param text The raw input as typed by the user
     *  @return	    The validated (and possibly modified) string or null
     */
    protected String validateInput(String text) {

	if (text.startsWith("-I")) {					//NOI18N
	    return text.substring(2);
	} else if (text.charAt(0) == '-') {
	    return null;
	} else {
	    return text;
	}
    }


    /** Create the widgets if first time */
    public void addNotify() {
	TargetData target = getMakefileData().getCurrentTarget();
	key = target.getKey();

	if (!initialized) {
	    create();
	    initialized = true;
	}
	
	// Initialize the list. First, remove any from the JList. Then, add any
	// entries from the target into the JList.
	DefaultListModel model = (DefaultListModel) getList().getModel();
	model.removeAllElements();
	String[] ilist = target.getIncludesList();
	if (ilist != null) {
	    for (int i = 0; i < ilist.length; i++) {
		model.addElement(ilist[i]);
	    }
	}

	super.addNotify();
    }


    /** Get the data from the panel and update the target */
    public void removeNotify() {
	super.removeNotify();

	TargetData target = getMakefileData().getTarget(key);
	target.setIncludesList(getListItems());
    }
}
