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


package org.netbeans.modules.visualweb.api.designtime.idebridge;


import com.sun.rave.designtime.DesignBean;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * Provides access to the <code>DesigntimeService</code> impl.
 *
 * @author Peter Zavadsky
 */
public final class DesigntimeIdeBridgeProvider {

    /** Creates a new instance of DesigntimeServiceProvider */
    private DesigntimeIdeBridgeProvider() {
    }


    /** Looks for the <code>DesigntimeIdeBridge</code> service in the globl lookup,
     * <code>Lookup.getDefault</code> and provides it. In case there
     * is no instance found, provides a dummy (no-op) impl. */
    public static DesigntimeIdeBridge getDefault() {
        DesigntimeIdeBridge designtimeService = (DesigntimeIdeBridge)Lookup.getDefault().
                lookup(DesigntimeIdeBridge.class);
        if(designtimeService == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new NullPointerException("No DesigntimeIdeBridge registered! Providing a dummy impl.")); // NOI18N

            designtimeService = new DummyDesigntimeService();
        }
        return designtimeService;
    }


    /** Dummy implementation of the service, as a fallback. */
    private static class DummyDesigntimeService implements DesigntimeIdeBridge {

        public Node getNodeRepresentation(DesignBean designBean) {
            return new BrokenNode(designBean);
        }
    } // End of DummyDesigntimeService.


    private static class BrokenNode extends AbstractNode {

        public BrokenNode(DesignBean designBean) {
            super(Children.LEAF);

            setDisplayName(NbBundle.getMessage(DesigntimeIdeBridgeProvider.class, "LBL_BrokenNode", designBean.getInstanceName()));
        }
    } // End of BrokenNode.
}
