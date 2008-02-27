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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 *
 * @author marek
 */
public class PropertyModel {

    private static PropertyModel instance;

    public static synchronized PropertyModel instance() {
        if (instance == null) {
            instance = new PropertyModel("org/netbeans/modules/css/resources/css_property_table"); //NOI18N
        }
        return instance;
    }

    private PropertyModel(String sourcePath) {
        parseSource(sourcePath);
    }
    private Map<String, Property> properties;

    public Collection<Property> properties() {
        return properties.values();
    }

    public Property getProperty(String name) {
        return properties.get(name);
    }

    private void parseSource(String sourcePath) {
        ResourceBundle bundle = NbBundle.getBundle(sourcePath);

        properties = new HashMap<String, Property>();

        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String name = keys.nextElement();
            String value = bundle.getString(name);

            //parse the value - delimiter is semicolon
            StringTokenizer st = new StringTokenizer(value, ";"); //NOI18N
            String values = st.nextToken();

            //XXX workaround - just return simple set of possible values
            //structural info about the values is desired instead
            //do not loose time with that, Schlieman's cc doesn't even
            //have values CC
            values = values.replaceAll("[\\[\\]|\\+\\?,]", ""); //NOI18N

            ArrayList<String> parsedValues = new ArrayList();
            StringTokenizer st2 = new StringTokenizer(values, " "); //NOI18N
            while (st2.hasMoreTokens()) {
                String val = st2.nextToken();
                //ignore the marked elements like !percentage, !length
                if(!val.startsWith("!")) {
                    parsedValues.add(val);
                }
            }

            String initialValue = st.nextToken().trim();
            String appliedTo = st.nextToken().trim();
            boolean inherited = Boolean.parseBoolean(st.nextToken());
            String percentages = st.nextToken().trim();
            
            //parse media groups list
            String mediaGroups = st.nextToken();
            ArrayList<String> mediaGroupsList = new ArrayList();
            StringTokenizer st3 = new StringTokenizer(mediaGroups, ","); //NOI18N
            while (st2.hasMoreTokens()) {
                mediaGroupsList.add(st3.nextToken());
            }

            if (st.hasMoreTokens()) {
                Logger.global.warning("Error in source for css properties model for property: " + name);
            }
 
            //parse bundle key - there might be more properties separated by semicolons
            StringTokenizer nameTokenizer = new StringTokenizer(name, ";");

            while (nameTokenizer.hasMoreTokens()) {
                String parsed_name = nameTokenizer.nextToken().trim();

                Property prop = new Property(parsed_name, parsedValues, initialValue,
                        appliedTo, inherited, percentages, mediaGroupsList);

                properties.put(parsed_name, prop);
            }

        }

        //resolve references (defined by aposthrophes in the definition file)
        for (String propertyName : properties.keySet()) {
            Property property = properties.get(propertyName);
            Collection<String> values = property.values();
            HashSet<String> resolved = new HashSet<String>(6);
            for (String value : values) {
                resolvePropertyValue(value, resolved);
            }
            property.setValues(resolved);
        }


    }

    /** recursivelly resolves all property values */
    private void resolvePropertyValue(String propertyValue, Collection<String> resolved) {
        if (!(propertyValue.startsWith("'") && propertyValue.endsWith("'"))) {
            //not reference property, just return
            resolved.add(propertyValue);
            return;
        }

        //reference - find appropriate property and resolve
        String cropped = propertyValue.substring(1, propertyValue.length() - 1);
        Property property = properties.get(cropped);
        if (property == null) {
            throw new IllegalStateException("Cannot resolve property " + propertyValue);
        }

        Collection<String> values = property.values();
        for (String value : values) {
            resolvePropertyValue(value, resolved);
        }


    }
}
