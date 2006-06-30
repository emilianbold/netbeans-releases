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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.FindCapability;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

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


