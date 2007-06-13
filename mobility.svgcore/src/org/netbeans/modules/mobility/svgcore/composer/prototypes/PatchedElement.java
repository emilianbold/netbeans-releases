/*
 * PatchedElement.java
 * 
 * Created on Jun 5, 2007, 2:41:06 PM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.svgcore.composer.prototypes;

import org.netbeans.modules.mobility.svgcore.composer.SVGObject;

/**
 *
 * @author Pavel Benes
 */
public interface PatchedElement {
    public void      attachSVGObject(SVGObject obj);
    public SVGObject getSVGObject();
    public void      setNullId(boolean isNull);
    public void      setPath(int [] path);
    public int []    getPath();
}
