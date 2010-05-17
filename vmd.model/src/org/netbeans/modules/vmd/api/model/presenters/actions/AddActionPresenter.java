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

package org.netbeans.modules.vmd.api.model.presenters.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Action;

import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public abstract class AddActionPresenter extends Presenter {

    public static final String ADD_ACTION = NbBundle.getMessage(AddActionPresenter.class, "NAME_AddActionPresenter"); //NOI18N

    // TODO - move this method to a ActionsPresenterFactory class and hide this class
    public static final Presenter create(Integer order, TypeID... types) {
        return create(null, order, types);
    }

    // TODO - move this method to a ActionsPresenterFactory class and hide this class
    public final static Presenter create(final String name, final int order,final TypeID... types) {
        
        return new AddActionPresenter() {
            public Integer getOrder() {
                return order;
            }
            
            public String getName(){
                return name;
            }
            
            public AddActionItem[] getAddActionItems() {
                List<Action> actions = new ArrayList<Action>();
                DesignDocument document = getComponent().getDocument();
                for (TypeID type : types) {
                    for (ComponentDescriptor descriptor : document.getDescriptorRegistry().getComponentDescriptors()){
                        if (getComponent().getDocument().getDescriptorRegistry().isInHierarchy(type, descriptor.getTypeDescriptor().getThisType())) {
                            for (ComponentProducer producer : document.getDescriptorRegistry().getComponentProducers()) {
                                if (producer.getMainComponentTypeID ().equals(descriptor.getTypeDescriptor().getThisType()) && AcceptSupport.isAcceptable(getComponent(), producer, null)) {
                                    Boolean isValid = producer.checkValidity(document, true);
                                    if (isValid != null && isValid)
                                        actions.add(AddActionItem.getInstance(getComponent(), producer));
                                }
                            }
                        }
                    }
                }
                Collections.sort(actions, ACTIONS_COMPARATOR);
                return actions.toArray( new AddActionItem[actions.size()]);
            }
            
        };
    }
    
    public abstract Integer getOrder();
    
    public abstract String getName();
    
    public abstract AddActionItem[] getAddActionItems();

    // TODO - move to ActionsPresenterFactory or ActionsSupport class
    protected static final Comparator<Action> ACTIONS_COMPARATOR = new Comparator<Action>() {
        public int compare(Action s1, Action s2) {
            String name1 = s1.getValue(Action.NAME).toString();
            String name2 = s2.getValue(Action.NAME).toString();
            
            return name1.compareTo(name2);
        }
    };
    
}
