#version 320 es
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

uniform lowp mat4 uMVPMatrix;
layout (location = 0) in lowp vec3 aPos;
layout (location = 1) in lowp vec2 aTexCoord;
layout (location = 2) in lowp vec3 aNormal;

out vec2 TexCoord;

void main()
{
    gl_Position = uMVPMatrix * vec4(aPos, 1.0);
    TexCoord = aTexCoord;
}
