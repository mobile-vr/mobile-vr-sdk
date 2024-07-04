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

out lowp vec4 FragColor;
in lowp vec2 TexCoord;

uniform lowp sampler2D map_Kd;
uniform lowp vec3 Ka;

lowp vec3 ambient;

void main()
{
    ambient = Ka * texture(map_Kd, TexCoord).rgb;
    FragColor = vec4(ambient, 1.0);
}
