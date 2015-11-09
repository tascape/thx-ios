/*
 * Copyright 2012-2013 eBay Software Foundation and ios-driver committers
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.libimobiledevice.ios.driver.binding.services;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.libimobiledevice.ios.driver.binding.raw.ImobiledeviceSdkLibrary.sdk_idevice_installation_service_status_cb_t;

public abstract class InstallCallback implements sdk_idevice_installation_service_status_cb_t {
    private static final Logger LOG = LoggerFactory.getLogger(IOSDevice.class);

    @Override
    public void apply(String operation, String message, int precent_complete, Pointer user_data) {
        int percent = precent_complete;
        if (precent_complete == -1) {
            percent = 100;
        }
        onUpdate(operation, percent, message);
    }

    protected abstract void onUpdate(String operation, int percent, String message);

    protected void sout(String operation, int percent, String message) {
        LOG.debug(operation + " ," + percent + " %" + " , " + message);
    }
}
