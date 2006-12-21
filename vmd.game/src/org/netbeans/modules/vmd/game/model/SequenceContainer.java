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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.game.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.vmd.game.editor.sequece.SequenceContainerEditor;

/**
 *
 * @author kaja
 */
public interface SequenceContainer extends Editable {
	
	public static final String PROPERTY_DEFAULT_SEQUENCE = "sequencecontainer.prop.defaultsequence";
	
	public void addPropertyChangeListener(PropertyChangeListener l);
	
	public void removePropertyChangeListener(PropertyChangeListener l);
	
	public void addSequenceContainerListener(SequenceContainerListener listener);
	
	public void removeSequenceContainerListener(SequenceContainerListener listener);
	
	public boolean append(Sequence sequence);
	
	public boolean insert(Sequence sequence, int index);
	
	public boolean remove(Sequence sequence);
	
	public void move(Sequence sequence, int newIndex);
	
	/**
	 * Create a duplicate of sequence
	 * Implementation must register the sequence in the list of all sequences available for it's ImageResource
	 * by calling ImageResource.addSequence(Sequence)
	 */
	public Sequence createSequence(String string, Sequence sequence);
	
	/**
	 * Implementation must register the sequence in the list of all sequences available for it's ImageResource
	 * by calling ImageResource.addSequence(Sequence)
	 *
	 */
	public Sequence createSequence(String name, int numberFrames);
	
	public Sequence getDefaultSequence();
	
	public Sequence getSequenceAt(int index);
	
	public Sequence getSequenceByName(String name);
	
	public int getSequenceCount();
	
	public List<Sequence> getSequences();
	
	public void setDefaultSequence(Sequence defaultSequence);
	
	public String getName();
	
	public int indexOf(Sequence sequence);

	/**
	 * Default implementation of SequenceContainer.
	 */
	public static class SequenceContainerImpl implements  SequenceContainer {
	
		public static final boolean DEBUG = false;

		EventListenerList listenerList;
		PropertyChangeSupport propertyChangeSupport;
		
		private List<Sequence> sequences;
		private Sequence defaultSequence;
		private SequenceContainer aggregator;
		
		private ImageResource imageResource;
		private SequenceContainerEditor editor;
		
		public SequenceContainerImpl(SequenceContainer aggregator, EventListenerList ll, PropertyChangeSupport pcs, ImageResource imageResource) {
			this.aggregator = aggregator;
			this.listenerList = (ll == null ? new EventListenerList(): ll);
			this.propertyChangeSupport = (pcs == null ? new PropertyChangeSupport(aggregator) : pcs);
			this.sequences = new ArrayList();
			this.imageResource = imageResource;
		}
		
		public void addPropertyChangeListener(PropertyChangeListener l) {
			propertyChangeSupport.addPropertyChangeListener(l);
		}

		public void removePropertyChangeListener(PropertyChangeListener l) {
			propertyChangeSupport.removePropertyChangeListener(l);
		}

		public void addSequenceContainerListener(SequenceContainerListener listener) {
			this.listenerList.add(SequenceContainerListener.class, listener);
		}
	
		public void removeSequenceContainerListener(SequenceContainerListener listener) {
			this.listenerList.remove(SequenceContainerListener.class, listener);
		}
	
