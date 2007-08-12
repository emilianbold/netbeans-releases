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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class Configurations {

    public static final String PROP_DEFAULT = "default"; // NOI18N
    public static final String PROP_ACTIVE_CONFIGURATION = "activeconfiguration"; // NOI18N

    private PropertyChangeSupport pcs;
    private List<Configuration> configurations;

    public Configurations() {
        pcs = new PropertyChangeSupport(this);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    /*
     * Initialize from a comma separated list of configurations ("Debug,Release").
     */
    /*
    public Confs init(String configurationNames) {
	init(configurationNames, null);
	return this;
    }
    */

    /*
     * Initialize from a comma separated list of configurations ("Debug,Release"). Also
     * setting default comfiguration if specified, othervise default configuration is
     * set to first configuration.
     */
    /*
    public Confs init(String configurationNames, String defaultConf) {
	configurations = new ArrayList();
	StringTokenizer st = new StringTokenizer(configurationNames, ","); // NOI18N
	while (st.hasMoreTokens()) {
	    String displayName = st.nextToken();
	    configurations.add(new Conf(displayName));
	}
	if (defaultConf != null)
	     setDefault(defaultConf);
	else
	    setDefault(0);
	return this;
    }
    */
    public Configurations init(Configuration[] confs, int defaultConf) {
	configurations = new ArrayList();
	for (int i = 0; i < confs.length; i ++)
	    configurations.add(confs[i]);
	if (defaultConf >= 0 && confs != null && confs.length > 0)
	    setActive(defaultConf);
	return this;
    }


    public int size() {
	return configurations.size();
    }


    /*
     * Get all configurations
    */
    public Configuration[] getConfs() {
	return (Configuration[]) configurations.toArray(new Configuration[size()]);
    }

    public Collection<Configuration> getConfsAsCollection() {
        Collection<Configuration> collection = new LinkedHashSet<Configuration>();
        collection.addAll(configurations);
        return collection;
    }
    
    public Configuration[] getClonedConfs() {
	Configuration[] cs = new Configuration[size()];
	for (int i = 0; i < size(); i++) {
	    Configuration c = (Configuration)configurations.get(i);
	    cs[i] = c.cloneConf();
	}
	return cs;
    }

    public String[] getConfsAsDisplayNames() {
	String[] names = new String[size()];
	for (int i = 0; i < size(); i++) {
	    Configuration configuration = (Configuration)configurations.get(i);
	    names[i] = configuration.toString();
	}
	return names;
    }

    public String[] getConfsAsNames() {
	String[] names = new String[size()];
	for (int i = 0; i < size(); i++) {
	    Configuration configuration = (Configuration)configurations.get(i);
	    names[i] = configuration.getName();
	}
	return names;
    }

    
    /*
     * Get a specific configuration
     */
    public Configuration getConf(int index) {
	checkValidIndex(index);
	return (Configuration)configurations.get(index);
    }

    public Configuration getConfByDisplayName(String displayName) {
	    Configuration ret = null;
	    for (Iterator it = configurations.iterator(); it.hasNext(); ) {
		Configuration c = (Configuration)it.next();
		if (c.getDisplayName().equals(displayName)) {
		    ret = c;
		    break;
		}
	    }
	    return ret;
    }

    public Configuration getConf(String name) {
	    Configuration ret = null;
	    for (Iterator it = configurations.iterator(); it.hasNext(); ) {
		Configuration c = (Configuration)it.next();
		if (c.getName().equals(name)) {
		    ret = c;
		    break;
		}
	    }
	    return ret;
    }


    /*
     * Set default configuration
     */
    public void setActive(Configuration def) {
        if (def == null)
            return;
        Configuration old = getActive();
        if (def == old)
            return; // Nothing has changed
        
	    for (Iterator it = configurations.iterator(); it.hasNext(); ) {
		Configuration c = (Configuration)it.next();
		c.setDefault(false);
            if (c == def) {
	    def.setDefault(true);
        pcs.firePropertyChange(PROP_ACTIVE_CONFIGURATION, old, def);
        pcs.firePropertyChange(PROP_DEFAULT, null, null);
    }
        }
    }

    /*
     * Set default configuration
     */
    public void setActive(String name) {
        setActive(getConf(name));
    }

    public void setActive(int index) {
        Configuration old = getActive();
	checkValidIndex(index);
	Configuration def = (Configuration)configurations.get(index);
	if (def != null) {
	    for (Iterator it = configurations.iterator(); it.hasNext(); ) {
		Configuration c = (Configuration)it.next();
		c.setDefault(false);
	    }
	    def.setDefault(true);
	}
        
        pcs.firePropertyChange(PROP_ACTIVE_CONFIGURATION, old, def);
        pcs.firePropertyChange(PROP_DEFAULT, null, null);
    }


    /*
     * Get default configuration
     */
    public String getActiveDisplayName() {
	String defDisplayName = null;
	Configuration def = getActive();
	if (def != null)
	    defDisplayName = def.getDisplayName();
	return defDisplayName;
    }
    
    /**
     * @deprecated. Use getActive()
     */
    public Configuration getDefault() {
        return getActive();
    }

    public Configuration getActive() {
	    for (Iterator it = configurations.iterator(); it.hasNext(); ) {
		Configuration c = (Configuration)it.next();
		if (c.isDefault()) {
                    return c;
		}
	    }
	    return null;
    }

    public int getActiveAsIndex() {
	    int index = -1;
	    for (Iterator it = configurations.iterator(); it.hasNext(); ) {
		index++;
		Configuration c = (Configuration)it.next();
		if (c.isDefault()) {
		    return index;
		}
	    }
	    return -1;
    }


    /*
     * Check valid index
     */
    private void checkValidIndex(int index) {
	if (index < 0 || index >= size()) {
	    ;// Error ???
	    ;// FIXUP ???
	}
    }

    public Configurations cloneConfs() {
	Configurations clone = new Configurations();
	clone.init(getClonedConfs(), getActiveAsIndex());
	return clone;
    }
    
}
