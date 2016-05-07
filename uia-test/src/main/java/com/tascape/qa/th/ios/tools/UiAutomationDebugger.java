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
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.libimobiledevice.ios.driver.binding.exceptions.SDKException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class UiAutomationDebugger implements UiAutomationTest {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(UiAutomationDebugger.class);

    private String uuid;

    private UiAutomationDevice device;

    private String appName = "Movies";

    private int debugMinutes = 30;

    private JButton jbLaunch = new JButton("Launch");

    private final JComboBox<String> jcbDevices = new JComboBox<>(new String[]{"detecting devices..."});

    private final JTextField jtfApp = new JTextField();

    private void start() throws Exception {
        SwingUtilities.invokeLater(() -> {
            WebLookAndFeel.install();
            JDialog jd = new JDialog((JFrame) null, "Launch iOS App");
            jd.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            JPanel jpContent = new JPanel(new BorderLayout());
            jd.setContentPane(jpContent);
            jpContent.setPreferredSize(new Dimension(528, 288));
            jpContent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JPanel jpInfo = new JPanel();
            jpContent.add(jpInfo, BorderLayout.PAGE_START);
            jpInfo.setLayout(new BorderLayout());
            {
                jbLaunch.setFont(jbLaunch.getFont().deriveFont(Font.BOLD));
                jpInfo.add(jbLaunch, BorderLayout.LINE_END);
                jbLaunch.addActionListener(event -> {
                    try {
                        jd.dispose();
                        this.launch();
                    } catch (Exception ex) {
                        throw new RuntimeException("Cannot interact with app");
                    }
                });
            }

            JPanel jpParameters = new JPanel(new BorderLayout());
            jpContent.add(jpParameters, BorderLayout.PAGE_END);
            {
                JPanel jpUuid = new JPanel();
                jpParameters.add(jpUuid, BorderLayout.PAGE_START);
                jpUuid.setLayout(new BoxLayout(jpUuid, BoxLayout.LINE_AXIS));
                jpUuid.add(new JLabel("Devices"));
                jpUuid.add(jcbDevices);
            }
            {
                JPanel jpApp = new JPanel();
                jpParameters.add(jpApp, BorderLayout.PAGE_END);
                jpApp.setLayout(new BoxLayout(jpApp, BoxLayout.LINE_AXIS));
                jpApp.add(new JLabel("App Name"));
                jpApp.add(jtfApp);
                if (StringUtils.isNotEmpty(appName)) {
                    jtfApp.setText(appName);
                }
            }

            jd.pack();
            jd.setVisible(true);
            jd.setAlwaysOnTop(true);
            jd.setLocationRelativeTo(null);

        });
        this.detectDevices();
    }

    public void detectDevices() throws SDKException, InterruptedException {
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

    private void launch() throws Exception {
        device = new UiAutomationDevice(this.jcbDevices.getSelectedItem() + "");
        device.start(this.jtfApp.getText(), 5000);
        this.testManually(device, debugMinutes);
    }

    public static void main(String[] args) {
        String instruction = "Usage:\n"
            + "java -cp $YOUR_CLASSPATH com.tascape.qa.th.ios.tools.UiAutomationDebugger APP_NAME DEBUG_TIME_IN_MINUTE";
        LOG.info("--------\n{}", instruction);

        SystemConfiguration.getInstance();
        UiAutomationDebugger debugger = new UiAutomationDebugger();

        if (args.length > 0) {
            debugger.appName = args[0];
        }
        if (args.length > 1) {
            debugger.debugMinutes = Integer.parseInt(args[1]);
        }

        try {
            debugger.start();
        } catch (Throwable ex) {
            LOG.error("Error", ex);
            System.exit(1);
        }
    }
}
