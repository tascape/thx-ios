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

    private String appName = "Movies";

    private int debugMinutes = 30;

    private void start() throws Exception {
        // todo: add ui for parameter update

        device = new UiAutomationDevice();
        device.start(appName, 5000);
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
        System.exit(0);
    }
}
