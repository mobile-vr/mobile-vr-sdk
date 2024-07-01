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
uniform lowp sampler2D map_Ks; // (Specular exponent texture map)
uniform lowp sampler2D map_Bump; //(Bump map texture)
uniform lowp vec3 Ka; // (ambient color)
uniform lowp vec3 Ks; // (specular color)
uniform lowp vec3 Ke; // (emissive color)
uniform lowp float Ni; // (optical density)
uniform lowp float d; // (dissolve factor)
uniform lowp float bumpMultiplier;
uniform lowp int illum; // (illumination model)
uniform lowp int map_Kd_presence;
uniform lowp int map_Ks_presence;
uniform lowp int map_Bump_presence;
uniform lowp vec3 lightPos; // (Light position)
uniform lowp vec3 viewPos; // (Camera position)
uniform lowp vec3 lightColor; // (Light color)

out lowp vec4 FragColor;

lowp vec3 ambient;

lowp vec3 calculateRefraction(lowp vec3 I, lowp vec3 N, lowp float eta) {
    lowp float cosi = clamp(dot(I, N), -1.0, 1.0);
    lowp float etai = 1.0, etat = eta;
    lowp vec3 n = N;
    if (cosi < 0.0) {
        cosi = -cosi;
    } else {
        n = -N;
        lowp float tmp = etai;
        etai = etat;
        etat = tmp;
    }
    lowp float etaRatio = etai / etat;
    lowp float k = 1.0 - etaRatio * etaRatio * (1.0 - cosi * cosi);
    return k < 0.0 ? vec3(0.0) : etaRatio * I + (etaRatio * cosi - sqrt(k)) * n;
}

void main()
{
    // Initialize texture samples
    lowp vec3 diffuseColor = vec3(1.0); // Default white color if map_Kd is null
    lowp float specularExponent = 1.0; // Default specular exponent if map_Ks is null
    lowp vec3 bump = vec3(0.5); // Default bump if map_Bump is null

    // Texture sampling
    if (map_Kd_presence != 1)
    {
        diffuseColor = texture(map_Kd, vTexCoord).rgb;
    }
    if (map_Ks_presence != 1)
    {
        specularExponent = texture(map_Ks, vTexCoord).r;
    }
    if (map_Bump_presence != 1)
    {
        bump = texture(map_Bump, vTexCoord).rgb;
    }

    // Normal mapping
    lowp vec3 norm = normalize(vNormal + (bump * 2.0 - 1.0) * bumpMultiplier);

    // Create ambient light
    ambient = Ka * diffuseColor;

    // Diffuse lighting
    lowp vec3 lightDir = normalize(lightPos - vFragPos);
    lowp float diff = max(dot(norm, lightDir), 0.0);
    lowp vec3 diffuse = diff * diffuseColor * lightColor;

    // Specular lighting
    lowp vec3 viewDir = normalize(viewPos - vFragPos);
    lowp vec3 reflectDir = reflect(-lightDir, norm);
    lowp float spec = pow(max(dot(viewDir, reflectDir), 0.0), specularExponent);
    lowp vec3 specular = Ks * spec * lightColor;

    // Refraction lighting
    lowp vec3 R = calculateRefraction(viewDir, norm, Ni);
    lowp vec3 refractionColor = texture(map_Ks , vTexCoord + R.xy).rgb;

    // Combine results
    lowp vec3 lighting = ambient + diffuse + specular + refractionColor;

    // Apply illumination model
    if (illum == 2)
    {
        lighting += Ke; // add emissive component
    }

    // Opacity
    FragColor = vec4(lighting, d);
}
