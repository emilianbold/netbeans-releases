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

package org.netbeans.modules.soa.mapper.common.palette;

import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.loaders.InstanceDataObject;
import org.openide.nodes.Node;

/**
 * The PaletteItem encapsulate all functoids that can be used as components in
 * the collaboration editor
 *
 *
 * @author Tientien Li
 */
public class PaletteItem {

    public static final String ATTR_IS_CONTAINER = "isContainer";    // NOI18N

    // Global class variables

    /** Weak reference to shared instance of the JavaBean */
    private java.lang.ref.WeakReference sharedReference = null;

    /** The JavaBean Class represented by this PaletteItem */
    private boolean beanClassFailed;

    /** Field itemNode           */
    private Node itemNode;

    /** Field instanceCookie           */
    private InstanceCookie instanceCookie;

    /** Field instanceDO           */
    private InstanceDataObject instanceDO;

    /**
     * Creates a new PaletteItem
     *
     * @param node the palette item node
     * @throws InstantiationException if the instance cookie is missing
     */
    public PaletteItem(Node node)
        throws InstantiationException {

        itemNode = node;

        InstanceCookie ic =
            (InstanceDataObject) itemNode.getCookie(InstanceDataObject.class);

        if (ic != null) {
            instanceDO = (InstanceDataObject) ic;
        } else {
            ic = (InstanceCookie) itemNode.getCookie(InstanceCookie.class);

            if (ic == null) {
                throw new InstantiationException();
            }
        }

        instanceCookie = ic;
    }

    // Class Methods

    /**
     * get the palette Item Node
     *
     *
     * @return the palette item node
     *
     */
    public Node getItemNode() {
        return itemNode;
    }

    /**
     * get the parent palette Category Node
     *
     *
     * @return the parent palette category node
     *
     */
    public Node getCategoryNode() {
        return itemNode.getParentNode();
    }

    /**
     * get the palette item Name
     *
     *
     * @return the palette item name
     *
     */
    public String getName() {

        if (itemNode instanceof PaletteItemNode) {
            String expName =
                ((PaletteItemNode) itemNode).getExplicitDisplayName();

            if (expName != null) {
                return expName;
            }
        }

        if (instanceDO != null) {
            String name = instanceCookie.instanceName();
            int    i    = name.lastIndexOf('.');

            if (i >= 0) {
                name = name.substring(i + 1);
            }

            return name;
        } else {
            return itemNode.getName();
        }
    }

    /**
     * get the palette item Display Name
     *
     *
     * @return  the palette item display name
     *
     */
    public String getDisplayName() {

        if (itemNode instanceof PaletteItemNode) {
            String expName =
                ((PaletteItemNode) itemNode).getExplicitDisplayName();

            if (expName != null) {
                return expName;
            }
        }

        return (instanceDO != null)
               ? instanceDO.instanceName()
               : itemNode.getName();
    }

    /**
     * get the palette item instance Cookie
     *
     *
     * @return the palette item instance Cookie
     *
     */
    public InstanceCookie getInstanceCookie() {
        return instanceCookie;
    }

    /**
     * compare the two palette items
     *
     *
     * @param obj the palette item to be checked
     *
     * @return true if the two items are equal
     *
     */
    public boolean equals(Object obj) {

        if (!(obj instanceof PaletteItem)) {
            return false;
        }

        PaletteItem item = (PaletteItem) obj;

        /*
        if (getBeanClass() != item.getBeanClass())
            return false;
        */
        if ((instanceDO != null) && (item.instanceDO != null)) {
            return true;
        }

        DataObject do1 = (DataObject) itemNode.getCookie(DataObject.class);
        DataObject do2 =
            (DataObject) item.itemNode.getCookie(DataObject.class);

        if (!(do1 instanceof DataShadow) || !(do2 instanceof DataShadow)) {
            return false;
        }

        return ((DataShadow) do1).getOriginal()
               == ((DataShadow) do2).getOriginal();
    }
}
