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

package org.netbeans.modules.xslt.mapper.methoid;

import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.common.IMapperEvent;
import org.netbeans.modules.soa.mapper.common.IMapperGroupNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdaterFactory;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.openide.util.NbBundle;


/**
 * Constructs literal updaters for different literal types.
 *
 * @author nk160297
 */
public class LiteralUpdaterFactory implements ILiteralUpdaterFactory {
    
    private StringLiteralUpdater mStringUpdater;
    private NumericLiteralUpdater mNumericUpdater;
    private XPathLiteralUpdater mXPathUpdater;
    private IBasicMapper mMapper;
    private AbstractLiteralUpdater.XPathNodeExpressionUpdater mXPathNodeExpressionUpdater;
    
    public LiteralUpdaterFactory(IBasicMapper mapper) {
        mMapper = mapper;
    }
    
    public ILiteralUpdater getStringUpdater() {
        if (mStringUpdater == null) {
            mStringUpdater = new StringLiteralUpdater();
            mStringUpdater.setXPathProcessor(getExpressionUpdater());
        }
        return mStringUpdater;
    }
    
    public ILiteralUpdater getNumericUpdater() {
         if (mNumericUpdater == null) {
            mNumericUpdater = new NumericLiteralUpdater();
            mNumericUpdater.setXPathProcessor(getExpressionUpdater());
        }
        return mNumericUpdater;
    }
    
    public ILiteralUpdater getXPathUpdater() {
        if (mXPathUpdater == null) {
            mXPathUpdater = new XPathLiteralUpdater();
            mXPathUpdater.setXPathProcessor(getExpressionUpdater());
        }
        return mXPathUpdater;
    }
    
    private AbstractLiteralUpdater.XPathNodeExpressionUpdater getExpressionUpdater() {
        if (mXPathNodeExpressionUpdater == null) {
            mXPathNodeExpressionUpdater = new AbstractLiteralUpdater.XPathNodeExpressionUpdater() {
                public void updateNodeExpression(IFieldNode sourceFieldNode) {
                    IMapperGroupNode groupNode = sourceFieldNode.getGroupNode();
                    mMapper.getMapperViewManager().postMapperEvent(
                            MapperUtilities.getMapperEvent(
                            this,
                            groupNode,
                            IMapperEvent.REQ_UPDATE_NODE,
                            "LITERAL UPDATED"));   // NOI18N
                           
                }
            };
        }
        return mXPathNodeExpressionUpdater;
    }
    
    /**
     * Returns the literal updater corresponding to the specified type.
     */
    public ILiteralUpdater createLiteralUpdater(String type) {
        Constants.LiteralType typeObj = Constants.LiteralType.findByName(type);
        return createLiteralUpdater(typeObj);
    }
    
    public ILiteralUpdater createLiteralUpdater(Constants.LiteralType type) {
        if (type == null) {
            return null;
        }
        ILiteralUpdater updater = null;
        switch (type) {
            case NUMBER_LITERAL_TYPE:
                updater = getNumericUpdater();
                break;
            case STRING_LITERAL_TYPE:
                updater = getStringUpdater();
                break;
            case XPATH_LITERAL_TYPE:
                updater = getXPathUpdater();
                break;
            default:
                break;
        }
        return updater;
    }
}
