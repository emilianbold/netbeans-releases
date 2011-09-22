/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.module.main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.CssModule;
import org.netbeans.modules.css.editor.module.spi.Property;
import org.netbeans.modules.css.editor.module.spi.Utilities;

/**
 *
 * @author marekfukala
 */
public class StandardPropertiesHelpResolverTest extends NbTestCase {

    public StandardPropertiesHelpResolverTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //allow the InstalledFilesLocator to work
        System.setProperty("netbeans.dirs", System.getProperty("cluster.path.final"));//NOI18N
    }

    public void testPropertyHelp() {
        assertPropertyHelp("animation");
        assertPropertyHelp("vertical-position");
    }
    
    //Bug 202493 - java.io.FileNotFoundException: JAR entry www.w3.org/TR/css3-lists//index.html not found in /home/tester/netbeans-7.1beta/ide/docs/css3-spec.zip
    public void testProperty_Fallback() {
        assertPropertyHelp("fallback");
    }
    
    public void testGetHelpForAllCSS3StandardProperties() {
        for(Property prop : CssModuleSupport.getProperties()) {
            if(!Css3Utils.isVendorSpecificProperty(prop.getName())) {
                CssModule module = prop.getCssModule();
                if(module == null) {
                    continue;
                }
                if(module instanceof BrowserSupportModule) {
                    continue;
                }
                if("http://www.w3.org/TR/CSS2".equals(module.getSpecificationURL())) {
                    continue;
                }
                assertPropertyHelp(prop.getName());
            }
        }
    }
    
    private void assertPropertyHelp(String propertyName) {
        StandardPropertiesHelpResolver instance = new StandardPropertiesHelpResolver();
        Property property = CssModuleSupport.getProperty(propertyName);
        assertNotNull(property);
        String helpContent = instance.getHelp(property);
//        System.out.println(helpContent);
        
        assertNotNull(String.format("Null help for property %s from module %s", propertyName, property.getCssModule().getDisplayName()), helpContent);
//        assertTrue(helpContent.startsWith("<h"));
        
    }

}
