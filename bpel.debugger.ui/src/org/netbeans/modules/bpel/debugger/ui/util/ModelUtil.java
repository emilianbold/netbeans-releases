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

package org.netbeans.modules.bpel.debugger.ui.util;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.text.StyledDocument;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.api.psm.ProcessStaticModel;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.model.spi.FindHelper;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Alexander Zgursky
 */
public final class ModelUtil {
    private static FindHelper findHelper =
            (FindHelper)Lookup.getDefault().lookup(FindHelper.class);
    
    private ModelUtil() {
    }
    
    public static FindHelper getFindHelper() {
        return findHelper;
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
                
//                BpelEntity[] entities = findHelper.findModelElements(model, xpath);
//                if (entities.length == 1) {
//                    result = entities[0].getUID();
//                }
                BpelEntity entity = findBpelEntity(model, xpath);
                if (entity != null) {
                    result = entity.getUID();
                }
                
            }
        };
        
        MyRunnable r = new MyRunnable();
        
        model.invoke(r);
        
        return r.getResult();
    }
    
    private static BpelEntity findBpelEntity(BpelModel model, String xpathExpression) {
        if (model.getProcess() == null) {
            return null;
        }
        Document doc = model.getProcess().getPeer().getOwnerDocument();
        XPath xpath = XPathFactory.newInstance().newXPath();
        NamespaceContext myNsContext = new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                if (ProcessStaticModel.BPEL_NAMESPACE_PREFIX.equals(prefix)) {
                    return ProcessStaticModel.BPEL_NAMESPACE_URI;
                } else {
                    return XMLConstants.NULL_NS_URI;
                }
            }
            public String getPrefix(String namespaceURI) {
                if (ProcessStaticModel.BPEL_NAMESPACE_URI.equals(namespaceURI)) {
                    return ProcessStaticModel.BPEL_NAMESPACE_PREFIX;
                } else {
                    return null;
                }
            }
            public Iterator getPrefixes(String namespaceURI) {
                String prefix = getPrefix(namespaceURI);
                if (prefix != null) {
                    return Collections.singletonList(prefix).iterator();
                } else {
                    return Collections.emptyList().iterator();
                }
            }
        };
        
        xpath.setNamespaceContext(myNsContext);
        Node node;
        try {
            node = (Node)xpath.evaluate(xpathExpression, doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            return null;
        }
        
        if ( node instanceof Element ){
            AbstractDocumentModel<BpelEntity> xamModel = (AbstractDocumentModel<BpelEntity>)model;
            List<Element> pathToRoot = xamModel.getAccess().
                    getPathFromRoot(xamModel.getDocument(), 
                            (Element)node );
            Component comp = xamModel.findComponent(pathToRoot);
            if (comp instanceof BpelEntity) {
                return (BpelEntity)comp;
            } else {
                return null;
            }
        } else {
            return null;
        }
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
                    result = bpelEntity.findPosition();
                }
            }
        };
        
        MyRunnable r = new MyRunnable();
        model.invoke(r);
        int offset = r.getResult();
        if (offset < 0) {
            return -1;
        }
        
        StyledDocument doc =(StyledDocument)model.getModelSource().getLookup().
                lookup(StyledDocument.class);

        return NbDocument.findLineNumber(doc, offset) + 1;
    }
}
