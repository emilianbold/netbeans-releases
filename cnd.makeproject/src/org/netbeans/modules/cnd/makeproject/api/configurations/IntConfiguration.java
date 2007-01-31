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

public class IntConfiguration {
    private IntConfiguration master;
    private int def;
    private String[] names;
    private String[] options;

    private int value;
    private boolean modified;

    public IntConfiguration(IntConfiguration master, int def, String[] names, String[] options) {
	this.master = master;
	this.def = def;
	this.names = names;
	this.options = options;
	reset();
    }

    public void setValue(int value) {
	this.value = value;
	if (master != null)
	    setModified(true);
	else
	    setModified(value != getDefault());
    }

    public void setValue(String s) {
	if (s != null) {
	    for (int i = 0; i < names.length; i++) {
		if (s.equals(names[i])) {
		    setValue(i);
		    break;
		}
	    }
	}
    }
    
    public int getValue() {
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

    public int getDefault() {
	return def;
    }

    public void reset() {
	value = getDefault();
	setModified(false);
    }

    public String getName() {
	if (getValue() < names.length)
	    return names[getValue()];
	else
	    return "???"; // FIXUP // NOI18N
    }

    public String[] getNames() {
	return names;
    }

    public String getOption() {
	return options[getValue()] + " "; // NOI18N
    }

    // Clone and Assign
    public void assign(IntConfiguration conf) {
	setValue(conf.getValue());
	setModified(conf.getModified());
    }

    public Object clone() {
	IntConfiguration clone = new IntConfiguration(master, def, names, options);
	clone.setValue(getValue());
	clone.setModified(getModified());
	return clone;
    }
}
