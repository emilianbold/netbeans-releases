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

package org.netbeans.modules.j2ee.dd.api.ejb;

// 
// This interface has all of the bean info accessor methods.
// 
import org.netbeans.api.web.dd.common.CommonDDBean;
import org.netbeans.api.web.dd.common.FindCapability;
import org.netbeans.api.web.dd.common.VersionNotSupportedException;

public interface EnterpriseBeans extends CommonDDBean, FindCapability {
        
        public static final String SESSION = "Session";	// NOI18N
	public static final String ENTITY = "Entity";	// NOI18N
	public static final String MESSAGE_DRIVEN = "MessageDriven";	// NOI18N
        
        public void setSession(int index, Session value);
        
        public void setSession(Session[] value);
        
        public Session getSession(int index);       

        public Session[] getSession();
        
	public int addSession(org.netbeans.modules.j2ee.dd.api.ejb.Session value);

	public int removeSession(org.netbeans.modules.j2ee.dd.api.ejb.Session value);
        
        public int sizeSession();
        
        public Session newSession();
                
        public void setEntity(int index, Entity value);
        
        public void setEntity(Entity[] value);
        
        public Entity getEntity(int index);       

        public Entity[] getEntity();
        
	public int removeEntity(org.netbeans.modules.j2ee.dd.api.ejb.Entity value);

	public int addEntity(org.netbeans.modules.j2ee.dd.api.ejb.Entity value);
        
        public int sizeEntity();
	
        public Entity newEntity();
        
        public void setMessageDriven(int index, MessageDriven value);

        public MessageDriven getMessageDriven(int index);

        public void setMessageDriven(MessageDriven[] value);

        public MessageDriven[] getMessageDriven();
        
	public int addMessageDriven(org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven value);

	public int sizeMessageDriven();

	public int removeMessageDriven(org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven value);

        public MessageDriven newMessageDriven();
        
        public Ejb[] getEjbs();
        
        public void removeEjb( Ejb value);
        
}


