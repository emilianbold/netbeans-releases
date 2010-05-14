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
package org.netbeans.modules.soa.xpath.mapper.palette;

import javax.swing.Icon;

import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;

import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
//import org.netbeans.modules.soa.validation.util.DeadlineDialog;
//import org.netbeans.modules.soa.validation.util.DurationDialog;
import org.netbeans.modules.soa.xpath.mapper.model.builder.VertexFactory;

import static org.netbeans.modules.soa.xpath.mapper.utils.XPathMapperUtils.*;

/**
 * @author Vladimir Yaroslavskiy
 * @author Nikita Krjukov
 * @version 2009.09.17
 */
public class DefaultItemHandler implements ItemHandler {

    public DefaultItemHandler(CoreFunctionType type) {
        this(type.getMetadata().getDisplayName(), type.getMetadata().getIcon(), type);
    }

    public DefaultItemHandler(CoreOperationType type) {
        this(type.getMetadata().getDisplayName(), type.getMetadata().getIcon(), type);
    }

    public DefaultItemHandler(String string) {
        this(i18n(DefaultItemHandler.class, "LBL_String_Literal"), icon(DefaultItemHandler.class, "string"), string); // NOI18N
    }

    public DefaultItemHandler(Number number) {
        this(i18n(DefaultItemHandler.class, "LBL_Numeric_Literal"), icon(DefaultItemHandler.class, "numeric"), number); // NOI18N
    }

    public DefaultItemHandler(ExtFunctionMetadata data) {
        this(data.getDisplayName(), data.getIcon(), data);
    }

    protected DefaultItemHandler(String name, Icon icon, Object object) {
        myName = name;
        myIcon = icon;
        myObject = object;
    }

    public Icon getIcon() {
        return myIcon;
    }

    public String getDisplayName() {
        return myName;
    }

    public boolean canAddGraphSubset() {
        return true;
    }

    public GraphSubset createGraphSubset() {
        return VertexFactory.getInstance().createGraphSubset(myObject);
    }

    protected void setObject(Object object) {
        myObject = object;
    }

    private Icon myIcon;
    private String myName;
    protected Object myObject;
}
