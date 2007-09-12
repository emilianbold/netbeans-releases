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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 * This class represents the base class for nodes which are has one specific
 * feature, which distint them from ordinal nodes.
 * They keeps the reference not to a container object, but to it's parent object.
 * <p>
 * For example, the VariableContainerNode is a container.
 * It keeps reference to BaseScope but not to the VariableContainer.
 * This behaviour is necessary to show such nodes at a tree view
 * even if they are not present at source model.
 * So a user will not ever asked to add container first and then add
 * variable. User will add variable straight away.
 * <p>
 * This method is intended to provide correct node searching.
 * See the findNode method.
 * <P>
 * This class is a generic class ans has additional generic type paramenter.
 * It specifies the type of container reference. 
 *
 * @author nk160297
 */
public abstract class ContainerBpelNode<RT, CT> extends BpelNode<RT> {
    
    public ContainerBpelNode(RT referent, Lookup lookup) {
        super(referent, lookup);
    }
    
    public ContainerBpelNode(RT referent, Children children, Lookup lookup) {
        super(referent, children, lookup);
    }
    
    @Override
    protected boolean isEventRequreUpdate(ChangeEvent event) {
        if (isRequireSpecialUpdate(event, getReference())) {
            return true;
        }
        
        if (event == null) {
            return false;
        }
        
        CT containerRef = getContainerReference();
        if (containerRef != null && event.getParent() == containerRef) {
            return true;
        }

        RT ref = getReference();
        BpelEntity eventParent = event.getParent();
        
        return ref != null && eventParent != null 
                && eventParent.getParent() == ref;
    }
    
    /**
     * The reference to the container object.
     */
    public abstract CT getContainerReference();
}
