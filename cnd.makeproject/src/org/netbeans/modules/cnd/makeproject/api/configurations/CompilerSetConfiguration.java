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

import java.util.ArrayList;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetChangeListener;
import org.netbeans.modules.cnd.api.compilers.CompilerSetEvent;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;

public class CompilerSetConfiguration extends IntConfiguration implements CompilerSetChangeListener {
    
    private ArrayList<String> names = new ArrayList();
    private ArrayList<String> options = new ArrayList();
    private CompilerSetConfiguration master;
    private int def;
    private int value;
    private boolean modified;
    private String oldname;
    
    /**
     * A configuration for compiler set names.
     *
     * @param master ???
     * @param def The default value
     * @param names An array of compiler set display names
     * @param optoins An array of compiler set names
     */
    public CompilerSetConfiguration(CompilerSetConfiguration master, int def, String[] names, String[] options) {
        CompilerSetManager.getDefault().addCompilerSetChangeListener(this);
        
        for (int i = 0; i < names.length; i++) {
            this.names.add(names[i]);
            this.options.add(options[i]);
        }
        this.master = master;
        this.def = def;
        value = def;
        modified = false;
        oldname = null;
    }
    
    public void setValue(int value) {
        if (value >= 0 && value <= names.size()) {
            this.value = value;
        }
        if (master != null) {
            setModified(true);
        } else {
            setModified(value != getDefault());
        }
    }

    public void setValue(String s) {
	if (s != null) {
            int i = 0;
            for (String csname : names) {
                if (s.equals(csname)) {
                    setValue(i);
                    return;
                }
                i++;
            }
            i = 0;
            for (String csname : options) {
                if (s.equals(csname)) {
                    setValue(i);
                    return;
                }
                i++;
            }
            oldname = s;
	}
    }
    
    public int getValue() {
        if (master != null && !getModified()) {
            return master.getValue();
        } else {
            return value;
        }
    }
    
    public void setModified(boolean modified) {
        this.modified = modified;
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
        if (getValue() >= 0 && getValue() < names.size()) {
	    return names.get(getValue());
        } else {
	    return "";
        }
    }
    
    public String[] getNames() {
        return names.toArray(new String[0]);
    }

    public String getOption() {
        if (getValue() >= 0 && getValue() <= options.size()) {
            return options.get(getValue());
        } else {
            return "";
        }
    }

    public int getDef() {
        return def;
    }
    
    public void assign(CompilerSetConfiguration conf) {
        setValue(conf.getValue());
        setModified(conf.getModified());
    }

    public Object clone() {
	CompilerSetConfiguration clone = new CompilerSetConfiguration(master, 
                def, names.toArray(new String[0]), options.toArray(new String[0]));
	clone.setValue(getValue());
	clone.setModified(getModified());
	return clone;
    }
    
    public void compilerSetChange(CompilerSetEvent ev) {
        CompilerSetManager csm = (CompilerSetManager) ev.getSource();
        String defdname = names.get(def);
        String curdname = value >= 0 ? names.get(value) : "xxx"; // NOI18N - want non-matching string
        String savename = options.get(value);
        ArrayList<String> nuoptions = new ArrayList();
        ArrayList<String> nunames = new ArrayList();
        def = value = -1;
        
        int i = 0;
        for (CompilerSet cs : csm.getCompilerSets()) {
            nuoptions.add(cs.getName());
            nunames.add(cs.getDisplayName());
            if (cs.getDisplayName().equals(defdname)) {
                def = i;
            }
            if (cs.getDisplayName().equals(curdname)) {
                value = i;
            }
        }
        names = nunames;
        options = nuoptions;
        
        // Save old name in case the selected compiler set gets removed from the user's path. This
        // lets it get restored during build or open validation
        if (oldname != null) {
            if (options.contains(oldname)) {
                value = options.indexOf(oldname);
                oldname = null;
            }
        } else if (!options.contains(savename)) {
            oldname = savename;
        }
        
        // Since I set them to -1 at the start of this method, ensure they don't get left that way
        if (def < 0) {
            def = 0;
        }
        if (value < 0) {
            value = def;
        }
    }
    
    /**
     * If oldname != null then we've had our compiler set removed. We save the oldname
     * so we can ask the user if they want to repair their PATH before operating on
     * the project.
     */
    public boolean isValid() {
        return oldname == null;
    }
    
    public void setValid() {
        oldname = null;
    }
    
    public String getOldName() {
        return oldname;
    }
}
