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
package org.netbeans.modules.bpel.mapper.palette;

import java.io.File;
import java.util.List;
import java.lang.reflect.Method;

import org.netbeans.modules.bpel.mapper.model.BpelVertexFactory;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.JavaMetadata;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.model.customitems.XmlLiteralEditor;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.xpath.mapper.palette.DefaultItemHandler;
import org.netbeans.modules.xml.clazz.ClazzDialog;
import org.netbeans.modules.xml.reference.ReferenceUtil;
import org.netbeans.modules.xml.time.DeadlineDialog;
import org.netbeans.modules.xml.time.DurationDialog;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @author Nikita Krjukov
 * @version 2009.09.17
 */
public interface BpelSpecificItemHandlers {

    class Deadline extends DefaultItemHandler {

        Deadline() {
            super(i18n(BpelSpecificItemHandlers.class, "LBL_Deadline_Literal"),
                    icon(BpelSpecificItemHandlers.class, "deadline"), ""); // NOI18N
        }

        @Override
        public GraphSubset createGraphSubset() {
            return BpelVertexFactory.getInstance().createGraphSubset(myObject);
        }

        @Override
        public boolean canAddGraphSubset() {
            DeadlineDialog dialog = new DeadlineDialog();
            dialog.setVisible(true);
            Object value = dialog.getValue();

            if (value != null) {
                setObject(value);
            }
//out("VALUE: " + time);
            return value != null;
        }
    }

    // ------------------------------------
    class Duration extends DefaultItemHandler {

        Duration() {
            super(i18n(BpelSpecificItemHandlers.class, "LBL_Duration_Literal"),
                    icon(BpelSpecificItemHandlers.class, "duration"), ""); // NOI18N
        }

        @Override
        public GraphSubset createGraphSubset() {
            return BpelVertexFactory.getInstance().createGraphSubset(myObject);
        }

        @Override
        public boolean canAddGraphSubset() {
            DurationDialog dialog = new DurationDialog();
            dialog.setVisible(true);
            Object value = dialog.getValue();

            if (value != null) {
                setObject(value);
            }
//out("VALUE: " + value);
            return value != null;
        }
    }

    // --------------------------------------
    class XmlLiteral extends DefaultItemHandler {

        XmlLiteral() {
            super(i18n(BpelSpecificItemHandlers.class, "LBL_XML_Literal"),
                    icon(BpelSpecificItemHandlers.class, "literal"), ""); // NOI18N
        }

        @Override
        public GraphSubset createGraphSubset() {
            return BpelVertexFactory.getInstance().createGraphSubset(myObject);
        }

        @Override
        public boolean canAddGraphSubset() {
            XmlLiteralEditor panel = new XmlLiteralEditor(null, null);

            if (XmlLiteralEditor.showDlg(panel)) {
                setObject(panel.getXmlDataObject());
                return true;
            }
            return false;
        }
    }

    // ------------------------------------
    class Java extends DefaultItemHandler {

        Java(MapperTcContext mapperTcContext) {
            super(i18n(BpelSpecificItemHandlers.class, "LBL_Java"), icon(BpelSpecificItemHandlers.class, "java"), ""); // NOI18N
            mMapperTcContext = mapperTcContext;
        }

        @Override
        public GraphSubset createGraphSubset() {
            return BpelVertexFactory.getInstance().createGraphSubset(myObject);
        }

        @Override
        public boolean canAddGraphSubset() {
            ClazzDialog dialog = new ClazzDialog(collectJarFiles());
            dialog.showAndWait();
            Method method = dialog.getMethod();

            if (method == null) {
                return false;
            }
            setObject(new JavaMetadata(method));
//out("VALUE: " + value);
            return true;
        }

        private List<File> collectJarFiles() {
            BpelModel model = mMapperTcContext.getDesignContextController().getContext().getBpelModel();
            return ReferenceUtil.getArchiveFiles(SoaUtil.getFileObjectByModel(model));
        }

        private MapperTcContext mMapperTcContext;
    }
}
