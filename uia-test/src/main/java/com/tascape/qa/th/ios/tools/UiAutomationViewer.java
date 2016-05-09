/*
 * Copyright 2015 - 2016 Nebula Bay.
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
package com.tascape.qa.th.ios.tools;

import com.alee.laf.WebLookAndFeel;
import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.ios.driver.UiAutomationDevice;
import com.tascape.qa.th.ios.test.UiAutomationTest;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.libimobiledevice.ios.driver.binding.exceptions.SDKException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class UiAutomationViewer implements UiAutomationTest {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(UiAutomationViewer.class);

    private UiAutomationDevice device;

    private String appName = "Movies";

    private int debugMinutes = 30;

    private JDialog jd;

    private final JButton jbLaunch = new JButton("Launch");

    private final JComboBox<String> jcbDevices = new JComboBox<>(new String[]{"detecting devices..."});

    private final JSpinner jsDebugMinutes = new JSpinner(new SpinnerNumberModel(30, 15, 180, 15));

    private final JTextField jtfApp = new JTextField();

    private void initDevice() throws Exception {
        SwingUtilities.invokeLater(() -> {
            WebLookAndFeel.install();
            jd = new JDialog((JFrame) null, "Launch iOS App");
            jd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JPanel jpContent = new JPanel(new BorderLayout());
            jd.setContentPane(jpContent);
            jpContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel jpParameters = new JPanel();
            jpParameters.setLayout(new BoxLayout(jpParameters, BoxLayout.PAGE_AXIS));
            jpContent.add(jpParameters, BorderLayout.CENTER);
            {
                JPanel jp = new JPanel();
                jpParameters.add(jp);
                jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
                jp.add(new JLabel("Devices"));
                jp.add(jcbDevices);
            }
            {
                JPanel jp = new JPanel();
                jpParameters.add(jp);
                jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
                jp.add(new JLabel("App Name"));
                jp.add(jtfApp);
                if (StringUtils.isNotEmpty(appName)) {
                    jtfApp.setText(appName);
                }

                jtfApp.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            jbLaunch.doClick();
                        }
                    }
                });
            }
            {
                JPanel jp = new JPanel();
                jpParameters.add(jp);
                jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
                jp.add(new JLabel("Interaction time (minute)"));
                jsDebugMinutes.getEditor().setEnabled(false);
                jp.add(jsDebugMinutes);
            }
            {
                JPanel jp = new JPanel();
                jpParameters.add(jp);
                jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
                jp.add(Box.createRigidArea(new Dimension(518, 2)));
            }

            JPanel jpInfo = new JPanel();
            jpContent.add(jpInfo, BorderLayout.PAGE_END);
            jpInfo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            jpInfo.setLayout(new BoxLayout(jpInfo, BoxLayout.LINE_AXIS));
            jbLaunch.setFont(jbLaunch.getFont().deriveFont(Font.BOLD));
            jbLaunch.setBorder(BorderFactory.createEtchedBorder());
            jbLaunch.setEnabled(false);
            jpInfo.add(jbLaunch);
            jbLaunch.addActionListener(event -> {
                new Thread() {
                    @Override
                    public void run() {
                        launch();
                    }
                }.start();
            });

            jd.pack();
            jd.setResizable(false);
            jd.setAlwaysOnTop(true);
            jd.setLocationRelativeTo(null);
            jd.setVisible(true);

            new Thread() {
                @Override
                public void run() {
                    try {
                        detectDevices();
                    } catch (SDKException | InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }.start();
        });
    }

    private void detectDevices() throws SDKException, InterruptedException {
        List<String> uuids = UiAutomationDevice.getAllUuids();
        ComboBoxModel<String> model = new DefaultComboBoxModel<>(uuids.toArray(new String[0]));
        jcbDevices.setModel(model);
        if (model.getSize() == 0) {
            JOptionPane.showMessageDialog(jcbDevices.getTopLevelAncestor(), "No attached iOS device found.");
            this.jbLaunch.setEnabled(false);
        } else {
            this.jbLaunch.setEnabled(true);
        }
    }

    private void launch() {
        try {
            jd.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            device = new UiAutomationDevice((String) this.jcbDevices.getSelectedItem());
            device.start(this.jtfApp.getText(), 5000);
        } catch (Throwable ex) {
            LOG.error("Error", ex);
            jd.setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(jbLaunch.getTopLevelAncestor(), "Cannot start app");
            return;
        }

        debugMinutes = (int) jsDebugMinutes.getValue();
        jd.dispose();
        try {
            this.testManually(device, debugMinutes);
        } catch (Throwable ex) {
            LOG.error("Error", ex);
            System.exit(1);
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        String instruction = "Usage:\n"
            + "java -cp $YOUR_CLASSPATH com.tascape.qa.th.ios.tools.UiAutomationDebugger APP_NAME";
        LOG.info("--------\n{}", instruction);

        SystemConfiguration.getInstance();
        UiAutomationViewer debugger = new UiAutomationViewer();

        if (args.length > 0) {
            debugger.appName = args[0];
        }

        try {
            debugger.initDevice();
        } catch (Throwable ex) {
            LOG.error("", ex);
            System.exit(1);
        }
    }
}
