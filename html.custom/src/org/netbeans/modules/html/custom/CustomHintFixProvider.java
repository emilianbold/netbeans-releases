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
package org.netbeans.modules.html.custom;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.html.custom.conf.Attribute;
import org.netbeans.modules.html.custom.conf.Configuration;
import org.netbeans.modules.html.custom.conf.Tag;
import org.netbeans.modules.html.editor.spi.HintFixProvider;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marek
 */
@ServiceProvider(service = HintFixProvider.class)
public class CustomHintFixProvider extends HintFixProvider {

    @Override
    public List<HintFix> getHintFixes(Context context) {
        List<HintFix> fixes = new ArrayList<>();
        String elementName = (String)context.getMetadata().get(UNKNOWN_ELEMENT_FOUND);
        String attributeName = (String)context.getMetadata().get(UNKNOWN_ATTRIBUTE_FOUND);
        String contextElementName = (String)context.getMetadata().get(UNKNOWN_ELEMENT_CONTEXT);
        
        assert contextElementName != null;
        
        if(elementName != null) {
            //unknown element found
            fixes.add(new AddUnknownElementToConfiguration(elementName, contextElementName, context.getSnapshot()));
            fixes.add(new EditProjectsConf(context.getSnapshot()));
            
        } else if(attributeName != null) {
            //unknown attribute found
            fixes.add(new AddUnknownAttributeToConfiguration(attributeName, contextElementName, context.getSnapshot()));
            fixes.add(new EditProjectsConf(context.getSnapshot()));
        } else {
            throw new IllegalStateException();
        }
        
        return fixes;
    }
    
    @NbBundle.Messages("addUnknownElementToProjectConfiguration=Add element \"{0}\" to the project's custom elements")
    private static final class AddUnknownElementToConfiguration implements HintFix {

        private final String elementName, elementContextName;
        private final Snapshot snapshot;

        public AddUnknownElementToConfiguration(String elementName, String elementContextName, Snapshot snapshot) {
            this.elementName = elementName;
            this.elementContextName = elementContextName;
            this.snapshot = snapshot;
        }
        
        @Override
        public String getDescription() {
            return Bundle.addUnknownElementToProjectConfiguration(elementName);
        }

        @Override
        public void implement() throws Exception {
            Configuration conf = Configuration.get(snapshot.getSource().getFileObject());
            conf.getRootTags().put(elementName, new Tag(elementName));
            conf.store();
            LexerUtils.rebuildTokenHierarchy(snapshot.getSource().getDocument(true));
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
        
        
    }
    
    @NbBundle.Messages("addUnknownAttributeToProjectConfiguration=Add attribute \"{0}\" to the project's custom elements")
    private static final class AddUnknownAttributeToConfiguration implements HintFix {

        private final String attributeName, elementContextName;
        private final Snapshot snapshot;

        public AddUnknownAttributeToConfiguration(String attributeName, String elementContextName, Snapshot snapshot) {
            this.attributeName = attributeName;
            this.elementContextName = elementContextName;
            this.snapshot = snapshot;
        }
        
        @Override
        public String getDescription() {
            return Bundle.addUnknownAttributeToProjectConfiguration(attributeName);
        }

        @Override
        public void implement() throws Exception {
            Configuration conf = Configuration.get(snapshot.getSource().getFileObject());
            conf.getRootAttributes().put(attributeName, new Attribute(attributeName));
            conf.store();
            LexerUtils.rebuildTokenHierarchy(snapshot.getSource().getDocument(true));
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
        
        
    }
    
    @NbBundle.Messages("editProjectConfiguration=Edit project's editor custom elements configuration file")
    private static final class EditProjectsConf implements HintFix {

        private final Snapshot snapshot;

        public EditProjectsConf(Snapshot snapshot) {
            this.snapshot = snapshot;
        }
        
        @Override
        public String getDescription() {
            return Bundle.editProjectConfiguration();
        }

        @Override
        public void implement() throws Exception {
            Configuration conf = Configuration.get(snapshot.getSource().getFileObject());
            DataObject dobj = DataObject.find(conf.getProjectsConfigurationFile());
            OpenCookie oc = dobj.getLookup().lookup(OpenCookie.class);
            oc.open();
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
        
        
    }
}
