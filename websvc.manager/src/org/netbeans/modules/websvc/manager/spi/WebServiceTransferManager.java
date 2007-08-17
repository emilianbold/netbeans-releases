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
package org.netbeans.modules.websvc.manager.spi;

import java.awt.datatransfer.Transferable;

/**
 * 
 * Service class that allows consumers to add to the DataFlavors present in 
 * the web service node Transferable.
 * 
 * @author quynguyen
 */
public interface WebServiceTransferManager {
    
    /**
     * Add DataFlavors specific to a web service consumer to the base <code>Transferable</code>.
     * This method must not modify existing <code>DataFlavor</code> to data mappings.
     * 
     * @param t the base <code>Transferable</code>
     * @return a <code>Transferable</code> that has the same data flavors as <code>t</code> with possible additions
     */
    public Transferable addDataFlavors(Transferable t);
}
