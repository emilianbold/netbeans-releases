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
package org.netbeans.modules.etl.ui.model.impl;

import java.io.StringReader;
import java.util.List;

import com.sun.sql.framework.exception.BaseException;

import org.netbeans.modules.etl.model.impl.ETLDefinitionImpl;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.sql.framework.common.utils.XmlUtil;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.impl.CollabSQLUIModelImpl;
import org.w3c.dom.Element;

/**
 * This class represents a model for GUI collaboration objects
 *
 * @author Ritesh Adval
 * @version $Revision$
 */

public class ETLCollaborationModel extends CollabSQLUIModelImpl implements CollabSQLUIModel {
    
    protected ETLDefinitionImpl etlDefinition;
    private String eTLXml;
    private ETLDataObject dObj;
    
    public ETLCollaborationModel() {
    }
    
    public ETLCollaborationModel(String name) {
        this();
        try {
            this.etlDefinition = new ETLDefinitionImpl(name);
        } catch (Exception ex) {
            // ignore
        }
        etlDefinition.addSQLObjectListener(this);
        super.setSQLDefinition(etlDefinition.getSQLDefinition());
        this.isReloaded = false;
    }
    
    public ETLCollaborationModel(ETLDataObject mObj) throws BaseException {
        this();
        this.dObj = mObj;
        try {
            this.etlDefinition = new ETLDefinitionImpl(this.dObj.getName());
        } catch (Exception ex) {
            // ignore
        }
        etlDefinition.addSQLObjectListener(this);
        super.setSQLDefinition(etlDefinition.getSQLDefinition());
        this.isReloaded = false;
        
    }
    
    // Reload
    public ETLCollaborationModel(ETLDataObject mObj, String etlDefinitionXml) throws BaseException {
        this();
        this.dObj = mObj;
        this.eTLXml = etlDefinitionXml;
        this.isReloaded = true;
    }
    
    public ETLDefinitionImpl getETLDefinition() {
        return etlDefinition;
    }
    
    public List getSourceDatabaseModels() {
        return etlDefinition.getSourceDatabaseModels();
    }
    
    public List getTargetDatabaseModels() {
        return etlDefinition.getTargetDatabaseModels();
    }
    
    public boolean isDrawingRequired() {
        boolean reloadStatus = isReloaded;
        this.isReloaded = false;
        return reloadStatus;
    }
    
    public void reLoad() throws BaseException {
        reLoad(this.eTLXml);
    }
    
    public void reLoad(String etlDefinitionXml) throws BaseException {
        this.eTLXml = etlDefinitionXml;
        
        // clear the listener
        if (this.etlDefinition != null) {
            this.etlDefinition.removeSQLObjectListener(this);
        }
        
        this.etlDefinition = new ETLDefinitionImpl(XmlUtil.loadXMLFile(new StringReader(etlDefinitionXml)), null);
        this.etlDefinition.addSQLObjectListener(this);
        super.setSQLDefinition(etlDefinition.getSQLDefinition());
    }
    
    public void setDefinitionContent(String definitionXml) {
        this.eTLXml = definitionXml;
    }
    
    public void setDefinitionContent(ETLDefinitionImpl etlDefn) {
        try {
            if(etlDefinition != null) {
                this.etlDefinition.removeSQLObjectListener(this);
            }            
            this.etlDefinition = etlDefn;            
            this.eTLXml = etlDefinition.toXMLString("");
            etlDefinition.addSQLObjectListener(this);
            super.setSQLDefinition(etlDefinition.getSQLDefinition());
        } catch (BaseException ex) {
            ex.printStackTrace();
        }
    }
}

