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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

/**
 * <p>
 * Use the <code>ui:radioButton</code> tag to display a radio button
 * in the rendered HTML page. The tag can be used as a single
 * radio button or as one radio button among a group of radio buttons.
 * A group of radio buttons represents a single selection list.
 * A radio button can represent a value of a class type such as
 * <code>Boolean, Byte, Character, Double,
 * Float, Integer, Long, Short, String</code> or the primitive form of one of
 * these class types. A radio button may also represent an application defined 
 * object value.
 * </p>
 * A <code>Boolean</code> value is useful for indicating whether
 * an item, such as a table row, is selected. A <code>String</code>
 * value is useful for passing a value for the radio button selection made in the
 * interface. An application defined <code>Object</code> value or class
 * instance can be used to hold more information related to the radio button
 * selection.
 * </p>
 * <p>
 * A group of radio buttons is the common way to use the the
 * <code>radioButton</code> tag. It can be used to represent different
 * types of data:
 * </p>
 * <ul>
 *   <li>a group of boolean controls where only one control is selected at a
 *   time.</li>
 *   <li>string values that are related to the radio button selection</li>
 *   <li>object values defined by the application</li>
 * </ul>
 * <p><em>
 * Note: It is not common to use a <code>radioButton</code> tag that is not
 * in a group. If a single radio button is not in a group, once it
 * is selected by the user in the interface, the user cannot deselect it.
 * This is because a radio button is defined to be a single selection
 * among several where one radio button is always selected. Since there
 * are no other radio buttons grouped with the single radio button, the user
 * cannot select an alternative, to cause the selected
 * radio button to be deselected.
 * </em></p>
 * <p>
 * Note: Another tag for rendering radio buttons is
 * <code>ui:radioButtonGroup</code>, which imposes a grid layout on a group
 * of radio buttons. The <code>radioButton</code> tag is useful in
 * situations where the <code>radioButtonGroup</code> tag layout is not
 * desirable, such as in a table row where only one row among several may be
 * selected.
 * </p>
 * <h3>Detecting a selected radio button</h3>
 * <p>
 * The <code>radioButton</code> tag uses both the <code>selected</code>
 * and <code>selectedValue</code> attributes to pass information about
 * the radio button's selection status. The <code>selected</code>
 * attribute is used to indicate that the radio button is selected, and should
 * have a check mark displayed in the page. The <code>selectedValue</code>
 * attribute is used to pass a data value for the
 * radio button. A radio button is considered to be selected when the value of the
 * <code>selected</code> attribute is equal to the value of
 * the <code>selectedValue</code> attribute. You can display a radio button as
 * selected on the initial viewing of the page by assigning the same value
 * to the <code>selectedValue</code> and the <code> selected</code> attributes.
 * </p>
 * <p>
 * If the <code>selectedValue</code> attribute is not specified or its
 * value is <code>null</code> then the radio button behaves like a
 * boolean control. If the radio button is selected, the value of the
 * <code>selected</code> attribute is a true <code>Boolean</code>
 * instance. If the radio button is not selected, the value of the
 * <code>selected</code> attribute will be a false <code>Boolean</code>
 * instance.
 * </p>
 * <p><em>
 * Note that a value binding expression that evaluates to a
 * primitive boolean value can be assigned to the <code>selected</code>
 * and <code>selectedValue</code> attributes.
 * </em>
 * </p>
 * <p>
 * When a radio button is part of a group, the value of the selected
 * radio button is maintained as a request attribute value in the 
 * <code>RequestMap</code>. The attribute name is the value of the
 * <code>name</code> attribute. The value of the request attribute
 * is the value of the <code>selectedValue</code> attribute of the
 * selected radio button. The value of the <code>selected</code> attribute
 * will also be equal to the <code>selectedValue</code> attribute of the
 * selected radio button. If no radio button is selected, no request
 * attribute will be created.<br>
 * The <code>RadioButton</code> class provides a convenience method for
 * obtaining the selected radio button in a group:
 * </p>
 * <p>
 * public static Object getSelected(String groupName);
 * </p>
 * <p> where <code>groupName</code> is the value of the <code>name</code>
 * attribtue. Note that unlike the <code>selected</code> and
 * <code>selectedValue</code> attributes, the return value of this method
 * is always a class instance and not a primitive value.
 * </p>
 * <p><em>
 * Note that the <code>radioButton</code> does not enforce that
 * at least one radio button is always be selected.
 * The application must ensure this behavior if necessary.
 * </em></p>
 * <h3>Using a <code>radioButton</code> tag as a boolean control</h3>
 * <p>
 * If the <code>selectedValue</code> attribute is not specified or its
 * value is <code>null</code> then the radio button behaves like a
 * boolean control.
 * </p>
 * <p>
 * To use the <code>radioButton</code> tag as a boolean control, do not
 * specify a value for the <code>selectedValue</code> attribute. The
 * radio button is selected if the <code>selected</code> attribute is not
 * null and has the value of a true <code>Boolean</code> instance or 
 * <code>boolean</code> primitive. If the radio button is not selected,
 * then the value of the <code>selected</code> attribute is a false
 * <code>Boolean</code> instance or <code>boolean</code> primitive.
 * </p>
 * <p>
 * Normally the value of the <code>selectedValue</code> attribute is
 * specified as the value of the &lt;input&gt; HTML element. When a
 * radio button is behaving as a boolean control the value of the &lt;input&gt;
 * element is the <code>clientId</code> of the radio button.
 * </p>
 * <p><em>
 * Note that using a boolean radio button in a group and
 * referencing the request attribute for the selected radio button is not
 * useful, since the value of the request attribute will be an
 * indistinguishable <code>Boolean</code> <code>true</code> value.</em>
 * </p>
 * <h3>Using a <code>radioButton</code> tag to represent an application defined
 * value</h3>
 * <p>
 * The <code>selectedValue</code> attribute can be assigned an
 * application defined object value to represent the value of a selected
 * radio button. If the radio button is selected, the value of the
 * <code>selected</code> attribute is assigned the value of the
 * <code>selectedValue</code> attribute.
 * </p>
 * <p>
 * If the value of the <code>selectedValue</code> attribute is an
 * application defined object, a converter must be registered
 * to convert to and from a <code>String</code> value. The
 * converter is used to encode the radio button value
 * as the value of the HTML &lt;input&gt; element and to decode the
 * submitted value in a request. In addition the object must support an
 * <code>equals</code> method that returns <code>true</code> when the
 * value of the <code>selectedValue</code> attribute is compared to
 * the <code>selected</code> attribute value in order to detect a
 * selected radio button.
 * </p>
 * <h3>Using a <code>radioButton</code> tag as one control in a group</h3>
 * <p>
 * The <code>name</code> attribute determines whether a
 * radio button is part of a group. A radio button is treated as part of a group
 * of radio buttons if the <code>name</code> attribute of the radio button is
 * assigned a value equal to the <code>name</code> attribute of the other
 * radio buttons in the group. In other words, all radio buttons of a group
 * have the same <code>name</code> attribute value. The group behaves
 * like a single selection list, where only one radio button
 * can be selected. The value of the name attribute must
 * be unique within the scope of the &lt;form&gt; element containing the
 * radio buttons.
 * </p>
 * 
 * <h3>Facets</h3>
 * <p>
 * The following facets are supported:
 * <ul>
 *   <li><em>image</em> If the image facet exists, it is rendered to the
 *       immediate right hand side of the radio button.
 *   <li><em>label</em> If the label facet exists, it is rendered to the
 * 	immediate right of the image, or to the immediate right of the
 * 	radio button if no image is rendered.
 * </ul>
 * </p>
 * <h3>Examples</h3>
 * <h4>Example 1: Two grouped boolean radio buttons with value bindings.</h4>
 * <code>
 * &lt;ui:radioButton id="rb0" name="rb1grp"
 *     selected="#{tldRbCbExample.selectedRb0}"/&gt;<br/>
 * &lt;br/&gt;<br/>
 * &lt;ui:radioButton id="rb1" name="rb1grp"
 *     selected="#{tldRbCbExample.selectedRb1}"/&gt;
 * </code>
 * <p>
 * The value bindings imply that
 * there are two methods implemented in the <code>tldRbCbExample</code>
 * managed bean for each value binding.
 * </p>
 * <ul>
 * <li>public void setSelectedRb0(boolean selected)</li>
 * <li>public boolean getSelectedRb0()</li>
 * <li>public void setSelectedRb1(boolean selected)</li>
 * <li>public boolean getSelectedRb1()</li>
 * </ul>
 * <p>
 * The "getSelected" methods will be called to determine the checked
 * state of the radio buttons during rendering.<br/>
 * When the tags are first rendered, the initial checked state is
 * determined by the return value of the "getSelected" methods, only one of
 * which should return true.
 * The radio button whose "getSelected" method returns 
 * <code>true</code> will be checked in the HTML page and not checked if it
 * returns <code>false</code>.
 * When one of the radio buttons is checked by the user its "setSelected" method
 * will be called with a <code>boolean</code> argument equal to <code>true</code>.
 * The other radio button's "setSelected" method will be called
 * with a <code>boolean</code> argument equal to <code>false</code>.<br/>
 * </p>
 * <p>
 * No image or label will be displayed by this example.
 * </p>
 * <h4>Example 2: Two grouped boolean radio buttons with value bindings,
 * that display an image and a label.</h4>
 * <code>
 * &lt;ui:radioButton id="rb2" name="rb2grp" 
 *     imageURL="tree_server.gif" label="Server"
 *     selected="#{tldRbCbExample.selectedRb2}"/&gt;<br/>
 * &lt;br/&gt;<br/>
 * &lt;ui:radioButton id="rb3" name="rb2grp"
 *     imageURL="pool_tree.gif" label="Pool"
 *     selected="#{tldRbCbExample.selectedRb3}"/&gt;
 * </code>
 * <p>
 * The behavior of these radio buttons is the same as example one.<br/>
 * In this example an image and a label are displayed next to both 
 * radio buttons. Both
 * the <code>imageURL</code> and <code>label</code> attributes may be assigned
 * value binding expressions instead of literal values.
 * </p>
 * <h4>Example 3: Two grouped String valued radio buttons with value bindings 
 * and labels.</h4>
 * <code>
 * &lt;ui:radioButton id="rb4" name="rb3grp" 
 * 	label="Print" selectedValue="Print"
 * 	selected="#{tldRbCbExample.selectedRb4}"/&gt;<br/>
 * &lt;br/&gt;<br/>
 * &lt;ui:radioButton id="rb5" name="rb3grp"
 * 	label="Fax" selectedValue="Fax"
 * 	selected="#{tldRbCbExample.selectedRb5}"/&gt;
 * </code>
 * <p>
 * The value bindings imply that
 * there are two methods implemented in the <code>tldRbCbExample</code>
 * managed bean for each value binding.
 * Because the <code>selectedValue</code> attribute is a
 * <code>String</code> the expected method signatures will be:
 * </p>
 * <ul>
 * <li>public void setSelectedRb4(String selected)</li>
 * <li>public String getSelectedRb4()</li>
 * <li>public void setSelectedRb5(String selected)</li>
 * <li>public String getSelectedRb5()</li>
 * </ul>
 * <p>
 * The "getSelected" methods will be called to determine the checked
 * state of the radio buttons during rendering.<br/>
 * When the tags are first rendered, the initial checked state is
 * determined by the return value of the "getSelected" methods.<br/>
 * With a <code>String</code> valued radio button, a radio button will 
 * be checked only if the "getSelected" method returns the value of its
 * <code>selectedValue</code> attribute.<br/>
 * For example if <code>getSelectedRb4</code> returns "Print", the
 * radio button "rb4" will be checked. <code>getSelectedRb5</code> must 
 * not return "Fax" and should return <code>null</code> in order for "rb4" to 
 * remain checked.<br/>
 * Alternatively if <code>getSelectedRb4</code> returns <code>null</code>
 * <code>getSelectedRb5</code> should return "Fax", and radio button "rb5"
 * will be checked.
 * </p>
 * <p>
 * When the radio button is checked by the user the "setSelected"
 * methods will be called with a <code>String</code> argument equal to the
 * value of the <code>selectedValue</code> attribute of the radio button.<br/>
 * When it is unchecked the method will be called with a <code>null</code>
 * <code>String</code>
 * argument.<br/>
 * For example if radio button "rb4" is checked by the user
 * <code>setSelectedRb4</code> will be called with "Print" as the argument and
 * <code>setSelectedRb5</code> will be called with a <code>null</code> argument.
 * </p>
 * <h4>Example 4: Two grouped object valued radio buttons with value bindings
 * and labels.</h4>
 * <code>
 * &lt;ui:radioButton id="rb6" name="rb4grp" label="Print"
 * 	selectedValue="#{tldRbCbExample.selectedValueRb6}"
 * 	selected="#{tldRbCbExample.selectedRb6}"
 * 	converter="#{tldRbCbExample.rbConverter}"/&gt;<br/>
 * &lt;br/&gt;<br/>
 * &lt;ui:radioButton id="rb7" name="rb4grp" label="Fax"
 * 	selectedValue="#{tldRbCbExample.selectedValueRb7}"
 * 	selected="#{tldRbCbExample.selectedRb7}"
 * 	converter="#{tldRbCbExample.rbConverter}"/&gt;
 * </code>
 * <p>
 * The value bindings imply that
 * there are two methods implemented in the <code>tldRbCbExample</code>
 * managed bean for each value binding.
 * Let's say the object value for "rb6" is an instance of the "Printer" class,
 * and "rb7" an instance of the "Fax" class, then the expected
 * method signatures will be:
 * </p>
 * <p>
 * <ul>
 * <li>public void setSelectedRb6(Printer selected)</li>
 * <li>public Printer getSelectedRb6()</li>
 * <li>public void setSelectedValueRb7(Fax selected)</li>
 * <li>public Printer getSelectedValueRb7()</li>
 * </ul>
 * </p>
 * A Printer class might look like:
 * <p>
 * <code><pre>
 *     public class Printer implements Device {
 * 	private String name;
 * 	private String location;
 * 	public Printer(String name, String location) {
 * 	    this.name = name;
 * 	    this.location = location;
 * 	}
 * 	public String getName() {
 * 	    return name;
 * 	}
 * 	public String getLocation() {
 * 	    return location;
 * 	}
 * 	public int getType() {
 * 	    return Device.PRINTER;
 * 	}
 * 	public boolean equals(Printer p) {
 * 	    return this.name.equals(p.getName()) &&
 * 		    this.location.equals(p.getLocation()) &&
 * 		    p.getType() == Device.PRINTER;
 * 	}
 *     };
 * </pre>
 * </code>
 * </p>
 * A Fax class might look like:
 * <p>
 * <code><pre>
 *     public class Fax implements Device {
 * 	private String name;
 * 	private String phoneNumber;
 * 	public Printer(String name, String phoneNumber) {
 * 	    this.name = name;
 * 	    this.phoneNumber = phoneNumber;
 * 	}
 * 	public String getName() {
 * 	    return name;
 *         }
 * 	public String getPhoneNumber() {
 * 	    return phoneNumber;
 * 	}
 * 	public int getType() {
 * 	    return Device.FAX;
 * 	}
 * 	public boolean equals(Fax f) {
 * 	    return this.name.equals(f.getName()) &&
 * 		    this.phoneNumber.equals(f.getPhoneNumber()) &&
 * 		    f.getType() == Device.FAX;
 * 	}
 *     };
 * </pre>
 * </code>
 * </p>
 * <p>
 * Since this radio button represents an application defined object value,
 * the application must provide a converter instance. The converter attribute's
 * value binding expression implies a method in the <code>tldRbCbExample</code>
 * managed bean called
 * </p>
 * <p>
 * <code>public Converter getRbConverter();</code>
 * </p>
 * The converter class might look like:
 * <code><pre>
 *     public class RbConverter implements javax.faces.convert.Converter {
 * 	public RbConverter() {
 * 	}
 * 	public String getAsString(FacesContext context, 
 * 		UIComponent component, Object value) {
 * 	    if (!value instanceof Device) {
 * 		throw new ConverterException("Not a Device value");
 * 	    }
 * 	    return String.valueOf(((Device)value).getType());
 * 	}
 * 	public Object getAsObject(FacesContext context, 
 * 		UIComponent component, String value) {
 * 	    if (value == null) {
 * 		return null;
 * 	    }
 * 	    // value is the String representation of "getType"
 * 	    //
 * 	    int type = Integer.parseInt(value);
 * 	    switch (type) {
 * 	    case Device.PRINTER:
 * 		return deviceDb.getClosestPrinter();
 * 	    break;
 * 	    case Device.FAX:
 * 		return deviceDb.getFax();
 * 	    break;
 * 	    default:
 * 		throw new ConverterException("No such device : " + value);
 * 	    break;
 * 	    }
 * 	}
 *     };
 * </pre>
 * </code>
 * <p>
 * The "getSelected" methods will be called to determine the checked
 * state of the radio buttons during rendering.<br/>
 * When the tags are first rendered, the initial checked state is
 * determined by the return value of the "getSelected" methods.<br/>
 * With <code>Object</code> valued radio buttons,
 * a radio button will be checked only if the "getSelected" method
 * returns an object instance that equals the object instance returned
 * by the "getSelectedValue" method.<br/>
 * For example if <code>getSelectedRb6</code> returns the <code>Printer</code>
 * instance value of "rb6"'s <code>selectedValue</code> attribute, then
 * "rb6" will be checked. <code>getSelectedRb7</code> should return
 * <code>null</code>. If the <code>getSelectedRb6</code> method returns a
 * <code>Printer</code> instance that is not equal as determined by
 * <code>getSelectedValueRb6().equals(getSelectedRb6())</code> the radio button
 * will not be checked.<br/>
 * When the radio button is checked by the user the "setSelectedValue"
 * methods will be called with the object instance returned by the converter.<br/>
 * For example if "rb6" is checked by the user, <code>setSelectedRb6</code> will
 * be called with a <code>Printer</code> instance returned by the converter.
 * <code>setSelectedRb7</code> will be called with a <code>null</code>
 * argument.
 * </p>
 * <p>
 * Note that when radio buttons are part of a group the value of the
 * selected radio button can be obtained directly from the request map.
 * For example, processing the selection could take place in the action
 * method of a submit button tag:
 * </p>
 * <p>
 * <code><pre>
 *     public void submit() {
 * 
 * 	// RadioButton.getSelected(String groupName) is
 * 	// a static convenience method that obtains the 
 * 	// selected radio button value from the request map
 * 	// <em>ONLY when the radio button is part of a group</em>.
 * 	//
 * 	Object selection = RadioButton.getSelected("rb4grp");
 * 
 * 	// Assume at least one radio button will be selected.
 * 	//
 * 	processSelection((Device)selection);
 *     }
 * </pre></code>
 * </p>
 * <h4>Example 5: Grouped Integer valued radio buttons in a table.</h4>
 * <p>
 * The following example shows a common use case for radio buttons in
 * a table. The radio buttons are used to select at most one row
 * for processing. The radio button state does not need to be
 * stored. The selected row index can be obtained directly in the
 * <code>#{tldRbCbExample.table5process}</code> method, using the
 * <code>RadioButton.getSelected(String groupName)</code> convenience
 * method. The markup in bold is how you would specify a radio button tag
 * for this purpose.  The <code>selectedValue</code> value binding,
 * <code>#{tldRbCbExample.currentRow1}</code>
 * is implemented to return the current row in the <code>table5row1</code>
 * tableRow tag.
 * </p>
 * <p>
 * Note that this example will not initially select a radio button
 * which is normally not the way radio buttons are used; one is usually
 * always checked.
 * </p>
 * <p>
 * <code><pre>
 *     &lt;ui:table id="table5"&gt;
 * 	&lt;ui:tableRow id="table5row1"
 * 	    sourceData="#{tldRbCbExample.table5row1data}"
 * 	    sourceVar="table5data"
 * 	    binding="#{tldRbCbExample.table5row1}"&gt;
 * 	    &lt;ui:tableColumn id="col1"&gt;
 * 
 * 		&lt;f:facet name="header"&gt;
 * 		    &lt;ui:tableHeader id="header1"
 * 			deselectAllButton="true"
 * 			selectAllButton="true"
 * 			selectId="rb5"/&gt;
 * 		&lt;/f:facet&gt;
 * 
 * 		<b>
 * 		&lt;ui:radioButton id="rb8" name="rb5grp" 
 * 			selectedValue="#{tldRbCbExample.currentRow1}"&gt;
 * 		&lt;/ui:radioButton&gt;
 * 		</b>
 * 
 * 	    &lt;/ui:tableColumn&gt;
 * 	    &lt;ui:tableColumn id="col2"&gt;
 * 		&lt;f:facet name="header"&gt;
 * 		    &lt;ui:staticText text="Application Data"/&gt;
 * 		&lt;/f:facet&gt;
 * 
 * 		&lt;ui:staticText text="#{table5data.text}"/&gt;
 * 
 * 	    &lt;/ui:tableColumn&gt;
 * 	&lt;/ui:tableRow&gt;
 * 	&lt;f:facet name="tableActionsBottom"&gt;
 * 	   &lt;ui:button id="table5process"
 * 		action="#{tldRbCbExample.table5process}"
 * 		text="Process Checked"/&gt;
 * 	&lt;/f:facet&gt;
 *     &lt;/ui:table&gt;
 * </pre>
 * </code>
 * </p>
 * <p>
 * See <a href="table.html" target="tagFrame">ui:table</a> for details
 * on using the  <code>&lt;ui:table&gt;</code> tag and other table child tags
 * and facets.
 * </p>
 * <p>
 * </p>
 * <p>
 * Normally when radio buttons are contained within a <code>ui:tableRow</code>
 * the application MUST provide a value binding for the <code>selected</code>
 * attribute and any attribute that is expected to maintain its state. This
 * is because the table only creates a single instance of the radio button for
 * all rows. It depends on a model to provide the storage for the attribute
 * values, as it iterates over the rows in the dataset.<br/>
 * In this example, we don't need to maintain the state across requests because
 * a row is only selected for processing. Once the processing
 * is complete, the radio button no longer needs to be checked.
 * <p>
 * The following code shows how the <code>table5process</code> action
 * method obtains the selected radio button value from the request map.
 * It calls a static member on <code>RadioButton</code> to return the
 * <code>Integer</code> row index.
 * </p>
 * <code><pre>
 *     public void table5process() {
 * 
 * 	// RadioButton.getSelected(String groupName) is
 * 	// a static convenience method that obtains the 
 * 	// selected radio button value from the request map
 * 	// <em>ONLY when the radio button is part of a group</em>.
 * 	//
 * 	Integer row = (Integer)RadioButton.getSelected("rb5grp");
 * 	if (row != null) {
 * 	    processRow(row.intValue());
 * 	}
 *     }
 * </pre></code>
 * <p>
 * <h4>Example 6: Grouped boolean radio buttons in a table, using value bindings to
 * maintain the state.</h4>
 * <p>
 * This example is similar to Example 5, but it maintains the state of the radio
 * buttons across requests, by specifying a value binding for the selected
 * attribute.  A simple way to store the radio button state, is to store the
 * state with the row data. The following code replaces the "ui:radioButton"
 * code in the previous example.
 * </p>
 * <code>
 * 	&lt;ui:radioButton id="rb6" name="rb6grp"
 * 		selected="#{table6data.selected}"&gt;
 * 	&lt;/ui:radioButton&gt;
 * </code>
 * <p>
 * The value binding <code>#{table6data.selected}</code> references a boolean
 * member in the row data for storing and retrieving the radio button state.
 * </p>
 * 
 * <h3>HTML Elements and Layout</h3>
 * <p>
 * A <code>radioButton</code> is rendered as at least one HTML &lt;span&gt;
 * element and one &lt;input&gt; element of type <em>radio</em>.
 * Each radio button may consist of the following elements:
 * </p>
 * <ul>
 *     <li>a &lt;span&gt; element</li>
 *     <li>an &lt;input&gt; element of type <em>radio</em></li>
 *     <li>an optional image if the <code>imageURL</code>
 *     attribute or an <code>image</code> facet is specified. If the 
 *     <code>imageURL</code> attribute is specified a
 *     <code>com.sun.rave.web.ui.component.ImageComponent</code> component is created
 *     and rendered. If an <code>image</code> facet is specified then the 
 *     component specified by the facet is rendered.</li>
 *     <li>an optional label if a <code>label</code>
 *     attribute or a <code>label</code> facet is specified.
 *     If the <code>label</code> attribute is
 *     specified a <code>com.sun.rave.web.ui.component.Label</code> component is
 *     created and rendered. If a <code>label</code> facet is specified then 
 *     the component specified by the facet is rendered.</li>
 * </ul>
 * <p>
 * The id attributes for HTML elements are constructed as follows,
 * where <em>rid</em> is the <code>clientId</code> of the 
 * component being rendered.
 * <p>
 * <ul>
 * <li> <em>rid_span</em> for the &lt;span&gt; element
 * </li>
 * <li> <em>rid</em> for the &lt;input element
 * </li>
 * <li> <em>rid</em><b>_image</b> for the image component if created.</li>
 * <li> <em>rid</em><b>_label</b> for the label component if created.</li>
 * </li>
 * </ul>
 * </p>
 * <p>
 * Note that the value of the <code>style</code> and <code>styleClass</code>
 * attributes of a radio button will be assigned to the containing 
 * &lt;span&gt; HTML element's <code>style</code> and <code>class</code> attributes
 * respectively.
 * </p>
 * <h3>Client Side Javascript Functions </h3>
 * <p>
 * <ul>
 *     <li><em>radioButton_setChecked(elementId, checked)</em>: Set the checked
 *     property for a radio button with the given element id, <em>elementId</em>.
 *     If <em>checked</em> is true the radio button is checked.
 *     If <em>checked</em> is false the radio button is unchecked.</li>
 * </ul>
 * </p>
 * 
 * <!--
 * 	<h3>Theme Identifiers</h3>
 * <p>
 * 	<lo>
 * 	<li>Rb for the INPUT element</li>
 * 	<li>RbDis for the INPUT element for disabled radio button</li>
 * 	<li>RbLbl for a LABEL element of a radio button</li>
 * 	<li>RbLblDis for a LABEL element of a disabled radio button</li>
 * 	<li>RbImg for an IMG element of a radio button</li>
 * 	<li>RbImgDis for an IMG element of a disabled radio button</li>
 * 	</lo>
 * </p>
 * -->
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class RadioButtonBase extends com.sun.rave.web.ui.component.RbCbSelector {

    /**
     * <p>Construct a new <code>RadioButtonBase</code>.</p>
     */
    public RadioButtonBase() {
        super();
        setRendererType("com.sun.rave.web.ui.RadioButton");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.RadioButton";
    }

    // labelLevel
    private int labelLevel = Integer.MIN_VALUE;
    private boolean labelLevel_set = false;

    /**
 * <p>Sets the style level for the generated label, provided the
 *       label attribute has been set. Valid values are 1 (largest), 2 and
 *       3 (smallest). The default value is 3.</p>
     */
    public int getLabelLevel() {
        if (this.labelLevel_set) {
            return this.labelLevel;
        }
        ValueBinding _vb = getValueBinding("labelLevel");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return 3;
    }

    /**
 * <p>Sets the style level for the generated label, provided the
 *       label attribute has been set. Valid values are 1 (largest), 2 and
 *       3 (smallest). The default value is 3.</p>
     * @see #getLabelLevel()
     */
    public void setLabelLevel(int labelLevel) {
        this.labelLevel = labelLevel;
        this.labelLevel_set = true;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.labelLevel = ((Integer) _values[1]).intValue();
        this.labelLevel_set = ((Boolean) _values[2]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[3];
        _values[0] = super.saveState(_context);
        _values[1] = new Integer(this.labelLevel);
        _values[2] = this.labelLevel_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
