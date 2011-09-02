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

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.modules.css.editor.csl.CssColor;
import org.netbeans.modules.css.editor.module.spi.CssModule;
import org.netbeans.modules.css.editor.module.spi.PropertyDescriptor;
import org.netbeans.modules.css.editor.module.spi.RenderingEngine;
import org.openide.util.lookup.ServiceProvider;

/**
 * The colors module functionality is partially implemented in the DefaultCssModule
 * from historical reasons. Newly added features are implemented here.
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = CssModule.class)
public class ColorsModule extends CssModule {

    private final PropertyDescriptor colorPropertyDescriptor = new PropertyDescriptor(
            "color", 
            "<colors-list> | <system-color> |  <rgb> | <rgba> | <hsl> | <hsla> | !hash_color_code | transparent | currentColor", 
            null,
            null,
            true,
            null,
            RenderingEngine.ALL);
    
    private final PropertyDescriptor rgbPropertyDescriptor = new PropertyDescriptor(
            "@rgb", 
            "rgb  (  [!number | !percentage]  ,  [ !number | !percentage ]  , [ !number | !percentage]  )", 
            null,
            null,
            false,
            null,
            RenderingEngine.ALL);
    
    private final PropertyDescriptor rgbaPropertyDescriptor = new PropertyDescriptor(
            "@rgba", 
            "rgba  (  [!number | !percentage]  ,  [ !number | !percentage ]  ,  [ !number | !percentage]  ,  !number )", 
            null,
            null,
            false,
            null,
            RenderingEngine.ALL);
    
    private final PropertyDescriptor hslPropertyDescriptor = new PropertyDescriptor(
            "@hsl", 
            "hsl  (  [!number | !percentage]  ,  [ !number | !percentage ]  ,  [ !number | !percentage]  )", 
            null,
            null,
            false,
            null,
            RenderingEngine.ALL);
    
    private final PropertyDescriptor hslaPropertyDescriptor = new PropertyDescriptor(
            "@hsla", 
            "hsla  (  [!number | !percentage]  ,  [ !number | !percentage ]  ,  [ !number | !percentage]  ,  !number )", 
            null,
            null,
            false,
            null,
            RenderingEngine.ALL);
    
    private final PropertyDescriptor colorsListPropertyDescriptor = new PropertyDescriptor(
            "@colors-list", 
            generateColorsList(), 
            null,
            null,
            false,
            null,
            RenderingEngine.ALL);
    
    private final PropertyDescriptor systemColorPropertyDescriptor = new PropertyDescriptor(
            "@system-color", 
            "activeborder | activecaption | appworkspace | background | buttonface | buttonhighlight | buttonshadow | buttontext | captiontext | graytext | highlight | highlighttext | inactiveborder | inactivecaption | inactivecaptiontext | infobackground | infotext | menu | menutext | scrollbar | threeddarkshadow | threedface | threedhighlight | threedlightshadow | threedshadow | window | windowframe | windowtext", 
            null,
            null,
            false,
            null,
            RenderingEngine.ALL);
    
    private final Collection<PropertyDescriptor> propertyDescriptors = 
            Arrays.asList(new PropertyDescriptor[]{
                colorPropertyDescriptor, 
                rgbPropertyDescriptor, 
                rgbaPropertyDescriptor,
                hslPropertyDescriptor, 
                hslaPropertyDescriptor, 
                colorsListPropertyDescriptor, 
                systemColorPropertyDescriptor});
    
    private String generateColorsList() {
        StringBuilder sb = new StringBuilder();
        CssColor[] vals = CssColor.values();
        for(int i = 0; i < vals.length; i++) {
            sb.append(' ');
            sb.append(vals[i]);
            if(i < vals.length - 1) {
                sb.append(" |");
            }
        }
        return sb.toString();
    }
    
    @Override
    public Collection<PropertyDescriptor> getPropertyDescriptors() {
        return propertyDescriptors;
    }

}
