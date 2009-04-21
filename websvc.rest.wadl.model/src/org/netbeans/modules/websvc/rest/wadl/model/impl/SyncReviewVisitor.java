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
package org.netbeans.modules.websvc.rest.wadl.model.impl;

import java.io.IOException;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory;
import org.netbeans.modules.websvc.rest.wadl.model.visitor.DefaultVisitor;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.SyncUnit;
import org.w3c.dom.Element;

/**
 *
 * @author Ayub Khan
 */
public class SyncReviewVisitor extends DefaultVisitor {

    private SyncUnit unit;

    /** Creates a new instance of SyncUnitReviewVisistor */
    public SyncReviewVisitor() {
    }

    SyncUnit review(SyncUnit toReview) {
        this.unit = toReview;
        if (unit.getTarget() instanceof WadlComponent) {
            ((WadlComponent)unit.getTarget()).accept(this);
        }
        return unit;
    }

    @Override
    public void visit(Doc target) {
        review(target, WadlQNames.DOC.getQName());
    }

    @Override
    public void visit(Application target) {
        review(target, WadlQNames.APPLICATION.getQName());
    }

    @Override
    public void visit(Grammars target) {
        review(target, WadlQNames.GRAMMARS.getQName());
    }

    @Override
    public void visit(Resources target) {
        review(target, WadlQNames.RESOURCES.getQName());
    }

    @Override
    public void visit(Resource target) {
        review(target, WadlQNames.RESOURCE.getQName());
    }

    @Override
    public void visit(ResourceType target) {
        review(target, WadlQNames.RESOURCE_TYPE.getQName());
    }

    @Override
    public void visit(Method target) {
        review(target, WadlQNames.METHOD.getQName());
    }

    @Override
    public void visit(Request target) {
        review(target, WadlQNames.REQUEST.getQName());
    }

    @Override
    public void visit(Response target) {
        review(target, WadlQNames.RESPONSE.getQName());
    }

    @Override
    public void visit(Param target) {
        review(target, WadlQNames.PARAM.getQName());
    }

    @Override
    public void visit(Option target) {
        review(target, WadlQNames.OPTION.getQName());
    }

    @Override
    public void visit(Link target) {
        review(target, WadlQNames.LINK.getQName());
    }

    @Override
    public void visit(Include target) {
        review(target, WadlQNames.INCLUDE.getQName());
    }

    @Override
    public void visit(Representation target) {
        review(target, WadlQNames.REPRESENTATION.getQName());
    }

    @Override
    public void visit(Fault target) {
        review(target, WadlQNames.FAULT.getQName());
    }

    private void review(WadlComponent target, QName name) {
        if (unit.getToAddList().size() > 0 || unit.getToRemoveList().size() > 0) {
            SyncUnit reviewed = new SyncUnit(target.getParent());
            ChangeInfo change = unit.getLastChange();
            Element peer = change.getParent();
            change.markParentAsChanged();
            change.setParentComponent(target.getParent());
            reviewed.addChange(change);
            reviewed.addToRemoveList(target);
            reviewed.addToAddList(create(target.getParent(), peer, name));
            unit = reviewed;
        }
    }

    private WadlComponent create(WadlComponent parent, Element e, QName name) {
        ElementFactory factory = ElementFactoryRegistry.getDefault().get(name);
        WadlComponent component = factory.create(parent, e);
        if (component == null) {
            throw new IllegalArgumentException(new IOException("Cannot create Wadl component: " + name.getLocalPart()));
        }
        return component;
    }
}
