/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.lang.reflect.InvocationTargetException;

/**
 * 
 * Default implementation of CharBindingMap interface.
 * Provides a mapping for the following symbols:<BR>
 * " !"#$%&'()*,-./0123456789:;\<\>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
 * @see org.netbeans.jemmy.CharBindingMap
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class DefaultCharBindingMap implements CharBindingMap {

    private char[] chars = {
	'\t',
	' ',
	'!',
	'"',
	'#',
	'$',
	'%',
	'&',
	'\'',
	'(',
	')',
	'*',
	'+',
	',',
	'-',
	'.',
	'/',
	':',
	';',
	'<',
	'=',
	'>',
	'?',
	'@',
	'[',
	'\\',
	']',
	'^',
	'_',
	'`',
	'{',
	'|',
	'}',
	'~',
	'\n'
    };

    private int[][] keys = {
	{0,                     KeyEvent.VK_TAB}, 
	{0,                     KeyEvent.VK_SPACE}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_1}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_QUOTE}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_3}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_4}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_5}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_7}, 
	{0,                     KeyEvent.VK_QUOTE}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_9}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_0}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_8}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_EQUALS}, 
	{0,                     KeyEvent.VK_COMMA}, 
	{0,                     KeyEvent.VK_MINUS}, 
	{0,                     KeyEvent.VK_PERIOD}, 
	{0,                     KeyEvent.VK_SLASH}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_SEMICOLON}, 
	{0,                     KeyEvent.VK_SEMICOLON}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_COMMA}, 
	{0,                     KeyEvent.VK_EQUALS}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_PERIOD}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_SLASH}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_2}, 
	{0,                     KeyEvent.VK_OPEN_BRACKET}, 
	{0,                     KeyEvent.VK_BACK_SLASH}, 
	{0,                     KeyEvent.VK_CLOSE_BRACKET}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_6}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_MINUS}, 
	{0,                     KeyEvent.VK_BACK_QUOTE}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_OPEN_BRACKET}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_BACK_SLASH}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_CLOSE_BRACKET}, 
	{InputEvent.SHIFT_MASK, KeyEvent.VK_BACK_QUOTE},
	{0,                     KeyEvent.VK_ENTER}
    };

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
    }

    //searches char in array and returns array index
    private int charIndex(char c) {
	for(int i = 0; i < chars.length; i++) {
	    if(chars[i] == c) {
		return(i);
	    }
	}
	return(-1);
    }

    /**
     * Returns the code of the primary key used to type a symbol.
     * @param c Symbol code.
     * @return a key code.
     * @see CharBindingMap#getCharKey(char)
     * @see java.awt.event.InputEvent
     */
    public int getCharKey(char c) {
	if(Character.isLetter(c) || Character.isDigit(c)) {
	    try {
		char[] carr = {c};
		return(((Integer)cl.getField("VK_" + new String(carr).toUpperCase())).intValue());
	    } catch(NoSuchFieldException e) {
		e.printStackTrace();
	    } catch(IllegalAccessException e) {
		e.printStackTrace();
	    }
	} else {
	    int ci = charIndex(c);
	    if(ci > 0) {
		return(keys[ci][1]);
	    }
	}
	return(KeyEvent.VK_UNDEFINED);
    }

    /**
     * Returns the modifiers that should be pressed to type a symbol.
     * @param c Symbol code.
     * @return a combination of InputEvent MASK fields.
     * @see CharBindingMap#getCharModifiers(char)
     * @see java.awt.event.InputEvent
     */
    public int getCharModifiers(char c) {
	if(Character.isLetter(c) && Character.isUpperCase(c)) {
	    return(InputEvent.SHIFT_MASK);
	} else {
	    int ci = charIndex(c);
	    if(ci > 0) {
		return(keys[ci][0]);
	    }
	}
	return(0);
    }
}
