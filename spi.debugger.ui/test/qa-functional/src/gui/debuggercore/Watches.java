/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * The Original Code is NetBeans. 
 * The Initial Developer of the Original Code is Sun Microsystems, Inc. 
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
 * All Rights Reserved.
 *
 * Contributor(s): Sun Microsystems, Inc.
 */

package gui.debuggercore;

import junit.textui.TestRunner;
import org.openide.nodes.Node;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;

public class Watches extends JellyTestCase {
    
    public Watches(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Watches("testBasicWatchFunctionality"));
        suite.addTest(new Watches("testExtendedWatchFilters"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** setUp method  */
    public void setUp() {
        Utilities.sleep(1000);
        System.out.println("########  " + getName() + "  #######");
    }
    
    /** tearDown method */
    public void tearDown() {
        Utilities.deleteAllBreakpoints();
        Utilities.deleteAllWatches();
        Utilities.closeZombieSessions();
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        org.netbeans.jellytools.nodes.Node projectNode = new org.netbeans.jellytools.nodes.Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);
        
        projectNode.performPopupActionNoBlock(Utilities.projectPropertiesAction);
        Utilities.sleep(2000);
        NbDialogOperator dialog = new NbDialogOperator(Utilities.projectPropertiesTitle + Utilities.testProjectName);
        org.netbeans.jellytools.nodes.Node helper = new org.netbeans.jellytools.nodes.Node(new JTreeOperator(dialog), "Run|" + Utilities.runningProjectTreeItem);
        helper.select();
        new JTextFieldOperator(dialog, 0).setText("examples.advanced.MemoryView");
        dialog.ok();
    }
    
    /**
     *
     */
    public void testBasicWatchFunctionality() {
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        org.netbeans.jellytools.nodes.Node projectNode = new org.netbeans.jellytools.nodes.Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);
        
        projectNode.performPopupActionNoBlock(Utilities.projectPropertiesAction);
        Utilities.sleep(2000);
        NbDialogOperator dialog = new NbDialogOperator(Utilities.projectPropertiesTitle + Utilities.testProjectName);
        org.netbeans.jellytools.nodes.Node helper = new org.netbeans.jellytools.nodes.Node(new JTreeOperator(dialog), "Run|Running Project");
        helper.select();
        new JTextFieldOperator(dialog, 0).setText("examples.advanced.MemoryView");
        dialog.ok();

        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        Utilities.sleep(2000);
        EditorOperator editorOperator = new EditorOperator("MemoryView.java");
        editorOperator.setCaretPosition(103, 1);
        Utilities.sleep(500);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.toggleBreakpointItem).toString(), null).perform();
        new Action(null, null, Utilities.toggleBreakpointShortcut).performShortcut();
        
        // create new watches
        CreateWatch("free");
        CreateWatch("taken");
        CreateWatch("total");
        CreateWatch("this");

        // start debugger and wait till breakpoint is hit
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runInDebuggerItem).toString(), null).perform();
        new Action(null, null, Utilities.debugProjectShortcut).performShortcut();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        Utilities.sleep(2000);
        mwo.waitStatusText("Thread main stopped at MemoryView.java:103.");
        
        // check watches and values
        Utilities.showWatchesView();
        TopComponentOperator watchesOper = new TopComponentOperator(Utilities.watchesViewTitle);
        TreeTableOperator jTableOperator = new TreeTableOperator(watchesOper);
        Node.Property property;
        int count = 0;
        
        try {
            if (!("free".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Watch for expression \'free\' was not created", false);
            property = (Node.Property)jTableOperator.getValueAt(count,1);
            if (!("long".equals(property.getValue())))
                assertTrue("Watch type for expression \'free\' is " + property.getValue() + ", should be long", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,2);
            long free = Long.parseLong(property.getValue().toString());
            
            if (!("taken".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Watch for expression \'taken\' was not created", false);
            property = (Node.Property)jTableOperator.getValueAt(count,1);
            if (!("int".equals(property.getValue())))
                assertTrue("Watch type for expression \'taken\' is " + property.getValue() + ", should be long", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,2);
            long taken = Long.parseLong(property.getValue().toString());
            
            if (!("total".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Watch for expression \'total\' was not created", false);
            property = (Node.Property)jTableOperator.getValueAt(count,1);
            if (!("long".equals(property.getValue())))
                assertTrue("Watch type for expression \'total\' is " + property.getValue() + ", should be long", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,2);
            long total = Long.parseLong(property.getValue().toString());
            
            assertTrue("Watches values does not seem to be correct (total != free + taken)", total == free + taken);
            
            if (!("this".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Watch for expression \'this\' was not created", false);
            property = (Node.Property)jTableOperator.getValueAt(count,1);
            if (!("MemoryView".equals(property.getValue())))
                assertTrue("Watch type for expression \'this\' is " + property.getValue() + ", should be MemoryView", false);
            if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "this").isLeaf())
                assertTrue("Watch this has no child nodes", false);
        }
        catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        }
        catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
       
        // finish bedugging session
        new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            mwo.waitStatusText(Utilities.finishedStatusBarText);
        } catch (TimeoutExpiredException tee) {
            System.out.println("Debugging session was not killed.");
            throw(tee);
        }
    }

    public void testExtendedWatchFilters() {
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        org.netbeans.jellytools.nodes.Node projectNode = new org.netbeans.jellytools.nodes.Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);
        
        projectNode.performPopupActionNoBlock(Utilities.projectPropertiesAction);
        Utilities.sleep(2000);
        NbDialogOperator dialog = new NbDialogOperator(Utilities.projectPropertiesTitle + Utilities.testProjectName);
        org.netbeans.jellytools.nodes.Node helper = new org.netbeans.jellytools.nodes.Node(new JTreeOperator(dialog), "Run|Running Project");
        helper.select();
        new JTextFieldOperator(dialog, 0).setText("examples.advanced.Variables");
        dialog.ok();
        
        // create watches
        Utilities.sleep(1000);
        CreateWatch("1==1");
        CreateWatch("1==0");
        CreateWatch("Integer.toString(10)");
        CreateWatch("Vpublic");
        CreateWatch("Vprotected");
        CreateWatch("Vprivate");
        CreateWatch("VpackagePrivate");
        CreateWatch("Spublic");
        CreateWatch("Sprotected");
        CreateWatch("Sprivate");
        CreateWatch("SpackagePrivate");
        CreateWatch("inheritedVpublic");
        CreateWatch("inheritedVprotected");
        CreateWatch("inheritedVprivate");
        CreateWatch("inheritedVpackagePrivate");
        CreateWatch("inheritedSpublic");
        CreateWatch("inheritedSprotected");
        CreateWatch("inheritedSprivate");
        CreateWatch("inheritedSpackagePrivate");
        CreateWatch("clazz");
        CreateWatch("n");
        CreateWatch("llist");
        CreateWatch("llist.toString()");
        CreateWatch("llist.getFirst()");
        CreateWatch("llist.getLast()");
        CreateWatch("llist.get(1)");
        CreateWatch("alist");
        CreateWatch("alist.toString()");
        CreateWatch("alist.get(2)");
        CreateWatch("vec");
        CreateWatch("vec.toString()");
        CreateWatch("vec.get(3)");
        CreateWatch("hmap");
        CreateWatch("hmap.containsKey(\"4\")");
        CreateWatch("hmap.get(\"5\")");
        CreateWatch("hmap.put(\"6\",\"test\")");
        CreateWatch("htab");
        CreateWatch("htab.containsKey(\"7\")");
        CreateWatch("htab.get(\"9\")");
        CreateWatch("htab.put(\"10\", \"test\")");
        CreateWatch("tmap");
        CreateWatch("tmap.containsKey(\"11\")");
        CreateWatch("tmap.get(\"12\")");
        CreateWatch("tmap.put(\"13\",\"test\")");
        CreateWatch("tset");
        CreateWatch("tset.contains(\"14. item\")");
        CreateWatch("tset.iterator()");
        CreateWatch("policko");
        CreateWatch("policko.length");
        CreateWatch("policko[1]");
        CreateWatch("policko[10]");
        CreateWatch("pole");
        CreateWatch("pole.length");
        CreateWatch("pole[1]");
        CreateWatch("d2");
        CreateWatch("d2.length");
        CreateWatch("d2[1]");
        CreateWatch("d2[1].length");
        CreateWatch("d2[1][1]");
        CreateWatch("d2[15].length");
        
        // start debugging
        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|Variables.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        Utilities.sleep(2000);
        EditorOperator editorOperator = new EditorOperator("Variables.java");
        editorOperator.setCaretPosition(53, 1);
        Utilities.sleep(2000);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runToCursorItem).toString(), null).perform();
        new Action(null, null, Utilities.runToCursorShortcut).performShortcut();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Thread main stopped at Variables.java:53.");

        // verify watches
        Utilities.showWatchesView();
        TopComponentOperator watchesOper = new TopComponentOperator(Utilities.watchesViewTitle);
        TreeTableOperator jTableOperator = new TreeTableOperator(watchesOper);
        
        int count = 0;
        CheckTTVLine(jTableOperator, count++, "1==1", "boolean", "true");
        CheckTTVLine(jTableOperator, count++, "1==0", "boolean", "false");
        CheckTTVLine(jTableOperator, count++, "Integer.toString(10)", "String", "\"10\"");
        CheckTTVLine(jTableOperator, count++, "Vpublic", "String", "\"Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "Vprotected", "String", "\"Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "Vprivate", "String", "\"Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "VpackagePrivate", "String", "\"Package-private Variable\"");
        CheckTTVLine(jTableOperator, count++, "Spublic", "String", "\"Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "Sprotected", "String", "\"Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "Sprivate", "String", "\"Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "SpackagePrivate", "String", "\"Package-private Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedVpublic", "String", "\"Inherited Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedVprotected", "String", "\"Inherited Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedVprivate", "String", "\"Inherited Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedVpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedSpublic", "String", "\"Inherited Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedSprotected", "String", "\"Inherited Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedSprivate", "String", "\"Inherited Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedSpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        CheckTTVLine(jTableOperator, count++, "clazz", "Class", "class java.lang.Runtime");
        if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "clazz").isLeaf())
            assertTrue("Node \'clazz\' has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "n", "int", "50");
        CheckTTVLine(jTableOperator, count++, "llist", "LinkedList", null);
        if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "llist").isLeaf())
            assertTrue("Node \'llist\' has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "llist.toString()", "String", "\"[0. item, 1. item, 2. item, 3. item, 4. item, 5. item, 6. item, 7. item, 8. item, 9. item, 10. item, 11. item, 12. item, 13. item, 14. item, 15. item, 16. item, 17. item, 18. item, 19. item, 20. item, 21. item, 22. item, 23. item, 24. item, 25. item, 26. item, 27. item, 28. item, 29. item, 30. item, 31. item, 32. item, 33. item, 34. item, 35. item, 36. item, 37. item, 38. item, 39. item, 40. item, 41. item, 42. item, 43. item, 44. item, 45. item, 46. item, 47. item, 48. item, 49. item]\"");
        CheckTTVLine(jTableOperator, count++, "llist.getFirst()", "String", "\"0. item\"");
        CheckTTVLine(jTableOperator, count++, "llist.getLast()", "String", "\"49. item\"");
        CheckTTVLine(jTableOperator, count++, "llist.get(1)", "String", "\"1. item\"");
        CheckTTVLine(jTableOperator, count++, "alist", "ArrayList", null);
        if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "alist").isLeaf())
            assertTrue("Node \'alist\' has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "alist.toString()", "String", "\"[0. item, 1. item, 2. item, 3. item, 4. item, 5. item, 6. item, 7. item, 8. item, 9. item, 10. item, 11. item, 12. item, 13. item, 14. item, 15. item, 16. item, 17. item, 18. item, 19. item, 20. item, 21. item, 22. item, 23. item, 24. item, 25. item, 26. item, 27. item, 28. item, 29. item, 30. item, 31. item, 32. item, 33. item, 34. item, 35. item, 36. item, 37. item, 38. item, 39. item, 40. item, 41. item, 42. item, 43. item, 44. item, 45. item, 46. item, 47. item, 48. item, 49. item]\"");
        CheckTTVLine(jTableOperator, count++, "alist.get(2)", "String", "\"2. item\"");
        CheckTTVLine(jTableOperator, count++, "vec", "Vector", null);
        if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "vec").isLeaf())
            assertTrue("Node \'vec\' has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "vec.toString()", "String", "\"[0. item, 1. item, 2. item, 3. item, 4. item, 5. item, 6. item, 7. item, 8. item, 9. item, 10. item, 11. item, 12. item, 13. item, 14. item, 15. item, 16. item, 17. item, 18. item, 19. item, 20. item, 21. item, 22. item, 23. item, 24. item, 25. item, 26. item, 27. item, 28. item, 29. item, 30. item, 31. item, 32. item, 33. item, 34. item, 35. item, 36. item, 37. item, 38. item, 39. item, 40. item, 41. item, 42. item, 43. item, 44. item, 45. item, 46. item, 47. item, 48. item, 49. item]\"");
        CheckTTVLine(jTableOperator, count++, "vec.get(3)", "String", "\"3. item\"");
        CheckTTVLine(jTableOperator, count++, "hmap", "HashMap", null);
        if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "hmap").isLeaf())
            assertTrue("Node \'hmap\' has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "hmap.containsKey(\"4\")", "boolean", "true");
        CheckTTVLine(jTableOperator, count++, "hmap.get(\"5\")", "String", "\"5. item\"");
        CheckTTVLine(jTableOperator, count++, "hmap.put(\"6\",\"test\")", "String", "\"6. item\"");
        CheckTTVLine(jTableOperator, count++, "htab", "Hashtable", null);
        if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "htab").isLeaf())
            assertTrue("Node \'htab\' has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "htab.containsKey(\"7\")", "boolean", "true");
        CheckTTVLine(jTableOperator, count++, "htab.get(\"9\")", "String", "\"9. item\"");
        CheckTTVLine(jTableOperator, count++, "htab.put(\"10\", \"test\")", "String", "\"10. item\"");
        CheckTTVLine(jTableOperator, count++, "tmap", "TreeMap", null);
        if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "tmap").isLeaf())
            assertTrue("Node \'tmap\' has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "tmap.containsKey(\"11\")", "boolean", "true");
        CheckTTVLine(jTableOperator, count++, "tmap.get(\"12\")", "String", "\"12. item\"");
        CheckTTVLine(jTableOperator, count++, "tmap.put(\"13\",\"test\")", "String", "\"13. item\"");
        CheckTTVLine(jTableOperator, count++, "tset", "TreeSet", null);
        if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "tset").isLeaf())
            assertTrue("Node \'tset\' has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "tset.contains(\"14. item\")", "boolean", "true");
        CheckTTVLine(jTableOperator, count++, "tset.iterator()", "TreeMap$KeyIterator", null);
        CheckTTVLine(jTableOperator, count++, "policko", "int[]", null);
        if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "policko").isLeaf())
            assertTrue("Node \'policko\' has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "policko.length", "int", "5");
        CheckTTVLine(jTableOperator, count++, "policko[1]", "int", "2");
        CheckTTVLine(jTableOperator, count++, "policko[10]", null, ">Array index \"10\" is out of range <0,4><");
        CheckTTVLine(jTableOperator, count++, "pole", "int[]", null);
        if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "pole").isLeaf())
            assertTrue("Node \'pole\' has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "pole.length", "int", "50");
        CheckTTVLine(jTableOperator, count++, "pole[1]", "int", "0");
        CheckTTVLine(jTableOperator, count++, "d2", "int[][]", null);
        if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "d2").isLeaf())
            assertTrue("Node \'d2\' has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "d2.length", "int", "10");
        CheckTTVLine(jTableOperator, count++, "d2[1]", "int[]", null);
        if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "d2[1]").isLeaf())
            assertTrue("Node \'d2[1]\' has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "d2[1].length", "int", "20");
        CheckTTVLine(jTableOperator, count++, "d2[1][1]", "int", "0");
        CheckTTVLine(jTableOperator, count++, "d2[15].length", null, ">Array index \"15\" is out of range <0,9><");
        
        // finish debugging session
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        mwo.waitStatusText(Utilities.finishedStatusBarText);
    }
    
    public void CreateWatch(String exp) {
        //new ActionNoBlock(Utilities.runMenu + "|" + Utilities.newWatchItem, null).perform();
        new ActionNoBlock(null, null, Utilities.newWatchShortcut).performShortcut();
        Utilities.sleep(500);
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newWatchTitle);
        new JTextFieldOperator(dialog, 0).typeText(exp);
        dialog.ok();
    }
    
    public void CheckTTVLine(TreeTableOperator table, int lineNumber, String name, String type, String value) {
        try {
            Node.Property property;
            if (!(name.equals(table.getValueAt(lineNumber,0).toString())))
                assertTrue("Node " + name + " not displayed in Watches view", false);
            property = (Node.Property)table.getValueAt(lineNumber,1);
            if ((type!= null)&&(!(type.equals(property.getValue()))))
                assertTrue("Node " + name + " has wrong type in Watches view", false);
            property = (Node.Property)table.getValueAt(lineNumber,2);
            if ((value!= null)&&(!(value.equals(property.getValue()))))
                assertTrue("Node " + name + " has wrong value in Watches view", false);
        }
        catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        }
        catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
    }
    
}