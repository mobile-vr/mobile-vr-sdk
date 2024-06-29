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

in lowp vec3 vNormal;
in lowp vec2 vTexCoord;
in lowp vec3 vFragPos;

uniform lowp sampler2D map_Kd;
uniform lowp sampler2D map_Ns; // (Specular exponent texture map)
uniform lowp sampler2D map_Bump; //(Bump map texture)
uniform lowp vec3 Ka; // (ambient color)
uniform lowp vec3 Ks; // (specular color)
uniform lowp vec3 Ke; // (emissive color)
uniform lowp float Ni; // (optical density)
uniform lowp float d; // (dissolve factor)
uniform lowp float bumpMultiplier;
uniform lowp int illum; // (illumination model)
uniform lowp vec3 lightPos; // (Light position)
uniform lowp vec3 viewPos; // (Camera position)
uniform lowp vec3 lightColor; // (Light color)

out lowp vec4 FragColor;

lowp vec3 ambient;

void main()
{
    // Initialize texture samples
    lowp vec3 diffuseColor = vec3(1.0); // Default white color if map_Kd is null
    lowp float specularExponent = 1.0; // Default specular exponent if map_Ns is null
    lowp vec3 bump = vec3(0.5); // Default bump if map_Bump is null

    // Texture sampling
    if (map_Kd != 0)
    {
        diffuseColor = texture(map_Kd, vTexCoord).rgb;
    }
    if (map_Ns != 0)
    {
        specularExponent = texture(map_Ns, vTexCoord).r;
    }
    if (map_Bump != 0)
    {
        bump = texture(map_Bump, vTexCoord).rgb;
    }

    // Normal mapping
    lowp vec3 norm = normalize(vNormal + (bump * 2.0 - 1.0) * bumpMultiplier);

    // Create ambient light
    ambient = Ka * diffuseColor;

    // Diffuse lighting
    lowp vec3 lightDir = normalize(lightPos - FragPos);
    lowp float diff = max(dot(norm, lightDir), 0.0);
    lowp vec3 diffuse = diff * diffuseColor * lightColor;

    // Specular lighting
    lowp vec3 viewDir = normalize(viewPos - FragPos);
    lowp vec3 reflectDir = reflect(-lightDir, norm);
    lowp float spec = pow(max(dot(viewDir, reflectDir), 0.0), specularExponent);
    lowp vec3 specular = Ks * spec * lightColor;

    // Combine results
    lowp vec3 lighting = ambient + diffuse + specular;

    // Apply illumination model
    if (illum == 2)
    {
        lighting += Ke; // add emissive component
    }

    // Opacity
    FragColor = vec4(lighting, d);
}
