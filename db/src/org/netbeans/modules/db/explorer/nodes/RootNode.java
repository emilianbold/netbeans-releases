/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.nodes;

import java.sql.*;
import java.util.*;

import org.openide.nodes.*;
import org.openide.NotifyDescriptor;
import org.openide.options.SystemOption;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.PointbasePlus;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/** Abstract class that can be used as super class of all data objects that
* should contain some nodes. It provides methods for adding/removing
* sub nodes.
*/
public class RootNode extends DatabaseNode {
    /** Stores DBNode's connections info */
    private static DatabaseOption option = null;

    /** DDLFactory */
    SpecificationFactory sfactory;

    public static DatabaseOption getOption() {
        if (option == null)
            option = (DatabaseOption)SystemOption.findObject(DatabaseOption.class, true);

        return option;
    }

    public RootNode() {
        try {
            sfactory = new SpecificationFactory();
            //initialization listener for debug mode
            initDebugListening();
            //initialization listener for SAMPLE database option
            initSampleDatabaseListening();
            DatabaseNodeInfo nfo = DatabaseNodeInfo.createNodeInfo(null, "root"); //NOI18N
            if (sfactory != null) nfo.setSpecificationFactory(sfactory);
            else throw new Exception(bundle.getString("EXC_NoSpecificationFactory"));

            setInfo(nfo);
            getInfo().setNode(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean canRename() {
        return true;
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
                if (e.getPropertyName().equals(DatabaseOption.PROP_DEBUG_MODE)){
                    sfactory.setDebugMode(
                        ((Boolean) e.getNewValue()).booleanValue()
                    );
                }
            }
        });
        sfactory.setDebugMode(option.getDebugMode());
    }

    void initSampleDatabaseListening() {
        if ( (getOption() == null) || (sfactory == null) ) {
            initSampleDatabaseListening();
        }
        option.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName() != null && e.getPropertyName().equals(DatabaseOption.PROP_AUTO_CONNECTION))
                    if(((Boolean)e.getNewValue()).booleanValue())
                        try {
                            PointbasePlus.addOrConnectAccordingToOption();
                        } catch (Exception exp) {
                            exp.printStackTrace();
                        }
            }
        });
    }

}
