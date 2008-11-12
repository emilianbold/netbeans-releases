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

import org.netbeans.modules.vmd.api.model.PresenterSerializer;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.util.List;
import java.util.Arrays;

/**
 * @author David Kaspar
 */
/* XXX would need a no-arg constructor
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.api.model.PresenterSerializer.class)
*/
public class MidpPropertyPresenterSerializer implements PresenterSerializer {

    private String displayName;
    private String editorID;
    private String propertyName;

    public MidpPropertyPresenterSerializer (String displayName, PropertyDescriptor property) {
        this.displayName = displayName;
        this.editorID = createEditorIDForPropertyDescriptor (property);
        this.propertyName = property.getName ();
    }

    public List<Element> serialize (Document document) {
        org.w3c.dom.Element element = document.createElement (MidpPropertyPresenterDeserializer.PROPERTY_NODE);
        XMLUtils.setAttribute (document, element, MidpPropertyPresenterDeserializer.DISPLAY_NAME_ATTR, displayName);
        if (editorID != null)
            XMLUtils.setAttribute (document, element, MidpPropertyPresenterDeserializer.EDITOR_ATTR, editorID);
        XMLUtils.setAttribute (document, element, MidpPropertyPresenterDeserializer.PROPERTY_NAME_ATTR, propertyName);
        return Arrays.asList (element);
    }

    private static String createEditorIDForPropertyDescriptor (PropertyDescriptor property) {
        TypeID type = property.getType ();
        if (MidpTypes.TYPEID_BOOLEAN.equals (type))
            return MidpPropertyPresenterDeserializer.EDITOR_BOOLEAN;
        if (MidpTypes.TYPEID_CHAR.equals (type))
            return MidpPropertyPresenterDeserializer.EDITOR_CHAR;
        if (MidpTypes.TYPEID_BYTE.equals (type))
            return MidpPropertyPresenterDeserializer.EDITOR_BYTE;
        if (MidpTypes.TYPEID_SHORT.equals (type))
            return MidpPropertyPresenterDeserializer.EDITOR_SHORT;
        if (MidpTypes.TYPEID_INT.equals (type))
            return MidpPropertyPresenterDeserializer.EDITOR_INT;
        if (MidpTypes.TYPEID_LONG.equals (type))
            return MidpPropertyPresenterDeserializer.EDITOR_LONG;
        if (MidpTypes.TYPEID_FLOAT.equals (type))
            return MidpPropertyPresenterDeserializer.EDITOR_FLOAT;
        if (MidpTypes.TYPEID_DOUBLE.equals (type))
            return MidpPropertyPresenterDeserializer.EDITOR_DOUBLE;
        // TODO
        if (MidpTypes.TYPEID_JAVA_LANG_STRING.equals (type))
            return MidpPropertyPresenterDeserializer.EDITOR_STRING;
        return MidpPropertyPresenterDeserializer.EDITOR_JAVA_CODE;
    }

}
