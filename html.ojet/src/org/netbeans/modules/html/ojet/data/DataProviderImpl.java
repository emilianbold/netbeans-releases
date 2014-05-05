/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.ojet.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.html.ojet.OJETUtils;

/**
 *
 * @author Petr Pisl
 */
public class DataProviderImpl extends DataProvider {

    private static DataProviderImpl instance = null;
    
    synchronized public static DataProvider getInstance() {
        if (instance == null) {
            instance = new DataProviderImpl();
        }
        return instance;
    }
    
    @Override
    public Collection<DataItem> getBindingOptions() {
        List<DataItem> result = new ArrayList(1);
        result.add(new DataItemImpl(OJETUtils.OJ_COMPONENT, null, null));
        return result;
    }

    @Override
    public Collection<DataItem> getComponents() {
        List<DataItem> result = new ArrayList<>();
        result.add(new DataItemImpl("ojButton", "Help for ojButton", null));
        result.add(new DataItemImpl("ojTab", "Help for ojTab", null));
        result.add(new DataItemImpl("ojButtonset", "Help for ojButtonset", null));
        return result;
    }

    @Override
    public Collection<DataItem> getComponentOptions(String compName) {
        List<DataItem> result = new ArrayList<>();
        switch (compName) {
            case "ojButton":
                result.add(new DataItemImpl("contextMenu", null, null));
                result.add(new DataItemImpl("disabled", null, null));
                result.add(new DataItemImpl("display", null, null));
                result.add(new DataItemImpl("icons", null, null));
                result.add(new DataItemImpl("label", null, null));
                result.add(new DataItemImpl("menu", null, null));
                result.add(new DataItemImpl("rootAttributes", null, null));
                break;
            case "ojButtonset":
                result.add(new DataItemImpl("contextMenu", null, null));
                result.add(new DataItemImpl("disabled", null, null));
                result.add(new DataItemImpl("checked", null, null));
                result.add(new DataItemImpl("focusManagement", null, null));
                result.add(new DataItemImpl("rootAttributes", null, null));
                break;
        }
        
        return result;
    }
}
