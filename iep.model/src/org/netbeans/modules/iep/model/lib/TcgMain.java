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

package org.netbeans.modules.iep.model.lib;

import javax.swing.ImageIcon;
import org.netbeans.modules.iep.model.lib.I18nException;
import org.netbeans.modules.iep.model.lib.TcgCodeType;
import org.netbeans.modules.iep.model.lib.TcgComponent;

/**
 * Description of the Class
 *
 * @author    Bing Lu
 * @created   May 14, 2005
 */
public class TcgMain {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TcgMain.class.getName());

    /**
     * The main program for the TcgMain class
     *
     * @param args              The command line arguments
     * @exception I18nException  Description of the Exception
     */
    public static void main(String[] args) throws I18nException {
        TcgPropertyType one = new TcgPropertyTypeImpl("One", "One", "string", "One", "textField", "string",
                "One-default", "read,write,map", false, true, "", "", false);

        TcgPropertyType two = new TcgPropertyTypeImpl("Two", "Two", "string", "Two", "textField", "string",
                "Two-default", "read,write,map", false, true, "", "", false);

        TcgComponentType num = new TcgComponentTypeImpl("Num", "Num", "Num", "Num", new ImageIcon("Num.gif"), false, true,
                //ritnew TcgCodeType[]{}, new TcgPropertyType[]{one, two}, new TcgComponentType[]{}, new DefaultValidator());
                new TcgCodeType[]{}, new TcgPropertyType[]{one, two}, new TcgComponentType[]{}, null);

        TcgComponentType xxx = new TcgComponentTypeImpl("XXX", "XXX", "XXX", "XXX", new ImageIcon("XXX.gif"), true, true,
                //ritnew TcgCodeType[]{}, new TcgPropertyType[]{}, new TcgComponentType[]{num}, new DefaultValidator());
                     new TcgCodeType[]{}, new TcgPropertyType[]{}, new TcgComponentType[]{num}, null);

        TcgComponent c1 = num.newTcgComponent("c1", "c1");

        mLog.info(c1.toString());

        mLog.info(c1.getProperty("One").getStringValue());
        mLog.info(c1.getProperty("Two").getStringValue());

        c1.getProperty("One").setValue("One-value");
        c1.getProperty("Two").setValue("Two-value");

        mLog.info(c1.getProperty("One").getStringValue());
        mLog.info(c1.getProperty("Two").getStringValue());

        mLog.info(((TcgComponent) c1).toString());

        TcgComponent c2 = xxx.newTcgComponent("c2", "c2");

        mLog.info(((TcgComponent) c2).toString());

        mLog.info(c2.getComponent("Num").getProperty("One").getStringValue());
        mLog.info(c2.getComponent("Num").getProperty("Two").getStringValue());

        c2.getComponent("Num").getProperty("One").setValue("One-value");
        c2.getComponent("Num").getProperty("Two").setValue("Two-value");

        mLog.info(c2.getComponent("Num").getProperty("One").getStringValue());
        mLog.info(c2.getComponent("Num").getProperty("Two").getStringValue());

        mLog.info(c2.toString());

        //c2.getComponent("Num").toXml();

        Object o = (Object) TcgComponentProxy.newProxy(c1, Num.class);
        mLog.info("" + ((Num) o).getOne());
        mLog.info("" + ((Num) o).getTwo());
        mLog.info(((TcgComponent) o).getProperty("One").getStringValue());

        o = TcgCPath.getComponent(c2, "/Num", Num.class);
        mLog.info("" + ((Num) o).getOne());
        mLog.info("" + ((Num) o).getTwo());
        mLog.info("" + ((TcgComponent) o).getProperty("One"));

        mLog.info("" + TcgCPath.getComponent(c2, "/Num/.."));
        mLog.info("" + TcgCPath.getComponent(c2, "/Num/../Num"));

        mLog.info("" + TcgCPath.getComponent(c1, "/"));
        mLog.info("" + TcgCPath.getComponent(c2, "/"));

        mLog.info("" + TcgCPath.getComponent(c1, "."));
        mLog.info("" + TcgCPath.getComponent(c2, "."));

        mLog.info("" + TcgCPath.getComponentList(c2, "/*"));
        mLog.info("" + TcgCPath.getPropertyList(c2, "/Num/@*"));
        mLog.info("" + TcgCPath.getComponent(c2, "/Num"));
        mLog.info("" + TcgCPath.getComponent(c2, "/*[@One='One-value']"));
        mLog.info("" + TcgCPath.getComponent(c2, "/*[@One='x']"));
        mLog.info("" + TcgCPath.getComponent(c2, "/*[@x='x']"));
        mLog.info("" + TcgCPath.getProperty(c1, "/@One"));
        mLog.info("" + TcgCPath.getPropertyValue(c1, "/@One"));

        mLog.info("" + TcgCPath.getComponentList(c2, "./*"));
        mLog.info("" + TcgCPath.getPropertyList(c2, "./Num/@*"));
        mLog.info("" + TcgCPath.getComponent(c2, "./Num"));
        mLog.info("" + TcgCPath.getComponent(c2, "./*[@One='One-value']"));
        mLog.info("" + TcgCPath.getComponent(c2, "./*[@One='x']"));
        mLog.info("" + TcgCPath.getComponent(c2, "./*[@x='x']"));
        mLog.info("" + TcgCPath.getProperty(c1, "./@One"));
        mLog.info("" + TcgCPath.getPropertyValue(c1, "./@One"));
    }
}

/**
 * Description of the Interface
 *
 * @author    Bing Lu
 * @created   May 14, 2005
 */
interface Num {

    /**
     * Gets the one attribute of the Num object
     *
     * @return   The one value
     */
    public Object getOne();

    /**
     * Sets the one attribute of the Num object
     *
     * @param o  The new one value
     */
    public void setOne(Object o);

    /**
     * Gets the two attribute of the Num object
     *
     * @return   The two value
     */
    public Object getTwo();

    /**
     * Sets the two attribute of the Num object
     *
     * @param o  The new two value
     */
    public void setTwo(Object o);
}