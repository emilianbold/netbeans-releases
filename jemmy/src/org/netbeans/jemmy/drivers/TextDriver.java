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

package org.netbeans.jemmy.drivers;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Defines how to work with text components.
 */
public interface TextDriver {

    /**
     * Moves caret.
     * @param oper Text component operator.
     * @param position Position to move caret to.
     */
    public void changeCaretPosition(ComponentOperator oper, int position);

    /**
     * Selects text.
     * @param oper Text component operator.
     * @param startPosition
     * @param finalPosition
     */
    public void selectText(ComponentOperator oper, int startPosition, int finalPosition);

    /**
     * Clears component text.
     * @param oper Text component operator.
     */
    public void clearText(ComponentOperator oper);

    /**
     * Types new text.
     * @param oper Text component operator.
     * @param text New text to type.
     * @param caretPosition Type text at that position.
     */
    public void typeText(ComponentOperator oper, String text, int caretPosition);

    /**
     * Replace component text.
     * @param oper Text component operator.
     * @param text New text to type.
     */
    public void changeText(ComponentOperator oper, String text);

    /**
     * Type text and push enter.
     * @param oper Text component operator.
     * @param text New text to type.
     */
    public void enterText(ComponentOperator oper, String text);
}
