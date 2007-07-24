/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
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
