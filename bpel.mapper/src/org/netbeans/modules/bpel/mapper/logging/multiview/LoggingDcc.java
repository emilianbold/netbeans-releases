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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.mapper.logging.multiview;

import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.mapper.logging.model.LoggingMapperModelFactory;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.multiview.MapperMultiviewElement;
import org.netbeans.modules.bpel.mapper.multiview.AbstractDccImpl;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.xml.xam.Nameable;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Implementation of design context's controller for logging mapper.
 *
 * @author Nikita Krjukov
 * @author Vitaly Bychkov
 *
 */
public class LoggingDcc extends AbstractDccImpl {

    /**
     * The lookup of mapper Tc must contain BPELDataObject, BpelModel and ShowMapperCookie
     * @param mapperTc
     */
    public LoggingDcc(TopComponent mapperTc) {
        super(mapperTc);
    }

    protected BpelDesignContext constructActivatedContext() {
        BpelDesignContext newContext = LoggingDesignContextFactory.
                getInstance().getActivatedContext(mBpelModel);
        return newContext;
    }

    protected MapperModel constructMapperModel() {
        MapperModel newMapperModel = new LoggingMapperModelFactory(
                    mMapperTcContext, mContext).constructModel();
        return newMapperModel;
    }

    protected void showMapperIsEmpty() {
        disableMapper(NbBundle.getMessage(MapperMultiviewElement.class,
                "LBL_EmptyLoggingMapperContext")); // NOI18N
    }

    protected void showModelIsInvalid() {
        disableMapper(NbBundle.getMessage(MapperMultiviewElement.class,
                "LBL_LoggingInvalid_BpelModel")); // NOI18N
    }

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
                "LBL_LoggingEmptyMapper", entityName)); // NOI18N
    }

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
                "LBL_LoggingInValidMapperContext", entityName)); // NOI18N
    }

}
