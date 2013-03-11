/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.prep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.modules.css.indexing.api.CssIndexModel;
import org.netbeans.modules.css.indexing.api.CssIndexModelFactory;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.prep.model.CPModel;
import org.netbeans.modules.css.prep.model.Element;
import org.netbeans.modules.css.prep.model.ElementHandle;
import org.netbeans.modules.css.prep.model.ElementType;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
public class CPCssIndexModel extends CssIndexModel {

    private static final String MIXINS_INDEX_KEY = "cp_mixins"; //NOI18N
    private static final String VARIABLES_INDEX_KEY = "cp_variables"; //NOI18N
    
    private static final Collection<String> INDEX_KEYS = Arrays.asList(new String[]{MIXINS_INDEX_KEY, VARIABLES_INDEX_KEY});
    
    private static final String VALUE_SEPARATOR = ",";
    private static final String TYPE_SEPARATOR = "/";
    
    private Collection<ElementHandle> mixins, variables;

    public CPCssIndexModel(Collection<ElementHandle> mixins, Collection<ElementHandle> variableNames) {
        this.mixins = mixins;
        this.variables = variableNames;
    }
    
    public Collection<ElementHandle> getVariables() {
        return variables;
    }
    
    public Collection<ElementHandle> getMixins() {
        return mixins;
    }
    
    @Override
    public void storeToIndex(IndexDocument document) {
         storeItems(mixins, document, MIXINS_INDEX_KEY);
         storeItems(variables, document, VARIABLES_INDEX_KEY);
    }
    
    private void storeItems(Collection<? extends ElementHandle> items, IndexDocument document, String key) {
        Iterator<? extends ElementHandle> i = items.iterator();
        StringBuilder sb = new StringBuilder();
        while (i.hasNext()) {
            ElementHandle handle = i.next();
            sb.append(handle.getName());
            sb.append(TYPE_SEPARATOR);
            sb.append(handle.getType().getIndexCode());
            
            if (i.hasNext()) {
                sb.append(VALUE_SEPARATOR); //NOI18N
            }
        }
        document.addPair(key, sb.toString(), false, true);
    }
    
    @ServiceProvider(service = CssIndexModelFactory.class)
    public static final class Factory extends CssIndexModelFactory{

        @Override
        public CPCssIndexModel getModel(CssParserResult result) {
            CPModel model = CPModel.getModel(result);
            Collection<Element> mixins = model.getMixins();
            Collection<Element> vars = model.getVariables();
            
            return new CPCssIndexModel(
                    Element.toHandles(mixins), 
                    Element.toHandles(vars));
        }

        @Override
        public CPCssIndexModel loadFromIndex(IndexResult result) {
            String mixins = result.getValue(MIXINS_INDEX_KEY);
            String variables = result.getValue(VARIABLES_INDEX_KEY);

            return new CPCssIndexModel(
                    parseItems(mixins, result.getFile()),
                    parseItems(variables, result.getFile()));
        }

        @Override
        public Collection<String> getIndexKeys() {
            return INDEX_KEYS;
        }
        
        private Collection<ElementHandle> parseItems(String value, FileObject file) {
            if(value == null || value.isEmpty()) {
                return Collections.emptyList();
            }
            String[] items = value.split(VALUE_SEPARATOR);
            Collection<ElementHandle> handles = new ArrayList<ElementHandle>(items.length);
            for(String item : items) {
                String[] name_type_pair = item.split(TYPE_SEPARATOR);
                String name = name_type_pair[0];
                String typeIndexCode = name_type_pair[1];
                
                ElementType type = ElementType.forIndexCode(typeIndexCode);
                ElementHandle handle = new ElementHandle(file, name, type);
                
                handles.add(handle);
            }
            return handles;
            
        }
        
    }
    
}
