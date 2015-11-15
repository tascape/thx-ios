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
import com.tascape.qa.th.ios.driver.IosUiAutomationDevice;
import com.tascape.qa.th.ios.driver.LibIMobileDevice;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
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

    private final JButton jbElementTree = new JButton("Element Tree");

    private final JTextArea jtaResponse = new JTextArea();

    private final JButton jbClear = new JButton("Clear");

    public JavaScriptDebugger() {
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
        ComboBoxModel<String> model = new DefaultComboBoxModel<>(LibIMobileDevice.getAllUuids().toArray(new String[0]));
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
                device.logElementTree();
            } else if (e.getSource() == jbClear) {
                this.jtaResponse.setText("");
            }
        } catch (SDKException | IOException | InterruptedException | LipeRMIException ex) {
            JOptionPane.showMessageDialog(jSplitPane, ex.getMessage());
        }
    }

    public void launchApp() throws SDKException, IOException, InterruptedException, LipeRMIException {
        String appName = jtfApp.getText().trim();
        if (StringUtils.isEmpty(appName)) {
            JOptionPane.showMessageDialog(jSplitPane, "No app name specified");
            return;
        }

        device = new IosUiAutomationDevice(jcbDevices.getSelectedItem() + "");
        this.device.addInstrumentsStreamObserver(this);
        device.setAppName(appName);
        device.start();

        this.jcbDevices.setEnabled(false);
        this.jtfApp.setEnabled(false);
        this.jbLaunch.setEnabled(false);
        this.jbElementTree.setEnabled(true);
        this.jbSendJs.setEnabled(true);
    }

    public void sendJavaScript() throws InterruptedException {
        String js = this.jtaJavaScript.getSelectedText();
        if (StringUtils.isEmpty(js)) {
            js = this.jtaJavaScript.getText().trim();
        }
        if (!StringUtils.isEmpty(js)) {
            device.sendJavaScript(js);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        String line = arg + "\n";
        if (line.startsWith("ERROR")) {
            throw new RuntimeException(line);
        }
        this.jtaResponse.append(line);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (device != null) {
            device.stop();
        }
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        SystemConfiguration.getInstance();
        JavaScriptDebugger debugger = new JavaScriptDebugger();
        JFrame jf = new JFrame("iOS UIAutomation JavaScript Debugger");
        jf.setContentPane(debugger.getJPanel());
        jf.pack();
        jf.setVisible(true);
        jf.addWindowListener(debugger);
        jf.setLocationRelativeTo(null);

        debugger.detectDevices();
    }

    /**
     * The SmartScroller will attempt to keep the viewport positioned based on
     * the users interaction with the scrollbar. The normal behaviour is to keep
     * the viewport positioned to see new data as it is dynamically added.
     * https://tips4java.wordpress.com/2013/03/03/smart-scrolling/
     *
     * Assuming vertical scrolling and data is added to the bottom:
     *
     * - when the viewport is at the bottom and new data is added,
     * then automatically scroll the viewport to the bottom
     * - when the viewport is not at the bottom and new data is added,
     * then do nothing with the viewport
     *
     * Assuming vertical scrolling and data is added to the top:
     *
     * - when the viewport is at the top and new data is added,
     * then do nothing with the viewport
     * - when the viewport is not at the top and new data is added, then adjust
     * the viewport to the relative position it was at before the data was added
     *
     * Similiar logic would apply for horizontal scrolling.
     */
    private static class SmartScroller implements AdjustmentListener {
        public final static int HORIZONTAL = 0;

        public final static int VERTICAL = 1;

        public final static int START = 0;

        public final static int END = 1;

        private int viewportPosition;

        private JScrollBar scrollBar;

        private boolean adjustScrollBar = true;

        private int previousValue = -1;

        private int previousMaximum = -1;

        /**
         * Convenience constructor.
         * Scroll direction is VERTICAL and viewport position is at the END.
         *
         * @param scrollPane the scroll pane to monitor
         */
        public SmartScroller(JScrollPane scrollPane) {
            this(scrollPane, VERTICAL, END);
        }

        /**
         * Convenience constructor.
         * Scroll direction is VERTICAL.
         *
         * @param scrollPane       the scroll pane to monitor
         * @param viewportPosition valid values are START and END
         */
        public SmartScroller(JScrollPane scrollPane, int viewportPosition) {
            this(scrollPane, VERTICAL, viewportPosition);
        }

        /**
         * Specify how the SmartScroller will function.
         *
         * @param scrollPane       the scroll pane to monitor
         * @param scrollDirection  indicates which JScrollBar to monitor.
         *                         Valid values are HORIZONTAL and VERTICAL.
         * @param viewportPosition indicates where the viewport will normally be
         *                         positioned as data is added.
         *                         Valid values are START and END
         */
        public SmartScroller(JScrollPane scrollPane, int scrollDirection, int viewportPosition) {
            if (scrollDirection != HORIZONTAL
                && scrollDirection != VERTICAL) {
                throw new IllegalArgumentException("invalid scroll direction specified");
            }

            if (viewportPosition != START
                && viewportPosition != END) {
                throw new IllegalArgumentException("invalid viewport position specified");
            }

            this.viewportPosition = viewportPosition;

            if (scrollDirection == HORIZONTAL) {
                scrollBar = scrollPane.getHorizontalScrollBar();
            } else {
                scrollBar = scrollPane.getVerticalScrollBar();
            }

            scrollBar.addAdjustmentListener(this);

            //  Turn off automatic scrolling for text components
            Component view = scrollPane.getViewport().getView();

            if (view instanceof JTextComponent) {
                JTextComponent textComponent = (JTextComponent) view;
                DefaultCaret caret = (DefaultCaret) textComponent.getCaret();
                caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
            }
        }

        @Override
        public void adjustmentValueChanged(final AdjustmentEvent e) {
            SwingUtilities.invokeLater(() -> {
                checkScrollBar(e);
            });
        }

        /*
         * Analyze every adjustment event to determine when the viewport
         * needs to be repositioned.
         */
        private void checkScrollBar(AdjustmentEvent e) {
            //  The scroll bar listModel contains information needed to determine
            //  whether the viewport should be repositioned or not.

            JScrollBar jsb = (JScrollBar) e.getSource();
            BoundedRangeModel listModel = jsb.getModel();
            int value = listModel.getValue();
            int extent = listModel.getExtent();
            int maximum = listModel.getMaximum();

            boolean valueChanged = previousValue != value;
            boolean maximumChanged = previousMaximum != maximum;

            //  Check if the user has manually repositioned the scrollbar
            if (valueChanged && !maximumChanged) {
                if (viewportPosition == START) {
                    adjustScrollBar = value != 0;
                } else {
                    adjustScrollBar = value + extent >= maximum;
                }
            }

            //  Reset the "value" so we can reposition the viewport and
            //  distinguish between a user scroll and a program scroll.
            //  (ie. valueChanged will be false on a program scroll)
            if (adjustScrollBar && viewportPosition == END) {
                //  Scroll the viewport to the end.
                jsb.removeAdjustmentListener(this);
                value = maximum - extent;
                jsb.setValue(value);
                jsb.addAdjustmentListener(this);
            }

            if (adjustScrollBar && viewportPosition == START) {
                //  Keep the viewport at the same relative viewportPosition
                jsb.removeAdjustmentListener(this);
                value = value + maximum - previousMaximum;
                jsb.setValue(value);
                jsb.addAdjustmentListener(this);
            }

            previousValue = value;
            previousMaximum = maximum;
        }
    }
}
