/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.markup;

import com.sun.rave.designtime.DesignInfo;

/**
 * <P>The MarkupDesignInfo interface is an extension of the DesignInfo interface that adds the
 * ability to annotate the render stream of a markup element for design-time, and provide clickable
 * sub-regions and context items within the visual markup element.</P>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DesignInfo
 */
public interface MarkupDesignInfo extends DesignInfo {

    /**
     * <p>This method is called <b>after</b> a JSF component has been invoked to render itself
     * in design-mode, and just <b>before</b> the rendered markup is displayed on the designer
     * surface.  The component author may use this hook to modify the design-time rendered content
     * for the given component.  They can also associate elements with MarkupMouseRegion
     * objects.</p>
     *
     * <p>For example, the following markup might be emitted when a JSF component is rendered:</p>
     *
     * <p><pre>
     * &lt;table border=&quot;1&quot;&gt;
     *   &lt;tr&gt;
     *     &lt;th&gt;Name&lt;/th&gt;
     *     &lt;th&gt;Address&lt;/th&gt;
     *     &lt;th&gt;Phone&lt;/th&gt;
     *   &lt;/tr&gt;
     *   &lt;tr&gt;
     *     &lt;td&gt;John Smith&lt;/td&gt;
     *     &lt;td&gt;100 Milky Way&lt;/td&gt;
     *     &lt;td&gt;555-1212&lt;/td&gt;
     *   &lt;/tr&gt;
     *   &lt;tr&gt;
     *     &lt;td&gt;Luke Johnson&lt;/td&gt;
     *     &lt;td&gt;200 Super Drive&lt;/td&gt;
     *     &lt;td&gt;911-1234&lt;/td&gt;
     *   &lt;/tr&gt;
     *   &lt;tr&gt;
     *     &lt;td&gt;Gary Raddo&lt;/td&gt;
     *     &lt;td&gt;747 Airport Way&lt;/td&gt;
     *     &lt;td&gt;277-9473&lt;/td&gt;
     *   &lt;/tr&gt;
     * &lt;/table&gt;
     * </pre></p>
     *
     * <p>This markup is parsed into a DOM DocumentFragment, and passed to the 'customizeRender'
     * method along with the instance of the DesignBean that rendered it.  The resultant DOM
     * DocumentFragment might look like this:</p>
     *
     * <p><pre>
     * &lt;table border=&quot;1&quot;&gt;
     *   &lt;tr&gt;
     *     &lt;th&gt;Name [CUSTOMER.NAME]&lt;/th&gt;
     *     &lt;th&gt;Address [CUSTOMER.ADDRESS]&lt;/th&gt;
     *     &lt;th&gt;Phone [CUSTOMER.PHONE]&lt;/th&gt;
     *   &lt;/tr&gt;
     *   &lt;tr&gt;
     *     &lt;td&gt;John Smith&lt;/td&gt;
     *     &lt;td&gt;100 Milky Way&lt;/td&gt;
     *     &lt;td&gt;555-1212&lt;/td&gt;
     *   &lt;/tr&gt;
     *   &lt;tr&gt;
     *     &lt;td&gt;Luke Johnson&lt;/td&gt;
     *     &lt;td&gt;200 Super Drive&lt;/td&gt;
     *     &lt;td&gt;911-1234&lt;/td&gt;
     *   &lt;/tr&gt;
     *   &lt;tr&gt;
     *     &lt;td&gt;Gary Raddo&lt;/td&gt;
     *     &lt;td&gt;747 Airport Way&lt;/td&gt;
     *     &lt;td&gt;277-9473&lt;/td&gt;
     *   &lt;/tr&gt;
     * &lt;/table&gt;
     * </pre></p>
     *
     * <p>This annotated markup stream would be passed to the designer, where it would be displayed
     * as an instance of an HTML table (in this example).  The component author may have also
     * 'hooked' the markup elements to instances of MarkupMouseRegion via the
     * 'MarkupRenderContext.associateMouseRegion' method.</p>
     *
     * @param designBean The MarkupDesignBean about to be displayed on the design surface
     * @param renderContext The context (including rendered content) representing this component's
     *        markup output
     */
    public void customizeRender(MarkupDesignBean designBean, MarkupRenderContext renderContext);
}
