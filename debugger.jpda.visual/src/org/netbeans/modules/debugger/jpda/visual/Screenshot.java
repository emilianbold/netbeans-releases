/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
//import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author martin
 */
public class Screenshot {
    
    private static final Logger logger = Logger.getLogger(Screenshot.class.getName());
    
    private static final String AWTThreadName = "AWT-EventQueue-";
    
    public static void start() {
        if (SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    start();
                }
            }, 1000);
            return ;
        }
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine != null) {
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            logger.severe("Debugger = "+debugger);
            if (debugger != null) {
                List<JPDAThread> allThreads = debugger.getThreadsCollector().getAllThreads();
                logger.severe("Threads = "+allThreads);
                for (JPDAThread t : allThreads) {
                    if (t.getName().startsWith(AWTThreadName)) {
                        try {
                            grab(t);
                        } catch (InvocationException iex) {
                            //ObjectReference exception = iex.exception();
                            Exceptions.printStackTrace(iex);
                            /*
                            final InvocationExceptionTranslated iextr = new InvocationExceptionTranslated(iex, (JPDADebuggerImpl) debugger);
                            RequestProcessor.getDefault().post(new Runnable() {
                                @Override
                                public void run() {
                                    iextr.getMessage();
                                    iextr.getLocalizedMessage();
                                    iextr.getCause();
                                    iextr.getStackTrace();
                                    Exceptions.printStackTrace(iextr);
                                }
                            }, 100);
                            */
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        break;
                    }
                }
            }
        }
    }

    private static void grab(JPDAThread t) throws ClassNotLoadedException, IncompatibleThreadStateException, InvalidTypeException, InvocationException {
        Lock threadLock = ((JPDAThreadImpl) t).accessLock.writeLock();
        try {
            threadLock.lock();
            logger.severe("grab("+t+"), is suspended = "+t.isSuspended());
            if (t.isSuspended()) {
                /*
                 * Run following code in the target VM:
                   Window[] windows = Window.getWindows();
                   for (Window w : windows) {
                       if (!w.isVisible()) {
                           continue;
                       }
                       Dimension d = w.getSize();
                       BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
                       Graphics g = bi.createGraphics();
                       w.paint(g);
                       Raster raster = bi.getData();
                       Object data = raster.getDataElements(0, 0, d.width, d.height, null);
                   }
                 */
                
                ThreadReference tawt = ((JPDAThreadImpl) t).getThreadReference();
                VirtualMachine vm = tawt.virtualMachine();
                ClassType windowClass = getClass(vm, "java.awt.Window");
                if (windowClass == null) {
                    logger.severe("No Window");
                    return ;
                }
                /*
                List<Method> methodsByName = windowClass.methodsByName("getWindows");
                Method getWindows = null;
                for (Method m : methodsByName) {
                    if (m.isStatic() && m.argumentTypes().isEmpty()) {
                        getWindows = m;
                        break;
                    }
                }
                 */
                Method getWindows = windowClass.concreteMethodByName("getWindows", "()[Ljava/awt/Window;");
                if (getWindows == null) {
                    logger.severe("No getWindows() method!");
                    return ;
                }
                ArrayReference windowsArray = (ArrayReference) ((ClassType) windowClass).invokeMethod(tawt, getWindows, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                List<Value> windows = windowsArray.getValues();
                logger.severe("Have "+windows.size()+" window(s).");
                
                for (Value windowValue : windows) {
                    ObjectReference window = (ObjectReference) windowValue;
                    dumpHierarchy(window);
                    /*
                    methodsByName = windowClass.methodsByName("getSize");
                    Method getSize = null;
                    for (Method m : methodsByName) {
                        if (!m.isStatic() && m.argumentTypes().isEmpty()) {
                            getSize = m;
                            break;
                        }
                    }
                     */
                    Method isVisible = windowClass.concreteMethodByName("isVisible", "()Z");
                    if (isVisible == null) {
                        logger.severe("No isVisible() method!");
                        return ;
                    }
                    BooleanValue visible = (BooleanValue) window.invokeMethod(tawt, isVisible, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                    if (!visible.value()) {
                        // Ignore windows that are not visible.
                        continue;
                    }
                    
                    Method getSize = windowClass.concreteMethodByName("getSize", "()Ljava/awt/Dimension;");
                    if (getSize == null) {
                        logger.severe("No getSize() method!");
                        return ;
                    }
                    ObjectReference sizeDimension = (ObjectReference) window.invokeMethod(tawt, getSize, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                    ClassType dimensionClass = getClass(vm, "java.awt.Dimension");
                    if (dimensionClass == null) {
                        logger.severe("No Dimension");
                        return ;
                    }
                    Field field = dimensionClass.fieldByName("width");
                    IntegerValue widthValue = (IntegerValue) sizeDimension.getValue(field);
                    int width = widthValue.value();
                    field = dimensionClass.fieldByName("height");
                    IntegerValue heightValue = (IntegerValue) sizeDimension.getValue(field);
                    int height = heightValue.value();
                    logger.severe("The size is "+width+" x "+height+"");
                    
                    ClassType bufferedImageClass = getClass(vm, "java.awt.image.BufferedImage");
                    Method constructor = bufferedImageClass.concreteMethodByName("<init>", "(III)V");
                    List<? extends Value> args = Arrays.asList(widthValue, heightValue, vm.mirrorOf(BufferedImage.TYPE_INT_ARGB));
                    ObjectReference bufferedImage = bufferedImageClass.newInstance(tawt, constructor, args, ObjectReference.INVOKE_SINGLE_THREADED);
                    Method createGraphics = bufferedImageClass.concreteMethodByName("createGraphics", "()Ljava/awt/Graphics2D;");
                    if (createGraphics == null) {
                        logger.severe("createGraphics() method is not found!");
                        return ;
                    }
                    ObjectReference graphics = (ObjectReference) bufferedImage.invokeMethod(tawt, createGraphics, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                    
                    
                    Method paint = windowClass.concreteMethodByName("paint", "(Ljava/awt/Graphics;)V");
                    window.invokeMethod(tawt, paint, Arrays.asList(graphics), ObjectReference.INVOKE_SINGLE_THREADED);
                    
                    /*
                    // getPeer() - java.awt.peer.ComponentPeer, ComponentPeer.paint()
                    Method getPeer = windowClass.concreteMethodByName("getPeer", "()Ljava/awt/peer/ComponentPeer;");
                    ObjectReference peer = (ObjectReference) window.invokeMethod(tawt, getPeer, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                    Method paint = ((ClassType) peer.referenceType()).concreteMethodByName("paint", "(Ljava/awt/Graphics;)V");
                    peer.invokeMethod(tawt, paint, Arrays.asList(graphics), ObjectReference.INVOKE_SINGLE_THREADED);
                    - paints nothing! */
                    
                    Method getData = bufferedImageClass.concreteMethodByName("getData", "()Ljava/awt/image/Raster;");
                    ObjectReference raster = (ObjectReference) bufferedImage.invokeMethod(tawt, getData, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                    
                    Method getDataElements = ((ClassType) raster.referenceType()).concreteMethodByName("getDataElements", "(IIIILjava/lang/Object;)Ljava/lang/Object;");
                    IntegerValue zero = vm.mirrorOf(0);
                    ArrayReference data = (ArrayReference) raster.invokeMethod(tawt, getDataElements, Arrays.asList(zero, zero, widthValue, heightValue, null), ObjectReference.INVOKE_SINGLE_THREADED);
                    
                    logger.severe("Image data length = "+data.length());
                    
                    List<Value> dataValues = data.getValues();
                    int[] dataArray = new int[data.length()];
                    int i = 0;
                    for (Value v : dataValues) {
                        dataArray[i++] = ((IntegerValue) v).value();
                    }
                    
                    displayScreenshot(width, height, dataArray);
                    addListenerTest(tawt, window);
                }
            }
        } finally {
            threadLock.unlock();
        }
    }
    
    private static ClassType getClass(VirtualMachine vm, String name) {
        List<ReferenceType> classList = vm.classesByName(name);
        ReferenceType clazz = null;
        for (ReferenceType c : classList) {
            if (c.classLoader() == null) {
                clazz = c;
                break;
            }
        }
        return (ClassType) clazz;
    }
    
    private static ArrayType getArrayClass(VirtualMachine vm, String name) {
        List<ReferenceType> classList = vm.classesByName(name);
        ReferenceType clazz = null;
        for (ReferenceType c : classList) {
            if (c.classLoader() == null) {
                clazz = c;
                break;
            }
        }
        return (ArrayType) clazz;
    }
    
    private static void displayScreenshot(int width, int height, int[] data) {
        final BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = bi.getRaster();
        raster.setDataElements(0, 0, width, height, data);
        //bi.setData(raster);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame f = new JFrame("SCREENSHOT VIA DEBUGGER!");
                f.setSize(new Dimension(bi.getWidth(), bi.getHeight()));
                f.setLayout(new BorderLayout());
                JLabel label = new JLabel(new ImageIcon(bi));
                //label.setBorder(new BevelBorder(0));
                f.add(label, BorderLayout.CENTER);
                f.pack();
                f.setVisible(true);
            }
        });
        
    }

    private static void dumpHierarchy(ObjectReference window) {
        VirtualMachine vm = window.virtualMachine();
        ClassType containerClass = getClass(vm, "java.awt.Container");
        
    }
    
    private static ObjectReference theCreatedListener = null;
    
    private static void addListenerTest(ThreadReference tawt, ObjectReference window) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException {
        // ClassLoader.getSystemClassLoader();
        // Class c = ClassLoader.defineClass(String name, byte[] b, int off, int len);
        // l = c.newInstance();
        // java.awt.Container.addContainerListener(ContainerListener l);
        
        // ClassLoader.getSystemClassLoader();
        VirtualMachine vm = window.virtualMachine();
        if (theCreatedListener == null) {
        ClassType classLoaderClass = getClass(vm, ClassLoader.class.getName());
        Method getSystemClassLoader = classLoaderClass.concreteMethodByName("getSystemClassLoader", "()Ljava/lang/ClassLoader;");
        ObjectReference systemClassLoader = (ObjectReference) classLoaderClass.invokeMethod(tawt, getSystemClassLoader, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
        
        printClassLoaders(vm);
        /*
        List<ReferenceType> classList = vm.classesByName(ClassLoader.class.getName());
        ReferenceType appClassLoader = null;
        for (ReferenceType c : classList) {
            logger.severe("Have class loader class "+c+", name = "+c.name()+", instances = "+c.instances(Integer.MAX_VALUE));
            List<ClassType> subclasses = ((ClassType) c).subclasses();
            logger.severe("  Sub-classes: "+subclasses);
            for (ClassType sc : subclasses) {
                logger.severe("Have sub-class loader class "+c+", name = "+c.name()+", instances = "+c.instances(Integer.MAX_VALUE));
            }
            if (c.classLoader() != null) {
                appClassLoader = c;
                //break;
            }
        }
        logger.severe("Have app class loader: "+appClassLoader);
         */
        //ReferenceType appClassLoader = getClassLoader(vm, "ExtClassLoader");
        //systemClassLoader = getClassLoader(vm, "ExtClassLoader");
        
        // Class c = ClassLoader.defineClass(String name, byte[] b, int off, int len);
        byte[] bytes = getTestContainerListenerCode();
        ArrayReference byteArray = createTargetBytes(vm, bytes);
        Method defineClass = classLoaderClass.concreteMethodByName("defineClass", "(Ljava/lang/String;[BII)Ljava/lang/Class;");
        ObjectReference theTestClass = (ObjectReference) systemClassLoader.invokeMethod(tawt, defineClass, Arrays.asList(vm.mirrorOf("javauidbg.TestContainerListener"), byteArray, vm.mirrorOf(0), vm.mirrorOf(bytes.length)), ObjectReference.INVOKE_SINGLE_THREADED);
        
        // l = c.newInstance();
        ClassType theClass = getClass(vm, Class.class.getName());
        // Perhaps it's not 100% correct, we should be calling the new class' newInstance() method, not Class.newInstance() method.
        Method newInstance = theClass.concreteMethodByName("newInstance", "()Ljava/lang/Object;");
        ObjectReference theListener = (ObjectReference) theTestClass.invokeMethod(tawt, newInstance, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
        theCreatedListener = theListener;
        }
        // java.awt.Container.addContainerListener(ContainerListener l);
        Method addContainerListener = ((ClassType) window.referenceType()).concreteMethodByName("addContainerListener", "(Ljava/awt/event/ContainerListener;)V");
        window.invokeMethod(tawt, addContainerListener, Arrays.asList(theCreatedListener), ObjectReference.INVOKE_SINGLE_THREADED);
    }
    
    private static void printClassLoaders(VirtualMachine vm) {
        List<ReferenceType> classList = vm.classesByName(ClassLoader.class.getName());
        ReferenceType appClassLoader = null;
        for (ReferenceType c : classList) {
            printClassLoaders(vm, c, "");
            if (c.classLoader() != null) {
                appClassLoader = c;
                //break;
            }
        }
        logger.severe("Have app class loader: "+appClassLoader);
    }
    
    private static void printClassLoaders(VirtualMachine vm, ReferenceType c, String ins) {
        logger.severe(ins+"Have class loader class "+c+", name = "+c.name()+", instances = "+c.instances(Integer.MAX_VALUE));
        List<ClassType> subclasses = ((ClassType) c).subclasses();
        logger.severe(ins+"  Sub-classes: "+subclasses);
        for (ClassType sc : subclasses) {
            //logger.severe(ins+"  Have sub-class loader class "+sc+", name = "+sc.name()+", instances = "+sc.instances(Integer.MAX_VALUE));
            printClassLoaders(vm, sc, ins + "  ");
        }
    }
    
    private static ObjectReference getClassLoader(VirtualMachine vm, String name) {
        List<ReferenceType> classList = vm.classesByName(ClassLoader.class.getName());
        ObjectReference cl = null;
        for (ReferenceType c : classList) {
            cl = getClassLoader(vm, name, c);
            if (cl != null) {
                break;
            }
        }
        return cl;
    }
    
    private static ObjectReference getClassLoader(VirtualMachine vm, String name, ReferenceType c) {
        if (c.name().indexOf(name) >= 0) {
            List<ObjectReference> instances = c.instances(1);
            if (!instances.isEmpty()) {
                return instances.get(0);
            }
        }
        List<ClassType> subclasses = ((ClassType) c).subclasses();
        for (ClassType sc : subclasses) {
            ObjectReference o = getClassLoader(vm, name, sc);
            if (o != null) {
                return o;
            }
        }
        return null;
    }
    
    private static byte[] getTestContainerListenerCode() {
        byte[] bytes;
        File f = new File("/home/martin/NETBEANS/SRC/UI_DBG_TEST/javauidbg/TestContainerListener.class");
        int length = (int) f.length();
        bytes = new byte[length];
        try {
            BufferedInputStream bin = new BufferedInputStream(new FileInputStream(f));
            int l = 0;
            while (l < length) {
                int n = bin.read(bytes, l, length - l);
                if (n < 0) {
                    break;
                }
                l += n;
            }
        } catch (IOException ioex) {
            Exceptions.printStackTrace(ioex);
            return null;
        }
        return bytes;
    }
    
    private static ArrayReference createTargetBytes(VirtualMachine vm, byte[] bytes) throws InvalidTypeException, ClassNotLoadedException {
        ArrayType bytesArrayClass = getArrayClass(vm, "byte[]");
        ArrayReference array = bytesArrayClass.newInstance(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            array.setValue(i, vm.mirrorOf(bytes[i]));
        }
        return array;
    }
    
}
