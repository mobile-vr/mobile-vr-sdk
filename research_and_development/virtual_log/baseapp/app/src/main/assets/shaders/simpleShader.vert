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
uniform mat4 uMVPMatrix;
attribute vec4 vPosition;
void main() {
  // the matrix must be included as a modifier of gl_Position
  // Note that the uMVPMatrix factor *must be first* in order
  // for the matrix multiplication product to be correct.
  gl_Position = uMVPMatrix * vPosition;
}