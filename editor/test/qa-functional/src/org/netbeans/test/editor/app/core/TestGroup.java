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
package org.netbeans.test.editor.app.core;

import java.beans.*;
import java.util.Vector;

import org.w3c.dom.Element;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
//import java.beans.beancontext.*;
import java.util.Set;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collections;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.beans.beancontext.BeanContext;

import org.w3c.dom.Element;
import java.util.Collection;
import javax.swing.tree.TreeNode;
import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.core.actions.ActionRegistry;
import org.netbeans.test.editor.app.gui.actions.TreeNewType;
import org.netbeans.test.editor.app.gui.tree.TestGroupNodeDelegate;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;
import org.netbeans.test.editor.app.util.ParsingUtils;

/**
 *
 * @author  ehucka
 * @version
 */
public abstract class TestGroup extends TestNode {
    
    public final static String CHANGE_CHILD = "Change child";
    public final static String REMOVE_CHILD = "Remove node";
    public final static String REMOVE_CHILDS = "Remove nodes";
    public final static String ADD_CHILD = "Add child";
    public final static String ADD_CHILDS = "Add childs";
    public final static String UP_CHILD = "Up child";
    public final static String DOWN_CHILD = "Down child";
    
    private Vector nodes;
    private boolean performFinished = false;
    
    /** Creates new TestGroup */
    public TestGroup(String name) {
        super(name);
        nodes=new Vector();
        registerNewTypes();
    }
    
    public TestGroup(Element node) {
        super(node);
        nodes = new Vector();
        addNodes(ParsingUtils.loadSubNodes(node));
        registerNewTypes();
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        return ParsingUtils.saveSubNodes(node, nodes);
    }
    
    public void addNode(TestNode node) {
        node.owner = this;
        nodes.add(node);
        super.firePropertyChange(ADD_CHILD,null,node);
    }
    
    public void addNodes(TestNode[] n) {
        for (int i=0;i < n.length;i++) {
            nodes.add(n[i]);
            n[i].owner=this;
        }
        firePropertyChange(ADD_CHILDS,null,n);
    }
    
    public void addNodes(Vector n) {
        for (int i=0;i < n.size();i++) {
            TestNode element = (TestNode) n.get(i);
            nodes.add(element);
            element.owner=this;
        }
        firePropertyChange(ADD_CHILDS,null,n.toArray(new TestNode[] {}));
    }
    
    public Vector getChildNodes() {
        return nodes;
    }
    
    public TestNode get(int i) {
        return (TestNode)nodes.elementAt(i);
    }
    
    public int getChildCount() {
        return nodes.size();
    }
    
    public TestNode remove(int i) {
        TestNode result = (TestNode) nodes.remove(i);
        firePropertyChange(REMOVE_CHILD,null,result);
        return result;
    }
    
    public void remove(TestNode node) {
        nodes.remove(node);
        firePropertyChange(REMOVE_CHILD,null,node);
    }
    
    public void removeNodes(TestNode[] n) {
        for (int i=0;i < n.length;i++) {
            nodes.remove(n[i]);
        }
        firePropertyChange(REMOVE_CHILDS,null,n);
    }
    
    public void removeNodes(Vector n) {
        for (int i=0;i < n.size();i++) {
            TestNode element = (TestNode) n.get(i);
            nodes.remove(element);
        }
        firePropertyChange(REMOVE_CHILDS,null,n.toArray(new TestNode[] {}));
    }
    
    public void removeAll() {
        Vector old=nodes;
        nodes=new Vector(); /// !!!!!!!!!!
        firePropertyChange(REMOVE_CHILDS,null,old.toArray(new TestNode[] {}));
    }
    
    public void upNode(TestNode n) {
        TestNode upper;
        
        for (int i=0;i < nodes.size();i++) {
            if (((TestNode)nodes.get(i)) == n && i > 0) {
                upper=(TestNode)(nodes.remove(i-1));
                nodes.insertElementAt(upper,i);
                firePropertyChange(UP_CHILD,null,n);
                break;
            }
        }
    }
    
    public void downNode(TestNode n) {
        TestNode down;
        for (int i=0;i < nodes.size();i++) {
            if (((TestNode)nodes.get(i)) == n && i < nodes.size()-1) {
                down=(TestNode)(nodes.remove(i+1));
                nodes.insertElementAt(down,i);
                firePropertyChange(DOWN_CHILD,null,n);
                break;
            }
        }
    }
    
    public TestNode[] getChilds() {
        int i,c;
        TestNode[] ret;
        
        c=nodes.size();
        ret=new TestNode[c];
        for(i=0;i < c;i++) {
            ret[i]=get(i);
        }
        return ret;
    }
    
    public boolean isParent() {
        return true;
    }
    
    public void perform(String what) {
        int point = what.indexOf('.');
        
        if (point == -1) {
            //Only call action
            for (int cntr = 0; cntr < getChildCount(); cntr++) {
                final TestNode node = get(cntr);
                performFinished = false;
                
                if (node instanceof TestCallAction && node.getName().equals(what)) {
                    System.err.println("CallAction "+node.getName()+" is automatically performed.");
                    ((TestCallAction)node).performAndWait();
                    System.err.println("CallAction "+node.getName()+" after performing.");
                    return;
                }
            }
            System.err.println("Call action: " + what + " not found.");
            return;
        }
        
        String step = what.substring(0, point);
        
        for (int cntr = 0; cntr < getChildCount(); cntr++) {
            TestNode node = get(cntr);
            
            if (node instanceof TestGroup && node.getName().equals(step)) {
                ((TestGroup)node).perform(what.substring(point + 1));
                return;
            }
        }
        System.err.println("SubTest: " + step + " not found.");
    }
    
