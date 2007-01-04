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

package org.netbeans.modules.cnd.execution;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.execution41.org.openide.loaders.ExecutionSupport;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/** Support for execution of a class file. Looks for the class with
* the same base name as the primary file, locates a main method
* in it, and starts it.
*
*/
public final class BinaryExecSupport extends ExecutionSupport {
    private static final String PROP_RUN_DIRECTORY = "rundirectory"; // NOI18N
    private PropertySupport rundirectoryProperty = null;
  
    /** new BinaryExecSupport */
    public BinaryExecSupport(Entry entry) {
	super(entry);
    }
  
    public void addProperties (Sheet.Set set) {
	set.put(createRunDirectoryProperty());
    }

    /**
     *  Create the run directory property.
     *
     *  @return The run directory property
     */
    private PropertySupport createRunDirectoryProperty() {

	return new PropertySupport.ReadWrite(PROP_RUN_DIRECTORY, String.class,
		    getString("PROP_RUN_DIRECTORY"), // NOI18N
		    getString("HINT_RUN_DIRECTORY")) { // NOI18N

	    public Object getValue() {
		return getRunDirectory();
	    }
	    public void setValue(Object val) {
		setRunDirectory((String) val);
	    }
	    public boolean supportsDefaultValue() {
		return true;
	    }
	    public void restoreDefaultValue() {
		setValue(null);
	    }
	    public boolean canWrite() {
		return getEntry().getFile().getParent().canWrite();
	    }
	};
    }

    /**
     *  Get the the run directory, the directory to invoke make from.
     *
     *  @return the run directory
     */
    public String getRunDirectory() {
	String dir = (String) getEntry().getFile().getAttribute(PROP_RUN_DIRECTORY);

	if (dir == null) {
	    dir = "."; //NOI18N
	    setRunDirectory(dir);
	}

	return dir;
    }

    /**
     *  Set the run directory
     *
     *  @param target the run directory
     */
    public void setRunDirectory(String dir) {
	try {
	    getEntry().getFile().setAttribute(PROP_RUN_DIRECTORY, dir);
	} catch (IOException ex) {
	    String msg = MessageFormat.format("INTERNAL ERROR: Cannot set run directory", // NOI18N
		    new Object[] { FileUtil.toFile(getEntry().getFile()).getPath() });

	    if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
		ex.printStackTrace();
	    }
	}
    }

    private ResourceBundle bundle = null;
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(BinaryExecSupport.class);
	}
	return bundle.getString(s);
    }

}
