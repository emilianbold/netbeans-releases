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

package org.netbeans.modules.websvc.registry.nodes;

import org.netbeans.modules.websvc.api.registry.WebServiceMethod;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.nodes.Sheet;
import org.openide.nodes.*;
import org.openide.nodes.Node.Cookie;
import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet.Set;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.Utilities;

import org.netbeans.modules.websvc.registry.actions.TestWebServiceMethodAction;
import org.netbeans.modules.websvc.registry.util.Util;

import com.sun.xml.rpc.processor.model.Operation;
import com.sun.xml.rpc.processor.model.Port;
import com.sun.xml.rpc.processor.model.java.JavaMethod;
import com.sun.xml.rpc.processor.model.java.JavaType;
import com.sun.xml.rpc.processor.model.java.JavaParameter;

import java.awt.Image;
import java.util.Iterator;
import javax.swing.Action;

/**
 * A simple node with no children.
 * Often used in conjunction with some kind of underlying data model, where
 * each node represents an element in that model. In this case, you should see
 * the Container Node template which will permit you to create a whole tree of
 * such nodes with the proper behavior.
 * @author octav
 */
public class WebServiceMethodNode extends AbstractNode implements WebServiceMethod {
    
    private JavaMethod javaMethod;
    private Port port;
    
    public WebServiceMethodNode() {
        this(null,null);
    }
    
    public Object getJavaMethod() {
        return javaMethod;
    }
    // will frequently accept an element from some data model in the constructor:
    public WebServiceMethodNode(Port inPort, Operation inOperation) {
        super(Children.LEAF);
//        setDefaultAction(SystemAction.get(TestWebServiceMethodAction.class));
        
        /**
         * Bug fix: 5059732
         * We need to get the port that this method is on to correctly get the type for the parameter.
         *
         */
        
        port = inPort;
        if(null == inOperation) {
            return;
        }
        javaMethod = inOperation.getJavaMethod();
        setName(javaMethod.getName());
        setIconBaseWithExtension("org/netbeans/modules/websvc/registry/resources/methodicon.png");
        
        /**
         * Set the shortDescription (tooltip) to the method signature
         */
        String signature = javaMethod.getReturnType().getFormalName() + " " + javaMethod.getName() + "(";
        Iterator parameterIterator = javaMethod.getParameters();
        JavaParameter currentParam = null;
        while(parameterIterator.hasNext()) {
            currentParam = (JavaParameter)parameterIterator.next();
            String parameterType = Util.getParameterType(inPort, currentParam);
            signature += parameterType + " " + currentParam.getName();
            if(parameterIterator.hasNext()) {
                signature += ", ";
            }
        }
        signature += ")";
        
        setShortDescription(signature);
        // Add cookies, e.g.:
        /*
        getCookieSet().add(new OpenCookie() {
                public void open() {
                    // Open something useful...
                    // will typically use the data model somehow
                }
            });
         */
        // If this node represents an element in a data model of some sort, consider
        // creating your own cookie which captures the existence of that underlying data,
        // and add it to the cookie set. Then you can write actions sensitive to that cookie,
        // and they will not need to directly refer to this node class - only to the cookie
        // and the data model.
		
		getCookieSet().add(this);
    }
    
    // Create the popup menu:
//    protected SystemAction[] createActions() {
//        return new SystemAction[] {
//            SystemAction.get(TestWebServiceMethodAction.class)
//        };
//    }
    
