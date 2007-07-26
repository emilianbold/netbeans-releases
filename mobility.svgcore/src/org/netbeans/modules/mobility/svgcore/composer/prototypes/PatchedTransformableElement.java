/*
 * PatchedElement.java
 * 
 * Created on Jun 5, 2007, 2:41:06 PM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.svgcore.composer.prototypes;

import com.sun.perseus.j2d.Transform;

/**
 *
 * @author Pavel Benes
 */
public interface PatchedTransformableElement extends PatchedElement{
    public Transform getTransform();
    public void      setTransform(Transform tfx);
}
