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
package org.netbeans.modules.web.beans.xml.impl;

import org.netbeans.modules.web.beans.xml.Beans;
import org.netbeans.modules.web.beans.xml.Deploy;
import org.netbeans.modules.web.beans.xml.Type;
import org.netbeans.modules.web.beans.xml.WebBeansComponent;
import org.netbeans.modules.web.beans.xml.WebBeansVisitor;
import org.netbeans.modules.xml.xam.ComponentUpdater;


/**
 * @author ads
 *
 */
class SyncUpdateVisitor implements ComponentUpdater<WebBeansComponent>, WebBeansVisitor {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.web.beans.xml.Beans)
     */
    public void visit( Beans beans ) {
        assert false : "Should never add or remove beans root"; // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.web.beans.xml.Deploy)
     */
    public void visit( Deploy deploy ) {
        assert getParent() instanceof Beans;
        Beans beans = (Beans)getParent();
        if ( isAdd() ){
            beans.addElement( getIndex(), deploy );
        }
        else if ( isRemove() ){
            beans.removeElement( deploy );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.WebBeansVisitor#visit(org.netbeans.modules.web.beans.xml.Type)
     */
    public void visit( Type type ) {
        assert getParent() instanceof Deploy;
        Deploy deploy = (Deploy)getParent();
        if ( isAdd() ){
            deploy.addType( getIndex(), type);
        }
        else if ( isRemove() ){
            deploy.removeType(type);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.ComponentUpdater#update(org.netbeans.modules.xml.xam.Component, org.netbeans.modules.xml.xam.Component, org.netbeans.modules.xml.xam.ComponentUpdater.Operation)
     */
    public void update( WebBeansComponent target, WebBeansComponent child,
            Operation operation )
    {
        update(target, child, -1 , operation);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.ComponentUpdater#update(org.netbeans.modules.xml.xam.Component, org.netbeans.modules.xml.xam.Component, int, org.netbeans.modules.xml.xam.ComponentUpdater.Operation)
     */
    public void update( WebBeansComponent target, WebBeansComponent child,
            int index, Operation operation )
    {
        assert target != null;
        assert child != null;
        assert operation == null || operation == Operation.ADD ||
            operation == Operation.REMOVE;

        myParent = target;
        myIndex = index;
        myOperation = operation;
        child.accept(this);
    }

    private boolean isAdd() {
        return getOperation() == Operation.ADD;
    }

    private boolean isRemove() {
        return getOperation() == Operation.REMOVE;
    }

    private WebBeansComponent getParent() {
        return myParent;
    }

    private int getIndex() {
        return myIndex;
    }

    private Operation getOperation() {
        return myOperation;
    }

    private WebBeansComponent myParent;

    private int myIndex;

    private Operation myOperation;
}
