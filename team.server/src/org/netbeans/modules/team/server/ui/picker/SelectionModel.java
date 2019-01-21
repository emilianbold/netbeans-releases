/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.team.server.ui.picker;

import java.awt.Point;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.team.commons.treelist.ListNode;
import org.netbeans.modules.team.commons.treelist.SelectionList;
import org.openide.util.ChangeSupport;

/**
 * Common selection model to ensure only a single project is selected.
 * 
 * 
 */
final class SelectionModel {

    private final ListSelectionListener selectionListener;
    private final ArrayList<SelectionList> lists = new ArrayList<>( 10 );
    private final ChangeSupport changeSupport = new ChangeSupport( this );
    private ListNode initialSelection = null;
    private ListNode currentSelection = null;

    SelectionModel() {
        selectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged( ListSelectionEvent e ) {
                onSelectionChange( e );
            }
        };
    }

    boolean add( SelectionList sl ) {
        synchronized( lists ) {
            if( lists.contains( sl ) )
                return false;
            boolean hasSelection = null != getSelectedItem();
            lists.add( sl );
            if( hasSelection )
                sl.clearSelection();
            boolean res = false;
            if( null != initialSelection ) {
                ListModel<ListNode> model = sl.getModel();
                for( int i=0; i<model.getSize(); i++ ) {
                    if( initialSelection.equals( model.getElementAt( i )  ) ) {
                        sl.setSelectedValue( initialSelection, true );
                        initialSelection = null;
                        currentSelection = initialSelection;
                        res = true;
                        break;
                    }
                }
            }
            sl.getSelectionModel().addListSelectionListener( selectionListener );
            return res;
        }
    }

    void remove( ListNode node ) {
        synchronized( lists ) {
            for (SelectionList sl : lists) {
                DefaultListModel m = (DefaultListModel) sl.getModel();
                if(m.removeElement(m)) {
                    return;
                }
            }
        }
    }
    
    void remove( SelectionList sl ) {
        synchronized( lists ) {
            if( !lists.contains( sl ) )
                return;
//            boolean hasSelection = null != getSelectedItem();
            lists.remove( sl );
            sl.getSelectionModel().removeListSelectionListener( selectionListener );
//            if( hasSelection )  //TODO to fire or to fire not?
//                changeSupport.fireChange();
        }
    }

    public ListNode getSelectedItem() {
        synchronized( lists ) {
            for( SelectionList sl : lists ) {
                ListNode sel = sl.getSelectedValue();
                if( null != sel )
                    return sel;
            }
            return null;
        }
    }

    public void setSelectedItem( ListNode item ) {
        assert SwingUtilities.isEventDispatchThread();
        synchronized( lists ) {
            currentSelection = null;
            if( null == item ) {
                for( SelectionList sl : lists ) {
                    sl.clearSelection();
                }
                return;
            } else {
                for( SelectionList sl : lists ) {
                    ListModel<ListNode> model = sl.getModel();
                    for( int i=0; i<model.getSize(); i++ ) {
                        if( item.equals( model.getElementAt( i )  ) ) {
                            sl.setSelectedValue( item, true );
                            currentSelection = item;
                            return;
                        }
                    }
                }
            }
        }
        //or just ignore silently?
        throw new IllegalArgumentException();
    }

    public void addChangeListener( ChangeListener cl ) {
        changeSupport.addChangeListener( cl );
    }

    public void removeChangeListener( ChangeListener cl ) {
        changeSupport.removeChangeListener( cl );
    }

    public void setInitialSelection( ListNode selNode ) {
        synchronized( lists ) {

            initialSelection = null;
            currentSelection = selNode;
            if( null == selNode ) {
                return;
            }

            for( SelectionList sl : lists ) {
                ListModel<ListNode> model = sl.getModel();
                for( int i=0; i<model.getSize(); i++ ) {
                    if( selNode.equals( model.getElementAt( i )  ) ) {
                        sl.setSelectedValue( selNode, true );
                        return;
                    }
                }
            }

            initialSelection = selNode;
        }
    }

    ListNode getInitialSelection() {
        return initialSelection;
    }

    private boolean ignoreSelectionEvents = false;
    private void onSelectionChange( ListSelectionEvent e ) {
        if( e.getValueIsAdjusting() )
            return;
        if( ignoreSelectionEvents ) {
            return;
        }
        ignoreSelectionEvents = true;
        synchronized( lists ) {
            for( SelectionList sl : lists ) {
                if( sl.getSelectionModel() == e.getSource() ) {
                    if( sl.getSelectedValue() == currentSelection ) { //selection index has changed but the selected item is the same as before
                                                                      //(probably an item with index lower than the selected one has been removed from the model)
                        ignoreSelectionEvents = false;
                        return;
                    }
                }
            }
            for( SelectionList sl : lists ) {
                if( sl.getSelectionModel() == e.getSource() )
                    continue;
                sl.clearSelection();
            }
        }
        ignoreSelectionEvents = false;
        changeSupport.fireChange();
    }
}
