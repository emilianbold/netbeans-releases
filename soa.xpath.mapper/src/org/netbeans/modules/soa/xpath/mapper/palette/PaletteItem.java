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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;

import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.utils.GraphLayout;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
// import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.11.19
 */
public final class PaletteItem extends JMenuItem implements DragGestureListener, Transferable {

    public PaletteItem(AbstractMapperPalette palette, ItemHandler handler) {
        super(handler.getDisplayName(), handler.getIcon());
        myPalette = palette;
        myHandler = handler;

        try {
            myDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);

        addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                MapperStaticContext staticContext = myPalette.getStaticContext();
                Mapper mapper = staticContext.getMapper();
                TreePath path = mapper.getSelectedPath();
                XPathMapperModel model = (XPathMapperModel) staticContext.getMapperModel();

                if (model == null) {
                    return;
                }
                GraphSubset graphSubset = model.add(path, myHandler, GraphLayout.getNextFreeX(model.getGraph(path)), 0);

                if (graphSubset != null && graphSubset.getVertexCount() > 0) {
                    mapper.getSelectionModel().setSelected(path, graphSubset.getVertex(0));
                }
            }
        });
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{myDataFlavor};
    }

    public Object getTransferData(DataFlavor flavor) {
        if (isDataFlavorSupported(flavor)) {
            return new Object[]{myHandler, myPalette};
        }
        return null;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == myDataFlavor;
    }

    public void dragGestureRecognized(DragGestureEvent event) {
        if (isEnabled() && isVisible()) {
            DragSource.getDefaultDragSource().startDrag(event, DragSource.DefaultCopyDrop, this, null);
        }
    }

    private AbstractMapperPalette myPalette;
    private ItemHandler myHandler;
    private DataFlavor myDataFlavor;
}
