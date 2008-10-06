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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;

/**
 * @author akorostelev
 */
public class SVGFormElement extends SVGComponentDrop{

    private static final String ID_PATTERN = PATTERN + "COMPONENT_ID" + PATTERN;//NOI18N

    public SVGFormElement(String idPrefix, String snippetPath) {
        assert idPrefix != null && snippetPath != null 
                : "id prefix or snippet path == null";//NOI18N
        myIdPrefix = idPrefix;
        mySnippetPath = snippetPath;
    }

    /**
     * is to be used by childs with different Ids generation aproach 
     * (like radiobutton, which should have 2 unique Ids)
     * @param snippetPath
     */
    protected SVGFormElement(String snippetPath) {
        assert snippetPath != null 
                : "snippet path == null";//NOI18N
        myIdPrefix = "";
        mySnippetPath = snippetPath;
    }

    protected boolean doTransfer() {
        SVGFileModel model = getSVGDataObject().getModel();
        try {
            String id = model.createUniqueId(myIdPrefix, false);
            String snippet = getSnippet(id);
            model.mergeImage(snippet, false);
            setSelection(id);
            return true;
        } catch (Exception ex) {
            SceneManager.error("Error during image merge", ex); //NOI18N
        }
        return false;
    }
    
    private String getSnippet(String id) throws IOException{
        String text = getSnippetString();
        String withId = text.replace(ID_PATTERN, id);
        return replaceCoordinates(withId);
    }
    
    protected String getSnippetString() throws IOException{
        InputStream is = SVGFormElement.class.getResourceAsStream(mySnippetPath);
        assert is != null : mySnippetPath + " resource Input Stream is null";//NOI18N
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }
  
    private String myIdPrefix;
    private String mySnippetPath;
}
