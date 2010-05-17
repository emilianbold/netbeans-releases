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
package com.sun.rave.web.ui.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

/**
 * The tab tag is used for inserting a Tab component into a TabSet.
 * The tab tag simply extends <a href="hyperlink.html">ui:hyperlink</a> differing
 * only in the value rendered by default for the class attriubte<br>
 * <h3>HTML Elements and Layout</h3>
 * The tab tag will render an XHTML anchor tag. Its output is identical to that of
 * <a href="hyperlink.html">ui:hyperlink</a> except for the value of the class
 * attribute
 * <br>
 * <h3>Client Side Javascript Functions</h3>
 * none.
 * <br>
 * <h3>Examples</h3>
 * <p><em>Please see <a href="tabSet.html">the tabSet tlddoc</a> for an example of 
 * defining tabs via a TabSet component binding</em></p>
 * <p><strong>Define three tabs as part of a TabSet</strong></p>
 * <p><code>&lt;ui:tabSet id="MyTabs" selected="tab1" &gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:tab id="tab1" 
 * text="Tab 1" action="#{TabSetBean.tab1Clicked}" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:tab id="tab2"
 * text="Tab 2" action="#{TabSetBean.tab2Clicked}" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:tab id="tab3"
 * text="Tab 3" action="#{TabSetBean.tab3Clicked}" /&gt;<br>
 * &lt;/ui:tabSet &gt;<br>
 * </code></p>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class TabBase extends com.sun.rave.web.ui.component.Hyperlink {

    /**
     * <p>Construct a new <code>TabBase</code>.</p>
     */
    public TabBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Tab");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Tab";
    }

    // selectedChildId
    private String selectedChildId = null;

    /**
 * <p>The id of this tab's currently selected Tab child or null if one is not selected.</p>
     */
    public String getSelectedChildId() {
        if (this.selectedChildId != null) {
            return this.selectedChildId;
        }
        ValueBinding _vb = getValueBinding("selectedChildId");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The id of this tab's currently selected Tab child or null if one is not selected.</p>
     * @see #getSelectedChildId()
     */
    public void setSelectedChildId(String selectedChildId) {
        this.selectedChildId = selectedChildId;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.selectedChildId = (String) _values[1];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[2];
        _values[0] = super.saveState(_context);
        _values[1] = this.selectedChildId;
        return _values;
    }

}
