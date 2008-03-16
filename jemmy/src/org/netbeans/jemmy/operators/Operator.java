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

package org.netbeans.jemmy.operators;

import org.netbeans.jemmy.Action;
import org.netbeans.jemmy.ActionProducer;
import org.netbeans.jemmy.CharBindingMap;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.QueueTool.QueueAction;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

import org.netbeans.jemmy.util.DefaultVisualizer;
import org.netbeans.jemmy.util.MouseVisualizer;

import java.awt.Component;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.lang.reflect.InvocationTargetException;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Keeps all environment and low-level methods.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public abstract class Operator extends Object 
    implements Timeoutable, Outputable {

    /**
     * Identifier for a "class" property.
     * @see #getDump
     */
    public static final String CLASS_DPROP = "Class";

    /**
     * Identifier for a "toString" property.
     * @see #getDump
     */
    public static final String TO_STRING_DPROP = "toString";


    private static Vector operatorPkgs;

    private Timeouts timeouts;
    private TestOut output;
    private ClassReference codeDefiner;
    private int model;
    private CharBindingMap map;
    private ComponentVisualizer visualizer;
    private StringComparator comparator;
    private PathParser parser;
    private QueueTool queueTool;
    private boolean verification = false;
    private JemmyProperties properties;

    /**
     * Inits environment.
     */
    public Operator() {
	super();
	initEnvironment();
    }

    /**
     * Specifies an object to be used by default to prepare component.
     * Each new operator created after the method using will have
     * defined visualizer.
     * Default implementation is org.netbeans.jemmy.util.DefaultVisualizer class.
     * @param visualizer ComponentVisualizer implementation
     * @return previous value
     * @see #setVisualizer(Operator.ComponentVisualizer)
     * @see #getDefaultComponentVisualizer()
     * @see org.netbeans.jemmy.util.DefaultVisualizer
     */
    public static ComponentVisualizer setDefaultComponentVisualizer(ComponentVisualizer visualizer) {
	return((ComponentVisualizer)JemmyProperties.
	       setCurrentProperty("ComponentOperator.ComponentVisualizer", visualizer));
    }

    /**
     * Returns an object to be used by default to prepare component.
     * @return Object is used by default to prepare component
     * @see #getVisualizer()
     * @see #setDefaultComponentVisualizer(Operator.ComponentVisualizer)
     */
    public static ComponentVisualizer getDefaultComponentVisualizer() {
	return((ComponentVisualizer)JemmyProperties.
	       getCurrentProperty("ComponentOperator.ComponentVisualizer"));
    }

    /**
     * Defines string comparator to be assigned in constructor.
     * @param comparator the comparator to be used by default.
     * @return previous value.
     * @see #getDefaultStringComparator()
     * @see Operator.StringComparator
     */
    public static StringComparator setDefaultStringComparator(StringComparator comparator) {
	return((StringComparator)JemmyProperties.
	       setCurrentProperty("ComponentOperator.StringComparator", comparator));
    }

    /**
     * Returns string comparator used to init operators.
     * @return the comparator used by default.
     * @see #setDefaultStringComparator(Operator.StringComparator)
     * @see Operator.StringComparator
     */
    public static StringComparator getDefaultStringComparator() {
	return((StringComparator)JemmyProperties.
	       getCurrentProperty("ComponentOperator.StringComparator"));
    }

    /**
     * Specifies an object used for parsing of path-like strings.
     * @param parser the parser.
     * @return a previous value.
     * @see Operator.PathParser
     * @see #getDefaultPathParser
     */
    public static PathParser setDefaultPathParser(PathParser parser) {
	return((PathParser)JemmyProperties.
	       setCurrentProperty("ComponentOperator.PathParser", parser));
    }

    /**
     * Returns an object used for parsing of path-like strings.
     * @return a parser used by default.
     * @see Operator.PathParser
     * @see #setDefaultPathParser
     */
    public static PathParser getDefaultPathParser() {
	return((PathParser)JemmyProperties.
	       getCurrentProperty("ComponentOperator.PathParser"));
    }

    /**
     * Defines weither newly created operators should perform operation verifications by default.
     * @param verification a verification mode to be used by default.
     * @return a prevoius value.
     * @see #getDefaultVerification()
     * @see #setVerification(boolean)
     */
    public static boolean setDefaultVerification(boolean verification) {
	Boolean oldValue = (Boolean)(JemmyProperties.
				     setCurrentProperty("Operator.Verification", 
							verification ? Boolean.TRUE : Boolean.FALSE));
	return((oldValue != null) ? oldValue.booleanValue() : false);
    }

    /**
     * Says weither newly created operators perform operations verifications by default.
     * @return a verification mode used by default.
     * @see #setDefaultVerification(boolean)
     * @see #getVerification()
     */
    public static boolean getDefaultVerification() {
	return(((Boolean)(JemmyProperties.
			  getCurrentProperty("Operator.Verification"))).booleanValue());
    }

    /**
     * Compares caption (button text, window title, ...) with a sample text.
     * @param caption String to be compared with match. Method returns false, if parameter is null.
     * @param match Sample to compare with. Method returns true, if parameter is null.
     * @param ce Compare exactly. If true, text can be a substring of caption.
     * @param ccs Compare case sensitively. If true, both text and caption are 
     * converted to upper case before comparison.
     * @return true is the captions matched the match.
     * @see #isCaptionEqual
     * @deprecated use another methods with the same name.
     */
    public static boolean isCaptionEqual(String caption, String match, boolean ce, boolean ccs) {
	return(new DefaultStringComparator(ce, ccs).equals(caption, match));
    }

    /**
     * Compares caption (button text, window title, ...) with a sample text.
     * @param caption String to be compared with match
     * @param match Sample to compare with
     * @param comparator StringComparator instance.
     * @return true is the captions matched the match.
     * @see #isCaptionEqual
     */
    public static boolean isCaptionEqual(String caption, String match, StringComparator comparator) {
	return(comparator.equals(caption, match));
    }

    /**
     * Returns default mouse button mask. 
     * @return <code>InputEvent.BUTTON*_MASK</code> field value
     */
    public static int getDefaultMouseButton() {
	return(InputEvent.BUTTON1_MASK);
    }

    /**
     * Returns mask of mouse button which used to popup expanding. (InputEvent.BUTTON3_MASK)
     * @return <code>InputEvent.BUTTON*_MASK</code> field value
     */
    public static int getPopupMouseButton() {
	return(InputEvent.BUTTON3_MASK);
    }

    /**
     * Creates operator for component.
     * Tries to find class with "operator package"."class name"Operator name,
     * where "operator package" is a package from operator packages list,
     * and "class name" is the name of class or one of its superclasses.
     * @param comp Component to create operator for.
     * @return a new operator with default environment.
     * @see #addOperatorPackage(String)
     */
    public static ComponentOperator createOperator(Component comp) {
	//hack!
	try {
	    Class cclass = Class.forName("java.awt.Component");
	    Class compClass = comp.getClass();
	    ComponentOperator result;
	    do {
		if((result = createOperator(comp, compClass)) != null) {
		    return(result);
		}
	    } while(cclass.isAssignableFrom(compClass = compClass.getSuperclass()));
	} catch(ClassNotFoundException e) {
	}
	return(null);
    }

    /**
     * Adds package to the list of packages containing operators. <BR>
     * "org.netbeans.jemmy.operators" is in the list by default.
     * @param pkgName Package name.
     * @see #createOperator(Component)
     */
    public static void addOperatorPackage(String pkgName) {
	operatorPkgs.add(pkgName);
    }


    /**
     * Returns an operator containing default environment.
     * @return an empty operator (not having any component source)
     * having default environment.
     */
    public static Operator getEnvironmentOperator() {
	return(new NullOperator());
    }

    static {
        //init visualizer depending on OS:
        //Linux - new MouseVisualizer(MouseVisualizer.TOP, 0.5, 10, false)
        //solaris - new MouseVisualizer()
        //others - new DefaultVisualizer()
        String os = System.getProperty("os.name").toUpperCase();
        if       (os.startsWith("LINUX")) {
            setDefaultComponentVisualizer(new MouseVisualizer(MouseVisualizer.TOP, 0.5, 10, false));
        } else if(os.startsWith("SUNOS")) {
            setDefaultComponentVisualizer(new MouseVisualizer());
        } else {
            setDefaultComponentVisualizer(new DefaultVisualizer());
        }
	operatorPkgs = new Vector ();
	setDefaultStringComparator(new DefaultStringComparator(false, false));
	setDefaultPathParser(new DefaultPathParser("|"));
	addOperatorPackage("org.netbeans.jemmy.operators");
	setDefaultVerification(true);
    }

    /**
     * Returns object operator is used for.
     * @return an instance of java.awt.Component subclass
     * which this operator was created for.
     */
    public abstract Component getSource();

    ////////////////////////////////////////////////////////
    //Environment                                         //
    ////////////////////////////////////////////////////////

    /**
     * Returns QueueTool is used to work with queue.
     * @return a QueueTool.
     */
    public QueueTool getQueueTool() {
	return(queueTool);
    }

    /**
     * Copies all environment (output, timeouts,
     * visualizer) from another operator.
     * @param anotherOperator an operator to copy the environment to.
     */
    public void copyEnvironment(Operator anotherOperator) {
	setTimeouts(anotherOperator.getTimeouts());
	setOutput(anotherOperator.getOutput());
	setVisualizer(anotherOperator.getVisualizer());
	setComparator(anotherOperator.getComparator());
	setVerification(anotherOperator.getVerification());
        setCharBindingMap(anotherOperator.getCharBindingMap());
	setProperties(anotherOperator.getProperties());
    }

    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
	queueTool.setTimeouts(timeouts);
    }

    public Timeouts getTimeouts() {
	return(timeouts);
    }
    
    /**
     * Returns component visualizer.
     * Visualizer is used from from makeComponentVisible() method.
     * @return a visualizer assigned to this operator.
     * @see #getDefaultComponentVisualizer()
     * @see #setVisualizer(Operator.ComponentVisualizer)
     */
    public ComponentVisualizer getVisualizer() {
 	return(visualizer);
    }
    
    /**
     * Changes component visualizer.
     * Visualizer is used from from makeComponentVisible() method.
     * @param vo a visualizer to assign to this operator.
     * @see #setDefaultComponentVisualizer(Operator.ComponentVisualizer)
     * @see #getVisualizer()
     */
    public void setVisualizer(ComponentVisualizer vo) {
 	visualizer = vo;
    }

    /**
     * Returns a JemmyProperty object assigned to this operator.
     * @return a JemmyProperty object got from the top of property stack 
     * or from another operator by copyuing environment.
     * @see #setProperties
     */
    public JemmyProperties getProperties() {
	return(properties);
    }

    /**
     * Assigns a JemmyProperty object to this operator.
     * @param properties a properties to assign to this operator.
     * @return previously assigned properties.
     * @see #getProperties
     */
    public JemmyProperties setProperties(JemmyProperties properties) {
	JemmyProperties oldProperties = getProperties();
	this.properties = properties;
	return(oldProperties);
    }

    /**
     * Defines CharBindingMap.
     * @param map a CharBindingMap to use for keyboard operations.
     * @see org.netbeans.jemmy.CharBindingMap
     * @see org.netbeans.jemmy.JemmyProperties#setCurrentCharBindingMap(CharBindingMap)
     * @see #getCharBindingMap
     */
    public void setCharBindingMap(CharBindingMap map) {
	this.map = map;
    }

    /**
     * Returns CharBindingMap used for keyboard operations.
     * @return a map assigned to this object.
     * @see #setCharBindingMap
     */
    public CharBindingMap getCharBindingMap() {
	return(map);
    }

    public void setOutput(TestOut out) {
	output = out;
	queueTool.setOutput(output.createErrorOutput());
    }
    
    public TestOut getOutput() {
	return(output);
    }

    /**
     * Returns object which is used for string comparison.
     * @return a comparator assigned to this operator.
     * @see org.netbeans.jemmy.operators.Operator.StringComparator
     * @see org.netbeans.jemmy.operators.Operator.DefaultStringComparator
     * @see #setComparator
     */
    public StringComparator getComparator() {
	return(comparator);
    }

    /**
     * Defines object which is used for string comparison. 
     * @param comparator a comparator to use for string comparision.
     * @see org.netbeans.jemmy.operators.Operator.StringComparator
     * @see org.netbeans.jemmy.operators.Operator.DefaultStringComparator
     * @see #getComparator
     */
    public void setComparator(StringComparator comparator) {
	this.comparator = comparator;
    }

    /**
     * Returns object which is used for parsing of path-like strings.
     * @return a comparator assigned to this operator.
     * @see #setPathParser
     */
    public PathParser getPathParser() {
	return(parser);
    }

    /**
     * Specifies object which is used for parsing of path-like strings.
     * @param parser a parser to use for path parsing.
     * @see #getPathParser
     */
    public void setPathParser(PathParser parser) {
	this.parser = parser;
    }

    /**
     * Defines weither operator should perform operation verifications.
     * @param verification new value.
     * @return old value
     * @see #setDefaultVerification(boolean)
     * @see #getDefaultVerification()
     * @see #getVerification()
     */
    public boolean setVerification(boolean verification) {
	boolean oldValue = this.verification;
	this.verification = verification;
	return(oldValue);
    }

    /**
     * Says weither operator performs operation verifications.
     * @return old value
     * @see #setDefaultVerification(boolean)
     * @see #getDefaultVerification()
     * @see #setVerification(boolean)
     */
    public boolean getVerification() {
	return(verification);
    }

    ////////////////////////////////////////////////////////
    //Util                                                //
    ////////////////////////////////////////////////////////

    /**
     * Creates new array which has all elements from 
     * first array, except last element.
     * @param path an original array
     * @return new array
     */
    public String[] getParentPath(String path[]) {
        if(path.length > 1) {
            String[] ppath = new String[path.length - 1];
            for(int i = 0; i < ppath.length; i++) {
                ppath[i] = path[i];
            }
            return(ppath);
        } else {
            return(new String[0]);
        }
    }
    public ComponentChooser[] getParentPath(ComponentChooser path[]) {
        if(path.length > 1) {
            ComponentChooser[] ppath = new ComponentChooser[path.length - 1];
            for(int i = 0; i < ppath.length; i++) {
                ppath[i] = path[i];
            }
            return(ppath);
        } else {
            return(new ComponentChooser[0]);
        }
    }

    /**
     * Parses a string to a string array
     * using a PathParser assigned to this operator.
     * @param path an original string
     * @return created String array.
     */
    public String[] parseString(String path) {
        return(getPathParser().parse(path));
    }

    /**
     * Parses strings like "1|2|3" into arrays {"1", "2", "3"}.
     * @param path an original string
     * @param delim a delimiter string
     * @return created String array.
     */
    public String[] parseString(String path, String delim) {
        return(new DefaultPathParser(delim).parse(path));
    }

    /**
     * Returns key code to be pressed for character typing.
     * @param c Character to be typed.
     * @return a value of one of the <code>KeyEvent.VK_*</code> fields.
     * @see org.netbeans.jemmy.CharBindingMap
     */
    public int getCharKey(char c) {
	return(map.getCharKey(c));
    }

    /**
     * Returns modifiers mask for character typing.
     * @param c Character to be typed.
     * @return a combination of <code>InputEvent.*_MASK</code> fields.
     * @see org.netbeans.jemmy.CharBindingMap
     */
    public int getCharModifiers(char c) {
	return(map.getCharModifiers(c));
    }

    /**
     * Returns key codes to by pressed for characters typing.
     * @param c Characters to be typed.
     * @return an array of <code>KeyEvent.VK_*</code> values.
     * @see org.netbeans.jemmy.CharBindingMap
     */
    public int[] getCharsKeys(char[] c) {
	int[] result = new int[c.length];
	for(int i = 0; i < c.length; i++) {
	    result[i] = getCharKey(c[i]);
	}
	return(result);
    }

    /**
     * Returns modifiers masks for characters typing.
     * @param c Characters to be typed.
     * @return an array of a combination of <code>InputEvent.*_MASK</code> fields.
     * @see org.netbeans.jemmy.CharBindingMap
     */
    public int[] getCharsModifiers(char[] c) {
	int[] result = new int[c.length];
	for(int i = 0; i < c.length; i++) {
	    result[i] = getCharModifiers(c[i]);
	}
	return(result);
    }

    /**
     * Returns key codes to by pressed for the string typing.
     * @param s String to be typed.
     * @return an array of <code>KeyEvent.VK_*</code> values.
     * @see org.netbeans.jemmy.CharBindingMap
     */
    public int[] getCharsKeys(String s) {
	return(getCharsKeys(s.toCharArray()));
    }

    /**
     * Returns modifiers masks for the string typing.
     * @param s String to be typed.
     * @return an array of a combination of <code>InputEvent.*_MASK</code> fields.
     * @see org.netbeans.jemmy.CharBindingMap
     */
    public int[] getCharsModifiers(String s) {
	return(getCharsModifiers(s.toCharArray()));
    }

    /**
     * Compares string using getComparator StringComparator.
     * @param caption a caption
     * @param match a pattern
     * @return true if <code>caption</code> and <code>match</code> match
     * @see #isCaptionEqual
     */
    public boolean isCaptionEqual(String caption, String match) {
	return(comparator.equals(caption, match));
    }

    /**
     * Prints component information into operator output.
     */
    public void printDump() {
	Hashtable result = getDump();
	Object[] keys = result.keySet().toArray();
	for(int i = 0; i < result.size(); i++) {
	    output.printLine((String)keys[i] + 
			     " = " + 
			     (String)result.get(keys[i]));
	}
    }

    /**
     * Returns information about component.
     * All records marked by simbolic constants defined in
     * public static final <code>*_DPROP</code> fields for
     * each operator type.
     * @return a Hashtable containing name-value pairs.
     */
    public Hashtable getDump() {
	Hashtable result = new Hashtable();
	result.put(CLASS_DPROP, getSource().getClass().getName());
        result.put(TO_STRING_DPROP, getSource().toString());
	return(result);
    }

    /**
     * Waits a state specified by a ComponentChooser instance.
     * @param state a ComponentChooser defining the state criteria.
     * @throws TimeoutExpiredException if the state has not
     * achieved in a value defined by <code>"ComponentOperator.WaitStateTimeout"</code>
     */
    public void waitState(final ComponentChooser state) {
	Waiter stateWaiter = new Waiter(new Waitable() {
		public Object actionProduced(Object obj) {
		    return(state.checkComponent(getSource()) ?
			   "" : null);
		}
		public String getDescription() {
		    return("Wait \"" + state.getDescription() + 
			   "\" state to be reached");
		}
	    });
	stateWaiter.setTimeouts(getTimeouts().cloneThis());
	stateWaiter.getTimeouts().
	    setTimeout("Waiter.WaitingTime",
		       getTimeouts().
		       getTimeout("ComponentOperator.WaitStateTimeout"));
	stateWaiter.setOutput(getOutput().createErrorOutput());
	try {
	    stateWaiter.waitAction(null);
	} catch(InterruptedException e) {
	    throw(new JemmyException("Waiting of \"" + state.getDescription() +
				     "\" state has been interrupted!"));
	}
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //
    ////////////////////////////////////////////////////////

    /**
     * Performs an operation with time control.
     * @param action an action to execute.
     * @param param an action parameters.
     * @param wholeTime a time for the action to be finished.
     * @return an action result.
     */
    protected Object produceTimeRestricted(Action action, final Object param, 
					   long wholeTime) {
	ActionProducer producer = new ActionProducer(action);
	producer.setOutput(getOutput().createErrorOutput());
	producer.setTimeouts(getTimeouts().cloneThis());
	producer.getTimeouts().setTimeout("ActionProducer.MaxActionTime", wholeTime);
	try {
            Object result = producer.produceAction(param);
            Throwable exception = producer.getException();
            if(exception != null) {
                if(exception instanceof JemmyException) {
                    throw((JemmyException)exception);
                } else {
                    throw(new JemmyException("Exception during " + action.getDescription(),
                                             exception));
                }
            }
	    return(result);
	} catch(InterruptedException e) {
	    throw(new JemmyException("Interrupted!", e));
	}
    }

    /**
     * Performs an operation with time control.
     * @param action an action to execute.
     * @param wholeTime a time for the action to be finished.
     * @return an action result.
     */
    protected Object produceTimeRestricted(Action action, long wholeTime) {
	return(produceTimeRestricted(action, null, wholeTime));
    }

    /**
     * Performs an operation without time control.
     * @param action an action to execute.
     * @param param an action parameters.
     */
    protected void produceNoBlocking(NoBlockingAction action, Object param) {
	try {
	    ActionProducer noBlockingProducer = new ActionProducer(action, false);
	    noBlockingProducer.setOutput(output.createErrorOutput());
	    noBlockingProducer.setTimeouts(timeouts);
	    noBlockingProducer.produceAction(param);
	} catch(InterruptedException e) {
	    throw(new JemmyException("Exception during \"" + 
				     action.getDescription() +
				     "\" execution",
				     e));
	}
	if(action.exception != null) {
	    throw(new JemmyException("Exception during nonblocking \"" +
				     action.getDescription() + "\"",
				     action.exception));
	}
    }

    /**
     * Performs an operation without time control.
     * @param action an action to execute.
     */
    protected void produceNoBlocking(NoBlockingAction action) {
	produceNoBlocking(action, null);
    }

    /**
     * Equivalent to <code>getQueue().lock();</code>.
     */
    protected void lockQueue() {
	queueTool.lock();
    }

    /**
     * Equivalent to <code>getQueue().unlock();</code>.
     */
    protected void unlockQueue() {
	queueTool.unlock();
    }

    /**
     * Unlocks Queue and then throw exception.
     * @param e an exception to be thrown.
     */
    protected void unlockAndThrow(Exception e) {
	unlockQueue();
	throw(new JemmyException("Exception during queue locking", e));
    }

    /**
     * To map nonprimitive type component's method.
     * @param action a mapping action.
     * @return an action result.
     * @see Operator.MapAction
     */
    protected Object runMapping(MapAction action) {
	return(runMappingPrimitive(action));
    }

    /**
     * To map char component's method.
     * @param action a mapping action.
     * @return an action result.
     * @see #runMapping(Operator.MapAction)
     * @see Operator.MapCharacterAction
     */
    protected char runMapping(MapCharacterAction action) {
	return(((Character)runMappingPrimitive(action)).charValue());
    }

    /**
     * To map byte component's method.
     * @param action a mapping action.
     * @return an action result.
     * @see #runMapping(Operator.MapAction)
     * @see Operator.MapByteAction
     */
    protected byte runMapping(MapByteAction action) {
	return(((Byte)runMappingPrimitive(action)).byteValue());
    }

    /**
     * To map int component's method.
     * @param action a mapping action.
     * @return an action result.
     * @see #runMapping(Operator.MapAction)
     * @see Operator.MapIntegerAction
     */
    protected int runMapping(MapIntegerAction action) {
	return(((Integer)runMappingPrimitive(action)).intValue());
    }

    /**
     * To map long component's method.
     * @param action a mapping action.
     * @return an action result.
     * @see #runMapping(Operator.MapAction)
     * @see Operator.MapLongAction
     */
    protected long runMapping(MapLongAction action) {
	return(((Long)runMappingPrimitive(action)).longValue());
    }

    /**
     * To map float component's method.
     * @param action a mapping action.
     * @return an action result.
     * @see #runMapping(Operator.MapAction)
     * @see Operator.MapFloatAction
     */
    protected float runMapping(MapFloatAction action) {
	return(((Float)runMappingPrimitive(action)).floatValue());
    }

    /**
     * To map double component's method.
     * @param action a mapping action.
     * @return an action result.
     * @see #runMapping(Operator.MapAction)
     * @see Operator.MapDoubleAction
     */
    protected double runMapping(MapDoubleAction action) {
	return(((Double)runMappingPrimitive(action)).doubleValue());
    }

    /**
     * To map boolean component's method.
     * @param action a mapping action.
     * @return an action result.
     * @see #runMapping(Operator.MapAction)
     * @see Operator.MapBooleanAction
     */
    protected boolean runMapping(MapBooleanAction action) {
	return(((Boolean)runMappingPrimitive(action)).booleanValue());
    }

    /**
     * To map void component's method.
     * @param action a mapping action.
     * @see #runMapping(Operator.MapAction)
     * @see Operator.MapVoidAction
     */
    protected void runMapping(MapVoidAction action) {
	runMappingPrimitive(action);
    }

    /**
     * Adds array of objects to dump hashtable.
     * Is used for multiple properties such as list items and tree nodes.
     * @param table a table to add properties to.
     * @param title property names prefix. Property names are constructed by
     * adding a number to the prefix: 
     * <code>title + "_" + Iteger.toString("ordinal index")</code>
     * @param items an array of property values.
     * @return an array of property names (with added numbers).
     */
    protected String[] addToDump(Hashtable table, String title, Object[] items) {
	String[] names = createNames(title + "_", items.length);
	for(int i = 0; i < items.length; i++) {
	    table.put(names[i], items[i].toString());
	}
	return(names);
    }

    /**
     * Adds two dimentional array of objects to dump hashtable.
     * Is used for multiple properties such as table cells.
     * @param table a table to add properties to.
     * @param title property names prefix. Property names are constructed by
     * adding two numbers to the prefix: 
     * <code>title + "_" + Iteger.toString("row index") + "_" + Iteger.toString("column index")</code>
     * @param items an array of property values.
     * @return an array of property names (with added numbers).
     */
    protected String[] addToDump(Hashtable table, String title, Object[][] items) {
	String[] names = createNames(title + "_", items.length);
	for(int i = 0; i < items.length; i++) {
	    addToDump(table, names[i], items[i]);
	}
	return(names);
    }
    ////////////////////////////////////////////////////////
    //Private                                             //
    ////////////////////////////////////////////////////////

    private Object runMappingPrimitive(QueueTool.QueueAction action) {
        return(queueTool.invokeSmoothly(action));
    }

    private String[] createNames(String title, int count) {
	String[] result = new String[count];
	int indexLength = Integer.toString(count).length();
	String zeroString = "";
	for(int i = 0; i < indexLength; i++) {
	    zeroString = zeroString + "0";
	}
	String indexString;
	for(int i = 0; i < count; i++) {
	    indexString = Integer.toString(i);
	    result[i] = title + 
		zeroString.substring(0, indexLength - indexString.length()) + 
		indexString;
	}
	return(result);
    }

    private static ComponentOperator createOperator(Component comp, Class compClass) {
	StringTokenizer token = new StringTokenizer(compClass.getName(), ".");
	String className = "";
	while(token.hasMoreTokens()) {
	    className = token.nextToken();
	}
	Object[] params = {comp};
	Class[] param_classes = {compClass};
	String operatorPackage;
	for(int i = 0; i < operatorPkgs.size(); i++) {
	    operatorPackage = (String)operatorPkgs.get(i);
	    try {
		return((ComponentOperator)
		       new ClassReference(operatorPackage + "." +
					  className + "Operator").
		       newInstance(params, param_classes));
	    } catch(ClassNotFoundException e) {
	    } catch(InvocationTargetException e) {
	    } catch(NoSuchMethodException e) {
	    } catch(IllegalAccessException e) {
	    } catch(InstantiationException e) {
	    }
	}
	return(null);
    }

    private void initEnvironment() {
	try {
	    codeDefiner = new ClassReference("java.awt.event.KeyEvent");
	} catch(ClassNotFoundException e) {
	}
	queueTool = new QueueTool();
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
	setCharBindingMap(JemmyProperties.getProperties().getCharBindingMap());
	setVisualizer(getDefaultComponentVisualizer());
	setComparator(getDefaultStringComparator());
	setVerification(getDefaultVerification());
	setProperties(JemmyProperties.getProperties());
	setPathParser(getDefaultPathParser());
    }

    private int nextDelimIndex(String path, String delim) {
	String restPath = path;
	int ind = 0;
	while((ind = restPath.indexOf(delim)) != -1) {
	    if(ind == 0 ||
	       restPath.substring(ind - 1, ind) != "\\") {
		return(ind);
	    }
	}
	return(-1);
    }

    /**
     * Returns toString() result from component of this operator. It calls
     * {@link #getSource}.toString() in dispatch thread.
     * @return toString() result from component of this operator.
     */
    public String toStringSource() {
        return (String)runMapping(new MapAction("getSource().toString()") {
            public Object map() {
                return getSource().toString();
            }
        });
    }

    /**
     * Interface used to make component visible & ready to to make operations with.
     */
    public interface ComponentVisualizer {
	/**
	 * Prepares component for a user input.
	 * @param compOper Operator asking for necessary actions.
	 */
	public void makeVisible(ComponentOperator compOper);
    }


    /**
     * Interface to compare string resources like labels, button text, ...
     * with match. <BR>
     */
    public interface StringComparator {
	/**
	 * Imlementation must return true if strings are equal.
         * @param caption a text to compare with pattern.
         * @param match a pattern
         * @return true if text and pattern matches.
	 */
	public boolean equals(String caption, String match);
    }

    /**
     * Default StringComparator implementation.
     */
    public static class DefaultStringComparator implements StringComparator {
	boolean ce;
	boolean ccs;

	/**
         * Constructs a DefaultStringComparator object.
	 * @param ce Compare exactly. If true, text can be a substring of caption.
	 * @param ccs Compare case sensitively. If true, both text and caption are 
	 */
	public DefaultStringComparator(boolean ce, boolean ccs) {
	    this.ce = ce;
	    this.ccs = ccs;
	}

	/**	
         * Compares a caption with a match using switched passed into constructor.
	 * @param caption String to be compared with match. Method returns false, if parameter is null.
	 * @param match Sample to compare with. Method returns true, if parameter is null.
         * @return true if text and pattern matches.
	 */
	public boolean equals(String caption, String match) {
	    if(match == null) {
		return(true);
	    }
	    if(caption == null) {
		return(false);
	    }
	    String c, t;
	    if(!ccs) {
		c = caption.toUpperCase();
		t = match.toUpperCase();
	    } else {
		c = caption;
		t = match;
	    }
	    if(ce) {
		return(c.equals(t));
	    } else {
		return(c.indexOf(t) != -1);
	    }
	}
    }

    /**
     * Used for parsing of path-like strings.
     */
    public interface PathParser {
        /**
         * Parses a string to a String array.
         * @param path a String to parse.
         * @return a parsed array.
         */
	public String[] parse(String path);
    }

    /**
     * Used for parsing of path-like strings where path components are
     * separated by a string-separator: "drive|directory|subdirectory|file".
     */
    public static class DefaultPathParser implements PathParser {
        String separator;
        /**
         * Constructs a DefaultPathParser object.
         * @param separator a string used as separator.
         */
        public DefaultPathParser(String separator) {
            this.separator = separator;
        }
 	public String[] parse(String path) {
            if(path.length() > 0) {
                Vector parsed = new Vector();
                int position = 0;
                int sepIndex = 0;
                while((sepIndex = path.indexOf(separator, position)) != -1) {
                    parsed.add(path.substring(position, sepIndex));
                    position = sepIndex + separator.length();
                }
                parsed.add(path.substring(position));
                String[] result = new String[parsed.size()];
                for(int i = 0; i < parsed.size(); i++) {
                    result[i] = (String)parsed.get(i);
                }
                return(result);
            } else {
                return(new String[0]);
            }
        }
    }

    /**
     * Allows to bind a compponent by a component type.
     */
    public static class Finder implements ComponentChooser {
        Class clz;
        ComponentChooser subchooser;
        /**
         * Constructs Finder.
         * @param clz a component class.
         * @param subchooser other searching criteria.
         */
        public Finder(Class clz, ComponentChooser subchooser) {
            this.clz = clz;
            this.subchooser = subchooser;
        }
        /**
         * Constructs Finder.
         * @param clz a component class.
         */
        public Finder(Class clz) {
            this(clz, ComponentSearcher.getTrueChooser("Any " + clz.getName()));
        }
        public boolean checkComponent(Component comp) {
            if(clz.isInstance(comp)) {
                return(subchooser.checkComponent(comp));
            }
            return(false);
        }
        public String getDescription() {
            return(subchooser.getDescription());
        }
    }

    /**
     * Can be used to make nonblocking operation implementation.
     * Typical scenario is: <BR>
     *	produceNoBlocking(new NoBlockingAction("Button pushing") {<BR>
     *		public Object doAction(Object param) {<BR>
     *		    push();<BR>
     *		    return(null);<BR>
     *		}<BR>
     *	    });<BR>
     */
    protected abstract class NoBlockingAction implements Action {
	String description;
	Exception exception;
	boolean finished;
        /**
         * Constructs a NoBlockingAction object.
         * @param description an action description.
         */
        public NoBlockingAction(String description) {
	    this.description = description;
	    exception = null;
	    finished = false;
	}
	public final Object launch(Object param) {
	    Object result = null;
	    try {
		result = doAction(param);
	    } catch(Exception e) {
		exception = e;
	    }
	    finished = true;
	    return(result);
	}
        /**
         * Performs a mapping action.
         * @param param an action parameter.
         * @return an action result.
         */
	public abstract Object doAction(Object param);
	public String getDescription() {
	    return(description);
	}
        /**
         * Specifies the exception.
         * @param e an exception.
         * @see #getException
         */
	protected void setException(Exception e) {
	    exception = e;
	}
        /**
         * Returns an exception occured diring the action execution.
         * @return an exception.
         * @see #setException
         */
	public Exception getException() {
	    return(exception);
	}
    }

    /**
     * Can be used to simplify nonprimitive type component's methods mapping.
     * Like this: <BR>
     * public Color getBackground() { <BR>
     *     return((Color)runMapping(new MapAction("getBackground") { <BR>
     *         public Object map() { <BR>
     *             return(((Component)getSource()).getBackground()); <BR>
     *         } <BR>
     *     })); <BR>
     * } <BR>
     * @see #runMapping(Operator.MapAction)
     */
    protected abstract class MapAction extends QueueTool.QueueAction {
        /**
         * Constructs a MapAction object.
         * @param description an action description.
         */
	public MapAction(String description) {
	    super(description);
	}
	public final Object launch() throws Exception {
	    return(map());
	}
        /**
         * Executes a map action.
         * @return an action result.
         * @throws Exception 
         */
	public abstract Object map() throws Exception;
    }

    /**
     * Can be used to simplify char component's methods mapping.
     * @see #runMapping(Operator.MapCharacterAction)
     */
    protected abstract class MapCharacterAction extends QueueTool.QueueAction {
        /**
         * Constructs a MapCharacterAction object.
         * @param description an action description.
         */
	public MapCharacterAction(String description) {
	    super(description);
	}
	public final Object launch() throws Exception {
	    return(new Character(map()));
	}
        /**
         * Executes a map action.
         * @return an action result.
         * @throws Exception 
         */
	public abstract char map() throws Exception;
    }

    /**
     * Can be used to simplify byte component's methods mapping.
     * @see #runMapping(Operator.MapByteAction)
     */
    protected abstract class MapByteAction extends QueueTool.QueueAction {
        /**
         * Constructs a MapByteAction object.
         * @param description an action description.
         */
	public MapByteAction(String description) {
	    super(description);
	}
	public final Object launch() throws Exception {
	    return(new Byte(map()));
	}
        /**
         * Executes a map action.
         * @return an action result.
         * @throws Exception 
         */
	public abstract byte map() throws Exception;
    }

    /**
     * Can be used to simplify int component's methods mapping.
     * @see #runMapping(Operator.MapIntegerAction)
     */
    protected abstract class MapIntegerAction extends QueueTool.QueueAction {
        /**
         * Constructs a MapIntegerAction object.
         * @param description an action description.
         */
	public MapIntegerAction(String description) {
	    super(description);
	}
	public final Object launch() throws Exception {
	    return(new Integer(map()));
	}
        /**
         * Executes a map action.
         * @return an action result.
         * @throws Exception 
         */
	public abstract int map() throws Exception;
    }

    /**
     * Can be used to simplify long component's methods mapping.
     * @see #runMapping(Operator.MapLongAction)
     */
    protected abstract class MapLongAction extends QueueTool.QueueAction {
        /**
         * Constructs a MapLongAction object.
         * @param description an action description.
         */
	public MapLongAction(String description) {
	    super(description);
	}
	public final Object launch() throws Exception {
	    return(new Long(map()));
	}
        /**
         * Executes a map action.
         * @return an action result.
         * @throws Exception 
         */
	public abstract long map() throws Exception;
    }

    /**
     * Can be used to simplify float component's methods mapping.
     * @see #runMapping(Operator.MapFloatAction)
     */
    protected abstract class MapFloatAction extends QueueTool.QueueAction {
        /**
         * Constructs a MapFloatAction object.
         * @param description an action description.
         */
	public MapFloatAction(String description) {
	    super(description);
	}
	public final Object launch() throws Exception {
	    return(new Float(map()));
	}
        /**
         * Executes a map action.
         * @return an action result.
         * @throws Exception 
         */
	public abstract float map() throws Exception;
    }

    /**
     * Can be used to simplify double component's methods mapping.
     * @see #runMapping(Operator.MapDoubleAction)
     */
    protected abstract class MapDoubleAction extends QueueTool.QueueAction {
        /**
         * Constructs a MapDoubleAction object.
         * @param description an action description.
         */
	public MapDoubleAction(String description) {
	    super(description);
	}
	public final Object launch() throws Exception {
	    return(new Double(map()));
	}
        /**
         * Executes a map action.
         * @return an action result.
         * @throws Exception 
         */
	public abstract double map() throws Exception;
    }

    /**
     * Can be used to simplify boolean component's methods mapping.
     * @see #runMapping(Operator.MapBooleanAction)
     */
    protected abstract class MapBooleanAction extends QueueTool.QueueAction {
        /**
         * Constructs a MapBooleanAction object.
         * @param description an action description.
         */
	public MapBooleanAction(String description) {
	    super(description);
	}
	public final Object launch() throws Exception {
	    return(map() ? Boolean.TRUE : Boolean.FALSE);
	}
        /**
         * Executes a map action.
         * @return an action result.
         * @throws Exception 
         */
	public abstract boolean map() throws Exception;
    }

    /**
     * Can be used to simplify void component's methods mapping.
     * @see #runMapping(Operator.MapVoidAction)
     */
    protected abstract class MapVoidAction extends QueueTool.QueueAction {
        /**
         * Constructs a MapVoidAction object.
         * @param description an action description.
         */
	public MapVoidAction(String description) {
	    super(description);
	}
	public final Object launch() throws Exception {
	    map();
	    return(null);
	}
        /**
         * Executes a map action.
         * @throws Exception 
         */
	public abstract void map() throws Exception;
    }

    private static class NullOperator extends Operator {
	public NullOperator() {
	    super();
	}
	public Component getSource() {
	    return(null);
	}
    }
}


