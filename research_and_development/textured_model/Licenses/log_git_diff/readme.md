In this repository you can get the details of the modifications from the 
hello_ar_java app from ARCore SDK for Android 1.41.0, and the mobilevr sdk
version 1.0.0.

The command used was:
```
git diff --no-index "arcore/activity_main.xml" "mobilevr/activity_main.xml" > diff_activity_main.diff
```

## List of the modified files from the ARCore package
arcore-android-sdk-master\samples\hello_ar_java\app\src\main\res\layout\activity_main.xml

arcore-android-sdk-master\samples\hello_ar_java\app\src\main\AndroidManifest.xml

arcore-android-sdk-master\samples\hello_ar_java\app\src\main\java\com\google\ar\core\examples\java\helloar\HelloArActivity.java

arcore-android-sdk-master\samples\hello_ar_java\app\src\main\java\com\google\ar\core\examples\java\common\samplerender\Mesh.java

arcore-android-sdk-master\samples\hello_ar_java\app\src\main\java\com\google\ar\core\examples\java\common\samplerender\SampleRender.java

arcore-android-sdk-master\samples\hello_ar_java\app\src\main\java\com\google\ar\core\examples\java\common\samplerender\Shader.java

arcore-android-sdk-master\samples\hello_ar_java\app\src\main\res\values\strings.xml