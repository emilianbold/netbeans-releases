/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * "DbgProfile.java"
 * Common code for DbxProfile and GdbProfile:
 *   - deal with common properties of dbx and gdb, like pathmap, signal,
 *     exceptions, output redirection, host etc.
 */

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSetOwner;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import java.beans.PropertyChangeSupport;

public abstract class DbgProfile extends ProfileSupport implements OptionSetOwner {

    public static final String PROP_OPTIONS = "options";        // NOI18N

    public static final String PROP_INTERCEPTLIST = "interceptlist";// NOI18N
    public static final String PROP_SIGNALS = "signals";	// NOI18N
    public static final String PROP_PATHMAP = "pathmap";	// NOI18N

    protected OptionSet options;
    protected boolean savedBuildFirst;
    protected boolean buildFirstOverriden;

    /**
     * Constructor
     * Don't call this directly. It will get called when creating
     * ...cnd.execution.profiles.Profile().
     */
    public DbgProfile() {
    }

    protected DbgProfile(PropertyChangeSupport pcs) {
	super(pcs);
    }

    /**
     * Initializes the object to default values
     */
    @Override
    public void initialize() {
    }

    @Override
    public OptionSet getOptions() {
	return options;
    }

    protected final void notifyOptionsChange() {
	// clones don't have a pcs
	if (pcs != null)
	    pcs.firePropertyChange(PROP_OPTIONS, null, null);
	needSave = true;
    }

    protected Signals signals;
    protected Exceptions exceptions;
    protected Pathmap pathmap;

    public Pathmap pathmap() {
	return pathmap;
    }

    public Exceptions exceptions() {
	return exceptions;
    }

    public Signals signals() {
	return signals;
    }

    protected String redirection;

    /**
     * @return Return the redirection in effect for the program.
     */
    public String getRedirection() {
	return redirection;
    }
    
    /**
     * Set the output redirection for this program
     * @param infile Input file
     * @param outfile Output file
     * @param append Append to outputfile?
     */
    public void setRedirection(String infile, String outfile,
			       boolean append) {
	StringBuilder sb = new StringBuilder();
	boolean in = (infile != null && infile.length() != 0);
	if (in) {
	    sb.append("< ");		// NOI18N
	    sb.append(infile);
	}
	if (outfile != null && outfile.length() != 0) {
	    if (in) {
		sb.append(" ");		// NOI18N
	    }
	    if (append) {
		sb.append(">> ");	// NOI18N
	    } else {
		sb.append("> ");	// NOI18N
	    }
	    sb.append(outfile);
	}

	if (sb.length() != 0) {
	    redirection = sb.toString();
	    needSave = true;
	}
    }

    // Host
    protected String host;

    public String getDefaultHost() {
	return "localhost"; // NOI18N
    }

    public String getHost() {
	return host;
    }

    public void setHost(String host) {
	this.host = host;
	needSave = true;
    }

    public boolean isDefaultHost() {
	return getHost().equals(getDefaultHost());
    }

    public void setBuildFirstOverriden(boolean buildFirstOverriden) {
	this.buildFirstOverriden = buildFirstOverriden;
	needSave = true;
    }

    public boolean isBuildFirstOverriden() {
	return buildFirstOverriden;
    }

    public boolean isSavedBuildFirst() {
	return savedBuildFirst;
    }

    public void setSavedBuildFirst(boolean savedBuildFirst) {
	this.savedBuildFirst = savedBuildFirst;
	needSave = true;
    }
    
    private String getDebugCommand() {
        return DebuggerOption.DEBUG_COMMAND.getCurrValue(options);
    }

    // START: Copied from RunProfile
    private int getArgIndex() {
        return getDebugCommand().indexOf(" "); // NOI18N // FIXUP <=== need a better check
    }

    public String getExecutable() {
        int argIndex = getArgIndex();
        if (argIndex > 0) {
            return getDebugCommand().substring(0, argIndex);
        } else {
            return getDebugCommand();
        }
    }

    public String getArgsFlat() {
        int argIndex = getArgIndex();
        if (argIndex > 0) {
            return getDebugCommand().substring(argIndex+1);
        } else {
            return "";
        }
    }
    // END: Copied from RunProfile
    
    public String getDebugDir() {
        return DebuggerOption.DEBUG_DIR.getCurrValue(options);
    }
    
    public void setDebugDir(String value) {
        DebuggerOption.DEBUG_DIR.setCurrValue(options, value);
    }
}
