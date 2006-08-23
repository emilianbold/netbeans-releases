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

package org.netbeans.modules.cnd.debugger.gdb.profiles;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;

import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.openide.util.NbBundle;

public class GdbProfile implements ConfigurationAuxObject {

    public static final String GDB_PROFILE_ID = "gdbdebugger"; // NOI18N
    
    public static final String PROP_GDB_COMMAND = "gdb_command"; // NOI18N

    private PropertyChangeSupport pcs = null;

    private boolean needSave = false;
    
    private String gdb_command;


    /**
     * Constructor
     * Don't call this directly. It will get called when creating
     * ...cnd.execution.profiles.Profile().
     */
    public GdbProfile() {
        initialize();
    }

    protected GdbProfile(PropertyChangeSupport pcs) {
	this.pcs = pcs;
        initialize();
    }
    
    public void initialize() {
        gdb_command = "gdb"; // NOI18N
    }

    public boolean shared() {
	return false;
    }
    
    /**
     * Returns a unique id (String) used to retrive this object from the
     * pool of aux objects.
     */
    public String getId() {
        return GDB_PROFILE_ID;
    }
    
    public String getGdbCommand() {
        return gdb_command;
    }
    
    public void setGdbCommand(String gdb_command) {
        int l = gdb_command.length();
        boolean b = this.gdb_command.equals(gdb_command);
        
        if (gdb_command.length() > 0 && !this.gdb_command.equals(gdb_command)) {
            if (pcs != null) {
                pcs.firePropertyChange(PROP_GDB_COMMAND, this.gdb_command, gdb_command);
            }
            this.gdb_command = gdb_command;
        }
    }
    
    /**
     *  Adds property change listener.
     *  @param l new listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null) {
            pcs.addPropertyChangeListener(l);
        }
    }
    
    /**
     *  Removes property change listener.
     *  @param l removed listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null) {
            pcs.removePropertyChangeListener(l);
        }
    }

    //
    // XML codec support
    // This stuff ends up in <projectdir>/nbproject/private/profiles.xml
    // 

    public XMLDecoder getXMLDecoder() {
	return new GdbProfileXMLCodec(this);
    }

    public XMLEncoder getXMLEncoder() {
	return new GdbProfileXMLCodec(this);
    }

    // interface ProfileAuxObject
    public boolean hasChanged() {
	return needSave;
    }

    // interface ProfileAuxObject
    public void clearChanged() {
	needSave = false;
    }


    // interface ProfileAuxObject
    /**
     * Assign all values from a profileAuxObject to this object (reverse
     * of clone)
     */
    public void assign(ConfigurationAuxObject profileAuxObject) {
	assert profileAuxObject instanceof GdbProfile;

	GdbProfile that = (GdbProfile) profileAuxObject;

	this.setGdbCommand(that.getGdbCommand());
    }

    

    // interface ProfileAuxObject
    /**
     * Clone itself to an identical (deep) copy.
     */
    public Object clone() {
	GdbProfile p = new GdbProfile();

	p.setGdbCommand(getGdbCommand());

	return p;
    }

    public Sheet getSheet() {
	Sheet sheet = new Sheet();
	Sheet.Set set = new Sheet.Set();
	set.setName("General"); // NOI18N
	set.setDisplayName(NbBundle.getMessage(GdbProfile.class, "LBL_GENERAL")); // NOI18N
	set.setShortDescription(NbBundle.getMessage(GdbProfile.class, "HINT_GENERAL")); // NOI18N
	set.put(new GdbCommandNodeProp());
	sheet.put(set);
	return sheet;
    }
    
    private class GdbCommandNodeProp extends PropertySupport {
        public GdbCommandNodeProp() {
            super(PROP_GDB_COMMAND, String.class,
                    NbBundle.getMessage(GdbProfile.class, "LBL_GDB_COMMAND"), // NOI18N
                    NbBundle.getMessage(GdbProfile.class, "HINT_GDB_COMMAND"), // NOI18N
                    true, true);
        }
        
        public Object getValue() {
            return getGdbCommand();
        }
        
        public void setValue(Object v) {
            setGdbCommand((String)v);
        }
    }
}
