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
package com.tascape.qa.th.ios.model;

import java.awt.geom.Point2D;

/**
 *
 * @author linsong wang
 */
public class UIAElement {

    UIAElement[] elements() {
        return null;
    }

    public static String toCGString(Point2D.Float point) {
        return String.format("{x:%f, y:%f}", point.x, point.y);
    }

    public static String toCGString(float x, float y) {
        return String.format("{x:%f, y:%f}", x, y);
    }
}
