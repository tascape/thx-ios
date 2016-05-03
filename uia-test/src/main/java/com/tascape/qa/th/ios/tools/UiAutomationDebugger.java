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
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class UiAutomationDebugger implements UiAutomationTest {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(UiAutomationDebugger.class);

    private UiAutomationDevice device;

    private String appName = "Moview";

    private int debugMinutes = 30;

    private UiAutomationDebugger() {
        // todo: add ui for parameter input
    }

    private void start() throws Exception {
        device = new UiAutomationDevice();
        device.start(appName, 5000);
        this.testManually(device, debugMinutes);
    }

    public static void main(String[] args) {
        SystemConfiguration.getInstance();

        UiAutomationDebugger debugger = new UiAutomationDebugger();
        try {
            debugger.start();
        } catch (Throwable ex) {
            LOG.error("Error", ex);
            System.exit(1);
        }
        System.exit(0);
    }
}
