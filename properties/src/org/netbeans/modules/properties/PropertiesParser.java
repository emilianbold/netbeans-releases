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


package org.netbeans.modules.properties;


import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.Position;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.text.PositionRef;
import org.openide.text.PositionBounds;
import org.openide.TopManager;
import org.openide.util.io.NullOutputStream;


/** 
 * Parser of Java source code. It generates the hierarchy
 * of the implementations of elements.
 *
 * @author Petr Jiricka, Petr Hamernik
 */
class PropertiesParser {

    /** PropertiesFileEntry for which source is this parser created. */
    PropertiesFileEntry pfe;

    /** Appropriate properties editor - used for creating the PositionRefs */
    PropertiesEditorSupport editor;

    /** Properties file reader. Input stream. */
    PropertiesReader input;

    /** Properties element */
    PropertiesStructure propStruct;

    
    /** 
     * Constructor.
     * @param pfe FileEntry where the properties file is stored.
     * @exception IOException if any i/o problem occured during reading
     */
    public PropertiesParser(PropertiesFileEntry pfe) throws IOException {
        this.pfe   = pfe;

        editor = pfe.getPropertiesEditor();        
        input = createReader();        
    }

    
    /** Creates new input stream from the file object.
    * Finds the properties data object, checks if the document is loaded
    * and creates the stream either from the file object or from the document.
    * @exception IOException if any i/o problem occured during reading
    */
    private PropertiesReader createReader() throws IOException {
        if (editor.isDocumentLoaded()) {
            // Loading from the memory (Document)
            final Document doc = editor.getDocument();
            final String[] str = new String[1];
            
            // safely take the text from the document
            doc.render(new Runnable() {
                public void run() {
                    try {
                        str[0] = doc.getText(0, doc.getLength());
                    } catch (javax.swing.text.BadLocationException e) {
                        // impossible
                    }
                }
            });
            
            return new PropertiesReader(str[0]);
            
        } else {
            // Loading from the file.
            InputStream is = new PropertiesEditorSupport.NewLineInputStream(pfe.getFile().getInputStream());
            
            return new PropertiesReader(is);
        }
    }

    /** Parses the file - this method starts the parser.
    * After super.parseFile finish, the result is set to
    * the appropriate SourceElementImpl.
    */
    public void parseFile() {
        try {
            PropertiesStructure ps = parseFileMain(input);
            input.close();
            pfe.getHandler().setPropertiesStructure(ps);
        }
        catch (IOException e) {
            // parsing failed, the old copy remains valid
            // if there is no old copy, notify user
            // PENDING notify the user
        }
    }

    /** Parses .properties file and creates <code>PropertiesStruture</code>. */
    private PropertiesStructure parseFileMain(PropertiesReader in) throws IOException {

        ArrayMapList aml = new ArrayMapList();
        
        while (true) {
            Element.ItemElem elem = readNextElem(in);
            if (elem == null)
                break;
            else {
                // add at the end of the list
                aml.add(elem.getKey(), elem);
            }
        }
        return new PropertiesStructure(createBiasBounds(0, in.position), aml);
    }

