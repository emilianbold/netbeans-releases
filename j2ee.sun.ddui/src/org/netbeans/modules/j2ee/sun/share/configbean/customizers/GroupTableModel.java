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
package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import javax.swing.table.TableModel;

/**
 *
 * @author peterw99
 */
public interface GroupTableModel extends TableModel {

    /** Model manipulation
     */
    public int addElement(String entry);
    public int replaceElement(String oldEntry, String newEntry);
    public int removeElement(String entry);
    public boolean contains(String entry);
    public String getElementAt(int rowIndex);

}
