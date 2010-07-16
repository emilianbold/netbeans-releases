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

import org.netbeans.modules.worklist.node.cookie.DataObjectCookieDelegate;
import org.netbeans.modules.worklist.node.cookie.WSDLElementCookie;
import org.netbeans.modules.worklist.util.Util;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.Named;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author radval
 */
public class WSDLComponentNode extends AbstractNode {

    private WSDLComponent mComponent;
    
    private InstanceContent mLookupContents;
    
            
     public WSDLComponentNode(WSDLComponent component, 
                             Children children) {
        this(component, children, new InstanceContent());
    }
    
    public WSDLComponentNode(WSDLComponent component, 
                             Children children,
                             InstanceContent contents) {
        super(children, new AbstractLookup(contents));
        this.mComponent = component;
        this.mLookupContents = contents;

        // Add various objects to the lookup.
        // Keep this node and its cookie implementation at the top of the
        // lookup, as they provide cookies needed elsewhere, and we want
        // this node to provide them, not the currently selected node.
        contents.add(this);
        contents.add(new WSDLElementCookie(mComponent));
        // Include the data object in order for the Navigator to
        // show the structure of the current document.
        DataObject dobj = Util.getDataObject(mComponent);
        if (dobj != null) {
            contents.add(dobj);
        }
        contents.add(new DataObjectCookieDelegate(dobj));
        contents.add(mComponent);
        
         updateDisplayName();
    }

    public WSDLComponent getWSDLComponent() {
        return this.mComponent;
    }
    
    /**
     * Determines if this node represents a component that is contained
     * in a valid (non-null) model.
     *
     * @return  true if model is valid, false otherwise.
     */
    protected boolean isValid() {
        return mComponent.getModel() != null;
    }

    /**
     * Used by subclasses to update the display name as needed. The default
     * implementation updates the display name for named WSDL components.
     * Note, this method may be called from the constructor, so be sure to
     * avoid using member variables!
     */
    protected void updateDisplayName() {
        // Need a component connected to a model to work properly.
        if (isValid()) {
            // Automatically keep the name in sync for named components.
            if (mComponent instanceof Named) {
                String name = ((Named) mComponent).getName();
                // Prevent getting an NPE from ExplorerManager.
                super.setName(name == null ? "" : name);
                if (name == null || name.length() == 0) {
                    name = mComponent.getPeer().getLocalName();
                }
                setDisplayName(name);
            }
        }
    }
}
