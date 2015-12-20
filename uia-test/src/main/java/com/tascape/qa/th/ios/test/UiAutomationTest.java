/*
 * Copyright 2015 tascape.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tascape.qa.th.ios.test;

import com.tascape.qa.th.exception.EntityDriverException;
import com.tascape.qa.th.ios.driver.UiAutomationDevice;
import com.tascape.qa.th.ios.model.UIAException;
import com.tascape.qa.th.ios.tools.SmartScroller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This interface provides default methods to be called in test cases.
 *
 * @author linsong wang
 */
public interface UiAutomationTest {

    /**
     * The method starts a GUI to let a tester inspect element tree and take screenshot when the tester is interacting
     * with the app-under-test manually. It is also possible to run UI Automation instruments JavaScript via this UI.
     * Please make sure to set test case timeout long enough for manual test cases.
     *
     * @param device the UiAutomationDevice instance used in test case
     *
     * @throws InterruptedException if case if being interrupted
     * @throws UIAException         in case of device communication issue
     */
    default void testManually(UiAutomationDevice device) throws Exception {
        AtomicBoolean visible = new AtomicBoolean(true);
        AtomicBoolean pass = new AtomicBoolean(true);

        final Logger LOG = LoggerFactory.getLogger(UiAutomationTest.class);
        String tName = Thread.currentThread().getName() + "m";
        LOG.info("Start UI to test manually");
        JFrame jf = new JFrame("Manual Device Interaction");
        jf.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        JPanel jpContent = new JPanel(new BorderLayout());
        jf.setContentPane(jpContent);
        jpContent.setPreferredSize(new Dimension(688, 418));
        jpContent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel jpInfo = new JPanel();
        jpContent.add(jpInfo, BorderLayout.PAGE_START);
        jpInfo.setLayout(new BoxLayout(jpInfo, BoxLayout.LINE_AXIS));
        {
            JButton jb = new JButton("PASS");
            jb.setForeground(Color.green);
            jb.setFont(jb.getFont().deriveFont(Font.BOLD));
            jpInfo.add(jb);
            jb.addActionListener(event -> {
                visible.set(false);
            });
        }
        {
            JButton jb = new JButton("FAIL");
            jb.setForeground(Color.red);
            jb.setFont(jb.getFont().deriveFont(Font.BOLD));
            jpInfo.add(jb);
            jb.addActionListener(event -> {
                pass.set(false);
                visible.set(false);
            });
        }
        jpInfo.add(Box.createHorizontalGlue());
        String info = device.model() + " " + device.name() + " " + device.systemName() + " " + device.systemVersion()
            + "\n" + device.getUuid();
        jpInfo.add(new JLabel(info));

        JPanel jpAction = new JPanel();
        jpContent.add(jpAction, BorderLayout.PAGE_END);
        jpAction.setLayout(new BoxLayout(jpAction, BoxLayout.LINE_AXIS));

        JTextArea jtaResponse = new JTextArea();
        jtaResponse.setEditable(false);
        jtaResponse.setTabSize(4);
        JScrollPane jsp = new JScrollPane(jtaResponse);
        new SmartScroller(jsp);
        JTextArea jtaJs = new JTextArea();
        JSplitPane jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jsp, jtaJs);
        jSplitPane.setResizeWeight(1);
        jpContent.add(jSplitPane, BorderLayout.CENTER);

        {
            JButton jbElementTree = new JButton("Log Element Tree");
            jpAction.add(jbElementTree);
            jbElementTree.addActionListener(event -> {
                Thread t = new Thread(tName) {
                    public void run() {
                        try {
                            device.mainWindow().logElement().forEach(line -> {
                                LOG.debug(line);
                                jtaResponse.append(line);
                                jtaResponse.append("\n");

                            });
                        } catch (UIAException ex) {
                            LOG.error("Cannot load element tree", ex);
                            jtaResponse.append("Cannot load element tree");
                            jtaResponse.append("\n");
                        }
                    }
                };
                t.start();
                try {
                    t.join();
                } catch (InterruptedException ex) {
                    LOG.error("Cannot load element tree", ex);
                }
            });
        }
        {
            JButton jbScreenShot = new JButton("Take Screenshot");
            jpAction.add(jbScreenShot);
            jbScreenShot.addActionListener(event -> {
                Thread t = new Thread(tName) {
                    public void run() {
                        try {
                            File png = device.takeDeviceScreenshot();
                            Desktop.getDesktop().open(png);
                        } catch (EntityDriverException | IOException ex) {
                            LOG.error("Cannot take screenshot", ex);
                            jtaResponse.append("Cannot take screenshot");
                            jtaResponse.append("\n");
                        }
                    }
                };
                t.start();
                try {
                    t.join();
                } catch (InterruptedException ex) {
                    LOG.error("Cannot take screenshot", ex);
                }
            });
        }
        {
            JButton jbJavaScript = new JButton("Run JavaScript");
            jpAction.add(Box.createHorizontalGlue());
            jpAction.add(jbJavaScript);
            jbJavaScript.addActionListener(event -> {
                String js = jtaJs.getSelectedText();
                if (js == null) {
                    js = jtaJs.getText();
                }
                if (StringUtils.isEmpty(js)) {
                    return;
                }
                String javaScript = js;
                Thread t = new Thread(tName) {
                    public void run() {
                        try {
                            device.runJavaScript(javaScript).forEach(l -> {
                                jtaResponse.append(l);
                                jtaResponse.append("\n");
                            });
                        } catch (UIAException ex) {
                            LOG.error("Cannot run javascript", ex);
                            jtaResponse.append("Cannot run javascript");
                            jtaResponse.append("\n");
                        }
                    }
                };
                t.start();
                try {
                    t.join();
                } catch (InterruptedException ex) {
                    LOG.error("Cannot run javascript", ex);
                }
            });
        }

        jf.pack();
        jf.setVisible(true);
        jf.setAlwaysOnTop(true);
        jf.setLocationRelativeTo(null);

        while (visible.get()) {
            Thread.sleep(500);
        }
        jf.dispose();

        if (pass.get()) {
            LOG.info("Manual Interaction returns PASS");
        } else {
            device.takeDeviceScreenshot();
            device.mainWindow().logElement().forEach(line -> {
                LOG.debug(line);
            });
            Assert.fail("Manual Interaction returns FAIL");
        }
    }
}
