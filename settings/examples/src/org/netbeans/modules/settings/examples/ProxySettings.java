/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings.examples;

import java.util.Properties;

/**
 *
 * @author  Jan Pokorsky
 */
public final class ProxySettings {
    private final static String PROP_PROXYHOST = "proxyHost"; //NOI18N
    private final static String PROP_PROXYPORT = "proxyPort"; //NOI18N
    
    /** Holds value of property proxyHost. */
    private String proxyHost;
    
    /** Utility field used by bound properties. */
    private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);
    
    /** Holds value of property proxyPort. */
    private int proxyPort;
    
    /** Creates a new instance of ProxySettings */
    public ProxySettings() {
    }
    
    /** Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    /** Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    /** Getter for property proxyHost.
     * @return Value of property proxyHost.
     */
    public String getProxyHost() {
        return (proxyHost == null)? "default": proxyHost;
    }
    
    /** Setter for property proxyHost.
     * @param proxyHost New value of property proxyHost.
     */
    public void setProxyHost(String proxyHost) {
        String oldProxyHost = this.proxyHost;
        this.proxyHost = proxyHost;
        propertyChangeSupport.firePropertyChange(PROP_PROXYHOST, oldProxyHost, proxyHost); //NOI18N
    }
    
    /** Getter for property proxyPort.
     * @return Value of property proxyPort.
     */
    public int getProxyPort() {
        return this.proxyPort;
    }
    
    /** Setter for property proxyPort.
     * @param proxyPort New value of property proxyPort.
     */
    public void setProxyPort(int proxyPort) {
        int oldProxyPort = this.proxyPort;
        this.proxyPort = proxyPort;
        propertyChangeSupport.firePropertyChange(PROP_PROXYPORT, new Integer(oldProxyPort), new Integer(proxyPort));
    }
    
    private void readProperties(Properties p) {
        this.proxyHost = p.getProperty(PROP_PROXYHOST); //NOI18N
        try {
            this.proxyPort = Integer.parseInt(p.getProperty(PROP_PROXYPORT));
        } catch (NumberFormatException ex) {
            this.proxyPort = 0;
        }
    }
    
    private void writeProperties(Properties p) {
        p.setProperty(PROP_PROXYHOST, proxyHost);
        p.setProperty(PROP_PROXYPORT, String.valueOf(proxyPort));
    }
}
