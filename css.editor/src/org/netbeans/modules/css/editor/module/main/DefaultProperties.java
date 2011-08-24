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
package org.netbeans.modules.css.editor.module.main;

import org.netbeans.modules.css.editor.module.spi.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import org.netbeans.modules.css.editor.module.spi.RenderingEngine;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public class DefaultProperties {

    static Collection<PropertyDescriptor> parseSource(String sourcePath) {
        Collection<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
        ResourceBundle bundle = NbBundle.getBundle(sourcePath);

        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String name = keys.nextElement();
            String value = bundle.getString(name);

            //parse the value - delimiter is semicolon
            StringTokenizer st = new StringTokenizer(value, ";"); //NOI18N
            if(!st.hasMoreTokens()) {
                throw new IllegalArgumentException("Error parsing property " + name);
            }
            String values = st.nextToken();

            //following tokens are not mandatory
            String initialValue = getNextToken(st);
            String appliedTo = getNextToken(st);
            boolean inherited = Boolean.parseBoolean(getNextToken(st));
            String percentages = getNextToken(st);

            //parse media groups list
            String mediaGroups = getNextToken(st);
            ArrayList<String> mediaGroupsList = new ArrayList<String>();
            if (mediaGroups != null) {
                StringTokenizer st3 = new StringTokenizer(mediaGroups, ","); //NOI18N
                while (st3.hasMoreTokens()) {
                    mediaGroupsList.add(st3.nextToken());
                }
            }

            //parse bundle key - there might be more properties separated by semicolons
            StringTokenizer nameTokenizer = new StringTokenizer(name, ";"); //NOI18N

            while (nameTokenizer.hasMoreTokens()) {
                String parsed_name = nameTokenizer.nextToken().trim();

                PropertyDescriptor prop = new PropertyDescriptor(parsed_name, values, initialValue,
                        appliedTo, inherited, mediaGroupsList, RenderingEngine.ALL);

                properties.add(prop);
            }

        }

        return properties;
    }

    private static String getNextToken(StringTokenizer st) {
        return st.hasMoreTokens() ? st.nextToken().trim() : null;
    }
}
