/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

// XXX: consider adding getInitialComment() and setInitialComment() methods
// (useful e.g. for GeneratedFilesHelper)

/**
 * Similar to {@link Properties} but designed to retain additional
 * information needed for safe hand-editing.
 * Useful for various <samp>*.properties</samp> in a project:
 * <ol>
 * <li>Can associate comments with particular entries.
 * <li>Order of entries preserved during modifications whenever possible.
 * <li>VCS-friendly: lines which are not semantically modified are not textually modified.
 * <li>Can automatically insert line breaks in new or modified values at positions
 *     that are likely to be semantically meaningful, e.g. between path components
 * </ol>
 * The file format (including encoding etc.) is compatible with the regular JRE implementation.
 * Only (non-null) String is supported for keys and values.
 * This class is not thread-safe; use only from a single thread, or use {@link Collections#synchronizedMap}.
 * @author Jesse Glick, David Konecny
 */
public final class EditableProperties extends AbstractMap/*<String,String>*/ implements Cloneable {
    
    /** List of Item instances as read from the properties file. Order is important.
     * Saving properties will save then in this order. */
    private List/*<Item>*/ items;

    /** Map of [property key, Item instance] for faster access. */
    private Map itemIndex;

    private boolean alphabetize = true;
    
    private static final String keyValueSeparators = "=: \t\r\n\f";

    private static final String strictKeyValueSeparators = "=:";

    private static final String whiteSpaceChars = " \t\r\n\f";

    private static final String commentChars = "#!";
    
    private static final String INDENT = "    ";

    // parse states:
    private static final int WAITING_FOR_KEY_VALUE = 1;
    private static final int READING_KEY_VALUE = 2;
    
    /**
     * Creates empty instance which items will not be sorted by default.
     */
    public EditableProperties() {
        items = new ArrayList();
        itemIndex = new HashMap();
    }

    /**
     * Creates empty instance.
     * @param alphabetize alphabetize new items according to key or not
     */
    public EditableProperties(boolean alphabetize) {
        this();
        this.alphabetize = alphabetize;
    }
    
    /**
     * Creates instance from an existing map. No comments will be defined.
     * Any order from the existing map will be retained.
     * @param map a map from String to String
     */
    public EditableProperties(Map map) {
        this();
        putAll(map);
    }
    
    /**
     * Creates new instance from an existing one.
     * @param ep an instance of EditableProperties
     */
    EditableProperties(EditableProperties ep) {
        this();
        Iterator it = ep.items.iterator();
        while (it.hasNext()) {
            Item item = (Item)it.next();
            addItem((Item)item.clone(), false);
        }
    }
    
    /**
     * Returns a set view of the mappings ordered according to their file 
     * position.  Each element in this set is a Map.Entry. See
     * {@link AbstractMap#entrySet} for more dertails.
     * @return set with Map.Entry instances.
     */
    public Set entrySet() {
        return new SetImpl(this);
    }
    
    /**
     * Load properties from a stream.
     * @param stream an input stream
     * @throws IOException if the contents are malformed or the stream could not be read
     */
    public void load(InputStream stream) throws IOException {
        int state = WAITING_FOR_KEY_VALUE;
        BufferedReader input = new BufferedReader(new InputStreamReader(stream, "ISO-8859-1"));
        LinkedList tempList = new LinkedList();
        String line;
        int commentLinesCount = 0;
        // Read block of lines and create instance of Item for each.
        // Separator is: either empty line or valid end of proeprty declaration
        while (null != (line = input.readLine())) {
            tempList.add(line);
            boolean empty = isEmpty(line);
            boolean comment = isComment(line);
            if (state == WAITING_FOR_KEY_VALUE) {
                if (empty) {
                    // empty line: create Item without any key
                    createNonKeyItem(tempList);
                    commentLinesCount = 0;
                } else {
                    if (comment) {
                        commentLinesCount++;
                    } else {
                        state = READING_KEY_VALUE;
                    }
                }
            }
            if (state == READING_KEY_VALUE && !isContinue(line)) {
                // valid end of property declaration: create Item for it
                createKeyItem(tempList, commentLinesCount);
                state = WAITING_FOR_KEY_VALUE;
                commentLinesCount = 0;
            }
        }
        if (tempList.size() > 0) {
            if (state == READING_KEY_VALUE) {
                // value was not ended correctly? ignore.
                createKeyItem(tempList, commentLinesCount);
            } else {
                createNonKeyItem(tempList);
            }
        }
    }

