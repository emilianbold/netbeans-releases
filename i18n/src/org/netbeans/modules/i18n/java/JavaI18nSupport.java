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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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


package org.netbeans.modules.i18n.java;


import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.InfoPanel;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.PropertyPanel;
import org.netbeans.modules.i18n.ResourceHolder;
import org.netbeans.modules.i18n.regexp.ParseException;
import org.netbeans.modules.i18n.regexp.Translator;
import org.netbeans.modules.properties.UtilConvert; // PENDING
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.MapFormat;
import org.openide.util.Lookup;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/** 
 * Support for internationalizing strings in java sources.
 *
 * @author Peter Zavadsky
 * @see I18nSupport
 */
public class JavaI18nSupport extends I18nSupport {

    /** Modifiers of field which are going to be internbationalized (default is private static final). */
    protected Set<Modifier> modifiers = EnumSet.of(Modifier.PRIVATE,
                                                   Modifier.STATIC,
                                                   Modifier.FINAL);

    /** Identifier of field element pointing to field which defines resource bundle in the source. */
    protected String identifier;

    /** Generate field? */
    protected boolean generateField;
    
    /** Init string format. */
    protected String initFormat;
    

    /** Constructor. 
     * @see I18nSupport */
    public JavaI18nSupport(DataObject sourceDataObject) {
        super(sourceDataObject);

        initFormat = I18nUtil.getOptions().getInitJavaCode();
    }
    
    
    /** Creates <code>I18nFinder</code>. Implements superclass abstract method. */
    protected I18nFinder createFinder() {
        return new JavaI18nFinder(document);
    }
    
    /** Creates <code>I18nReplacer</code>. Implemens superclass abstract method. */
    protected I18nReplacer createReplacer() {        
        return new JavaI18nReplacer();
    }
    
    /** Creates <code>ResourceHolder</code>. Implemens superclass abstract method. */
    protected ResourceHolder createResourceHolder() {
        return new JavaResourceHolder();
    }
    
    /** Implements superclass abstract method. */
    public I18nString getDefaultI18nString(HardCodedString hcString) {
        I18nString i18nString = new JavaI18nString(this);
        
        final ResourceHolder resourceHolder
                = i18nString.getSupport().getResourceHolder();
        if (resourceHolder.getResource() == null) {
            DataObject lastResource = I18nUtil.getOptions().getLastResource2();
            if (lastResource != null) {
                FileObject sourceFile = sourceDataObject.getPrimaryFile();
                FileObject bundleFile = lastResource.getPrimaryFile();
                ClassPath execClassPath = ClassPath.getClassPath(sourceFile,
                                                                 ClassPath.EXECUTE);
                if (execClassPath.getResourceName(bundleFile) != null) {
                    resourceHolder.setResource(lastResource);
                }
            }
        }

        if (hcString == null) {
            return i18nString;
        }
        
        i18nString.setComment(""); // NOI18N
        String text = decodeUnicodeSeq(hcString.getText());
        i18nString.setKey(text.replace(' ', '_'));
        i18nString.setValue(text);

        // If generation of field is set and replace format doesn't include identifier argument replace it with the default with identifier.
        if (isGenerateField() && i18nString.getReplaceFormat().indexOf("{identifier}") == -1) { // NOI18N
            i18nString.setReplaceFormat(I18nUtil.getReplaceFormatItems().get(0));
        }
        return i18nString;
    }

    private static final String octalDigitChars
                                = "01234567";                           //NOI18N
    private static final String hexaDigitChars
                                = "0123456789abcdefABCDEF";             //NOI18N

