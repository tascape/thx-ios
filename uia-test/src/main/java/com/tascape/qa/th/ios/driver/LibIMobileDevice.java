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
package com.tascape.qa.th.ios.driver;

import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.driver.EntityDriver;
import com.tascape.qa.th.exception.EntityDriverException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.imaging.ImageReadException;
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

    public static List<String> getAllUuids() throws SDKException, InterruptedException {
        LOG.info("Detecting attached devices");
        List<String> uuids = new ArrayList<>();
        new DeviceService().startDetection(new DeviceCallBack() {
            @Override
            protected void onDeviceAdded(String uuid) {
                LOG.info("uuid {}", uuid);
                try {
                    uuids.add(uuid);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            protected void onDeviceRemoved(String uuid) {
                uuids.remove(uuid);
            }
        });
        Thread.sleep(5000);
        return uuids;
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

    public IOSDevice getIosDevice() {
        return iosDevice;
    }

    @Override
    public String getName() {
        try {
            return String.format("%s, %s, %s",
                informationService.getDeviceName(),
                informationService.getDeviceType(),
                informationService.getProductVersion());
        } catch (SDKException ex) {
            return LibIMobileDevice.class.getName();
        }
    }

    @Override
    public void reset() throws Exception {
        LOG.debug("NA");
    }

    public void launchApp(String bundleId) throws SDKException {
        String id = installerService.getApplication("com.bcgdv.haoyun").getApplicationId();
        LOG.debug("app id " + id);
        debugService.launch(id);
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

    public List<String> getApps() throws SDKException {
        return installerService.listApplications(InstallerService.ApplicationType.USER).stream()
            .map(app -> app.getApplicationId()).collect(Collectors.toList());
    }

    public File takeDeviceScreenshot() throws EntityDriverException {
        try {
            File png = this.getLogPath().resolve("ss-" + System.currentTimeMillis() + ".png").toFile();
            png.mkdirs();
            png.createNewFile();
            this.screenshotService.takeScreenshot(png);
            LOG.debug("Save screenshot to {}", png.getAbsolutePath());
            return png;
        } catch (IOException | SDKException | ImageReadException ex) {
            throw new EntityDriverException(ex);
        }
    }

    public static void main(String[] args) throws Exception {
        SystemConfiguration.getInstance();
        List<String> uuids = LibIMobileDevice.getAllUuids();
        LibIMobileDevice device = new LibIMobileDevice(uuids.get(0));
        String id = device.getInstallerService().getApplication("com.bcgdv.haoyun").getApplicationId();
        LOG.debug(id);
        device.getDebugService().launch(id);
    }
}
