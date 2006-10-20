/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.bpel.debugger.ui.util;

import java.io.IOException;
import java.util.concurrent.Callable;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.model.spi.FindHelper;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Zgursky
 */
public final class ModelUtil {
    private static FindHelper findHelper =
            (FindHelper)Lookup.getDefault().lookup(FindHelper.class);
    
    private ModelUtil() {
    }
    
    public static String getXpath(final UniqueId bpelEntityId) {
        final BpelModel model = bpelEntityId.getModel();
        
        try {
            model.sync();
        } catch (IOException ex) {
            return null;
        }
        
        
        class MyRunnable implements Runnable{
            private String result = null;
            
            public String getResult(){
                return this.result;
            }
            
            public void run() {
                if (!model.getState().equals(BpelModel.State.VALID)) {
                    return;
                }
                BpelEntity bpelEntity = model.getEntity(bpelEntityId);
                if (bpelEntity != null) {
                    result = EditorContextBridge.normalizeXpath(
                            findHelper.getXPath(bpelEntity));
                }
                
                
            }
        };
        
        MyRunnable r = new MyRunnable();
        
        model.invoke(r);
        
        return r.getResult();
    }
    
    public static UniqueId getBpelEntityId(
            final BpelModel model, final String xpath)
    {
        try {
            model.sync();
        } catch (IOException ex) {
            return null;
        }
        
        class MyRunnable implements Runnable{
            private UniqueId result = null;
            
            public UniqueId getResult(){
                return this.result;
            }
            
            public void run() {
                if (!model.getState().equals(BpelModel.State.VALID)) {
                    return;
                }
        
                BpelEntity[] entities = findHelper.findModelElements(model, xpath);
                if (entities.length == 1) {
                    result = entities[0].getUID();
                }
                
            }
        };
        
        MyRunnable r = new MyRunnable();
        
        model.invoke(r);
        
        return r.getResult();
    }
    
    public static UniqueId getBpelEntityId(
            final BpelModel model, final int offset)
    {
        try {
            model.sync();
        } catch (IOException ex) {
            return null;
        }
        
        
        class MyRunnable implements Runnable{
            private UniqueId result = null;
            
            public UniqueId getResult(){
                return this.result;
            }
            
            public void run() {
                if (!model.getState().equals(BpelModel.State.VALID)) {
                    return;
                }
                BpelEntity bpelEntity = model.findElement(offset);
                if (bpelEntity != null) {
                    result =  bpelEntity.getUID();
                }
                
            }
        };
        
        MyRunnable r = new MyRunnable();
        
        model.invoke(r);
        
        return r.getResult();
    }
    
    public static int getLineNumber(final UniqueId bpelEntityId) {
        final BpelModel model = bpelEntityId.getModel();
        
        try {
            model.sync();
        } catch (IOException ex) {
            return -1;
        }
        
        class MyRunnable implements Runnable{
            private int result = -1;
            
            public int getResult(){
                return this.result;
            }
            
            public void run() {
                if (!model.getState().equals(BpelModel.State.VALID)) {
                    return;
                }
                BpelEntity bpelEntity = model.getEntity(bpelEntityId);
                if (bpelEntity != null) {
                    
                    int offset = bpelEntity.findPosition();
                    
                    StyledDocument doc =(StyledDocument)model.getModelSource().getLookup().
                            lookup(StyledDocument.class);
                    
                    result =  NbDocument.findLineNumber(doc, offset) + 1;
                }
            }
        };
        
        MyRunnable r = new MyRunnable();
        
        model.invoke(r);
        
        return r.getResult();
    }
}