		protected void fireSequenceAdded(Sequence sequence, int index) {
			Object[] listeners = listenerList.getListenerList();
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == SequenceContainerListener.class) {
					((SequenceContainerListener) listeners[i+1]).sequenceAdded(this.aggregator, sequence, index);
				}
			}
		}
		
		protected void fireSequenceMoved(Sequence sequence, int oldIndex, int newIndex) {
			Object[] listeners = listenerList.getListenerList();
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == SequenceContainerListener.class) {
					((SequenceContainerListener) listeners[i+1]).sequenceMoved(this.aggregator, sequence, oldIndex, newIndex);
				}
			}
		}
		
		protected void fireSequenceRemoved(Sequence sequence, int index) {
			Object[] listeners = listenerList.getListenerList();
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == SequenceContainerListener.class) {
					((SequenceContainerListener) listeners[i+1]).sequenceRemoved(this.aggregator, sequence, index);
				}
			}
		}
		
		//------SequenceContainer-------
		public Sequence createSequence(String name, int numberFrames) {
			Sequence sequence = new Sequence(name, this.imageResource, numberFrames);
			//this registers the sequence in the list of all sequences available for this ImageResource
			this.imageResource.addSequence(sequence);
			this.append(sequence);
			return sequence;
		}
		
		public Sequence createSequence(String name, Sequence sequence) {
			Sequence newSequence = new Sequence(name, sequence);
			this.imageResource.addSequence(sequence);
			this.append(sequence);
			return newSequence;
		}

		
		public boolean append(Sequence sequence) {
			if (DEBUG) System.out.println(this + " append " + sequence);
			if (this.sequences.contains(sequence)) {
				if (!this.remove(sequence))
					return false;
			}
			this.sequences.add(sequence);
			int index = this.sequences.indexOf(sequence);
			this.fireSequenceAdded(sequence, index);
			return true;
		}
		
		public boolean insert(Sequence sequence, int index) {
			if (DEBUG) System.out.println(this + " insert " + sequence + " at " + index);
			if (this.sequences.contains(sequence)) {
				if (!this.remove(sequence))
					return false;
			}
			this.sequences.add(index, sequence);
			this.fireSequenceAdded(sequence, index);
			return true;
		}
		
		public boolean remove(Sequence sequence) {
			if (DEBUG) System.out.println(this + " remove " + sequence);
			//cannot remove Default sequence
			if (this.getDefaultSequence() == sequence)
				return false;
			
			int index = this.sequences.indexOf(sequence);
			if (this.sequences.remove(sequence)) {
				this.fireSequenceRemoved(sequence, index);
			}
			return true;
		}
		
		public void move(Sequence sequence, int newIndex) {
			int oldIndex = this.sequences.indexOf(sequence);
			if (oldIndex == -1) {
				if (DEBUG) System.out.println(this + " cannot move " + sequence + " - it is not present");
				return;
			}
			if (DEBUG) System.out.println(this + " move " + sequence + " from " + oldIndex + " to " + newIndex);
			this.sequences.remove(sequence);
			this.sequences.add(newIndex, sequence);
			this.fireSequenceMoved(sequence, oldIndex, newIndex);
		}
		
		public Sequence getSequenceByName(String name) {
			Sequence seq = null;
			for (Iterator iter = this.sequences.iterator(); iter.hasNext();) {
				Sequence tmp = (Sequence) iter.next();
				if (tmp.getName().equals(name)) {
					seq = tmp;
					break;
				}
			}
			return seq;
		}
		
		public void setDefaultSequence(Sequence defaultSequence) {
			if (DEBUG) System.out.println("DefaultSequence set to: " + defaultSequence);
			Sequence oldDef = this.defaultSequence;
			if (!this.sequences.contains(defaultSequence)) {
				this.append(defaultSequence);
			}
			this.defaultSequence = defaultSequence;
			this.propertyChangeSupport.firePropertyChange(PROPERTY_DEFAULT_SEQUENCE, oldDef, defaultSequence);
		}
		
		public Sequence getDefaultSequence() {
			return (Sequence) this.defaultSequence;
		}
		
		public int indexOf(Sequence sequence) {
			return this.sequences.indexOf(sequence);
		}
		
		public Sequence getSequenceAt(int index) {
			return (Sequence) this.sequences.get(index);
		}
		
		public String getName() {
			throw new UnsupportedOperationException("Must be overriden by aggregating class.");
		}
		
		public List getSequences() {
			return Collections.unmodifiableList(this.sequences);
		}
		
		public int getSequenceCount() {
			return this.sequences.size();
		}

		public JComponent getEditor() {
			return this.editor == null ? this.editor = new SequenceContainerEditor(this) : this.editor;
		}

		public ImageResource getImageResource() {
			return this.imageResource;
		}

		public JComponent getNavigator() {
			return null;
		}
		
	}
	
}
