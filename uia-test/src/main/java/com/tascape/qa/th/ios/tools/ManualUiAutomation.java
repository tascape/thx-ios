/*
 * Copyright 2016 tascape.
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

/**
 *
 * @author linsong wang
 */
public class ManualUiAutomation implements UiAutomationTest {

    public static final String APP_NAME = "Movies";

    public static void main(String[] args) throws Exception {
        SystemConfiguration.getInstance();
        String app = args.length > 0 ? args[0] : APP_NAME;
        UiAutomationDevice device = new UiAutomationDevice();
        device.start(app, 5000);
        try {
            ManualUiAutomation test = new ManualUiAutomation();
            test.testManually(device);
        } finally {
            System.exit(0);
        }
    }
}
