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
package org.netbeans.modules.etl.ui.model.impl;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import com.sun.etl.exception.BaseException;

import javax.swing.event.UndoableEditListener;
import org.netbeans.modules.etl.model.impl.ETLDefinitionImpl;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.sql.framework.common.utils.XmlUtil;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.impl.CollabSQLUIModelImpl;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ModelSource;

/**
 * This class represents a model for GUI collaboration objects
 *
 * @author Ritesh Adval
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
    
    @Override
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

    public void removeComponentListener(ComponentListener cl) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addComponentListener(ComponentListener cl) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeUndoableEditListener(UndoableEditListener uel) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addUndoableEditListener(UndoableEditListener uel) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeUndoableRefactorListener(UndoableEditListener uel) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addUndoableRefactorListener(UndoableEditListener uel) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sync() throws IOException {
      //  throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean inSync() {
        return true;
      //  throw new UnsupportedOperationException("Not supported yet.");
    }

    public State getState() {
        return null;
      //  throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isIntransaction() {
        return true;
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean startTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void endTransaction() {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addChildComponent(Component target, Component child, int index) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeChildComponent(Component child) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public ModelSource getModelSource() {
        return null;
       // throw new UnsupportedOperationException("Not supported yet.");
    }
}

