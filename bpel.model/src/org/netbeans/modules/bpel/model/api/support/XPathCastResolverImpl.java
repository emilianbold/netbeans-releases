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
package org.netbeans.modules.bpel.model.api.support;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.openide.ErrorManager;

/**
 * @author nk160297
 * @author Vladimir Yaroslavskiy
 * @version 2008.03.27
 */
public class XPathCastResolverImpl implements XPathCastResolver {

    private List<XPathCast> mXPathCasts;
    private List<XPathPseudoComp> mXPathPseudoCompList;

    public XPathCastResolverImpl(List<Cast> casts, List<PseudoComp> pseudoComps) {
        mXPathCasts = new ArrayList<XPathCast>();
        //
        for (Cast cast : casts) {
            XPathCastImpl xPathCast = XPathCastImpl.convert(cast);
            if (xPathCast != null) {
                mXPathCasts.add(xPathCast);
            } else {
                String msg = "An error while processing the cast: path=\"" + 
                        cast.getPath() + "\" castTo=\"" + 
                        cast.getType() + "\"";
                ErrorManager.getDefault().log(ErrorManager.WARNING, msg );
            }
        }
        //
        mXPathPseudoCompList = new ArrayList<XPathPseudoComp>();
        //
        for (PseudoComp pseudoComp : pseudoComps) {
            XPathPseudoCompImpl xPathPseudoComp = XPathPseudoCompImpl.convert(pseudoComp);
            if (xPathPseudoComp != null) {
                mXPathPseudoCompList.add(xPathPseudoComp);
            } else {
                String msg = "An error while processing the pseudo schema component: " +
                        "parentPath=\"" + pseudoComp.getParentPath() + "\" " +
                        "name=\"" + pseudoComp.getName() + "\"";
                ErrorManager.getDefault().log(ErrorManager.WARNING, msg );
            }
        }
    }

    public List<XPathCast> getXPathCasts() {
        return mXPathCasts;
    }

    public XPathCast getCast(XPathSchemaContext soughtContext) {
        if (mXPathCasts == null || mXPathCasts.size() == 0) {
            return null;
        }
        //
        for (XPathCast xPathCast : mXPathCasts) {
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