    /**
     * Translates Java Unicode sequences (<code>&#x5c;u<i>nnnn</i></code>)
     * to the corresponding characters.
     * @param  text  text with or without Unicode sequences
     * @return  the same text with Unicode sequences replaced with corresponding
     *          characters; may be the same instance as the passed text
     *          if there were no valid Unicode sequences present in it
     * @author  Marian Petras
     */
    private static String decodeUnicodeSeq(String text) {
        final StringBuilder result = new StringBuilder(text.length());
        final char[] chars = text.toCharArray();

        final int stateInitial = 0;
        final int stateBackSlash = 1;
        final int stateUnicode = 2;
        final int stateOctalValue = 3;

        int state = stateInitial;
        int unicodeValue = 0;
        char[] unicodeValueChars = new char[3];
        int valueBytesRead = 0;
        int position;

        int charIndex = 0;
        while (charIndex < chars.length) {
            char c = chars[charIndex++];
            switch (state) {
                case stateInitial:
                    if (c == '\\') {
                        state = stateBackSlash;
                    } else {
                        result.append(c);
                    }
                    break;
                case stateBackSlash:
                    if (c == 'u') {
                        state = stateUnicode;
                    } else if ((c >= '0') && (c <= '3')) {
                        unicodeValue = c - '0';
                        assert (unicodeValue >= 0) && (unicodeValue <= 3);
                        valueBytesRead = 1;
                        state = stateOctalValue;
                    } else {
                        result.append('\\').append(c);
                        state = stateInitial;
                    }
                    break;
                case stateOctalValue:
                    position = octalDigitChars.indexOf(c);
                    if (position >= 0) {
                        unicodeValue = (unicodeValue << 3) | position;
                        valueBytesRead++;
                    } else {
                        charIndex--;    //handle the character in the next round
                    }
                    if ((position < 0) || (valueBytesRead == 3)) {
                        appendChar(result, unicodeValue);
                        state = stateInitial;
                        valueBytesRead = 0;
                        unicodeValue = 0;
                    }
                    break;
                case stateUnicode:
                    position = hexaDigitChars.indexOf(c);
                    if (position >= 0) {
                        if (position > 15) {   //one of [A-F] used
                            position -= 6;     //transform to lowercase
                        }
                        assert position <= 15;
                        unicodeValue = (unicodeValue << 4) | position;
                        if (++valueBytesRead == 4) {
                            appendChar(result, unicodeValue);
                            state = stateInitial;
                        } else {
                            unicodeValueChars[valueBytesRead - 1] = c;
                            /* keep the state at stateUnicode */
                        }
                    } else if (c == 'u') {
                        /*
                         * Handles \\u.... sequences with multiple
                         * 'u' characters, such as \\uuu1234 (which is legal).
                         */

                        /* keep the state at stateUnicode */
                    } else {
                        /* append the malformed Unicode sequence: */
                        result.append('\\');
                        result.append('u');
                        for (int i = 0; i < valueBytesRead; i++) {
                            result.append(unicodeValueChars[i]);
                        }
                        result.append(c);
                        state = stateInitial;
                    }
                    if (state != stateUnicode) {
                        valueBytesRead = 0;
                        unicodeValue = 0;
                    }
                    break;
                default:
                    assert false;
                    throw new IllegalStateException();
            } //switch (state)
        } //for-loop
        switch (state) {
            case stateInitial:
                break;
            case stateBackSlash:
                result.append('\\');
                break;
            case stateOctalValue:
                assert (valueBytesRead >= 0) && (valueBytesRead < 3);
                appendChar(result, unicodeValue);
                break;
            case stateUnicode:
                /* append the incomplete Unicode sequence: */
                assert (valueBytesRead >= 0) && (valueBytesRead < 4);
                result.append('\\').append('u');
                for (int i = 0; i < valueBytesRead; i++) {
                    result.append(unicodeValueChars[i]);
                }
                break;
            default:
                assert false;
                throw new IllegalStateException();
        }

        return result.toString();
    }

    /**
     * Appends a character to the given buffer.
     * 
     * @param  buf  buffer to which a character is to be added
     * @param  unicodeValue  Unicode value of the character to be appended;
     *                       must be in range from {@code 0} to {@code 65535}
     * @return  the passed buffer
     */
    private static final StringBuilder appendChar(StringBuilder buf,
                                                  int unicodeValue)
                throws IllegalArgumentException {
        if ((unicodeValue < 0) || (unicodeValue > 0xffff)) {
            throw new IllegalArgumentException("value out of range: "   //NOI18N
                                               + unicodeValue);
        }

        /* append the Unicode character: */
        if ((unicodeValue >= 0x20) && (unicodeValue != 0x7f)) {
            buf.append((char) unicodeValue);
        } else {
            buf.append('\\');
            switch (unicodeValue) {
                case 0x08:
                    buf.append('b');            //bell
                    break;
                case 0x09:
                    buf.append('t');            //tab
                    break;
                case 0x0a:
                    buf.append('n');            //NL
                    break;
                case 0x0c:
                    buf.append('f');            //FF
                    break;
                case 0x0d:
                    buf.append('r');            //CR
                    break;
                default:
                    buf.append('u');
                    for (int shift = 12; shift >= 0; shift -= 4) {
                        buf.append(hexaDigitChars.charAt(
                                ((unicodeValue >> shift) & 0xf)));
                    }
                    break;
            }
        }

        return buf;
    }
    
    /** Implements <code>I18nSupport</code> superclass abstract method. Gets info panel about found hard string. */
    public JPanel getInfo(HardCodedString hcString) {
        return new JavaInfoPanel(hcString, document);
    }

