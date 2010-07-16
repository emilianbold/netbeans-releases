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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.modules.worklist.node.cookie.WSDLElementCookie;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class WSDLElementChildren extends Children.Keys {
    
    private WSDLComponent mElement;
    
    public WSDLElementChildren(WSDLComponent element) {
       this.mElement = element;
    }
    
    protected void addNotify() {
        super.addNotify();
        refreshChildren();
    }

    protected void removeNotify() {
        super.removeNotify();
        super.nodes.clear();
        refresh();
    }
    
    public WSDLComponent getWSDLComponent() {
            return this.mElement;
    }
    
    @SuppressWarnings("unchecked")
    protected Collection getKeys() {
        
        WSDLComponent element = getWSDLComponent();
        if(element != null) {
            ArrayList keys = new ArrayList();
            
            List children = element.getChildren();
            if(children != null) {
                keys.addAll(children);
            }
            return keys;
        }
        
        return Collections.EMPTY_LIST;
    }
    
    @SuppressWarnings("unchecked")
    protected void refreshChildren() {
        //at the start of the list
        List keys = new ArrayList();
        keys.addAll(getKeys());
        this.setKeys(keys);
    }
    
        
    
    public Node getChildNode(Object element) {
        Node childNode = null;
        
        Enumeration enu = this.nodes();
        while(enu.hasMoreElements()) {
            Node child = (Node) enu.nextElement();
            WSDLElementCookie cookie = (WSDLElementCookie) child.getCookie(WSDLElementCookie.class);
            if(cookie != null && element.equals(cookie.getWSDLComponent())) {
                childNode = child;
                break;
            }
        }
        
        return childNode;
    }
    

    
}
