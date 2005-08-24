/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjar.project.ui;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Ejbs contained within a module
 * @author Chris Webster
 */
public class EjbContainerChildren extends Children.Keys implements PropertyChangeListener {
    
    private final EjbJar model;
    private final ClassPath srcPath;
    private final FileObject ddFile;
    private final EjbNodesFactory nodeFactory;
    
    public EjbContainerChildren(EjbJar model, ClassPath srcPath, FileObject ddFile, EjbNodesFactory nodeFactory) {
        this.model = model;
        this.srcPath = srcPath;
        this.ddFile = ddFile;
        this.nodeFactory = nodeFactory;
    }
    
    protected void addNotify() {
        super.addNotify();
        updateKeys();
        model.addPropertyChangeListener(this);
    }
    
    private void updateKeys() {
        EnterpriseBeans beans = model.getEnterpriseBeans();
        List keys = Collections.EMPTY_LIST;
        if (beans != null) {
            Session[] sessionBeans = beans.getSession();
            Entity[] entityBeans = beans.getEntity();
            MessageDriven[] messageBeans = beans.getMessageDriven();
            Comparator ejbComparator = new Comparator() {
                public int compare(Object o1, Object o2) {
                    return getEjbDisplayName((Ejb) o1).compareTo(getEjbDisplayName((Ejb) o2));
                }

                private String getEjbDisplayName(Ejb ejb) {
                    String name = ejb.getDefaultDisplayName();
                    if (name == null) {
                        name = ejb.getEjbName();
                    }
                    if (name == null) {
                        name = "";
                    }
                    return name;
                }
            };
            Arrays.sort(sessionBeans, ejbComparator);
            Arrays.sort(entityBeans, ejbComparator);
            Arrays.sort(messageBeans, ejbComparator);
            keys = new ArrayList(sessionBeans.length +
                                 entityBeans.length  +
                                 messageBeans.length);
            addKeyValues(keys, Arrays.asList(sessionBeans));
            addKeyValues(keys, Arrays.asList(messageBeans));
            addKeyValues(keys, Arrays.asList(entityBeans));
        }
        setKeys(keys);
    }
    
    protected void removeNotify() {
        model.removePropertyChangeListener(this);
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
    
    protected Node[] createNodes(Object key) {
        Node[] node = null;
        if (key instanceof Session) {
            // do not create node for web service
            Session s = (Session) key;
            boolean isWebService = false;
            try {
                isWebService = s.getServiceEndpoint() != null;
            } catch (VersionNotSupportedException vnse) {
                // J2EE 1.3 web services are not directly suppored
            }
            if (!isWebService && nodeFactory != null) {
                node =  new Node[] { nodeFactory.createSessionNode(s, model, srcPath)};
            }
        }
        if (key instanceof Entity && nodeFactory != null) {
            node = new Node[] { nodeFactory.createEntityNode((Entity)key, model, srcPath, ddFile)};
        }
        if (key instanceof MessageDriven && nodeFactory != null) {
            node = new Node[] { nodeFactory.createMessageNode((MessageDriven) key, model, srcPath)};
        }
        return node == null?new Node[0]:node;
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        updateKeys();
    }
    
    private void addKeyValues(List keyContainer, List beans) {
        keyContainer.addAll(beans);
    }
}