    /** Getter for identifier. */    
    public String getIdentifier() {
        if ((identifier == null) || (identifier == "")) {               //NOI18N
            createIdentifier();
        }
        return identifier;
    }

    /** Setter for identifier. */    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /** Getter for modifiers. */
    public Set<Modifier> getModifiers() {
        return modifiers;
    }
    
    /** Setter for modifiers. */
    public void setModifiers(Set<Modifier> modifiers) {
        this.modifiers = modifiers;
    }
    
    /** Getter for generate field property.*/
    public boolean isGenerateField() {
        return generateField;
    }
    
    /** Setter for generate field property. */
    public void setGenerateField(boolean generateField) {
        this.generateField = generateField;
    }
    
    /** Getter for init format property. */
    public String getInitFormat() {
        return initFormat;
    }
    
    /** Setter for init format property. */
    public void setInitFormat(String initFormat) {
        this.initFormat = initFormat;
    }

    /** Overrides superclass method. */
    @Override
    public PropertyPanel getPropertyPanel() {
        return new JavaPropertyPanel();
    }
    
    /** Overrides superclass method. 
     * @return true */
    @Override
    public boolean hasAdditionalCustomizer() {
        return true;
    }
    
    /** Overrides superclass method. 
     * @return <code>JavaReplacePanel</code> which offers to customize additional
     * source values (in our case for creating bundle field) */
    @Override
    public JPanel getAdditionalCustomizer() {
        return new JavaReplacePanel(this);
    }

    /** Overrides superclass method. 
     * Actuallay creates bundle field specified by user */
    @Override
    public void performAdditionalChanges() {
        // Creates field.
        createField();
    }

    /** Utility method. Creates identifier for this support instance. */
    public void createIdentifier() {
        String name;
        
        try {
            name = resourceHolder.getResource().getName();
        } catch (NullPointerException npe) {
            identifier = ""; // NOI18N
            return;
        }

        // first letter to lowercase
        if (name.length() > 0) {
            name = name.substring(0, 1).toLowerCase() + name.substring(1);
        } else {
            name = name.toLowerCase();
        }
        
        identifier = name;
    }

    /**
     * Task that adds a field to the internationalized Java source file.
     */
    private final class AddFieldTask implements Task<WorkingCopy> {

        /** name of the field to be added */
        private final String fieldName;

        AddFieldTask(String fieldName) {
            this.fieldName = fieldName;
        }

        public void run(WorkingCopy workingCopy) throws Exception {
            final TypeElement sourceClassElem = getClass(workingCopy);
            if (sourceClassElem == null) {
                return;
            }

            List<? extends javax.lang.model.element.Element> classMembers
                    = sourceClassElem.getEnclosedElements();
            List<? extends VariableElement> fields
                    = ElementFilter.fieldsIn(classMembers);
            if (containsField(fields, fieldName)) {
                return;
            }

            int targetPosition = findTargetPosition(classMembers, fields);

            final TreeMaker treeMaker = workingCopy.getTreeMaker();
            final Elements elements = workingCopy.getElements();
            final Trees trees = workingCopy.getTrees();
            final TreeUtilities treeUtilities = workingCopy.getTreeUtilities();

            TypeElement resourceBundleTypeElem = elements.getTypeElement(
                                            "java.util.ResourceBundle");//NOI18N
            assert resourceBundleTypeElem != null;

            ExpressionTree fieldDefaultValue
                    = treeUtilities.parseVariableInitializer(getInitString(),
                                                             new SourcePositions[1]);
            TreePath classTreePath = trees.getPath(sourceClassElem);
            Scope classScope = trees.getScope(classTreePath);
            if (classScope != null) {
                treeUtilities.attributeTree(fieldDefaultValue, classScope);
            }

            VariableTree field = treeMaker.Variable(
                    treeMaker.Modifiers(modifiers),
                    fieldName,
                    treeMaker.QualIdent(resourceBundleTypeElem),
                    GeneratorUtilities.get(workingCopy).importFQNs(fieldDefaultValue));

            ClassTree oldClassTree = (ClassTree) classTreePath.getLeaf();
            ClassTree newClassTree = (targetPosition != -1) 
                                     ? treeMaker.insertClassMember(oldClassTree, targetPosition, field)
                                     : treeMaker.addClassMember(oldClassTree, field);
            workingCopy.rewrite(oldClassTree, newClassTree);
        }

