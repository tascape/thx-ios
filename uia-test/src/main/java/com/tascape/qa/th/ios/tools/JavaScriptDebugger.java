/*
 * Copyright 2015.
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

import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.exception.EntityDriverException;
import com.tascape.qa.th.ios.driver.IosUiAutomationDevice;
import com.tascape.qa.th.ios.driver.LibIMobileDevice;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import net.sf.lipermi.exception.LipeRMIException;
import org.apache.commons.lang3.StringUtils;
import org.libimobiledevice.ios.driver.binding.exceptions.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class JavaScriptDebugger extends WindowAdapter implements ActionListener, Observer {
    private static final Logger LOG = LoggerFactory.getLogger(JavaScriptDebugger.class);

    private IosUiAutomationDevice device;

    private final JSplitPane jSplitPane = new JSplitPane();

    private final JComboBox<String> jcbDevices = new JComboBox<>(new String[]{"detecting devices..."});

    private final JTextField jtfApp = new JTextField("app name");

    private final JButton jbLaunch = new JButton("Launch");

    private final JTextArea jtaJavaScript = new JTextArea();

    private final JButton jbSendJs = new JButton("Send");

    private final JButton jbElementTree = new JButton("Log Element Tree");

    private final JTextArea jtaResponse = new JTextArea();

    private final JButton jbClear = new JButton("Clear");

    private String appName = "";

    public JavaScriptDebugger(String app) {
        this.appName = app;
        this.initUi();
    }

    private void initUi() {
        jSplitPane.setContinuousLayout(true);
        jSplitPane.setResizeWeight(1);
        {
            JPanel jPanel = new JPanel(new BorderLayout());
            jPanel.setPreferredSize(new Dimension(550, 600));
            jPanel.setMinimumSize(new Dimension(550, 400));
            jSplitPane.setRightComponent(jPanel);
            {
                JPanel jp = new JPanel();
                jPanel.add(jp, BorderLayout.PAGE_START);
                jp.add(jcbDevices);
                jp.add(jtfApp);
                jtfApp.setText(appName);
                jbLaunch.addActionListener(this);
                jp.add(jbLaunch);
                this.jbLaunch.setEnabled(false);
            }
            {
                JPanel jp = new JPanel(new BorderLayout());
                jp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
                jPanel.add(jp, BorderLayout.CENTER);
                JScrollPane jsp = new JScrollPane(jtaJavaScript);
                jp.add(jsp, BorderLayout.CENTER);
                jtaJavaScript.setBorder(BorderFactory.createLoweredBevelBorder());
                JPanel jpSend = new JPanel();
                jp.add(jpSend, BorderLayout.PAGE_END);
                jpSend.setLayout(new BoxLayout(jpSend, BoxLayout.LINE_AXIS));
                jpSend.add(jbElementTree);
                jbElementTree.addActionListener(this);
                jpSend.add(Box.createHorizontalGlue());
                jbElementTree.setEnabled(false);
                jpSend.add(jbSendJs);
                jbSendJs.addActionListener(this);
                jbSendJs.setEnabled(false);
            }
        }
        {
            JPanel jPanel = new JPanel(new BorderLayout());
            jPanel.setPreferredSize(new Dimension(700, 600));
            jPanel.setMinimumSize(new Dimension(600, 400));
            jPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            this.jSplitPane.setLeftComponent(jPanel);
            JScrollPane jsp = new JScrollPane(jtaResponse);
            new SmartScroller(jsp);
            jPanel.add(jsp, BorderLayout.CENTER);
            JPanel jpClear = new JPanel();
            jpClear.setLayout(new BoxLayout(jpClear, BoxLayout.LINE_AXIS));
            jPanel.add(jpClear, BorderLayout.PAGE_END);
            jpClear.add(Box.createHorizontalGlue());
            jpClear.add(jbClear);
            jbClear.addActionListener(this);
        }
    }

    public void detectDevices() throws SDKException, InterruptedException {
        List<String> uuids = LibIMobileDevice.getAllUuids();
        ComboBoxModel<String> model = new DefaultComboBoxModel<>(uuids.toArray(new String[0]));
        jcbDevices.setModel(model);
        if (model.getSize() == 0) {
            JOptionPane.showMessageDialog(jSplitPane, "No attached iOS device found.");
            this.jbLaunch.setEnabled(false);
        } else {
            this.jbLaunch.setEnabled(true);
        }
    }

    public JSplitPane getJPanel() {
        return jSplitPane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == jbLaunch) {
                this.launchApp();
            } else if (e.getSource() == jbSendJs) {
                this.sendJavaScript();
            } else if (e.getSource() == jbElementTree) {
                device.logElementTree().forEach(line -> {
                    this.appendResponse(line);
                });
            } else if (e.getSource() == jbClear) {
                this.jtaResponse.setText("");
            }
        } catch (SDKException | IOException | InterruptedException | LipeRMIException | EntityDriverException ex) {
            LOG.error("", ex);
            JOptionPane.showMessageDialog(jSplitPane, ex.getMessage());
        }
    }

    public void launchApp() throws SDKException, IOException, InterruptedException, LipeRMIException {
        String app = jtfApp.getText().trim();
        if (StringUtils.isEmpty(app)) {
            JOptionPane.showMessageDialog(jSplitPane, "No app name specified");
            return;
        }

        device = new IosUiAutomationDevice(jcbDevices.getSelectedItem() + "");
        device.start(app);

        this.jcbDevices.setEnabled(false);
        this.jtfApp.setEnabled(false);
        this.jbLaunch.setEnabled(false);
        this.jbElementTree.setEnabled(true);
        this.jbSendJs.setEnabled(true);
    }

    public void sendJavaScript() throws InterruptedException, EntityDriverException {
        String js = this.jtaJavaScript.getSelectedText();
        if (StringUtils.isEmpty(js)) {
            js = this.jtaJavaScript.getText().trim();
        }
        if (StringUtils.isEmpty(js)) {
            return;
        }
        device.sendJavaScript(js).forEach(line -> {
            this.appendResponse(line);
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        String line = arg + "\n";
        if (line.startsWith("ERROR")) {
            throw new RuntimeException(line);
        }
        this.appendResponse(line);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (device != null) {
            device.stop();
        }
        System.exit(0);
    }

    private void appendResponse(String res) {
        this.jtaResponse.append(res);
        this.jtaResponse.append("\n");
    }

    public static void main(String[] args) throws Exception {
        SystemConfiguration.getInstance();
        String app = args.length > 0 ? args[0] : "App Name";
        JavaScriptDebugger debugger = new JavaScriptDebugger(app);
        JFrame jf = new JFrame("iOS UIAutomation JavaScript Debugger");
        jf.setContentPane(debugger.getJPanel());
        jf.pack();
        jf.setVisible(true);
        jf.addWindowListener(debugger);
        jf.setLocationRelativeTo(null);

        debugger.detectDevices();
    }
}
