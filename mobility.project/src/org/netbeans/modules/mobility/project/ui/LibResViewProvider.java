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
import java.util.Map;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.openide.ErrorManager;
import org.openide.actions.CopyAction;
import org.openide.actions.PasteAction;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
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
    
    private class ConfPropChangeListener implements ChangeListener
    {
        final AntProjectHelper helper; 
        final J2MEProject project;
        final Node node;
        final PropertyProvider pp;
        private String oldValue, oldPropName;
        
        ConfPropChangeListener(Node n, PropertyProvider pp)
        {
            super();            
            node=n;
            project=node.getLookup().lookup(J2MEProject.class);
            helper=project.getLookup().lookup(AntProjectHelper.class);
            this.pp=pp;
        }

        public void propertyChange(final PropertyChangeEvent evt) {
            RequestProcessor.getDefault().post(new Runnable(){
                public void run() {
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
            });
        }

        public void stateChanged(ChangeEvent e)
        {
            Map<String,String> props=pp.getProperties();
            String newPropName;
            ProjectConfiguration conf=project.getConfigurationHelper().getActiveConfiguration();
            if (conf.getDisplayName().equals(project.getConfigurationHelper().getDefaultConfiguration().getDisplayName()))
            {
                newPropName=DefaultPropertiesDescriptor.LIBS_CLASSPATH;
            }
            else
            {
                newPropName=J2MEProjectProperties.CONFIG_PREFIX+conf.getDisplayName()+"."+DefaultPropertiesDescriptor.LIBS_CLASSPATH;
            }    
            String newValue=props.get(newPropName);
            if (newPropName ==null)
                return;
            if (newValue == null || !newPropName.equals(oldPropName) || !newValue.equals(oldValue))
            {
                PropertyChangeEvent ev=new PropertyChangeEvent(this,newPropName, oldValue,newValue);
                propertyChange(ev);
                oldValue=newValue;
                oldPropName=newPropName;
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
            PropertyProvider pp=helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            pp.addChangeListener(new ConfPropChangeListener(node,pp));
            //helper.getStandardPropertyEvaluator().addPropertyChangeListener();
        }
        
        public void propertyChange(final PropertyChangeEvent evt)
        {
            RequestProcessor.getDefault().post(new Runnable(){
                public void run() {
                    if (evt.getNewValue() instanceof ProjectConfiguration[]){
                        final List<ProjectConfiguration> nObj=Arrays.asList((ProjectConfiguration[])evt.getNewValue());
                        final List<ProjectConfiguration> oObj=Arrays.asList((ProjectConfiguration[])evt.getOldValue());
                        //Add new configurations
                        List<ProjectConfiguration> base=new ArrayList(nObj);
                        base.removeAll(oObj);
                        Node n=null;
                        ProjectConfiguration acfg=node.getLookup().lookup(J2MEProject.class).getConfigurationHelper().getActiveConfiguration();
                                
                        for (ProjectConfiguration cfg : base)
                        {
                            //new Configuration
                            final J2MEProject project=node.getLookup().lookup(J2MEProject.class);
                            n=new NodeFactory.CfgNode(
                                    new ConfigChildren(),
                                    Lookups.fixed(new Object[] {project, cfg, AbilitiesPanel.hintInstance}),
                                    cfg.getDisplayName(),PLATFORM_ICON,
                                    new Action[] {
                                                  SystemAction.get(NodeActions.RunConfigurationAction.class),
                                                  SystemAction.get(NodeActions.DebugConfigurationAction.class),
                                                  SystemAction.get(NodeActions.BuildConfigurationAction.class),
                                                  SystemAction.get(NodeActions.CleanAndBuildConfigurationAction.class),
                                                  SystemAction.get(NodeActions.CleanConfigurationAction.class),
                                                  SystemAction.get(NodeActions.DeployConfigurationAction.class),
                                                  SystemAction.get(NodeActions.SetConfigurationAction.class),
                                                  null,
                                                  SystemAction.get(CopyAction.class),
                                                  SystemAction.get(NodeActions.RemoveConfigurationAction.class),
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

                        if (oObj!=nObj)
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
                                else
                                {
                                    //We must wait until the configuration nodes are updated    
                                    RequestProcessor.getDefault().post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            while (true)
                                            {
                                                ProjectConfiguration cfg=node.getLookup().lookup(J2MEProject.class).getConfigurationHelper().getActiveConfiguration();
                                                Node n=node.getChildren().findChild(cfg.getDisplayName());
                                                if (n != null)
                                                {
                                                    n.setValue("bold",Boolean.TRUE);
                                                    n.setName(n.getDisplayName());
                                                    break;
                                                }
                                                else
                                                {
                                                    try {
                                                        Thread.sleep(100);
                                                        continue;
                                                    } catch (InterruptedException ex) {}
                                                }
                                            }
                                        }
                                    },100);
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
                new NodeFactory.CfgNode(new ConfigChildren(), 
                    Lookups.fixed(new Object[] {project, confs[i], AbilitiesPanel.hintInstance}),
                    confs[i].getDisplayName(),PLATFORM_ICON,
                    new Action[] {
                                  SystemAction.get(NodeActions.RunConfigurationAction.class),
                                  SystemAction.get(NodeActions.DebugConfigurationAction.class),
                                  SystemAction.get(NodeActions.BuildConfigurationAction.class),
                                  SystemAction.get(NodeActions.CleanAndBuildConfigurationAction.class),
                                  SystemAction.get(NodeActions.CleanConfigurationAction.class),
                                  SystemAction.get(NodeActions.DeployConfigurationAction.class),
                                  SystemAction.get(NodeActions.SetConfigurationAction.class),
                                 }) :
                new NodeFactory.CfgNode(new ConfigChildren(),
                    Lookups.fixed(new Object[] {project, confs[i], AbilitiesPanel.hintInstance}),
                    confs[i].getDisplayName(),PLATFORM_ICON,
                    new Action[] {
                                  SystemAction.get(NodeActions.RunConfigurationAction.class),
                                  SystemAction.get(NodeActions.DebugConfigurationAction.class),
                                  SystemAction.get(NodeActions.BuildConfigurationAction.class),
                                  SystemAction.get(NodeActions.CleanAndBuildConfigurationAction.class),
                                  SystemAction.get(NodeActions.CleanConfigurationAction.class),
                                  SystemAction.get(NodeActions.DeployConfigurationAction.class),
                                  SystemAction.get(NodeActions.SetConfigurationAction.class),
                                  null,
                                  SystemAction.get(CopyAction.class),
                                  SystemAction.get(NodeActions.RemoveConfigurationAction.class),
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
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(evt.getPropertyName()) && evt.getNewValue() instanceof Node[] &&
                    evt.getSource() instanceof TopComponent.Registry && 
                    ((TopComponent.Registry)evt.getSource()).getActivated() instanceof ExplorerManager.Provider)
                {
                    final Node nodes[]=(Node[])evt.getNewValue();
                    if (nodes.length>0)
                    {
                        final Node node=((Node[])evt.getNewValue())[0];
                        AbilitiesPanel.ABPanel.setAbilities((Node[])evt.getNewValue());
                    }
                }
            }
        });
        
        final Node node=NodeFactory.createProjCfgsNode(createActionNodes(project), Lookups.singleton(project),
                NbBundle.getMessage(LibResViewProvider.class,"LBL_ProjectConfigurations"),
                CONFIGS_ICON, new Action[] {NodeActions.AddConfigurationAction.getStaticInstance(),null,
                                          SystemAction.get(PasteAction.class),});
        project.getConfigurationHelper().addPropertyChangeListener(new ConfChangeListener(node));        
        return new Node[] {node};
    }
 }
