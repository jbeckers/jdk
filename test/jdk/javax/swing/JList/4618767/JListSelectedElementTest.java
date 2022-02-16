/*
 * Copyright (c) 2002, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


import static javax.swing.UIManager.getInstalledLookAndFeels;

/*
 * @test
 * @key headful
 * @bug 4618767
 * @summary Typing a letter while a JList has focus now makes the selection jump to the item whose text
 *          starts with that letter even though that letter is accompanied by modifier keys such as ALT or CTRL.
 * @run main JListSelectedElementTest
 */
public class JListSelectedElementTest {

    private static final int MENU = KeyEvent.VK_F;

    private static JFrame frame;
    private static JList<String> list;
    private static Robot robot;

    public static void main(String[] args) throws Exception {
        runTest();
    }

    public static void runTest() throws Exception {
        robot = new Robot();
        robot.setAutoWaitForIdle(true);
        robot.setAutoDelay(200);

        final boolean isAMac = System.getProperty("os.name").toLowerCase().contains("os x");

        List<String> lafs = Arrays.stream(getInstalledLookAndFeels())
                                  .map(UIManager.LookAndFeelInfo::getClassName)
                                  .collect(Collectors.toList());
        for (final String laf : lafs) {
            try {
                SwingUtilities.invokeAndWait(() -> frame = new JFrame());
                robot.waitForIdle();

                SwingUtilities.invokeAndWait(() -> {
                    setLookAndFeel(laf);
                    createUI();
                });

                // Select element named as bill
                robot.keyPress(KeyEvent.VK_B);
                robot.keyRelease(KeyEvent.VK_B);

                // Assertion check to verify that selected node is 'bill'
                String elementSel = list.getSelectedValue();
                if (!"bill".equals(elementSel)) {
                    throw new RuntimeException("Test failed for " + laf
                            + " as the list element selected: " + elementSel
                            + " is not the expected value 'bill'"
                    );
                }

                //Now operate Menu using Mnemonics ALT+M
                if (isAMac && laf.contains("Nimbus")) {
                    robot.keyPress(KeyEvent.VK_ALT);
                    robot.keyPress(MENU);
                    robot.keyRelease(KeyEvent.VK_ALT);
                    robot.keyRelease(MENU);
                } else {
                    robot.keyPress(KeyEvent.VK_ALT);
                    robot.keyPress(KeyEvent.VK_CONTROL);
                    robot.keyPress(MENU);
                    robot.keyRelease(KeyEvent.VK_ALT);
                    robot.keyRelease(KeyEvent.VK_CONTROL);
                    robot.keyRelease(MENU);
                }

                robot.delay(500);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);

                String elementSelAfter = list.getSelectedValue();

                // As per the fix of BugID 4618767, the list element selection should not change
                if (!elementSel.equals(elementSelAfter)) {
                    throw new RuntimeException("Test failed for " + laf
                            + " as list.getSelectedValue() before = " + elementSel
                            + " not equal to list.getSelectedValue() after pressing Enter = " + elementSelAfter
                    );
                }
                System.out.println("Test passed for laf: " + laf);

            } finally {
                SwingUtilities.invokeAndWait(JListSelectedElementTest::disposeFrame);
            }
        }
    }

    private static void createUI() {
        list = new JList(new String[]{"anaheim", "bill", "chicago", "dingo", "ernie", "freak"});

        JMenu menu = new JMenu("File");
        menu.setMnemonic(MENU);

        JMenuItem menuItem = new JMenuItem("Dummy");
        menu.add(menuItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);

        frame.setJMenuBar(menuBar);
        frame.setContentPane(list);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void setLookAndFeel(final String laf) {
        try {
            UIManager.setLookAndFeel(laf);
            System.out.println("LookAndFeel: " + laf);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void disposeFrame() {
        if (frame != null) {
            frame.dispose();
            frame = null;
        }
    }

}
