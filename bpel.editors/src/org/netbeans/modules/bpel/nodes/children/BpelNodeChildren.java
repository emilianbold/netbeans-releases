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

package org.netbeans.modules.bpel.nodes.children;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.BpelSafeReference;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 30 March 2006
 *
 */
public abstract class BpelNodeChildren<T extends BpelEntity> extends Children.Keys
    implements ReloadableChildren 
{
    protected Lookup lookup;
    private BpelSafeReference reference;
    
    public BpelNodeChildren(T bpelEntity, Lookup contextLookup) {
        this.lookup = contextLookup;
        setReference(bpelEntity);
    }

    public Lookup getLookup() {
        return lookup;
    }
    
    protected void setReference(T bpelEntity) {
        this.reference = new BpelSafeReference<T>(bpelEntity);
    }
    
    public T getReference() {
        return (T)reference.getBpelObject();
    }
    
    public abstract Collection getNodeKeys();
    
//    protected Node[] createNodes(Object object) {
//        Node childNode = Node.EMPTY;
//        if (object != null && object instanceof Node) {
//            childNode = (Node)object;
//        }
//        return new Node[] {childNode};
//    }

    // by default NavigatorNodeFactory is used 
    // for others factories this method should be overriden
    protected Node[] createNodes(Object object) {
        if (object != null && object instanceof BpelEntity) {
            NavigatorNodeFactory factory = NavigatorNodeFactory.getInstance();
            Node childNode = factory.createNode((BpelEntity)object,lookup);
            if (childNode != null) {
                return new Node[] {childNode};
            }
        } 
        
        return new Node[0];
    }
    
    protected void addNotify() {
        reload();
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }
    
    public void reload() {
        setKeys(getNodeKeys());
    }
}