    public Vector getPerformedActions(String what) {
        int point = what.indexOf('.');
        
        if (point == (-1)) {
            //Only call action
            for (int cntr = 0; cntr < getChildCount(); cntr++) {
                final TestNode node = get(cntr);
                performFinished = false;
                
                if (node instanceof TestCallAction && node.getName().equals(what)) {
                    return ((TestCallAction)node).getPerformedActions();
                };
            };
            return null;
        };
        String step = what.substring(0, point);
        
        for (int cntr = 0; cntr < getChildCount(); cntr++) {
            TestNode node = get(cntr);
            if (node instanceof TestGroup && node.getName().equals(step)) {
                return ((TestGroup)node).getPerformedActions(what.substring(point + 1));
            };
        };
        return null;
    }
    
    protected abstract void registerNewTypes();
    
    public static final void createNewTypes() {
        ActionRegistry.getDefault().registerNewType(TestSubTest.class,new TreeNewType() {
            public void create(TestGroup group) {
                TestSubTest st;
                if (Main.question("Create new Logger for new Sub Test?")) {
                    st=new TestSubTest(getNameCounter(),new Logger(Main.frame.getEditor()));
                } else {
                    st=new TestSubTest(getNameCounter(),((Test)group).getLogger());
                }
                group.addNode(st);
            }
            
            public String getName() {
                return "Sub Test";
            }
            
            public String getHelpCtx() {
                return "Create new Sub Test (Test Suite)";
            }
        });
        
        ActionRegistry.getDefault().registerNewType(TestStep.class,new TreeNewType() {
            public void create(TestGroup group) {
                group.addNode(new TestStep(getNameCounter()));
            }
            
            public String getName() {
                return "Test Step";
            }
            
            public String getHelpCtx() {
                return "Add test step - test case.";
            }
        });
        
        ActionRegistry.getDefault().registerNewType(TestCallAction.class,new TreeNewType() {
            public void create(TestGroup group) {
                group.addNode(new TestCallAction(getNameCounter()));
            }
            
            public String getName() {
                return "Call Action";
            }
            
            public String getHelpCtx() {
                return "Add Call action - test case definition.";
            }
        });
        
        ActionRegistry.getDefault().registerNewType(TestLogAction.class,new TreeNewType() {
            public void create(TestGroup group) {
                group.addNode(new TestLogAction(getNameCounter()));
            }
            
            public String getName() {
                return "Log Action";
            }
            
            public String getHelpCtx() {
                return "Add Log Action (Action Event)";
            }
        });
        
        ActionRegistry.getDefault().registerNewType(TestSetKitAction.class,new TreeNewType() {
            public void create(TestGroup group) {
                group.addNode(new TestSetKitAction(getNameCounter()));
            }
            
            public String getName() {
                return "Set Kit Action";
            }
            
            public String getHelpCtx() {
                return "Add Editor Kit Setting Action";
            }
        });
        ActionRegistry.getDefault().registerNewType(TestSetIEAction.class,new TreeNewType() {
            public void create(TestGroup group) {
                group.addNode(new TestSetIEAction(getNameCounter()));
            }
            
            public String getName() {
                return "Set IE Action";
            }
            
            public String getHelpCtx() {
                return "Add Indentation Engine Setting Action";
            }
        });
        ActionRegistry.getDefault().registerNewType(TestSetJavaIEAction.class,new TreeNewType() {
            public void create(TestGroup group) {
                group.addNode(new TestSetJavaIEAction(getNameCounter()));
            }
            
            public String getName() {
                return "Set Java IE Action";
            }
            
            public String getHelpCtx() {
                return "Add Java Indentation Engine Setting Action";
            }
        });
        ActionRegistry.getDefault().registerNewType(TestStringAction.class,new TreeNewType() {
            public void create(TestGroup group) {
                group.addNode(new TestStringAction(getNameCounter()));
            }
            
            public String getName() {
                return "String Action";
            }
            
            public String getHelpCtx() {
                return "Add String Action - packed Lock Actions";
            }
        });
        ActionRegistry.getDefault().registerNewType(TestCompletionAction.class,new TreeNewType() {
            public void create(TestGroup group) {
                group.addNode(new TestCompletionAction(getNameCounter()));
            }
            
            public String getName() {
                return "Completion Action";
            }
            
            public String getHelpCtx() {
                return "Add Completion Action - invoked on completion panel.";
            }
        });
        ActionRegistry.getDefault().registerNewType(TestSetCompletionAction.class,new TreeNewType() {
            public void create(TestGroup group) {
                group.addNode(new TestSetCompletionAction(getNameCounter()));
            }
            
            public String getName() {
                return "Set Completion Action";
            }
            
            public String getHelpCtx() {
                return "Add Set Completion Action.";
            }
        });
        ActionRegistry.getDefault().registerNewType(TestAddAbbreviationAction.class,new TreeNewType() {
            public void create(TestGroup group) {
                group.addNode(new TestAddAbbreviationAction(getNameCounter()));
            }
            
            public String getName() {
                return "Add Abbreviation Action";
            }
            
            public String getHelpCtx() {
                return "Add New Abbreviation Action.";
            }
        });
    }
    
    public TreeNode createNodeDelegate() {
        TestGroupNodeDelegate ret=new TestGroupNodeDelegate(this);
        TestNodeDelegate nd;
        //create node delegates for all children
        for (int i=0;i < nodes.size();i++) {
            nd=(TestNodeDelegate)(((TestNode)(nodes.get(i))).getNodeDelegate());
            ret.add(nd);
        }
        return ret;
    }
}