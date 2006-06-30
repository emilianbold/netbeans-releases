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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy;

import java.awt.Component;
import java.awt.Container;

/**
 *
 * Contains methods to search for components below a
 * a given <code>java.awt.Container</code> in the display containment hierarchy.
 * Uses a <code>ComponentChooser</code> interface implementation to find a
 * component.
 *
 * @see ComponentChooser
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class ComponentSearcher implements Outputable{

    private int ordinalIndex;
    private Container container;
    private TestOut out;
    private QueueTool queueTool;
    private String containerToString;

    /**
     * Contructor.
     * The search is constrained so that only components that lie below
     * the given container in the containment hierarchy are considered.
     * @param c Container to find components in.
     */
    public ComponentSearcher(Container c) {
	super();
	container = c;
	setOutput(JemmyProperties.getProperties().getOutput());
        queueTool = new QueueTool();
    }
    

    /**
     * Creates <code>ComponentChooser</code> implementation 
     * whose <code>checkComponent(Component)</code>
     * method returns <code>true</code> for any component.
     * @param description Component description.
     * @return ComponentChooser instance.
     */
    public static ComponentChooser getTrueChooser(String description) {
	class TrueChooser implements ComponentChooser {
	    private String description;
	    public TrueChooser(String desc) {
		description = desc;
	    }
	    public boolean checkComponent(Component comp) {
		return(true);
	    }
	    public String getDescription() {
		return(description);
	    }
	}
	return(new TrueChooser(description));
    }

    /**
     * Defines print output streams or writers.
     * 
     * @param	output ?out? Identify the streams or writers used for print output.
     * @see	org.netbeans.jemmy.TestOut
     * @see	org.netbeans.jemmy.Outputable
     * @see #getOutput
     */
    public void setOutput(TestOut output) {
	out = output;
    }

    /**
     * Returns print output streams or writers.
     * @return an object that contains references to objects for
     * printing to output and err streams.
     * @see org.netbeans.jemmy.TestOut
     * @see org.netbeans.jemmy.Outputable
     * @see #setOutput
     */
    public TestOut getOutput() {
	return(out);
    }

    /** Returns container.toString(). It is called in dispatch thread.
     * @return container.toString()
     */
    private String containerToString() {
        if(containerToString == null) {
            containerToString = container == null ? "null" : 
                        (String)queueTool.invokeSmoothly(
                                new QueueTool.QueueAction("container.toString()") {
                                    public Object launch() {
                                        return container.toString();
                                    }
                                }
                        );
        }
        return containerToString;
    }

    /**
     * Searches for a component.
     * The search for the component proceeds recursively in the component hierarchy
     * rooted in this <code>ComponentChooser</code>'s container.
     * @param chooser ComponentChooser instance, defining and applying the
     * search criteria.
     * @param index Ordinal component index.  Indices start at 0.
     * @return the <code>index</code>'th component from among those components
     * for which the chooser's <code>checkComponent(Component)</code> method
     * returns <code>true</code>.
     * A <code>null</code> reference is returned if there are fewer than
     * <code>index-1</code> components meeting the search
     * criteria exist in the component hierarchy rooted in this
     * <code>ComponentChooser</code>'s container.
     */
    public Component findComponent(ComponentChooser chooser, int index) {
	ordinalIndex = 0;
	final Component result = findComponentInContainer(container, chooser, index);
	if(result != null) {
            // get result.toString() - run in dispatch thread
            String resultToString = (String)queueTool.invokeSmoothly(
                            new QueueTool.QueueAction("result.toString()") {
                                public Object launch() {
                                    return result.toString();
                                }
                            }
            );
	    out.printTrace("Component " + chooser.getDescription() +
			   "\n    was found in container " + containerToString() + 
			   "\n    " + resultToString);
	    out.printGolden("Component \"" + chooser.getDescription() + "\" was found"); 
	} else {
	    out.printTrace("Component " + chooser.getDescription() +
			   "\n    was not found in container " + containerToString());
	    out.printGolden("Component \"" + chooser.getDescription() + "\" was not found"); 
	}
	return(result);
    }

    /**
     * Searches for a component.
     * The search for the component proceeds recursively in the component hierarchy
     * rooted in this <code>ComponentChooser</code>'s container.
     * @param chooser ComponentChooser instance, defining and applying the
     * search criteria.
     * @return the first component for which the chooser's
     * <code>checkComponent(Component)</code> method returns <code>true</code>.
     * A <code>null</code> reference is returned if no component meeting the search
     * criteria exist in the component hierarchy rooted in this
     * <code>ComponentChooser</code>'s container.
     */
    public Component findComponent(ComponentChooser chooser) {
	return(findComponent(chooser, 0));
    }

    private Component findComponentInContainer(Container cont, ComponentChooser chooser, int index) {
	Component[] components = cont.getComponents();
	Component target;
	for(int i = 0; i < components.length; i++) {
	    if(components[i] != null) {
		if(chooser.checkComponent(components[i])) {
		    if(ordinalIndex == index) {
			return(components[i]);
		    } else {
			ordinalIndex++;
		    }
		}
		if(components[i] instanceof Container) {
		    if((target = findComponentInContainer((Container)components[i], 
							  chooser, index)) != null) {
			return(target);
		    }
		}
	    }
	}
	return(null);
    }
}
