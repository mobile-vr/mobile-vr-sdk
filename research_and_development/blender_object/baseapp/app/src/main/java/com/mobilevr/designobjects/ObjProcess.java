package com.mobilevr.designobjects;

import android.content.Context;

import com.mobilevr.modified.samplerender.Mesh;
import com.mobilevr.modified.samplerender.SampleRender;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.javagl.obj.Mtl;
import de.javagl.obj.MtlReader;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjSplitting;
import de.javagl.obj.ObjUtils;

public class ObjProcess {
    private Map<String, SubObject> subObjects;

    public ObjProcess(Context context, SampleRender render, String objPath, String mtlPath) throws IOException {
        // Obj processing
        try (InputStream inputStream = context.getAssets().open(objPath)) {
            Obj originalObj = ObjReader.read(inputStream);
            Map<String, Obj> mtlObjs = ObjSplitting.splitByMaterialGroups(originalObj);

            subObjects = new HashMap<>();

            for (Map.Entry<String, Obj> entry : mtlObjs.entrySet()) {
                String key = entry.getKey();
                Obj value = entry.getValue();
                Obj obj = ObjUtils.convertToRenderable(value);
                Mesh mesh = Mesh.createFromObj(render, obj);

                SubObject subObject = new SubObject();
                subObject.setMesh(mesh);

                subObjects.put(key, subObject);
            }

        }

        // mtl processing
        try (InputStream inputStream = context.getAssets().open(mtlPath)) {
            List<Mtl> mtls = MtlReader.read(inputStream);

            if (mtls.size() != subObjects.size()) {
                throw new IllegalArgumentException("Number of materials (mtls) does not match number of sub-objects");
            }

            for (Mtl mtl : mtls) {
                String key = mtl.getName();
                Objects.requireNonNull(subObjects.get(key)).setMtl(mtl);
            }
        }

    }

    public Map<String, SubObject> getSubObjects() {
        return subObjects;
    }
}
