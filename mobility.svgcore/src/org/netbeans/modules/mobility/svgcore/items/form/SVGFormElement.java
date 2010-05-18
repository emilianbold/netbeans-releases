/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.mobility.svgcore.items.form;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.api.snippets.SVGSnippetsProvider;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.netbeans.modules.mobility.svgcore.options.SvgcoreSettings;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * @author akorostelev
 */
public abstract class SVGFormElement extends SVGComponentDrop{

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

    protected boolean doTransfer(SVGDataObject svgDataObject) {
        SVGFileModel model = svgDataObject.getModel();
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
    
    @Override
    protected boolean doTransfer(JTextComponent target) {
        SVGDataObject svgDataObject = SVGDataObject.getActiveDataObject(target);
        final SVGFileModel model = svgDataObject.getModel();
        try {
            String id = model.createUniqueId(myIdPrefix, false);
            String snippet = getSnippet(id);
            insertToTextComponent(snippet, target);
            // #165130
            RequestProcessor.getDefault().post(new Runnable(){
                public void run() {
                    model.forceUpdateModel();
                }
            });
            return true;
        } catch (Exception ex) {
            SceneManager.error("Error during image merge", ex); //NOI18N
        }
        return false;
    }

    /**
     * loads snippet string from resource file,
     * which part is specified in conbstructor.
     * Path is relative to current class -
     * getClass().getResourceAsStream(PATH) is used to load resource.
     * @return snippet String
     * @throws java.io.IOException
     */
    protected final String loadSnippetString() throws IOException{
        Class curentSnippetsProviderClass = getCurrentSnippetsProviderClass();
        return loadSnippetString(curentSnippetsProviderClass, mySnippetPath);
    }

    private String getSnippet(String id) throws IOException {
        String text = loadSnippetString();
        String withId = text.replace(ID_PATTERN, id);
        return replaceCoordinates(withId);
    }
    
    private String myIdPrefix;
    private String mySnippetPath;

    private static final Class getCurrentSnippetsProviderClass() {
        String providerName = SvgcoreSettings.getDefault().getCurrentSnippet();
        if (providerName == null) {
            return SVGSnipetsProviderClassic.class;
        }
        Collection<? extends SVGSnippetsProvider> snippetCollection = Lookup.getDefault().lookupAll(SVGSnippetsProvider.class);

        for (SVGSnippetsProvider provider : snippetCollection) {
            if (providerName.equals(provider.getDisplayName())) {
                return provider.getClass();
            }
        }

        return SVGSnipetsProviderClassic.class;
    }
}
