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

public class BooleanConfiguration {
    private BooleanConfiguration master;

    private boolean def;
    private String falseValue;
    private String trueValue;

    private boolean value;
    private boolean modified;

    public BooleanConfiguration(BooleanConfiguration master, boolean def) {
	this.master = master;
	this.def = def;
	falseValue = ""; // NOI18N
	trueValue = ""; // NOI18N
	reset();
    }

    public BooleanConfiguration(BooleanConfiguration master, boolean def, String falseValue, String trueValue) {
	this.master = master;
	this.def = def;
	this.falseValue = falseValue;
	this.trueValue = trueValue;
	reset();
    }

    public void setValue(boolean b) {
	this.value = b;
	if (master != null)
	    setModified(true);
	else
	    setModified(b != getDefault());
    }
    public boolean getValue() {
	if (master != null && !getModified())
	    return master.getValue();
	else
	    return value;
    }
    public void setModified(boolean b) {
	this.modified = b;
    }
    public boolean getModified() {
	return modified;
    }
    public boolean getDefault() {
	return def;
    }
    public void reset() {
	value = getDefault();
	setModified(false);
    }
    public String getOption() {
	if (getValue())
	    return trueValue;
	else
	    return falseValue;
    }

    // Clone and Assign
    public void assign(BooleanConfiguration conf) {
	setValue(conf.getValue());
	setModified(conf.getModified());
    }

    public Object clone() {
	BooleanConfiguration clone = new BooleanConfiguration(master, def, falseValue, trueValue);
	clone.setValue(getValue());
	clone.setModified(getModified());
	return clone;
    }
}
