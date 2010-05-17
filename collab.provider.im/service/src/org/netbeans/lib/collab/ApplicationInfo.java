/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.lib.collab;

import java.util.ArrayList;

/**
 *  This class can be used to set the characteristics/properties of all
 *  CollaborationSession objects
 *  that will be created using a given SessionProvider. Currently you
 *  can set category, type and name
 *  properties for the Session objects.
 *
 * @since version 0.1
 *
 */
public class ApplicationInfo {

    private String _category;
    private String _type;
    private String _name;
    private String _version = "1.0";
    
    private ArrayList _features = new ArrayList();
;
    
    /**
     * set the category of the Session object
     * @param category to which the entity using the Session object belongs to
     */
    public void setCategory(String category) {
        _category = category;
    }
    
    /**
     * set the type of the Session object
     * @param type to which the entity using the Session object belongs to within the given category.
     *
     */
    public void setType(String type) {
        _type = type;
    }
    
    /**
     * set the type of the Session object
     * @param name with which the entity using the Session object would like to advertize.
     *
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     * get the category of the Session
     * @return - String representing the category
     */
    public String getCategory() {
        return _category;
    }

    /**
     * get the type of the Session, the type has to be allowed types for the particular category
     * @return - String representing the type
     */
    
    public String getType() {
       return _type;    
    }
    
    /**
     * returns the name of the Session object
     * @return - String representing the name.
     */
    public String getName() {
       return _name; 
    }        
    
    /**
     * Adds a supported feature
     *
     * Many features are not core requirements, and rely on specific support
     * provided by the calling application.  For instance, file transfer, 
     * xhtml-formatted messages, voice, etc...  This method enables the api
     * to let other entities (clients, servers, ) know that the application 
     * supports a given feature.  The way the feature is advertized is 
     * protocol-specific.  The feature identifiers are also typically protocol-
     * specific.
     *
     * @param feature feature identifier, e.g. 
     *    http://jabber.org/protocol/xhtml-im
     *
     */
    public void addFeature(String feature){
        if (!_features.contains(feature)) _features.add(feature);
    }

    /**
     * removes a supported feature
     * @param feature feature identifier, e.g. 
     *    http://jabber.org/protocol/xhtml-im
     *
     */
    public void removeFeature(String feature){
        _features.remove(feature);
    }
    
    /**
     * retrieves the current list of features.
     * @param feature feature identifier, e.g. 
     *    http://jabber.org/protocol/xhtml-im
     */
    public ArrayList getFeatures(){
        return _features;
    } 
    
    public boolean hasFeature(String feature) throws CollaborationException {
        return (_features != null) ? _features.contains(feature) : false;
    }    

    /**
     * gets the version of this application
     * @return version string
     */
    public String getVersion() {
        return _version;
    }
    
    /**
     * Sets the version of this application
     * @param ver version string
     */
    public void setVersion(String ver) {
        _version = ver;
    }
}
