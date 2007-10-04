/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
