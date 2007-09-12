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

package org.netbeans.modules.xslt.mapper.view;

import java.awt.Point;
import java.awt.event.InputEvent;
import org.netbeans.modules.soa.mapper.common.IMapperEvent;
import org.netbeans.modules.soa.mapper.common.IMapperListener;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperRule;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralEditor;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IField;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasMouseData;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasMouseListener;
import org.netbeans.modules.xslt.mapper.methoid.Constants;
import org.netbeans.modules.xslt.mapper.model.nodes.LiteralCanvasNode;
import org.openide.filesystems.FileObject;

/**
 * This class is a workaround to support edit literals which are created
 * from standard NetBeans palette.
 *
 * @author nk160297
 */
public class LiteralEditListener
        implements ICanvasMouseListener, IMapperListener {
    
    private XsltMapper myMapper;
    
    public LiteralEditListener(IBasicMapper mapper) {
        assert mapper instanceof XsltMapper;
        myMapper = (XsltMapper)mapper;
    }
    
    public boolean doMouseDblClick(ICanvasMouseData event) {
        int mods = event.getMouseModifier();
        if ((mods & InputEvent.BUTTON1_MASK) == 0) {
            return false;
        }
        ICanvasView canvas =
                myMapper.getMapperViewManager().getCanvasView().getCanvas();
        Point point = event.getViewLocation();
        ICanvasMethoidNode canvasMethoidNode =
                canvas.getCanvasMethoidNodeByPoint(point);
        if (canvasMethoidNode == null) {
            return false;
        }
        IMethoidNode methoidNode = canvasMethoidNode.getMethoidNode();
        if (methoidNode == null) {
            return false;
        }
        Object nodeObject = methoidNode.getNodeObject();
        if (nodeObject == null || !(nodeObject instanceof LiteralCanvasNode)) {
            // Only Literals are supported.
            return false; 
        }
        ICanvasFieldNode canvasFieldNode =
                canvas.getCanvasFieldNodeByPoint(point);
        if (canvasFieldNode == null) {
            return false;
        }
        IFieldNode fieldNode = canvasFieldNode.getFieldNode();
        if (fieldNode == null) {
            return false;
        }
        IField field = (IField) fieldNode.getFieldObject();
        if (field == null) {
            return false;
        }
        ILiteralUpdater literalUpdater = field.getLiteralUpdater();
        if (literalUpdater == null) {
            String type = field.getType();
            literalUpdater = myMapper.getLiteralUpdaterFactory().
                    createLiteralUpdater(type);
            if (literalUpdater == null) {
                return false;
            }
        }
        
        ILiteralEditor editor =
                literalUpdater.getEditor(myMapper, fieldNode);
        if (editor != null) {
            editor.show();
        }
        
        return true;
    }
    
    public boolean doMouseUp(ICanvasMouseData data) {
        return false;
    }
    
    public boolean doMouseMove(ICanvasMouseData data) {
        return false;
    }
    
    public boolean doMouseDown(ICanvasMouseData data) {
        return false;
    }
    
    public boolean doMouseClick(ICanvasMouseData data) {
        return false;
    }
    
    public void eventInvoked(IMapperEvent e) {
        Object eventObject = e.getTransferObject();
        //
        if (e.getEventType().equals(IMapperEvent.REQ_NEW_NODE)) {
            IBasicMapperRule rule = myMapper.getMapperRule();
            if (rule != null && eventObject instanceof IMapperNode) {
                IMethoidNode mNode = (IMethoidNode)eventObject;
                if (rule.isAllowToCreate(mNode)) {
                    handleLiteral(mNode);
                }
            }
        }
    }
    
    private void handleLiteral(IMethoidNode mNode) {
        IMethoid methoid = (IMethoid) mNode.getMethoidObject();
        //
        if (methoid.isLiteral()) {
            IFieldNode fieldNode = (IFieldNode)mNode.getOutputFieldNodes().get(0); // all literals has one output
            String outputType = fieldNode.getTypeName();
            switch (Constants.LiteralType.findByName(outputType)) {
                case XPATH_LITERAL_TYPE:
                    
//                    Node dataNode = NodeFactory.getInstance().createNode(exprObj);
//                    mNode.setNodeObject(dataNode);
//                    fieldNode.setNodeObject(dataNode);
//                    getMapper().addNode(mNode);
                    
                    ILiteralUpdater literalUpdater = 
                            myMapper.getLiteralUpdaterFactory().
                            createLiteralUpdater(outputType);
                    literalUpdater.getEditor(myMapper, fieldNode).show();
                    return;
            }
        }
    }
    
}