	public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(TestWebServiceMethodAction.class)
        };
	}
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // When you have help, change to:
        // return new HelpCtx(WebServiceMethodNode.class);
    }
    
    // RECOMMENDED - handle cloning specially (so as not to invoke the overhead of FilterNode):
    /*
    public Node cloneNode() {
        // Try to pass in similar constructor params to what you originally got,
        // typically meaning passing in the same data model element:
        return new WebServiceMethodNode();
    }
     */
    
    /**
     * Create a property sheet for the individual method node
     * @return property sheet for the data source nodes
     */
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Set ss = sheet.get("data"); // NOI18N
        
        if (ss == null) {
            ss = new Set();
            ss.setName("data");  // NOI18N
            ss.setDisplayName(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_INFO"));
            ss.setShortDescription(NbBundle.getMessage(WebServiceMethodNode.class, "METHOD_INFO"));
            sheet.put(ss);
        }
        
        try {
            Reflection p;
            
            p = new Reflection(javaMethod, String.class, "getName", null); // NOI18N
            p.setName("name"); // NOI18N
            p.setDisplayName(NbBundle.getMessage(WebServicesNode.class, "METHOD_NAME"));
            p.setShortDescription(NbBundle.getMessage(WebServicesNode.class, "METHOD_NAME"));
            ss.put(p);
            String signature = javaMethod.getReturnType().getRealName() + " " +
            javaMethod.getName() + "(";
            
            Iterator tempIterator = javaMethod.getParameters();
            JavaParameter currentparam = null;
            while(tempIterator.hasNext()) {
                currentparam = (JavaParameter)tempIterator.next();
                signature += currentparam.getType().getRealName() + " " + currentparam.getName();
                if(tempIterator.hasNext()) {
                    signature += ", ";
                }
            }
            
            signature += ")";
            
            Iterator excpIterator = javaMethod.getExceptions();
            if(excpIterator.hasNext()) {
                signature += " throws";
                while(excpIterator.hasNext()) {
                    String currentExcp = (String)excpIterator.next();
                    signature +=  " " + currentExcp;
                    if(excpIterator.hasNext()) {
                        signature +=  ",";
                    }
                }
                
                
            }
            
            p = new Reflection(signature, String.class, "toString", null); // NOI18N
            p.setName("signature"); // NOI18N
            p.setDisplayName(NbBundle.getMessage(WebServicesNode.class, "METHOD_SIGNATURE"));
            p.setShortDescription(NbBundle.getMessage(WebServicesNode.class, "METHOD_SIGNATURE"));
            ss.put(p);
            
            p = new Reflection(javaMethod.getReturnType(), String.class, "getRealName", null); // NOI18N
            p.setName("returntype"); // NOI18N
            p.setDisplayName(NbBundle.getMessage(WebServicesNode.class, "METHOD_RETURNTYPE"));
            p.setShortDescription(NbBundle.getMessage(WebServicesNode.class, "METHOD_RETURNTYPE"));
            ss.put(p);
            
            Set paramSet = sheet.get("parameters"); // NOI18N
            if (paramSet == null) {
                paramSet = new Sheet.Set();
                paramSet.setName("parameters"); // NOI18N
                paramSet.setDisplayName(NbBundle.getMessage(WebServicesNode.class, "METHOD_PARAMDIVIDER")); // NOI18N
                paramSet.setShortDescription(NbBundle.getMessage(WebServicesNode.class, "METHOD_PARAMDIVIDER")); // NOI18N
                sheet.put(paramSet);
            }
            Iterator paramIterator = javaMethod.getParameters();
            if(paramIterator.hasNext()) {
                p = new Reflection(NbBundle.getMessage(WebServicesNode.class, "METHOD_PARAMTYPE"),
                String.class,
                "toString",
                null); // NOI18N
                p.setName("paramdivider2"); // NOI18N
                p.setDisplayName(NbBundle.getMessage(WebServicesNode.class, "METHOD_PARAMNAME"));
                p.setShortDescription(NbBundle.getMessage(WebServicesNode.class, "METHOD_PARAMNAME") +
                "-" + NbBundle.getMessage(WebServicesNode.class, "METHOD_PARAMTYPE"));
                paramSet.put(p);
                
                
                JavaParameter currentParameter = null;
                for(int ii=0;paramIterator.hasNext();ii++) {
                    currentParameter = (JavaParameter)paramIterator.next();
                    if(currentParameter.getType().isHolder()) {
                        p = new Reflection(Util.getParameterType(port,currentParameter), String.class, "toString", null); // NOI18N
                    } else {
                        p = new Reflection(currentParameter.getType(), String.class, "getRealName", null); // NOI18N
                    }
                    p.setName("paramname" + ii); // NOI18N
                    p.setDisplayName(currentParameter.getName());
                    p.setShortDescription(currentParameter.getName() + "-" +
                    currentParameter.getType().getRealName());
                    paramSet.put(p);
                }
            }
            Set exceptionSet = sheet.get("exceptions"); // NOI18N
            if (exceptionSet == null) {
                exceptionSet = new Sheet.Set();
                exceptionSet.setName("exceptions"); // NOI18N
                exceptionSet.setDisplayName(NbBundle.getMessage(WebServicesNode.class, "METHOD_EXCEPTIONDIVIDER")); // NOI18N
                exceptionSet.setShortDescription(NbBundle.getMessage(WebServicesNode.class, "METHOD_EXCEPTIONDIVIDER")); // NOI18N
                sheet.put(exceptionSet);
            }
            
            Iterator exceptionIterator = javaMethod.getExceptions();
            String currentException = null;
            for(int ii=0;exceptionIterator.hasNext();ii++) {
                currentException = (String)exceptionIterator.next();
                p = new Reflection(currentException, String.class, "toString", null); // NOI18N
                p.setName("exception" + ii); // NOI18N
                p.setDisplayName(NbBundle.getMessage(WebServicesNode.class, "METHOD_PARAMTYPE"));
                p.setShortDescription(NbBundle.getMessage(WebServicesNode.class, "METHOD_PARAMTYPE"));
                exceptionSet.put(p);
            }
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
        }
        
        return sheet;
    }
    
    // Handle renaming:
    /*
    public boolean canRename() {
        return true;
    }
    public void setName(String nue) {
        // Typically implemented by changing the name of an element from an underlying
        // data model. This node class should be listening to changes in the name of the
        // element and calling super.setName when it notices any (or better, override getName
        // and perhaps getDisplayName and call fireNameChange).
        // For example, if there is an instance field
        // private final MyDataElement data;
        // then you might write this method as:
        // data.setID(nue);
        // where you would also have:
        // public String getName() {return data.getID();}
        // and in the constructor, if WebServiceMethodNode implements ModelListener:
        // data.addModelListener((ModelListener)WeakListener.create(ModelListener.class, this, data));
        // where the interface is implemented as:
        // public void modelChanged(ModelEvent ev) {fireNameChange(null, null);}
    }
     */
    
    // Handle deleting:
    /*
    public boolean canDestroy() {
        return true;
    }
    public void destroy() throws IOException {
        // Typically implemented by removing an element from an underlying data model.
        // For example, if there is an instance field
        // private final MyDataElement data;
        // then you might write this method as:
        // data.getContainingModel().removeElement(data);
        // The parent container children should be listening to the model, notice
        // the removal, set a new key list without this data element, and thus
        // remove this node from its children list.
    }
     */
    
    // Handle copying and cutting specially:
    /*
    public boolean canCopy() {
        return true;
    }
    public boolean canCut() {
        return true;
    }
    public Transferable clipboardCopy() {
        // Add to, do not replace, the default node copy flavor:
        ExTransferable et = ExTransferable.create(super.clipboardCopy());
        et.put(new ExTransferable.Single(DataFlavor.stringFlavor) {
                protected Object getData() {
                    // just an example:
                    return WebServiceMethodNode.this.getDisplayName();
                    // more commonly, will use some underlying data model
                }
            });
        return et;
    }
    public Transferable clipboardCut() {
        // Add to, do not replace, the default node cut flavor:
        ExTransferable et = ExTransferable.create(super.clipboardCut());
        // This is not so useful because this node will not be destroyed afterwards
        // (it is up to the paste type to decide whether to remove the "original",
        // and it is not safe to assume that getData will only be called once):
        et.put(new ExTransferable.Single(DataFlavor.stringFlavor) {
                protected Object getData() {
                    // just an example:
                    return WebServiceMethodNode.this.getDisplayName();
                    // more commonly, will use some underlying data model
                }
            });
        return et;
    }
     */
    
    // Permit user to customize whole node at once (instead of per-property):
    /*
    public boolean hasCustomizer() {
        return true;
    }
    public Component getCustomizer() {
        // more commonly, will pass in underlying data:
        return new MyCustomizingPanel(this);
    }
     */
    
}
