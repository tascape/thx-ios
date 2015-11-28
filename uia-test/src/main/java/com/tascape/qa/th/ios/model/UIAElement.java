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
package com.tascape.qa.th.ios.model;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author linsong wang
 */
public interface UIAElement {

    static String toCGString(Rectangle2D.Float rect) {
        return String.format("{{%f, %f}, {%f, %f}}", rect.x, rect.y, rect.width, rect.height);
    }

    static String toCGString(Point2D.Float point) {
        return String.format("{x:%f, y:%f}", point.x, point.y);
    }

    static String toCGString(float x, float y) {
        return String.format("{x:%f, y:%f}", x, y);
    }
}
