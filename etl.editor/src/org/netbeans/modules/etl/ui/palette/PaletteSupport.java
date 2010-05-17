/*
 * The contents of this file are subject to the terms of the Common
 * Development
The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"
Contributor(s):
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 *
 */
package org.netbeans.modules.etl.ui.palette;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopPanel;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.impl.OperatorXmlInfoModel;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author nithya
 */
public class PaletteSupport {

    public static final String MASHUP_DATA_FLAVOR = DataFlavor.javaJVMLocalObjectMimeType;
    private PaletteController controller;
    private static IGraphView graphView;

    public PaletteSupport() {
    }

    /**
     *
     * @return
     */
    public PaletteController createPalette(final ETLCollaborationTopPanel etlPanel) throws IOException {
        controller = PaletteFactory.createPalette("ETLOperators", new ETLAction(), null, new ETLDnDHandler());
        controller.addPropertyChangeListener(new PropertyChangeListener() {

            // FIXME: There should be a better way to do this.
            ETLCollaborationTopPanel topPanel = etlPanel;
            public void propertyChange(PropertyChangeEvent evt) {
                if (PaletteController.PROP_SELECTED_ITEM.equals(evt.getPropertyName())) {
                    Lookup selItem = controller.getSelectedItem();

                    graphView = topPanel.getGraphView();
                    if (null != selItem) {
                        Node selNode = selItem.lookup(Node.class);
                        if (null != selNode) {
                            IOperatorXmlInfo opXmlInfo = OperatorXmlInfoModel.getInstance("ETLOperators").findOperatorXmlInfo(selNode.getName());
                            graphView.setXMLInfo(opXmlInfo);
                        }
                    }
                }
            }
        });
        return controller;
    }

    public static class ETLAction extends PaletteActions {

        /**
         *
         * @return
         */
        public Action[] getImportActions() {
           return new Action[0];
        }

        /**
         *
         * @return
         */
        public Action[] getCustomPaletteActions() {
            return new Action[0];
        }

        /**
         *
         * @param lookup
         * @return
         */
        public Action[] getCustomCategoryActions(Lookup lookup) {
            return new Action[0];
        }

        /**
         *
         * @param lookup
         * @return
         */
        public Action[] getCustomItemActions(Lookup lookup) {
            return new Action[0];
        }

        /**
         *
         * @param lookup
         * @return
         */
        public Action getPreferredAction(Lookup lookup) {
            return new PreferredAction(lookup);
        }
    }

    public static class PreferredAction implements Action {

        private IOperatorXmlInfo opXmlInfo = null;

        // FIXME: There should be a better way to do this.
        public PreferredAction(Lookup lookup) {
            Node node = lookup.lookup(Node.class);
            if (null != node) {
                opXmlInfo = OperatorXmlInfoModel.getInstance("ETLOperators").findOperatorXmlInfo(node.getName());
                graphView.setXMLInfo(opXmlInfo);
            }
        }

        public static String getSHORT_DESCRIPTION() {
            return SHORT_DESCRIPTION;
        }

        
        public Object getValue(String key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void putValue(String key, Object value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setEnabled(boolean b) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isEnabled() {
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void actionPerformed(ActionEvent e) {
            Point viewCoord = new Point();
            if (graphView.getGraphController() != null) {
                graphView.getGraphController().handleNodeAdded(opXmlInfo, viewCoord);
            }
        }
    }

    private static class ETLDnDHandler extends DragAndDropHandler {

        public void customize(ExTransferable exTransferable, Lookup lookupobj) {
            final Node node = lookupobj.lookup(Node.class);
            DataFlavor flv = null;
            try {
                flv = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            exTransferable.put(new ExTransferable.Single(flv) {

                protected Object getData() throws IOException, UnsupportedFlavorException {
                    return node;
                }
            });
        }
    }
}
