# ImageFilter-FloydSteinberg
Android app for pick and crop an image and apply - FloydSteinberg - Error Diffusion algorithm 

## Documentation

You can find project documentation [here](https://docs.google.com/document/d/13zo047KhsjRp6bn2xlypTmduJ1FbdKjcpA2nC8NPdho/edit?usp=sharing).

## Source code: 

You can find implementation of an alghorithm of The Floyd–Steinberg error diffusion filtering :

  *  Only alghorithm at class [Dither](https://github.com/Bzahov98/ImageFilter-FloydSteinberg/blob/master/app/src/main/java/com/example/imagefilter/transformations/Dither.java])
  *  Alghorithm is embeded into Transformation, at class [DitherTransformation](https://github.com/Bzahov98/ImageFilter-FloydSteinberg/blob/master/app/src/main/java/com/example/imagefilter/transformations/Dither.java])

## Executable APK file: 
You can download latest release of the Executable APK file from [here](https://github.com/Bzahov98/ImageFilter-FloydSteinberg/releases)
Може да свалите изпълнимия код на приложението и да го изпробвате от тук.

## App User flow

* User choose an image from his gallery
* User choose image region to crop
* Apply error diffusion filtering on cropped area
* Show filtered image
