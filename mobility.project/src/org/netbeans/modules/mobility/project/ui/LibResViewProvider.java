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
 * LibResViewProvider.java
 *
 * Created on 24 April 2006, 13:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.actions.CopyAction;
import org.openide.actions.PasteAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author Lukas Waldmann
 */
class LibResViewProvider  extends J2MEPhysicalViewProvider.ChildLookup
{
    private static final String PLATFORM_ICON = "org/netbeans/modules/mobility/project/ui/resources/config.gif";    //NOI18N   
    private static final String CONFIGS_ICON = "org/netbeans/modules/mobility/project/ui/resources/configs.gif";    //NOI18N   
    
    final protected NodeCache cache;
    
    LibResViewProvider(final NodeCache c)
    {
        cache=c;
    }
    
    class ConfigChildren extends Children.Keys
    {
        final Node[] nodes=new Node[0];
        
        ConfigChildren()
        {
           super();
           setKeys(new String[] {"Resources"});
        }
        
        protected Node[] createNodes(final Object key)
        {
            final ProjectConfiguration conf=getNode().getLookup().lookup(ProjectConfiguration.class);
            final String type=(String)key;
            if ("Resources".equals(type))
            {
                return cache.getNodes(conf);
            }
            return null;
        }
        
        void refreshNode(final Object key)
        {
            String name=this.getNode().getName();
            cache.update(name);            
            refreshKey(key);
        }
    }
    /*
     * This listner checks changes of properties of particular project
     */
    
    private class ConfPropChangeListener implements PropertyChangeListener
    {
        final AntProjectHelper helper; 
        final J2MEProject project;
        final Node node;
        
        ConfPropChangeListener(Node n)
        {
            super();            
            node=n;
            project=node.getLookup().lookup(J2MEProject.class);
            helper=project.getLookup().lookup(AntProjectHelper.class);
        }

