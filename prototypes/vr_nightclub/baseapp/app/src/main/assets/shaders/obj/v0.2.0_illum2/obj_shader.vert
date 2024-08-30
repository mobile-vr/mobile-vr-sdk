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

uniform lowp mat4 model;
uniform lowp mat4 view;
uniform lowp mat4 projection;
uniform lowp vec3 textureOffset; // Uniform for texture coordinate offset

layout (location = 0) in lowp vec3 aPos;
layout (location = 1) in lowp vec2 aTexCoord;
layout (location = 2) in lowp vec3 aNormal;

out vec3 vNormal;
out vec2 vTexCoord;
out vec3 vFragPos;

void main()
{
    // Calculate the position of the vertex in world space
    lowp vec4 worldPosition = model * vec4(aPos, 1.0);
    vFragPos = worldPosition.xyz;

    // Pass the normal to the fragment shader
    vNormal = mat3(transpose(inverse(model))) * aNormal;

    // Apply texture coordinate offset if textureOffset is not null
    if (textureOffset != vec3(0.0)) {
        vTexCoord = aTexCoord + textureOffset.xy;
    } else {
        vTexCoord = aTexCoord; // Use default texture coordinates if offset is not provided
    }

    // create the gl_Position: Output final vertex position (clip space)
    gl_Position = projection * view * worldPosition;
}
