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

/**
 * Superclass that implements DescriptionInterface for Servlet2.4 beans.
 *
 * @author  Milan Kuchtiak
 */

package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.api.web.dd.common.VersionNotSupportedException;

public abstract class GetAllEjbs extends EnclosingBean {
    
    public GetAllEjbs(java.util.Vector comps, Version version) {
        super(comps, version);
    }
    
    public abstract Entity[] getEntity();
    public abstract MessageDriven[] getMessageDriven();
    public abstract Session[] getSession();
    
    public abstract int sizeSession();
    public abstract int sizeEntity();
    public abstract int sizeMessageDriven();
    public abstract int removeSession(Session s);
    public abstract int removeEntity(Entity e);
    public abstract int removeMessageDriven(MessageDriven m);
    
    public void removeEjb(Ejb value){
        
        if(value instanceof Entity){
            removeEntity((Entity) value);
        }
        else  if(value instanceof Session){
            removeSession((Session) value);
        }
        else  if(value instanceof MessageDriven){
            removeMessageDriven((MessageDriven) value);
        }
        
        
    }
    public Ejb[] getEjbs(){
        int sizeEntity = sizeEntity();
        int sizeSession = sizeSession();
        int sizeMessageDriven = sizeMessageDriven();
        int size = sizeEntity + sizeSession + sizeMessageDriven;
        
        Ejb[] ejbs = new Ejb[size];
        Entity[] enBeans = getEntity();
        Session[] ssbeans = getSession();
        MessageDriven[] mdbeans = getMessageDriven();
        int addindex=0;
        for(int i=0; i<sizeEntity ; i++){
            ejbs[addindex] = (Ejb)enBeans[i];
            addindex++;
        }
        for(int j=0; j<sizeSession ; j++){
            ejbs[addindex] = (Ejb)ssbeans[j];
            addindex++;
        }
        
        for(int j=0; j<sizeMessageDriven ; j++){
            ejbs[addindex] = (Ejb)mdbeans[j];
            addindex++;
        }
        return ejbs;
    }
    
}
