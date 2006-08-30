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

/*
 * NodeFactory.java
 *
 * Created on 27 April 2006, 16:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.project.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.PasteType;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Lukas Waldmann
 */
final class NodeFactory
{
    private static class NodeKeys extends Children.Keys
    {
        final HashMap<String,Node> nodeMap=new HashMap<String,Node>();
        
        NodeKeys(final Node[] ns)
        {
            add(ns);
        }
        
        public boolean add(Node[] ns)
        {
            for ( Node n : ns)
            {
                nodeMap.put(n.getName(),n);
            }
            this.setKeys(nodeMap.keySet());
            return true;
        }
        
        public boolean remove(Node[] ns)
        {
            for ( Node n : ns )
            {
                nodeMap.remove(n.getName());
            }
            this.setKeys(nodeMap.keySet());
            return true;
        }

        
        protected void removeNotify() {
            this.setKeys(Collections.EMPTY_SET);
        }
        
        protected Node[] createNodes(Object key)
        { 
            return new Node[] {nodeMap.get(key)};
        }
    }
    
    static public Node createNode(final Node nodes[], final Lookup lookup, final String name, final String icon)
    {
        final Children child=new NodeKeys(nodes);
        
        return new ActionNode(child,lookup,name,icon);
    }
    
    static public Node createNode(final Node nodes[], final Lookup lookup, final String name, final String icon, final Action act[])
    {
        final Children child=new NodeKeys(nodes);
        return new ActionNode(child,lookup,name,icon,act);
    }
    
    static public Node createNode(final Node nodes[], final Lookup lookup,final String name, final String dName, final String icon)
    {
        final Children child=new NodeKeys(nodes);
        return new ActionNode(child,lookup,name,dName, icon);
    }
    
    static public Node createNode(final Node nodes[], final Lookup lookup,final String name, final String dName, final String icon, final Action act[])
    {
        final Children child=new NodeKeys(nodes);
        return new ActionNode(child,lookup,name,dName, icon, act);
    }
}

class ActionNode extends AbstractNode
{
    Action[] actions;    
    
    
    public ActionNode(Children ch,final Lookup lookup,String name,String dName,String icon, Action act[])
    {
        super(ch,lookup);
        setName(name);
        if (dName != null) setDisplayName(dName);
        if (icon  != null) setIconBaseWithExtension(icon);
        actions=act;
    }
    
    public ActionNode(Children ch,final Lookup lookup, String name,String icon, Action act[])
    {
        this(ch,lookup,name,null,icon,act);
    }
    
    public ActionNode(Children ch,final Lookup lookup,String name,String icon)
    {
        this(ch,lookup,name,null,icon,null);        
    }
    
    public ActionNode(Children ch,final Lookup lookup,String name,String dName,String icon)
    {
        this(ch,lookup,name,dName,icon,null);        
    }

    public void setActions( final Action[] act)
    {
        actions=act;
    }
    
    private PasteType getPasteType (final Transferable tr,int action, DataFlavor[] flavors ) {
        final String PRIMARY_TYPE = "application";   //NOI18N
        final String LIST_TYPE = "x-java-file-list"; //NOI18N
        final String DND_TYPE = "x-java-openide-nodednd"; //NOI18N
        final String MULTI_TYPE = "x-java-openide-multinode"; //NOI18N
        final HashSet<VisualClassPathItem> set=new HashSet<VisualClassPathItem>();
            
        class NDPasteType extends PasteType
        {
            public Transferable paste() throws IOException
            {
                if (set.size() != 0)                        
                {
                    NodeAction.pasteAction(set,ActionNode.this);
                    set.clear();
                }
                    
                return tr;
            }
        }
        
        for (DataFlavor flavor : flavors) {
            if (PRIMARY_TYPE.equals(flavor.getPrimaryType ()))
            {                
                if (LIST_TYPE.equals(flavor.getSubType ())) {
                    List<File> files;
                    try
                    {
                        files = (List<File>) tr.getTransferData(flavor);
                        for (File file : files)
                        {
                            final String s = file.getName().toLowerCase();
                            if (file.isDirectory())
                            {
                                file = FileUtil.normalizeFile(file);
                                set.add(new VisualClassPathItem( file,
                                    VisualClassPathItem.TYPE_FOLDER,
                                    null,
                                    file.getPath()));
                            }
                            else if (s.endsWith(".zip") || s.endsWith(".jar"))
                            {
                                file = FileUtil.normalizeFile(file);
                                set.add(new VisualClassPathItem( file,
                                    VisualClassPathItem.TYPE_JAR,
                                    null,
                                    file.getPath()));
                            }
                            else
                            {
                                set.clear();
                                return null;
                            }
                        }
                        return new NDPasteType();
                            
                    } catch (Exception ex)
                    {
                        return null;
                    }
                    
                }
                
                 if (MULTI_TYPE.equals(flavor.getSubType ())) {
                    Node nodes[]=NodeTransfer.nodes(tr,NodeTransfer.DND_COPY_OR_MOVE);
                    for (Node node : nodes)
                    {
                        if (node != null && node.getValue("resource") != null )
                        {
                            VisualClassPathItem item=(VisualClassPathItem)node.getValue("VCPI");
                            if (item != null)
                                set.add(item);
                        }
                        //Node is not of correct type
                        else 
                        {
                            set.clear();
                            return null;
                        }
                    }
                    return  new NDPasteType();
                }
                
                if (DND_TYPE.equals(flavor.getSubType ())) {
                    Node node=NodeTransfer.node(tr,NodeTransfer.DND_COPY_OR_MOVE);
                    if (node != null && node.getValue("resource") != null )
                    {
                        VisualClassPathItem item=(VisualClassPathItem)node.getValue("VCPI");
                        if (item != null)
                            set.add(item);
                    }
                    //Node is not of correct type
                    else 
                    {
                        set.clear();
                        return null;
                    }
                    return  new NDPasteType();
                }
            }
        }
        return null;
    }
    
    public PasteType getDropType(Transferable tr, int action, int index)
    {
        final Boolean gray=(Boolean)this.getValue("gray");
        if (gray == Boolean.FALSE)
        {
            Object o=null;
            DataFlavor fr[]=tr.getTransferDataFlavors();
            PasteType type=getPasteType(tr,action,fr);
            return type;
        }
        return null;
    }
    
    protected void createPasteTypes(Transferable t, List<PasteType> s) 
    {
        PasteType pt=getDropType(t,0,0);
        if (pt != null) s.add(pt);
    }
    
    
    public Action[] getActions(final boolean context)
    {
        return actions==null?super.getActions(context):actions.clone();
    }
    

    final public void setName(final String name)
    {
        if (name==this.getName())
            fireDisplayNameChange(null, null);
        else
            super.setName(name);
    }

    public String getHtmlDisplayName () {
        String displayName = this.getDisplayName();
        try {
            displayName = XMLUtil.toElementContent(displayName);
        } catch (CharConversionException ex) {
            // OK, no annotation in this case
            return null;
        }
        final Boolean bold=(Boolean)this.getValue("bold");
        if (bold==Boolean.TRUE)
            return "<B>" + displayName + "</B>"; //NOI18N
        
        final Boolean error=(Boolean)this.getValue("error");
        if (error==Boolean.TRUE)
            return "<font color=\"#A40000\">"+displayName+"</font>";
        
        final Boolean gray=(Boolean)this.getValue("gray");
        if (gray==Boolean.TRUE)
            return "<font color=\"#A0A0A0\">"+displayName+"</font>";
            
        return displayName ; //NOI18N
            
    }
}