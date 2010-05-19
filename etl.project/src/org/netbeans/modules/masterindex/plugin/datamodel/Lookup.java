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
package org.netbeans.modules.masterindex.plugin.datamodel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.project.Localizer;


/**
 * @author Sujit Biswas
 * @author Manish Bharani
 */
public class Lookup {

    private static transient final Logger mLogger = Logger.getLogger(Lookup.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private HashMap<String, HashMap<String, Integer>> lookupMap = new HashMap<String, HashMap<String, Integer>>();
    private HashMap<String, Integer> childIndex = new HashMap<String, Integer>();
    private transient ObjectDefinition objectdef;

    /**
     * @param objectdef
     */
    private Lookup(ObjectDefinition objectdef) {
        this.objectdef = objectdef;
        createLookup();
    }

    /**
     *
     * @param objectDefinition
     */
    private Lookup(InputStream objectDefinition) {
        ObjectDefinitionBuilder b = new ObjectDefinitionBuilder();
        this.objectdef = b.parse(objectDefinition);
        createLookup();
    }

    /**
     * populate the lookupMap and the childIndex
     *
     */
    private void createLookup() {
        createLooupMap(lookupMap, objectdef, objectdef.getName());
        createChildIndex(childIndex, objectdef, objectdef.getName());

    }

    /**
     * populate the childIndex
     *
     */
    private void createChildIndex(HashMap<String, Integer> childTypeIndex,
            ObjectDefinition context, String prefix) {
        for (int i = 0; i < context.getChildren().size(); i++) {

            ObjectDefinition child = context.getChildren().get(i);
            String cname = child.getName();
            String key = prefix + "." + cname;
            childTypeIndex.put(key, i);
            createChildIndex(childTypeIndex, child, key);
        }

    }

    /**
     * populate the lookupMap
     *
     */
    private void createLooupMap(HashMap<String, HashMap<String, Integer>> lmap,
            ObjectDefinition context, String prefix) {

        lmap.put(prefix, createFieldMap(context));

        for (int i = 0; i < context.getChildren().size(); i++) {

            ObjectDefinition child = context.getChildren().get(i);
            String cname = child.getName();
            String key = prefix + "." + cname;
            createLooupMap(lmap, child, key);
        }

    }

    /**
     * create the field map for a given ObjectDefinition
     *
     * @param context
     * @return
     */
    private HashMap<String, Integer> createFieldMap(ObjectDefinition context) {

        HashMap<String, Integer> map = new HashMap<String, Integer>();

        ArrayList<Field> fields = context.getFields();

        for (int i = 0; i < fields.size(); i++) {
            map.put(fields.get(i).getName(), i);
        }
        return map;
    }

    /**
     * utility method to create Lookup from objectDefinition InputStream
     *
     * @param objectDefinition
     * @return
     */
    public static Lookup createLookup(InputStream objectDefinition) {

        Lookup l = new Lookup(objectDefinition);
        return l;

    }

    /**
     * utility method to create Lookup from objectDefinition Object
     *
     * @param objectDefinition
     * @return
     */
    public static Lookup createLookup(ObjectDefinition objectDefinition) {
        Lookup l = new Lookup(objectDefinition);
        return l;

    }

    /**
     * returns the field index for a given fieldName and prefix where given an
     * ePath Person.Address.city , fieldName=city and prefix=Person.Address,
     * will return -1 if the field does not exist in the object definition
     *
     * @param fieldName
     * @param prefix
     * @return
     */
    public int getFieldIndex(String fieldName, String prefix) {

        if (lookupMap.get(prefix) == null) {
            return -1;
        }

        Integer i = lookupMap.get(prefix).get(fieldName);

        if (i == null) {
            return -1;
        } else {
            return i.intValue();
        }

    }

    /**
     * returns the child index for a given prefix can be Person.Address
     *
     * @param prefix
     * @return
     */
    public int getChildTypeIndex(String prefix) {
        Integer i = childIndex.get(prefix);

        if (i == null) {
            return -1;
        } else {
            return i.intValue();
        }
    }

    public String getChildTypeName(int j) {
        Iterator i = childIndex.keySet().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            if ((int) childIndex.get(key) == j) {
                return key.substring(key.indexOf(".") + 1);
            }
        }
        return null;
    }

    public HashMap getChildIndexMap() {
        return this.childIndex;
    }

    public HashMap getLookupMap() {
        return this.lookupMap;
    }

    public ArrayList getFields(String tablename) {
        // Look out for parent fields
        if (this.objectdef.getName().equals(tablename)) {
            return objectdef.getFields();
        } else {
            // Look out for children fields
            for (int i = 0; i < objectdef.getChildren().size(); i++) {
                ObjectDefinition odef = (ObjectDefinition) this.objectdef.getchild(i);
                if (odef.getName().equals(tablename)) {
                    return odef.getFields();
                }
            }
        }

        mLogger.infoNoloc(mLoc.t("PRJS014: Fields for table [{0} cannot be found in the data model", tablename));
        return null;
    }

    public String getRootName() {
        return this.objectdef.getName();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {
            //FileInputStream fis = new FileInputStream("D:/temp/eviewconfig/objectdef.xml");
            FileInputStream fis = new FileInputStream("D:/temp/forMANISH/objectdef.xml");
            Lookup l = createLookup(fis);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
