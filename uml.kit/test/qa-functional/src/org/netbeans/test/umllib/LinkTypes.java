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




package org.netbeans.test.umllib;

/**
 * All types of links must be defined here
 * and used as contants in other places
 * @author Alexei Mokeev
 */
public enum LinkTypes {
    // This means ANY link */
    ANY("Any"),
    
    //Context Palette 
    IMPLEMENTATION("Implementation"),
    GENERALIZATION("Generalization"),
    COMMENT_LINK("CommentLink"),
  
    // Association 
    ASSOCIATION("Association"),
    ASSOCIATION_CLASS("AssociationClass"),
    COMPOSITION("Composition"),
    NAVIGABLE_COMPOSITION("Navigable Composition"),
    AGGREGATION("Aggregation"),
    NAVIGABLE_AGGREGATION("Navigable Aggregation"),
    NAVIGABLE_ASSOCIATION("Navigable Association"),
    
    // Dependece  
    DEPENDENCY("Dependency"),
    REALIZE("Realization"),
    USAGE("Usage"),
    PERMISSION("Permission"),
    ABSTRACTION("Abstraction"), 
  
    NESTED_LINK("Class"),
    CLASS("Class"),

    DERIVATION_EDGE("Derivation"), 
    DELEGATE("Delegate"),
    ASSEMBLY("AssemblyConnector"),
    
    
    //activity diagram specific
    ACTIVITY_EDGE("MultiFlow"),
    COMMENT("Comment"),
    
    //collab diagram specific
    CONNECTOR("MessageConnector"),
    
    //usecase diagram specific
    INCLUDE("Include"),
    EXTEND("Extend"),
    
    //seq spec
    SYNC_MESSAGE("Synchronous Message"), 
    MESSAGE_TO_SELF("Message"), //this should be corrected, no such type exists 
    ASYNC_MESSAGE("Asynchronous Message"),  
    CREATE_MESSAGE("Create Message"),  
    DESTROY_LIFELINE("Destroy Lifeline"),
    MESSAGE("Message"),
    PART_FACADE("PartFacade"),
    ROLE("Role"),
    PACKAGE("Package"),
    //patterns
    ROLE_BINDING("PartFacade"),
    //PartFacada - inner type of nested links with title Class Role,Interface Role.. etc
    CLASS_ROLE("PartFacade"),
    INTERFACE_ROLE("PartFacade"),
    ACTOR_ROLE("PartFacade"),
    USE_CASE_ROLE("PartFacade");
    
    
    private String val = "";
    
    private LinkTypes(String val){
        this.val = val;
    }
    
    /**
     * 
     * @return 
     */
    public String toString(){
        return val;
    }
    
    //workaround for morphing nested link
    /**
     * 
     * @param toType 
     * @return 
     */
    public LinkTypes NESTED_LINK(String toType)
    {
        LinkTypes tmp=NESTED_LINK;
        tmp.val=toType;
        return tmp;
    }
}
