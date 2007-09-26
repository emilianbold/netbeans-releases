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
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
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
        // System.out.println("Creating with last " + last);
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