        /**
         * Finds the target position within the source class element.
         * In the current implementation, the target position is just below
         * the last static field of the class; if there is no static field
         * in the class, the target position is the top of the class.
         * 
         * @param  classMembers  list of all members of the class
         * @param  fields  list of the fields in the class
         * @return  target position ({@code 0}-based) of the field,
         *          or {@code -1} if the field should be added to the end
         *          of the class
         */
        private int findTargetPosition(
                List<? extends javax.lang.model.element.Element> classMembers,
                List<? extends VariableElement> fields) {
            if (fields.isEmpty()) {
                return 0;
            }

            int target = 0;
            boolean skippingStaticFields = false;
            Iterator<? extends javax.lang.model.element.Element> membersIt
                    = classMembers.iterator();
            for (int index = 0; membersIt.hasNext(); index++) {
                javax.lang.model.element.Element member = membersIt.next();
                ElementKind kind = member.getKind();
                if (kind.isField()
                        && (kind != ElementKind.ENUM_CONSTANT)
                        && member.getModifiers().contains(Modifier.STATIC)) {
                    /* it is a static field - skip it! */
                    skippingStaticFields = true;
                } else if (skippingStaticFields) {
                    /* we were skipping all static fields - until now */
                    skippingStaticFields = false;
                    target = index;
                }
            }

            return !skippingStaticFields ? target : -1;
        }

        /**
         * Finds a main top-level class or a nested class element
         * for {@code sourceDataObject} which should be initialized.
         */
        private TypeElement getClass(WorkingCopy workingCopy)
                                                            throws IOException {
            workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);

            final String preferredName = sourceDataObject.getName();
            TypeElement firstPublicNestedClass = null;
            
            List<? extends TypeElement> topClasses = workingCopy.getTopLevelElements();
            for (TypeElement topElement : topClasses) {
                ElementKind elementKind = topElement.getKind();
                if (!elementKind.isClass()) {
                    continue;
                }

                if (topElement.getSimpleName().contentEquals(preferredName)) {
                    return topElement;
                }

                if ((firstPublicNestedClass == null)
                        && topElement.getModifiers().contains(Modifier.PUBLIC)) {
                    firstPublicNestedClass = topElement;
                }
            }

            return firstPublicNestedClass;
        }

