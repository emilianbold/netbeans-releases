/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.mobility.svgcore.items.form;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.netbeans.modules.mobility.svgcore.util.SVGComponentsSupport;

/**
 * SVGComponentDrop implementation for Button.
 * loads snippet text from button_snippet.xml_template
 * <p>
 * Note: Patterns used in this snipped differ from other snippets.
 * <p>
 * patterns used in snippet:  %%FRAME_COMPONENT_ID%%, %%COORDINATE_X%%, %%COORDINATE_Y%%, 
 * %%COMPONENT_ID_1%%, %%COMPONENT_ID_2%%
 * 
 * @author akorostelev
 */
public class RadioButton extends SVGFormElement{

    private static final String ID_PATTERN_FRAME = PATTERN + "FRAME_COMPONENT_ID" + PATTERN;//NOI18N
    private static final String ID_PATTERN_1 = PATTERN + "COMPONENT_ID_1" + PATTERN;//NOI18N
    private static final String ID_PATTERN_2 = PATTERN + "COMPONENT_ID_2" + PATTERN;//NOI18N
    private static final String SNIPPET_PATH = "radiobutton_snippet.xml_template"; //NOI18N
    
    
    public RadioButton() {
        super(SNIPPET_PATH);
    }
    //
    
    @Override
    protected boolean doTransfer() {
        SVGFileModel model = getSVGDataObject().getModel();
        try {
            String idFrame = model.createUniqueId(SVGComponentsSupport.ID_PREFIX_RADIOBUTTON_FRAME, false);
            String id1 = model.createUniqueId(SVGComponentsSupport.ID_PREFIX_RADIOBUTTON, false);
            String id2 = model.createUniqueId(SVGComponentsSupport.ID_PREFIX_RADIOBUTTON, false, 
                    new HashSet(Arrays.asList(id1)) );
            
            String snippet = getSnippet(idFrame, id1, id2);
            model.mergeImage(snippet, false);
            setSelection(idFrame);
            return true;
        } catch (Exception ex) {
            SceneManager.error("Error during image merge", ex); //NOI18N
        }
        return false;
    }
    
    private String getSnippet(String idFrame, String id1, String id2) throws IOException{
        String text = getSnippetString();
        String withIdFrame = text.replace(ID_PATTERN_FRAME, idFrame);
        String withId1 = withIdFrame.replace(ID_PATTERN_1, id1);
        String withId2 = withId1.replace(ID_PATTERN_2, id2);
        return replaceCoordinates(withId2);
    }
    
}
