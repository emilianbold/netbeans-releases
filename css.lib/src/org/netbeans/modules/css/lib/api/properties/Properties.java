/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * An entry point for clients wishing to operate with the property api.
 *
 * @author marekfukala
 */
public class Properties {

    private static final Logger LOGGER = Logger.getLogger(Properties.class.getSimpleName());
    
    //TODO possibly add support for refreshing the cached data based on css module changes in the lookup    
    private static final AtomicReference<Map<String, Collection<PropertyDefinition>>> PROPERTIES_MAP = new AtomicReference<Map<String, Collection<PropertyDefinition>>>();
    private static final AtomicReference<Collection<PropertyDefinition>> PROPERTIES = new AtomicReference<Collection<PropertyDefinition>>();
    private static final Map<String, PropertyModel> PROPERTY_MODELS = new HashMap<String, PropertyModel>();
    
    public static boolean isVisibleProperty(PropertyDefinition propertyDefinition) {
        char c = propertyDefinition.getName().charAt(0);
        return c != '@';
    }
    
    public static boolean isVendorSpecificProperty(PropertyDefinition propertyDefinition) {
        return isVendorSpecificPropertyName(propertyDefinition.getName());
    }
    
    public static boolean isVendorSpecificPropertyName(String propertyName) {
        char c = propertyName.charAt(0);
        return c == '_' || c == '-';
    }
    
    /**
     * 
     * @return collection of all available property definitions
     */
    public static Collection<PropertyDefinition> getProperties(boolean visibleOnly) {
        Collection<PropertyDefinition> props = getProperties();
        if(visibleOnly) {
            //filter
            Collection<PropertyDefinition> filtered = new ArrayList<PropertyDefinition>();
            for(PropertyDefinition pd : props) {
                if(isVisibleProperty(pd)) {
                    filtered.add(pd);
                }
            }
            return filtered;
            
        } else {
            return props;
        }
    }
    /**
     * 
     * @return collection of all available <b>VISIBLE</b> property definitions
     */
    public static Collection<PropertyDefinition> getProperties() {
        synchronized (PROPERTIES) {
            if (PROPERTIES.get() == null) {
                PROPERTIES.set(createAllPropertiesCollection());
            }
            return PROPERTIES.get();
        }
    }

    //return first found, to be changed!
    public static PropertyDefinition getProperty(String propertyName) {
        Collection<PropertyDefinition> found = getProperties(propertyName);
        return found != null && !found.isEmpty() 
                ? found.iterator().next()
                : null;
    }
    
    /**
     * Returns a collection of property definitions for the given property name.
     */
    public static Collection<PropertyDefinition> getProperties(String propertyName) {
        return getProperties(propertyName, false);
    }

    /**
     * Same as {@link #getProperties(java.lang.String) but first it tries to obtain 
     * the invisible (@-prefixed) properties. 
     */
    public static Collection<PropertyDefinition> getProperties(String propertyName, boolean preferInvisibleProperties) {
        //try to resolve the refered element name with the at-sign prefix so
        //the property appearance may contain link to appearance, which in fact
        //will be resolved as the @appearance property:
        //
        //appearance=<appearance> |normal
        //@appearance=...
        //
        StringBuilder sb = new StringBuilder().append(GrammarElement.INVISIBLE_PROPERTY_PREFIX).append(propertyName);
        Collection<PropertyDefinition> invisibleProperty = getPropertiesMap().get(sb.toString());

        return preferInvisibleProperties && invisibleProperty != null ? invisibleProperty : PROPERTIES_MAP.get().get(propertyName);
    }

    /**
     * Returns a cached PropertyModel for the given property name.
     */
    public static PropertyModel getPropertyModel(String name) {
        synchronized (PROPERTY_MODELS) {
            PropertyModel model = PROPERTY_MODELS.get(name);
            if (model == null) {
                Collection<PropertyDefinition> properties = getProperties(name);
                model = properties != null ? new PropertyModel(name, properties) : null;
                PROPERTY_MODELS.put(name, model);
            }
            return model;
        }

    }
    
    /**
     * @return map of property name to collection of Property impls.
     */
    private static Map<String, Collection<PropertyDefinition>> getPropertiesMap() {
        synchronized (PROPERTIES_MAP) {
            if (PROPERTIES_MAP.get() == null) {
                PROPERTIES_MAP.set(loadProperties());
            }
            return PROPERTIES_MAP.get();
        }
    }

    private static Collection<PropertyDefinition> createAllPropertiesCollection() {
        Collection<PropertyDefinition> all = new LinkedList<PropertyDefinition>();
        for (Collection<PropertyDefinition> props : getPropertiesMap().values()) {
            all.addAll(props);
        }
        return all;
    }

    //property name to set of Property impls - one name may be mapped to more properties
    private static Map<String, Collection<PropertyDefinition>> loadProperties() {
        Map<String, Collection<PropertyDefinition>> all = new HashMap<String, Collection<PropertyDefinition>>();
        for (PropertyDefinition pd : PropertyDefinitionProvider.Query.getProperties()) {
            String propertyName = pd.getName();
            Collection<PropertyDefinition> props = all.get(propertyName);
            if (props == null) {
                props = new LinkedList<PropertyDefinition>();
                all.put(propertyName, props);
            }
            if (!GrammarElement.isArtificialElementName(propertyName)) {
                //standart (visible) properties cannot be duplicated
                if (!props.isEmpty()) {
                    LOGGER.warning(String.format("Duplicate property %s found, "
                            + "offending css module: %s", pd.getName(), pd.getCssModule())); //NOI18N
                    for (PropertyDefinition p : props) {
                        LOGGER.warning(String.format("Existing property found"
                                + " in css module: %s", p.getCssModule())); //NOI18N
                    }
                }
            }
            props.add(pd);

        }
        return all;
    }
}