        /**
         * Checks whether the given class contains a field of the given name.
         * 
         * @param  clazz  class that should be searched
         * @param  fieldName  name of the field
         * @return  {@code true} if the class contains such a field,
         *          {@code false} otherwise
         */
        private boolean containsField(List<? extends VariableElement> fields,
                                      String fieldName) {
            if (!fields.isEmpty()) {
                for (VariableElement field : fields) {
                    if (field.getSimpleName().contentEquals(fieldName)) {
                        return true;
                    }
                }
            }
            return false;
        }
    
    }
    
    /**
     * Creates a new field which holds a reference to the resource holder
     * (resource bundle). The field is added to the internationalized file.
     */
    private void createField() {
        // Check if we have to generate field.
        if (!isGenerateField()) {
            return;
        }

        final JavaSource javaSource = JavaSource.forDocument(document);
        try {
            ModificationResult result
                    = javaSource.runModificationTask(new AddFieldTask(getIdentifier()));
            result.commit();
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
    }

    /** 
     * Helper method.
     * Gets the string, the piece of code which initializes field resource
     * bundle in the source.
     * E.g.:
     * <pre><code>java.util.ResourceBundle &lt;identifier name&gt;<br />
     *           = <b>java.util.ResourceBundle.getBundle(&quot;&lt;package name&gt;</b>&quot;)</code></pre>
     *
     * @return  String -&gt; piece of initilizing code.
     */
    public String getInitString() {
        String initJavaFormat = getInitFormat();

        // Create map.
        FileObject fo = resourceHolder.getResource().getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);

        
        Map<String,String> map = new HashMap<String,String>(3);

        map.put("bundleNameSlashes", cp.getResourceName(fo, '/', false));//NOI18N
        map.put("bundleNameDots", cp.getResourceName(fo, '.', false));  //NOI18N
        map.put("sourceFileName", (sourceDataObject != null)
                                  ? sourceDataObject.getPrimaryFile().getName()
                                  : "");                                //NOI18N

        return MapFormat.format(initJavaFormat, map);
        
    }
    
    /** Finder which search hard coded strings in java sources. */
    public static class JavaI18nFinder implements I18nFinder {

        /** State when finder is in normal java code. */
        protected static final int STATE_JAVA = 0;
        /** State when finder is at backslash in normal java code. */
        protected static final int STATE_JAVA_A_SLASH = 1;
        /** State when finder is in line comment. */
        protected static final int STATE_LINECOMMENT = 2;
        /** State when finder is in block comment. */
        protected static final int STATE_BLOCKCOMMENT = 3;
        /** State when finder is at star in block commnet. */
        protected static final int STATE_BLOCKCOMMENT_A_STAR = 4;
        /** State when finder is in string found in nornal java code. */
        protected static final int STATE_STRING = 5;
        /** State when finder is at backslash in string. */
        protected static final int STATE_STRING_A_BSLASH = 6;
        /** State when finder is in char in noraml java code. */
        protected static final int STATE_CHAR = 7; // to avoid misinterpreting of '"' resp. '\"' char.

        /** Document on which the search is performed. */
        protected StyledDocument document;

        /** Keeps current state. */
        protected int state;
        
        /** Flag of search type, if it is searched for i18n-ized strings or non-i18n-ized ones. */
        protected boolean i18nSearch;

        /** Keeps position from last search iteration. */
        protected Position lastPosition;

        /** Helper variable for keeping the java string (means pure java code, no coments etc.). */
        protected StringBuffer lastJavaString;

        /** Helper variable. Buffer at which perform search. */
        protected char[] buffer;
        
        /** Helper variable. Actual position of search in buffer. */
        protected int position;
        
        /** Helper variable. Start of actual found hard coded string or -1. */
        protected int currentStringStart;
        
        /** Helper variable. End of actual found hard coded string or -1. */
        protected int currentStringEnd;
        

        /** Constructs finder. */
        public JavaI18nFinder(StyledDocument document) {
            this.document = document;

            init();
        }

        /** Initializes finder. */
        private void init() {
            state = STATE_JAVA;
            initJavaStringBuffer();

            lastPosition = null;
        }
        
        /** Resets finder. */
        protected void reset() {
            init();
        }

        /**
         * Implements <code>I18nFinder</code> interface method.
         * Finds all non-internationalized hard coded strings in source document. */
        public HardCodedString[] findAllHardCodedStrings() {
            reset();
            i18nSearch = false;
            
            return findAllStrings();
        }
        
        /**
         * Implements <code>I18nFinder</code> inetrface method. 
         * Finds hard coded non-internationalized string in buffer.
         * @return next <code>HardCodedString</code> or null if there is no more one.
         */
        public HardCodedString findNextHardCodedString() {
            i18nSearch = false;
            
            return findNextString();
        }
        
        /**
         * Implements <code>I18nFinder</code> interface method.
         * Finds all internationalized hard coded strings in source document. 
         * It's used in test tool. */
        public HardCodedString[] findAllI18nStrings() {
            reset();
            i18nSearch = true;
            
            return findAllStrings();
        }
        
        /**
         * Implements <code>I18nFinder</code> inetrface method. 
         * Finds hard coded internationalized string in buffer. It's used in test tool.
         * @return next <code>HardCodedString</code> or null if there is no more one.
         */
        public HardCodedString findNextI18nString() {
            i18nSearch = true;
            
            return findNextString();
        }
        

        /** Finds all strings according specified regular expression. */
        protected HardCodedString[] findAllStrings() {
            
            List<HardCodedString> list = new ArrayList<HardCodedString>();

            HardCodedString hardString;
            while ((hardString = findNextString()) != null) {
                list.add(hardString);
            }

            return !list.isEmpty()
                   ? list.toArray(new HardCodedString[list.size()])
                   :  null;
        }
        
        /** Finds next string according specified regular expression. */
        protected HardCodedString findNextString() {
            // Reset buffer.
            try {
                buffer = document.getText(0, document.getLength()).toCharArray();
            } catch (BadLocationException ble) {
                if (Boolean.getBoolean("netbeans.debug.exception")) {   //NOI18N
                    ble.printStackTrace();
                }
                return null;
            }

            // Initialize position.
            position = (lastPosition == null) 
                       ? 0
                       : lastPosition.getOffset();

            // Reset hard coded string offsets.
            currentStringStart = -1;
            currentStringEnd = -1;

            // Now serious work.
            while (position < buffer.length) {

                char ch = buffer[position];

                // Other chars than '\n' (new line).
                if (ch != '\n') {
                    HardCodedString foundHardString = handleCharacter(ch);
                    if (foundHardString != null) {
                        return foundHardString;
                    }
                } else {
                    handleNewLineCharacter();
                }
                position++;

            } // End of while.

            // Indicate end was reached and nothing found.
            return null;
        }

        /** Handles state changes according next charcter. */
        protected HardCodedString handleCharacter(char character) {
            if (state == STATE_JAVA) {
                return handleStateJava(character);
            } else if (state == STATE_JAVA_A_SLASH) {
                return handleStateJavaASlash(character);
            } else if (state == STATE_CHAR) {
                return handleStateChar(character);
            } else if (state == STATE_STRING_A_BSLASH) {
                return handleStateStringABSlash(character);
            } else if (state == STATE_LINECOMMENT) {
                return handleStateLineComment(character);
            } else if (state == STATE_BLOCKCOMMENT) {
                return handleStateBlockComment(character);
            } else if (state == STATE_BLOCKCOMMENT_A_STAR) {
                return handleStateBlockCommentAStar(character);
            } else if (state == STATE_STRING) {
                return handleStateString(character);
            }
            
            return null;            
        }

        /** Handles state when new line '\n' char occures. */
        protected void handleNewLineCharacter() {
            // New line char '\n' -> reset the state.
            if (state == STATE_JAVA 
                    || state == STATE_JAVA_A_SLASH
                    || state == STATE_CHAR
                    || state == STATE_LINECOMMENT
                    || state == STATE_STRING
                    || state == STATE_STRING_A_BSLASH) {
                initJavaStringBuffer();
                currentStringStart = -1;
                currentStringEnd = -1;
                state = STATE_JAVA;
            } else if (state == STATE_BLOCKCOMMENT
                       || state == STATE_BLOCKCOMMENT_A_STAR) {
                state = STATE_BLOCKCOMMENT;
            }
        }
        
        
        /** Handles state <code>STATE_JAVA</code>.
         * @param character char to proceede 
         * @return <code>HardCodedString</code> or null if not found yet */
        protected HardCodedString handleStateJava(char character) {
            lastJavaString.append(character);
            if (character == '/') {
                state = STATE_JAVA_A_SLASH;
            } else if (character == '"') {
                state = STATE_STRING;
                if (currentStringStart == -1) {
                    // Found start of hard coded string.
                    currentStringStart = position;
                }
            } else if (character == '\'') {
                state = STATE_CHAR;
            }
            
            return null;
        }

        /** Handles state <code>STATE_JAVA_A_SLASH</code>.
         * @param character char to proceede 
         * @return <code>HardCodedString</code> or null if not found yet */
        protected HardCodedString handleStateJavaASlash(char character) {
            lastJavaString.append(character);
            if (character == '/') {
                state = STATE_LINECOMMENT;
            } else if(character == '*') {
                state = STATE_BLOCKCOMMENT;
            }
            
            return null;
        }

        /** Handles state <code>STATE_CHAR</code>.
         * @param character char to proceede 
         * @return <code>HardCodedString</code> or null if not found yet */
        protected HardCodedString handleStateChar(char character) {
            lastJavaString.append(character);

            if (character == '\'') {
                state = STATE_JAVA;
            }
            
            return null;
        }

        /** Handles state <code>STATE_STRING_A_BSLASH</code>.
         * @param character char to proceede 
         * @return <code>HardCodedString</code> or null if not found yet */
        protected HardCodedString handleStateStringABSlash(char character) {
            state = STATE_STRING;
            
            return null;
        }

        /** Handles state <code>STATE_LINECOMMENT</code>.
         * @param character char to proceede 
         * @return null */
        protected HardCodedString handleStateLineComment(char character) {
            return null;
        }
        
        /** Handles state <code>STATE_BLOCKCOMMENT</code>.
         * @param character char to proceede 
         * @return <code>HardCodedString</code> or null if not found yet */
        protected HardCodedString handleStateBlockComment(char character) {
            if (character == '*') {
                state = STATE_BLOCKCOMMENT_A_STAR;
            }
            
            return null;
        }

        /** Handles state <code>STATE_BLOCKCOMMENT_A_STAR</code>.
         * @param character char to proceede 
         * @return <code>HardCodedString</code> or null if not found yet */
        protected HardCodedString handleStateBlockCommentAStar(char character) {
            if (character == '/') {
                state = STATE_JAVA;
                initJavaStringBuffer();
            } else if (character != '*') {
                state = STATE_BLOCKCOMMENT;
            }
            
            return null;
        }

        /** Handles state <code>STATE_STRING</code>.
         * @param character char to proceede 
         * @return <code>HardCodedString</code> or null if not found yet */
        protected HardCodedString handleStateString(char character) {
            if (character == '\\') {
                state = STATE_STRING_A_BSLASH;
            } else if (character == '"') {
                state = STATE_JAVA;

                if ((currentStringEnd == -1) && (currentStringStart != -1)) {
                    // Found end of hard coded string.
                    currentStringEnd = position + 1;

                    int foundStringLength = currentStringEnd - currentStringStart;

                    try {
                        // Get hard coded string.
                        Position hardStringStart = document.createPosition(currentStringStart);
                        Position hardStringEnd   = document.createPosition(currentStringEnd);

                        String hardString = document.getText(hardStringStart.getOffset(),
                                                             foundStringLength);

                        // Retrieve offset of the end of line where was found hard coded string.
                        String restBuffer = new String(buffer,
                                                       currentStringEnd,
                                                       buffer.length - currentStringEnd);
                        int endOfLine = restBuffer.indexOf('\n');
                        if (endOfLine == -1) {
                            endOfLine = restBuffer.length();
                        }

                        lastJavaString.append(document.getText(currentStringStart + 1,
                                                               hardString.length()));

                        // Get the rest of line.
                        String restOfLine = document.getText(currentStringStart + 1 + hardString.length(),
                                                             currentStringEnd + endOfLine - currentStringStart - hardString.length());

                        // Replace rest of occurences of \" to cheat out regular expression for very minor case when the same string is after our at the same line.
                        lastJavaString.append(restOfLine.replace('\"', '_'));

                        // If not matches regular expression -> is not internationalized.
                        if (isSearchedString(lastJavaString.toString(), hardString)) {
                            lastPosition = hardStringEnd;

                            // Search was successful -> return.
                            return new HardCodedString(extractString(hardString),
                                                       hardStringStart,
                                                       hardStringEnd);
                        }
                    } catch (BadLocationException ble) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                                         ble);
                    } finally {
                        currentStringStart = -1;
                        currentStringEnd = -1;

                        initJavaStringBuffer();
                    }
                }
            }
            
            return null;
        }
        
        /** Resets <code>lastJavaString</code> variable.
         * @see #lastJavaString*/
        private void initJavaStringBuffer() {
            lastJavaString = new StringBuffer();
        }

        /** Helper utility method. */
        private String extractString(String sourceString) {
            if (sourceString == null) {
                return "";                                              //NOI18N
            }

            if ((sourceString.length() >= 2) &&
                    (sourceString.charAt(0) == '"') &&
                    (sourceString.charAt(sourceString.length() - 1) == '"')) {
                sourceString = sourceString.substring(1, sourceString.length() - 1);
            }
            return sourceString;
        }

        /** 
         * Help method for decision if found hard coded string is searched string. It means
         * if it is i18n-zed or non-internationalized (depending on <code>i18nSearch</code> flag. 
         * <p>
         * The part of line 
         * (starts after previous found hard coded string) with current found hard code string is compared
         * against regular expression which can user specify via i18n options. If the compared line matches 
         * that regular expression the hard coded string is considered as internationalized.
         *
         * @param partHardLine line of code which includes hard coded string and starts from beginning or
         * the end of previous hard coded string.
         * @param hardString found hard code string
         * @return <code>true<code> if string is internationalized and <code>i18nSearch</code> flag is <code>true</code>
         *   or if if string is non-internationalized and <code>i18nSearch</code> flag is <code>false</code> */
        protected boolean isSearchedString(String partHardLine, String hardString) {
            String lineToMatch = UtilConvert.unicodesToChars(partHardLine);
            
            Exception ex = null;
            try {
                String regexp = createRegularExpression(hardString);
                return Pattern.compile(regexp).matcher(lineToMatch).find()
                       == i18nSearch;
            } catch (ParseException ex1) {
                ex = ex1;
            } catch (PatternSyntaxException ex2) {
                ex = ex2;
            }

            /*
             * Handle the situation that some syntax error has been detected:
             */
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);

            // Indicate error, but allow user what to do with the found hard coded string to be able go thru
            // this problem.
            // Note: All this shouldn't happen. The reason is 1) bad set reg exp format (in options) or 
            // 2) it's error in this code.
            String msg = NbBundle.getMessage(JavaI18nSupport.class, 
                                             "MSG_RegExpCompileError",  //NOI18N
                                             hardString);
            
            Object answer = DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Confirmation(
                            msg,
                            NotifyDescriptor.YES_NO_OPTION, 
                            NotifyDescriptor.ERROR_MESSAGE));
            return NotifyDescriptor.YES_OPTION.equals(answer);
        }

         /**
          * Creates a regular expression matching the pattern specified in the
          * module options.
          * The pattern specified in the options contains a special token
          * <code>{hardString}</code>. This token is replaced with a regular
          * expression matching exactly the string passed as a parameter
          * and a result of this substitution is returned.
          *
          * @param  hardString  hard-coded string whose regexp-equivalent is
          *                     to be put in place of token
          *                     <code>{hardString}</code>
          * @return  regular expression matching the pattern specified
          *          in the module options
          */
        private String createRegularExpression(String hardString)
                throws ParseException {
            String regexpForm;
            if (i18nSearch) {
                regexpForm = I18nUtil.getOptions().getI18nRegularExpression();
            } else {
                regexpForm = I18nUtil.getOptions().getRegularExpression();
            }

            /*
             * Translate the regexp form to the JDK's java.util.regex syntax
             * and replace tokens "{key}" and "{hardString}" with the passed
             * hard-coded string.
             */
            Map<String,String> map = new HashMap<String,String>(3);
            map.put("key", hardString);  //older form of regexp format  //NOI18N
            map.put("hardString", hardString);                          //NOI18N
            return Translator.translateRegexp(regexpForm, map);
        }

    } // End of JavaI18nFinder nested class.
    
    
    /** Replacer for java sources used by enclosing class. */
    public static class JavaI18nReplacer implements I18nReplacer {
        
        /** Constructor.*/
        public JavaI18nReplacer() {
        }
        

        /** Replaces found hard coded string in source. 
         * @param hcString found hard coded string to-be replaced 
         * @param rbString holds replacing values */
        public void replace(final HardCodedString hcString,
                            final I18nString i18nString) {
            if (!(i18nString instanceof JavaI18nString)) {
                throw new IllegalArgumentException(
                        "I18N module: i18nString have to be an instance of JavaI18nString.");//NOI18N
            }
            
            final String newCode = i18nString.getReplaceString();

            final StyledDocument document = i18nString.getSupport().getDocument();
            
            // Call runAtomic method to break guarded flag if it is necessary. (For non-guarded works as well).
            NbDocument.runAtomic(
            document,
            new Runnable() {
                public void run() {
                    try {
                        if (hcString.getLength() > 0) {
                            document.remove(hcString.getStartPosition().getOffset(),
                                            hcString.getLength());
                        }
                        if (newCode != null && newCode.length() > 0) {
                            document.insertString(hcString.getEndPosition().getOffset(),
                                                  newCode, null);
                        }
                    } catch (BadLocationException ble) {
                        NotifyDescriptor.Message message
                                = new NotifyDescriptor.Message(
                                        NbBundle.getMessage(JavaI18nSupport.class,
                                                            "MSG_CouldNotReplace"),//NOI18N
                                        NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(message);
                    }
                }
            });
        }
    } // End of nested class JavaI18nReplacer.

    
    /** Panel for showing info about hard coded string. */
    private static class JavaInfoPanel extends InfoPanel {
        
        /** Constructor. */
        public JavaInfoPanel(HardCodedString hcString, StyledDocument document) {
            super(hcString, document);
        }
        
        /** Implements superclass abstract method. */
        protected void setHardCodedString(HardCodedString hcString, StyledDocument document) {

            getStringText().setText(hcString == null ? ""               //NOI18N
                                                     : hcString.getText());
            
            int pos;

            String hardLine;
            
            if (hcString.getStartPosition() == null) {
                hardLine = "";                                          //NOI18N
            } else {
                pos = hcString.getStartPosition().getOffset();

                try {
                    Element paragraph = document.getParagraphElement(pos);
                    hardLine = document.getText(paragraph.getStartOffset(),
                                                paragraph.getEndOffset() - paragraph.getStartOffset())
                                       .trim();
                } catch (BadLocationException ble) {
                    hardLine = ""; // NOI18N
                }
            }

            getFoundInText().setText(hardLine);
            
            remove(getComponentLabel());
            remove(getComponentText());
            remove(getPropertyLabel());
            remove(getPropertyText());
        }
    } // End of JavaInfoPanel inner class.
    
    
    /** Factory for {@code JavaI18nSupport}. */
    public static class Factory extends I18nSupport.Factory {
        
        /** Implements interface. */
        public I18nSupport createI18nSupport(DataObject dataObject) {
            return new JavaI18nSupport(dataObject);
        }

        /** Gets class of supported <code>DataObject</code>.
         * @return <code>JavaDataObject</code> class or <code>null</code> 
         * if java module is not available */
        public Class getDataObjectClass() {
            // XXX Cleaner should be this code dependend on java module
            // -> I18n API needed.
            try {
                return Class.forName(
                    "org.netbeans.modules.java.JavaDataObject", // NOI18N
                    false,
                    Lookup.getDefault().lookup(ClassLoader.class));
            } catch (ClassNotFoundException cnfe) {
                return null;
            }
        }
    } // End of class Factory.
}
