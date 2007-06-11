/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;


/**
 * @author   Jan Jancura
 */
public class WatchesNodeModel extends VariablesNodeModel implements ExtendedNodeModel {

    public static final String WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/Watch";


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
            /*if (isEmptyWatch(o)) {
                return "<html><font color=\"#808080\">&lt;Enter new watch&gt;</font></html>";
            }*/
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
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return WATCH;
        }
        if (o instanceof JPDAWatch) {
            if (isEmptyWatch(o)) {
                return null;
            }
            return WATCH;
        }
        return super.getIconBase (o);
    }

    public boolean canRename(Object node) throws UnknownTypeException {
        return (node instanceof JPDAWatch);
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

    /*
    public Transferable drag(Object node) throws IOException,
                                                 UnknownTypeException {
        if (node instanceof JPDAWatch) {
            return new StringSelection(((JPDAWatch) node).getExpression());
        } else {
            return null;
        }
    }
     */

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

    /*
    public PasteType getDropType(Object node, Transferable t, int action,
                                 int index) throws UnknownTypeException {
        return null;
    }
     */

    public void setName(Object node, String name) throws UnknownTypeException {
        ((JPDAWatch) node).setExpression(name);
    }

    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        String iconBase = getIconBase(node);
        if (iconBase == null) {
            return null;
        } else {
            return iconBase + ".gif";
        }
    }
    
}
