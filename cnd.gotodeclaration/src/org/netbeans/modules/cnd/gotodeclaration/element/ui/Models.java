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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.element.ui;

import javax.swing.ListModel;
import java.util.List;

/**
 *
 * @author vk155633
 */
public class Models {
    
    public static <T> ListModel fromList( List<? extends T> list ) {
        return new ListListModel<T>( list );
    }

 
    // Private innerclasses ----------------------------------------------------        
    
    private static class ListListModel<T> implements ListModel {
    
        private List<? extends T> list;

        /** Creates a new instance of IteratorList */
        public ListListModel( List<? extends T> list ) {
            this.list = list;
        }

        // List implementataion ------------------------------------------------

        public T getElementAt(int index) {
            // System.out.println("GE " + index );
            return list.get( index );
        }

        public int getSize() {
            return list.size();
        }

        public void removeListDataListener(javax.swing.event.ListDataListener l) {
            // Does nothing - unmodifiable
        }

        public void addListDataListener(javax.swing.event.ListDataListener l) {
            // Does nothing - unmodifiable
        }

    }
    

}
