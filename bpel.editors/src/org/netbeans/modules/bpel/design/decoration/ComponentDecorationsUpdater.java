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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.design.decoration;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.decoration.components.DecorationComponent;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.model.api.BpelEntity;

/**
 *
 * @author Alexey
 */
public class ComponentDecorationsUpdater {

    private DesignView designView;
    private ArrayList<Component> processComponents;
    private ArrayList<Component> consumerComponents;
    private ArrayList<Component> providerComponents;

    public ComponentDecorationsUpdater(DesignView designView) {
        this.designView = designView;
    }

    public void update() {
        processComponents = new ArrayList<Component>();
        consumerComponents = new ArrayList<Component>();
        providerComponents = new ArrayList<Component>();

        buildComponentsList(designView.getModel().getRootPattern());

        
        
        syncComponentsList(processComponents, designView.getProcessView());
        syncComponentsList(consumerComponents, designView.getConsumersView());
        syncComponentsList(providerComponents, designView.getProvidersView());

    }

    private static void syncComponentsList(ArrayList<Component> newList,
                                          DiagramView view) {
        Set<Component> oldSet = new HashSet<Component>();
        

        
        for (Component c : view.getComponents()) {
            if (c instanceof DecorationComponent){
                oldSet.add(c);
            }
        }

        for (Component c : newList) {
            if (!oldSet.remove(c)) {
                view.add(c);
            }

        }
        for (Component c : oldSet) {
            view.remove(c);
        }
    }

    private void buildComponentsList(Pattern pattern) {

        Decoration decoration = designView.getDecorationManager().getDecoration(pattern);
        //Add all components, decorating given bpelentity to set,

        DiagramView view = pattern.getView();
        
        ArrayList<Component> list = 
                (view == designView.getProcessView()) ? processComponents :
                (view == designView.getConsumersView()) ? consumerComponents :
                (view == designView.getProvidersView()) ? providerComponents : null;
        
        
        if (list != null // 128296 - nonvisual patterns hasn't got view
                && decoration != null && decoration.hasComponents()) 
        {
            for (Component c : decoration.getComponents()) {
                list.add(c);
            }

        }

        if (pattern instanceof CompositePattern) {
            //call recurision for all childs
            for (Pattern p : ((CompositePattern) pattern).getNestedPatterns()) {
                buildComponentsList(p);
            }
        }
    }


}
