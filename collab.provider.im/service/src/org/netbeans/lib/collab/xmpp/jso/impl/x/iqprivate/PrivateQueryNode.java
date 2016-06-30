/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */


package org.netbeans.lib.collab.xmpp;

import java.util.Iterator;
import java.util.Set;

import org.jabberstudio.jso.Extension;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.io.StreamBuilder;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;
import org.jabberstudio.jso.util.Utilities;
import org.jabberstudio.jso.x.core.AuthQuery;
import org.jabberstudio.jso.StreamDataFactory;

import net.outer_planes.jso.ExtensionBuilder;
import net.outer_planes.jso.ExtensionNode;

/**
 *
 */
public class PrivateQueryNode extends ExtensionNode implements PrivateQuery {
    StreamDataFactory _sdf;

    //Constructors
    public PrivateQueryNode(StreamDataFactory sdf) {
        this(sdf, new NSI("query", NAMESPACE));
	_sdf = sdf;
    }
    public PrivateQueryNode(StreamDataFactory sdf, NSI name) {
        super(sdf, name);
	_sdf = sdf;
    }
    protected PrivateQueryNode(StreamElement parent, PrivateQueryNode aqn) {
        super(parent, aqn);
    }

    //Methods
    public StreamBuilder createBuilder() {
        return new ExtensionBuilder(this);
    }

    public Set getFieldNames() {
        Set         fields = new java.util.TreeSet();
        Iterator    itr = listElements(new NSI(null, getNamespaceURI())).iterator();

        while (itr.hasNext()) {
            StreamElement   field = (StreamElement)itr.next();
            String          ln = field.getLocalName();

            fields.add(field.getLocalName());
        }

        return fields;
    }
    public String getField(String name) throws IllegalArgumentException {
        Iterator        itr;
        String          value = null;

        //Validate name
        if (!Utilities.isValidString(name))
            throw new IllegalArgumentException("Name cannot be null or \"\"");

        //Retrieve and "normalize"
        itr = listElements(name).iterator();
        if (itr.hasNext())
            value = ((StreamElement)itr.next()).normalizeTrimText();
        
        return value;
    }


     public void setField(String name, String value) throws IllegalArgumentException {
         //Removed existing field(s) -- Validates name as a side-effect
         unsetField(name);
        
         //Add the field
	 addElement(name).addText(value);
     }


    public void unsetField(String name) throws IllegalArgumentException {
        Iterator        itr;

        //Validate name
        if (!Utilities.isValidString(name))
            throw new IllegalArgumentException("Name cannot be null or \"\"");
        
        //Removed existing field(s)
        itr = listElements(name).iterator();
        while (itr.hasNext())
            remove((StreamElement)itr.next());
    }
    
    public StreamObject copy(StreamElement parent) {
        return new PrivateQueryNode(parent, this);
    }
}
