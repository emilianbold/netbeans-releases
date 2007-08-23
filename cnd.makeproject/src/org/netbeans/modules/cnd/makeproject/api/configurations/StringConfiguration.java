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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.StringTokenizer;

public class StringConfiguration {
    private StringConfiguration master;
    private String def;;

    private String value;
    private boolean modified;

    public StringConfiguration(StringConfiguration master, String def) {
	this.master = master;
	this.def = def;
	reset();
    }

    public void setValue(String b) {
	if (b == null)
	    b = ""; // NOI18N
	this.value = b;
	if (master != null)
	    setModified(true);
	else
	    setModified(!b.equals(getDefault()));
    }
    public String getValue() {
	if (master != null && !getModified())
	    return master.getValue();
	else
	    return value;
    }
    public String getValueDef(String def) {
	if (master != null && !getModified() && !master.getModified() && def != null)
            return def;
	if (master != null && !getModified())
	    return master.getValue();
	else if (!getModified() && def != null)
            return def;
        else
	    return value;
    }
    public String getValue(String delim) {
	StringBuilder ret = new StringBuilder();
	StringTokenizer tokenizer = new StringTokenizer(getValue());
	while (tokenizer.hasMoreTokens()) {
	    ret.append(tokenizer.nextToken());
	    if (tokenizer.hasMoreTokens())
		ret.append(delim);
	}
	return ret.toString();
    }
    public void setModified(boolean b) {
	this.modified = b;
    }
    public boolean getModified() {
	return modified;
    }
    public String getDefault() {
	return def;
    }
    public void reset() {
	value = getDefault();
	setModified(false);
    }

    // Clone and Assign
    public void assign(StringConfiguration conf) {
	setValue(conf.getValue());
	setModified(conf.getModified());
    }

    public Object clone() {
	StringConfiguration clone = new StringConfiguration(master, def);
	clone.setValue(getValue());
	clone.setModified(getModified());
	return clone;
    }
}
