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
package org.netbeans.modules.xml.xpath.ext.spi;

import org.netbeans.modules.xml.xpath.ext.schema.resolver.CastSchemaContext;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SimpleSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;

/**
 * Looks through the chan of SchemaContext elements and 
 * try finding the corresponding cast. 
 * 
 * @author nk160297
 */
public class SchemaContextBasedCastResolver implements XPathCastResolver {

    private XPathSchemaContext mSContext;
    
    public SchemaContextBasedCastResolver(XPathSchemaContext sContext) {
        mSContext = sContext;
    }
    
    public List<XPathCast> getXPathCasts() {
        List<XPathCast> castList = new ArrayList<XPathCast>();
        populateCastList(mSContext, castList);
        return castList;
    }

    public XPathCast getCast(XPathSchemaContext baseSContext) {
        return getCast(mSContext, baseSContext);
    }
  
    public List<XPathPseudoComp> getPseudoCompList(XPathSchemaContext parentSContext) {
        List<XPathPseudoComp> pseudoList = new ArrayList<XPathPseudoComp>();
        collectPseudoComps(mSContext, parentSContext, pseudoList);
        return pseudoList;
    }

    private XPathCast getCast(XPathSchemaContext lookInside, 
            XPathSchemaContext soughtSContext) {
        if (lookInside == null) {
            return null;
        }
        //
        if (lookInside instanceof CastSchemaContext) {
            CastSchemaContext castContext = (CastSchemaContext)lookInside;
            XPathSchemaContext baseSContext = castContext.getBaseContext();
            if (baseSContext != null && baseSContext.equalsChain(soughtSContext)) {
                XPathCast result = castContext.getTypeCast();
                return result;
            }
        } else {
            return getCast(lookInside.getParentContext(), soughtSContext);
        }
        //
        return null;
    }

    private void collectPseudoComps(XPathSchemaContext lookInside,
            XPathSchemaContext soughtSContext,
            List<XPathPseudoComp> pseudoList) {
        //
        if (lookInside == null) {
            return;
        }
        //
        if (lookInside instanceof SimpleSchemaContext) {
            SimpleSchemaContext simpleContext = (SimpleSchemaContext)lookInside;
            SchemaCompHolder scHolder = XPathSchemaContext.Utilities.
                    getSchemaCompHolder(simpleContext, false);
            if (scHolder.isPseudoComp()) {
                Object obj = scHolder.getHeldComponent();
                assert obj instanceof XPathPseudoComp;
                XPathPseudoComp pseudo = (XPathPseudoComp)obj;
                XPathSchemaContext pseudoContext = pseudo.getSchemaContext();
                if (pseudoContext != null &&
                        pseudoContext.equalsChain(soughtSContext)) {
                    pseudoList.add(pseudo);
                }
            }
        }
        //
        // Collect other pseudocomps.
        collectPseudoComps(lookInside.getParentContext(),soughtSContext, pseudoList);
    }

    private void populateCastList(XPathSchemaContext lookInside, 
            List<XPathCast> castList) {
        if (lookInside == null) {
            return;
        }
        //
        if (lookInside instanceof CastSchemaContext) {
            CastSchemaContext castContext = (CastSchemaContext)lookInside;
            XPathCast cast = castContext.getTypeCast();
            castList.add(cast);
        }
        populateCastList(lookInside.getParentContext(), castList);
    }

}
