/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n.java;


import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import org.apache.regexp.CharacterIterator;
import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.StringCharacterIterator;

import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.InfoPanel;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.PropertyPanel;
import org.netbeans.modules.i18n.ResourceHolder;

import org.openide.cookies.SourceCookie;
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.src.ClassElement;
import org.openide.src.FieldElement;
import org.openide.src.Identifier;
import org.openide.src.SourceElement;
import org.openide.src.SourceException;
import org.openide.src.Type;
import org.openide.text.NbDocument;
import org.openide.TopManager;
import org.openide.util.MapFormat;


/** 
 * Support for internationalizing strings in java sources.
 *
 * @author Peter Zavadsky
 * @see I18nSupport
 */
public class JavaI18nSupport extends I18nSupport {

    /** Modifiers of field which are going to be internbationalized (default is private static final). */
    protected int modifiers = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;

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
        
        if(i18nString.getSupport().getResourceHolder().getResource() == null && I18nUtil.getOptions().getLastResource() != null)
            i18nString.getSupport().getResourceHolder().setResource(I18nUtil.getOptions().getLastResource());

        if(hcString == null)
            return i18nString;
        
        i18nString.setComment(""); // NOI18N
        i18nString.setKey(hcString.getText().replace(' ', '_' ));
        i18nString.setValue(hcString.getText());
        
        // If generation of field is set and replace format doesn't include identifier argument replace it with the default with identifier.
        if(isGenerateField() && i18nString.getReplaceFormat().indexOf("{identifier}") == -1) // NOI18N
            i18nString.setReplaceFormat((String)I18nUtil.getReplaceFormatItems().get(0));
        
