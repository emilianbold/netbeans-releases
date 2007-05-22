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
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
 *
 * Contributor(s): Petr Hrebejk
 */

package org.netbeans.modules.jumpto.file;

import java.util.List;
import javax.swing.ListModel;

/** Unmodifyiable ListModel based on a List.
 *
 * @author Petr Hrebejk
 */
    
class ListListModel<T> implements ListModel {
    
    private List list;
    private Object last;

    /** Creates a new instance of IteratorList */
    public ListListModel( List<? extends T> list ) {
        this( list, null );
    }

    public ListListModel( List<? extends T> list, Object last ) {
        this.list = list;
        this.last = last;
        System.out.println("Creating with last " + last);
    }
    
    // List implementataion ------------------------------------------------

    public Object getElementAt(int index) {
        // System.out.println("GE " + index );
        
        if ( last != null && index == list.size() ) {
            return last;
        }
        
        return list.get( index );
    }

    public int getSize() {
        return list.size() + (last == null ? 0 : 1);
    }

    public void removeListDataListener(javax.swing.event.ListDataListener l) {
        // Does nothing - unmodifiable
    }

    public void addListDataListener(javax.swing.event.ListDataListener l) {
        // Does nothing - unmodifiable
    }
    
}
