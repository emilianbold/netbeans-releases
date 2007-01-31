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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.StringTokenizer;
import java.util.Vector;
import org.netbeans.modules.cnd.api.utils.CppUtils;

public class OptionsConfiguration {
    private String preDefined = ""; // NOI18N
    boolean dirty = false;

    private String commandLine;
    private boolean commandLineModified;

    // Constructors
    public OptionsConfiguration() {
	optionsReset();
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getDirty() {
        return dirty;
    }

    // Options
    public void setValue(String commandLine) {
        setDirty(!this.commandLine.equals(commandLine));
	this.commandLine = commandLine;
	setModified(!commandLine.equals(getDefault()));
    }
    public String getValue() {
	return commandLine;
    }
    public void setModified(boolean b) {
	this.commandLineModified = b;
    }
    public boolean getModified() {
	return commandLineModified;
    }
    public String getDefault() {
	return ""; // NOI18N
    }
    public void optionsReset() {
	commandLine = getDefault();
	commandLineModified = false;
    }

    public String getOptions(String prepend) {
	return CppUtils.reformatWhitespaces(getValue(), prepend);
    }

    public String[] getValues() {
	Vector values = new Vector();
	StringTokenizer st = new StringTokenizer(getValue());
	while (st.hasMoreTokens()) {
	    values.add(st.nextToken());
	}
	return (String[])values.toArray(new String[values.size()]);
    }
    public Vector getValuesAsVector() {
	Vector values = new Vector();
	StringTokenizer st = new StringTokenizer(getValue());
	while (st.hasMoreTokens()) {
	    values.add(st.nextToken());
	}
        return values;
    }

    // Predefined
    public void setPreDefined(String preDefined) {
	this.preDefined = preDefined;
    }
    public String getPreDefined() {
	return preDefined;
    }

    // Clone and assign
    public void assign(OptionsConfiguration conf) {
	setValue(conf.getValue());
	setModified(conf.getModified());
	setDirty(conf.getDirty());
	setPreDefined(conf.getPreDefined());
    }

    public Object clone() {
	OptionsConfiguration clone = new OptionsConfiguration();
	clone.setValue(getValue());
	clone.setModified(getModified());
	clone.setDirty(getDirty());
	clone.setPreDefined(getPreDefined());
	return clone;
    }
}
