/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.soa.xpath.mapper.lsm;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 * This Cast Resolver is based on a list of MapperCastingExtension. 
 * 
 * @author Nikita Krjukov
 */
public class MapperXPathCastResolver implements XPathCastResolver {

    private List<XPathCast> mXPathCastList;
    private List<XPathPseudoComp> mXPathPseudoCompList;

    public MapperXPathCastResolver(DirectedList<? extends MapperLsm> location) {
        mXPathCastList = new ArrayList<XPathCast>();
        mXPathPseudoCompList = new ArrayList<XPathPseudoComp>();
        //
        for (MapperLsm mce : location) {
            if (mce instanceof XPathCast) {
                mXPathCastList.add((XPathCast)mce);
            } else if (mce instanceof XPathPseudoComp) {
                mXPathPseudoCompList.add((XPathPseudoComp)mce);
            }
        }
    }

    public List<XPathCast> getXPathCasts() {
        return mXPathCastList;
    }

    public XPathCast getCast(XPathSchemaContext soughtContext) {
        if (mXPathCastList == null || mXPathCastList.size() == 0) {
            return null;
        }
        //
        for (XPathCast xPathCast : mXPathCastList) {
            XPathSchemaContext sContext = xPathCast.getSchemaContext();
            if (sContext != null && sContext.equalsChain(soughtContext)) {
                return xPathCast;
            }
        }
        //
        return null;
    }

    public List<XPathPseudoComp> getPseudoCompList(XPathSchemaContext parentSContext) {
        //
        if (mXPathPseudoCompList == null || mXPathPseudoCompList.size() == 0) {
            return null;
        }
        //
        ArrayList<XPathPseudoComp> result = new ArrayList<XPathPseudoComp>();
        // 
        for (XPathPseudoComp xPseudoComp : mXPathPseudoCompList) {
            //
            // Filter by location
            XPathSchemaContext sContext = xPseudoComp.getSchemaContext();
            if (sContext != null && sContext.equalsChain(parentSContext)) {
                result.add(xPseudoComp);
            }
        }
        //
        return result;
    }
    
}

