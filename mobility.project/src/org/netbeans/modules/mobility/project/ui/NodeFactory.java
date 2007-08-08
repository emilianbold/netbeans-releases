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

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.openide.ErrorManager;
import org.openide.util.Utilities;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.ui.customizer.CloneConfigurationPanel;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.mobility.project.ui.customizer.VisualConfigSupport;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
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
        final java.util.Map<String,Node> nodeMap;
        
        NodeKeys(final java.util.Map<String,Node> map,final Node[] ns)
        {
            nodeMap=map;
            add(ns);
        }
        
        public boolean add(Node[] ns)
        {
            for ( Node n : ns)
            {
                nodeMap.put(n.getName()+n.getDisplayName(),n);
            }
            this.setKeys(nodeMap.keySet());
            return true;
        }
        
        public boolean remove(Node[] ns)
        {
            for ( Node n : ns )
            {
                nodeMap.remove(n.getName()+n.getDisplayName());
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

    
    static public Node createProjCfgsNode(final Node nodes[], final Lookup lookup, final String name, final String icon, final Action act[])
    {
        final Children child=new NodeKeys(
                new TreeMap<String,Node>(new Comparator<String>() {
                    public int compare(String o1, String o2)
                    {
                        if (o1.equals(o2))
                            return 0;
                        if (o1.equals(ProjectConfigurationsHelper.DEFAULT_CONFIGURATION_NAME))
                            return -1;
                        if (o2.equals(ProjectConfigurationsHelper.DEFAULT_CONFIGURATION_NAME))
                            return 1;
                        return o1.compareToIgnoreCase(o2);
                    }

                    public boolean equals(Object that)
                    {
                        if (this == that) 
                            return true;
                        else
                            return false;
                    };
                }),
                nodes);
        return new ProjCfgNode(child,lookup,name,icon,act);
    }
    
    static public Node resourcesNode(final Node nodes[], final Lookup lookup,final String name, final String dName, final String icon)
    {
        final Children child=new NodeKeys(new LinkedHashMap<String,Node>(),nodes);
        return new ResourcesNode(child,lookup,name,dName, icon, null);
    }
    
    static public Node resourcesNode(final Node nodes[], final Lookup lookup,final String name, final String dName, final String icon, final Action act[])
    {
        final Children child=new NodeKeys(new LinkedHashMap<String,Node>(),nodes);
        return new ResourcesNode(child,lookup,name,dName, icon, act);
    }

static class ActionNode extends AbstractNode
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
    

    public void setActions( final Action[] act)
    {
        actions=act;
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

static class ProjCfgNode extends ActionNode implements AntProjectListener, PropertyChangeListener
{
    private boolean broken = false;
    
    /* Those two variables are used for pasting nodes. As D&D sends me nodes node by node I must collect them to one
     * collection again. First call to paste do operation on all transfered nodes and any subsequent call do nothing until
     * new transfer is initiated
     */
    final HashMap<J2MEProject,HashSet<Node>> map=new HashMap<J2MEProject,HashSet<Node>>();
    PasteType pType;
    
    protected ProjCfgNode(Children ch,Lookup lookup,String name,String icon, Action act[])
    {
        super(ch,lookup,name,null,icon,act);
        JavaPlatformManager.getDefault().addPropertyChangeListener(this);
        J2MEProject project=getLookup().lookup(J2MEProject.class);
        AntProjectHelper antHelper=project.getLookup().lookup(AntProjectHelper.class);
        antHelper.addAntProjectListener(this);
        checkBroken();
    }
    
    public void configurationXmlChanged(final AntProjectEvent ev) {
        checkBroken();
    }

    public void propertiesChanged(final AntProjectEvent ev) {
        checkBroken();
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        checkBroken();
    }
    
    public String getHtmlDisplayName() {
        J2MEPhysicalViewProvider.J2MEProjectRootNode node=(J2MEPhysicalViewProvider.J2MEProjectRootNode)getParentNode();        
        String displayName = this.getDisplayName();
        try {
            displayName = XMLUtil.toElementContent(displayName);
        } catch (CharConversionException ex) {
            // OK, no annotation in this case
            return null;
        }
                
        if (broken) 
            displayName = "<font color=\"#A40000\">" + displayName + "</font>"; //NOI18N
        
        return displayName;
    }
    
    public Image getIcon( final int type ) {
        final Image icon=super.getIcon(type);
        return broken ? Utilities.mergeImages(icon, Utilities.loadImage( "org/netbeans/modules/mobility/project/ui/resources/brokenProjectBadge.gif" ), 8, 0) : icon; //NOI18N
    }
    
    public Image getOpenedIcon( final int type ) {
        return getIcon( type );
    }
        
    private boolean hasBrokenLinks()
    {
        Node node=this.getParentNode();
        if (node != null)
        {
            
        }
        return false;
    }
    
    protected void checkBroken() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                J2MEPhysicalViewProvider.J2MEProjectRootNode node=(J2MEPhysicalViewProvider.J2MEProjectRootNode)ProjCfgNode.this.getParentNode();
                if (node != null)
                {
                    boolean br=node.isBroken();
                    boolean changed = false;
                    synchronized(this) 
                    {
                        if (broken != br) {
                            broken ^= true; //faster way of negation
                            changed=true;
                        }
                    }
                    if (changed) {
                        fireIconChange();
                        fireOpenedIconChange();
                        fireDisplayNameChange(null, null);
                    }
                }
            }
        });
    }
    
    private PasteType getPasteType (final Transferable tr, DataFlavor[] flavors ) 
    {
        final String PRIMARY_TYPE = "application";   //NOI18N     
        final String DND_TYPE = "x-java-openide-nodednd"; //NOI18N
        final String MULTI_TYPE = "x-java-openide-multinode"; //NOI18N
        
        class CfgPasteType extends PasteType
        {
            public Transferable paste() throws IOException
            {
                final J2MEProject projectDrop=ProjCfgNode.this.getLookup().lookup(J2MEProject.class);
                if (projectDrop==null) 
                    return null;
                final J2MEProjectProperties dropProperties = new J2MEProjectProperties( projectDrop, 
                                                    projectDrop.getLookup().lookup(AntProjectHelper.class),
                                                    projectDrop.getLookup().lookup(ReferenceHelper.class), 
                                                    projectDrop.getConfigurationHelper() );
                final ArrayList<ProjectConfiguration> allNames=new ArrayList<ProjectConfiguration>(Arrays.asList(dropProperties.getConfigurations()));                
                final int size=allNames.size();
                ProjectConfiguration oldCfg=null;
                ProjectConfiguration newCfg=null;
                
                for (J2MEProject project : map.keySet())
                {
                    HashSet<Node> set=map.get(project);
                    final ArrayList<String> allStrNames=new ArrayList<String>(allNames.size()+set.size());
                    final J2MEProjectProperties j2meProperties = new J2MEProjectProperties( project, 
                            project.getLookup().lookup(AntProjectHelper.class),
                            project.getLookup().lookup(ReferenceHelper.class), 
                            project.getConfigurationHelper() );
                    
                    for (ProjectConfiguration name : allNames)
                        allStrNames.add(name.getDisplayName());

                    for (Node node : set)
                    {
                        newCfg=oldCfg=node.getLookup().lookup(ProjectConfiguration.class);
                        //Check if configuration with the same name already exist
                        ProjectConfiguration exst=projectDrop.getConfigurationHelper().getConfigurationByName(oldCfg.getDisplayName());
                        if (exst != null)
                        {
                            final CloneConfigurationPanel ccp = new CloneConfigurationPanel(allStrNames);
                            final DialogDescriptor dd = new DialogDescriptor(ccp, NbBundle.getMessage(VisualConfigSupport.class, "LBL_VCS_DuplConfiguration", oldCfg.getDisplayName()), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null); //NOI18N
                            ccp.setDialogDescriptor(dd);
                            final String newName = NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd)) ? ccp.getName() : null;
                            if (newName != null) {
                                newCfg = new ProjectConfiguration() {
                                    public String getDisplayName() {
                                        return newName;
                                    }
                                };
                                allStrNames.add(newName);
                            }
                            else
                                continue;
                        }
                        final String keys[] = j2meProperties.keySet().toArray(new String[j2meProperties.size()]);
                        final String prefix = J2MEProjectProperties.CONFIG_PREFIX + oldCfg.getDisplayName();
                        for (int i=0; i<keys.length; i++) {
                            if (keys[i].startsWith(prefix))
                                dropProperties.put(J2MEProjectProperties.CONFIG_PREFIX + newCfg.getDisplayName() + keys[i].substring(prefix.length()), j2meProperties.get(keys[i]));
                        }

                        
                        allNames.add(newCfg);
                    }
                }
                map.clear();
                synchronized (CfgPasteType.this)
                {
                    pType=null;
                }
                //No configuration was added
                if (allNames.size() == size)
                    return null;
                
                dropProperties.setConfigurations(allNames.toArray(new ProjectConfiguration[allNames.size()]));
                // Store the properties
                final ProjectConfiguration lcfg=newCfg;
                    
                SwingUtilities.invokeLater( new Runnable() 
                {
                    public void run() {  
                        assert lcfg != null;
                        try {
                            Children.MUTEX.writeAccess( new Runnable() {
                                public void run()
                                {
                                    dropProperties.store();                                                                
                                }
                            });
                            
                            projectDrop.getConfigurationHelper().setActiveConfiguration(lcfg);
                        } catch (IllegalArgumentException ex) {
                            ErrorManager.getDefault().notify(ex);
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ex);
                        } 
                    }
                });
                return null;
            }
        }
        
        synchronized (this)
        {
            if (pType == null)
                pType=new CfgPasteType();
        }
        
        for (DataFlavor flavor : flavors) {
            if (PRIMARY_TYPE.equals(flavor.getPrimaryType ()))
            {
                if (MULTI_TYPE.equals(flavor.getSubType ())) {
                    Node nodes[]=NodeTransfer.nodes(tr,NodeTransfer.DND_COPY_OR_MOVE);
                    if (nodes == null) return null;
                    for (Node node : nodes)
                    {  
                        if (node instanceof CfgNode)
                        {
                            J2MEProject project=node.getLookup().lookup(J2MEProject.class);
                            if (project != null)
                            {
                                HashSet<Node> set=map.get(project);
                                if (set == null)
                                {
                                    set = new HashSet<Node>();
                                    map.put(project,set);
                                }
                                set.add(node);
                            }
                        }
                    }
                    if (map.size() != 0)
                        return pType;
                }
                if (DND_TYPE.equals(flavor.getSubType ())) {
                    Node node=NodeTransfer.node(tr,NodeTransfer.DND_COPY_OR_MOVE);
                    if (node instanceof CfgNode)
                    {
                        J2MEProject project=node.getLookup().lookup(J2MEProject.class);
                        if (project != null)
                        {
                            HashSet<Node> set=map.get(project);
                            if (set == null)
                            {
                                set = new HashSet<Node>();
                                map.put(project,set);
                            }
                            set.add(node);
                        }
                    }
                    if (map.size() != 0)
                        return pType;
                }
            }
        }
        return null;
    }
    
    public PasteType getDropType(Transferable tr, int action, int index)
    {
        DataFlavor fr[]=tr.getTransferDataFlavors();
        PasteType type=getPasteType(tr,fr);
        return type;
    }
    
    protected void createPasteTypes(Transferable t, List<PasteType> s) 
    {
        PasteType pt=getDropType(t,0,0);
        if (pt != null) s.add(pt);
    }
}

