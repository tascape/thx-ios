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
package com.tascape.qa.th.ios.model;

import com.tascape.qa.th.ios.comm.Instruments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class UIAWindow extends UIAElement {
    private static final Logger LOG = LoggerFactory.getLogger(UIAWindow.class);

    @Override
    public void setInstruments(Instruments instruments) {
        super.setInstruments(instruments);
    }

    @Override
    public <T extends UIAElement> T findElement(Class<T> type, String name) {
        LOG.debug("Look for {}['{}']", type.getSimpleName(), name);
        return type.cast(super.findElement(type, name));
    }

    public UIAButton findButton(String name) {
        return this.findElement(UIAButton.class, name);
    }

    public UIAStaticText findStaticText(String name) {
        return this.findElement(UIAStaticText.class, name);
    }

    public UIATextView findTextView(String name) {
        return this.findElement(UIATextView.class, name);
    }

    public UIALink findLink(String name) {
        return this.findElement(UIALink.class, name);
    }
}
