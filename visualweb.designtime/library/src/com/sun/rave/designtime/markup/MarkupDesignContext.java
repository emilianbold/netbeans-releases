/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.markup;

import java.util.Map;
import java.awt.Image;
import com.sun.rave.designtime.DesignContext;

/**
 * The MarkupDesignContext is an extension to the DesignContext interface that adds markup-specific
 * functionality.  This adds methods for previewing CSS style changes at design-time.  A
 * MarkupDesignContext can be accessed by calling the DesignBean.getDesignContext() method and
 * testing the returned DesignContext for 'instanceof' MarkupDesignContext.  If the file being
 * designed is a markup backing file (eg: *.jsp), the DesignContext will be an instanceof
 * MarkupDesignContext.
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DesignContext
 * @see com.sun.rave.designtime.DesignBean#getDesignContext()
 */
public interface MarkupDesignContext extends DesignContext {

    /**
     * Generates and returns a preview image (sized width x height) of the specified MarkupDesignBean
     * after applying the specified CSS styles and style classes to the rendered markup.
     *
     * @param cssStyle The CSS style string to apply to the rendered markup from the
     *        MarkupDesignBean
     * @param cssStyleClasses The CSS style classes to apply to the rendered markup from the
     *        MarkupDesignBean
     * @param designBean The MarkupDesignBean to render the image of
     * @param width The desired width of the resulting image (in pixels)
     * @param height The desired height of the resulting image (in pixels)
     * @return The generated Image representing the MarkupDesignBean render output with the applied
     *         styles
     */
    public Image getCssPreviewImage(String cssStyle, String[] cssStyleClasses,
        MarkupDesignBean designBean, int width, int height);

    /**
     * Converts a CSS inline style string into a Map of style elements
     *
     * @param cssStyle The CSS inline style string to convert
     * @return A Map containing the parsed CSS styles
     */
    public Map convertCssStyleToMap(String cssStyle);

    /**
     * Converts a Map of CSS styles into an inline CSS style string
     *
     * @param cssStyleMap The Map of CSS styles to convert
     * @return An inline CSS style string
     */
    public String convertMapToCssStyle(Map cssStyleMap);
}