        return i18nString;
    }
    
    /** Implements <code>I18nSupport</code> superclass abstract method. Gets info panel about found hard string. */
    public JPanel getInfo(HardCodedString hcString) {
        return new JavaInfoPanel(hcString, document);
    }

    /** Getter for identifier. */    
    public String getIdentifier() {
        if(identifier == null || identifier == "") // NOI18N
            createIdentifier();
        
        return identifier;
    }

    /** Setter for identifier. */    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /** Getter for modifiers. */
    public int getModifiers() {
        return modifiers;
    }
    
    /** Setter for modifiers. */
    public void setModifiers(int modifiers) {
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
    public PropertyPanel getPropertyPanel() {
        return new JavaPropertyPanel();
    }
    
    /** Overrides superclass method. 
     * @return true */
    public boolean hasAdditionalCustomizer() {
        return true;
    }
    
    /** Overrides superclass method. 
     * @return <code>JavaReplacePanel</code> which offers to customize additional
     * source values (in our case for creating bundle field) */
    public JPanel getAdditionalCustomizer() {
        return new JavaReplacePanel(this);
    }

    /** Overrides superclass method. 
     * Actuallay creates bundle field specified by user */
    public void performAdditionalChanges() {
        // Creates field.
        createField();
    }

    /** Utility method. Creates identifier for this support instance. */
    public void createIdentifier() {
        String name;
        
        try {
            name = resourceHolder.getResource().getName();
        } catch(NullPointerException npe) {
            identifier = ""; // NOI18N
            return;
        }

        // first letter to lowercase
        if(name.length() > 0) {
            name = name.substring(0,1).toLowerCase() + name.substring(1);
        } else {
            name = name.toLowerCase();
        }
        
        identifier = name;
    }
    
    /** Helper method. Creates a new field in java source hierarchy. 
     * @param javaI18nSupport which holds info about going-to-be created field element
     * @param sourceDataObject object to which source will be new field added,
     * the object have to have <code>SourceCookie</code>
     * @see org.openide.cookies.SourceCookie */
    private void createField() {
        // Check if we have to generate field.
        if(!isGenerateField())
            return;

        ClassElement sourceClass = getSourceClassElement();

        if(sourceClass.getField(Identifier.create(getIdentifier())) != null)
            // Field with such identifer exsit already, do nothing.
            return;
        
        try {
            FieldElement newField = new FieldElement();
            newField.setName(Identifier.create(getIdentifier()));
            newField.setModifiers(getModifiers());
            newField.setType(Type.parse("java.util.ResourceBundle")); // NOI18N
            newField.setInitValue(getInitString());
            
            if(sourceClass != null)
                // Trying to add new field.
                sourceClass.addField(newField);
        } catch(SourceException se) {
            // do nothing, means the field already exist
            if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                se.printStackTrace();
        } catch(NullPointerException npe) {
            // something wrong happened, probably sourceDataObject was not initialized
            if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                npe.printStackTrace();
        }
    }

    /** 
     * Helper method. Gets the string, the piece of code which initializes field resource bundle in the source. E.g.:
     * <p>
     * java.util.ResourceBundle <identifier name> = <b>java.util.ResourceBundle.getBundle("<package name></b>")
     * @return String -> piece of initilizing code. */
    public String getInitString() {
        String initJavaFormat = getInitFormat();

        // Create map.
        Map map = new HashMap(3);

        map.put("bundleNameSlashes", resourceHolder.getResource().getPrimaryFile().getPackageName('/')); // NOI18N
        map.put("bundleNameDots", resourceHolder.getResource().getPrimaryFile().getPackageName('.')); // NOI18N
        map.put("sourceFileName", sourceDataObject == null ? "" : sourceDataObject.getPrimaryFile().getName()); // NOI18N

        return MapFormat.format(initJavaFormat, map);
    }
    
    /** Helper method. Finds main top-level class element for <code>sourceDataObject</code> which should be initialized. */
    private ClassElement getSourceClassElement() {
        SourceElement sourceElem = ((SourceCookie)sourceDataObject.getCookie(SourceCookie.class)).getSource();
        ClassElement sourceClass = sourceElem.getClass(Identifier.create(sourceDataObject.getName()));
        
        if(sourceClass != null)
            return sourceClass;
        
        ClassElement[] classes = sourceElem.getClasses();
        
        // find source class
        for(int i=0; i<classes.length; i++) {
            int modifs = classes[i].getModifiers();
            if(classes[i].isClass() && Modifier.isPublic(modifs)) {
                sourceClass = classes[i];
                break;
            }
        }
        
        return sourceClass;
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
        
        /** Helper variable for regular expression program. */
        private final RECompiler rec = new RECompiler();


        /** Constructs finder. */
        public JavaI18nFinder(StyledDocument document) {
            this.document = document;

            initialize();
        }

        /** Initializes finder. */
        protected void initialize() {
            state = STATE_JAVA;
            initJavaStringBuffer();

            lastPosition = null;
        }

        /**
         * Implements <code>I18nFinder</code> interface method.
         * Finds all non-internationalized hard coded strings in source document. */
        public HardCodedString[] findAllHardCodedStrings() {
            initialize();
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
         * Finds all internationalized hard coded strings in source document. It's used in text tool. */
        public HardCodedString[] findAllI18nStrings() {
            initialize();
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
            
            ArrayList list = new ArrayList();

            HardCodedString hardString;

            for(;(hardString = findNextString())!= null;) {
                list.add(hardString);
            }

            if(list.isEmpty())
                return null;
            else {
                HardCodedString[] hardStrings = new HardCodedString[list.size()];
                list.toArray(hardStrings);
                return hardStrings;
            }
        }
        
        /** Finds next string according specified regular expression. */
        protected HardCodedString findNextString() {
            // Reset buffer.
            try {
                buffer = document.getText(0, document.getLength()).toCharArray();
            } catch(BadLocationException ble) {
                if(Boolean.getBoolean("netbeans.debug.exception")) // NOI18N
                    ble.printStackTrace();
                
                return null;
            }

            // Initialize position.
            if(lastPosition == null) 
                position = 0;
            else
                position = lastPosition.getOffset();

            // Reset hard coded string offsets.
            currentStringStart = -1;
            currentStringEnd = -1;

            // Now serious work.
            while(position < buffer.length) {

                char ch = buffer[position];

                // Other chars than '\n' (new line).
                if(ch != '\n') {
                    HardCodedString foundHardString = handleCharacter(ch);
                    
                    if(foundHardString != null)
                        return foundHardString;
                    
                } else
                    handleNewLineCharacter();

                position++;

            } // End of while.

            // Indicate end was reached and nothing found.
            return null;
        }

        /** Handles state changes according next charcter. */
        protected HardCodedString handleCharacter(char character) {
            if(state == STATE_JAVA) {
                return handleStateJava(character);
            } else if(state == STATE_JAVA_A_SLASH) {
                return handleStateJavaASlash(character);
            } else if(state == STATE_CHAR) {
                return handleStateChar(character);
            } else if(state == STATE_STRING_A_BSLASH) {
                return handleStateStringABSlash(character);
            } else if(state == STATE_LINECOMMENT) {
                return handleStateLineComment(character);
            } else if(state == STATE_BLOCKCOMMENT) {
                return handleStateBlockComment(character);
            } else if(state == STATE_BLOCKCOMMENT_A_STAR) {
                return handleStateBlockCommentAStar(character);
            } else if(state == STATE_STRING) {
                return handleStateString(character);
            }
            
            return null;            
        }

        /** Handles state when new line '\n' char occures. */
        protected void handleNewLineCharacter() {
            // New line char '\n' -> reset the state.
            if(state == STATE_JAVA 
                || state == STATE_JAVA_A_SLASH
                || state == STATE_CHAR
                || state == STATE_LINECOMMENT
                || state == STATE_STRING
                || state == STATE_STRING_A_BSLASH) {
                    initJavaStringBuffer();
                    currentStringStart = -1;
                    currentStringEnd = -1;
                    state = STATE_JAVA;
            } else if(state == STATE_BLOCKCOMMENT
                || state == STATE_BLOCKCOMMENT_A_STAR) {
                    state = STATE_BLOCKCOMMENT;
            }
        }
        
        
        /** Handles state <code>STATE_JAVA</code>.
         * @param character char to proceede 
         * @return <code>HardCodedString</code> or null if not found yet */
        protected HardCodedString handleStateJava(char character) {
            lastJavaString.append(character);
            if(character == '/') {
                state = STATE_JAVA_A_SLASH;
            } else if(character == '"') {
                state = STATE_STRING;
                if(currentStringStart == -1)
                    // Found start of hard coded string.
                    currentStringStart = position;
            } else if(character == '\'') {
                state = STATE_CHAR;
            }
            
            return null;
        }

        /** Handles state <code>STATE_JAVA_A_SLASH</code>.
         * @param character char to proceede 
         * @return <code>HardCodedString</code> or null if not found yet */
        protected HardCodedString handleStateJavaASlash(char character) {
            lastJavaString.append(character);
            if(character == '/') {
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

            if(character == '\'') {
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
            if(character == '*') {
                state = STATE_BLOCKCOMMENT_A_STAR;
            }
            
            return null;
        }

        /** Handles state <code>STATE_BLOCKCOMMENT_A_STAR</code>.
         * @param character char to proceede 
         * @return <code>HardCodedString</code> or null if not found yet */
        protected HardCodedString handleStateBlockCommentAStar(char character) {
            if(character == '/') {
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
            if(character == '\\') {
                state = STATE_STRING_A_BSLASH;
            } else if(character == '"') {
                state = STATE_JAVA;

                if((currentStringEnd == -1) && (currentStringStart != -1)) {
                    // Found end of hard coded string.
                    currentStringEnd = position + 1;

                    int foundStringLength = currentStringEnd - currentStringStart;

                    try {
                        // Get hard coded string.
                        Position hardStringStart = document.createPosition(currentStringStart);
                        Position hardStringEnd   = document.createPosition(currentStringEnd);

                        String hardString = document.getText(hardStringStart.getOffset(), foundStringLength);

                        // Retrieve offset of the end of line where was found hard coded string.
                        String restBuffer = new String(buffer, currentStringEnd, buffer.length-currentStringEnd);
                        int endOfLine = restBuffer.indexOf('\n');
                        if(endOfLine == -1)
                            endOfLine = restBuffer.length();

                        lastJavaString.append(document.getText(currentStringStart+1, hardString.length()));

                        // Get the rest of line.
                        String restOfLine = document.getText(currentStringStart+1+hardString.length(), currentStringEnd+endOfLine-currentStringStart-hardString.length());

                        // Replace rest of occurences of \" to cheat out regular expression for very minor case when the same string is after our at the same line.
                        lastJavaString.append(restOfLine.replace('\"', '_'));

                        // If not matches regular expression -> is not internationalized.
                        if(isSearchedString(lastJavaString.toString(), hardString) ) {
                            lastPosition = hardStringEnd;

                            // Search was successful -> return.
                            return new HardCodedString(extractString(hardString), hardStringStart, hardStringEnd);
                        }
                    } catch(BadLocationException ble) {
                        if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                            ble.printStackTrace();
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
            if(sourceString == null)
                return ""; // NOI18N

            if ((sourceString.length() >= 2) &&
            (sourceString.charAt(0) == '"') &&
            (sourceString.charAt(sourceString.length() - 1) == '"'))
                sourceString = sourceString.substring(1, sourceString.length() - 1);
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
         * @return true if string is non-internationalized */
        protected boolean isSearchedString(String partHardLine, String hardString) {
            String regExp = createRegularExpression(hardString);
            
            REProgram rep;

            try {
                rep = rec.compile(regExp);
            } catch(RESyntaxException e) {
                if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    e.printStackTrace();

                // Indicate error, but allow user what to do with the found hard coded string to be able go thru
                // this propblem.
                // Note: All this shouldn't happen. The reason is 1) bad set reg exp format (in options) or 
                // 2) it's error in this code.
                String msg = MessageFormat.format(
                    I18nUtil.getBundle().getString("MSG_RegExpCompileError"),
                    new Object[] {hardString}
                );
                
                NotifyDescriptor.Confirmation confirmation = new NotifyDescriptor.Confirmation(msg,
                    NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.ERROR_MESSAGE);
                
                if(NotifyDescriptor.YES_OPTION.equals(TopManager.getDefault().notify(confirmation)))
                    return true;
                else
                    return false;
            }

            RE re = new RE(rep, RE.MATCH_MULTILINE);
            CharacterIterator chi = new StringCharacterIterator(partHardLine);

            if(re.match(chi, 0))
                return i18nSearch ? true : false;

            return i18nSearch ? false : true;
        }

        /** Creates regular expression. Helper method.
         * @param hardString found hard coded string used by reg exp creation */
        private String createRegularExpression(String hardString) {
            // It's necessary to escape parenthises in hardString so reg exp will be compiled.
            hardString = escapeMetaCharacters(hardString);
            
            Map map = new HashMap(2);
            map.put("key", hardString); // Older form of reg exp format. // NOI18N
            map.put("hardString", hardString); // NOI18N

            return MapFormat.format(
                i18nSearch ? I18nUtil.getOptions().getI18nRegularExpression() : I18nUtil.getOptions().getRegularExpression(),
                map
            );
        }

        /** Escapes some reg exp meta charcters.
         * @param source source string
         * @return string with escaped paranthases and ? char or null if source is null */
        private static String escapeMetaCharacters(String source) {
            if(source == null) return null;
            StringBuffer result = new StringBuffer();
            for (int i=0; i<source.length(); i++) {
                char ch = source.charAt(i);
                
                if(ch == '(' 
                    || ch == ')'
                    || ch == '{'
                    || ch == '}'
                    || ch == '['
                    || ch == ']'
                    || ch == '?') {
                    
                    result.append('\\');
                }
                
                result.append(ch);
            }
            return result.toString();
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
        public void replace(final HardCodedString hcString, final I18nString i18nString) {
            if(!(i18nString instanceof JavaI18nString))
                throw new IllegalArgumentException("I18N module: i18nString have to be an instance of JavaI18nString."); // NOI18N
            
            final String newCode = i18nString.getReplaceString();

            final StyledDocument document = i18nString.getSupport().getDocument();
            
            // Call runAtomic method to break guarded flag if it is necessary. (For non-guarded works as well).
            NbDocument.runAtomic(
            document,
            new Runnable() {
                public void run() {
                    try {
                        if(hcString.getLength() > 0) {
                            document.remove(hcString.getStartPosition().getOffset(), hcString.getLength());
                        }
                        if(newCode != null && newCode.length() > 0) {
                            document.insertString(hcString.getEndPosition().getOffset(), newCode, null);
                        }
                    } catch(BadLocationException ble) {
                        NotifyDescriptor.Message message = new NotifyDescriptor.Message(
                            I18nUtil.getBundle().getString("MSG_CouldNotReplace"), NotifyDescriptor.ERROR_MESSAGE);
                        TopManager.getDefault().notify(message);
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

            getStringText().setText(hcString == null ? "" : hcString.getText()); // NOI18N
            
            int pos;

            String hardLine;
            
            if(hcString.getStartPosition() == null)
                hardLine = ""; // NOI18N
            else {
                pos = hcString.getStartPosition().getOffset();

                try {
                    Element paragraph = document.getParagraphElement(pos);
                    hardLine = document.getText(paragraph.getStartOffset(), paragraph.getEndOffset()-paragraph.getStartOffset()).trim();
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
    
    
    /** Factory for <code>JavaI18nSupport</code>. */
    public static class Factory extends I18nSupport.Factory {
        
        /** Implements interface. */
        public I18nSupport createI18nSupport(DataObject dataObject) {
            return new JavaI18nSupport(dataObject);
        }
    }
}
