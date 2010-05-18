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
package com.sun.rave.web.ui.util;

import com.sun.rave.web.ui.component.util.descriptors.LayoutElement;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.faces.component.UIComponent;


/**
 *  This class takes a "Permission String" and is cabable of determining if
 *  a user passes the permission check.  Supported "checks" are:
 *
 *  <UL><LI>Boolean -- "true" or "false"</LI>
 *	</UL>
 *
 *  The format of the "Permission String" must be an equation that results in a
 *  boolean answer.  The supported functions/operators are:
 *
 *	 --
 *  <UL><LI>$&lt;type&gt;{&lt;key&gt;} -- To read a value according to
 *	    &lt;type&gt; using &lt;key&gt; (See:
 *	    {@link com.sun.rave.web.ui.util.VariableResolver}).  (null is
 *	    interpretted as false, non boolean values (besides the string
 *	    "false") are interpretted to mean true) </LI>
 *	<LI>'(' and ')' can be used to define order of operation </LI>
 *	<LI>'!' may be used to negate a value </LI>
 *	<LI>'|' may be used as a logical OR </LI>
 *	<LI>'&' may be used as a logical AND </LI>
 *	<LI>'=' may be used as a String equals </LI>
 *	</UL>
 *
 *
 *  Operator Precedence (for infix notation) is:
 *  <UL><LI> () -- Highest </LI>
 *	<LI> ! </LI>
 *	<LI> & </LI>
 *	<LI> | </LI>
 *	<LI> = </LI>
 *	</UL>
 *
 *  @see VariableResolver
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class PermissionChecker {

    /**
     *	This is the constructor method that is required to create this object.
     */
    public PermissionChecker(LayoutElement desc, UIComponent component, String infixStr) {
	setLayoutElement(desc);
	setUIComponent(component);
	setInfix(stripWhiteSpace(infixStr));
    }


    /**
     *	<P>This method sets the LayoutElement that is associated with the 'if'
     *	check being evaluated.  This is not normally needed, it is only needed
     *	if the 'if' check contains an expression which requires a
     *	LayoutElement to be properly evaluated.</P>
     */
    protected void setUIComponent(UIComponent component) {
	_component = component;
    }


    /**
     *	<P>Retreives the LayoutElement associated with this PermissionChecker
     *	(only needed in cases where a expression requires a LayoutElement for
     *	evaluation).</P>
     */
    public UIComponent getUIComponent() {
	return _component;
    }


    /**
     *	<P>This method sets the LayoutElement that is associated with the 'if'
     *	check being evaluated.  This is not normally needed, it is only needed
     *	if the 'if' check contains an expression which requires a
     *	LayoutElement to be properly evaluated.</P>
     */
    protected void setLayoutElement(LayoutElement desc) {
	_desc = desc;
    }


    /**
     *	<P>Retreives the LayoutElement associated with this PermissionChecker
     *	(only needed in cases where a expression requires a LayoutElement for
     *	evaluation).</P>
     */
    public LayoutElement getLayoutElement() {
	return _desc;
    }


    /**
     *	This method returns the precedence of the given operator.  This only
     *	applies to infix notation (of course) and is needed to correctly order
     *	the operators when converting to postfix.
     *
     *	<UL><LI> ! (not) has the highest precedence
     *	    <LI> & (and)
     *	    <LI> | (and)
     *	    </UL>
     *
     *	Of course '(' and ')' can be used to override the order of operations
     *	in infix notation.
     *
     *	@param op	The operator to evaluate.
     *
     *	@return A number that can be used to compare its precedence.
     */
    private static int getPrecedence(char op) {
	switch (op) {
	    case LEFT_PAREN:
		return 1;
	    case RIGHT_PAREN:
		return 999;
	    case EQUALS_OPERATOR:
		return 2;
	    case LESS_THAN_OPERATOR:
	    case MORE_THAN_OPERATOR:
		return 4;
	    case OR_OPERATOR:
		return 8;
	    case AND_OPERATOR:
		return 16;
	    case MODULUS_OPERATOR:
		return 32;
	    case NOT_OPERATOR:
		return 64;
	}
	return 1;
    }

    /**
     *	This method replaces all "true" / "false" strings w/ 't'/'f'.  It
     *	converts the String into a char[].  It replaces all user defined
     *	functions w/ 'F' and places a Function in a list per the registered
     *	user-defined function.  All other strings are converted to an 'F' and
     *	a StringFunction is added to the function list.
     */
    protected char[] preProcessString(String source) {
    	char arr[] = source.toCharArray();
	int sourceLen = arr.length;
	int destLen = 0;
	String str = null;

	// Loop through the String, char by char
	for (int idx=0; idx<sourceLen ; idx++) {
	    switch (arr[idx]) {
		case POST_TRUE:
		case POST_TRUE_CAP:
		    if (((idx + TRUE.length()) <= sourceLen) &&
                        TRUE.equalsIgnoreCase(new String(arr, idx, TRUE.length()))) {
			arr[destLen++] = POST_TRUE;
			idx += TRUE.length()-1;
		    } else {
			idx = storeFunction(arr, idx);
                        arr[destLen++] = FUNCTION_MARKER;
                    }
		    break;
		case POST_FALSE:
		case POST_FALSE_CAP:
		    if (((idx + FALSE.length()) <= sourceLen) &&
                        FALSE.equalsIgnoreCase(new String(arr, idx, FALSE.length()))) {
			arr[destLen++] = POST_FALSE;
			idx += FALSE.length()-1;
		    } else {
			idx = storeFunction(arr, idx);
                        arr[destLen++] = FUNCTION_MARKER;
                    }
		    break;
		case OR_OPERATOR:
		case EQUALS_OPERATOR:
		case LESS_THAN_OPERATOR:
		case MORE_THAN_OPERATOR:
		case MODULUS_OPERATOR:
		case AND_OPERATOR:
		case NOT_OPERATOR:
		case LEFT_PAREN:
		case RIGHT_PAREN:
		    arr[destLen++] = arr[idx];
		    break;
		default:
		    idx = storeFunction(arr, idx);
		    arr[destLen++] = FUNCTION_MARKER;
	    }
	}
	char dest[] = new char[destLen];
	for (int idx=0; idx<destLen; idx++) {
	    dest[idx] = arr[idx];
	}
	return dest;
    }


    /**
     *	This method searches for "$...{...}" in given string and replaces them
     *	with FUNCTION_MARKER's.  It adds a Function to the _tmpFunctionStack
     *	for each of these so that it can be inserted in the proper place during
     *	preProcessing.  If the "${}" evaluates to (null), it is replaced with
     *	false (BooleanFunction); if it evalutes to "false", it also will use a
     *	false BooleanFunction.  In all other cases, it will be replaced with a
     *	StringFunction which evaluates to true.
    protected String substituteVariables(String string) {
// FIXME: Consider reworking this to be an inline substitution... since nested
// FIXME: $()'s aren't supported, a single-pass forward replacement may
// FIXME: simplify this.
	int stringLen = string.length();
	int startTokenLen = VariableResolver.SUB_START.length();
	int delimLen = VariableResolver.SUB_TYPE_DELIM.length();
	int endTokenLen = VariableResolver.SUB_END.length();
	int endIndex, delimIndex;
	int parenSemi;
	char firstEndChar = VariableResolver.SUB_END.charAt(0);
	char firstDelimChar = VariableResolver.SUB_TYPE_DELIM.charAt(0);
	char currChar;
	Object obj = null;

	// Temporary Function List (functions here will be inserted correctly
	// during preProcessing)
	_tmpFunctionStack = new Stack();
	Stack tmpStack = new Stack();
	_currTmpFunction = 0;

	// Iterate through the String backwards looking for substitutions
	for (int startIndex = string.indexOf(VariableResolver.SUB_START);
		 startIndex != -1;
	         startIndex = string.indexOf(VariableResolver.SUB_START, startIndex+1)) {

	    // Find make sure we have a typeDelim
	    delimIndex = string.indexOf(VariableResolver.SUB_TYPE_DELIM, startIndex+startTokenLen);
	    if (delimIndex == -1) {
		continue;
	    }

	    // Next find the end token
	    parenSemi = 0;
	    endIndex = -1;
	    for (int curr = delimIndex+delimLen; curr<stringLen; ) {
		currChar = string.charAt(curr);
		if ((currChar == firstDelimChar) && VariableResolver.SUB_TYPE_DELIM.equals(string.substring(curr, curr+delimLen))) {
		    parenSemi++;
		    curr += delimLen;
		    continue;
		}
		if ((currChar == firstEndChar) && VariableResolver.SUB_END.equals(string.substring(curr, curr+endTokenLen))) {
		    parenSemi--;
		    if (parenSemi < 0) {
			endIndex = curr;
			break;
		    }
		    curr += endTokenLen;
		    continue;
		}
		curr++;
	    }
	    if (endIndex == -1) {
		continue;
	    }

	    // increment the endIndex to point just after it
	    endIndex += endTokenLen;

	    // We found a match!  Create a StringFunction
	    obj = new StringFunction(string.substring(startIndex, endIndex));

	    // Add the function
	    tmpStack.push(obj);

	    // Make new string
	    string = string.substring(0, startIndex) +	// Before replacement
	    	     FUNCTION_MARKER +			// Replacement
		     string.substring(endIndex);	// After
	}

	// Flip Stack (b/c we search forward to help eval ${} correctly
	while (!tmpStack.empty()) {
	    _tmpFunctionStack.push(tmpStack.pop());
	}

	// Return the (new) string
	return string;
    }
     */


    /**
     *	<P>This method looks at the given char array starting at index and
     *	continues until an operator (or end of String) is encountered.  It then
     *	uses this string to lookup a registered function (if any), it stores
     *	that function (with parameters)... or if the function is not found, it
     *	registers a "String function" (which always returns true).</P>
     */
    protected int storeFunction(char arr[], int idx) {
	// Find string...
	int start = idx;
	int len = arr.length;
	while ((idx < len) && !isOperator(arr[idx])) {
	    idx++;
	}

	// Create String...
	String str = new String(arr, start, idx-start);

	// Check to see if it is a registered function...
	Function function = getFunction(str);
	if (function != null) {
	    // Find the left paren...
	    int left = idx;
	    if ((left >= len) || (arr[left] != LEFT_PAREN)) {
		throw new RuntimeException("Function '"+str+
		    "' is expected to have a '"+LEFT_PAREN+
		    "' immediately following it.  Equation: '"+
		    new String(arr)+"'.");
	    }

	    ArrayList arguments = new ArrayList();

	    // Find the right Paren...
	    while ((++idx < len) && (arr[idx] != RIGHT_PAREN)) {
		if (arr[idx] == ARGUMENT_SEPARATOR) {
		    left++;
		    arguments.add(new String(arr, left, idx-left));
		    left = idx;
		}
	    }

	    // Make sure we don't have ()
	    left++;
	    if (idx > left) {
		arguments.add(new String(arr, left, idx-left));
	    }

	    // Set the arguments...
	    function.setArguments(arguments);
	} else {
	    // Not a registered function...
	    idx--; // In this case, there are no ()'s to consume, backup 1
	    if ((str.charAt(0) == FUNCTION_MARKER) && (str.length() == 1) &&
		    !_tmpFunctionStack.empty()) {
		// We have a function added during the subtitute() phase
		function = (Function)_tmpFunctionStack.pop();
	    } else {
		// Create a StringFunction
		function = new StringFunction(str);
	    }
	}

	// Add the function to the function list
	_functionList.add(function);

	// Return the number of characters that we consumed...
	return idx;
    }


    /**
     *	This method is a factory method for constructing a new function based
     *	on the function name passed in.  The function must be registered prior
     *	to invoking this method.
     */
    protected static Function getFunction(String functionName) {
	// Get the Function class
	Class functionClass = (Class)_functions.get(functionName);
	if (functionClass == null) {
	    return null;
	}

	// Create a new instance
	Function function = null;
	try {
	    function = (Function)functionClass.newInstance();
	} catch (Exception ex) {
	    throw new RuntimeException("Unable to instantiate '"+
		functionClass.getName()+"' for '"+functionName+"'", ex);
	}

	// Return the instance
	return function;
    }


    /**
     *	<P>This method allows arbitrary functions to be registered.  Function
     *	names should only contain letters or numbers, other characters or
     *	whitespace may cause problems.  No checking is done to ensure this,
     *	however.</P>
     *
     *	<P>Functions will be expressed in an equation as follows:</P>
     *
     *	<UL><LI>function_name(param1,param2)</LI></UL>
     *	
     *	<P>Function parameters also should only contain alpha-numeric
     *	characters.</P>
     *
     *	<P>Functions must implement PermissionChecker.Function interface</P>
     */
    public static void registerFunction(String functionName, Class function) {
	if (function == null) {
	    _functions.remove(functionName);
	}
	if (!Function.class.isAssignableFrom(function)) {
	    throw new RuntimeException("'"+function.getName()+
		"' must implement '"+Function.class.getName()+"'");
	}
	_functions.put(functionName, function);
    }


    /**
     *	This method returns true if the given character is a valid operator.
     */
    public static boolean isOperator(char ch) {
	switch (ch) {
	    case LEFT_PAREN:
	    case RIGHT_PAREN:
	    case EQUALS_OPERATOR:
	    case LESS_THAN_OPERATOR:
	    case MORE_THAN_OPERATOR:
	    case MODULUS_OPERATOR:
	    case OR_OPERATOR:
	    case AND_OPERATOR:
	    case NOT_OPERATOR:
//	    case AT_OPERATOR:
//	    case POUND_OPERATOR:
//	    case DOLLAR_OPERATOR:
//	    case UP_OPERATOR:
//	    case STAR_OPERATOR:
//	    case TILDA_OPERATOR:
//	    case ARGUMENT_SEPARATOR:
		return true;
	}
	return false;
    }


    /**
     *	This method calculates the postfix representation of the infix equation
     *	passed in.  It returns the postfix equation as a char[].
     *
     *	@param infixStr	The infix representation of the equation.
     *
     *	@return postfix representation of the equation as a char[] (the f()'s
     *		are removed and stored in _functionList).
     */
    protected char [] generatePostfix(String infixStr) {
	// Reset the _functionList
	_functionList = new ArrayList();

	// Convert string to our parsable format
	char result[] = preProcessString(infixStr);
//System.out.println("DEBUG: Initial String: '"+infixStr+"'");
//System.out.println("DEBUG: After Pre-process: '"+new String(result)+"'");
	int resultLen = result.length;
	int postIdx = 0;
	int precedence = 0;
	Stack opStack = new Stack();

	// Put f()'s directly into result, push operators into right order
	for (int idx=0; idx<resultLen; idx++) {
	    switch(result[idx]) {
		case FUNCTION_MARKER:
		case POST_TRUE:
		case POST_FALSE:
		    result[postIdx++] = result[idx];
		    break;
		case LEFT_PAREN:
		    opStack.push(new Character(LEFT_PAREN));
		    break;
		case RIGHT_PAREN:
		    while (!opStack.empty() && (((Character)opStack.peek()).charValue() != LEFT_PAREN)) {
			result[postIdx++] = ((Character)opStack.pop()).charValue();
		    }
		    if (!opStack.empty()) {
			// Throw away the LEFT_PAREN that should still be there
			opStack.pop();
		    }
		    break;
		default:
		    // clear stuff
		    precedence = getPrecedence(result[idx]);
		    while (!opStack.empty() && (getPrecedence(((Character)opStack.peek()).charValue()) >= precedence)) {
			result[postIdx++] = ((Character)opStack.pop()).charValue();
		    }

		    /* Put it on the stack */
		    opStack.push(new Character(result[idx]));
		    break;
	    }
	}

	// empty the rest of the stack to the result
	while (!opStack.empty()) {
	    result[postIdx++] = ((Character)opStack.pop()).charValue();
	}
	// Copy the result to the postfixStr
	char postfixStr[] = new char[postIdx];
	for (int idx=0; idx<postIdx; idx++) {
	    postfixStr[idx] = result[idx];
	}
//System.out.println("DEBUG: Postfix String: '"+new String(postfixStr)+"'");
	return postfixStr;
    }


    /**
     *	This method is invoked to determine if the equation evaluates to true
     *	or false.
     */
    public boolean hasPermission() {
	char postfixArr[] = getPostfixArr();
	int len = postfixArr.length;
	Stack result = new Stack();
	result.push(FALSE_BOOLEAN_FUNCTION); // Default to false
	boolean val1, val2;
	Iterator it = _functionList.iterator();
	Function func = null;

	// Iterate through the postfix array
    	for (int idx=0; idx<len; idx++) {
	    switch (postfixArr[idx]) {
		case POST_TRUE:
		    result.push(TRUE_BOOLEAN_FUNCTION);
		    break;
		case POST_FALSE:
		    result.push(FALSE_BOOLEAN_FUNCTION);
		    break;
		case FUNCTION_MARKER:
		    if (!it.hasNext()) {
			throw new RuntimeException("Unable to evaluate: '"+
			    toString()+"' -- found function marker w/o "+
			    "cooresponding function!");
		    }
		    result.push(it.next());
		    break;
		case EQUALS_OPERATOR:
		    try {
			// Allow reg expression matching
			String matchStr = result.pop().toString();
			val1 = result.pop().toString().matches(matchStr);
		    } catch (EmptyStackException ex) {
			throw new RuntimeException("Unable to evaluate: '"+toString()+"'.", ex);
		    }
		    result.push(val1 ? TRUE_BOOLEAN_FUNCTION : FALSE_BOOLEAN_FUNCTION);
		    break;
		case LESS_THAN_OPERATOR:
		    try {
			// The stack reverses the order, so check greater than
			val1 = Integer.parseInt(result.pop().toString()) > Integer.parseInt(result.pop().toString());
		    } catch (EmptyStackException ex) {
			throw new RuntimeException("Unable to evaluate: '"+toString()+"'.", ex);
		    }
		    result.push(val1 ? TRUE_BOOLEAN_FUNCTION : FALSE_BOOLEAN_FUNCTION);
		    break;
		case MORE_THAN_OPERATOR:
		    try {
			// The stack reverses the order, so check greater than
			val1 = Integer.parseInt(result.pop().toString()) < Integer.parseInt(result.pop().toString());
		    } catch (EmptyStackException ex) {
			throw new RuntimeException("Unable to evaluate: '"+toString()+"'.", ex);
		    }
		    result.push(val1 ? TRUE_BOOLEAN_FUNCTION : FALSE_BOOLEAN_FUNCTION);
		    break;
		case MODULUS_OPERATOR:
		    try {
			// The stack reverses the order...
			int modNumber = Integer.parseInt(result.pop().toString());
			int num = Integer.parseInt(result.pop().toString());
			result.push(new StringFunction(""+(num % modNumber)));
		    } catch (EmptyStackException ex) {
			throw new RuntimeException("Unable to evaluate: '"+toString()+"'.", ex);
		    }
		    break;
		case OR_OPERATOR:
		    try {
			val1 = ((Function)result.pop()).evaluate();
			val2 = ((Function)result.pop()).evaluate();
		    } catch (EmptyStackException ex) {
			throw new RuntimeException("Unable to evaluate: '"+toString()+"'.", ex);
		    }
		    result.push((val1 || val2) ? TRUE_BOOLEAN_FUNCTION : FALSE_BOOLEAN_FUNCTION);
		    break;
		case AND_OPERATOR:
		    try {
			val1 = ((Function)result.pop()).evaluate();
			val2 = ((Function)result.pop()).evaluate();
		    } catch (EmptyStackException ex) {
			throw new RuntimeException("Unable to evaluate: '"+toString()+"'.", ex);
		    }
		    result.push((val1 && val2) ? TRUE_BOOLEAN_FUNCTION : FALSE_BOOLEAN_FUNCTION);
		    break;
		case NOT_OPERATOR:
		    try {
			val1 = ((Function)result.pop()).evaluate();
		    } catch (EmptyStackException ex) {
			throw new RuntimeException("Unable to evaluate: '"+toString()+"'.", ex);
		    }
		    result.push((!val1) ? TRUE_BOOLEAN_FUNCTION : FALSE_BOOLEAN_FUNCTION);
		    break;
	    }
	}

	// Return the only element on the stack (hopefully)
	try {
	    val1 = ((Function)result.pop()).evaluate();
	} catch (EmptyStackException ex) {
	    throw new RuntimeException("Unable to evaluate: '"+
		toString()+"'.", ex);
	}
	if (!result.empty()) {
	    result.pop();  // We added a false that wasn't needed
	    if (!result.empty()) {
		throw new RuntimeException("Unable to evaluate: '"+
		    toString()+"' -- values left on the stack.");
	    }
	}
	return val1;
    }


    /**
     *	This method returns the infix representation of the equation, in other
     *	words: the original String passed in.
     */
    public String getInfix() {
	return _infixStr;
    }


    /**
     *	This method sets the equation and forces a re-evaluation of the
     *	equation.  It returns the postfix representation of the equation.
     *
     *	@param equation	The infix equation to use
     *
     */
    public void setInfix(String equation) {
	_infixStr = equation;
	setPostfixArr(generatePostfix(equation));
    }


    /**
     *
     */
    protected char [] getPostfixArr() {
	if (_postfixArr == null) {
	    _postfixArr = new char[] {' '};
	}
	return _postfixArr;
    }


    /**
     *
     */
    protected void setPostfixArr(char postfix[]) {
	_postfixArr = postfix;
    }


    /**
     *
     */
    public String getPostfix() {
	if (getPostfixArr() == null) {
	    return "";
	}
	return new String(getPostfixArr());
    }


    /**
     *	Displays the infix and postfix version of the equation.
     */
    public String toString() {
    	return _infixStr + " = " + toString(getPostfixArr());
    }


    /**
     *	This toString(...) method generates just the postfix representation of
     *	the equation.  The postfix notation is stored as a char[] and it has
     *	the functions removed from the char[].  This method iterates through
     *	the char[] and generates a String with the functions put back into the
     *	equation.
     *
     *	@param post The char[] representation of the postfix equation
     */
    private String toString(char post[]) {
	int len = post.length;
	StringBuffer result = new StringBuffer("");
	Iterator it = _functionList.iterator();

	for (int idx=0; idx<len; idx++) {
	    switch (post[idx]) {
		case POST_TRUE:
		    result.append(TRUE);
		    break;
		case POST_FALSE:
		    result.append(FALSE);
		    break;
		case FUNCTION_MARKER:
		    result.append(((Function)it.next()).toString());
		    break;
		default:
		    result.append(post[idx]);
	    }
	}

	return result.toString();
    }


    /**
     *	This method removes all whitespace from the given String
     */
    public static String stripWhiteSpace(String input) {
	char arr[] = input.toCharArray();
	int len = arr.length;
	int destLen = 0;

	// Loop through the array skipping whitespace
	for (int idx=0; idx<len; idx++) {
	    if (Character.isWhitespace(arr[idx])) {
		continue;
	    }
	    arr[destLen++] = arr[idx];
	}

	// Return the result
	return new String(arr, 0, destLen);
    }




    /**
     *	This class must be implemented by all user defined Functions.
     *	
     *	<P>In addition to these methods, a toString() should be implemented
     *	that reconstructs the original format of the function (i.e.
     *	function_name(arg1,arg2...)).
     */
    public static interface Function {

	/**
	 *  This method returns the List of arguments.
	 */
	public List getArguments();

	/**
	 *  This method is invoked be the PermissionChecker to set the
	 *  arguments.
	 */
	public void setArguments(List args);

	/**
	 *  This method is invoked by the PermissionCheck to evaluate the
	 *  function to true or false.
	 */
	public boolean evaluate();
    }

    /**
     *	StringFunction implements Function and serves as the default function.
     *	This function is special in that it is NEVER registered and is the
     *	only function that SHOULD NOT be followed by ()'s.  This function will
     *	process embedded expressions and evaulate to false if the entire string
     *	evaulates to null. Otherwise it will return true.  This Function
     *	ignores all arguments (arguments only apply if it is registered, which
     *	shouldn't be the case anyway).
     */
    protected class StringFunction implements PermissionChecker.Function {

	/**
	 *  Constructor.
	 *
	 *  @param  value   The expression to evaluate.
	 */
	public StringFunction(String value) {
	    _value = value;
	}

	/**
	 *  Not used.
	 */
	public List getArguments() {
	    return null;
	}

	/**
	 *  Not used.
	 */
	public void setArguments(List args) {
	}

	public boolean evaluate() {
	    Object obj = getEvaluatedValue();
	    if (obj == null) {
		return false;
	    }
	    if (obj.toString().equalsIgnoreCase("false")) {
		return false;
	    }
	    return true;
	}

	public Object getEvaluatedValue() {
	     return VariableResolver.resolveVariables(getLayoutElement(),
		getUIComponent(), _value);
	}

	public String toString() {
	    Object obj = getEvaluatedValue();
	    if (obj == null) {
		return "";
	    }
	    return obj.toString();
	}

	private String _value;
    }

    /**
     *	BooleanFunction is either true or false.  It is used internally by
     *	PermissionChecker and is not needed outside PermissionChecker since
     *	"true" and "false" used in an equation are equivalent.
     */
    protected static class BooleanFunction implements PermissionChecker.Function {
	public BooleanFunction() {
	}

	public BooleanFunction(boolean value) {
	    _value = value;
	}

	public List getArguments() {
	    return null;
	}

	public void setArguments(List args) {
	}

	public boolean evaluate() {
	    return _value;
	}

	public String toString() {
	    return _value ? "true" : "false";
	}

	private boolean _value;
    }


    /**
     *	This is here to provide some test cases.  It only tests the conversion
     *	to postfix notation.
     */
    public static void main(String args[]) {
    	PermissionChecker checker;
	if (args.length > 0) {
	    for (int count=0; count<args.length; count++) {
		checker = new PermissionChecker(null, null, args[count]);
		System.out.println("Output:\n" + checker.toString()+" ("+checker.hasPermission()+")");
	    }
	} else {
	    boolean success = true;
	    checker = new PermissionChecker(null, null, "true |false");
	    System.out.println("Output:\n" + checker.toString()+" ("+checker.hasPermission()+")");
	    if (!checker.toString().equals("true|false = truefalse|")) {
		System.out.println("\tFAILED!");
		System.out.println("Should have been:\n"+"true|false = truefalse|");
		success = false;
	    }
	    if (!checker.hasPermission()) {
		System.out.println("\tFAILED!");
		System.out.println("hasPermission("+checker.toString(checker.getPostfixArr())+") returned the wrong result!");
		success = false;
	    }

	    checker = new PermissionChecker(null, null, "true&(false|true)");
	    System.out.println("Output:\n" + checker.toString()+" ("+checker.hasPermission()+")");
	    if (!checker.toString().equals("true&(false|true) = truefalsetrue|&")) {
		System.out.println("\tFAILED!");
		System.out.println("Should have been:\n"+"true&(false|true) = truefalsetrue|&");
		success = false;
	    }
	    if (!checker.hasPermission()) {
		System.out.println("\tFAILED!");
		System.out.println("hasPermission("+checker.toString(checker.getPostfixArr())+") returned the wrong result!");
		success = false;
	    }

	    checker = new PermissionChecker(null, null, "true&false|true");
	    System.out.println("Output:\n" + checker.toString()+" ("+checker.hasPermission()+")");
	    if (!checker.toString().equals("true&false|true = truefalse&true|")) {
		System.out.println("\tFAILED!");
		System.out.println("Should have been:\n"+"true&false|true = truefalse&true|");
		success = false;
	    }
	    if (!checker.hasPermission()) {
		System.out.println("\tFAILED!");
		System.out.println("hasPermission("+checker.toString(checker.getPostfixArr())+") returned the wrong result!");
		success = false;
	    }

	    checker = new PermissionChecker(null, null, "true&true|false&true");
	    System.out.println("Output:\n" + checker.toString()+" ("+checker.hasPermission()+")");
	    if (!checker.toString().equals("true&true|false&true = truetrue&falsetrue&|")) {
		System.out.println("\tFAILED!");
		System.out.println("Should have been:\n"+"true&true|false&true = truetrue&falsetrue&|");
		success = false;
	    }
	    if (!checker.hasPermission()) {
		System.out.println("\tFAILED!");
		System.out.println("hasPermission("+checker.toString(checker.getPostfixArr())+") returned the wrong result!");
		success = false;
	    }

	    checker = new PermissionChecker(null, null, "!true|false&!(false|true)");
	    System.out.println("Output:\n" + checker.toString()+" ("+checker.hasPermission()+")");
	    if (!checker.toString().equals("!true|false&!(false|true) = true!falsefalsetrue|!&|")) {
		System.out.println("\tFAILED!");
		System.out.println("Should have been:\n"+"!true|false&!(false|true) = true!falsefalsetrue|!&|");
		success = false;
	    }
	    if (checker.hasPermission()) {
		System.out.println("\tFAILED!");
		System.out.println("hasPermission("+checker.toString(checker.getPostfixArr())+") returned the wrong result!");
		success = false;
	    }

	    checker = new PermissionChecker(null, null, "!(!(true&!true)|!(false|false))|(true|false)&true");
	    System.out.println("Output:\n" + checker.toString()+" ("+checker.hasPermission()+")");
	    if (!checker.toString().equals("!(!(true&!true)|!(false|false))|(true|false)&true = truetrue!&!falsefalse|!|!truefalse|true&|")) {

		System.out.println("\tFAILED!");
		System.out.println("Should have been:\n"+"!(!(true&!true)|!(false|false))|(true|false)&true = truetrue!&!falsefalse|!|!truefalse|true&|");
		success = false;
	    }
	    if (!checker.hasPermission()) {
		System.out.println("\tFAILED!");
		System.out.println("hasPermission("+checker.toString(checker.getPostfixArr())+") returned the wrong result!");
		success = false;
	    }

	    // Test '='
	    checker = new PermissionChecker(null, null, "false =false");
	    System.out.println("Output:\n" + checker.toString()+" ("+checker.hasPermission()+")");
	    if (!checker.toString().equals("false=false = falsefalse=")) {
		System.out.println("\tFAILED!");
		System.out.println("Should have been:\n"+"false=false = falsefalse=");
		success = false;
	    }
	    if (!checker.hasPermission()) {
		System.out.println("\tFAILED!");
		System.out.println("hasPermission("+checker.toString(checker.getPostfixArr())+") returned the wrong result!");
		success = false;
	    }

	    checker = new PermissionChecker(null, null, " test= me ");
	    System.out.println("Output:\n" + checker.toString()+" ("+checker.hasPermission()+")");
	    if (!checker.toString().equals("test=me = testme=")) {
		System.out.println("\tFAILED!");
		System.out.println("Should have been:\n"+"test=me = testme=");
		success = false;
	    }
	    if (checker.hasPermission()) {
		System.out.println("\tFAILED!");
		System.out.println("hasPermission("+checker.toString(checker.getPostfixArr())+") returned the wrong result!");
		success = false;
	    }

	    checker = new PermissionChecker(null, null, " this should work=thisshouldwork");
	    System.out.println("Output:\n" + checker.toString()+" ("+checker.hasPermission()+")");
	    if (!checker.toString().equals("thisshouldwork=thisshouldwork = thisshouldworkthisshouldwork=")) {
		System.out.println("\tFAILED!");
		System.out.println("Should have been:\n"+"thisshouldwork=thisshouldwork = thisshouldworkthisshouldwork=");
		success = false;
	    }
	    if (!checker.hasPermission()) {
		System.out.println("\tFAILED!");
		System.out.println("hasPermission("+checker.toString(checker.getPostfixArr())+") returned the wrong result!");
		success = false;
	    }

	    checker = new PermissionChecker(null, null, "false|ab=true");
	    System.out.println("Output:\n" + checker.toString()+" ("+checker.hasPermission()+")");
	    if (!checker.toString().equals("false|ab=true = falseab|true=")) {
		System.out.println("\tFAILED!");
		System.out.println("Should have been:\n"+"false|ab=true = falseab|true=");
		success = false;
	    }
	    if (!checker.hasPermission()) {
		System.out.println("\tFAILED!");
		System.out.println("hasPermission("+checker.toString(checker.getPostfixArr())+") returned the wrong result!");
		success = false;
	    }

	    checker = new PermissionChecker(null, null, "false|(ab=true)");
	    System.out.println("Output:\n" + checker.toString()+" ("+checker.hasPermission()+")");
	    if (!checker.toString().equals("false|(ab=true) = falseabtrue=|")) {
		System.out.println("\tFAILED!");
		System.out.println("Should have been:\n"+"false|ab=true = falseab|true=");
		success = false;
	    }
	    if (checker.hasPermission()) {
		System.out.println("\tFAILED!");
		System.out.println("hasPermission("+checker.toString(checker.getPostfixArr())+") returned the wrong result!");
		success = false;
	    }

	    checker = new PermissionChecker(null, null, "false|(ab=ab)");
	    System.out.println("Output:\n" + checker.toString()+" ("+checker.hasPermission()+")");
	    if (!checker.toString().equals("false|(ab=ab) = falseabab=|")) {
		System.out.println("\tFAILED!");
		System.out.println("Should have been:\n"+"false|ab=true = falseab|true=");
		success = false;
	    }
	    if (!checker.hasPermission()) {
		System.out.println("\tFAILED!");
		System.out.println("hasPermission("+checker.toString(checker.getPostfixArr())+") returned the wrong result!");
		success = false;
	    }


	    if (success) {
		System.out.println("\n\tALL TESTS PASSED!");
	    } else {
		System.out.println("\n\tNOT ALL TESTS PASSED!");
	    }
	}
    }


    /**
     *	This variable represents a "false" BooleanFunction.
     */
    public static final BooleanFunction	FALSE_BOOLEAN_FUNCTION =
	new BooleanFunction(false);

    /**
     *	This variable represents a "true" BooleanFunction.
     */
    public static final BooleanFunction	TRUE_BOOLEAN_FUNCTION =
	new BooleanFunction(true);


    protected static final char POST_TRUE	= 't';
    protected static final char POST_FALSE	= 'f';
    protected static final char POST_TRUE_CAP	= 'T';
    protected static final char POST_FALSE_CAP	= 'F';

    public static final String TRUE		= "true";
    public static final String FALSE		= "false";

    // Function representation in postfix String
    public static final char FUNCTION_MARKER	= 'F';

    // Operator constants
    public static final char LEFT_PAREN		= '(';
    public static final char RIGHT_PAREN	= ')';
    public static final char EQUALS_OPERATOR	= '=';
    public static final char OR_OPERATOR	= '|';
    public static final char AND_OPERATOR	= '&';
    public static final char NOT_OPERATOR	= '!';
    public static final char LESS_THAN_OPERATOR	= '<';
    public static final char MORE_THAN_OPERATOR	= '>';
    public static final char MODULUS_OPERATOR	= '%';

    // The COMMA separates function arguments... not really an operator
    public static final char ARGUMENT_SEPARATOR	= ',';

    // Reserved operators, although not currently used...
/*
 *  These will be added eventually, but currently they are commented out to
 *  enable the AppServer AdminGUI to function correctly
 *
 *    public static final char AT_OPERATOR	= '@';
 *    public static final char POUND_OPERATOR	= '#';
 *    public static final char DOLLAR_OPERATOR	= '$';
 *    public static final char UP_OPERATOR	= '^';
 *    public static final char STAR_OPERATOR	= '*';
 *    public static final char TILDA_OPERATOR	= '~';
 */

    /**
     *	This holds the infix equation
     */
    private String _infixStr = null;

    /**
     *	This holds the postfix equation
     */
    private char _postfixArr[] = null;

    /**
     *	This is a Map of Class objects which are user-registered functions.
     */
    private static Map _functions = new HashMap();

    /**
     *	This List holds the actual Function objects that correspond to the 'F'
     *	markers in the postfix string.
     */
    private List _functionList = null;

    /**
     *	This List of functions maintains variableSubstitution Functions which
     *	happen out-of-order.  They will be pulled from this list as placed into
     *	the actual _functionList when the are encountered during the
     *	preProcessing.
     */
    private Stack _tmpFunctionStack = null;
    private LayoutElement _desc = null;
    private UIComponent _component = null;
}
