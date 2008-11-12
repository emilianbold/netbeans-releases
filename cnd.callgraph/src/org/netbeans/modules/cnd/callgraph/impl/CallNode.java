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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.callgraph.impl;

import org.netbeans.modules.cnd.callgraph.api.*;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class CallNode extends AbstractNode {
    private Call object;
    private CallGraphState model;
    private boolean isCalls;

    public CallNode(Call element, CallGraphState model, boolean isCalls) {
        super(new CallChildren(element, model, isCalls));
        object = element;
        this.model = model;
        this.isCalls = isCalls;
        if (isCalls) {
            setName(element.getCallee().getName());
        } else {
            setName(element.getCaller().getName());
        }
        ((CallChildren)getChildren()).setParent(this);
        model.addCallToScene(element);
    }

    @Override
    public String getHtmlDisplayName() {
        String displayName;
        if (isCalls) {
            displayName = object.getCallee().getHtmlDisplayName();
        } else {
            displayName = object.getCaller().getHtmlDisplayName();
        }
        if (((CallChildren)getChildren()).isRecusion()) {
            return displayName+NbBundle.getMessage(CallNode.class, "CTL_Recuesion"); // NOI18N
        } else {
            return displayName;
        }
    }
    
    @Override
    public Image getIcon(int param) {
        Image res = null;
        if (isCalls) {
            res = object.getCallee().getIcon();
        } else {
            res = object.getCaller().getIcon();
        }
        if (res == null){
            res = super.getIcon(param);
        }
        return mergeBadge(res);
    }

    private Image mergeBadge(Image original) {
        if (isCalls) {
            return ImageUtilities.mergeImages(original, downBadge, 0, 0);
        } else {
            Image res = ImageUtilities.mergeImages(emptyBadge, original, 4, 0);
            return ImageUtilities.mergeImages(res, upBadge, 0, 0);
        }
    }

    /*package-local*/ Call getCall(){
        return object;
    }

    @Override
    public Image getOpenedIcon(int param) {
        return getIcon(param);
    }
    
    @Override
    public Action getPreferredAction() {
        return new GoToReferenceAction(object);
    }

    @Override
    public Action[] getActions(boolean context) {
        ArrayList<Action> actions = new ArrayList<Action>();
        Action action = getPreferredAction();
        if (action != null){
            actions.add(action);
            actions.add(new GoToReferenceAction(object.getCaller(), 1));
            actions.add(new GoToReferenceAction(object.getCallee(), 2));
            actions.add(null);
        }
        for(Action a:model.getActions()) {
            actions.add(a);
        }
        return actions.toArray(new Action[actions.size()]);
    }

    /*package-local*/ static Image downBadge = ImageUtilities.loadImage( "org/netbeans/modules/cnd/callgraph/resources/down_20.png" ); // NOI18N
    private static Image upBadge = ImageUtilities.loadImage( "org/netbeans/modules/cnd/callgraph/resources/up_8.png" ); // NOI18N
    private static Image emptyBadge = ImageUtilities.loadImage( "org/netbeans/modules/cnd/callgraph/resources/empty_20.png" ); // NOI18N
}