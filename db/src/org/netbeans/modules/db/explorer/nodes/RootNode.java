/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
