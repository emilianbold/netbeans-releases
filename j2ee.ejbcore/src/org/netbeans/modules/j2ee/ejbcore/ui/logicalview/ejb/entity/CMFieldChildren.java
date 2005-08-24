/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.openide.filesystems.FileObject;

import org.openide.nodes.*;



/**
 * @author Chris Webster
 */
public class CMFieldChildren extends Children.Keys implements PropertyChangeListener {
    private final EntityMethodController controller;
    private final Entity model;
    private final EjbJar ejbJar;
    private final FileObject ddFile;
    
    public CMFieldChildren(EntityMethodController controller,
                           Entity model,
                           EjbJar jar,
                           FileObject ddFile) {
        this.model = model;
        this.controller = controller;
        this.ejbJar = jar;
        this.ddFile = ddFile;
    }
    
    protected void addNotify() {
        super.addNotify();
        updateKeys();
        model.addPropertyChangeListener(this);
        ejbJar.addPropertyChangeListener(this);
    }
    
    private void updateKeys() {
        List keys = getCmrFields(model.getEjbName());
        CmpField[] cmpFields = model.getCmpField();
        Arrays.sort(cmpFields, new Comparator() {
            public int compare(Object o1, Object o2) {
                String s1 = ((CmpField) o1).getFieldName();
                String s2 = ((CmpField) o2).getFieldName();
                if (s1 == null) {
                    s1 = "";
                }
                if (s2 == null) {
                    s2 = "";
                }
                return s1.compareTo(s2);
            }
        });
        keys.addAll(Arrays.asList(cmpFields));
        setKeys(keys);
    }
    
    protected void removeNotify() {
        model.removePropertyChangeListener(this);
        ejbJar.removePropertyChangeListener(this);
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
     
    protected Node[] createNodes(Object key) {
        Node[] nodes = null;
        if (key instanceof CmpField) {
            CmpField field = (CmpField) key;
            Node n = new CMPFieldNode(field, controller, ddFile);
            nodes = new Node[] { n };
        } else if (key instanceof CmrField) {
            CmrField field = (CmrField) key;
            Node n = new CMRFieldNode(field, controller, ddFile);
            nodes = new Node[] { n };
        }
        return nodes;
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        updateKeys();
    }
    
    private CmrField getCmrField(EjbRelationshipRole role, String ejbName) {
        return (role != null &&
               role.getRelationshipRoleSource() != null &&
               ejbName.equals(role.getRelationshipRoleSource().getEjbName()) &&
               role.getCmrField() != null) ? role.getCmrField():null;
    }
    
    private void getFields(String ejbName, List l) {
        Relationships r = ejbJar.getSingleRelationships();
        if (r != null) {
            EjbRelation[] relations = r.getEjbRelation();
            if (relations != null) {
                for (int i = 0; i < relations.length; i++) {
                    CmrField f = 
                            getCmrField(relations[i].getEjbRelationshipRole(), ejbName); 
                    if (f != null) {
                        l.add(f);
                    }
                    f = getCmrField(relations[i].getEjbRelationshipRole2(), ejbName);
                    if (f != null) {
                        l.add(f);
                    }
                }
            }
        }
    }
    
    private List getCmrFields(String ejbName) {
        List l = new LinkedList();
        getFields(ejbName+"", l);
        return l;
    }
    
}
