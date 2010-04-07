/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.search.impl.core;

import java.util.List;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;

import org.netbeans.modules.xml.search.api.SearchException;
import org.netbeans.modules.xml.search.api.SearchOption;
import org.netbeans.modules.xml.search.api.SearchTarget;
import org.netbeans.modules.xml.search.spi.SearchEngine;
import org.netbeans.modules.xml.search.spi.SearchProvider;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.13
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.search.spi.SearchEngine.class)
public final class Engine extends SearchEngine.Adapter {

    public void search(SearchOption option) throws SearchException {
        myProvider = option.getProvider();
        SearchTarget target = option.getTarget();

        if (target == null) {
            myClazz = null;
        } else {
            myClazz = target.getClazz();
        }
//out();
        fireSearchStarted(option);
        search(myProvider.getRoot(), ""); // NOI18N
        fireSearchFinished(option);
    }

    private void search(Object object, String indent) {
//out(indent + " !!!: " + object);
        if (!(object instanceof Component)) {
            return;
        }
        Component component = (Component) object;
        process(component, indent);
        List children = component.getChildren();
//out(indent + " LST: " + children.size());

        for (Object child : children) {
            search(child, indent + "    "); // NOI18N
        }
    }

    private void process(Component component, String indent) {
//out(indent + " see: " + component);
        if (checkClazz(component) && checkName(component)) {
//out(indent + "      add.");
            fireSearchFound(myProvider.getElement(component));
        }
    }

    private boolean checkClazz(Object object) {
        if (myClazz == null) {
            return true;
        }
        return myClazz.isAssignableFrom(object.getClass());
    }

    private boolean checkName(Component component) {
        String name = ""; // NOI18N

        if (component instanceof Named) {
            name = ((Named) component).getName();
    
            if (name == null) {
                name = ""; // NOI18N
            }
        }
        return accepts(name);
    }

    public boolean isApplicable(Object root) {
        return root instanceof Component;
    }

    public String getDisplayName() {
        return i18n(Engine.class, "LBL_Engine_Display_Name"); // NOI18N
    }

    public String getShortDescription() {
        return i18n(Engine.class, "LBL_Engine_Short_Description"); // NOI18N
    }

    private SearchProvider myProvider;
    private Class<? extends Object> myClazz;
}
