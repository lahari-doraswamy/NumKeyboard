# NumKeyboard
>Step 1. Add the JitPack repository to your build file

''' gradel
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
 '''

>Add the dependency

'''gradel
dependencies {
	        implementation 'com.github.lahari-doraswamy:NumKeyboard:Tag'
	}
'''
