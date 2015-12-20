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

import com.tascape.qa.th.ios.driver.UiAutomationDevice;
import javax.swing.JFrame;
import org.libimobiledevice.ios.driver.binding.exceptions.SDKException;

/**
 * This test suite supports plug-n-play for multiple devices.
 *
 * @author linsong wang
 */
public interface UiAutomationTest {

    default JFrame doManualInteraction(UiAutomationDevice device) throws SDKException, InterruptedException {
        JFrame jf = new JFrame("Device Interaction");
        // todo
        return jf;
    }
}
