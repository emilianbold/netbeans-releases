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
package org.netbeans.modules.mashup.db.bootstrap;

import java.util.List;

import org.netbeans.modules.mashup.db.common.FlatfileDBException;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;


/**
 * Simple interface defining a parser to be used by the Flatfile Database wizard to obtain
 * an initial set of FlatfileField instances to present to the user in the record layout
 * configuration panel.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface FlatfileBootstrapParser {

    /**
     * Gets List of FlatfileDBColumn instances which are derived from the parse properties
     * of the currently associated ParseConfigurator and the given Flatfile instance.
     * 
     * @param table FlatfileDBTable whose contents will be parsed.
     * @return List of FlatfileDBColumn instances synthesized from the contents of
     *         <code>file</code> and parameters contained in the current FlatfileDBTable
     * @throws FlatfileDBException if error occurs during retrieval
     */
    public List buildFlatfileDBColumns(FlatfileDBTable table) throws FlatfileDBException;

    /**
     * Based on the given file guess the record length and other properties
     * 
     * @param table
     * @throws FlatfileDBException
     */
    public void makeGuess(FlatfileDBTable table) throws FlatfileDBException;

    /**
     * Is this file format acceptable by this parser
     * 
     * @param table
     * @return
     * @throws FlatfileDBException
     */
    public boolean acceptable(FlatfileDBTable table) throws FlatfileDBException;
}
