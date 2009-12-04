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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;

import java.io.StringReader;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.spi.viewmodel.DnDNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;


/**
 * @author   Jan Jancura
 */
public class WatchesNodeModel extends VariablesNodeModel implements DnDNodeModel {

    public static final String WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/watch_16.png";


    public WatchesNodeModel (ContextProvider lookupProvider) {
        super (lookupProvider);
    }
    
    public static boolean isEmptyWatch(Object node) {
        return "EmptyWatch".equals(node.getClass().getSimpleName());
    }
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return NbBundle.getBundle (WatchesNodeModel.class).
                getString ("CTL_WatchesModel_Column_Name_Name");
        if (o instanceof JPDAWatch) {
            if (isEmptyWatch(o)) {
                return "<_html><font color=\"#808080\">&lt;" + // [TODO] <_html> tag used as workaround, see TreeModelNode.setName()
                    NbBundle.getBundle (WatchesNodeModel.class).getString("CTL_WatchesModel_Empty_Watch_Hint") +
                    "&gt;</font></html>";
            }
            return ((JPDAWatch) o).getExpression ();
        }
        return super.getDisplayName (o);
    }
    
    protected String getShortDescriptionSynch (Object o) {
        if (o instanceof JPDAWatch) {
            if (isEmptyWatch(o)) {
                return NbBundle.getMessage(WatchesNodeModel.class, "TTP_NewWatch");
            }
            JPDAWatch w = (JPDAWatch) o;
            boolean evaluated;
            evaluated = VariablesTreeModelFilter.isEvaluated(o);
            if (!evaluated) {
                return w.getExpression ();
            }
            String e = w.getExceptionDescription ();
            if (e != null)
                return w.getExpression () + " = >" + e + "<";
            String t = w.getType ();
            if (t == null)
                return w.getExpression () + " = " + w.getValue ();
            else
                try {
                    return w.getExpression () + " = (" + w.getType () + ") " + 
                        w.getToStringValue ();
                } catch (InvalidExpressionException ex) {
                    return ex.getLocalizedMessage ();
                }
        }
        return super.getShortDescriptionSynch(o);
    }
    
    protected void testKnown(Object o) throws UnknownTypeException {
        if (o instanceof JPDAWatch) return ;
        super.testKnown(o);
    }
    
    public boolean canRename(Object node) throws UnknownTypeException {
        return node instanceof JPDAWatch;
    }

    public boolean canCopy(Object node) throws UnknownTypeException {
        return (node instanceof JPDAWatch) && !isEmptyWatch(node);
    }

    public boolean canCut(Object node) throws UnknownTypeException {
        return (node instanceof JPDAWatch) && !isEmptyWatch(node);
    }

    public Transferable clipboardCopy(Object node) throws IOException,
                                                          UnknownTypeException {
        return new StringSelection(((JPDAWatch) node).getExpression());
    }

    public Transferable clipboardCut(Object node) throws IOException,
                                                         UnknownTypeException {
        return new StringSelection(((JPDAWatch) node).getExpression());
    }

    public int getAllowedDragActions() {
        return DnDConstants.ACTION_COPY_OR_MOVE;
    }

    public int getAllowedDropActions() {
        return DnDConstants.ACTION_COPY;
    }

    public int getAllowedDropActions(Object node, Transferable t) {
        if (t != null && t.isDataFlavorSupported(new DataFlavor(JPDAWatch.class, null))) {
            return DnDConstants.ACTION_COPY_OR_MOVE;
        } else {
            return DnDConstants.ACTION_COPY;
        }
    }

    public Transferable drag(Object node) throws IOException,
                                                 UnknownTypeException {
        if (node instanceof JPDAWatch) {
            return new WatchSelection((JPDAWatch) node);
        } else {
            return null;
        }
    }

    public PasteType[] getPasteTypes(final Object node, final Transferable t) throws UnknownTypeException {
        if (node != TreeModel.ROOT && !(node instanceof JPDAWatch)) {
            return null;
        }
        DataFlavor[] flavors = t.getTransferDataFlavors();
        final DataFlavor textFlavor = DataFlavor.selectBestTextFlavor(flavors);
        if (textFlavor != null) {
            return new PasteType[] { new PasteType() {

                public Transferable paste() {
                    try {
                        java.io.Reader r = textFlavor.getReaderForText(t);
                        java.nio.CharBuffer cb = java.nio.CharBuffer.allocate(1000);
                        r.read(cb);
                        cb.flip();
                        if (node instanceof JPDAWatch) {
                            ((JPDAWatch) node).setExpression(cb.toString());
                            fireModelChange(new ModelEvent.NodeChanged(WatchesNodeModel.this, node));
                        } else {
                            // Root => add a new watch
                            DebuggerManager.getDebuggerManager().createWatch(cb.toString());
                        }
                    } catch (Exception ex) {}
                    return null;
                }
            } };
        } else {
            return null;
        }
    }

    public PasteType getDropType(final Object node, final Transferable t, int action,
                                 int index) throws UnknownTypeException {
        //System.err.println("\n\ngetDropType("+node+", "+t+", "+action+", "+index+")");
        DataFlavor[] flavors = t.getTransferDataFlavors();
        final DataFlavor textFlavor = DataFlavor.selectBestTextFlavor(flavors);
        //System.err.println("Text Flavor = "+textFlavor);
        if (textFlavor != null) {
            return new PasteType() {

                public Transferable paste() {
                    try {
                        java.io.Reader r = textFlavor.getReaderForText(t);
                        java.nio.CharBuffer cb = java.nio.CharBuffer.allocate(1000);
                        r.read(cb);
                        cb.flip();
                        if (node instanceof JPDAWatch) {
                            ((JPDAWatch) node).setExpression(cb.toString());
                            fireModelChange(new ModelEvent.NodeChanged(WatchesNodeModel.this, node));
                        } else {
                            // Root => add a new watch
                            DebuggerManager.getDebuggerManager().createWatch(cb.toString());
                        }
                    } catch (Exception ex) {}
                    return t;
                }
            };
        } else {
            return null;
        }
    }

    public void setName(Object node, String name) throws UnknownTypeException {
        ((JPDAWatch) node).setExpression(name);
    }

    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
            return WATCH;
        }
        if (node instanceof JPDAWatch) {
            if (isEmptyWatch(node)) {
                return null;
            }
            return WATCH;
        }
        return super.getIconBaseWithExtension(node);
    }

    private static  class WatchSelection implements Transferable, ClipboardOwner {

        private static final int STRING = 0;
        private static final int PLAIN_TEXT = 1;
        private static final int WATCH = 2;

        private static final DataFlavor[] flavors = {
            DataFlavor.stringFlavor,
            DataFlavor.plainTextFlavor, // deprecated
            new DataFlavor(JPDAWatch.class, "Watch")
        };

        private JPDAWatch watch;
        private String str;

        /**
         * Creates a <code>Transferable</code> capable of transferring
         * the specified <code>Watch</code>.
         */
        public WatchSelection(JPDAWatch watch) {
            this.watch = watch;
            this.str = watch.getExpression();
        }

        /**
         * Returns an array of flavors in which this <code>Transferable</code>
         * can provide the data.
         */
        public DataFlavor[] getTransferDataFlavors() {
            // returning flavors itself would allow client code to modify
            // our internal behavior
            return (DataFlavor[])flavors.clone();
        }

        /**
         * Returns whether the requested flavor is supported by this
         * <code>Transferable</code>.
         * 
         * @throws NullPointerException if flavor is <code>null</code>
         */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for (int i = 0; i < flavors.length; i++) {
                if (flavor.equals(flavors[i])) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns the <code>Transferable</code>'s data in the requested
         * <code>DataFlavor</code> if possible.
         * @param flavor the requested flavor for the data
         * @return the data in the requested flavor, as outlined above
         * @throws UnsupportedFlavorException if the requested data flavor is
         *         not supported.
         * @throws IOException if an IOException occurs while retrieving the data.
         * @throws NullPointerException if flavor is <code>null</code>
         */
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,
                                                                IOException {
            if (flavor.equals(flavors[STRING])) {
                return (Object)str;
            } else if (flavor.equals(flavors[PLAIN_TEXT])) {
                return new StringReader(str == null ? "" : str);
            } else if (flavor.equals(flavors[WATCH])) {
                return watch;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

        public void lostOwnership(Clipboard clipboard, Transferable contents) {
        }
    }

}
