/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package com.sun.rave.web.ui.util;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/*
 * Utilities for retrieving messages from FacesMessages
 * TODO: Move to a superclass for Message and MessageGroup only
 */

public class FacesMessageUtils {

    /**
     * Return a iterator that can be used to retrieve messages from
     * FacesContext.
     *
     * @param context The FacesContext of the request
     * @param forComponentId The component associated with the message(s)
     * @param msgComponent The Message, MessageGroup component
     *
     * @return an Iterator over FacesMessages that are queued.
     */
    public static Iterator getMessageIterator(FacesContext context,
    	    String forComponentId, UIComponent msgComponent) {
	
        Iterator messageIterator = null;

	// Return messages for the specified component
	if (forComponentId != null) {
	    if (forComponentId.length() == 0) {
		// Return global messages - not associated with any component.
		messageIterator = context.getMessages(null);
	    } else {
		// Get messages for the specified component only.
		UIComponent forComponent = getForComponent(context, 
							   forComponentId,
							   msgComponent);
		if (forComponent != null) {
		    String clientId = forComponent.getClientId(context);
		    messageIterator = context.getMessages(clientId);
		} else {
		    messageIterator = Collections.EMPTY_LIST.iterator();
		}
	    }
	} else {
	    // No component specified return all messages.
	    messageIterator = context.getMessages();
	}

	return messageIterator;
    }


    /**
     * Walk the component tree looking for the specified component.
     *
     * @param context The FacesContext of the request
     * @param forComponentId The component to look for
     * @param msgComponent The Message, MessageGroup component to start 
     * the search.
     *
     * @return the matching component, or null if no match is found.
     */
    private static UIComponent getForComponent(FacesContext context,
    	    String forComponentId, UIComponent msgComponent) {

        if (forComponentId == null || forComponentId.length() == 0) {
            return null;
        }

        UIComponent forComponent = null;
        UIComponent currentParent = msgComponent;
        try {
            // Check the naming container of the current 
            // component for the forComponent
            while (currentParent != null) {
                // If the current component is a NamingContainer,
                // see if it contains what we're looking for.
                forComponent = currentParent.findComponent(forComponentId);
                if (forComponent != null)
                    break;
                // if not, start checking further up in the view
                currentParent = currentParent.getParent();
            }                   

            // no hit from above, scan for a NamingContainer
            // that contains the component we're looking for from the root.    
            if (forComponent == null) {
                forComponent =
                    findUIComponentBelow(context.getViewRoot(), forComponentId);
            }
        } catch (Throwable t) {
	    //TODO: fix.
            throw new IllegalArgumentException("For component not found");
        }

        if (forComponent == null) {
	    // Log a message.
        }
        return forComponent;
    }


    /**
     * Recursively searches for NamingContainers from the top of the tree
     * looking for the specified component
     *
     * @param context The FacesContext of the request
     * @param forComponentId the component to search for
     *
     * @return the matching component, or null if no match is found.
     * 
     */
    private static UIComponent findUIComponentBelow(UIComponent startComponent,
	String forComponentId) {

        UIComponent forComponent = null;
        List children = startComponent.getChildren();

        for (int i = 0, size = children.size(); i < size; i++) {
            UIComponent comp = (UIComponent) children.get(i);

            if (comp instanceof NamingContainer) {
                forComponent = comp.findComponent(forComponentId);
            }

            if (forComponent == null) {
                if (comp.getChildCount() > 0) {
                    forComponent = findUIComponentBelow(comp, forComponentId);
                }
            }

            if (forComponent != null)
                break;
        }
        return forComponent;
    }
}
