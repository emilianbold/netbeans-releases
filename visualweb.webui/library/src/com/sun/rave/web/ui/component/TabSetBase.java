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
 * <h3>About This Tag</h3>
 * 
 * <p>The TabSet renders a set of Tab children. It keeps track of the currently
 * selected child Tab as well as applying any specified ActionListener.</p>
 * 
 * <h3>Configuring the TabSet Tag</h3>
 * 
 * <p>The TabSet can currently be used in one of two ways: via a component binding to a
 * TabSet and group of child Tab components (defined in a backing bean); or by specifying the
 * TabSet and child Tabs directly in your JSP.</p><p>Examples of both are shown in the Examples
 * section below. It is anticipated that the component binding method will be more
 * common as this allows a single set of Tabs to be easily shared among many pages. In
 * either case, the initial selection for the TabSet component can be specified using the
 * &quot;selected&quot; property. Note that if an ActionListener is applied to the TabSet
 * component, it adds the specified ActionListener to each of its child Tab components action
 * listener lists.</p>
 * 
 * <h3>Facets</h3>
 * 
 * <p>None at this time</p>
 * 
 * <h3>Client Side Javascript Functions</h3>
 * 
 * <p>None at this time</p>
 * 
 * <h3>Examples</h3>
 * 
 * <p><strong>Example 1: Define the TabSet via a component binding</strong><br>
 * One way a TabSet component can be specified is via a JSF component binding to an instance
 * defined in a backing bean. The contents of the JSP in this case will simply be something
 * like:</p>
 * 
 * <p><code>&lt;ui:tabSet binding=&quot;#{TabSetBean.sportsTabSet}&quot; /&gt;</code></p>
 * 
 * <p>The code in the corresponding backing bean instance would look something like:</p>
 * 
 * <p><code>import java.util.List;<br>
 * import java.lang.Class;<br>
 * import javax.faces.FactoryFinder;<br>
 * import javax.faces.el.MethodBinding;<br>
 * import javax.faces.event.ActionEvent;<br>
 * import javax.faces.application.Application;<br>
 * import javax.faces.application.ApplicationFactory;<br>
 * import com.sun.rave.web.ui.component.Tab;<br>
 * import com.sun.rave.web.ui.component.TabSet;</p>
 * 
 * <p>public class TabSetBean {<br>
 * &nbsp;&nbsp;&nbsp; private TabSet sportsTabSet = null;<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;  // Creates a new instance of TabSetBean //<br>
 * &nbsp;&nbsp;&nbsp; public TabSetBean() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sportsTabSet = new TabSet();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; List kids = sportsTabSet.getChildren();<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Tab level1Tab = new Tab(&quot;Baseball&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; level1Tab.setId(&quot;Baseball&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Tab level2Tab = addTab(level1Tab,
 * &quot;National&quot;); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Mets&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Pirates&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Cubs&quot;);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; level2Tab = addTab(level1Tab,
 * &quot;American&quot;); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Yankees&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Tigers&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Mariners&quot;);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; level2Tab = addTab(level1Tab, &quot;AAA&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Spinners&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Renegades&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Clippers&quot;); <br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; kids.add(level1Tab);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; level1Tab = new Tab(&quot;Football&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; level1Tab.setId(&quot;Football&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; level2Tab = addTab(level1Tab, &quot;NFC&quot;);
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Giants&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Bears&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Falcons&quot;);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; level2Tab = addTab(level1Tab, &quot;AFC&quot;);
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Jets&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Patriots&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Colts&quot;);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; level2Tab = addTab(level1Tab,
 * &quot;College&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Wolverines&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Hurricanes&quot;);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; addTab(level2Tab, &quot;Buckeyes&quot;);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; kids.add(level1Tab);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Class[] args = new Class[] { ActionEvent.class
 * };<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; MethodBinding binding =
 * createBinding(&quot;#{TabSetBean.tabClicked}&quot;, args);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sportsTabSet.setActionListener(binding); <br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sportsTabSet.setSelected(&quot;Jets&quot;);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; private MethodBinding createBinding(String expr, Class[] args) { <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ApplicationFactory factory =
 * (ApplicationFactory)<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Application app = factory.getApplication();<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return app.createMethodBinding(expr, args);<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; private Tab addTab(Tab parent, String newTabLabel) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Tab tab = new Tab(newTabLabel);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; tab.setId(newTabLabel); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; parent.getChildren().add(tab);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return tab;<br>
 * &nbsp;&nbsp;&nbsp; } </p>
 * 
 * <p>&nbsp;&nbsp;&nbsp; public void tabClicked(ActionEvent event) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; String clickedTabId = event.getComponent().getId():<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; String selectedTabId = sportsTabSet.getSelected();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // ... do sometehing based upon the clicked or
 * selected tab id ...<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; public TabSet getSportsTabSet() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return sportsTabSet;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; public void setSportsTabSet(TabSet tabs) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sportsTabSet = tabs;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * }</code></p>
 * 
 * <p><strong>Example 2: Define the TabSet in your JSP</strong><br>
 * A tabSet can also be defined directly in your JSP. The following example defines a set of tabs with
 * three level one tabs (labelled &quot;One&quot;, &quot;Two&quot; and &quot;Three&quot;). Each
 * level one tab also has two level two tab childeren (labelled &quot;XxxA&quot; and
 * &quot;XxxB&quot; where X is the top level tab number. The initially selected Tab for this
 * TabSet will be &quot;TwoA&quot;.</p>
 * 
 * <p><code>&lt;ui:tabSet selected=&quot;TwoA&quot;&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tab id=&quot;One&quot; text=&quot;One&quot;&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:tab id=&quot;OneA&quot; text=&quot;One
 * A&quot; /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:tab id=&quot;OneB&quot; text=&quot;One
 * B&quot; /&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tab&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tab id=&quot;Two&quot; text=&quot;Two&quot;&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:tab id=&quot;TwoA&quot; text=&quot;Two
 * A&quot; /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:tab id=&quot;TwoB&quot; text=&quot;Two
 * B&quot; /&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tab&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tab id=&quot;Three&quot; text=&quot;Three&quot;&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:tab id=&quot;ThreeA&quot;
 * text=&quot;Three A&quot; /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:tab id=&quot;ThreeB&quot;
 * text=&quot;Three B&quot; /&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tab&gt; <br>
 * &lt;/ui:tabSet&gt;</code></p>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class TabSetBase extends javax.faces.component.UINamingContainer {

    /**
     * <p>Construct a new <code>TabSetBase</code>.</p>
     */
    public TabSetBase() {
        super();
        setRendererType("com.sun.rave.web.ui.TabSet");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.TabSet";
    }

    // actionListener
    private javax.faces.el.MethodBinding actionListener = null;

    /**
 * <p>Use the actionListener attribute to cause the hyperlink to fire an
 *         event. The value must be an EL expression and it must evaluate to the 
 *         name of a public method that takes an ActionEvent parameter and returns
 *         void.</p>
     */
    public javax.faces.el.MethodBinding getActionListener() {
        if (this.actionListener != null) {
            return this.actionListener;
        }
        ValueBinding _vb = getValueBinding("actionListener");
        if (_vb != null) {
            return (javax.faces.el.MethodBinding) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Use the actionListener attribute to cause the hyperlink to fire an
 *         event. The value must be an EL expression and it must evaluate to the 
 *         name of a public method that takes an ActionEvent parameter and returns
 *         void.</p>
     * @see #getActionListener()
     */
    public void setActionListener(javax.faces.el.MethodBinding actionListener) {
        this.actionListener = actionListener;
    }

    // lite
    private boolean lite = false;
    private boolean lite_set = false;

    /**
 * <p>Render a style of tabs that isn't so visually "heavy".  This property
 *         must be used in conjunction with the "mini" property set to true
 *         in order to work.</p>
     */
    public boolean isLite() {
        if (this.lite_set) {
            return this.lite;
        }
        ValueBinding _vb = getValueBinding("lite");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Render a style of tabs that isn't so visually "heavy".  This property
 *         must be used in conjunction with the "mini" property set to true
 *         in order to work.</p>
     * @see #isLite()
     */
    public void setLite(boolean lite) {
        this.lite = lite;
        this.lite_set = true;
    }

    // mini
    private boolean mini = false;
    private boolean mini_set = false;

    /**
 * <p>Specify "true" if this TabSet should have the mini style</p>
     */
    public boolean isMini() {
        if (this.mini_set) {
            return this.mini;
        }
        ValueBinding _vb = getValueBinding("mini");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Specify "true" if this TabSet should have the mini style</p>
     * @see #isMini()
     */
    public void setMini(boolean mini) {
        this.mini = mini;
        this.mini_set = true;
    }

    // selected
    private String selected = null;

    /**
 * <p>The id of the selected tab</p>
     */
    public String getSelected() {
        if (this.selected != null) {
            return this.selected;
        }
        ValueBinding _vb = getValueBinding("selected");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The id of the selected tab</p>
     * @see #getSelected()
     */
    public void setSelected(String selected) {
        this.selected = selected;
    }

    // style
    private String style = null;

    /**
 * <p>CSS style(s) to be applied when this component is rendered.</p>
     */
    public String getStyle() {
        if (this.style != null) {
            return this.style;
        }
        ValueBinding _vb = getValueBinding("style");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>CSS style(s) to be applied when this component is rendered.</p>
     * @see #getStyle()
     */
    public void setStyle(String style) {
        this.style = style;
    }

    // styleClass
    private String styleClass = null;

    /**
 * <p>CSS style class(es) to be applied when this component is rendered.</p>
     */
    public String getStyleClass() {
        if (this.styleClass != null) {
            return this.styleClass;
        }
        ValueBinding _vb = getValueBinding("styleClass");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>CSS style class(es) to be applied when this component is rendered.</p>
     * @see #getStyleClass()
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    // visible
    private boolean visible = false;
    private boolean visible_set = false;

    /**
 * <p>Use the visible attribute to indicate whether the component should be
 *     viewable by the user in the rendered HTML page. If set to false, the
 *     HTML code for the component is present in the page, but the component
 *     is hidden with style attributes. By default, visible is set to true, so
 *     HTML for the component HTML is included and visible to the user. If the
 *     component is not visible, it can still be processed on subsequent form
 *     submissions because the HTML is present.</p>
     */
    public boolean isVisible() {
        if (this.visible_set) {
            return this.visible;
        }
        ValueBinding _vb = getValueBinding("visible");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return true;
    }

    /**
 * <p>Use the visible attribute to indicate whether the component should be
 *     viewable by the user in the rendered HTML page. If set to false, the
 *     HTML code for the component is present in the page, but the component
 *     is hidden with style attributes. By default, visible is set to true, so
 *     HTML for the component HTML is included and visible to the user. If the
 *     component is not visible, it can still be processed on subsequent form
 *     submissions because the HTML is present.</p>
     * @see #isVisible()
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        this.visible_set = true;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.actionListener = (javax.faces.el.MethodBinding) restoreAttachedState(_context, _values[1]);
        this.lite = ((Boolean) _values[2]).booleanValue();
        this.lite_set = ((Boolean) _values[3]).booleanValue();
        this.mini = ((Boolean) _values[4]).booleanValue();
        this.mini_set = ((Boolean) _values[5]).booleanValue();
        this.selected = (String) _values[6];
        this.style = (String) _values[7];
        this.styleClass = (String) _values[8];
        this.visible = ((Boolean) _values[9]).booleanValue();
        this.visible_set = ((Boolean) _values[10]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[11];
        _values[0] = super.saveState(_context);
        _values[1] = saveAttachedState(_context, actionListener);
        _values[2] = this.lite ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.lite_set ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.mini ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.mini_set ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = this.selected;
        _values[7] = this.style;
        _values[8] = this.styleClass;
        _values[9] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[10] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