        public void propertyChange(final PropertyChangeEvent evt)
        {
            final String oldV=(String)evt.getOldValue();
            final String newV=(String)evt.getNewValue();
            final String prop=evt.getPropertyName();
            int pos;
            Node parentNode=null;
            String confName;
            final String defName=project.getConfigurationHelper().getDefaultConfiguration().getDisplayName();
            
            if ((pos=prop.indexOf(DefaultPropertiesDescriptor.LIBS_CLASSPATH))!=-1)
            {
                if (pos==0)
                {
                    confName=defName;
                }
                else
                {
                   final int begin=J2MEProjectProperties.CONFIG_PREFIX.length();
                   final int end=pos-1;
                   confName=prop.substring(begin,end);
                }
                parentNode=node.getChildren().findChild(confName);
                if (parentNode != null)
                {
                    final ConfigChildren children=(ConfigChildren)parentNode.getChildren();
                    children.refreshNode("Resources");
                }
                
                //We must refresh resources of all "default resources" configurations
                if (pos==0)
                {
                    final ProjectConfiguration confs[]=project.getConfigurationHelper().getConfigurations().toArray(new ProjectConfiguration[0]);
                    final AntProjectHelper helper=project.getLookup().lookup(AntProjectHelper.class);
                    for (int i=0;i<confs.length;i++)
                    {
                        if (!confs[i].getDisplayName().equals(defName))
                        {
                            final String libs=helper.getStandardPropertyEvaluator().getProperty(J2MEProjectProperties.CONFIG_PREFIX+confs[i].getDisplayName()+"."+DefaultPropertiesDescriptor.LIBS_CLASSPATH);
                            if (libs==null)
                            {
                                parentNode=node.getChildren().findChild(confs[i].getDisplayName());
                                if (parentNode != null)
                                {
                                    final ConfigChildren children=(ConfigChildren)parentNode.getChildren();
                                    children.refreshNode("Resources");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * This listener check the changes of active configuration
     */
            
    private class ConfChangeListener implements PropertyChangeListener
    {   
        final Node node;
        
        private ConfChangeListener(Node n)
        {
            super();
            node=n;
            final J2MEProject project=node.getLookup().lookup(J2MEProject.class);
            AntProjectHelper helper=project.getLookup().lookup(AntProjectHelper.class);
            helper.getStandardPropertyEvaluator().addPropertyChangeListener(new ConfPropChangeListener(node));
        }
        
        public void propertyChange(final PropertyChangeEvent evt)
        {
            RequestProcessor.getDefault().post(new Runnable(){
                public void run() {
                    if (evt.getNewValue() instanceof ProjectConfiguration[])
                    {
                        final List<ProjectConfiguration> nObj=Arrays.asList((ProjectConfiguration[])evt.getNewValue());
                        final List<ProjectConfiguration> oObj=Arrays.asList((ProjectConfiguration[])evt.getOldValue());

                        //Add new configurations
                        List<ProjectConfiguration> base=new ArrayList(nObj);
                        base.removeAll(oObj);
                        for (ProjectConfiguration cfg : base)
                        {
                            //new Configuration
                            final J2MEProject project=node.getLookup().lookup(J2MEProject.class);
                            final Node n=new CfgNode(
                                    new ConfigChildren(),
                                    Lookups.fixed(new Object[] {project, cfg, new AbilitiesPanel.VAData()}),
                                    cfg.getDisplayName(),PLATFORM_ICON,
                                    new Action[] {
                                                  SetConfigurationAction.getStaticInstance(),
                                                  null,
                                                  SystemAction.get(CopyAction.class),
                                                  RemoveConfigurationAction.getStaticInstance(),
                                                 });
                            node.getChildren().add(new Node[] {n});
                            n.setName(cfg.getDisplayName());
                        }

                        //Remove configurations
                        base=new ArrayList(oObj);
                        base.removeAll(nObj);
                        for (ProjectConfiguration cfg : base)
                        {
                           Node parentNode=node.getChildren().findChild(cfg.getDisplayName());
                           if (parentNode!= null)
                             node.getChildren().remove(new Node[] {parentNode}) ;
                           return;
                        }
                    }

                    //Change of active configuration
                    if (evt.getNewValue() instanceof ProjectConfiguration)
                    {   // Configuration changed
                        final ProjectConfiguration nObj=(ProjectConfiguration)evt.getNewValue();
                        final ProjectConfiguration oObj=(ProjectConfiguration)evt.getOldValue();

                        if (oObj!=null && oObj!=nObj)
                        {
                            if (oObj!=null)
                            {
                                final Node n=node.getChildren().findChild(oObj.getDisplayName());
                                if (n!=null)
                                {
                                    n.setValue("bold",Boolean.FALSE);
                                    n.setName(oObj.getDisplayName());
                                }
                            }

                            if (nObj!=null)
                            {
                                Node n=node.getChildren().findChild(nObj.getDisplayName());
                                if (n!=null)
                                {
                                    n.setValue("bold",Boolean.TRUE);
                                    n.setName(nObj.getDisplayName());
                                }
                            }                    
                        }
                    }
                }
            });
        }
    }
    
    private Node[] createActionNodes(final J2MEProject project)
    {
        final ArrayList<Node> nodeArray=new ArrayList<Node>();
        final Node nodes[]=new Node[0];

        final ProjectConfigurationsHelper confHelper=project.getConfigurationHelper();
        final ProjectConfiguration confs[]=confHelper.getConfigurations().toArray(new ProjectConfiguration[0]);        
        final ProjectConfiguration active=confHelper.getActiveConfiguration();
        
        for (int i=0;i<confs.length;i++)
        {
            final Node node=confs[i].equals(project.getConfigurationHelper().getDefaultConfiguration()) ? 
                new CfgNode(new ConfigChildren(), 
                    Lookups.fixed(new Object[] {project, confs[i], new AbilitiesPanel.VAData()}),
                    confs[i].getDisplayName(),PLATFORM_ICON,
                    new Action[] {SetConfigurationAction.getStaticInstance(),
                                 }) :
                new CfgNode(new ConfigChildren(),
                    Lookups.fixed(new Object[] {project, confs[i], new AbilitiesPanel.VAData()}),
                    confs[i].getDisplayName(),PLATFORM_ICON,
                    new Action[] {
                                  SetConfigurationAction.getStaticInstance(),
                                  null,
                                  SystemAction.get(CopyAction.class),
                                  RemoveConfigurationAction.getStaticInstance(),
                                  });
            nodeArray.add(node);
            if (confs[i].getDisplayName().equals(active.getDisplayName()))
            {
                node.setValue("bold",Boolean.TRUE);
            }
        }
        
        return nodeArray.toArray(nodes);
    }
    
    public Node[] createNodes(final J2MEProject project)
    {
        //Selection of a particular confiugration in projects view cause change of abilities navigator
        TopComponent.getRegistry().addPropertyChangeListener(new PropertyChangeListener()
        {
            private Node active=null;
            
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(evt.getPropertyName()))
                {
                    if (evt.getNewValue() instanceof Node[] )
                    {
                        final Node nodes[]=(Node[])evt.getNewValue();
                        if (nodes.length>0)
                        {
                            final Node node=((Node[])evt.getNewValue())[0];
                            if (node!=active)
                            {
                                AbilitiesPanel.ABPanel.setAbilities(node.getLookup());
                            }
                        }
                    }
                }
            }
        });
        
        final Node node=NodeFactory.createProjCfgsNode(createActionNodes(project), Lookups.singleton(project),
                NbBundle.getMessage(LibResViewProvider.class,"LBL_ProjectConfigurations"),
                CONFIGS_ICON, new Action[] {AddConfigurationAction.getStaticInstance(),null,
                                          SystemAction.get(PasteAction.class),});
        project.getConfigurationHelper().addPropertyChangeListener(new ConfChangeListener(node));        
        return new Node[] {node};
    }
 }