    /**
     * Store properties to a stream.
     * @param stream an output stream
     * @throws IOException if the stream could not be written to
     */
    public void store(OutputStream stream) throws IOException {
        boolean previousLineWasEmpty = true;
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(stream, "ISO-8859-1"));
        Iterator it = items.iterator();
        while (it.hasNext()) {
            Item item = (Item)it.next();
            if (item.isSeparate() && !previousLineWasEmpty) {
                output.newLine();
            }
            Iterator it2 = item.getRawData().iterator();
            String line = null;
            while (it2.hasNext()) {
                line = (String)it2.next();
                output.write(line);
                output.newLine();
            }
            if (line != null) {
                previousLineWasEmpty = isEmpty(line);
            }
        }
        output.flush();
    }
    
    public Object put(Object key, Object value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        Item item = (Item)itemIndex.get((String) key);
        String result = null;
        if (item != null) {
            result = item.getValue();
            item.setValue((String) value);
        } else {
            item = new Item((String) key, (String) value);
            addItem(item, alphabetize);
        }
        return result;
    }

    /**
     * Convenience method to get a property as a string.
     * Same behavior as {@link #get} but has the correct return type.
     * @param key a property name; cannot be null nor empty
     * @return the property value, or null if it was not defined
     */
    public String getProperty(String key) {
        return (String)get(key);
    }
    
    /**
     * Convenience method to set a property.
     * Same behavior as {@link #put} but has the correct argument types.
     * (Slightly slower however.)
     * @param key a property name; cannot be null nor empty
     * @param value the desired value; cannot be null
     * @return previous value of the property or null if there was not any
     */
    public String setProperty(String key, String value) {
        String result = getProperty(key);
        put(key, value);
        return result;
    }

    /**
     * Sets a property to a value broken into segments for readability.
     * Same behavior as {@link #setProperty(String,String)} with the difference that each item
     * will be stored on its own line of text. {@link #getProperty} will simply concatenate
     * all the items into one string, so generally separators
     * (such as <samp>:</samp> for path-like properties) must be included in
     * the items (for example, at the end of all but the last item).
     * @param key a property name; cannot be null nor empty
     * @param value the desired value; cannot be null; can be empty array
     * @return previous value of the property or null if there was not any
     */
    public String setProperty(String key, String[] value) {
        String result = getProperty(key);
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        List/*<String>*/ valueList = Arrays.asList(value);
        Item item = (Item) itemIndex.get(key);
        if (item != null) {
            item.setValue(valueList);
        } else {
            addItem(new Item(key, valueList), alphabetize);
        }
        return result;
    }

    /**
     * Returns comment associated with the property. The comment lines are
     * returned as defined in properties file, that is comment delimiter is
     * included. Comment for property is defined as: continuous block of lines
     * starting with comment delimiter which are followed by property
     * declaration (no empty line separator allowed).
     * @param key a property name; cannot be null nor empty
     * @return array of String lines as specified in properties file; comment
     *    delimiter character is included
     */
    public String[] getComment(String key) {
        Item item = (Item)itemIndex.get(key);
        if (item == null) {
            return new String[0];
        }
        return item.getComment();
    }

    /**
     * Create comment for the property.
     * <p>Note: if a comment includes non-ISO-8859-1 characters, they will be written
     * to disk using Unicode escapes (and {@link #getComment} will interpret
     * such escapes), but of course they will be unreadable for humans.
     * @param key a property name; cannot be null nor empty
     * @param comment lines of comment which will be written just above
     *    the property; no reformatting; comment lines must start with 
     *    comment delimiter; cannot be null; cannot be emty array
     * @param separate whether the comment should be separated from previous
     *    item by empty line
     */
    public void setComment(String key, String[] comment, boolean separate) {
        // XXX: check validity of comment parameter
        Item item = (Item)itemIndex.get(key);
        if (item == null) {
            throw new IllegalArgumentException("Cannot set comment for non-existing property "+key);
        }
        item.setComment(comment, separate);
    }
    
    public Object clone() {
        return cloneProperties();
    }
    
    /**
     * Create an exact copy of this properties object.
     * @return a clone of this object
     */
    public EditableProperties cloneProperties() {
        return new EditableProperties(this);
    }

    // non-key item is block of empty lines/comment not associated with any property
    private void createNonKeyItem(List/*<String>*/ lines) {
        // First check that previous item is not non-key item.
        if (items.size() > 0) {
            Item item = (Item)items.get(items.size()-1);
            if (item.getKey() == null) {
                // it is non-key item:  merge them
                item.addCommentLines(lines);
                lines.clear();
                return;
            }
        }
        // create new non-key item
        Item item = new Item(lines);
        addItem(item, false);
        lines.clear();
    }

    // opposite to non-key item: item with valid property declaration and 
    // perhaps some comment lines
    private void createKeyItem(List/*<String>*/ lines, int commentLinesCount) {
        Item item = new Item(lines.subList(0, commentLinesCount), lines.subList(commentLinesCount, lines.size()));
        addItem(item, false);
        lines.clear();
    }
    
    private void addItem(Item item, boolean sort) {
        String key = item.getKey();
        if (sort) {
            assert key != null;
            for (int i=0; i<items.size(); i++) {
                String k = ((Item)items.get(i)).getKey();
                if (k != null && k.compareToIgnoreCase(key) > 0) {
                    items.add(i, item);
                    itemIndex.put(key, item);
                    return;
                }
            }
        }
        items.add(item);
        if (key != null) {
            itemIndex.put(key, item);
        }
    }
    
    private void removeItem(Item item) {
        items.remove(item);
        if (item.getKey() != null) {
            itemIndex.remove(item.getKey());
        }
    }
    
    // does property declaration continue on next line?
    private boolean isContinue(String line) {
        int index = line.length() - 1;
        int slashCount = 0;
        while (index >= 0 && line.charAt(index) == '\\') {
            slashCount++;
            index--;
        }
        // if line ends with odd number of backslash then property definition 
        // continues on next line
        return (slashCount % 2 != 0);
    }
    
    // does line start with comment delimiter? (whitespaces are ignored)
    private static boolean isComment(String line) {
        line = trimLeft(line);
        if (line.length() != 0 && commentChars.indexOf(line.charAt(0)) != -1) {
            return true;
        } else {
            return false;
        }
    }

    // is line empty? (whitespaces are ignored)
    private static boolean isEmpty(String line) {
        return trimLeft(line).length() == 0;
    }

    // remove all whitespaces from left
    private static String trimLeft(String line) {
        int start = 0;
        while (start < line.length()) {
            if (whiteSpaceChars.indexOf(line.charAt(start)) == -1) {
                break;
            }
            start++;
        }
        return line.substring(start);
    }
    
    /**
     * Representation of one item read from properties file. It can be either
     * valid property declaration with associated comment or chunk of empty
     * lines or lines with comment which are not associated with any property.
     */
    private static class Item implements Cloneable {

        /** Lines of comment as read from properties file and as they will be
         * written back to properties file. */
        private List/*<String>*/ commentLines;

        /** Lines with property name and value declaration as read from 
         * properties file and as they will be written back to properties file. */
        private List/*<String>*/ keyValueLines;

        /** Property key */
        private String key;
        
        /** Property value */
        private String value;

        /** Should this property be separated from previous one by at least
         * one empty line. */
        private boolean separate;
        
        // constructor only for cloning
        private Item() {
        }
        
        /**
         * Create instance which does not have any key and value - just 
         * some empty or comment lines. This item is READ-ONLY.
         */
        public Item(List/*<String>*/ commentLines) {
            this.commentLines = new ArrayList(commentLines);
        }

        /**
         * Create instance from the lines of comment and property declaration.
         * Property name and value will be split.
         */
        public Item(List/*<String>*/ commentLines, List/*<String>*/ keyValueLines) {
            this.commentLines = new ArrayList(commentLines);
            this.keyValueLines = new ArrayList(keyValueLines);
            parse(keyValueLines);
        }

        /**
         * Create new instance with key and value.
         */
        public Item(String key, String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Create new instance with key and value.
         */
        public Item(String key, List/*<String>*/ value) {
            this.key = key;
            setValue(value);
        }

        // backdoor for merging non-key items
        void addCommentLines(List/*<String>*/ lines) {
            assert key == null;
            commentLines.addAll(lines);
        }
        
        public String[] getComment() {
            String[] res = new String[commentLines.size()];
            for (int i = 0; i < res.length; i++) {
                // #60249: the comment might have Unicode chars in escapes.
                res[i] = decodeUnicode((String) commentLines.get(i));
            }
            return res;
        }
        
        public void setComment(String[] commentLines, boolean separate) {
            this.separate = separate;
            this.commentLines = new ArrayList(commentLines.length);
            for (int i = 0; i < commentLines.length; i++) {
                // #60249 again - write only ISO-8859-1.
                this.commentLines.add(encodeUnicode(commentLines[i]));
            }
        }
        
        public String getKey() {
            return key;
        }
        
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
            keyValueLines = null;
        }

        public void setValue(List/*<String>*/ value) {
            StringBuffer val = new StringBuffer();
            List/*<String>*/ l = new ArrayList();
            if (!value.isEmpty()) {
                l.add(encode(key, true) + "=\\"); // NOI18N
                Iterator it = value.iterator();
                while (it.hasNext()) {
                    String s = (String)it.next();
                    val.append(s);
                    s = encode(s, false);
                    l.add(it.hasNext() ? INDENT + s + '\\' : INDENT + s); // NOI18N
                }
            } else {
                // #45061: for no vals, use just "prop="
                l.add(encode(key, true) + '='); // NOI18N
            }
            this.value = val.toString();
            keyValueLines = l;
        }

        public boolean isSeparate() {
            return separate;
        }

        /**
         * Returns persistent image of this property.
         */
        public List/*<String>*/ getRawData() {
            ArrayList l = new ArrayList();
            if (commentLines != null) {
                l.addAll(commentLines);
            }
            if (keyValueLines != null) {
                l.addAll(keyValueLines);
            } else {
                keyValueLines = new ArrayList();
                if (key != null && value != null) {
                    keyValueLines.add(encode(key, true)+"="+encode(value, false));
                }
                l.addAll(keyValueLines);
            }
            return l;
        }
        
        private void parse(List/*<String>*/ keyValueLines) {
            // merge lines into one:
            String line = mergeLines(keyValueLines);
            // split key and value
            splitKeyValue(line);
        }
        
        private String mergeLines(List/*<String>*/ lines) {
            String line = "";
            Iterator it = lines.iterator();
            while (it.hasNext()) {
                String l = trimLeft((String)it.next());
                // if this is not the last line then remove last backslash
                if (it.hasNext()) {
                    assert l.endsWith("\\") : lines;
                    l = l.substring(0, l.length()-1);
                }
                line += l;
            }
            return line;
        }
        
        private void splitKeyValue(String line) {
            int separatorIndex = 0;
            while (separatorIndex < line.length()) {
                char ch = line.charAt(separatorIndex);
                if (ch == '\\') {
                    // ignore next one character
                    separatorIndex++;
                } else {
                    if (keyValueSeparators.indexOf(ch) != -1) {
                        break;
                    }
                }
                separatorIndex++;
            }
            key = decode(line.substring(0, separatorIndex));
            line = trimLeft(line.substring(separatorIndex));
            if (line.length() == 0) {
                value = "";
                return;
            }
            if (strictKeyValueSeparators.indexOf(line.charAt(0)) != -1) {
                line = trimLeft(line.substring(1));
            }
            value = decode(line);
        }
        
        private static String decode(String input) {
            char ch;
            int len = input.length();
            StringBuffer output = new StringBuffer(len);
            for (int x=0; x<len; x++) {
                ch = input.charAt(x);
                if (ch != '\\') {
                    output.append(ch);
                    continue;
                }
                x++;
                if (x==len) {
                    // backslash at the end? syntax error: ignore it
                    continue;
                }
                ch = input.charAt(x);
                if (ch == 'u') {
                    if (x+5>len) {
                        // unicode character not finished? syntax error: ignore
                        output.append(input.substring(x-1));
                        x += 4;
                        continue;
                    }
                    String val = input.substring(x+1, x+5);
                    try {
                        output.append((char)Integer.parseInt(val, 16));
                    } catch (NumberFormatException e) {
                        // #46234: handle gracefully
                        output.append(input.substring(x - 1, x + 5));
                    }
                    x += 4;
                } else {
                    if (ch == 't') ch = '\t';
                    else if (ch == 'r') ch = '\r';
                    else if (ch == 'n') ch = '\n';
                    else if (ch == 'f') ch = '\f';
                    output.append(ch);
                }
            }
            return output.toString();
        }

        private static String encode(String input, boolean escapeSpace) {
            int len = input.length();
            StringBuffer output = new StringBuffer(len*2);

            for(int x=0; x<len; x++) {
                char ch = input.charAt(x);
                switch(ch) {
                    case ' ':
                        if (x == 0 || escapeSpace)  {
                            output.append('\\');
                        }
                        output.append(' ');
                        break;
                    case '\\':
                        output.append("\\\\");
                        break;
                    case '\t':
                        output.append("\\t");
                        break;
                    case '\n':
                        output.append("\\n");
                        break;
                    case '\r':
                        output.append("\\r");
                        break;
                    case '\f':
                        output.append("\\f");
                        break;
                    default:
                        if ((ch < 0x0020) || (ch > 0x007e)) {
                            output.append("\\u");
                            String hex = Integer.toHexString(ch);
                            for (int i = 0; i < 4 - hex.length(); i++) {
                                output.append('0');
                            }
                            output.append(hex);
                        } else {
                            output.append(ch);
                        }
                }
            }
            return output.toString();
        }
        
        private static String decodeUnicode(String input) {
            char ch;
            int len = input.length();
            StringBuffer output = new StringBuffer(len);
            for (int x = 0; x < len; x++) {
                ch = input.charAt(x);
                if (ch != '\\') {
                    output.append(ch);
                    continue;
                }
                x++;
                if (x==len) {
                    // backslash at the end? syntax error: ignore it
                    continue;
                }
                ch = input.charAt(x);
                if (ch == 'u') {
                    if (x+5>len) {
                        // unicode character not finished? syntax error: ignore
                        output.append(input.substring(x-1));
                        x += 4;
                        continue;
                    }
                    String val = input.substring(x+1, x+5);
                    try {
                        output.append((char)Integer.parseInt(val, 16));
                    } catch (NumberFormatException e) {
                        // #46234: handle gracefully
                        output.append(input.substring(x - 1, x + 5));
                    }
                    x += 4;
                } else {
                    output.append(ch);
                }
            }
            return output.toString();
        }

        private static String encodeUnicode(String input) {
            int len = input.length();
            StringBuffer output = new StringBuffer(len * 2);
            for (int x = 0; x < len; x++) {
                char ch = input.charAt(x);
                if ((ch < 0x0020) || (ch > 0x007e)) {
                    output.append("\\u"); // NOI18N
                    String hex = Integer.toHexString(ch);
                    for (int i = 0; i < 4 - hex.length(); i++) {
                        output.append('0');
                    }
                    output.append(hex);
                } else {
                    output.append(ch);
                }
            }
            return output.toString();
        }
        
        public Object clone() {
            Item item = new Item();
            if (keyValueLines != null) {
                item.keyValueLines = new ArrayList(keyValueLines);
            }
            if (commentLines != null) {
                item.commentLines = new ArrayList(commentLines);
            }
            item.key = key;
            item.value = value;
            return item;
        }
    
    }
    
    private static class SetImpl extends AbstractSet {

        private EditableProperties props;
        
        public SetImpl(EditableProperties props) {
            this.props = props;
        }
        
        public Iterator iterator() {
            return new IteratorImpl(props);
        }
        
        public int size() {
            return props.items.size();
        }
        
    }
    
    private static class IteratorImpl implements Iterator {

        private EditableProperties props;
        private int index = -1;
        private Item item;
        
        public IteratorImpl(EditableProperties props) {
            this.props = props;
        }
        
        public boolean hasNext() {
            return findNext() != -1;
        }
        
        public Object next() {
            index = findNext();
            if (index == -1) {
                throw new NoSuchElementException("There is no more items");
            }
            item = (Item)props.items.get(index);
            return new MapEntryImpl(item);
        }
        
        public void remove() {
            if (item == null) {
                throw new IllegalStateException();
            }
            props.removeItem(item);
            index--;
            item = null;
        }
        
        private int findNext() {
            int res = index+1;
            while (res < props.size()) {
                Item i = (Item)props.items.get(res);
                if (i.getKey() != null && i.getValue() != null) {
                    return res;
                }
                res++;
            }
            return -1;
        }
        
    }
    
    private static class MapEntryImpl implements Map.Entry {
        
        private Item item;
        
        public MapEntryImpl(Item item) {
            this.item = item;
        }
        
        public Object getKey() {
            return item.getKey();
        }
        
        public Object getValue() {
            return item.getValue();
        }
        
        public Object setValue(Object value) {
            String result = item.getValue();
            item.setValue((String)value);
            return result;
        }
        
    }
    
}
