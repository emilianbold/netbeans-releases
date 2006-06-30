/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.operators;

import java.awt.Component;
import java.awt.Container;

import java.text.ParseException;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import javax.swing.event.ChangeListener;

import javax.swing.plaf.SpinnerUI;

import org.netbeans.jemmy.Action;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.ScrollDriver;

import org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster;

/**
 * Provides methods to work with <code>javax.swing.JSpinner</code> component <br>
 *
 * @see NumberSpinnerOperator
 * @see ListSpinnerOperator
 * @see DateSpinnerOperator
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JSpinnerOperator extends JComponentOperator 
    implements Timeoutable, Outputable {

    /**
     * Identifier for a "value" property.
     * @see #getDump
     */
    public static final String VALUE_DPROP = "Value";

    private final static long WHOLE_SCROLL_TIMEOUT = 60000;

    private Timeouts timeouts;
    private TestOut output;

    private ScrollDriver driver;

    private JButtonOperator increaseOperator = null;
    private JButtonOperator decreaseOperator = null;

    /**
     * Constructor.
     * @param b JSpinner component.
     */
    public JSpinnerOperator(JSpinner b) {
	super(b);
	driver = DriverManager.getScrollDriver(getClass());
    }

    /**
     * Constructs a JSpinnerOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     * @throws TimeoutExpiredException
     */
    public JSpinnerOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JSpinner)cont.
             waitSubComponent(new JSpinnerFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JSpinnerOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @throws TimeoutExpiredException
     */
    public JSpinnerOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructs a JSpinnerOperator object.
     * @param cont The operator for a container containing the sought for button.
     * @param text toString() representation of the current spinner value. 
     * @param index Ordinal component index. The first component has <code>index</code> 0.
     * @throws TimeoutExpiredException
     */
    public JSpinnerOperator(ContainerOperator cont, String text, int index) {
	this((JSpinner)waitComponent(cont, 
                                     new JSpinnerByTextFinder(text, 
                                                              cont.getComparator()),
                                     index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JSpinnerOperator object.
     * @param cont The operator for a container containing the sought for button.
     * @param text toString() representation of the current spinner value. 
     * @throws TimeoutExpiredException
     */
    public JSpinnerOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JSpinnerOperator(ContainerOperator cont, int index) {
	this((JSpinner)waitComponent(cont, 
				    new JSpinnerFinder(), 
				    index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @throws TimeoutExpiredException
     */
    public JSpinnerOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JSpinner in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JSpinner instance or null if component was not found.
     */
    public static JSpinner findJSpinner(Container cont, ComponentChooser chooser, int index) {
	return((JSpinner)findComponent(cont, new JSpinnerFinder(chooser), index));
    }

    /**
     * Searches 0'th JSpinner in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JSpinner instance or null if component was not found.
     */
    public static JSpinner findJSpinner(Container cont, ComponentChooser chooser) {
	return(findJSpinner(cont, chooser, 0));
    }

    /**
     * Searches JSpinner in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JSpinner instance or null if component was not found.
     */
    public static JSpinner findJSpinner(Container cont, int index) {
	return(findJSpinner(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JSpinner instance"), index));
    }

    /**
     * Searches 0'th JSpinner in container.
     * @param cont Container to search component in.
     * @return JSpinner instance or null if component was not found.
     */
    public static JSpinner findJSpinner(Container cont) {
	return(findJSpinner(cont, 0));
    }

    /**
     * Waits JSpinner in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JSpinner instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JSpinner waitJSpinner(Container cont, ComponentChooser chooser, int index)  {
	return((JSpinner)waitComponent(cont, new JSpinnerFinder(chooser), index));
    }

    /**
     * Waits 0'th JSpinner in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JSpinner instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JSpinner waitJSpinner(Container cont, ComponentChooser chooser) {
	return(waitJSpinner(cont, chooser, 0));
    }

    /**
     * Waits JSpinner in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JSpinner instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JSpinner waitJSpinner(Container cont, int index)  {
	return(waitJSpinner(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JSpinner instance"), index));
    }

    /**
     * Waits 0'th JSpinner in container.
     * @param cont Container to search component in.
     * @return JSpinner instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JSpinner waitJSpinner(Container cont) {
	return(waitJSpinner(cont, 0));
    }

    /**
     * Checks operator's model type.
     * @param oper an operator to check model
     * @param modelClass a model class.
     * @throws SpinnerModelException if an operator's model is not an instance of
     * specified class.
     */
    public static void checkModel(JSpinnerOperator oper, Class modelClass) {
        if(!modelClass.isInstance(oper.getModel())) {
            throw(new SpinnerModelException("JSpinner model is not a " + modelClass.getName(),
                                            oper.getSource()));
        }
    }

    static {
	Timeouts.initDefault("JSpinnerOperator.WholeScrollTimeout", WHOLE_SCROLL_TIMEOUT);
    }

    public void setOutput(TestOut out) {
	output = out;
	super.setOutput(output.createErrorOutput());
    }

    public TestOut getOutput() {
	return(output);
    }

    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
	super.setTimeouts(timeouts);
    }

    public Timeouts getTimeouts() {
	return(timeouts);
    }

    /**
     * Returns an instance of <code>NumberSpinnerOperator</code> operator,
     * the operator used for <code>JSpinner</code> having <code>SpinnerNumberModel</code> model.
     * @return a <code>NumberSpinnerOperator</code> created for the same <code>JSpinner</code> as this operator.
     * @throws SpinnerModelException if an operator's model is not an instance of <code>SpinnerNumberModel</code>
     */
    public NumberSpinnerOperator getNumberSpinner() {
        return(new NumberSpinnerOperator(this));
    }

    /**
     * Returns an instance of <code>ListSpinnerOperator</code> operator,
     * the operator used for <code>JSpinner</code> having <code>SpinnerListModel</code> model.
     * @return a <code>ListSpinnerOperator</code> created for the same <code>JSpinner</code> as this operator.
     * @throws SpinnerModelException if an operator's model is not an instance of <code>SpinnerListModel</code>
     */
    public ListSpinnerOperator getListSpinner() {
        return(new ListSpinnerOperator(this));
    }

    /**
     * Returns an instance of <code>DateSpinnerOperator</code> operator,
     * the operator used for <code>JSpinner</code> having <code>SpinnerDateModel</code> model.
     * @return a <code>DateSpinnerOperator</code> created for the same <code>JSpinner</code> as this operator.
     * @throws SpinnerModelException if an operator's model is not an instance of <code>SpinnerDateModel</code>
     */
    public DateSpinnerOperator getDateSpinner() {
        return(new DateSpinnerOperator(this));
    }

    /**
     * Scrolls to reach a condition specified by <code>ScrollAdjuster</code>
     * @param adj scrolling criteria.
     */
    public void scrollTo(final ScrollAdjuster adj) {
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.scroll(JSpinnerOperator.this, adj);
		    return(null);
		}
		public String getDescription() {
		    return("Scrolling");
		}
	    }, getTimeouts().getTimeout("JSpinnerOperator.WholeScrollTimeout"));
    }

    /**
     * Scrolls to maximum value.
     * @throws SpinnerModelException if an operator's model does not have a maximum value.
     */
    public void scrollToMaximum() {
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.scrollToMaximum(JSpinnerOperator.this, SwingConstants.VERTICAL);
		    return(null);
		}
		public String getDescription() {
		    return("Scrolling");
		}
	    }, getTimeouts().getTimeout("JSpinnerOperator.WholeScrollTimeout"));
    }

    /**
     * Scrolls to minimum value.
     * @throws SpinnerModelException if an operator's model does not have a minimum value.
     */
    public void scrollToMinimum() {
	produceTimeRestricted(new Action() {
		public Object launch(Object obj) {
		    driver.scrollToMinimum(JSpinnerOperator.this, SwingConstants.VERTICAL);
		    return(null);
		}
		public String getDescription() {
		    return("Scrolling");
		}
	    }, getTimeouts().getTimeout("JSpinnerOperator.WholeScrollTimeout"));
    }

    /**
     * Scrolls to exact match of a spinner value to the specified value.
     * @param value an value to scroll to.
     * @param direction a scrolling direction - one of <code>ScrollAdjuster.*_SCROLL_DIRECTION</code> fields.
     */
    public void scrollToObject(Object value, int direction) {
        scrollTo(new ExactScrollAdjuster(this, value, direction));
    }

    /**
     * Scrolls to matching of <code>getValue().toString() with the pattern.
     * @param pattern a pattern to compare with
     * @param comparator a string comparision criteria
     * @param direction a scrolling direction - one of <code>ScrollAdjuster.*_SCROLL_DIRECTION</code> fields.
     */
    public void scrollToString(String pattern, StringComparator comparator, int direction) {
        scrollTo(new ToStringScrollAdjuster(this, pattern, comparator, direction));
    }

    /**
     * Scrolls to matching of <code>getValue().toString()</code> with the pattern.
     * Uses <code>StringComparator</code> assigned to the operator.
     * @param pattern a pattern to compare with
     * @param direction a scrolling direction - one of <code>ScrollAdjuster.*_SCROLL_DIRECTION</code> fields.
     */
    public void scrollToString(String pattern, int direction) {
        scrollToString(pattern, getComparator(), direction);
    }

    /**
     * Returns an operator for a button used for value increasing.
     * @return an operator for a first <code>JButton<code> inside this spinner.
     */
    public JButtonOperator getIncreaseOperator() {
        if(increaseOperator == null) {
            increaseOperator = (JButtonOperator)createSubOperator(new JButtonOperator.JButtonFinder(), 0);
            increaseOperator.copyEnvironment(this);
            increaseOperator.setOutput(getOutput().createErrorOutput());
        }
        return(increaseOperator);
    }

    /**
     * Returns an operator for a button used for value decreasing.
     * @return an operator for a second <code>JButton<code> inside this spinner.
     */
    public JButtonOperator getDecreaseOperator() {
        if(decreaseOperator == null) {
            decreaseOperator = (JButtonOperator)createSubOperator(new JButtonOperator.JButtonFinder(), 1);
            decreaseOperator.copyEnvironment(this);
            decreaseOperator.setOutput(getOutput().createErrorOutput());
        }
        return(decreaseOperator);
    }

    /**
     * Returns a minimal value. Returns null if model is not
     * one of the following: 
     * <code>javax.swing.SpinnerDateModel</code>, 
     * <code>javax.swing.SpinnerListModel</code>, 
     * <code>javax.swing.SpinnerNumberModel</code>.
     * Also, returns null if the model does not have a minimal value.
     * @return a minimal value.
     */
    public Object getMinimum() {
        SpinnerModel model = getModel();
        if       (model instanceof SpinnerNumberModel) {
            return(((SpinnerNumberModel)model).getMinimum());
        } else if(model instanceof SpinnerDateModel) {
            return(((SpinnerDateModel)model).getEnd());
        } else if(model instanceof SpinnerListModel) {
            List list = ((SpinnerListModel)model).getList();
            return(list.get(list.size() - 1));
        } else {
            return(null);
        }
    }

    /**
     * Returns a maximal value. Returns null if model is not
     * one of the following: 
     * <code>javax.swing.SpinnerDateModel</code>, 
     * <code>javax.swing.SpinnerListModel</code>, 
     * <code>javax.swing.SpinnerNumberModel</code>.
     * Also, returns null if the model does not have a maximal value.
     * @return a maximal value.
     */
    public Object getMaximum() {
        SpinnerModel model = getModel();
        if       (model instanceof SpinnerNumberModel) {
            return(((SpinnerNumberModel)model).getMaximum());
        } else if(model instanceof SpinnerDateModel) {
            return(((SpinnerDateModel)model).getEnd());
        } else if(model instanceof SpinnerListModel) {
            List list = ((SpinnerListModel)model).getList();
            return(list.get(list.size() - 1));
        } else {
            return(null);
        }
    }

    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put(VALUE_DPROP, ((JSpinner)getSource()).getValue().toString());
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JSpinner.getValue()</code> through queue*/
    public Object getValue() {
	return((Object)runMapping(new MapAction("getValue") {
		public Object map() {
		    return(((JSpinner)getSource()).getValue());
		}}));}

    /**Maps <code>JSpinner.setValue(Object)</code> through queue*/
    public void setValue(final Object object) {
	runMapping(new MapVoidAction("setValue") {
		public void map() {
		    ((JSpinner)getSource()).setValue(object);
		}});}

    /**Maps <code>JSpinner.getUI()</code> through queue*/
    public SpinnerUI getUI() {
	return((SpinnerUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JSpinner)getSource()).getUI());
		}}));}

    /**Maps <code>JSpinner.setUI(SpinnerUI)</code> through queue*/
    public void setUI(final SpinnerUI spinnerUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JSpinner)getSource()).setUI(spinnerUI);
		}});}

    /**Maps <code>JSpinner.setModel(SpinnerModel)</code> through queue*/
    public void setModel(final SpinnerModel spinnerModel) {
	runMapping(new MapVoidAction("setModel") {
		public void map() {
		    ((JSpinner)getSource()).setModel(spinnerModel);
		}});}

    /**Maps <code>JSpinner.getModel()</code> through queue*/
    public SpinnerModel getModel() {
	return((SpinnerModel)runMapping(new MapAction("getModel") {
		public Object map() {
		    return(((JSpinner)getSource()).getModel());
		}}));}

    /**Maps <code>JSpinner.getNextValue()</code> through queue*/
    public Object getNextValue() {
	return((Object)runMapping(new MapAction("getNextValue") {
		public Object map() {
		    return(((JSpinner)getSource()).getNextValue());
		}}));}

    /**Maps <code>JSpinner.addChangeListener(ChangeListener)</code> through queue*/
    public void addChangeListener(final ChangeListener changeListener) {
	runMapping(new MapVoidAction("addChangeListener") {
		public void map() {
		    ((JSpinner)getSource()).addChangeListener(changeListener);
		}});}

    /**Maps <code>JSpinner.removeChangeListener(ChangeListener)</code> through queue*/
    public void removeChangeListener(final ChangeListener changeListener) {
	runMapping(new MapVoidAction("removeChangeListener") {
		public void map() {
		    ((JSpinner)getSource()).removeChangeListener(changeListener);
		}});}

    /**Maps <code>JSpinner.getChangeListeners()</code> through queue*/
    public ChangeListener[] getChangeListeners() {
	return((ChangeListener[])runMapping(new MapAction("getChangeListeners") {
		public Object map() {
		    return(((JSpinner)getSource()).getChangeListeners());
		}}));}

    /**Maps <code>JSpinner.getPreviousValue()</code> through queue*/
    public Object getPreviousValue() {
	return((Object)runMapping(new MapAction("getPreviousValue") {
		public Object map() {
		    return(((JSpinner)getSource()).getPreviousValue());
		}}));}

    /**Maps <code>JSpinner.setEditor(JComponent)</code> through queue*/
    public void setEditor(final JComponent jComponent) {
	runMapping(new MapVoidAction("setEditor") {
		public void map() {
		    ((JSpinner)getSource()).setEditor(jComponent);
		}});}

    /**Maps <code>JSpinner.getEditor()</code> through queue*/
    public JComponent getEditor() {
	return((JComponent)runMapping(new MapAction("getEditor") {
		public Object map() {
		    return(((JSpinner)getSource()).getEditor());
		}}));}

    /**Maps <code>JSpinner.commitEdit()</code> through queue*/
    public void commitEdit() {
	runMapping(new MapVoidAction("commitEdit") {
		public void map() throws ParseException {
		    ((JSpinner)getSource()).commitEdit();
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Allows to find component by text.
     */
    public static class JSpinnerByTextFinder implements ComponentChooser {
	String label;
	StringComparator comparator;

        /**
         * Constructs JSpinnerByTextFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public JSpinnerByTextFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}

        /**
         * Constructs JSpinnerByTextFinder.
         * @param lb a text pattern
         */
	public JSpinnerByTextFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}

	public boolean checkComponent(Component comp) {
	    if(comp instanceof JSpinner) {
		if(((JSpinner)comp).getValue() != null) {
		    return(comparator.equals(((JSpinner)comp).getValue().toString(),
					     label));
		}
	    }
	    return(false);
	}

	public String getDescription() {
	    return("JSpinner with text \"" + label + "\"");
	}
    }

    /**
     * Checks component type.
     */
    public static class JSpinnerFinder extends Finder {
        /**
         * Constructs JSpinnerFinder.
         * @param sf other searching criteria.
         */
	public JSpinnerFinder(ComponentChooser sf) {
            super(JSpinner.class, sf);
	}
        /**
         * Constructs JSpinnerFinder.
         */
	public JSpinnerFinder() {
            super(JSpinner.class);
	}
    }

    /**
     * A <code>ScrollAdjuster</code> to be used for <code>JSpinner</code>
     * component having <code>SpinnerNumberModel</code> model.
     * @see NumberSpinnerOperator
     */
    public static class NumberScrollAdjuster implements ScrollAdjuster {
        SpinnerNumberModel model;
        double value;
        /**
         * Constructs a <code>NumberScrollAdjuster</code> object.
         * @param oper an operator to work with.
         * @param value a value to scroll to.
         */
	public NumberScrollAdjuster(JSpinnerOperator oper, double value) {
	    this.value = value;
            checkModel(oper, SpinnerNumberModel.class);
            model = (SpinnerNumberModel)oper.getModel();
	}
        /**
         * Constructs a <code>NumberScrollAdjuster</code> object.
         * @param oper an operator to work with.
         * @param value a value to scroll to.
         */
	public NumberScrollAdjuster(JSpinnerOperator oper, Number value) {
	    this(oper, value.doubleValue());
	}
	public int getScrollDirection() {
            if       (value > model.getNumber().doubleValue()) {
                return(ScrollAdjuster.INCREASE_SCROLL_DIRECTION);
            } else if(value < model.getNumber().doubleValue()) {
                return(ScrollAdjuster.DECREASE_SCROLL_DIRECTION);
            } else {
                return(ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION);
            }
	}
	public int getScrollOrientation() {
	    return(SwingConstants.VERTICAL);
	}
	public String getDescription() {
	    return("Spin to " + value + " value");
	}
    }

    /**
     * A <code>ScrollAdjuster</code> to be used for <code>JSpinner</code>
     * component having <code>SpinnerListModel</code> model.
     * @see ListSpinnerOperator
     */
    public static class ListScrollAdjuster implements ScrollAdjuster {
        SpinnerListModel model;
        int itemIndex;
        List elements;
	private ListScrollAdjuster(JSpinnerOperator oper) {
            checkModel(oper, SpinnerListModel.class);
            model = (SpinnerListModel)oper.getModel();
            elements = model.getList();
        }
        /**
         * Constructs a <code>ListScrollAdjuster</code> object.
         * @param oper an operator to work with.
         * @param value a value to scroll to.
         */
	public ListScrollAdjuster(JSpinnerOperator oper, Object value) {
            this(oper);
            this.itemIndex = elements.indexOf(value);
	}
        /**
         * Constructs a <code>ListScrollAdjuster</code> object.
         * @param oper an operator to work with.
         * @param itemIndex an item index to scroll to.
         */
	public ListScrollAdjuster(JSpinnerOperator oper, int itemIndex) {
            this(oper);
            this.itemIndex = itemIndex;
        }
	public int getScrollDirection() {
            int curIndex = elements.indexOf(model.getValue());
            if       (itemIndex > curIndex) {
                return(ScrollAdjuster.INCREASE_SCROLL_DIRECTION);
            } else if(itemIndex < curIndex) {
                return(ScrollAdjuster.DECREASE_SCROLL_DIRECTION);
            } else {
                return(ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION);
            }
	}
	public int getScrollOrientation() {
	    return(SwingConstants.VERTICAL);
	}
	public String getDescription() {
	    return("Spin to " + Integer.toString(itemIndex) + "'th item");
	}
    }

    /**
     * A <code>ScrollAdjuster</code> to be used for <code>JSpinner</code>
     * component having <code>SpinnerDateModel</code> model.
     * @see DateSpinnerOperator
     */
    public static class DateScrollAdjuster implements ScrollAdjuster {
        Date date;
        SpinnerDateModel model;
        /**
         * Constructs a <code>DateScrollAdjuster</code> object.
         * @param oper an operator to work with.
         * @param date a date to scroll to.
         */
	public DateScrollAdjuster(JSpinnerOperator oper, Date date) {
            this.date = date;
            checkModel(oper, SpinnerDateModel.class);
            model = (SpinnerDateModel)oper.getModel();
        }
	public int getScrollDirection() {
            if       (date.after (model.getDate())) {
                return(ScrollAdjuster.INCREASE_SCROLL_DIRECTION);
            } else if(date.before(model.getDate())) {
                return(ScrollAdjuster.DECREASE_SCROLL_DIRECTION);
            } else {
                return(ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION);
            }
	}
	public int getScrollOrientation() {
	    return(SwingConstants.VERTICAL);
	}
	public String getDescription() {
	    return("Spin to " + date.toString() + " date");
	}
    }

    /**
     * Abstract class for a scrolling of a spinner having unknown model type.
     * A subclass needs to override <code>equals(Object)</code> value
     * to specify a criteria of successful scrolling.
     */
    public abstract static class ObjectScrollAdjuster implements ScrollAdjuster {
        SpinnerModel model;
        int direction;
        /**
         * Constructs a <code>ObjectScrollAdjuster</code> object.
         * @param oper an operator to work with.
         * @param direction a scrolling direction - one of <code>ScrollAdjuster.*_SCROLL_DIRECTION</code> fields.
         */
	public ObjectScrollAdjuster(JSpinnerOperator oper, int direction) {
            this.direction = direction;
            model = oper.getModel();
        }
	public int getScrollDirection() {
            if(equals(model.getValue())) {
                return(ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION);
            } else if(direction == ScrollAdjuster.INCREASE_SCROLL_DIRECTION &&
                      model.getNextValue() != null ||
                      direction == ScrollAdjuster.DECREASE_SCROLL_DIRECTION &&
                      model.getPreviousValue() != null) {
                return(direction);
            } else {
                return(ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION);
            }
	}
        public abstract boolean equals(Object curvalue);
	public int getScrollOrientation() {
	    return(SwingConstants.VERTICAL);
	}
    }

    /**
     * Class for a scrolling of a spinner having unknown model type.
     * Checks spinner value for exact equality with a specified value.
     */
    public static class ExactScrollAdjuster extends ObjectScrollAdjuster {
        Object obj;
        /**
         * Constructs a <code>ExactScrollAdjuster</code> object.
         * @param oper an operator to work with.
         * @param direction a scrolling direction - one of <code>ScrollAdjuster.*_SCROLL_DIRECTION</code> fields.
         */
	public ExactScrollAdjuster(JSpinnerOperator oper, Object obj, int direction) {
            super(oper, direction);
            this.obj = obj;
        }
        public boolean equals(Object curvalue) {
            return(curvalue.equals(obj));
        }
	public String getDescription() {
	    return("Spin to " + obj.toString() + " value");
	}
	public int getScrollOrientation() {
	    return(SwingConstants.VERTICAL);
	}
    }

    /**
     * Class for a scrolling of a spinner having unknown model type.
     * Checks spinner value's toString() reprsentation to match a string pattern.
     */
    public static class ToStringScrollAdjuster extends ObjectScrollAdjuster {
        String pattern;
        StringComparator comparator;
        /**
         * Constructs a <code>ToStringScrollAdjuster</code> object.
         * @param oper an operator to work with.
         * @param pattern a pattern to compare with
         * @param comparator specifies string comparision algorithm.
         * @param direction a scrolling direction - one of <code>ScrollAdjuster.*_SCROLL_DIRECTION</code> fields.
         */
	public ToStringScrollAdjuster(JSpinnerOperator oper, String pattern, StringComparator comparator, int direction) {
            super(oper, direction);
            this.pattern = pattern;
            this.comparator = comparator;
        }
        /**
         * Constructs a <code>ToStringScrollAdjuster</code> object.
         * Uses <code>StringComparator</code> assigned to the operator.
         * @param oper an operator to work with.
         * @param pattern a pattern to compare with
         * @param comparator specifies string comparision algorithm.
         * @param direction a scrolling direction - one of <code>ScrollAdjuster.*_SCROLL_DIRECTION</code> fields.
         */
	public ToStringScrollAdjuster(JSpinnerOperator oper, String pattern, int direction) {
            this(oper, pattern, oper.getComparator(), direction);
        }
        public boolean equals(Object curvalue) {
            return(comparator.equals(curvalue.toString(), pattern));
        }
	public String getDescription() {
	    return("Spin to \"" + pattern + "\" value");
	}
	public int getScrollOrientation() {
	    return(SwingConstants.VERTICAL);
	}
    }

    /**
     * Provides some specific functionality for <code>JSpinner</code>
     * components having <code>SpinnerNumberModel</code> model.
     * Constructor of this object is private - it cannot be received only from
     * another JSpinnerOperator instance.
     * @see #getNumberSpinner
     */
    public class NumberSpinnerOperator extends JSpinnerOperator {
        private NumberSpinnerOperator(JSpinnerOperator spinner) {
            super((JSpinner)spinner.getSource());
            copyEnvironment(spinner);
            checkModel(this, SpinnerNumberModel.class);
        }
        /**
         * Costs spinner's model to <code>SpinnerNumberModel<code>.
         * @return a spinner model.
         */
        public SpinnerNumberModel getNumberModel() {
            return((SpinnerNumberModel)getModel());
        }
        /**
         * Scrolls to a double value.
         * @param value a value to scroll to.
         */
        public void scrollToValue(double value) {
            scrollTo(new NumberScrollAdjuster(this, value));
        }
        /**
         * Scrolls to a number value.
         * @param value a value to scroll to.
         */
        public void scrollToValue(Number value) {
            scrollTo(new NumberScrollAdjuster(this, value));
        }
    }

    /**
     * Provides some specific functionality for <code>JSpinner</code>
     * components having <code>SpinnerListModel</code> model.
     * Constructor of this object is private - it cannot be received only from
     * another JSpinnerOperator instance.
     * @see #getListSpinner
     */
    public class ListSpinnerOperator extends JSpinnerOperator {
        private ListSpinnerOperator(JSpinnerOperator spinner) {
            super((JSpinner)spinner.getSource());
            copyEnvironment(spinner);
            checkModel(this, SpinnerListModel.class);
        }
        /**
         * Costs spinner's model to <code>SpinnerListModel<code>.
         * @return a spinner model.
         */
        public SpinnerListModel getListModel() {
            return((SpinnerListModel)getModel());
        }
        /**
         * Looks for an index of an item having <code>toString()</code> matching a specified pattern.
         * @param pattern a string pattern
         * @param comparator a string comparision criteria.
         */
        public int findItem(String pattern, StringComparator comparator) {
            List list = getListModel().getList();
            for(int i = 0; i < list.size(); i++) {
                if(comparator.equals(list.get(i).toString(), pattern)) {
                    return(i);
                }
            }
            return(-1);
        }
        /**
         * Looks for an index of an item having <code>toString()</code> matching a specified pattern.
         * Uses a <code>StringComparator</code> assigned to the operator.
         * @param pattern a string pattern
         */
        public int findItem(String pattern) {
            return(findItem(pattern, getComparator()));
        }
        /**
         * Scrolls to an item having specified instance.
         * @param index an index to scroll to.
         */
        public void scrollToIndex(int index) {
            scrollTo(new ListScrollAdjuster(this, index));
        }
        /**
         * Scrolls to <code>getValue().toString()</code> match a specified pattern.
         * @param pattern a string pattern
         * @param comparator a string comparision criteria.
         */
        public void scrollToString(String pattern, StringComparator comparator) {
            int index = findItem(pattern, comparator);
            if(index != -1) {
                scrollToIndex(index);
            } else {
                throw(new JemmyException("No \"" + pattern + "\" item in JSpinner", getSource()));
            }
        }
        /**
         * Scrolls to <code>getValue().toString()</code> match a specified pattern.
         * Uses a <code>StringComparator</code> assigned to the operator.
         * @param pattern a string pattern
         */
        public void scrollToString(String pattern) {
            scrollToString(pattern, getComparator());
        }
    }

    /**
     * Provides some specific functionality for <code>JSpinner</code>
     * components having <code>SpinnerDateModel</code> model.
     * Constructor of this object is private - it cannot be received only from
     * another JSpinnerOperator instance.
     * @see #getDateSpinner
     */
    public class DateSpinnerOperator extends JSpinnerOperator {
        private DateSpinnerOperator(JSpinnerOperator spinner) {
            super((JSpinner)spinner.getSource());
            copyEnvironment(spinner);
            checkModel(this, SpinnerDateModel.class);
        }
        /**
         * Costs spinner's model to <code>SpinnerDateModel<code>.
         * @return a spinner model.
         */
        public SpinnerDateModel getDateModel() {
            return((SpinnerDateModel)getModel());
        }
        /**
         * Scrolls to a date.
         * @param date a date to scroll to.
         */
        public void scrollToDate(Date date) {
            scrollTo(new DateScrollAdjuster(this, date));
        }
    }

    /**
     * Exception is thown whenever spinner model is threated wrong.
     */
    public static class SpinnerModelException extends JemmyException {
        /**
         * Constructs a <code>SpinnerModelException</code> object.
         * @param message error message.
         * @param comp a spinner which model cased the exception.
         */
        public SpinnerModelException(String message, Component comp) {
            super(message, comp);
        }
    }
}
