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


/*
 * ElementCastException.java
 *
 * Created on 31 Март 2005 г., 16:42
 */

package org.netbeans.test.umllib.exceptions;

/**
 * example: can be thrown on attemp to 
 * @author psb
 */
public class UnexpectedMenuItemStatusException extends UMLCommonException {
    
    /**
     * Creates a new instance of UnexpectedMenuItemStatusException
     * @param message 
     */
    public UnexpectedMenuItemStatusException(String message) {
        super(message);
    }
    /**
     * Creates a new instance of UnexpectedMenuItemStatusException
     * @param message 
     * @param st 
     */
    public UnexpectedMenuItemStatusException(String message,Status st) {
        super(message);
        status=st;
    }
    /**
     * Creates a new instance of UnexpectedMenuItemStatusException
     * @param message 
     * @param mn 
     */
    public UnexpectedMenuItemStatusException(String message,MenuType mn) {
        super(message);
        type=mn;
    }
    /**
     * Creates a new instance of UnexpectedMenuItemStatusException
     * @param message 
     * @param st 
     * @param mn 
     */
    public UnexpectedMenuItemStatusException(String message,Status st,MenuType mn) {
        super(message);
        status=st;
        type=mn;
    }
    /**
     * Creates a new instance of UnexpectedMenuItemStatusException
     * @param message 
     * @param st 
     * @param mn 
     * @param Id 
     */
    public UnexpectedMenuItemStatusException(String message,Status st,MenuType mn,int Id) {
        super(message);
        status=st;
        type=mn;
        id=Id;
    }
    
    private Status status=Status.UNKNOWN;
    private MenuType type=MenuType.UNKNOWN;
    private int id;
    
    /**
     * 
     * @return 
     */
    public Status getStatus()
    {
         return status;
    }
    /**
     * 
     * @return 
     */
    public MenuType getMenuType()
    {
         return type;
    }
    /**
     * 
     * @return 
     */
    public int getId()
    {
         return id;
    }
    
     /**
     * Status for popup or menu items
     */
    static public enum Status
    {
        UNKNOWN("Any or unknown status for menu item."),
        DISABLED("Item is disabled."),
        ENABLED("Item is enabled."),
        EXIST("Item exists."),
        ABSENT("Item is absent.");
                
        private String description="";
        
        /**
         * 
         * @param desc 
         */
        Status(String desc)
        {
            description=desc;
        }
        Status()
        {
            description=name();
        }
        
    }
     /**
     * MenuType
     */
    static public enum MenuType
    {
        UNKNOWN("Any or unknown menu type."),
        TREENODE_POPUP(),
        DIAGRAMELEMENT_POPUP(),
        MAINMENU();
                
        private String description="";
        
        /**
         * 
         * @param desc 
         */
        MenuType(String desc)
        {
            description=desc;
        }
        MenuType()
        {
            description=name();
        }
        
        /**
         * 
         * @param dsc 
         * @return 
         */
        public static MenuType TREENODE_POPUP(String dsc)
        {
            MenuType ret=TREENODE_POPUP;
            ret.description=dsc;
            return ret;
        }
        /**
         * 
         * @param dsc 
         * @return 
         */
        public static MenuType DIAGRAMELEMNT_POPUP(String dsc)
        {
            MenuType ret=DIAGRAMELEMENT_POPUP;
            ret.description=dsc;
            return ret;
        }
        /**
         * 
         * @param dsc 
         * @return 
         */
        public static MenuType MAINMENU(String dsc)
        {
            MenuType ret=MAINMENU;
            ret.description=dsc;
            return ret;
        }
    }
   
}