    /** @return next element or null if the end of the stream occurred */
    private Element.ItemElem readNextElem(PropertiesReader in) throws IOException {
        Element.CommentElem commE;
        Element.KeyElem keyE;
        Element.ValueElem valueE;

        int charRead;
        int begPos = in.position;

        // read the comment
        int keyPos = begPos;
        FlaggedLine fl = in.readLineExpectComment();
        StringBuffer comment = new StringBuffer();
        boolean firstNull = true;
        while (fl != null) {
            firstNull = false;
            if (fl.flag) {
                // part of the comment
                comment.append(fl.line);
                comment.append(fl.lineSep);
                keyPos = in.position;
            }
            else
                // not a part of a comment
                break;
            fl = in.readLineExpectComment();
        }

        // exit completely if null is returned the very first time
        if (firstNull)
            return null;

        String comHelp;
        comHelp = comment.toString();
        if (comment.length() > 0)
            if (comment.charAt(comment.length() - 1) == '\n')
                comHelp = comment.substring(0, comment.length() - 1);

        commE = new Element.CommentElem(createBiasBounds(begPos, keyPos), comHelp);
        // fl now contains the line after the comment or  null if none exists


        if (fl == null) {
            keyE = null;
            valueE = null;
        } else {
            // read the key and the value
            // list of
            ArrayList lines = new ArrayList(2);
            fl.startPosition = keyPos;
            fl.stringValue = fl.line.toString();
            lines.add(fl);
            int nowPos;
            while (UtilConvert.continueLine(fl.line)) {
                // do something with the previous line
                fl.stringValue = fl.stringValue.substring(0, fl.stringValue/*fix: was: line*/.length() - 1);
                // now the new line
                nowPos = in.position;
                fl = in.readLineNoFrills();
                if (fl == null) break;
                // delete the leading whitespaces
                int startIndex=0;
                for(startIndex=0; startIndex < fl.line.length(); startIndex++)
                    if (UtilConvert.whiteSpaceChars.indexOf(fl.line.charAt(startIndex)) == -1)
                        break;
                fl.stringValue = fl.line.substring(startIndex, fl.line.length());
                fl.startPosition = nowPos + startIndex;
                lines.add(fl);
            }
            // now I have an ArrayList with strings representing lines and positions of the first non-whitespace character

            PositionMap pm = new PositionMap(lines);
            String line = pm.getString();

            // Find start of key
            int len = line.length();
            int keyStart;
            for(keyStart=0; keyStart<len; keyStart++) {
                if(UtilConvert.whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1)
                    break;
            }
            
            // Find separation between key and value
            int separatorIndex;
            for(separatorIndex=keyStart; separatorIndex<len; separatorIndex++) {
                char currentChar = line.charAt(separatorIndex);
                if (currentChar == '\\')
                    separatorIndex++;
                else if(UtilConvert.keyValueSeparators.indexOf(currentChar) != -1)
                    break;
            }

            // Skip over whitespace after key if any
            int valueIndex;
            for (valueIndex=separatorIndex; valueIndex<len; valueIndex++)
                if (UtilConvert.whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                    break;

            // Skip over one non whitespace key value separators if any
            if (valueIndex < len)
                if (UtilConvert.strictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1)
                    valueIndex++;

            // Skip over white space after other separators if any
            while (valueIndex < len) {
                if (UtilConvert.whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                    break;
                valueIndex++;
            }
            String key = line.substring(keyStart, separatorIndex);
            String value = (separatorIndex < len) ? line.substring(valueIndex, len) : ""; // NOI18N

            if (key == null)
                // PENDING - should join with the next comment
                ;

            int currentPos = in.position;
            int valuePosFile = 0;
            try {
                valuePosFile = pm.getFilePosition(valueIndex);
            }
            catch (ArrayIndexOutOfBoundsException e) {
                valuePosFile = currentPos;
            }
            keyE   = new Element.KeyElem  (createBiasBounds(keyPos, valuePosFile), key);
            valueE = new Element.ValueElem(createBiasBounds(valuePosFile, currentPos), value);
        }
        return new Element.ItemElem(createBiasBounds(begPos, in.position), keyE, valueE, commE);
    }

    /** Computes the real offset from the long value representing position
    * in the parser.
    * @return the offset
    */
    static int position(long p) {
        return (int)(p & 0xFFFFFFFFL);
    }

    /** Creates position bounds. For obtaining the real offsets is used
    * previous method position()
    * @param begin the begin in the internal position form
    * @param end the end in the internal position form
    * @return the bounds
    */
    PositionBounds createBiasBounds(long begin, long end) {
        PositionRef posBegin = editor.createPositionRef(position(begin), Position.Bias.Forward);
        PositionRef posEnd = editor.createPositionRef(position(end), Position.Bias.Backward);
        return new PositionBounds(posBegin, posEnd);
    }

    /** 
     * A properties reader which allows reading from an input stream or from a string and remembers
     * its position in the document.
     */
    private static class PropertiesReader {

        /** The underlaying reader. */
        private Reader reader;

        /** Position after the last character read. */
        public int position;

        /** The character that someone peeked. */
        private int peekChar;

        
        /** Does the initialization */
        private PropertiesReader() {
            peekChar = -1;
            position = 0;
        }
        
        /** Creates the reader from the text. */
        private PropertiesReader(String text) {
            this();
            reader = new StringReader(text);
        }

        /** Creates the reader from the another stream. */
        private PropertiesReader(InputStream stream) {
            this();
            try {
                reader = new BufferedReader(new InputStreamReader(stream, "8859_1")); // NOI18N
            } catch (UnsupportedEncodingException e) {
                // Impossible - this encoding is always supported.
            }
        }

        
        /** Read one character from the stream and increases the position.
        * @return the character or -1 if the end of the stream has been reached
        */
        public int read() throws IOException {
            int character = peek();
            peekChar = -1;
            if (character != -1)
                position++;

            return character;
        }

        /** Returns the next character without increasing the position. Subsequent calls
        * to peek() and read() will return the same character.
        * @return the character or -1 if the end of the stream has been reached
        */
        private int peek() throws IOException {
            if (peekChar == -1)
                peekChar = reader.read();
            return peekChar;
        }

        /** Reads the next line and returns the flag as true if the line is a comment line.
         *  If the input is empty returns null
         *  Flag in the result is true if the line is a comment line
         */
        public FlaggedLine readLineExpectComment() throws IOException {
            int charRead = read();
            if (charRead == -1)
                // end of the reader reached
                return null;

            boolean decided = false;
            FlaggedLine fl = new FlaggedLine();
            while (charRead != -1 && charRead != (int)'\n' && charRead != (int)'\r') {
                if (!decided)
                    if(UtilConvert.whiteSpaceChars.indexOf((char)charRead) == -1) {
                        // not a whitespace - decide now
                        fl.flag = (((char)charRead == '!') || ((char)charRead == '#'));
                        decided = true;
                    }
                fl.line.append((char)charRead);
                charRead = read();
            }

            if (!decided)
                // all were whitespaces
                fl.flag = true;

            // set the line separator
            if (charRead == (int)'\r')
                if (peek() == (int)'\n') {
                    charRead = read();
                    fl.lineSep = "\r\n"; // NOI18N
                }
                else
                    fl.lineSep = "\r"; // NOI18N
            else
                if (charRead == (int)'\n') 
                    fl.lineSep = "\n"; // NOI18N
                else
                    fl.lineSep = System.getProperty("line.separator"); // NOI18N

            return fl;
        }

        /** Reads the next line .
         *  If the input is empty returns null
         */
        public FlaggedLine readLineNoFrills() throws IOException {
            int charRead = read();
            if (charRead == -1)
                // end of the reader reached
                return null;

            FlaggedLine fl = new FlaggedLine();
            while (charRead != -1 && charRead != (int)'\n' && charRead != (int)'\r') {
                fl.line.append((char)charRead);
                charRead = read();
            }

            // set the line separator
            if (charRead == (int)'\r')
                if (peek() == (int)'\n') {
                    charRead = read();
                    fl.lineSep = "\r\n"; // NOI18N
                }
                else
                    fl.lineSep = "\r"; // NOI18N
            else
                if (charRead == (int)'\n') // NOI18N
                    fl.lineSep = "\n"; // NOI18N
                else
                    fl.lineSep = System.getProperty("line.separator"); // NOI18N

            return fl;
        }

        /** Closes the stream */
        public void close() throws IOException {
            reader.close();
        }
    } // End of inner class PropertiesReader.

    
    /** Inner  class which maps positions in a string to positions in the underlying file.
     * @see FlaggedLine */
    private static class PositionMap {

        /** List of <code>FlaggedLine</code>'s. */
        private ArrayList list;
        
        
        /** Constructor - expects a list of FlaggedLine */
        PositionMap(ArrayList lines) {
            list = lines;
        }
        

        /** Returns the string represented by the object */
        public String getString() {
            String allLines = ((FlaggedLine)list.get(0)).stringValue;
            for (int part=1; part<list.size(); part++)
                allLines += ((FlaggedLine)list.get(part)).stringValue;
            return allLines;
        }

        /** Returns position in the file for a position in a string
        * @param posString position in the string to find file position for
        * @return position in the file 
        * @exception ArrayIndexOutOfBoundsException if the requested position is outside 
        *  the area represented by this object
        */                  
        public int getFilePosition(int posString) throws ArrayIndexOutOfBoundsException {
            // get the part
            int part;
            int lengthSoFar = 0;
            int lastLengthSoFar = 0;
            for (part=0; part < list.size(); part++) {
                lastLengthSoFar = lengthSoFar;
                lengthSoFar += ((FlaggedLine)list.get(part)).stringValue.length();
                // brute patch - last (cr)lf should not be the part of the thing, other should
                if (part == list.size() - 1)
                    if (lengthSoFar >= posString)
                        break;
                    else;
                else
                    if (lengthSoFar > posString)
                        break;
            }
            if (posString > lengthSoFar)
                throw new ArrayIndexOutOfBoundsException("not in scope"); // NOI18N

            return ((FlaggedLine)list.get(part)).startPosition + posString - lastLengthSoFar;
        }
    } // End of inner class PositionMap.

    
    /** Helper inner class */
    private static class FlaggedLine {

        /** Line buffer. */
        StringBuffer line;
        
        /** Flag. */
        boolean flag;
        
        /** Line separator. */
        String lineSep;
        
        /** STart position. */
        int startPosition;
        
        /** Value. */
        String stringValue;

        
        /** Construtor. */
        FlaggedLine() {
            line = new StringBuffer();
            flag = false;
            lineSep = "\n"; // NOI18N
            startPosition = 0;
        }
    } // End of inner class FlaggedLine.
}
