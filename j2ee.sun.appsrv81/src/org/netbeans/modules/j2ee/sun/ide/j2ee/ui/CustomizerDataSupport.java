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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
/*
 * CustomizerDataSupport.java
 *
 * Created on March 23, 2006, 10:16 PM
 *
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;

/**
 *
 * @author sherold
 */
public class CustomizerDataSupport {
    
    private final DeploymentManagerProperties dmp;
    
    // models
    private CustomizerSupport.PathModel sourceModel;
    private CustomizerSupport.PathModel classModel;
    private CustomizerSupport.PathModel javadocModel;
    
    // model dirty flags
    private boolean sourceModelFlag;
    private boolean javadocModelFlag;
    
    /**
     * Creates a new instance of CustomizerDataSupport 
     */
    public CustomizerDataSupport(DeploymentManagerProperties dmp) {
        this.dmp = dmp;
        init();
    }
    
    /** Initialize the customizer models. */
    private void init() {
        // classModel
        classModel = new CustomizerSupport.PathModel(dmp.getClasses());
        
        // sourceModel
        sourceModel = new CustomizerSupport.PathModel(dmp.getSources());
        sourceModel.addListDataListener(new ModelChangeAdapter() {
            public void modelChanged() {
                sourceModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // javadocModel
        javadocModel = new CustomizerSupport.PathModel(dmp.getJavadocs());
        javadocModel.addListDataListener(new ModelChangeAdapter() {
            public void modelChanged() {
                javadocModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
    }
    
    public CustomizerSupport.PathModel getClassModel() {
        return classModel;
    }
    
    public CustomizerSupport.PathModel getSourceModel() {
        return sourceModel;
    }
    
    public CustomizerSupport.PathModel getJavadocsModel() {
        return javadocModel;
    }
    
    // private helper methods -------------------------------------------------
    
    /** Save all changes */
    private void store() {
        if (sourceModelFlag) {
            dmp.setSources(sourceModel.getData());
            sourceModelFlag = false;
        }
        
        if (javadocModelFlag) {
            dmp.setJavadocs(javadocModel.getData());
            javadocModelFlag = false;
        }
    }
    
    // private helper class ---------------------------------------------------
    
    /** 
     * Adapter that implements several listeners, which is useful for dirty model
     * monitoring.
     */
    private abstract class ModelChangeAdapter implements ListDataListener, 
            DocumentListener, ItemListener, ChangeListener {
        
        public abstract void modelChanged();
        
        public void contentsChanged(ListDataEvent e) {
            modelChanged();
        }

        public void intervalAdded(ListDataEvent e) {
            modelChanged();
        }

        public void intervalRemoved(ListDataEvent e) {
            modelChanged();
        }

        public void changedUpdate(DocumentEvent e) {
            modelChanged();
        }

        public void removeUpdate(DocumentEvent e) {
            modelChanged();
        }

        public void insertUpdate(DocumentEvent e) {
            modelChanged();
        }

        public void itemStateChanged(ItemEvent e) {
            modelChanged();
        }

        public void stateChanged(javax.swing.event.ChangeEvent e) {
            modelChanged();
        }
    }
}
