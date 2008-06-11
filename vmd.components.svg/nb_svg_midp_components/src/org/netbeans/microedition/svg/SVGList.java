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
package org.netbeans.microedition.svg;

import java.util.Vector;

import org.netbeans.microedition.svg.input.InputHandler;
import org.netbeans.microedition.svg.input.NumPadInputHandler;


/**
 * @author ads
 *
 */
public class SVGList extends SVGComponent {


    public SVGList( SVGForm form, String elemId ) {
        super(form, elemId);
        
        myRenderer = new SVGDefaultListCellRenderer();
        mySelectionModel = new DefaultSelectionModel();
        myHandler = new ListHandler();
    }


    public SVGListCellRenderer getRenderer(){
        return myRenderer;
    }
    
    public ListModel getModel(){
        return myModel;
    }
    
    public void setModel( ListModel model ){
        myModel = model;
        renderList();
    }
    
    public void setRenderer( SVGListCellRenderer renderer ){
        myRenderer = renderer;
    }
    
    public SelectionModel getSelectionModel(){
        return mySelectionModel;
    }
    
    public InputHandler getInputHandler(){
        return myHandler;
    }
    
    private void renderList() {
        ListModel model = getModel();
        int size = model.getSize();
        SVGListCellRenderer renderer = getRenderer();
        for( int i =0 ; i<size ; i++ ){
            renderer.getCellRendererComponent( this , model.getElementAt(i), i, 
                    getSelectionModel().isSelectedIndx(i));
        }
    }
    
    public interface ListModel {
        Object getElementAt( int index );
        int getSize();
    }
    
    public interface SelectionModel {
        void clearSelection();
        boolean isSelectedIndx( int index );
        void addSelectionInterval( int from , int to);
    }
    
    public static class DefaultListMoldel implements ListModel {
        
        public DefaultListMoldel( Vector data ){
            myData = new Vector();
            for ( int i=0; i<data.size() ; i++ ){
                myData.addElement( data.elementAt( i ));
            }
        }
        
        public Object getElementAt( int indx ) {
            return myData.elementAt(indx);
        }

        public int getSize() {
            return myData.size();
        }
        
        private Vector myData;
    }
    
    private class DefaultSelectionModel implements SelectionModel {

        public boolean isSelectedIndx( int index ) {
            return index == mySelectedIndex;
        }
        
        public void addSelectionInterval( int from, int to ) {
            if ( to != from ){
                throw new IllegalArgumentException( 
                        DefaultSelectionModel.class.getName() +" is not designed" +
                        		" for multiple selection");
            }
            mySelectedIndex = from;
        }
        
        public void clearSelection() {
            mySelectedIndex = 0;
        }
        
        private int mySelectedIndex;

    }
    
    private class ListHandler extends InputHandler {

        public boolean handleKeyPress( SVGComponent comp, int keyCode ) {
            return (comp instanceof SVGList) && 
                 ( keyCode == LEFT || keyCode == RIGHT || keyCode == FIRE ); 
        }

        public boolean handleKeyRelease( SVGComponent comp, int keyCode ) {
            boolean ret = false;
            if ( comp instanceof SVGList ){
                if ( keyCode == LEFT ){
                    myIndex = Math.max( 0 , myIndex -1 );
                    getSelectionModel().clearSelection();
                    getSelectionModel().addSelectionInterval( myIndex, myIndex);
                    renderList();
                    ret = true;
                }
                else if ( keyCode == RIGHT ){
                    myIndex = Math.min( myIndex +1 , getModel().getSize() -1  );
                    getSelectionModel().clearSelection();
                    getSelectionModel().addSelectionInterval( myIndex, myIndex);
                    renderList();
                    ret = true;
                }
                else if( keyCode == FIRE ){
                    fireActionPerformed();
                }
            }
            return ret;
        }
        
    }
    
    private SVGListCellRenderer myRenderer;
    private ListModel myModel;
    private SelectionModel mySelectionModel;
    private InputHandler myHandler;
    
    private int myIndex;

}
