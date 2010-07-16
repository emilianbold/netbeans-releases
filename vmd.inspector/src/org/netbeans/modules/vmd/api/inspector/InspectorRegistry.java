/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.vmd.api.inspector;

import java.util.Collection;
import java.util.Collections;
import java.util.WeakHashMap;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.inspector.InspectorManagerView;
import org.netbeans.modules.vmd.inspector.InspectorWrapperTree;
import org.openide.util.WeakSet;

/**
 *
 * @author Karol Harezlak
 */
/**
 * This class store information about DesigneComponents to update in the Mobility Visual Designer Navigator.
 * When registry contains any DesignComponents then scans all DesignComponents for
 * folders attached to the added DesignComponents through InspectorFolderPresenter and
 * updating them in the structure of Navigator. After end of the update registry all DesignComponents are removed from registry.
 * Mobility Visual Designer Navigator updates its self automatically on following events:
 * <ul>
 * <li>DesignComponent removed from the DesignDocument</li>
 * <li>DesignComponent added to the DesignDocument</li>
 * <li>DesignComponent chenged in the DesignDocument</li>
 * <li>DesignComponent added to the DesignDocument</li>
 * </ul>
 * If it's necessary to update some DesignComponents in the Navigator at some 
 * other event or any other reason, simply add necessary DesignComponents to this registry
 */
public final class InspectorRegistry {

    private static WeakHashMap<DesignDocument, InspectorRegistry> registryMap;
    private Collection<DesignComponent> components = new WeakSet<DesignComponent>();
    
    /**
     * Returns instance of InspectorRegistry for given DesignDocument.
     * @param DesignDocument 
     */ 
    public static InspectorRegistry getInstance(DesignDocument document) {
        if (registryMap == null) {
            registryMap = new WeakHashMap<DesignDocument, InspectorRegistry>();
        }
        if (registryMap.get(document) == null) {
            registryMap.put(document, new InspectorRegistry());
        }
        return registryMap.get(document);
    }

    private InspectorRegistry() {
    }

    /**
     * Add DesignComponent to update in the Mobility Designer Navigator.
     * @param DesignComponent component to update
     */
    public void addComponent(DesignComponent component) {
        components.add(component);
    }

    /**
     * Returns Collection of DesignComponents to update.
     * @return Collection of DesignComponents to update or Collections.EMTY_SET when registry is empty
     */
    @SuppressWarnings(value = "unchecked") // NOI18N
    public Collection<DesignComponent> getComponentsToUpdate() {
        if (components == null) {
            return Collections.EMPTY_SET;
        }
        return Collections.unmodifiableCollection(components);
    }

    /**
     * DO NOT USE THIS METHOD. This method is only accessible from class InspectorWrapperTree.
     */
    public void cleanUpRegistry() {
        if (!Debug.isFriend(InspectorManagerView.class)) {
            throw new IllegalStateException("This method is only accessible from class InspectorWrapperTree"); //NOI18N
        }
        if (components != null) {
            components.clear();
        }
    }
    
     /**
     * DO NOT USE THIS METHOD. This method is only accessible from class InspectorWrapperTree.
     */
    public void remove(Collection<DesignComponent> components) {
        if (!Debug.isFriend(InspectorWrapperTree.class)) {
            throw new IllegalStateException("This method is only accessible from class InspectorWrapperTree"); //NOI18N
        }
        if ((this.components != null && this.components.size() > 0) && (components != null && components.size() == 0)) {
            this.components.removeAll(components);
        }
    }
}
