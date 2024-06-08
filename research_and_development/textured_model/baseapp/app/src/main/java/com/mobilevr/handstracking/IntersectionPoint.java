/*
 * Copyright 2024 MasterHansCoding (GitHub)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobilevr.handstracking;

/**
 * Class to communicate the intersection point information like:
 *   - its coordinates in World Coordinate frame,
 *   - if this intersection is touching its target.
 *   - if the action attribute is true.
 */
public class IntersectionPoint {
    public float[] point;
    public Boolean touching;
    public Boolean action;
    private int actionTrueCounter, actionFalseCounter;

    public IntersectionPoint() {

        touching = false;
        action = false;
        actionTrueCounter = 0;
        actionFalseCounter = 0;
    }

    /**
     * Set the touching attribute to true or false.
     *
     * @param myTouching: Boolean.
     */
    public void setTouching(Boolean myTouching) {
        touching = myTouching;
    }

    /**
     * Set action attribute to true or false.
     *
     * @param myAction: Boolean.
     */
    public void setAction(Boolean myAction) {
        if (myAction) {
            actionTrueCounter += 1;
            actionFalseCounter = 0;
        } else {
            actionFalseCounter += 1;
            actionTrueCounter = 0;
        }
        if (actionTrueCounter > 2) {
            action = true;
        } else if (actionFalseCounter > 2) {
            action = false;
        }
    }

    /**
     * Set the intersection point values.
     *
     * @param myPoint: float[3].
     */
    public void setPoint(float[] myPoint){
        point = myPoint;
    }
}
