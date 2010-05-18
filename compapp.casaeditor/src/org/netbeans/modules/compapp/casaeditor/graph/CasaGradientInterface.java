/*
 * CasaGradientInterface.java
 *
 * Created on February 16, 2007, 10:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Rectangle;

/**
 *
 * @author rdara
 */
public interface CasaGradientInterface {
    
    GradientRectangleColorScheme getGradientColorSceheme();
    boolean isBorderShown();
    Rectangle getRectangleToBePainted();
}
