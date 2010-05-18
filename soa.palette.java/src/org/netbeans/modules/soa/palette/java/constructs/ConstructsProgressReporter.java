/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.soa.palette.java.constructs;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author Useradmin
 */
public class ConstructsProgressReporter {
    private static final int CODE_GEN_START_PERCENTAGE = 50;
    private static final int UPDATING_SRC_PERCENTAGE = 90;
    private static String GEN_CODE_MSG = null;
    private ProgressHandle h = null;
    private int nodes = 0;
    private int processedNodes = 0;
    private int tProcessedNodes = 0;
          
    public ConstructsProgressReporter(){
        String msg = getMessage("MSG_progressbar"); //NOI18N                
        h = ProgressHandleFactory.createHandle(msg);
    }
    
    private String getMessage(String key){
        return NbBundle.getMessage(ConstructsProgressReporter.class, key); 
    }
    
    private String getGeneratingCodeMsg(){
        if (GEN_CODE_MSG == null){
            GEN_CODE_MSG = getMessage("MSG_generatingCode"); //NOI18N
        }
        return GEN_CODE_MSG;
    }
    
    public void start(){
        h.start(100);            
    };
    
    public void readingXMLFile(){
        String msg = getMessage("MSG_progressReadingXML"); //NOI18N
        h.progress(msg, 10);           
    }

    public void readXMLFile(){
        String msg = getMessage("MSG_creatingDomNodes"); //NOI18N
        h.progress(msg, 15);           
    }

    public void creatingNodes(){
        String msg = getMessage("MSG_creatingDomNodes"); //NOI18N
        h.progress(msg, 20);           
    }

    public void generatingCode(int nodes){
        this.nodes = nodes;
        String msg = getGeneratingCodeMsg();
        h.progress(msg, CODE_GEN_START_PERCENTAGE);           
    }
    
    public void processedNode(){
        this.tProcessedNodes++;
        if (this.tProcessedNodes >= 10){
            this.processedNodes = this.processedNodes + this.tProcessedNodes;
            this.tProcessedNodes = 0;
            float cgProgress = ((UPDATING_SRC_PERCENTAGE - 
                    CODE_GEN_START_PERCENTAGE) * this.processedNodes)/this.nodes;
            int totalProgress = (int) (CODE_GEN_START_PERCENTAGE + cgProgress);
            if (totalProgress >= UPDATING_SRC_PERCENTAGE){
                totalProgress = UPDATING_SRC_PERCENTAGE;
            }
            String msg = getGeneratingCodeMsg();            
            h.progress(msg, totalProgress);
        }
    }    
    
    public void updatingSource(){
        String msg = getMessage("MSG_ProgressUpdatingSource"); //NOI18N
        h.progress(msg, UPDATING_SRC_PERCENTAGE);                   
    }
    
    public void updatedSource(){
        h.finish();
    }
    
}
