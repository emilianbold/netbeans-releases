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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.collab.util;

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.channels.*;

import org.apache.log4j.*;
import org.apache.log4j.varia.*;

/**
 * 1. Always register for read select except while reading
 * 2. Register for write select only when data is unable to be written in one go.
 *
 * @author Jacques Belissent
 * @author Vijayakumar Palaniappan
 *
 */
public class SelectWorker implements Runnable
{

    HashSet registerList = new HashSet();
    HashSet interestList = new HashSet();
    HashSet cancelList = new HashSet();
    Selector mySelector;
    boolean stop = false;
    Worker _worker;
    boolean _privateWorker = true;
    private LinkedList _tasks = new LinkedList();

    protected static Logger logger = LogManager.getLogger("nbcollab.nio");
    protected static Logger getLogger() { return logger; }

    class Selection {
        SelectableChannel channel;
        SelectionKey key;
        Runnable readRunnable;
        BufferedByteChannel writer;
        int operations = 0;
        boolean reading = false;
    
        Selection(SelectableChannel channel,
                  Runnable runnable) {
            this.channel = channel;
            readRunnable = runnable;
        }

        Selection(SelectableChannel channel,
                  Runnable readable,
                  BufferedByteChannel writer) {
            this.channel = channel;
            readRunnable = readable;
            this.writer = writer;
        }

        Selection(SelectableChannel channel) {
            this.channel = channel;
        }


