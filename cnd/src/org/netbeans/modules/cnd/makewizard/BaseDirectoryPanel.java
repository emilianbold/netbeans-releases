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
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.api.utils.IpeUtils;

/**
 * Create the second panel in the Makefile wizard.
 */

public class BaseDirectoryPanel extends DirectoryChooserPanel {

    /** Is the base directory a valid (existing) directory? */
    private boolean baseIsValid;

    /** Serial version number */
    static final long serialVersionUID = -4831717621793094L;

    private boolean initialized;


    /**
     * Constructor for the Directory panel. Remember, most of the panel is
     * inherited from WizardDescriptor.
     */
    public BaseDirectoryPanel(MakefileWizard wd) {
	super(wd);
	String subtitle = getString("LBL_BaseDirectoryPanel"); // NOI18N
	setSubTitle(subtitle);
	this.getAccessibleContext().setAccessibleDescription(subtitle);
	initialized = false;
	baseIsValid = false;
    }


    /** Validate the requested directory. Warn the user if it doesn't exist */
    public void validateData(ArrayList msgs, int key) {
	File file = new File(getMakefileData().getBaseDirectory(MakefileData.EXPAND));

	if (file.exists()) {
	    if (!file.isDirectory()) {
		warn(msgs, WARN_CWD_NOT_DIR, file.getPath());
	    }
	} else {
	    warn(msgs, WARN_CWD_DOES_NOT_EXIST, file.getPath());
	}
    }


    /**
     *  The default validation method. Most panels don't do validation so don't
     *  need to override this.
     */
    public boolean isPanelValid() { 
	return baseIsValid;
    }


    /** Override the defualt and do some validation */
    protected final void onOk() {
	checkit();
    }


    /**
     *  Validate the base directory currently typed into the text field. This method should
     *  not be confused with validateData(), which is called during Makefile generation.
     *  This validation occurs while the panel is posted. The validateData() occurs much
     *  later.
     */
    private void validateCurrentBase() {
	String text;
	JTextField tf = getText();
	File dir = null;

	baseIsValid = false;
	text = IpeUtils.expandPath(tf.getText());
	if (text.length() > 0) {
	    if (text.charAt(0) == File.separatorChar) {
		dir = new File(text);
	    } else {
		dir = new File(".", text);  // NOI18N
	    }
	}

	if (dir != null && !dir.isFile()) {
	    baseIsValid = true;
	    MakefileWizard.getMakefileWizard().updateState();
	}
    }


    /**
     *  Create the panel. Do the superclasss first and then some panel-specific stuff
     *  afterwards.
     */
    private void create() {

	create(getString("LBL_BaseDirectory"),			// NOI18N
			FileChooserPanel.ABSOLUTE_PATH,
			getString("HLP_BaseDirectory"));		// NOI18N

	JTextField tf = getText();
	tf.getDocument().addDocumentListener(new DocumentListener() {

	    public void changedUpdate(DocumentEvent ev) {
		checkit();
	    }

	    public void insertUpdate(DocumentEvent ev) {
		checkit();
	    }

	    public void removeUpdate(DocumentEvent ev) {
		checkit();
	    }
	});

	getLabel().setLabelFor(tf);
	getLabel().setDisplayedMnemonic(
		    getString("MNEM_BaseDirectory").charAt(0));	// NOI18N
    }

    private final void checkit() {
	boolean oldVal = baseIsValid;

	validateCurrentBase();
	if (baseIsValid != oldVal) {
	    MakefileWizard.getMakefileWizard().updateState();
	}
    }

    /** Set initial data in dialog */
    public void addNotify() {
	if (!initialized) {
	    initialized = true;
	    MakefileWizard.getMakefileWizard().initDirPaths();
	    create();
	}
	super.addNotify();


	getText().setText(getMakefileData().getBaseDirectory());
	validateCurrentBase();
    }

    /** Update MakefileData if the data was changed */
    public void removeNotify() {
	super.removeNotify();

	String base = getText().getText();
	if (!base.equals(getMakefileData().getBaseDirectory())) {
	    getMakefileData().setBaseDirectory(base);
	}

	MakefileWizard.getMakefileWizard().initMakefileName();
    }
}
