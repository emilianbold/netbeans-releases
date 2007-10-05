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
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * Default implementation of CharBindingMap interface.
 * Provides a mapping for the following symbols:<BR>
 * @see org.netbeans.jemmy.CharBindingMap
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class DefaultCharBindingMap implements CharBindingMap {

    private Hashtable chars;
    private ClassReference cl;

    /**
     * Constructor.
     */
    public DefaultCharBindingMap() {
	try {
	    cl = new ClassReference("java.awt.event.KeyEvent");
	} catch(ClassNotFoundException e) {
	    e.printStackTrace();
	}
        initMap();
    }

    /**
     * Returns the code of the primary key used to type a symbol.
     * @param c Symbol code.
     * @return a key code.
     * @see CharBindingMap#getCharKey(char)
     * @see java.awt.event.InputEvent
     */
    public int getCharKey(char c) {
        return(getKeyAndModifiers(c)[0]);
    }

    /**
     * Returns the modifiers that should be pressed to type a symbol.
     * @param c Symbol code.
     * @return a combination of InputEvent MASK fields.
     * @see CharBindingMap#getCharModifiers(char)
     * @see java.awt.event.InputEvent
     */
    public int getCharModifiers(char c) {
        return(getKeyAndModifiers(c)[1]);
    }

    /**
     * Returns key + modifiers pair.
     * @param c Symbol code.
     * @return an array of two elements: key code and modifiers mask - 
     * a combination of InputEvent MASK fields.
     */
    public int[] getKeyAndModifiers(char c) {
        CharKey key = (CharKey)chars.get(new Character(c));
        if(key != null) {
            return(new int[] {key.key, key.modifiers});
        } else {
            return(new int[] {KeyEvent.VK_UNDEFINED, 0});
        }
    }

    /**
     * Returns an array of all supported chars.
     * @return an array of chars representing the supported chars values.
     */
    public char[] getSupportedChars() {
        char[] charArray = new char[chars.size()];
        Enumeration keys = chars.keys();
        int index = 0;
        while(keys.hasMoreElements()) {
            charArray[index] = ((Character)keys.nextElement()).charValue();
        }
        return(charArray);
    }

    /**
     * Removes a char from supported.
     * @param c Symbol code.
     */
    public void removeChar(char c) {
        chars.remove(new Character(c));
    }

    /**
     * Adds a char to supported.
     * @param c Symbol code.
     * @param key key code.
     * @param modifiers a combination of InputEvent MASK fields.
     */
    public void addChar(char c, int key, int modifiers) {
        chars.put(new Character(c), new CharKey(key, modifiers));
    }

    private void initMap() {
        chars = new Hashtable();
        //first add latters and digits represented by KeyEvent.VK_. fields
        Field[] fields = KeyEvent.class.getFields();
        for(int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            if((fields[i].getModifiers() & Modifier.PUBLIC) != 0 &&
               (fields[i].getModifiers() & Modifier.STATIC) != 0 &&
               fields[i].getType() == Integer.TYPE &&
               name.startsWith("VK_") &&
               name.length() == 4) {
                String latter = name.substring(3, 4);
                try {
                    int key = fields[i].getInt(null);
                    addChar(latter.toLowerCase().charAt(0), key, 0);
                    if(!latter.toUpperCase().equals(latter.toLowerCase())) {
                        addChar(latter.toUpperCase().charAt(0), key, InputEvent.SHIFT_MASK);
                    }
                } catch(IllegalAccessException e) {
                    //never could happen!
                }
            }
        }
        //add special simbols
        addChar('\t', KeyEvent.VK_TAB          , 0); 
        addChar(' ',  KeyEvent.VK_SPACE        , 0); 
        addChar('!',  KeyEvent.VK_1            , InputEvent.SHIFT_MASK); 
        addChar('"',  KeyEvent.VK_QUOTE        , InputEvent.SHIFT_MASK); 
        addChar('#',  KeyEvent.VK_3            , InputEvent.SHIFT_MASK); 
        addChar('$',  KeyEvent.VK_4            , InputEvent.SHIFT_MASK); 
        addChar('%',  KeyEvent.VK_5            , InputEvent.SHIFT_MASK); 
        addChar('&',  KeyEvent.VK_7            , InputEvent.SHIFT_MASK); 
        addChar('\'', KeyEvent.VK_QUOTE        , 0); 
        addChar('(',  KeyEvent.VK_9            , InputEvent.SHIFT_MASK); 
        addChar(')',  KeyEvent.VK_0            , InputEvent.SHIFT_MASK); 
        addChar('*',  KeyEvent.VK_8            , InputEvent.SHIFT_MASK); 
        addChar('+',  KeyEvent.VK_EQUALS       , InputEvent.SHIFT_MASK); 
        addChar(',',  KeyEvent.VK_COMMA        , 0); 
        addChar('-',  KeyEvent.VK_MINUS        , 0); 
        addChar('.',  KeyEvent.VK_PERIOD       , 0); 
        addChar('/',  KeyEvent.VK_SLASH        , 0); 
        addChar(':',  KeyEvent.VK_SEMICOLON    , InputEvent.SHIFT_MASK); 
        addChar(';',  KeyEvent.VK_SEMICOLON    , 0); 
        addChar('<',  KeyEvent.VK_COMMA        , InputEvent.SHIFT_MASK); 
        addChar('=',  KeyEvent.VK_EQUALS       , 0); 
        addChar('>',  KeyEvent.VK_PERIOD       , InputEvent.SHIFT_MASK); 
        addChar('?',  KeyEvent.VK_SLASH        , InputEvent.SHIFT_MASK); 
        addChar('@',  KeyEvent.VK_2            , InputEvent.SHIFT_MASK); 
        addChar('[',  KeyEvent.VK_OPEN_BRACKET , 0); 
        addChar('\\', KeyEvent.VK_BACK_SLASH   , 0); 
        addChar(']',  KeyEvent.VK_CLOSE_BRACKET, 0); 
        addChar('^',  KeyEvent.VK_6            , InputEvent.SHIFT_MASK); 
        addChar('_',  KeyEvent.VK_MINUS        , InputEvent.SHIFT_MASK); 
        addChar('`',  KeyEvent.VK_BACK_QUOTE   , 0); 
        addChar('{',  KeyEvent.VK_OPEN_BRACKET , InputEvent.SHIFT_MASK); 
        addChar('|',  KeyEvent.VK_BACK_SLASH   , InputEvent.SHIFT_MASK); 
        addChar('}',  KeyEvent.VK_CLOSE_BRACKET, InputEvent.SHIFT_MASK); 
        addChar('~',  KeyEvent.VK_BACK_QUOTE   , InputEvent.SHIFT_MASK);
        addChar('\n', KeyEvent.VK_ENTER        , 0);
    }

    private static class CharKey {
        public int key;
        public int modifiers;
        public CharKey(int key, int modifiers) {
            this.key = key;
            this.modifiers = modifiers;
        }
    }

}
