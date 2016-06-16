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
package com.tascape.qa.th.ios.driver;

import com.tascape.qa.th.driver.EntityDriver;
import com.tascape.qa.th.ios.model.UIAElement;
import com.tascape.qa.th.ios.model.UIAException;
import com.tascape.qa.th.ios.model.UIAWindow;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
@SuppressWarnings("ProtectedField")
public abstract class App extends EntityDriver {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    protected UiAutomationDevice device;

    public abstract String getBundleId();

    public abstract int getLaunchDelayMillis();

    protected String version;

    @Override
    public String getVersion() {
        if (StringUtils.isBlank(version)) {
            try {
                version = device.getAppVersion(getBundleId());
            } catch (Exception ex) {
                LOG.warn(ex.getMessage());
                version = "";
            }
        }
        return version;
    }

    public int getLaunchTries() {
        return 2;
    }

    public void launch() throws Exception {
        device.getDebugService().killApp(this.getBundleId());

        device.setAlertAutoDismiss();
        device.start(this.getName(), getLaunchTries(), getLaunchDelayMillis());
    }

    public UiAutomationDevice getDevice() {
        return device;
    }

    public void setDevice(UiAutomationDevice device) {
        this.device = device;
    }

    public void interactManually() throws Exception {
        interactManually(30);
    }

