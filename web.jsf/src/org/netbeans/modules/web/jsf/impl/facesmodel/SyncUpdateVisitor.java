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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import org.netbeans.modules.web.jsf.api.facesmodel.*;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;

/**
 *
 * @author Petr Pisl
 */
public class SyncUpdateVisitor extends JSFConfigVisitor.Default implements ComponentUpdater<JSFConfigComponent>{
    
    private JSFConfigComponent target;
    private Operation operation;
    private int index;
    
    /** Creates a new instance of SyncUpdateVisitor */
    public SyncUpdateVisitor() {
    }
    

    public void update(JSFConfigComponent target, JSFConfigComponent child, Operation operation) {
        update(target, child, -1 , operation);
    }

    public void update(JSFConfigComponent target, JSFConfigComponent child, int index, Operation operation) {
        assert target != null;
        assert child != null;
        this.target = target;
        this.index = index;
        this.operation = operation;
        child.accept(this);
    }

    private void insert(String propertyName, JSFConfigComponent component) {
        ((JSFConfigComponentImpl)target).insertAtIndex(propertyName, component, index);
    }
    
    private void remove(String propertyName, JSFConfigComponent component) {
        ((JSFConfigComponentImpl)target).removeChild(propertyName, component);
    }
    
    @Override
    public void visit(ManagedBean component){
        if (target instanceof FacesConfig) {
            if (operation == Operation.ADD) {
                insert(FacesConfig.MANAGED_BEAN, component);
            } else {
                remove(FacesConfig.MANAGED_BEAN, component);
            }
        }
    }
    
    @Override
    public void visit(NavigationRule component){
        if (target instanceof FacesConfig) {
            if (operation == Operation.ADD) {
                insert(FacesConfig.NAVIGATION_RULE, component);
            } else {
                remove(FacesConfig.NAVIGATION_RULE, component);
            }
        }
    }
    
    @Override
    public void visit(NavigationCase component){
        if (target instanceof NavigationRule) {
            if (operation == Operation.ADD) {
                insert(NavigationRule.NAVIGATION_CASE, component);
            } else {
                remove(NavigationRule.NAVIGATION_CASE, component);
            }
        }
    }
    
    @Override
    public void visit(Converter component){
        if (target instanceof FacesConfig) {
            if (operation == Operation.ADD) {
                insert(FacesConfig.CONVERTER, component);
            } else {
                remove(FacesConfig.CONVERTER, component);
            }
        }
    }

    @Override
    public void visit(Application component) {
        if (target instanceof FacesConfig) {
            if (operation == Operation.ADD) {
                insert(FacesConfig.APPLICATION, component);
            } else {
                remove(FacesConfig.APPLICATION, component);
            }
        }
    }

    @Override
    public void visit(ViewHandler component) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.VIEW_HANDLER, component);
            } else {
                remove(Application.VIEW_HANDLER, component);
            }
        }
    }

    @Override
    public void visit(LocaleConfig component) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.LOCALE_CONFIG, component);
            } else {
                remove(Application.LOCALE_CONFIG, component);
            }
        }
    }

    @Override
    public void visit(DefaultLocale component) {
        if (target instanceof LocaleConfig) {
            if (operation == Operation.ADD) {
                insert(LocaleConfig.DEFAULT_LOCALE, component);
            } else {
                remove(LocaleConfig.DEFAULT_LOCALE, component);
            }
        }
    }
    
    @Override
    public void visit(SupportedLocale component) {
        if (target instanceof LocaleConfig) {
            if (operation == Operation.ADD) {
                insert(LocaleConfig.SUPPORTED_LOCALE, component);
            } else {
                remove(LocaleConfig.SUPPORTED_LOCALE, component);
            }
        }
    }
    
    @Override
    public void visit(ResourceBundle component) {
        if (target instanceof Application) {
            if (operation == Operation.ADD) {
                insert(Application.RESOURCE_BUNDLE, component);
            } else {
                remove(Application.RESOURCE_BUNDLE, component);
            }
        }
    }
}
