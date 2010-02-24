/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 *
 * @todo fix minimum multiplicity, now we always anticipate 1-sg.
 * 
 * @author marek
 */
public class PropertyModel {

    private static PropertyModel instance;

    public static synchronized PropertyModel instance() {
        if (instance == null) {
            instance = new PropertyModel("org/netbeans/modules/css/resources/css_property_table"); //NOI18N
        }
        return instance;
    }

    private PropertyModel(String sourcePath) {
        parseSource(sourcePath);
    }
    private Map<String, Property> properties;

    public Collection<Property> properties() {
        return properties.values();
    }

    public Property getProperty(String name) {
        return properties.get(name.toLowerCase());
    }

    private void parseSource(String sourcePath) {
        ResourceBundle bundle = NbBundle.getBundle(sourcePath);

        properties = new HashMap<String, Property>();

        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String name = keys.nextElement();
            String value = bundle.getString(name);

            //parse the value - delimiter is semicolon
            StringTokenizer st = new StringTokenizer(value, ";"); //NOI18N
            String values = st.nextToken();

            String initialValue = st.nextToken().trim();
            String appliedTo = st.nextToken().trim();
            boolean inherited = Boolean.parseBoolean(st.nextToken());
            String percentages = st.nextToken().trim();

            //parse media groups list
            String mediaGroups = st.nextToken();
            ArrayList<String> mediaGroupsList = new ArrayList<String>();
            StringTokenizer st3 = new StringTokenizer(mediaGroups, ","); //NOI18N
            while (st3.hasMoreTokens()) {
                mediaGroupsList.add(st3.nextToken());
            }

            if (st.hasMoreTokens()) {
                Logger.global.warning("Error in source for css properties model for property: " + name); //NOI18N
            }

            //parse bundle key - there might be more properties separated by semicolons
            StringTokenizer nameTokenizer = new StringTokenizer(name, ";"); //NOI18N

            while (nameTokenizer.hasMoreTokens()) {
                String parsed_name = nameTokenizer.nextToken().trim();

                Property prop = new Property(parsed_name, initialValue, values,
                        appliedTo, inherited, percentages, mediaGroupsList);

                properties.put(parsed_name, prop);
            }

        }

    }

    GroupElement parse(String expresssion) {
        return parse(expresssion, null);
    }
    
    GroupElement parse(String expression, String propertyName) {
        group_index = 0;
        GroupElement root = new GroupElement(null, group_index, propertyName);
        ParserInput input = new ParserInput(expression);
        group_index = 0; //reset
        parseElements(input, root, false);
        return root;
    }
    private int group_index;

    private void parseElements(ParserInput input, GroupElement parent, boolean ignoreInherits) {
        Element last = null;
        for (;;) {
            char c = input.read();
            if (c == Character.MAX_VALUE) {
                return;
            }
            switch (c) {
                case ' ':
                case '\t':
                    //ws, ignore
                    break;
                case '[':
                    //group start
                    last = new GroupElement(parent, ++group_index);
                    parseElements(input, (GroupElement) last, false);
                    parent.addElement(last);
                    break;

                case '|':
                    char next = input.read();
                    if (next == '|') {
                        //the group is a list
                        parent.setIsList(true);
                    } // else it means OR, ignore
                    break;

                case '>':
                    parent.setIsSequence(true);
                    break;

                case ']':
                    //group end
                    return;

                case '\'':
                    //reference
                    StringBuffer buf = new StringBuffer();
                    for (;;) {
                        c = input.read();
                        if (c == '\'') {
                            break;
                        } else {
                            buf.append(c);
                        }
                    }

                    //resolve reference
                    String referredElementName = buf.toString();
                    Property p = getProperty(referredElementName);

                    if (p == null) {
                        throw new IllegalStateException("no referred element '" + referredElementName + "' found!"); //NOI18N
                    }

                    last = new GroupElement(parent, ++group_index, referredElementName);

//                    System.out.println("resolving element " + referredElementName + " (" + p.valuesText() + ") into group " + last.toString()); //NOI18N
                    ParserInput pinput = new ParserInput(p.valuesText());

                    //ignore inherit tokens in the subtree
                    parseElements(pinput, (GroupElement) last, true);

//                    last = new ReferenceElement(parent);
//                    ((ReferenceElement)last).setReferedElementName(buf.toString());
                    parent.addElement(last);
                    break;

                case '!':
                    //unit value
                    buf = new StringBuffer();
                    for (;;) {
                        c = input.read();
                        if (c == Character.MAX_VALUE) {
                            break;
                        }
                        if (isEndOfValueChar(c)) {
                            input.backup(1);
                            break;
                        } else {
                            buf.append(c);
                        }
                    }

                    last = new ValueElement(parent);
                    ((ValueElement) last).setValue(buf.toString());
                    ((ValueElement) last).setIsUnit(true);
                    parent.addElement(last);
                    break;

                case '{':
                    //multiplicity range {min,max}
                    StringBuffer text = new StringBuffer();
                    for (;;) {
                        c = input.read();
                        if (c == '}') {
                            break;
                        } else {
                            text.append(c);
                        }
                    }
                    StringTokenizer st = new StringTokenizer(text.toString(), ","); //NOI18N
                    int min = Integer.parseInt(st.nextToken());
                    int max = Integer.parseInt(st.nextToken());
                    
                    last.setMinimumOccurances(min);
                    last.setMaximumOccurances(max);
                    
                    break;

                case '+':
                    //multiplicity 1-infinity
                    last.setMaximumOccurances(Integer.MAX_VALUE);
                    break;

                case '*':
                    //multiplicity 0-infinity
                    last.setMinimumOccurances(0);
                    last.setMaximumOccurances(Integer.MAX_VALUE);
                    break;

                case '?':
                    //multiplicity 0-1
                    last.setMinimumOccurances(0);
                    last.setMaximumOccurances(1);
                    break;


                default:
                    //values
                    buf = new StringBuffer();
                    for (;;) {
                        if (c == Character.MAX_VALUE) {
                            break;
                        }
                        if (isEndOfValueChar(c)) {
                            input.backup(1);
                            break;
                        } else {
                            buf.append(c);
                        }

                        c = input.read(); //also include the char from main loop

                    }
                    String image = buf.toString();

                    if (!(ignoreInherits && "inherit".equalsIgnoreCase(image))) { //NOI18N
                        last = new ValueElement(parent);
                        ((ValueElement) last).setValue(image);
                        ((ValueElement) last).setIsUnit(false);
                        parent.addElement(last);
                    }
                    break;


            }
//            break;
        }

    }

    private static boolean isEndOfValueChar(char c) {
        return c == ' ' || c == '+' || c == '?' || c == '{' || c == '[' || c == ']' || c == '|';
    }

    private static class ParserInput {

        CharSequence text;
        private int pos = 0;

        private ParserInput(CharSequence text) {
            this.text = text;
        }

        public char read() {
            if (pos == text.length()) {
                return Character.MAX_VALUE;
            } else {
                return text.charAt(pos++);
            }
        }

        public void backup(int chars) {
            pos -= chars;
        }
    }

    public static abstract class Element {

        private GroupElement parent;

        public Element(GroupElement parent) {
            this.parent = parent;
        }
        private int minimum_occurances = 1;
        private int maximum_occurances = 1;

        void setMinimumOccurances(int i) {
            this.minimum_occurances = i;
        }

        void setMaximumOccurances(int i) {
            maximum_occurances = i;
        }

        public int getMaximumOccurances() {
            return maximum_occurances;
        }

        public int getMinimumOccurances() {
            return minimum_occurances;
        }

        public GroupElement parent() {
            return parent;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Element)) {
                return false;
            }
            Element e = (Element) o;
            return path().equalsIgnoreCase(e.path());
        }

        /** returns a name of the property from which this element comes from */
        public String origin() {
            GroupElement p = parent;
            while(p != null) {
                if(p.referenceName != null) {
                    return p.referenceName;
                }
                p = p.parent();
            }
            return null;
        }
        
        public String getResolvedOrigin() {
            String origin = origin();
            if (origin == null) {
                return null;
            }
            if (origin.startsWith("-")) { //NOI18N
                //artificial origin, get real origin from the first ancestor element with an origin
                Element parentElement = this;
                while ((parentElement = parentElement.parent()) != null) {
                    if (parentElement.origin() != null && !parentElement.origin().startsWith("-")) {
                        origin = parentElement.origin();
                        break;
                    }
                }
            }
            return origin;
        }

        public String path() {
            StringBuffer sb = new StringBuffer();
            if (parent() != null) {
                sb.append(parent().path());
                sb.append('/');
            }
            sb.append(toString());
            return sb.toString();
        }

        @Override
        public String toString() {
            if (getMinimumOccurances() != 1 || getMaximumOccurances() != 1) {
                return "{" + getMinimumOccurances() + "," + (getMaximumOccurances() == Integer.MAX_VALUE ? "inf" : getMaximumOccurances()) + "}";  //NOI18N
            } else {
                return ""; //NOI18N
            }
        }

        public String toString2(int level) {
            return indentString(level) + toString();
        }

        protected String indentString(int level) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < level; i++) {
                sb.append('\t');
            }
            return sb.toString();
        }
    }


    public static class ValueElement extends Element {

        public ValueElement(GroupElement parent) {
            super(parent);
        }
        private boolean isUnit = false;
        private String value = null;

        /** true for 'unit' values like length, angle etc. Simply for those which has no fixed value. */
        public boolean isUnit() {
            return isUnit;
        }

        void setIsUnit(boolean isUnit) {
            this.isUnit = isUnit;
        }

        public String value() {
            return value;
        }

        void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return (isUnit() ? "!" : "") + value() + super.toString(); //NOI18N
        }
    }

    public static class GroupElement extends Element {

        private int index;
        private String referenceName = null;

        private GroupElement(GroupElement parent, int index, String referenceName) {
            this(parent, index);
            this.referenceName = referenceName;
        }

        private GroupElement(GroupElement parent, int index) {
            super(parent);
            this.index = index;
        }
        private List<Element> elements = new ArrayList<Element>(5);
        private boolean list = false;
        private boolean sequence = false;

        /** if true any of the elements can be present in the value otherwise just one of them */
        public boolean isList() {
            return list;
        }

        public boolean isSequence() {
            return this.sequence;
        }

        void setIsList(boolean isList) {
            this.list = isList;
        }

        void setIsSequence(boolean isSequence) {
            this.sequence = isSequence;
        }

        public List<Element> elements() {
            return elements;
        }

        void addElement(Element element) {
            elements.add(element);
        }

        public List<Element> getAllPossibleValues() {
            List<Element> list = new ArrayList<Element>(10);

            if (isSequence()) {
                //sequence
                Element e = elements.get(0); //first element
                if (e instanceof GroupElement) {
                    list.addAll(((GroupElement) e).getAllPossibleValues());
                } else {
                    list.add(e);
                }

            } else {
                //list or set
                for (Element e : elements()) {
                    if (e instanceof GroupElement) {
                        list.addAll(((GroupElement) e).getAllPossibleValues());
                    } else {
                        list.add(e);
                    }
                }
            }
            return list;
        }

        public String toString2(int level) {
            StringBuilder sb = new StringBuilder();
            sb.append(indentString(level) + "[G" + index + " "); //NOI18N
            if (referenceName != null) {
                sb.append("(" + referenceName + ") "); //NOI18N
            }
            if (sequence) {
                sb.append("SEQUENCE"); //NOI18N
            } else {
                if (list) {
                    sb.append("ANY: "); //NOI18N
                } else {
                    sb.append("ONE: "); //NOI18N
                }
            }
            sb.append('\n');
            for (Element e : elements()) {
                sb.append(e.toString2(level + 1));
                sb.append('\n');
            }
            sb.append(indentString(level));
            sb.append(']');
            sb.append(super.toString());
            return sb.toString();
        }

        @Override
        public String toString() {
            return "[G" + index + "]"; //NOI18N
        }
    }
}
