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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.vmd.midp.serialization;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PresenterDeserializer;
import org.netbeans.modules.vmd.api.model.Versionable;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * @author David Kaspar
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.api.model.PresenterDeserializer.class)
public class MidpSetterPresenterDeserializer extends PresenterDeserializer {

    public static final String SETTER_NODE = "MidpSetter"; // NOI18N
    public static final String NAME_ATTR = "name"; // NOI18N
    public static final String PARAMETERS_ATTR = "parameters"; // NOI18N

    public MidpSetterPresenterDeserializer () {
        super (MidpDocumentSupport.PROJECT_TYPE_MIDP);
    }

    public PresenterFactory deserialize (Node node) {
        if (! SETTER_NODE.equalsIgnoreCase (node.getNodeName ()))
            return null;
        String name = XMLUtils.getAttributeValue (node, NAME_ATTR);
        String parametersString = XMLUtils.getAttributeValue (node, PARAMETERS_ATTR);
        StringTokenizer tokenizer = new StringTokenizer (parametersString, ","); // NOI18N
        ArrayList<String> parameters = new ArrayList<String> ();
        while (tokenizer.hasMoreTokens ())
            parameters.add (tokenizer.nextToken ());
        return new MidpPropertyPresenterFactory (name, parameters.toArray (new String[parameters.size ()]));
    }

    private static class MidpPropertyPresenterFactory extends PresenterFactory {

        private String name;
        private String[] parameters;

        public MidpPropertyPresenterFactory (String name, String[] parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        public List<Presenter> createPresenters (ComponentDescriptor descriptor) {
            MidpSetter setter;
            if (name != null)
                setter = MidpSetter.createSetter (name, Versionable.FOREVER);
            else
                setter = MidpSetter.createConstructor (descriptor.getTypeDescriptor ().getThisType (), Versionable.FOREVER);
            setter.addParameters (parameters);
            return Arrays.<Presenter>asList (
                new CodeSetterPresenter ().addParameters (MidpParameter.create (parameters)).addSetters (setter)
            );
        }

    }

}