static class ResourcesNode extends ActionNode
{
    private static final Image ICON_BADGE = Utilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/libraries-badge.png");    //NOI18N

    protected ResourcesNode(Children ch,Lookup lookup,String name,String dName,String icon, Action act[])
    {
        super(ch,lookup,name,dName,icon,act);
    }
    
    private PasteType getPasteType (final Transferable tr, DataFlavor[] flavors ) 
    {
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
                    NodeActions.NodeAction.pasteAction(set,ResourcesNode.this);
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
                                continue;
                            }
                        }
                        return set.size()==0?null:new NDPasteType();
                            
                    } catch (Exception ex)
                    {
                        return null;
                    }
                    
                }
                
                 if (MULTI_TYPE.equals(flavor.getSubType ())) {
                    Node nodes[]=NodeTransfer.nodes(tr,NodeTransfer.DND_COPY_OR_MOVE);
                    if (nodes == null) return null;
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
                            continue;
                        }
                    }
                    return  set.size()==0?null:new NDPasteType();
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
                        continue;
                    }
                    return  set.size()==0?null:new NDPasteType();
                }
            }
        }
        return null;
    }
    
    public Image getIcon( int type ) {        
        Image image = super.getIcon(type);
        image = Utilities.mergeImages(image, ICON_BADGE, 7, 7 );
        return image;        
    }
    
    public Image getOpenedIcon( int type ) {        
        Image image = super.getOpenedIcon(type);
        image = Utilities.mergeImages(image, ICON_BADGE, 7, 7 );
        return image;        
    }

    
    public PasteType getDropType(Transferable tr, int action, int index)
    {
        final Boolean gray=(Boolean)this.getValue("gray");
        if (gray == Boolean.FALSE)
        {
            DataFlavor fr[]=tr.getTransferDataFlavors();
            PasteType type=getPasteType(tr,fr);
            return type;
        }
        return null;
    }
    
    protected void createPasteTypes(Transferable t, List<PasteType> s) 
    {
        PasteType pt=getDropType(t,0,0);
        if (pt != null) s.add(pt);
    }    
}

