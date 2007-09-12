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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.bpel.properties;

import java.util.Collection;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * Keeps information about selected type and message part and query for a Property Alias.
 * The message part is pertinent only in case the type is Message.
 *
 * @author nk160297
 */
public class PropAliasSelectionContainer {
    
    private TypeContainer myTypeContainer;
    private Part myMsgPart;
    private String myQueryContent;
    
    public PropAliasSelectionContainer(PropertyAlias propAlias) {
        boolean processed = false;
        //
        NamedComponentReference<Message> msgRef = propAlias.getMessageType();
        if (msgRef != null) {
            Message msg = msgRef.get();
            if (msg != null) {
                myTypeContainer = new TypeContainer(msg);
                processed = true;
                //
                // Look for a Part by the name
                String partName = propAlias.getPart();
                Collection<Part> parts = msg.getParts();
                for (Part part : parts) {
                    if (part.getName().equals(partName)) {
                        myMsgPart = part;
                        break;
                    }
                }
            }
        }
        //
        if (!processed) {
            NamedComponentReference<GlobalElement> elementRef = propAlias.getElement();
            if (elementRef != null) {
                GlobalElement element = elementRef.get();
                if (element != null) {
                    myTypeContainer = new TypeContainer(element);
                    processed = true;
                }
            }
        }
        //
        if (!processed) {
            NamedComponentReference<GlobalType> typeRef = propAlias.getType();
            if (typeRef != null) {
                GlobalType type = typeRef.get();
                if (type != null) {
                    myTypeContainer = new TypeContainer(type);
                    processed = true;
                }
            }
        }
        //
        Query query = propAlias.getQuery();
        if (query != null) {
            myQueryContent = query.getContent();
        }
    }
    
    public PropAliasSelectionContainer(Part msgPart, String queryContent) {
        myMsgPart = msgPart;
        //
        WSDLComponent msg = msgPart.getParent();
        assert msg instanceof Message;
        myTypeContainer = new TypeContainer((Message)msg);
        //
        myQueryContent = queryContent;
    }
    
    public PropAliasSelectionContainer(Message msg, Part msgPart, String queryContent) {
        myTypeContainer = new TypeContainer(msg);
        myMsgPart = msgPart;
        myQueryContent = queryContent;
    }
    
    public PropAliasSelectionContainer(TypeContainer typeContainer, String queryContent) {
        myTypeContainer = typeContainer;
        myQueryContent = queryContent;
    }
    
    public TypeContainer getTypeContainer() {
        return myTypeContainer;
    }
    
    public Part getMessagePart() {
        return myMsgPart;
    }
    
    public String getQueryContent() {
        return myQueryContent;
    }
}
