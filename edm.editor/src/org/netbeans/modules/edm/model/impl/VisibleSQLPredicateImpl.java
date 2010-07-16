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
package org.netbeans.modules.edm.model.impl;

import org.netbeans.modules.edm.editor.utils.TagParserUtility;
import org.netbeans.modules.edm.model.GUIInfo;
import org.netbeans.modules.edm.model.SQLCanvasObject;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLInputObject;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.VisibleSQLPredicate;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.netbeans.modules.edm.model.EDMException;
import org.openide.util.NbBundle;

/**
 * Extends SQLPredicate to represent boolean conditional expressions that exist as UI
 * elements on the SQLBuilder collaboration definition canvas.
 * 
 * @author Jonathan Giron
 */

public class VisibleSQLPredicateImpl extends SQLPredicateImpl implements VisibleSQLPredicate {

    /**
     * Extends VisibleSQLPredicateImpl class to handle predicates with single inputs.
     * 
     * @author Jonathan Giron
     * @version $Revision$
     */
    public static class LeftUnary extends VisibleSQLPredicateImpl {

        /** Creates a new instance of VisibleSQLPredicateImpl.LeftUnary */
        public LeftUnary() {
            super();
        }

        /** Creates a clone using the passed LeftUnary object */
        public LeftUnary(LeftUnary obj) throws EDMException {
            super(obj);
        }

        @Override
        public void addInput(String argName, SQLObject newInput) throws EDMException {
            if (LEFT.equals(argName)) {
                super.addInput(argName, newInput);
            }
        }

        /**
         * Clone this object.
         */
        public Object clone() throws CloneNotSupportedException {
            try {
                LeftUnary unaryPredicate = new LeftUnary(this);
                return unaryPredicate;
            } catch (EDMException ex) {
                throw new CloneNotSupportedException(NbBundle.getMessage(VisibleSQLPredicateImpl.class, "ERROR_can_not_create_clone") + this.getOperatorType());
            }
        }

        @Override
        public SQLObject removeInputByArgName(String argName, SQLObject sqlObj) throws EDMException {
            if (LEFT.equals(argName)) {
                return super.removeInputByArgName(argName, sqlObj);
            }
            return null;
        }

        @Override
        public void secondPassParse(Element element) throws EDMException {
            super.secondPassParse(element);

            // Unconditionally remove RIGHT input object.
            inputMap.remove(RIGHT);
        }

        @Override
        protected void postProcessInputList() {
            super.postProcessInputList();

            // Only remove RIGHT input object if it has been resolved. Otherwise, defer
            // until secondPassParse is called.
            SQLInputObject rightInput = getInput(RIGHT);
            if (rightInput != null && rightInput.getSQLObject() != null) {
                inputMap.remove(RIGHT);
            }
        }
    }

    /**
     * Extends VisibleSQLPredicateImpl class to handle predicates with single inputs.
     */
    public static class RightUnary extends VisibleSQLPredicateImpl {

        /** Creates a new instance of VisibleSQLPredicateImpl.LeftUnary */
        public RightUnary() {
            super();
        }

        /** Creates a clone using the passed LeftUnary object */
        public RightUnary(RightUnary obj) throws EDMException {
            super(obj);
        }

        @Override
        public void addInput(String argName, SQLObject newInput) throws EDMException {
            if (RIGHT.equals(argName)) {
                super.addInput(argName, newInput);
            }
        }

        /**
         * Clone this object.
         */
        @Override
        public Object clone() throws CloneNotSupportedException {
            try {
                RightUnary unaryPredicate = new RightUnary(this);
                return unaryPredicate;
            } catch (EDMException ex) {
                throw new CloneNotSupportedException(NbBundle.getMessage(VisibleSQLPredicateImpl.class, "ERROR_can_not_create_clone") + this.getOperatorType());
            }
        }

        @Override
        public SQLObject removeInputByArgName(String argName, SQLObject sqlObj) throws EDMException {
            if (RIGHT.equals(argName)) {
                return super.removeInputByArgName(argName, sqlObj);
            }
            return null;
        }

