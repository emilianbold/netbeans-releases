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

public final class StdLib{
    // Each flag is a boolean for using/not using a library
    private boolean used;
    private String cmd;
    private String name;
    private char mnemonic;

    /**
     * Constructor
     */
    StdLib(String name, char mnemonic, String cmd) {
	this.name = name;
	this.mnemonic = mnemonic;
	this.cmd = cmd;
	used = false;
    }

    StdLib(StdLib old) {
	this.name = old.getName();
	this.mnemonic = old.getMnemonic();
	this.cmd = old.getCmd();
	this.used = old.isUsed();
    }

    /** Getter and setter for the used flag */
    public boolean isUsed() {
	return used;
    }
    public void setUsed(boolean used) {
	this.used = used;
    }

    /** Getter and setter for name */
    public String getName() {
	return name;
    }
    public void setName(String name) {
	this.name = name;
    }

    /** Getter and setter for cmd */
    public String getCmd() {
	return cmd;
    }
    public void setCmd(String cmd) {
	this.cmd = cmd;
    }

    /** Getter and setter for mnemonic */
    public char getMnemonic() {
	return mnemonic;
    }
    public void setMnemonic(char mnemonic) {
	this.mnemonic = mnemonic;
    }
}

