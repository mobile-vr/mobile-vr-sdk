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

package com.mobilevr.utils;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Class to keep float arrays, use it as a First In First Out by creating your own method
 * that adds a new array and takes out the last after some time, or after its size is high enough
 * to you.
 *
 * How does a FIFO work:
 *     list : [e1, e2, e3, ...]
 *     e1 is at the "last" position of the queue.
 *     e3 is at the "first" position of the queue.
 */
public class FloatArrayFIFO {
    private Queue<float[]> fifo;
    public FloatArrayFIFO() {
        fifo = new LinkedList<>();
    }

    /**
     * Add an array at the first position of the queue.
     *
     * @param floatArray: float[3]
     */
    public void enqueue(float[] floatArray) {
        if (floatArray.length != 3) {
            throw new IllegalArgumentException("Input array must have a length of 3.");
        }
        fifo.offer(floatArray);
    }

    /**
     * Take out and return the last array of the queue.
     *
     * @return float[3]
     */
    public float[] dequeue() {
        return fifo.poll();
    }

    /**
     * Get the first position in the queue which is the newest position added in the fifo.
     *
     * @return float[3]
     */
    public float[] getFirst() {
        return fifo.peek();
    }

    /**
     * Returns the newest position added into the fifo.
     *
     * @return float[3]
     */
    public float[] getLast() {
        float[] last = null;
        for (float[] item : fifo) {
            last = item;
        }
        return last;
    }

    /**
     * Get the two last float array added in the FIFO.
     *
     * @return float[3]
     */
    public float[][] getTwoLast() {
        int size = this.size();
        Object[] fifoArray = fifo.toArray();
        float[] point1 = (float[]) fifoArray[size-1];
        float[] point2 = (float[]) fifoArray[size-2];
        return new float[][] {point1, point2};
    }

    /**
     * Return true if the fifo is empty.
     *
     * @return Boolean
     */
    public boolean isEmpty() {
        return fifo.isEmpty();
    }

    /**
     * Return the size of the fifo.
     *
     * @return int
     */
    public int size() {
        return fifo.size();
    }
}
