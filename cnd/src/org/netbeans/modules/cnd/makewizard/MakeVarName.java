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

    /**
     *  We need to emit lots of make variables of the form "$(foo_bar)", where
     *  "foo" is the related to the variable we want to creat and "bar" is
     *  related to the current target. This class is a helper class which
     *  creates these names for us. To cut down on object creation its intended
     *  to be reused with different variables and targets.
     */

public class MakeVarName {
	private String targetName;		// this gets appended to name
	private StringBuffer lastName;		// save the last name created
	private StringBuffer lastRef;		// save the last ref created
	private StringBuffer lastSuffix;	// check if same as last call

	private StringBuffer buffer = new StringBuffer(80);

	public MakeVarName() {
	    targetName = null;
	    lastName = new StringBuffer(80);
	    lastRef = new StringBuffer(80);
	    lastSuffix = new StringBuffer(20);
	}


	/**
	 *  Change the targetName so we can reuse this same object with another
	 *  target.
	 */
	public void setTargetName(String targetName) {
	    this.targetName = targetName;

	    lastName.delete(0, lastName.length());
	    lastRef.delete(0, lastRef.length());
	    lastSuffix.delete(0, lastSuffix.length());
	}


	/**
	 *  Return a string with the desired name. Cache the last suffix and
	 *  returned string so we don't need to recreate it if we match the
	 *  last call. This should happen fairly often.
	 */
	public String makeName(String suffix) {

	    if (suffix.equals(lastSuffix.toString())) {
		return lastName.toString();
	    } else {
		buffer.replace(0, buffer.length(), suffix);
		buffer.append(targetName);
		lastName.replace(0, lastName.length(), buffer.toString());
		return buffer.toString();
	    }
	}


	/**
	 *  Return a string with the desired name. This flavor allows an extra
	 *  string to be appended to the name.
	 */
	public String makeName(String suffix, String extra) {

	    if (suffix.equals(lastSuffix.toString())) {
		return lastName.toString();
	    } else {
		buffer.replace(0, buffer.length(), suffix);
		buffer.append(targetName);
		buffer.append("_");					// NOI18N
		buffer.append(extra);
		lastName.replace(0, lastName.length(), buffer.toString());
		return buffer.toString();
	    }
	}


	/**
	 *  Return a string with the desired variable reference. Cache the last
	 *  suffix and returned string so we don't need to recreate it if we
	 *  match the last call. This should happen fairly often.
	 */
	public String makeRef(String suffix) {

	    if (suffix.equals(lastSuffix.toString())) {
		return lastRef.toString();
	    } else {
		buffer.replace(0, buffer.length(), "$(");		// NOI18N
		buffer.append(suffix);
		buffer.append(targetName);
		buffer.append(")");					// NOI18N
		lastRef.replace(0, lastRef.length(), buffer.toString());
		return buffer.toString();
	    }
	}


	/**
	 *  Return a string with the desired variable reference. This flavor
	 *  allows an extra string to be appended to the name.
	 */
	public String makeRef(String suffix, String extra) {

	    if (suffix.equals(lastSuffix.toString())) {
		return lastRef.toString();
	    } else {
		buffer.replace(0, buffer.length(), "$(");		// NOI18N
		buffer.append(suffix);
		buffer.append(targetName);
		buffer.append("_");					// NOI18N
		buffer.append(extra);
		buffer.append(")");					// NOI18N
		lastRef.replace(0, lastRef.length(), buffer.toString());
		return buffer.toString();
	    }
	}


	/** Return the last name we created */
	public String lastName() {
	    return lastName.toString();
	}


	/** Return the last variable reference we created */
	public String lastRef() {
	    return lastRef.toString();
	}
    }
