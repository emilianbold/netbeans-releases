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
package org.netbeans.modules.mashup.db.model;

import java.util.Map;

import org.netbeans.modules.sql.framework.model.SQLDBColumn;


/**
 * Extends DBColumn to hold metadata required for parsing a flatfile field as a column in
 * a database table.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface FlatfileDBColumn extends SQLDBColumn {

    int getCardinalPosition();

    /**
     * Gets the SQL create statement to create a column representing this flatfile field.
     * 
     * @return SQL statement fragment to create of a column representing this field
     */
    String getCreateStatementSQL();

    /**
     * Gets Map of current properties associated with this field.
     * 
     * @return unmodifiable Map of current properties.
     */
    Map getProperties();

    /**
     * Gets property string associated with the given name.
     * 
     * @param propName property key
     * @return property associated with propName, or null if no such property exists.
     */
    String getProperty(String propName);

    /**
     * Indicates whether column is selected
     * 
     * @return true if selected, false otherwise
     */
    boolean isSelected();

    void setCardinalPosition(int theCardinalPosition);

}

