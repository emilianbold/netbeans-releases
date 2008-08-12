/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.javascript.debugger.http.ui.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.modules.web.client.javascript.debugger.http.api.HttpActivity;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ModelEvent.TreeChanged;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author joelle
 */
public abstract class HttpHeaderModel implements TableModel, TreeModel, NodeModel {
    
    public final static String KEY = "KEY";
    private final ActivitiesPropertyChange activityPropertyChangeListener = new ActivitiesPropertyChange();
    private final List<ModelListener> listeners = new ArrayList<ModelListener>();

    public HttpHeaderModel(ExplorerManager activityExplorerManager) {
        activityExplorerManager.addPropertyChangeListener(activityPropertyChangeListener);
    }

    
    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        
        if (ROOT.equals(node)) {
            getHeader().entrySet();
        }
        if( node instanceof Entry && columnID.equals(VALUE_COLUMN)){
            return ((Entry)node).getValue();
        } else {
            throw new UnknownTypeException("Type not recognized:" + node);
        }
       // return new UnsupportedOperationException("This column type is not recognized: " + columnID);
    }

    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        return true;
    }

    public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }

    public Object getRoot() {
        return ROOT;
    }

    public Object[] getChildren(Object node, int from, int to) throws UnknownTypeException {
        if (ROOT.equals(node)) {
            return getHeader().entrySet().toArray();
        }
        return new Object[0];
    }

    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (ROOT.equals(node)) {
            return false;
        }
        return true;
    }

    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (ROOT.equals(node)) {
            return getHeader().keySet().size();
        }
        return 0;
    }

    public String getDisplayName(Object node) throws UnknownTypeException {
        if (ROOT.equals(node)) {
            return NbBundle.getMessage(HttpHeaderModel.class, KEY);
        } else if ( node instanceof Entry ) {
            return ((Entry)node).getKey().toString();
        }
        
        throw new UnknownTypeException("Type not recognized:" + node);
    }

    public String getIconBase(Object node) throws UnknownTypeException {
        return null;
    }

    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof Entry) {
            return ((Entry)node).getKey().toString();
        } 
        throw new UnknownTypeException("Type not recognized:" + node);
    }
    
    
    private static final ValueColumn valueColumn = new ValueColumn();
    public final static String VALUE_COLUMN = "VALUE_COLUMN";
    public static final ColumnModel getColumnModel ( String column ){
        if ( VALUE_COLUMN.equals(column)){
            return valueColumn;
        }
        return null;
    }
    
    private static final class ValueColumn extends AbstractColumnModel {

        @Override
        public String getID() {
            return VALUE_COLUMN;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HttpHeaderModel.class, VALUE_COLUMN);
        }

        @Override
        public Class getType() {
            return String.class;
        }

        @Override
        public int getColumnWidth() {
            return super.getColumnWidth();
        }
        
    }

    private void fireModelChange() {
        for (ModelListener l : listeners) {
            l.modelChanged(new TreeChanged(this));
        }
    }
    
    private HttpActivity selectedActivity;

    protected abstract Map<String,String> getHeader();
    protected final HttpActivity getSelectedActivity() {
        return selectedActivity;
    }
    

    private class ActivitiesPropertyChange implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                Object obj = evt.getNewValue();
                if ( obj == null ){
                    return;
                }
                assert evt.getNewValue() instanceof Node[];
                Node[] nodes = (Node[]) evt.getNewValue();
                
                if( nodes != null && nodes.length == 1) {    
                    selectedActivity = nodes[0].getLookup().lookup(HttpActivity.class);
                } else {
                    selectedActivity = null;
                }

                fireModelChange();
            }

        }
    }
    
}
