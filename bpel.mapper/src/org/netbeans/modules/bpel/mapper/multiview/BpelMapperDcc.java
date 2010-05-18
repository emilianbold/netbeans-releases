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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.mapper.multiview;

import java.text.MessageFormat;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModelFactory;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.xml.xam.Nameable;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Implementation of design context's controller for bpel mapper.
 *
 * @author Nikita Krjukov
 * @author Vitaly Bychkov
 *
 */
public class BpelMapperDcc extends AbstractDccImpl {

    /**
     * The lookup of mapper Tc must contain BPELDataObject, BpelModel and ShowMapperCookie
     * @param mapperTc
     */
    public BpelMapperDcc(TopComponent mapperTc) {
        super(mapperTc);
    }

    @Override
    protected BpelDesignContext constructActivatedContext() {
        BpelDesignContext newContext = BpelDesignContextFactory.
                getInstance().getActivatedContext(mBpelModel);
        return newContext;
    }

    @Override
    protected MapperModel constructMapperModel() {
        MapperModel newMapperModel = new BpelMapperModelFactory(
                    mMapperTcContext, mContext).constructModel();
        return newMapperModel;
    }

    @Override
    protected void showMapperIsEmpty() {
        disableMapper(NbBundle.getMessage(MapperMultiviewElement.class, 
                "LBL_EmptyMapperContext")); // NOI18N
    }

    @Override
    protected void showModelIsInvalid() {
        disableMapper(NbBundle.getMessage(MapperMultiviewElement.class, 
                "LBL_Invalid_BpelModel")); // NOI18N
    }

    @Override
    protected void showUnsupportedEntity(BpelDesignContext context) {
        assert SwingUtilities.isEventDispatchThread();
        //
        assert context != null;
        String entityName = null;
        Node node = context.getActivatedNode();
        entityName = node != null ? node.getDisplayName() : null;
        if (entityName == null) {
            BpelEntity entity = context.getSelectedEntity();
            entityName = entity instanceof Nameable ? 
                ((Nameable)entity).getName() : EditorUtil.getTagName(entity);
        }
        entityName = entityName == null ? "" : entityName;
        disableMapper(NbBundle.getMessage(MapperMultiviewElement.class,
                "LBL_EmptyMapper", entityName)); // NOI18N
    }

    @Override
    protected void showNotValidContext(BpelDesignContext context) {
        assert SwingUtilities.isEventDispatchThread();
        //
        assert context != null;
        String entityName = null;
        Node node = context.getActivatedNode();
        entityName = node != null ? node.getDisplayName() : null;
        if (entityName == null) {
            BpelEntity entity = context.getSelectedEntity();
            entityName = entity instanceof Nameable ?
                ((Nameable)entity).getName() : EditorUtil.getTagName(entity);
        }
        entityName = entityName == null ? "" : entityName;
        disableMapper(NbBundle.getMessage(MapperMultiviewElement.class,
                "LBL_InValidMapperContext", entityName)); // NOI18N
    }

    public static void addErrMessage(StringBuffer errMsgBuffer, 
        String xpathExpression, String tagName) {
        if ((errMsgBuffer == null) || (xpathExpression == null) || (tagName == null)) return;

        String 
            errMsgPattern = NbBundle.getMessage(BpelMapperDcc.class,
            "LBL_Bpel_Mapper_Err_Msg_Wrong_XPathExpr_Data"),
            errMsg = MessageFormat.format(errMsgPattern, new Object[] {tagName,
                xpathExpression});                

            errMsgBuffer.append((errMsgBuffer.length() > 0) ? 
                " " + errMsg : errMsg);                
            errMsgBuffer.append(",\n");
    }

}