static class CfgNode extends ActionNode implements AntProjectListener, PropertyChangeListener
{
    protected boolean broken = false;
    final AntProjectHelper antHelper;
    final ReferenceHelper  refHelper;
    final ProjectConfiguration cfg;
    final J2MEProject project;
    
    protected CfgNode(Children ch,Lookup lookup,String name,String icon, Action act[])
    {
        super(ch,lookup,name,null,icon,act);
        project=getLookup().lookup(J2MEProject.class);
        cfg=getLookup().lookup(ProjectConfiguration.class);
        refHelper=project.getLookup().lookup(ReferenceHelper.class);
        antHelper=project.getLookup().lookup(AntProjectHelper.class);
        
        JavaPlatformManager.getDefault().addPropertyChangeListener(this);
        antHelper.addAntProjectListener(this);
        this.broken = hasBrokenLinks();
        
    }
        
    private String usedLibs(final ProjectConfiguration cfg)
    {
        String libs;
        final AntProjectHelper helper=project.getLookup().lookup(AntProjectHelper.class);        
        
        /* Check for default lib config */
        if (cfg.equals(project.getConfigurationHelper().getDefaultConfiguration()))
        {
            libs=DefaultPropertiesDescriptor.LIBS_CLASSPATH;
        }
        else
        {
            libs=helper.getStandardPropertyEvaluator().getProperty(J2MEProjectProperties.CONFIG_PREFIX+cfg.getDisplayName()+"."+DefaultPropertiesDescriptor.LIBS_CLASSPATH);
            if (libs==null)
                libs=DefaultPropertiesDescriptor.LIBS_CLASSPATH;
            else
                libs=J2MEProjectProperties.CONFIG_PREFIX+cfg.getDisplayName()+"."+DefaultPropertiesDescriptor.LIBS_CLASSPATH;
        }
        return libs;
    }
    