    /**
     * The method starts a GUI to let an user inspect element tree and take screenshot when the user is interacting
     * with the app-under-test manually. It is also possible to run UI Automation instruments JavaScript via this UI.
     * Please make sure to set timeout long enough for manual interaction.
     *
     * @param timeoutMinutes timeout in minutes to fail the manual steps
     *
     * @throws Exception if case of error
     */
    public void interactManually(int timeoutMinutes) throws Exception {
        LOG.info("Start manual UI interaction");
        long end = System.currentTimeMillis() + timeoutMinutes * 60000L;

        AtomicBoolean visible = new AtomicBoolean(true);
        AtomicBoolean pass = new AtomicBoolean(false);
        String tName = Thread.currentThread().getName() + "m";
        SwingUtilities.invokeLater(() -> {
            String info = device.getProductDetail() + "" + device.getUuid();
            JDialog jd = new JDialog((JFrame) null, "Manual Device UI Interaction - " + info);
            jd.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            JPanel jpContent = new JPanel(new BorderLayout());
            jd.setContentPane(jpContent);
            jpContent.setPreferredSize(new Dimension(1088, 828));
            jpContent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JPanel jpInfo = new JPanel();
            jpContent.add(jpInfo, BorderLayout.PAGE_START);
            jpInfo.setLayout(new BorderLayout());
            {
                JButton jb = new JButton("PASS");
                jb.setForeground(Color.green.darker());
                jb.setFont(jb.getFont().deriveFont(Font.BOLD));
                jpInfo.add(jb, BorderLayout.LINE_START);
                jb.addActionListener(event -> {
                    pass.set(true);
                    jd.dispose();
                    visible.set(false);
                });
            }
            {
                JButton jb = new JButton("FAIL");
                jb.setForeground(Color.red);
                jb.setFont(jb.getFont().deriveFont(Font.BOLD));
                jpInfo.add(jb, BorderLayout.LINE_END);
                jb.addActionListener(event -> {
                    pass.set(false);
                    jd.dispose();
                    visible.set(false);
                });
            }

            JLabel jlTimeout = new JLabel("xxx seconds left", SwingConstants.CENTER);
            jpInfo.add(jlTimeout, BorderLayout.CENTER);
            jpInfo.add(jlTimeout, BorderLayout.CENTER);
            new SwingWorker<Long, Long>() {
                @Override
                protected Long doInBackground() throws Exception {
                    while (System.currentTimeMillis() < end) {
                        Thread.sleep(1000);
                        long left = (end - System.currentTimeMillis()) / 1000;
                        this.publish(left);
                    }
                    return 0L;
                }

                @Override
                protected void process(List<Long> chunks) {
                    Long l = chunks.get(chunks.size() - 1);
                    jlTimeout.setText(l + " seconds left");
                    if (l < 850) {
                        jlTimeout.setForeground(Color.red);
                    }
                }
            }.execute();

            JPanel jpResponse = new JPanel(new BorderLayout());
            JPanel jpProgress = new JPanel(new BorderLayout());
            jpResponse.add(jpProgress, BorderLayout.PAGE_START);

            JTextArea jtaResponse = new JTextArea();
            jtaResponse.setEditable(false);
            jtaResponse.setTabSize(4);
            Font font = jtaResponse.getFont();
            jtaResponse.setFont(new Font("Courier New", font.getStyle(), font.getSize()));

            JTabbedPane jtp = new JTabbedPane();
            JTree jtView = new JTree();
            jtView.setVisible(false);
            jtView.setCellRenderer(new UIAElementCellRenderer());
            jtp.add("tree", new JScrollPane(jtView));
            jtp.add("text", new JScrollPane(new JScrollPane(jtaResponse)));
            jpResponse.add(jtp, BorderLayout.CENTER);

            JPanel jpScreen = new JPanel();
            jpScreen.setMinimumSize(new Dimension(200, 200));
            jpScreen.setLayout(new BoxLayout(jpScreen, BoxLayout.PAGE_AXIS));
            JScrollPane jsp1 = new JScrollPane(jpScreen);
            jpResponse.add(jsp1, BorderLayout.LINE_START);

            JPanel jpJs = new JPanel(new BorderLayout());
            JTextArea jtaJs = new JTextArea();
            jpJs.add(new JScrollPane(jtaJs), BorderLayout.CENTER);

            JSplitPane jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jpResponse, jpJs);
            jSplitPane.setResizeWeight(0.88);
            jpContent.add(jSplitPane, BorderLayout.CENTER);

            JPanel jpLog = new JPanel();
            jpLog.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            jpLog.setLayout(new BoxLayout(jpLog, BoxLayout.LINE_AXIS));

            JCheckBox jcbTap = new JCheckBox("Enable Tap", null, false);
            jpLog.add(jcbTap);
            jpLog.add(Box.createHorizontalStrut(8));

            JButton jbLogUi = new JButton("Log Main Window");
            jpResponse.add(jpLog, BorderLayout.PAGE_END);
            {
                jpLog.add(jbLogUi);
                jbLogUi.addActionListener((ActionEvent event) -> {
                    jtaResponse.setText("waiting for screenshot...");
                    Thread t = new Thread(tName) {
                        @Override
                        public void run() {
                            LOG.debug("\n\n");
                            try {

                                File png = device.takeDeviceScreenshot();
                                BufferedImage image = ImageIO.read(png);

                                int w = device.getDisplaySize().width;
                                int h = device.getDisplaySize().height;

                                BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                                Graphics2D g2 = resizedImg.createGraphics();
                                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                                g2.drawImage(image, 0, 0, w, h, null);
                                g2.dispose();

                                JLabel jLabel = new JLabel(new ImageIcon(resizedImg));
                                jpScreen.removeAll();
                                jsp1.setPreferredSize(new Dimension(w + 30, h));
                                jpScreen.add(jLabel);

                                jLabel.addMouseListener(new MouseAdapter() {
                                    @Override
                                    public void mouseClicked(MouseEvent e) {
                                        LOG.debug("clicked at {},{}", e.getPoint().getX(), e.getPoint().getY());
                                        if (jcbTap.isSelected()) {
                                            device.tap(e.getPoint().x, e.getPoint().y);
                                            jbLogUi.doClick();
                                        }
                                    }
                                });

                                UIAWindow window = device.mainWindow();
                                jtView.setModel(getModel(window));
                                jtView.setVisible(true);
                                jtaResponse.setText("");
                                window.logElement().forEach(line -> {
                                    LOG.debug(line);
                                    jtaResponse.append(line);
                                    jtaResponse.append("\n");
                                });

                            } catch (Exception ex) {
                                LOG.error("Cannot log screen", ex);
                                jtaResponse.append("Cannot log screen");
                            }
                            jtaResponse.append("\n\n\n");
                            LOG.debug("\n\n");

                            jd.setSize(jd.getBounds().width + 1, jd.getBounds().height + 1);
                            jd.setSize(jd.getBounds().width - 1, jd.getBounds().height - 1);
                        }
                    };
                    t.start();
                });
            }
            jpLog.add(Box.createHorizontalStrut(38));
            {
                JButton jbLogMsg = new JButton("Log Message");
                jpLog.add(jbLogMsg);
                JTextField jtMsg = new JTextField(10);
                jpLog.add(jtMsg);
                jtMsg.addFocusListener(new FocusListener() {
                    @Override
                    public void focusLost(final FocusEvent pE) {
                    }

                    @Override
                    public void focusGained(final FocusEvent pE) {
                        jtMsg.selectAll();
                    }
                });
                jtMsg.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            jbLogMsg.doClick();
                        }
                    }
                });
                jbLogMsg.addActionListener(event -> {
                    Thread t = new Thread(tName) {
                        @Override
                        public void run() {
                            String msg = jtMsg.getText();
                            if (StringUtils.isNotBlank(msg)) {
                                LOG.info("{}", msg);
                                jtMsg.selectAll();
                            }
                        }
                    };
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException ex) {
                        LOG.error("Cannot take screenshot", ex);
                    }
                    jtMsg.requestFocus();
                });
            }
            jpLog.add(Box.createHorizontalStrut(38));
            {
                JButton jbClear = new JButton("Clear");
                jpLog.add(jbClear);
                jbClear.addActionListener(event -> {
                    jtaResponse.setText("");
                });
            }

            JPanel jpAction = new JPanel();
            jpContent.add(jpAction, BorderLayout.PAGE_END);
            jpAction.setLayout(new BoxLayout(jpAction, BoxLayout.LINE_AXIS));
            jpJs.add(jpAction, BorderLayout.PAGE_END);
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
                        @Override
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

            jd.pack();
            jd.setVisible(true);
            jd.setLocationRelativeTo(null);

            jbLogUi.doClick();
        });

        while (visible.get()) {
            if (System.currentTimeMillis() > end) {
                LOG.error("Manual UI interaction timeout");
                break;
            }
            Thread.sleep(500);
        }

        if (pass.get()) {
            LOG.info("Manual UI Interaction returns PASS");
        } else {
            Assert.fail("Manual UI Interaction returns FAIL");
        }
    }

    private TreeModel getModel(UIAWindow window) {
        DefaultMutableTreeNode rootNode = createNode(window);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        return treeModel;
    }

    private DefaultMutableTreeNode createNode(UIAElement element) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(element);
        for (UIAElement e : element.elements()) {
            node.add(createNode(e));
        }
        return node;
    }

    private static class UIAElementCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            Object uo = ((DefaultMutableTreeNode) value).getUserObject();
            if (uo instanceof UIAElement) {
                UIAElement element = (UIAElement) uo;
                Rectangle.Float rect = element.rect();
                String s = element.getClass().getSimpleName() + " " + element.name() + " "
                    + String.format("[%s,%s,%s,%s]", rect.x, rect.y, rect.width, rect.height);
                this.setText(s);
            }
            return this;
        }
    }
}