        void closeChannel() throws IOException {
            try {
                if (key != null) {
                    key.cancel();
                    if (logger != null) {
                        logger.info("[SelectWorker] cancelled " + channel);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (logger != null) {
                    logger.warn("key cancellation error: " + channel + " err: " + e);
                }
            }
            
            try {
                if (channel.isOpen()) {
                    if (logger != null) {
                        logger.info("[SelectWorker] closing " + channel);
                    }
                    channel.close();
                    if (logger != null) {
                        logger.info("[SelectWorker] closed " + channel);
                    }
                } else {
                    logger.info("[SelectWorker] cancelled channel already closed " + channel);
                }
            } catch (Exception e) {
                if (logger != null) {
                    logger.warn("channel close error: " + channel + " err: " + e);
                }
            }

            key = null;
            readable = null;
            //if (writable != null) writable.setSelectionKey(null);
            //System.out.println("NIODEBUG: closed channel " + channel);
        }

        SelectableChannel getChannel() { return channel; }

        // register is called only once and at that time operations
        // is set.
        void register() throws ClosedChannelException 
        {
            if (channel.isOpen()) {

                key = channel.register(mySelector, SelectionKey.OP_READ, this);
                if (logger != null) {
                    logger.info("SelectWorker registered=" + channel);
                }
            }
        }

        // this is called when select is not currently running,
        // so interestOps should not block.
        void resetInterest()
        {
            if (!channel.isOpen()) {
                if (logger != null) {
                    logger.info("[SelectWorker] NOT resetting interest for closed " + channel);
                }
                return;
            }
            int ops = 0;
            synchronized(this) {
                ops = operations;
                if (logger != null) {
                    logger.debug("[SelectWorker] resetting interest to " + ops + " for " + channel);
                }
                if (ops == 0) return;
                operations = 0;
            }
            if (key != null && key.isValid()) {
                //Append to existing
                key.interestOps(ops | key.interestOps());
                if (logger != null) {
                    logger.debug("[SelectWorker] reset interest to " + ops + " for " + channel);
                }
            }
        }

        // invoked by read and write worker threads when they are
        // finished and ready again to receive events
        void addInterestOps(int ops)
        {
            boolean interestAlreadyQueued = false;
            synchronized(this) {
                // don't set interest yet if reading
                if (reading)  ops &= (SelectionKey.OP_READ ^ 0xffff);
        
                interestAlreadyQueued = (operations != 0);
                operations |= ops;
            }
            if (!interestAlreadyQueued) {
                synchronized(interestList) {
                    if (!interestList.contains(this)) {
                        interestList.add(this);
                        if (logger != null) {
                            logger.debug("[SelectWorker] registering interest to " + ops + " for " + channel);
                        }
                    }
                } 
            }
            mySelector.wakeup();
        } 

        // for debugging
        void print(PrintStream out)
        {
            out.println("[SelectWorker] Selection channel=" + channel + " key=" + key + " readRunnable=" + readRunnable + " writer=" + writer + " operations=" + operations + " reading=" + reading);
        }

        Runnable readable = new Runnable() {
            public void run() {
                if (channel.isOpen()) {
                    //System.out.println("NIODEBUG: readable " + channel);
                    synchronized(Selection.this) {
                        reading = true;
                        operations &= (SelectionKey.OP_READ ^ 0xffff);
                    }
                    try {
                        readRunnable.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    synchronized(Selection.this) {
                        reading = false;
                    }
                    addInterestOps(SelectionKey.OP_READ);
                    //System.out.println("NIODEBUG: read " + channel);
                } else {
                    if (logger != null) {
                        logger.info("[SelectWorker] read, channel already closed: " + channel);
                    }
                }
            }
            
            public String toString() {
                return "" + readRunnable;
            }
        };

        Runnable writable = new Runnable() {
            public void run() {
                if (channel.isOpen()) {
                    //System.out.println("NIODEBUG: writable " + channel);
                    synchronized(Selection.this) {
                        operations &= (SelectionKey.OP_WRITE ^ 0xffff);
                                }
                    try {
                        if (writer.writeNow() > 0) {
                            addInterestOps(SelectionKey.OP_WRITE);
                        }
                        //System.out.println("NIODEBUG: wrote " + channel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (logger != null) {
                        logger.info("[SelectWorker] write, channel already closed: " + channel);
                    }
                }
            }
        };

    }
        
    public SelectWorker(int minThreads, int maxThreads) throws Exception 
    {
        this(new Worker(minThreads, maxThreads, "SelectWorker"));
    }

    public SelectWorker(Worker w) throws Exception 
    {
        _privateWorker = false;
        _worker = w;
        mySelector = Selector.open();
    }

    public void close() {
        stop = true;
        try {
            //Though select.close doc says, the call will wakeup
            //the selector, it does not do so.    
            mySelector.wakeup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        //System.out.println(logPrefix + "read selector starting"); 
        while (!stop) {
            try {
                
                //System.out.println("NIODEBUG: before select");
                int n = mySelector.select();
                if (stop) break;
                
                if (logger != null) {
                    logger.info("NIODEBUG: selected " + n);
                }

                // process pending cancellations
                synchronized (cancelList) {
                    for (Iterator iter = cancelList.iterator();
                         iter.hasNext(); ) {
                        Selection sel = (Selection)iter.next();
                        try {
                            sel.closeChannel();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cancelList.clear();
                }

                // process pending registrations
                synchronized (registerList) {
                    for (Iterator iter = registerList.iterator();
                         iter.hasNext(); ) {
                        ((Selection)iter.next()).register();
                    }
                    registerList.clear();
                }

                // process pending operations
                synchronized(this) {
                    for (Iterator iter = mySelector.selectedKeys().iterator();
                         iter.hasNext(); ) {
                        SelectionKey key = (SelectionKey)iter.next();
                        iter.remove();
                        
                        boolean readable = false, writable = false;
                        if (key.isValid()) {
                            if ((key.interestOps() & SelectionKey.OP_READ) != 0 &&
                                key.isReadable()) {
                                readable = true;
                            }
                            
                            if ((key.interestOps() & SelectionKey.OP_WRITE) != 0 &&
                                key.isWritable()) {
                                writable = true;
                            }

                            //key.cancel();

                            // reset interest ops so this channel is not
                            // selected until after
                            // it has finished processing this event and
                            // is waiting for more.
                            //key.interestOps(0);
                            
                            Selection sel = (Selection)key.attachment();

                            if (logger != null) {
                                logger.info("Selected key readable=" + readable + " writable=" + writable + " channel=" + sel.getChannel());
                            }
            

                            if (writable) {
                                //Remove the write interest alone
                                key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE ));
                                _worker.addRunnable(sel.writable);
                            }
                            if (readable && sel.readable != null) {
                                // do not read more than you can process
                                //Remove the read interest alone
                                key.interestOps(key.interestOps() & (~SelectionKey.OP_READ ));
                                if (_worker.addRunnableIfPossible(sel.readable) < 0) {
                                    // if no jobs are left, put READ back
                                    // on interest list
                                    key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                                } else {
				    if (logger != null) {
					logger.warn("Read was delayed for " + sel.getChannel());
				    }
				}
                            }

                        } else {
                            if (logger != null) {
                                logger.info("invalid key selected");
                            }
                        }
                    }
                }

                // process pending changes of interest
                synchronized (interestList) {
                    for (Iterator iter = interestList.iterator();
                         iter.hasNext(); ) {
                        ((Selection)iter.next()).resetInterest();
                    }
                    interestList.clear();
                }

                // process tasks
                List tasks = null;
                synchronized(_tasks) {
                    if (_tasks.size() > 0) {
                        tasks = (List)_tasks.clone();
                        _tasks.clear();
                    }
                }
                if (tasks != null) {
                    for (Iterator t = tasks.iterator(); t.hasNext(); ) {
                        SelectWorker.Task task = (SelectWorker.Task)t.next();
                        LinkedList mustClose = null;
                        task.starting();
                        for (Iterator iter = mySelector.keys().iterator();
                             iter.hasNext(); ) {
                            SelectionKey key = (SelectionKey)iter.next();
                            Selection sel = (Selection)key.attachment();
                            if (sel != null) {
                                if (!task.process(sel.readRunnable)) {
                                    if (mustClose == null) mustClose = new LinkedList();
                                    mustClose.add(sel);
                                }
                            }
                        }
                        task.completed();

                        if (mustClose != null) {
                            for (Iterator iter = mustClose.iterator();
                                 iter.hasNext(); ) {
                                ((Selection)iter.next()).closeChannel();
                            }
                        }
                    }
                }

            } catch (Exception e) {
		if (logger != null) {
		    logger.error("Select loop error: " + e);
		}
                e.printStackTrace();
                break;
                //System.out.println(logPrefix + e.toString());
            }

        }

        // Out of the while loop, we are finished
        try {
            mySelector.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (_privateWorker) _worker.stop();
    }

    // Place registrations in list. If we try to register a
    // channel when a selector
    // is waiting in select() will cause the register() to block.
    public Selection register(SelectableChannel channel, 
                              Runnable runnable)
        throws IOException
    {
        channel.configureBlocking(false);
        Selection selection = new Selection(channel, runnable);
        register(selection);
        return selection;
    }


    public Selection register(SelectableChannel channel, 
                              Runnable readable, 
                              BufferedByteChannel writable)
        throws IOException
    {
        channel.configureBlocking(false);
        Selection selection = new Selection(channel, readable, writable);
        writable.setSelectionKey(selection);
        register(selection);
        return selection;
    }
    
    private void register(Selection selection) {
        synchronized (registerList) {
            if (!registerList.contains(selection)) registerList.add(selection);
        }
        mySelector.wakeup();
    }

    public void cancel(Object o) {
        if (!(o instanceof Selection)) return;

        synchronized (registerList) {
            registerList.remove(o);
        }
        synchronized (interestList) {
            interestList.remove(o);
        }
        synchronized (cancelList) {
            if (!cancelList.contains(o)) cancelList.add(o);
        }
        mySelector.wakeup();
    }

    public void interestOps(Object o, int ops) {
        if (!(o instanceof Selection)) return;
        ((Selection)o).addInterestOps(ops);
    }

    private Selection getSelection(Object o)
    {
        if (o instanceof Selection) {
            return (Selection)o;
        } else if (o instanceof SelectionKey) {
            Object sel = ((SelectionKey)o).attachment();
            if (!(sel instanceof Selection)) {
                return null;
            } else {
                return (Selection)sel;
            }
        } else {
            throw new IllegalArgumentException("Unexpected argument class: " +
                            ((o != null) ? o.getClass().toString() : "null"));
        }
    }

    public SelectableChannel getChannel(Object o) 
    {
        Selection sel = getSelection(o);
        if (sel != null) return sel.getChannel();
        else return null;
    }

    public SelectionKey getSelectionKey(Object o) 
    {
        if (o instanceof Selection) {
            return ((Selection)o).key;
        } else if (o instanceof SelectionKey) {
            return (SelectionKey)o;
        } else {
            throw new IllegalArgumentException("Unexpected argument class: " +
                            ((o != null) ? o.getClass().toString() : "null"));
        }
    }

    public Object attachment(Object o) 
    {
        Selection sel = getSelection(o);
        if (sel != null) return sel.readRunnable;
        else return null;
    }

    public void print(PrintStream out, Object o) 
    {
        Selection sel = getSelection(o);
        if (sel != null) sel.print(out);
        else out.println("no attachment in selection key " + o);
    }

    public Set keys() { return mySelector.keys(); }

    public Selector getSelector() { return mySelector; }

    /**
     * interface allowing application to perform a task on all channels
     * in a thread-safe fashion.  This is used in particular for 
     * activity checks.
     */
    public interface Task {
        /**
         * perform task on readable object passed during registration
         * @param o readable object passed during registration
         * @return true if the task was performed, false if this was not the case,
         * and therefore the corresponding key should be cancelled.
         */ 
        public boolean process(Object o);
        /**
         * invoked when the task starts
         */ 
        public void starting();
        /**
         * invoked when the task completes
         */ 
        public void completed();
    }

    /**
     * add a task to perform after select returns.  This method actually
     * causes select to return
     * @param task task to perform
     */
    public void addTask(SelectWorker.Task task)
    {
        synchronized(_tasks) {
            _tasks.add(task);
        }
        mySelector.wakeup();
    }

}

