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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;

/**
 *
 * @author girix
 */
public interface CatalogFileWrapper {
    
    public List<CatalogEntry> getSystems();
    public void setSystem(int index, CatalogEntry catEnt) throws IOException;
    public void deleteSystem(int index) throws IOException;
    public void addSystem(CatalogEntry catEnt) throws IOException;
    
    public List<CatalogEntry> getRewriteSystems();
    public void setRewriteSystem(int index, CatalogEntry catEnt) throws IOException;
    public void deleteRewriteSystem(int index) throws IOException;
    public void addRewriteSystem(CatalogEntry catEnt) throws IOException;
    
    
    public List<CatalogEntry> getDelegateSystems() ;
    public void setDelegateSystem(int index, CatalogEntry catEnt) throws IOException ;
    public void deleteDelegateSystem(int index) throws IOException;
    public void addDelegateSystem(CatalogEntry catEnt) throws IOException ;
    
    //for listening to the state of the wraper object
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    public void removePropertyChangeListener(PropertyChangeListener l);


    public void close();
    
    public void cleanInstance();

    public Model.State getCatalogState();

    public List<CatalogEntry> getNextCatalogs();
    public void addNextCatalog(CatalogEntry catEnt)throws IOException ;
    public void deleteNextCatalog(int index)throws IOException ;
    
    
}
