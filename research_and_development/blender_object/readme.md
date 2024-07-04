Based on sdk-v1.2.0

![screenshot](/screenshots/blender_object_screenshot.jpg)

## What does it do?
Displays a textured obj speaker via OpenGL.

## Why this feature?
To demonstrate how to use the obj_shader shader.

## Limitations
- Having a clean obj
- Having a clean mtl
- textures paths in the mtl should be from the "assets" folder. For example in the assets folder, I have the model folder with my textures inside. Thus in the mtl file, the path should be: "models/speaker/Textures/PlasticMoldDryBlast002/PlasticMoldDryBlast002_COL_4K.png". Look in the mtl file of the displayed object.
- Before using a 3D model, check that when you load the obj and its mtl into Blender, the object displays properly.