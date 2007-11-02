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
 * NodeCache.java
 *
 * Created on 21 July 2006, 11:04
 *
 */

package org.netbeans.modules.mobility.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.Action;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.actions.PasteAction;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Lukas Waldmann
 */
class NodeCache implements PropertyChangeListener
{
    private static final String PLATFORM_ICON = "org/netbeans/modules/mobility/cldcplatform/resources/platform.gif";    //NOI18N   
    final private HashMap<String,Collection<Node>> nodes=new HashMap<String,Collection<Node>>();
    final private HashMap<String,Collection<Node>> clones=new HashMap<String,Collection<Node>>();
    final private J2MEProject project;

    final static private Action[] emptyAction = new Action[] {};
    final private Action[] pActions=new Action[] {
                NodeActions.AddProjectAction.getStaticInstance(),
                NodeActions.AddJarAction.getStaticInstance(),
                NodeActions.AddFolderAction.getStaticInstance(),
                NodeActions.AddLibraryAction.getStaticInstance(),
                null,
                SystemAction.get(PasteAction.class),
            };
    
    /** Creates a new instance of NodeCache */
    NodeCache(final J2MEProject prj)
    {
        project=prj;
        project.getConfigurationHelper().addPropertyChangeListener(this);
    }
    
    public Node[] getNodes(ProjectConfiguration cfg)
    {
        Collection<Node> res=nodes.get(cfg.getDisplayName());
        if (res != null)
            return res.toArray(new Node[res.size()]);
        else
        {
            ProjectConfiguration newCfg = project.getConfigurationHelper().getConfigurationByName(cfg.getDisplayName());
            if (newCfg != null)
            {
                addNode(newCfg);
                return getNodes(newCfg);
            }
        }
        return null;
    }
    
    public Node[] getClones(ProjectConfiguration cfg)
    {
        Collection<Node> res=clones.get(cfg.getDisplayName());
        if (res != null)
            return res.toArray(new Node[res.size()]);
        else
        {
            ProjectConfiguration newCfg = project.getConfigurationHelper().getConfigurationByName(cfg.getDisplayName());
            if (newCfg != null)
            {
                addNode(newCfg);
                return getClones(newCfg);
            }
        }
        return null;
    }
    
    private String usedLibs(final ProjectConfiguration cfg)
    {
        String libs;
        final AntProjectHelper helper=project.getLookup().lookup(AntProjectHelper.class);        
        
        /* Check for default lib config */
        if (cfg.getDisplayName().equals(project.getConfigurationHelper().getDefaultConfiguration().getDisplayName()))
        {
            libs=helper.getStandardPropertyEvaluator().getProperty(DefaultPropertiesDescriptor.LIBS_CLASSPATH);
        }
        else
        {
            libs=helper.getStandardPropertyEvaluator().getProperty(J2MEProjectProperties.CONFIG_PREFIX+cfg.getDisplayName()+"."+DefaultPropertiesDescriptor.LIBS_CLASSPATH);
        }
        return libs;
    }
    
    private void addNode(final ProjectConfiguration cfg)
    {
        String libs=usedLibs(cfg);
        final Collection<Node> resNodes=ConfigurationsProvider.createResourcesNodes(project,cfg);
        final Collection<Node> allNodes=new ArrayList<Node>();
        final boolean gray = libs == null;
        
        Node resNode=null;
        Node subNodes[]=new Node[resNodes.size()];
        Lookup lookup=Lookups.fixed( new Object[] {project,cfg});
        if (gray)
        {
            resNode=NodeFactory.resourcesNode(resNodes.toArray(subNodes), lookup,
                    "Resources",NbBundle.getMessage(NodeCache.class,"LBL_NodeCache_InheritedResources",project.getConfigurationHelper().getDefaultConfiguration().getDisplayName()),PLATFORM_ICON,emptyAction);
        }
        else
        {
            resNode=NodeFactory.resourcesNode(resNodes.toArray(subNodes), lookup, "Resources",NbBundle.getMessage(NodeCache.class,"LBL_NodeCache_Resources"),PLATFORM_ICON,pActions);
        }
        resNode.setValue("gray",gray);
        allNodes.add(resNode);        
        nodes.put(cfg.getDisplayName(),allNodes);
        
        /* we must do clones of node to add it to another branch of the logical view */
        final ArrayList<Node> list=new ArrayList<Node>(allNodes.size());
        for (Node node : allNodes)
            list.add(node.cloneNode());
        clones.put(cfg.getDisplayName(),list);
    }
    
    public void update(final String name)
    {
        final ProjectConfiguration cfg=new ProjectConfiguration()
        {
            public String getDisplayName()
            {
                return name;
            }
        };
        
        Collection<Node> allNodes;
        
        allNodes=nodes.get(name);
        if (allNodes == null)
            addNode(cfg);
        else
        {
            /* recreate modified resource nodes*/
            final Collection<Node> resNodes=ConfigurationsProvider.createResourcesNodes(project,cfg);
            final boolean gray = usedLibs(cfg) == null ? true : false;
            /* Nodes for all configurations branch */
            for (Node resNode : allNodes)
            {
                if (resNode.getName().equals("Resources"))
                {
                    Node nodes[]=resNodes.toArray(new Node[resNodes.size()]);
                    resNode.getChildren().remove(resNode.getChildren().getNodes());
                    resNode.getChildren().add(nodes);
                    if (gray)
                    {
                        resNode.setDisplayName(NbBundle.getMessage(NodeCache.class, "LBL_NodeCache_InheritedResources", project.getConfigurationHelper().getDefaultConfiguration().getDisplayName())); //NOI18N
                        ((NodeFactory.ActionNode)resNode).setActions(emptyAction);
                    }
                    else
                    {
                        resNode.setDisplayName(NbBundle.getMessage(NodeCache.class, "LBL_NodeCache_Resources")); //NOI18N
                        ((NodeFactory.ActionNode)resNode).setActions(pActions);
                    }
                    resNode.setValue("gray",gray);
                }
            }
            
            /* Nodes for cloned current configuration branch */
            allNodes=clones.get(name);
            for (Node resNode : allNodes)
            {
                if (resNode.getName().equals("Resources"))
                {
                  Node nodes[]=resNodes.toArray(new Node[resNodes.size()]);
                  Node clNodes[]=new Node[resNodes.size()];
                  for (int i = 0; i < nodes.length; i++)
                  {
                      clNodes[i]=nodes[i].cloneNode();
                  } 
                  resNode.getChildren().remove(resNode.getChildren().getNodes());
                  resNode.getChildren().add(clNodes);
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals("configurations"))
        {
            List<ProjectConfiguration> oldV=Arrays.asList((ProjectConfiguration[])evt.getOldValue());
            List<ProjectConfiguration> newV=Arrays.asList((ProjectConfiguration[])evt.getNewValue());
            HashSet<ProjectConfiguration> set=new HashSet<ProjectConfiguration>(oldV);
            set.removeAll(newV);
            for (ProjectConfiguration cfg:set)
            {
                nodes.remove(cfg.getDisplayName());
                clones.remove(cfg.getDisplayName());
            }
        }    
    }

}