    private String[] getBreakableProperties(J2MEProject project,ProjectConfiguration cfg) {
        String s[] = new String[3];
        s[0] = DefaultPropertiesDescriptor.SRC_DIR;
        s[1] = usedLibs(cfg);
        if (project.getConfigurationHelper().getDefaultConfiguration().equals(cfg)) {
            s[2] = DefaultPropertiesDescriptor.SIGN_KEYSTORE;
        } else {
            s[2] = J2MEProjectProperties.CONFIG_PREFIX + cfg.getDisplayName() + "." + DefaultPropertiesDescriptor.SIGN_KEYSTORE; //NOI18N
        }
        return s;
    }
    
    
    private String usedActive(final ProjectConfiguration cfg)
    {
        String libs;
        final AntProjectHelper helper=project.getLookup().lookup(AntProjectHelper.class);        
        
        /* Check for default lib config */
        if (cfg.equals(project.getConfigurationHelper().getDefaultConfiguration()))
        {
            libs=DefaultPropertiesDescriptor.PLATFORM_ACTIVE;
        }
        else
        {
            libs=helper.getStandardPropertyEvaluator().getProperty(J2MEProjectProperties.CONFIG_PREFIX + cfg.getDisplayName() + "." + DefaultPropertiesDescriptor.PLATFORM_ACTIVE);
            if (libs==null)
                libs=DefaultPropertiesDescriptor.PLATFORM_ACTIVE;
            else
                libs=J2MEProjectProperties.CONFIG_PREFIX + cfg.getDisplayName() + "." + DefaultPropertiesDescriptor.PLATFORM_ACTIVE;
        }
        return libs;
    }
    
