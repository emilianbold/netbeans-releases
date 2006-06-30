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

package org.netbeans.modules.db.explorer.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.impl.SpecificationFactory;
import org.netbeans.modules.db.explorer.DatabaseOption;

//commented out for 3.6 release, need to solve for next Studio release
//import org.netbeans.modules.db.explorer.PointbasePlus;

import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.RootNodeInfo;

/** Abstract class that can be used as super class of all data objects that
* should contain some nodes. It provides methods for adding/removing
* sub nodes.
*/
public class RootNode extends DatabaseNode {
    /** Stores DBNode's connections info */
    private static DatabaseOption option = null;
    private static RootNode rootNode = null;

    /** DDLFactory */
    SpecificationFactory sfactory;

    public static DatabaseOption getOption() {
        if (option == null)
            option = (DatabaseOption)SystemOption.findObject(DatabaseOption.class, true);

        return option;
    }

    public static RootNode getInstance() {
        if (rootNode == null) {
            rootNode = new RootNode();
        }
        return rootNode;
    }
    
    private RootNode() {
        setDisplayName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NDN_Databases")); //NOI18N
        try {
            sfactory = new SpecificationFactory();
            //initialization listener for debug mode
            initDebugListening();
            
//commented out for 3.6 release, need to solve for next Studio release
            //initialization listener for SAMPLE database option
//            initSampleDatabaseListening();
            
            DatabaseNodeInfo nfo = RootNodeInfo.getInstance();
            if (sfactory != null) nfo.setSpecificationFactory(sfactory);
            else throw new Exception(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("EXC_NoSpecificationFactory"));

            setInfo(nfo);
            getInfo().setNode(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean canRename() {
        return false;
    }

    /**
     * Connects the debug property in sfactory and debugMode property in DBExplorer module's option.
     */
    void initDebugListening() {
        if ( (getOption() == null) || (sfactory == null) ) {
            initDebugListening();
        }
        option.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName() == null) {
                    sfactory.setDebugMode(option.getDebugMode());
                    return;
                }
                if (e.getPropertyName().equals(DatabaseOption.PROP_DEBUG_MODE))
                    sfactory.setDebugMode(((Boolean) e.getNewValue()).booleanValue());
            }
        });
        sfactory.setDebugMode(option.getDebugMode());
    }

//commented out for 3.6 release, need to solve for next Studio release
//    void initSampleDatabaseListening() {
//        if ( (getOption() == null) || (sfactory == null) ) {
//            initSampleDatabaseListening();
//        }
//        option.addPropertyChangeListener(new PropertyChangeListener() {
//            public void propertyChange(PropertyChangeEvent e) {
//                if (e.getPropertyName() != null && e.getPropertyName().equals(DatabaseOption.PROP_AUTO_CONNECTION))
//                    if(((Boolean)e.getNewValue()).booleanValue())
//                        try {
//                            PointbasePlus.addOrConnectAccordingToOption();
//                        } catch (Exception exp) {
//                            exp.printStackTrace();
//                        }
//            }
//        });
//    }

    public String getShortDescription() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_Root"); //NOI18N
    }
}
