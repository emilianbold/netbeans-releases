/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dm.virtual.db.bootstrap;

import java.util.List;

import org.netbeans.modules.dm.virtual.db.model.VirtualDBException;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;


/**
 * Simple interface defining a parser to be used by the Virtual Database wizard to obtain
 * an initial set of table column to present to the user in the record layout
 * configuration panel.
 * 
 * @author Ahimanikya Satapathy
 */
public interface VirtualTableBootstrapParser {

    /**
     * Gets List of VirtualDBColumn instances which are derived from the parse properties
     * of the currently associated ParseConfigurator and the given Virtual DB instance.
     * 
     * @param table VirtualDBTable whose contents will be parsed.
     * @return List of VirtualDBColumn instances synthesized from the contents of
     *         <code>file</code> and parameters contained in the current VirtualDBTable
     * @throws VirtualDBException if error occurs during retrieval
     */
    public List buildVirtualDBColumns(VirtualDBTable table) throws VirtualDBException;

    /**
     * Based on the given file guess the record length and other properties
     * 
     * @param table
     * @throws VirtualDBException
     */
    public void makeGuess(VirtualDBTable table) throws VirtualDBException;

    /**
     * Is this file format acceptable by this parser
     * 
     * @param table
     * @return
     * @throws VirtualDBException
     */
    public boolean acceptable(VirtualDBTable table) throws VirtualDBException;
}
