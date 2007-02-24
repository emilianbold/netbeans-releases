/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.ui.products.ad.graphobjects;

import org.netbeans.modules.uml.common.generics.IteratorT;
import com.tomsawyer.graph.TSGraph;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEGraphManager;
import com.tomsawyer.editor.complexity.*;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSNode;
import com.tomsawyer.graph.event.TSEventManager;
import com.tomsawyer.editor.event.TSEEventManager;
import com.tomsawyer.event.TSEvent;
import com.tomsawyer.editor.event.TSEViewportChangeEvent;
import com.tomsawyer.drawing.TSDGraph;
import org.netbeans.modules.uml.core.support.Debug;
import org.openide.ErrorManager;

public class ETGraphManager extends TSEGraphManager
{
	protected boolean m_readingGMF = false;
	
	/**
	 * Constructor of the class.
	 */
	public ETGraphManager()
	{
		super();
		this.init();
	}

	private void init()
	{
		TSENestingManager.getManager(this);
		TSEHidingManager.getManager(this);
		TSEFoldingManager.getManager(this);
	}
	
	/*
	 * Returns true if the graph manager is reading a file.
	 */
	public boolean isReadingGMF()
	{
		return m_readingGMF;
	}

	public void onFailedToLoadDiagram()
	{
		IteratorT<TSEGraph> iter = new IteratorT<TSEGraph>( this.buildGraphs(REACHABLE));
		while (iter.hasNext())
		{
			iter.next().setBoundsUpdatingEnabled(true);
		}
		m_readingGMF = false;
	}
	
        public void setGraphWindow(com.tomsawyer.editor.TSEGraphWindow window)
        {
            super.setGraphWindow(window);
        }
        
	/**
	 * This method allocates a new graph for this graph manager. 
	 * This method should be implemented to enable TSEGraph inheritance.
	 *
	 * @return an object of the type derived from TSEGraph
	 */
	protected TSGraph newGraph()
	{
		ETGraph graph = new ETGraph();
		if (this.m_readingGMF && graph != null)
		{
			// We turn this back on in the drawingArea control
			graph.setBoundsUpdatingEnabled(false);
		}
		return graph;
	}

        
        /**
         * This method allocates a new edge for this graph. This method
         * should be implemented to enable <code>TSEEdge</code> inheritance.
         *
         * @return an object of the type derived from <code>TSEEdge</code>
         */
        protected TSEdge newEdge() {
            return new ETEdge();
        }        
        

	/**
	 * This method copies attributes of the source object to this
	 * object. The source object has to be of the type compatible
	 * with this class (equal or derived). The method should make a
	 * deep copy of all instance variables declared in this class.
	 * Variables of simple (non-object) types are automatically copied
	 * by the call to the copy method of the super class.
	 *
	 * @param sourceObject the source from which all attributes must
	 *                     be copied
	 */
	public void copy(Object sourceObject)
	{
		// copy the attributes of the super class first
		super.copy(sourceObject);

		// copy any class specific attributes here
		// ...
	}

        //Jyothi: Uncommenting this to support gmf format..
	/* (non-Javadoc)
	 * @see com.tomsawyer.graph.TSGraphManager#readGMF(java.io.Reader, boolean)
	*/
	public void readGMF(Reader arg0, boolean arg1) throws IOException
	{
		m_readingGMF = true;
		super.readGMF(arg0, arg1);
		m_readingGMF = false;
	}
        
	/* (non-Javadoc)
	 * @see com.tomsawyer.graph.TSGraphManager#readGMF(java.io.Reader)
	*/
	public void readGMF(Reader arg0) throws IOException
	{
		m_readingGMF = true;
		super.readGMF(arg0);
		m_readingGMF = false;
	}        
        
        
        //Jyothi: adding this method for testing purposes only.. should be deleted later..
        /**
         * This method returns a new event manager. By overriding this method, one can
         * create event manager of user-defined types.
         *
         * @return a non-null event manager of type equal to<code>TSEEventManager</code>
         *         or derived from it.
         */
        public TSEventManager newEventManager() {
            return new TSEEventManager() {
                /**
                 * This method fires the specified event to all thelisteners who are
                 * interested in it.
                 */
                public boolean fireEvent(TSEvent event) {
                    //XXX - Kris - uncommented following two lines to see event
//                    ErrorManager.getDefault().log(ErrorManager.WARNING, getEventManager().isCoalesce() + " " +getEventManager().isFiringEvents());
//                    ErrorManager.getDefault().log(ErrorManager.WARNING, " Firing event : event = "+event.getClass()+"  fulltype = "+TSEventManager.getFullType(event));
                    
                    
                    return super.fireEvent(event);
                }
                
                protected void fireNotification(java.util.EventListener listener, TSEvent event) {
                    //XXX - Kris - added to track stack overflow
//                    String myEvent = this.getFullType(event) ;
//                    String report = "***"+myEvent ;
//                    if (myEvent.equalsIgnoreCase("UI PROPERTY CHANGED")) {
//                        report += "\n" ;
//                        StackTraceElement[] ste = (new Throwable()).getStackTrace() ;
//                        int i = 0;
//                        for (StackTraceElement elem: ste) {
//                            if (i++ > 100) break;
//                            report += "["+i+"] "+elem +"\n";
//                        }
//                       ErrorManager.getDefault().log(ErrorManager.WARNING, report ); 
//                    } else {
//                    ErrorManager.getDefault().log(ErrorManager.WARNING, report );
                    super.fireNotification(listener, event);
//                    }
                }
            };
	}
        
        /**
         * This method sets the main display graph of this drawing manager.
         */
        public void setMainDisplayGraph(TSDGraph newMainDisplayGraph) {
            //new Exception().printStackTrace();
            super.setMainDisplayGraph(newMainDisplayGraph);
        }

}