    private String[] getBreakablePlatformProperties(J2MEProject project,ProjectConfiguration cfg) {
        String s[]=new String[1];
        s[0]=usedActive(cfg);
        return s;
    }
    
    public boolean hasBrokenLinks() {        
        if (project != null && refHelper != null && antHelper != null)
            return BrokenReferencesSupport.isBroken( antHelper, refHelper, getBreakableProperties(project,cfg), getBreakablePlatformProperties(project,cfg));
        return false;
    }
    
    public Image getIcon( final int type ) {
        final Image icon=super.getIcon(type);
        return broken ? Utilities.mergeImages(icon, Utilities.loadImage( "org/netbeans/modules/mobility/project/ui/resources/brokenProjectBadge.gif" ), 8, 0) : icon; //NOI18N
    }
    
    public Image getOpenedIcon( final int type ) {
        return getIcon( type );
    }
    
    protected void checkBroken() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                boolean br=hasBrokenLinks();
                boolean changed = false;
                synchronized(this) 
                {
                    if (broken != br) {
                        broken ^= true; //faster way of negation
                        changed=true;
                    }
                }
                if (changed) {
                    fireIconChange();
                    fireOpenedIconChange();
                    fireDisplayNameChange(null, null);                    
                }
            }
        });
    }
    
    public String getHtmlDisplayName() {
        String displayName = this.getDisplayName();
        try {
            displayName = XMLUtil.toElementContent(displayName);
        } catch (CharConversionException ex) {
            // OK, no annotation in this case
            return null;
        }
        
        if (broken) 
            displayName = "<font color=\"#A40000\">" + displayName + "</font>"; //NOI18N
        
        final Boolean error=(Boolean)this.getValue("error");
        if (error==Boolean.TRUE)
            return "<font color=\"#A40000\">"+displayName+"</font>";
        
        final Boolean gray=(Boolean)this.getValue("gray");
        if (gray==Boolean.TRUE)
            return "<font color=\"#A0A0A0\">"+displayName+"</font>";

        
        final Boolean bold=(Boolean)this.getValue("bold");
        if (bold==Boolean.TRUE)
            return "<B>" + displayName + "</B>"; //NOI18N
        
        return displayName ; //NOI18N
    }
    
    public void configurationXmlChanged(final AntProjectEvent ev) {
        checkBroken();
    }

    public void propertiesChanged(final AntProjectEvent ev) {
        checkBroken();
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        checkBroken();
    }
}
}
