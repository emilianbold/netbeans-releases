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

import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.properties.parser.PropertyModel;
import org.netbeans.modules.css.editor.properties.parser.PropertyModelTest;
import org.netbeans.modules.css.editor.properties.parser.PropertyValue;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author mfukala@netbeans.org
 */
public class BackgroundsAndBordersModuleTest extends CslTestBase {

    public BackgroundsAndBordersModuleTest(String name) {
        super(name);
    }

    public void testBackground_Attachment() throws ParseException {
        PropertyModel prop = CssModuleSupport.getProperty("background-attachment");
        assertNotNull(prop);

        assertTrue(new PropertyValue(prop, "scroll").success());
        assertTrue(new PropertyValue(prop, "fixed").success());
        assertTrue(new PropertyValue(prop, "local").success());

        assertTrue(new PropertyValue(prop, "local, local, scroll").success());
        assertTrue(new PropertyValue(prop, "fixed,scroll").success());
    }
    
    public void testBackground_Image() throws ParseException {
        PropertyModel prop = CssModuleSupport.getProperty("background-image");
        assertNotNull(prop);

        assertTrue(new PropertyValue(prop, "none").success());
        assertTrue(new PropertyValue(prop, "url(http://site.org/img.png)").success());
        assertTrue(new PropertyValue(prop, "url(picture.jpg)").success());
        
        assertTrue(new PropertyValue(prop, "url(picture.jpg), none, url(x.jpg)").success());
   
         //[ top | bottom ]|[[ <percentage> | <length> | left | center | right ][ <percentage> | <length> | top | center | bottom ]?]|[[ center | [ left | right ] [ <percentage> | <length> ]? ][ center | [ top | bottom ] [ <percentage> | <length> ]? ]]
    }
    
    public void testBackground_Position() throws ParseException {
        PropertyModel prop = CssModuleSupport.getProperty("background-position");
        assertNotNull(prop);

        assertTrue(new PropertyValue(prop, "left 10px top 15px").success());
        assertTrue(new PropertyValue(prop, " left      top     ").success());
//        assertTrue(new PropertyValue(prop, "left      top 15px").success());
        
    }

    public void testIt() {
        PropertyModel prop = CssModuleSupport.getProperty("-bg-position");
        PropertyValue pv = new PropertyValue(prop, "left     top 15px");
        PropertyModelTest.dumpResult(pv);
    }
}
