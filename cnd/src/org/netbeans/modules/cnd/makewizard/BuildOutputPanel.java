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
import org.netbeans.modules.cnd.api.utils.IpeUtils;

/**
 * Create the BuildOutputPanel in the MakefileWizard.
 */

public class BuildOutputPanel extends DirectoryChooserPanel {

    /** Serial version number */
    static final long serialVersionUID = 2730227827286861351L;

    /** Store the target key */
    private int		key;

    private boolean initialized;

    /**
     * Constructor for the BuildOutputPanel.
     */
    public BuildOutputPanel(MakefileWizard wd) {
	super(wd);
	String subtitle = getString("LBL_BuildOutputPanel"); //NOI18N
	setSubTitle(subtitle);
	this.getAccessibleContext().setAccessibleDescription(subtitle);
	initialized = false;
    }


    /** Defer widget creation until the panel needs to be displayed */
    private void create() {

	//create(getString("LBL_TargetDirectory"), NAME_ONLY);		//NOI18N
	create(getString("LBL_TargetDirectory"), RELATIVE_PATH);	//NOI18N
	getLabel().setLabelFor(getText());
	getLabel().setDisplayedMnemonic(
		    getString("MNEM_TargetDirectory").charAt(0));	//NOI18N
    }


    /** Validate the output directory */
    public void validateData(ArrayList msgs, int key) {
	TargetData target = (TargetData) getMakefileData().getTarget(key);
	String cwd = getMakefileData().getBaseDirectory(MakefileData.EXPAND);
	String odir = IpeUtils.expandPath(target.getOutputDirectory());
	File outdir = null;
	File outpar = null;
	File cwf = null;

	if (odir.length() > 0 && !odir.equals(cwd)) {
	    if (odir.startsWith(File.separator)) {
		outdir = new File(odir);
	    } else {
		outdir = new File(cwd, odir);
	    }
	    outpar = outdir.getParentFile();
	    cwf = new File(cwd);
	}

	if (outdir != null && !outdir.equals(cwf)) {
	    if (!outdir.exists()) {
		if (outpar == null || !outpar.canWrite()) {
		    warn(msgs, WARN_CANNOT_CREATE_OUTPUT_DIR, outdir.getPath());
		}
	    } else if (!outdir.canWrite()) {
		warn(msgs, WARN_CANNOT_WRITE_TO_OUTPUT_DIR, outdir.getPath());
	    }
	}
    }


    /** Initialize the panel and update the values when displayed */
    public void addNotify() {
	TargetData target = getMakefileData().getCurrentTarget();
	String od = target.getOutputDirectory();
	String real_od = od;
	key = target.getKey();

	if (!initialized) {
	    create();
	    initialized = true;
	}

	if (od.charAt(0) == '$') {
	    od = IpeUtils.expandPath(real_od);
	}
	if (od.charAt(0) == '/' || od.charAt(0) == '~') {
	    getText().setText(real_od);
	} else {
	    //getText().setText(getMakefileData().getBaseDirectory() + File.separator + real_od);
	    getText().setText(IpeUtils.getRelativePath(getMakefileData().getBaseDirectory(), real_od));
	}
	super.addNotify();
    }


    /** Update the MakefileData */
    public void removeNotify() {
	super.removeNotify();

	TargetData target = getMakefileData().getTarget(key);
	String od = getText().getText();
	if (od == null)
	    od = "."; // NOI18N
	od = od.trim();
	if (od.length() > 1)
	    od = IpeUtils.trimpath(od);
	if (od.length() == 0)
	    od = "."; // NOI18N
	target.setOutputDirectory(od); // FIXUP: no trim here ????
    }
}
