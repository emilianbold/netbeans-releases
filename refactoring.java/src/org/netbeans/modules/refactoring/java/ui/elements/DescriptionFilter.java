/*
 * ClassMemberFilters.java
 *
 * Created on November 9, 2006, 5:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.refactoring.java.ui.elements;


import java.util.Collection;
import org.netbeans.modules.refactoring.java.ui.elements.ElementNode.Description;

/** 
 * Creates filtering for the ClassMemberPanel
 *
 * @author Ralph Ruijs
 */
public interface DescriptionFilter {
    
    public Collection<Description> filter( Collection<Description> original );
    public void setNaturalSort( boolean naturalSort );
    public boolean isNaturalSort();
}
