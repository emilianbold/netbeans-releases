package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.*;

import java.awt.*;

import java.awt.event.*;

import java.io.*;

import java.lang.reflect.*;

import java.util.*;

import javax.swing.*;

public class jemmy_043 extends JemmyTest {

    JFrameOperator frame;
    JTextAreaOperator jtextArea;
    JButtonOperator jbutton;

    public int runIt(Object obj) {

	try {
	    (new ClassReference("org.netbeans.jemmy.testing.Application_043")).startApplication();

            TestOut eventEvents = 
                new TestOut(null,
                            new PrintStream(new FileOutputStream(System.getProperty("user.dir") + 
                                                                 System.getProperty("file.separator") + 
                                                                 "event_events")),
                                            null);
            TestOut robotEvents = 
                new TestOut(null,
                            new PrintStream(new FileOutputStream(System.getProperty("user.dir") + 
                                                                 System.getProperty("file.separator") + 
                                                                 "robot_events")),
                                            null);

	    frame = new JFrameOperator("Application_043");
            jtextArea = new JTextAreaOperator(frame);
            jbutton = new JButtonOperator(frame);

            MyListener duringEvents = new MyListener();

            JemmyProperties.setCurrentDispatchingModel(JemmyProperties.getCurrentDispatchingModel() -
                                                       (JemmyProperties.getCurrentDispatchingModel() &
                                                        JemmyProperties.ROBOT_MODEL_MASK));

            addListener(duringEvents, eventEvents);

            scenario();

            removeListener(duringEvents);

            jtextArea.clearText();

            MyListener duringRobot = new MyListener();

            JemmyProperties.setCurrentDispatchingModel(JemmyProperties.getCurrentDispatchingModel() |
                                                       JemmyProperties.ROBOT_MODEL_MASK);

            addListener(duringRobot, robotEvents);

            scenario();

            removeListener(duringRobot);

            duringEvents.print();
            duringRobot.print();

	} catch(Exception e) {
                finalize();
	    throw(new TestCompletedException(1, e));
            }

                finalize();

	return(diff());
            }

    void scenario() {
        jbutton.clickMouse();
        jbutton.typeKey('\n');

        jtextArea.clickMouse();
        jtextArea.typeText("123\n");
            }

    int diff() {
        try {
            BufferedReader eventEvents = 
                new BufferedReader(new FileReader(System.getProperty("user.dir") + 
                                                  System.getProperty("file.separator") + 
                                                  "event_events"));
            BufferedReader robotEvents = 
                new BufferedReader(new FileReader(System.getProperty("user.dir") + 
                                                  System.getProperty("file.separator") + 
                                                  "robot_events"));
            String eventLine = "";
            String robotLine = "";
            do {
                if(!eventLine.equals(robotLine)) {
                    getOutput().printErrLine("Event sequences are different:");
                    getOutput().printErrLine(eventLine);
                    getOutput().printErrLine(robotLine);
                    return(1);
            }
                eventLine = eventEvents.readLine();
                robotLine = robotEvents.readLine();
            } while(eventLine != null && robotLine != null);
        } catch(IOException e) {
            throw(new JemmyException("IOException!", e));
            }
        return(0);
            }

    void addListener(MyListener listener, TestOut output) {
        listener.setOutput(output);
        addListeners(frame.getSource(), listener);
        addListeners(jtextArea.getSource(), listener);
        addListeners(jbutton.getSource(), listener);
            }

    void addListeners(Component comp, MyListener listener) {
        comp.addMouseListener(listener);
        comp.addKeyListener(listener);
            }

    void removeListener(MyListener listener) {
        removeListeners(frame.getSource(), listener);
        removeListeners(jtextArea.getSource(), listener);
        removeListeners(jbutton.getSource(), listener);
            }

    void removeListeners(Component comp, MyListener listener) {
        comp.removeMouseListener(listener);
        comp.removeKeyListener(listener);
            }

    class MyListener implements MouseListener, KeyListener {
        Vector events;
        Vector components;
        TestOut output;
        public MyListener() {
            events = new Vector();
            components = new Vector();
            output = JemmyProperties.getCurrentOutput();
	}
        public void setOutput(TestOut output) {
            this.output = output;
    }
        public void eventDispatched(AWTEvent e) {
            events.add(e);
            components.add(e.getSource());
    }
        public void print() {
            for(int i = 0; i < events.size(); i++) {
                AWTEvent e = (AWTEvent)events.get(i);
                String eventDescription = e.toString();
                if(e instanceof KeyEvent) {
                    if(e.getID() == KeyEvent.KEY_PRESSED) {
                        eventDescription = "Key pressed";
                    } else if(e.getID() == KeyEvent.KEY_RELEASED) {
                        eventDescription = "Key released";
                    } else if(e.getID() == KeyEvent.KEY_TYPED) {
                        eventDescription = "Key typed";
    }
                    eventDescription = eventDescription + " " + 
                        getKeyName(((KeyEvent)e).getKeyCode());
                } else if(e instanceof MouseEvent) {
                    if(e.getID() == MouseEvent.MOUSE_PRESSED) {
                        eventDescription = "Mouse pressed";
                    } else if(e.getID() == MouseEvent.MOUSE_RELEASED) {
                        eventDescription = "Mouse released";
                    } else if(e.getID() == MouseEvent.MOUSE_CLICKED) {
                        eventDescription = "Mouse clicked";
                    } else if(e.getID() == MouseEvent.MOUSE_ENTERED) {
                        eventDescription = "Mouse entered";
                    } else if(e.getID() == MouseEvent.MOUSE_EXITED) {
                        eventDescription = "Mouse exited";
                    }
                    eventDescription = eventDescription + " " +
                        ((MouseEvent)e).getX() + " " +
                        ((MouseEvent)e).getY();
                }
                output.printLine(eventDescription + " on " + 
                                 ((Component)components.get(i)).getClass().getName());
            }
        }
        public void mouseClicked(MouseEvent e) {
            eventDispatched(e);
        }
        public void mouseEntered(MouseEvent e) {
            eventDispatched(e);
        }
        public void mouseExited(MouseEvent e) {
            eventDispatched(e);
        }
        public void mousePressed(MouseEvent e) {
            eventDispatched(e);
        }
        public void mouseReleased(MouseEvent e) {
            eventDispatched(e);
        }
        public void keyPressed(KeyEvent e) {
            eventDispatched(e);
        }
        public void keyReleased(KeyEvent e) {
            eventDispatched(e);
        }
        public void keyTyped(KeyEvent e) {
            eventDispatched(e);
        }
    }

    String getKeyName(int keyCode) {
	try {
            Class eventClass = Class.forName("java.awt.event.KeyEvent");
            Field[] fields = eventClass.getFields();
            for(int i = 0; i < fields.length; i++) {
                if((fields[i].getModifiers() &
                    (Modifier.PUBLIC | Modifier.STATIC)) != 0 &&
                   fields[i].getType().equals(Integer.TYPE) &&
                   fields[i].getName().startsWith("VK_")) {
                    if(keyCode == ((Integer)fields[i].get(null)).intValue()) {
                        return(fields[i].getName());
        }
    }
}
        } catch(ClassNotFoundException e) {
            JemmyProperties.getCurrentOutput().printStackTrace(e);
        } catch(IllegalAccessException e) {
            JemmyProperties.getCurrentOutput().printStackTrace(e);
        }
        return("unknown");
    }
}
