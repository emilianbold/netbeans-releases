/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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


package org.netbeans.tests.j2eeserver.plugin.jsr88;

import java.beans.*;

/**
 *
 * @author  George Finklang
 */
public class ResourceManager extends Object implements java.io.Serializable {

    private static final String PROP_SAMPLE_PROPERTY = "SampleProperty";

    private PropertyChangeSupport propertySupport;

    /** Holds value of property resourceName. */
    private String resourceName;

    /** Holds value of property resourceJndiName. */
    private String resourceJndiName;

    /** Holds value of property resourceUrl. */
    private String resourceUrl;

    /** Creates new ResourceManager */
    public ResourceManager(String resourceName) {
        this.resourceName = resourceName;
        propertySupport = new PropertyChangeSupport( this );
    }


    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    /** Getter for property resourceName.
     * @return Value of property resourceName.
     */
    public String getResourceName() {
        return this.resourceName;
    }

    /** Getter for property resourceJndiName.
     * @return Value of property resourceJndiName.
     */
    public String getResourceJndiName() {
        return this.resourceJndiName;
    }

    /** Setter for property resourceJndiName.
     * @param resourceJndiName New value of property resourceJndiName.
     */
    public void setResourceJndiName(String resourceJndiName) {
        String oldResourceJndiName = this.resourceJndiName;
        this.resourceJndiName = resourceJndiName;
        propertySupport.firePropertyChange("resourceJndiName", oldResourceJndiName, resourceJndiName);
    }

    /** Getter for property resourceUrl.
     * @return Value of property resourceUrl.
     */
    public String getResourceUrl() {
        return this.resourceUrl;
    }

    /** Setter for property resourceUrl.
     * @param resourceUrl New value of property resourceUrl.
     */
    public void setResourceUrl(String resourceUrl) {
        String oldResourceUrl = this.resourceUrl;
        this.resourceUrl = resourceUrl;
        propertySupport.firePropertyChange("resourceUrl", oldResourceUrl, resourceUrl);
    }

}
