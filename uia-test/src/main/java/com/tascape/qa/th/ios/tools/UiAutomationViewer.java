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

import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.ios.driver.UiAutomationDevice;
import com.tascape.qa.th.ios.test.UiAutomationTest;
import java.util.List;
import javax.swing.JOptionPane;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class UiAutomationViewer implements UiAutomationTest {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(UiAutomationViewer.class);

    private String uuid;

    private UiAutomationDevice device;

    private String appName = "Movies";

    private int timeoutMinutes = 30;

    private void initDevice() throws Exception {
        List<String> uuids = UiAutomationDevice.getAllUuids();
        Object input = JOptionPane.showInputDialog(null, "Please select a device id", "All connected devices",
            JOptionPane.INFORMATION_MESSAGE, null, uuids.toArray(), uuids.get(0));
        this.uuid = input + "";

        input = JOptionPane.showInputDialog(null, "Please type app name", "iOS App Name",
            JOptionPane.INFORMATION_MESSAGE, null, null, appName);
        this.appName = input + "";

        Object timeout = JOptionPane.showInputDialog(null, "Please select total time (minute)",
            "Total Interaction Time", JOptionPane.INFORMATION_MESSAGE, null, new Integer[]{15, 30, 60, 120}, 30);
        this.timeoutMinutes = Integer.parseInt(timeout + "");
    }

    private void start() throws Exception {
        device = new UiAutomationDevice(uuid + "");
        device.start(appName, 5000);
        testManually(device, timeoutMinutes);
    }

    public static void main(String[] args) {
        String instruction = "Usage:\n"
            + "java -cp $YOUR_CLASSPATH com.tascape.qa.th.ios.tools.UiAutomationDebugger APP_NAME DEBUG_TIME_IN_MINUTE";
        LOG.info("--------\n{}", instruction);

        SystemConfiguration.getInstance();
        UiAutomationViewer debugger = new UiAutomationViewer();

        if (args.length > 0) {
            debugger.appName = args[0];
        }

        try {
            debugger.initDevice();
            debugger.start();
        } catch (Throwable ex) {
            LOG.error("Error", ex);
            System.exit(1);
        }
        System.exit(0);
    }
}
