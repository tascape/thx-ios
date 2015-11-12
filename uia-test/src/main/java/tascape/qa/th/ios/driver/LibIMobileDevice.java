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
package tascape.qa.th.ios.driver;

import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.driver.EntityDriver;
import java.util.HashMap;
import java.util.Map;
import org.libimobiledevice.ios.driver.binding.exceptions.SDKException;
import org.libimobiledevice.ios.driver.binding.raw.JNAInit;
import org.libimobiledevice.ios.driver.binding.services.AppContainerService;
import org.libimobiledevice.ios.driver.binding.services.DebugService;
import org.libimobiledevice.ios.driver.binding.services.DeviceCallBack;
import org.libimobiledevice.ios.driver.binding.services.DeviceService;
import org.libimobiledevice.ios.driver.binding.services.IOSDevice;
import org.libimobiledevice.ios.driver.binding.services.ImageMountingService;
import org.libimobiledevice.ios.driver.binding.services.InformationService;
import org.libimobiledevice.ios.driver.binding.services.InstallerService;
import org.libimobiledevice.ios.driver.binding.services.ProvisioningService;
import org.libimobiledevice.ios.driver.binding.services.ScreenshotService;
import org.libimobiledevice.ios.driver.binding.services.SysLogService;
import org.libimobiledevice.ios.driver.binding.services.WebInspectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class LibIMobileDevice extends EntityDriver {
    private static final Logger LOG = LoggerFactory.getLogger(LibIMobileDevice.class);

    static {
        JNAInit.init();
    }

    private final IOSDevice iosDevice;

    private final AppContainerService appContainerService;

    private final DebugService debugService;

    private final ImageMountingService imageMountingService;

    private final InformationService informationService;

    private final InstallerService installerService;

    private final ProvisioningService provisioningService;

    private final ScreenshotService screenshotService;

    private final SysLogService sysLogService;

    private final WebInspectorService webInspectorService;

    public static Map<String, LibIMobileDevice> getAllDevices() throws SDKException, InterruptedException {
        Map<String, LibIMobileDevice> devices = new HashMap<>();

        new DeviceService().startDetection(new DeviceCallBack() {
            @Override
            protected void onDeviceAdded(String uuid) {
                LOG.debug("{}", uuid);
                try {
                    devices.put(uuid, new LibIMobileDevice(uuid));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            protected void onDeviceRemoved(String uuid) {
                devices.remove(uuid);
            }
        });

        LOG.debug("detecting attached devices");
        Thread.sleep(5000);
        return devices;
    }

    public LibIMobileDevice(String udid) throws SDKException {
        this.iosDevice = DeviceService.get(udid);
        this.appContainerService = new AppContainerService(iosDevice);
        this.debugService = new DebugService(iosDevice);
        this.imageMountingService = new ImageMountingService(iosDevice);
        this.informationService = new InformationService(iosDevice);
        this.installerService = new InstallerService(iosDevice);
        this.provisioningService = new ProvisioningService(iosDevice);
        this.screenshotService = new ScreenshotService(iosDevice);
        this.sysLogService = this.iosDevice.getSysLogService();
        this.webInspectorService = new WebInspectorService(iosDevice);

        LOG.debug("{}, {}, {}, dev mode {}",
            informationService.getDeviceName(),
            informationService.getDeviceType(),
            informationService.getProductVersion(),
            informationService.isDevModeEnabled());
    }

    @Override
    public String getName() {
        return LibIMobileDevice.class.getSimpleName();
    }

    @Override
    public void reset() throws Exception {
        LOG.debug("NA");
    }

    public AppContainerService getAppContainerService() {
        return appContainerService;
    }

    public DebugService getDebugService() {
        return debugService;
    }

    public ImageMountingService getImageMountingService() {
        return imageMountingService;
    }

    public InformationService getInformationService() {
        return informationService;
    }

    public InstallerService getInstallerService() {
        return installerService;
    }

    public ProvisioningService getProvisioningService() {
        return provisioningService;
    }

    public ScreenshotService getScreenshotService() {
        return screenshotService;
    }

    public SysLogService getSysLogService() {
        return sysLogService;
    }

    public WebInspectorService getWebInspectorService() {
        return webInspectorService;
    }

    public static void main(String[] args) throws Exception {
        SystemConfiguration.getInstance();
        Map<String, LibIMobileDevice> devices = LibIMobileDevice.getAllDevices();
        LibIMobileDevice device = devices.values().iterator().next();
        String id = device.getInstallerService().getApplication("com.bcgdv.haoyun")
            .getApplicationId();
        LOG.debug(id);
        device.getDebugService().launch(id);
    }
}