        @Override
        public void secondPassParse(Element element) throws EDMException {
            super.secondPassParse(element);

            // Unconditionally remove LEFT input object.
            inputMap.remove(LEFT);
        }

        @Override
          protected void postProcessInputList() {
            super.postProcessInputList();

            // Only remove LEFT input object if it has been resolved. Otherwise, defer
            // until secondPassParse is called.
            SQLInputObject rightInput = getInput(LEFT);
            if (rightInput != null && rightInput.getSQLObject() != null) {
                inputMap.remove(LEFT);
            }
        }
    }

    /* GUI state info */
    protected GUIInfo guiInfo = new GUIInfo();

    /** Creates a new instance of SQLPredicate */
    public VisibleSQLPredicateImpl() {
        super();
        type = SQLConstants.VISIBLE_PREDICATE;
    }

    public VisibleSQLPredicateImpl(VisibleSQLPredicate src) throws EDMException {
        this();
        if (src == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(VisibleSQLPredicateImpl.class, "ERROR_Cannot_create_VisibleSQLPredicate"));
        }

        super.copyFrom(src);

        // copy GUI info
        GUIInfo gInfo = src.getGUIInfo();
        this.guiInfo = gInfo != null ? (GUIInfo) gInfo.clone() : null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            VisibleSQLPredicateImpl predicate = new VisibleSQLPredicateImpl(this);
            return predicate;
        } catch (EDMException ex) {
            throw new CloneNotSupportedException(NbBundle.getMessage(VisibleSQLPredicateImpl.class, "ERROR_can_not_create_clone") + this.getOperatorType());
        }
    }

    /**
     * @see java.lang.Object#equals
     */
    @Override
    public boolean equals(Object refObj) {
        if (!(refObj instanceof VisibleSQLPredicate)) {
            return false;
        }

        return super.equals(refObj);
    }

    /**
     * @see SQLCanvasObject#getGUIInfo
     */
    public GUIInfo getGUIInfo() {
        return guiInfo;
    }

    /**
     * @see java.lang.Object#hashCode
     */
    @Override
    public int hashCode() {
        int myHash = super.hashCode();

        myHash += (guiInfo != null) ? guiInfo.hashCode() : 0;

        return myHash;
    }

    /**
     * Parses the given SQLPredicate Element
     * 
     * @param xmlElement Element to be parsed
     * @throws EDMException if error occurs while parsing
     */
    @Override
    public void parseXML(Element xmlElement) throws EDMException {
        super.parseCommonFields(xmlElement);

        NodeList inputArgList = xmlElement.getElementsByTagName(SQLObject.TAG_INPUT);
        TagParserUtility.parseInputTagList(this, inputArgList);
        postProcessInputList();

        NodeList guiInfoList = xmlElement.getElementsByTagName(GUIInfo.TAG_GUIINFO);
        if (guiInfoList != null && guiInfoList.getLength() != 0) {
            Element elem = (Element) guiInfoList.item(0);
            guiInfo = new GUIInfo(elem);
        }
    }

    /**
     * Overrides parent implementation to append GUIInfo information.
     * 
     * @param prefix String to append to each new line of the XML representation
     * @return XML representation of this SQLObject instance
     */
    @Override
    public String toXMLString(String prefix) throws EDMException {
        StringBuilder buffer = new StringBuilder(500);
        if (prefix == null) {
            prefix = "";
        }

        buffer.append(prefix).append(getHeader());
        buffer.append(toXMLAttributeTags(prefix));
        buffer.append(TagParserUtility.toXMLInputTag(prefix + "\t", this.inputMap));
        buffer.append(this.guiInfo.toXMLString(prefix + "\t"));
        buffer.append(prefix).append(getFooter());

        return buffer.toString();
    }

    /**
     * Hook to allow subclasses to modify SQLInputObjects arguments after parsing them
     * into the predicate. Default implementation is empty.
     */
    protected void postProcessInputList() {
        // Do nothing.
    }
}

