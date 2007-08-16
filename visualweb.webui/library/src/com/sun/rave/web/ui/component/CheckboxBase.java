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
 * Use the <code>ui:checkbox</code> tag to display a checkbox
 * in the rendered HTML page. The tag can be used as a single
 * checkbox or as one checkbox among a group of checkboxes. A group
 * of checkboxes represents a multiple selection list which can have any
 * number of checkboxes selected, or none selected. A checkbox can
 * represent a value of a class type such as <code>Boolean, Byte, Character,
 * Double, Float, Integer, Long, Short, String</code> or the primitive form
 * of one of these class types.
 * A checkbox may also represent an application defined object value.
 * </p>
 * <p>
 * A <code>Boolean</code> value is useful for indicating whether
 * an item, such as a table row, is selected. A <code>String</code>
 * value is useful for passing values for checkbox selections made in the
 * interface. An application defined <code>Object</code> value or class
 * instance can be used to hold more information related to a checkbox
 * selection.
 * </p>
 * <p>
 * A single checkbox can be used to represent several types of data:
 * </p>
 * <ul>
 *   <li>a boolean control</li>
 *   <li>a string value that is related to the checkbox selection</li>
 *   <li>an object value defined by the application</li>
 * </ul>
 * <p>
 * A group of checkboxes can be used to represent:
 * </p>
 * <ul>
 *   <li>string values that are related to the checkbox selections</li>
 *   <li>object values defined by the application</li>
 * </ul>
 * <p>
 * Note: Another tag for rendering checkboxes is
 * <code>ui:checkboxGroup</code>, which imposes a grid layout on a group
 * of checkboxes. The <code>checkbox</code> tag is useful in
 * situations where the <code>checkboxGroup</code> tag layout is not
 * desirable, such as in a table row.
 * </p>
 * <p>
 * </p>
 * <h3>Detecting a selected checkbox</h3>
 * <p>
 * The <code>checkbox</code> tag uses both the <code>selected</code>
 * and <code>selectedValue</code> attributes to pass information about
 * the checkbox's selection status. The <code>selected</code>
 * attribute is used to indicate that the checkbox is selected, and should
 * have a check mark displayed in the page. The <code>selectedValue</code>
 * attribute is used to pass a data value for the
 * checkbox. A checkbox is considered to be selected when the value of the
 * <code>selected</code> attribute is equal to the value of
 * the <code>selectedValue</code> attribute. You can display a checkbox as
 * selected on the initial viewing of the page by assigning the same value
 * to the <code>selectedValue</code> and the <code> selected</code> attributes.
 * </p>
 * <p>
 * If the <code>selectedValue</code> attribute is not specified or its
 * value is <code>null</code> then the checkbox behaves like a
 * boolean control. If the checkbox is selected, the value of the
 * <code>selected</code> attribute is a true <code>Boolean</code>
 * instance. If the checkbox is not selected, the value of the
 * <code>selected</code> attribute will be a false <code>Boolean</code>
 * instance.
 * </p>
 * <p><em>
 * Note that a value binding expression that evaluates to a
 * primitive value can be assigned to the <code>selected</code>
 * and <code>selectedValue</code> attributes.
 * </em>
 * </p>
 * <p>
 * When checkboxes are part of a group, an <code>ArrayList</code> of
 * selected checkboxes is maintained. If any checkboxes within a group are
 * selected, a request attribute whose name is the value of the <code>name</code>
 * attribute is created and added to the <code>RequestMap</code>. The
 * request attribute value is an <code>ArrayList</code> containing the
 * value of the <code>selectedValue</code> attribute of each selected
 * checkbox. If no checkboxes are selected, no request attribute is
 * created. The <code>selected</code> attribute of each selected checkbox
 * within the group will also contain the value of the <code>selectedValue</code>
 * attribute of the respective selected checkbox.<br/>
 * </p>
 * The <code>Checkbox</code> class provides a convenience method for
 * obtaining the selected checkboxes in a group:
 * </p>
 * <p>
 * public static ArrayList getSelected(String groupName);
 * </p>
 * <p> where <code>groupName</code> is the value of the <code>name</code>
 * attribtue. Note that unlike the <code>selected</code> and
 * <code>selectedValue</code> attributes, the return value of this method
 * is always an ArrayList of class instances and not primitive values.
 * </p>
 * <h3>Using a <code>checkbox</code> tag as a boolean control</h3>
 * <p>
 * If the <code>selectedValue</code> attribute is not specified or its
 * value is <code>null</code> then the checkbox behaves like a
 * boolean control.
 * </p>
 * <p>
 * To use the <code>checkbox</code> tag as a boolean control, do not
 * specify a value for the <code>selectedValue</code> attribute. The
 * checkbox is selected if the <code>selected</code> attribute is not
 * null and has the value of a true <code>Boolean</code> instance or
 * a <code>boolean</code> primitive value.
 * If the checkbox is not selected, then the value of the
 * <code>selected</code> attribute is a false <code>Boolean</code> instance
 * or <code>boolean</code> primitive.
 * </p>
 * <p>
 * Normally the value of the <code>selectedValue</code> attribute is
 * specified as the value of the &lt;input&gt; HTML element. When a
 * checkbox is behaving as a boolean control the value of the &lt;input&gt;
 * element is the <code>clientId</code> of the checkbox.
 * </p>
 * <p><em>
 * Note that using a boolean checkbox in a group and
 * referencing the request attribute for the selected checkboxes is not
 * useful, since the value of the request attribute will be an <code>ArrayList
 * </code> of indistinguishable <code>true</code> values.
 * </em>
 * </p>
 * <h3>Using a <code>checkbox</code> tag to represent an application defined
 * value</h3>
 * <p>
 * The <code>selectedValue</code> attribute can be assigned an
 * application defined object value to represent the value of a selected
 * checkbox. If the checkbox is selected, the value of the <code>selected</code>
 * attribute is assigned the value of the <code>selectedValue</code>
 * attribute.
 * </p>
 * <p>
 * If the value of the <code>selectedValue</code> attribute is an
 * application defined object, a converter must be registered
 * to convert to and from a <code>String</code> value. The
 * converter is used to encode the checkbox value
 * as the value of the HTML &lt;input&gt; element and to decode the
 * submitted value in a request. In addition the object must support an
 * <code>equals</code> method that returns <code>true</code> when the 
 * value of the <code>selectedValue</code> attribute is compared to
 * the <code>selected</code> attribute value in order to detect a
 * selected checkbox.
 * </p>
 * <h3>Using a <code>checkbox</code> tag as one control in a group</h3>
 * <p>
 * The <code>name</code> attribute determines whether a
 * checkbox is part of a group. A checkbox is treated as part of a group
 * of checkboxes if the <code>name</code> attribute of the checkbox is
 * assigned a value equal to the <code>name</code> attribute of the other
 * checkboxes in the group. In other words, all checkboxes of a group have the
 * same <code>name</code> attribute value. The group behaves
 * like a multiple selection list, where zero or more checkboxes
 * can be selected. The value of the name attribute must
 * be unique within the scope of the &lt;form&gt; element containing the
 * checkboxes.
 * </p>
 * <h3>Facets</h3>
 * <p>
 * The following facets are supported:
 * </p>
 * <ul>
 *   <li><em>image</em> If the image facet exists, it is rendered to the
 *       immediate right hand side of the checkbox.
 *   <li><em>label</em> If the label facet exists, it is rendered to the
 * 	immediate right of the image, or to the immediate right of the
 * 	checkbox if no image is rendered.
 * </ul>
 * <h3>Examples</h3>
 * <h4>Example 1: Single boolean checkbox with value binding.</h4>
 * <code>
 * &lt;ui:checkbox id="cb1" selected="#{tldRbCbExample.selectedCb1}"/&gt;
 * </code>
 * <p>
 * The value binding <code>#{tldRbCbExample.selectedCb1}</code> implies that
 * there are two methods implemented in the <code>tldRbCbExample</code>
 * managed bean.
 * <ul>
 * <li>public void setSelectedCb1(boolean selected)</li>
 * <li>public boolean getSelectedCb1()</li>
 * </ul>
 * The <code>getSelectedCb1</code> method will be called to determine the checked
 * state of the checkbox during rendering.<br/>
 * When the tag is first rendered, its initial checked state is
 * determined by the return value of <code>getSelectedCb1</code>. If it returns
 * <code>true</code> the checkbox will be checked on the HTML page and 
 * not checked if it returns <code>false</code><br/>
 * When the checkbox is checked by the user the <code>setSelectedCb1</code> method
 * will be called with a <code>boolean</code> argument equal to <code>true</code>.
 * When it is unchecked the method will be called with a <code>boolean</code>
 * argument equal to <code>false</code>.<br/>
 * </p>
 * <p>No image or label will be displayed by this example.</p>
 * 
 * <h4>Example 2: Single boolean checkbox with value binding,
 * that displays an image and a label.</h4>
 * <code>
 * &lt;ui:checkbox id="cb2" selected="#{tldRbCbExample.selectedCb2}"
 * imageURL="tree_server.gif label="Server"/&gt;
 * </code>
 * <p>
 * The behavior of this checkbox is the same as example one.<br/>
 * In this example an image and a label are displayed next to the checkbox. Both
 * the <code>imageURL</code> and <code>label</code> attributes may be assigned
 * value binding expressions instead of literal values.
 * </p>
 * <h4>Example 3: Single String valued checkbox with value binding.</h4>
 * <code>
 * &lt;ui:checkbox id="cb3" label="Printer" selectedValue="Printer"
 * selected="#{tldRbCbExample.selectedCb3}"/&gt;
 * </code>
 * <p>
 * The value binding <code>#{tldRbCbExample.selectedCb3}</code> implies that
 * there are two methods implemented in the <code>tldRbCbExample</code>
 * managed bean. Because the <code>selectedValue</code> attribute is a
 * <code>String</code> the expected method signatures will be:
 * <ul>
 * <li>public void setSelectedCb3(String selected)</li>
 * <li>public String getSelectedCb3()</li>
 * </ul>
 * The <code>getSelectedCb3</code> method will be called to determine the
 * checked state of the checkbox during rendering.<br/>
 * When the tag is first rendered, its initial checked state is determined by
 * the return value of <code>getSelectedCb3</code>. With a <code>String</code>
 * valued checkbox, this checkbox will be checked only if the
 * <code>getSelectedCb3</code> method returns "Printer", since that is the value
 * of the checkbox as dictated by the <code>selectedValue="Printer"</code>
 * attribute. If the <code>getSelectedCb3</code> method returns anything else,
 * the checkbox will not be checked.<br/>
 * When the checkbox is checked by the user the <code>setSelectedCb3</code>
 * method will be called with a <code>String</code> argument equal to "Printer".
 * When it is unchecked the method will be called with a null <code>String</code>
 * argument.
 * </p>
 * <h4>Example 4: Single Object valued checkbox with value bindings and a label.</h4>
 * <code>
 * &lt;ui:checkbox id="cb4" label="Printer"
 * 	selectedValue="#{tldRbCbExample.selectedValueCb4}"
 * 	selected="#{tldRbCbExample.selectedCb4}"
 * 	converter="#{tldRbCbExample.printerConverter}"/&gt;
 * </code>
 * <p>
 * The value bindings <code>#{tldRbCbExample.selectedCb4}</code> and
 * <code>#{tldRbCbExample.selectedValueCb4}</code> imply the following methods
 * are implemented in the <code>tldRbCbExample</code> managed bean. Let's say
 * the object value is an instance of the "Printer" class, then the expected
 * method signatures will be:
 * </p>
 * <p>
 * <ul>
 * <li>public void setSelectedCb4(Printer selected)</li>
 * <li>public Printer getSelectedCb4()</li>
 * <li>public void setSelectedValueCb4(Printer selected)</li>
 * <li>public Printer getSelectedValueCb4()</li>
 * </ul>
 * </p>
 * A Printer class might look like:
 * <p>
 * <code><pre>
 *     public static class Printer {
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
 * 	public boolean equals(Printer p) {
 * 	    return this.name.equals(p.getName()) &&
 * 		    this.location.equals(p.getLocation());
 * 	}
 *     };
 * </pre>
 * </code>
 * </p>
 * <p>
 * Since this is an application defined object value, the application must supply
 * a converter, as indicated in the example. The converter attribute's
 * value binding expression implies a method in the <code>tldRbCbExample</code>
 * managed bean called
 * </p>
 * <p>
 * <code>public Converter getPrinterConverter();</code>.
 * </p>
 * The converter class might look like:
 * <code><pre>
 *     public class PrinterConverter implements javax.faces.convert.Converter {
 * 	public PrinterConverter() {
 * 	}
 * 	public String getAsString(FacesContext context, 
 * 		UIComponent component, Object value) {
 * 	    if (!(value instanceof Printer)) {
 * 		throw new ConverterException("Not a Printer value");
 * 	    }
 * 	    return ((Printer)value).getName();
 * 	}
 * 	public Object getAsObject(FacesContext context, 
 * 		UIComponent component, String value) {
 * 	    if (!value.equals("printer1")) {
 * 		throw new ConverterException("Unrecognized printer: " + value);
 * 	    }
 * 	    return printerDb.getPrinter("printer1");
 * 	}
 *     };
 * </pre>
 * </code>
 * </p>
 * <p>
 * The <code>getSelectedCb4</code> method will be called to determine the
 * checked state of the checkbox during rendering.<br/> When the tag
 * is first rendered, its initial state is determined by the return value of
 * <code>getSelectedCb4</code>. With an <code>Object</code> valued checkbox,
 * this checkbox will be checked only if the <code>getSelectedCb4</code> method
 * returns a Printer instance that equals the Printer instance returned
 * by the <code>getSelectedValueCb4</code> method.<br/>
 * If the <code>getSelectedCb4</code> method returns a Printer instance that
 * is not equal as determined by
 * <code>getSelectedValueCb4().equals(getSelectedCb4())</code> the checkbox
 * will not be checked.<br/>
 * When the checkbox is checked by the user the <code>setSelectedCb4</code>
 * method will be called with the Printer instance returned by the converter.
 * </p>
 * 
 * <h4>Example 5: Grouped Integer valued checkboxes in a table.</h4>
 * <p>
 * The following example shows a common use case for checkboxes in
 * a table. The checkboxes are used to select zero or more rows
 * for processing. The checkbox state does not need to be
 * stored. The selected row indexes can be obtained directly as
 * <code>Integer</code> values from the <code>ArrayList</code> of
 * selected checkboxes maintained by the checkbox
 * in the action callback <code>#{tldRbCbExample.table5process}</code>.<br/>
 * The markup in bold is how you would specify a checkbox tag for this purpose.
 * The <code>selectedValue</code> value binding,
 * <code>#{tldRbCbExample.currentRow1}</code>
 * is implemented to return the current row in the <code>table5row1</code>
 * tableRow tag.
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
 * 			selectId="cb5"/&gt;
 * 		&lt;/f:facet&gt;
 * 
 * 		<b>
 * 		&lt;ui:checkbox id="cb5" name="cb5Grp" 
 * 			selectedValue="#{tldRbCbExample.currentRow1}"&gt;
 * 		&lt;/ui:checkbox&gt;
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
 * on using the <code>&lt;ui:table&gt;</code> tag and other table child tags
 * and facets.
 * </p>
 * <p>
 * </p>
 * <p>
 * Normally when checkboxes are contained within a <code>ui:tableRow</code>
 * the application MUST provide a value binding for the <code>selected</code>
 * attribute and any attribute that is expected to maintain its state. This
 * is because the table only creates a single instance of the checkbox for
 * all rows. It depends on a model to provide the storage for the attribute
 * values, as it iterates over the rows in the dataset.<br/>
 * In this example, we don't need to maintain the state across requests because
 * the rows just need to be selected for processing. Once the processing
 * is complete, the checkbox no longer needs to be checked.
 * <p>
 * The following code shows how the <code>table5process</code> action
 * method obtains the selected checkbox values from the request map.
 * It calls a static member on <code>Checkbox</code> to return the
 * <code>ArrayList</code>
 * </p>
 * <p>
 * <code>public static ArrayList getSelected(String groupName)</code>
 * </p>
 * <code><pre>
 *     public void table5process() {
 * 
 * 	// Checkbox.getSelected(String groupName) is
 * 	// a static convenience method that obtains the 
 * 	// ArrayList of selected checkboxes from the request map
 * 	// <em>ONLY when the checkboxes are part of a group</em>.
 * 	//
 * 	ArrayList al = Checkbox.getSelected("cb5Grp");
 * 	if (al != null) {
 * 	    ListIterator li = al.listIterator();
 * 	    while (li.hasNext()) {
 * 		processRow(((Integer)li.next()).intValue());
 * 	    }
 * 	}
 *     }
 * </pre></code>
 * <p>
 * <h4>Example 6: Grouped boolean checkboxes in a table, using value bindings to
 * maintain the state.</h4>
 * <p>
 * This example is similar to Example 5, but it maintains the state of checkboxes
 * across requests, by specifying a value binding for the selected attribute.
 * A simple way to store the checkbox state is to store the state with the
 * row data.
 * </p>
 * <code>
 * 	&lt;ui:checkbox id="cb6" selected="#{table6data.selected}"&gt;
 * 	&lt;/ui:checkbox&gt;
 * </code>
 * <p>
 * The value binding <code>#{table6data.selected}</code> references a boolean
 * member in the row data for storing and retrieving the checkbox state.
 * Notice also that it is not necessary to group the checkboxes by specifying
 * a value for the <code>name</code> attribute. It is not useful to specify
 * boolean checkboxes in a group, in order to obtain the list of selected
 * checkboxes from the request map. The list will consist of indistinguishable
 * <code>true</code> values; one for each selected checkbox.
 * </p>
 * <h3>HTML Elements and Layout</h3>
 * <p>
 * A <code>checkbox</code> is rendered as at least one HTML &lt;span&gt; 
 * element and one &lt;input&gt; element of type <em>checkbox</em>. 
 * Each checkbox may consist of the following elements and components:
 * </p>
 * <ul>
 *   <li>a &lt;span&gt; element</li>
 *   <li>an &lt;input&gt; element of type <em>checkbox</em></li>
 *   <li>an optional image, if the <code>imageURL</code>
 *       attribute or an <code>image</code> facet is specified. If the
 *       <code>imageURL</code> is specified and no image facet exists
 *       a <code>com.sun.rave.web.ui.component.ImageComponent</code> is created
 *       and rendered. If an <code>image</code> facet is specified then the 
 *       component specified by the facet is rendered.</li>
 *   <li>an optional label, if the <code>label</code>
 *       attribute or a <code>label</code> facet is specified. If the
 *       <code>label</code> attribute is specified and no label facet exists
 *       a <code>com.sun.rave.web.ui.component.Label</code> is created and rendered
 *       If a <code>label</code> facet is specified then 
 *       the component specified by the facet is rendered.</li>
 * </ul>
 * <p>
 * The id attributes for HTML elements and components are constructed as follows,
 * where <em>cid</em> is the <code>clientId</code> of the component
 * being rendered.
 * </p>
 * <ul>
 *   <li> <em>cid</em><b>_span</b> for the &lt;span&gt; element</li>
 *   <li> <em>cid</em> for the &lt;input&gt; element</li>
 *   <li> <em>cid</em><b>_image</b> for the image component if created.</li>
 *   <li> <em>cid</em><b>_label</b> for the label component if created.</li>
 * </ul>
 * <h3>Client Side Javascript Functions </h3>
 * <ul>
 *   <li><em>checkbox_setChecked(elementId, checked)</em>: Set the 
 * 	  checked property for a checkbox with the given element id,
 * 	  <em>elementId</em>.
 * 	  If <em>checked</em> is true, the checkbox is checked.
 * 	  If <em>checked</em> is false, the checkbox is unchecked.</li>
 * </ul>
 * <p></p>
 * <!--
 * 	<h3>Theme Identifiers</h3>
 * <p>
 * 	<lo>
 * 	<li>Cb for the INPUT element</li>
 * 	<li>CbDis for the INPUT element for disabled checkbox</li>
 * 	<li>CbLbl for a LABEL element of a checkbox</li>
 * 	<li>CbLblDis for a LABEL element of a disabled checkbox</li>
 * 	<li>CbImg for an IMG element of a checkbox</li>
 * 	<li>CbImgDis for an IMG element of a disabled checkbox</li>
 * 	</lo>
 * </p>
 * -->
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class CheckboxBase extends com.sun.rave.web.ui.component.RbCbSelector {

    /**
     * <p>Construct a new <code>CheckboxBase</code>.</p>
     */
    public CheckboxBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Checkbox");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Checkbox";
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
