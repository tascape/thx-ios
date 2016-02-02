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
package com.tascape.qa.th.ios.driver;

import com.tascape.qa.th.driver.EntityDriver;
import org.libimobiledevice.ios.driver.binding.exceptions.SDKException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
@SuppressWarnings("ProtectedField")
public abstract class App extends EntityDriver {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(App.class);

    protected UiAutomationDevice uiaDevice;

    public abstract String getBundleId();

    public abstract int getLaunchDelayMillis();

    @Override
    public String getVersion() {
        try {
            return uiaDevice.getAppVersion(getBundleId());
        } catch (SDKException ex) {
            LOG.warn(ex.getMessage());
            return "na";
        }
    }

    public void attachTo(UiAutomationDevice device) throws Exception {
        this.uiaDevice = device;
        this.launch();
    }

    public UiAutomationDevice getUiaDevice() {
        return uiaDevice;
    }

    public void launch() throws Exception {
        uiaDevice.start(this.getName(), getLaunchDelayMillis());
    }
}
