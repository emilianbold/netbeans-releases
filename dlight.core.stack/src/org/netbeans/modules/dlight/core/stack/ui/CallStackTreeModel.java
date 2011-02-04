/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.core.stack.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.modules.dlight.core.stack.api.Function;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.dataprovider.SourceFileInfoDataProvider;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;

/**
 *
 * @author mt154047
 */
final class CallStackTreeModel extends DefaultTreeModel{
//    private final List<List<FunctionCall
    private SourceFileInfoDataProvider dataProvider;
    private final DefaultMutableTreeNode rootNode;
    
    CallStackTreeModel(SourceFileInfoDataProvider dataProvider) {
        this(dataProvider, new DefaultMutableTreeNode());
    }
    
    
    private CallStackTreeModel(SourceFileInfoDataProvider dataProvider, DefaultMutableTreeNode root){
        super(root);
        this.rootNode = root;
        this.dataProvider = dataProvider;
    }
    
    void clear(){
        rootNode.removeAllChildren();
        rootNode.setUserObject(null);
    }
       
    void addStack(List<FunctionCall> stack){
        List<FunctionCall> functionCalls = new ArrayList<FunctionCall>();
        functionCalls.addAll(stack);
        add(rootNode, functionCalls);
    }
    
    
    private DefaultMutableTreeNode createNode(List<FunctionCall> stack){
        DefaultMutableTreeNode result = new DefaultMutableTreeNode(stack.get(0));
        DefaultMutableTreeNode parent = result;
        for (int i = 1; i < stack.size(); i++){
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(stack.get(i));
            parent.add(node);
            parent = node;
        }
        return result;           
    }
        
    
    private void add(DefaultMutableTreeNode root, List<FunctionCall> functionCalls){
        //functionCalls are correctly ordered right now, let's start with the beginning
        if (functionCalls.isEmpty()){
            return;
        }
        FunctionCall rootCall = functionCalls.get(0);
        Function function = rootCall.getFunction();        
        boolean notFound = true;
        for (int i = 0; i < root.getChildCount(); i++){
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)root.getChildAt(i);
            FunctionCall childFunctionCall = (FunctionCall)child.getUserObject();
            if (childFunctionCall == null){
                return;
            }
            //get offset inside the 
            Function childFunction = childFunctionCall.getFunction();
            long offset = childFunctionCall.getOffset();
            if (!function.equals(childFunction)){
                continue;                
            }
            //check offset inside the function
            if (offset !=  rootCall.getOffset()){
                continue;
            }                        
            //here we are: let's try to the current root node stack staring from the 1 index
            FunctionCall[] stack = functionCalls.toArray(new FunctionCall[0]);

            FunctionCall[] resultStack = new FunctionCall[stack.length -1];
            System.arraycopy(stack, 1, resultStack, 0, stack.length -1);
            notFound = false;
            add(child, Arrays.asList(resultStack));            
        }                
        if(notFound){
           root.add(createNode(functionCalls));
        }        
    }
    
    
    List<FunctionCall> getRootChildren(){
        List<FunctionCall> result = new ArrayList<FunctionCall>();
        for (int i = 0; i < rootNode.getChildCount(); i++){
            FunctionCall call = (FunctionCall)((DefaultMutableTreeNode)rootNode.getChildAt(i)).getUserObject();
            if (call != null){
                result.add(call);
            }                    
        }
        return result;
    }
    
    List<FunctionCall> getCallers(FunctionCall call){
        //find the treeNodes children        
        //find the function call 
        
//        //find in the list
//        int index = stack.indexOf(call);
//        //if the last one show it self
//        //return the next one
//        if (index == 0 ){
//            return null;
//        }
//        return stack.get(index - 1);
        DefaultMutableTreeNode node = findByFunctionCall(rootNode,call);
        List<FunctionCall> result = new ArrayList<FunctionCall>();
        for (int i = 0; i < node.getChildCount(); i ++){
            FunctionCall fCall = (FunctionCall)((DefaultMutableTreeNode)node.getChildAt(i)).getUserObject();
            if (fCall != null){
                result.add(fCall);
            }                
        }
        return result;
    }
    
    DefaultMutableTreeNode findByFunctionCall(DefaultMutableTreeNode node, FunctionCall call){
        FunctionCall fCall = (FunctionCall)node.getUserObject();
        if (fCall != null){
            Function childFunction = fCall.getFunction();
            long offset = fCall.getOffset();
            if (call.getFunction().equals(childFunction) && offset == call.getOffset()){
                return  node;
            }
        }
        
        for (int i = 0; i < node.getChildCount(); i ++){
            final DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(i);
            DefaultMutableTreeNode result = findByFunctionCall(child, call);
            if (result != null){
                return result;
            }
            
        }        
        return null;
    }
    

    SourceFileInfo getSourceFileInfo(FunctionCall call){
        return dataProvider.getSourceFileInfo(call);
    }

    SourceFileInfoDataProvider getSourceFileInfoProvider(){
        return dataProvider;
    }

    
    void setSourceFileInfoProvider(SourceFileInfoDataProvider p){
        this.dataProvider = p;
    }
    
    

}
