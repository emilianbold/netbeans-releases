/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.core.syntax.completion.api;

import java.awt.Color;

import javax.swing.ImageIcon;

import org.openide.util.ImageUtilities;

/**
 *
 * @author marekfukala
 */
public class ElCompletionItem {

    public static JspCompletionItem createELImplicitObject(String name, int substitutionOffset, int type) {
        return new ELImplicitObject(name, substitutionOffset, type);
    }

    public static JspCompletionItem createELBean(String name, int substitutionOffset, String type) {
        return new ELBean(name, substitutionOffset, type);
    }

    public static JspCompletionItem createELProperty(String name, String insertText, 
            int substitutionOffset, String type) 
    {
        return new ELProperty(name, insertText, substitutionOffset, type);
    }

    public static JspCompletionItem createELFunction(String name, int substitutionOffset, String type, String prefix, String parameters) {
        return new ELFunction(name, substitutionOffset, type, prefix, parameters);
    }

    public static class ELImplicitObject extends JspCompletionItem {

        private static final String OBJECT_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/class_16.png"; //NOI18N
        private static final String MAP_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/map_16.png";      //NOI18N
        int type;

        ELImplicitObject(String text, int substitutionOffset, int type) {
            super(text, substitutionOffset);
            this.type = type;
        }

        @Override
        public int getSortPriority() {
            return 15;
        }

        @Override
        protected ImageIcon getIcon() {
            ImageIcon icon = null;
            switch (type) {
                case org.netbeans.modules.web.core.syntax.spi.ELImplicitObject.OBJECT_TYPE:
                    icon = ImageUtilities.loadImageIcon(OBJECT_PATH, false);
                    break;
                case org.netbeans.modules.web.core.syntax.spi.ELImplicitObject.MAP_TYPE:
                    icon = ImageUtilities.loadImageIcon(MAP_PATH, false);
                    break;
            }
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color=#0000ff>" + getItemText() + "</font>";
        }

        @Override
        public String getItemText() {
            String result = text;
            if (type == org.netbeans.modules.web.core.syntax.spi.ELImplicitObject.MAP_TYPE) {
                result = result + "[]";
            }
            return result;    //NOI18N
        }

        @Override
        protected int getMoveBackLength() {
            return type == org.netbeans.modules.web.core.syntax.spi.ELImplicitObject.MAP_TYPE ? 1 : 0;
        }
    }

    public static class ELBean extends JspCompletionItem {

        private static final String BEAN_NAME_COLOR = hexColorCode(Color.blue.darker().darker());
        private static final String BEAN_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/bean_16.png";    //NOI18N
        private String type;

        public ELBean(String text, int substitutionOffset, String type) {
            super(text, substitutionOffset);
            parseType(type);
        }

        /** Parses property type.
         * expected inputs:
         * java.util.Collection<java.lang.String>
         * java.util.Collection
         * Collection
         */
        private void parseType(String t) {
            int ltIndex = t.indexOf("<");

            String mainType = ltIndex == -1 ? t : t.substring(0, ltIndex);
            String genericType = ltIndex == -1 ? null : t.substring(ltIndex + 1);

            this.type = stripPackage(mainType) + (genericType != null ? "<" + stripPackage(genericType) : ""); //NOI18N

            //replace illegal html chars
            this.type = type.replace("<", "&lt;").replace(">", "&gt;"); //NOI18N
        }

        private String stripPackage(String type) {
            int dotIndex = type.lastIndexOf('.');
            if (dotIndex >= 0) {
                type = type.substring(dotIndex + 1);
            }
            return type;
        }

        @Override
        public int getSortPriority() {
            return 10;
        }

        @Override
        protected String getLeftHtmlText() {
            return "<font color=#" + BEAN_NAME_COLOR + ">" + getItemText() + "</font>";
        }

        @Override
        protected String getRightHtmlText() {
            return this.type;
        }

        @Override
        protected ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon(BEAN_PATH, false);
        }
    }

    public static class ELProperty extends ELBean {

        private static final String PROPERTY_NAME_COLOR = hexColorCode(Color.blue.darker().darker());
        private static final String PROPERTY_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/property_16.png"; //NOI18N

        public ELProperty(String text, int substitutionOffset, 
                String type) 
        {
            this(text, text , substitutionOffset, type);
        }
        
        public ELProperty(String text, String insertText , int substitutionOffset, 
                String type) 
        {
            super(text, substitutionOffset, type);
            myInsertText = insertText;
        }

        protected String getSubstituteText() {
            return myInsertText;
        }
        
        @Override
        protected String getLeftHtmlText() {
            return "<font color=#" + PROPERTY_NAME_COLOR + ">" + getItemText() + "</font>";
        }

        @Override
        protected ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon(PROPERTY_PATH, false);
        }
        
        private String myInsertText;
    }

    public static class ELFunction extends ELBean {

        private static final String PREFIX_COLOR = hexColorCode(Color.blue.darker().darker());
        private static final String FUNCTION_NAME_COLOR = hexColorCode(Color.black);
        private static final String PARAMETER_COLOR = hexColorCode(Color.black);
        private static final String ICON_PATH = "org/netbeans/modules/web/core/syntax/completion/resources/function_16.png";
        private String prefix;
        private String parameters;

        public ELFunction(String name, int substitutionOffset, String type, String prefix, String parameters) {
            super(name, substitutionOffset, type);
            this.prefix = prefix;
            this.parameters = parameters;
        }

        @Override
        protected String getLeftHtmlText() {
            StringBuilder lText = new StringBuilder();
            lText.append("<font color=#" + PREFIX_COLOR + "><b>" + prefix + "</b></font>");
            lText.append("<font color=#" + FUNCTION_NAME_COLOR + "><b>" + ":" + getItemText() + "</b></font>");
            lText.append("<font color=#" + PARAMETER_COLOR + ">" + "(" + "</b></font>");
            if (parameters != null) {
                lText.append("<font color=#" + PARAMETER_COLOR + ">" + parameters + "</b></font>");
            }
            lText.append("<font color=#" + PARAMETER_COLOR + ">" + ")" + "</b></font>");
            return lText.toString();

        }

        @Override
        protected ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon(ICON_PATH, false);
        }

        @Override
        public int getSortPriority() {
            return 12;
        }

        @Override
        protected String getSubstituteText() {
            return prefix + ":" + super.getItemText() + "()";    //NOI18N
        }

        @Override
        protected int getMoveBackLength() {
            return 1;
        }
    }
}
