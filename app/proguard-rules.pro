-dontobfuscate

-keepclassmembers class * extends org.insa.cipherdit.io.WritableObject {
	*;
}

-keepclassmembers class * extends org.insa.cipherdit.jsonwrap.JsonObject$JsonDeserializable {
	*;
}

-keepclassmembers class org.insa.cipherdit.R { *; }
-keepclassmembers class org.insa.cipherdit.R$xml {	*; }
-keepclassmembers class org.insa.cipherdit.R$string {	*; }

-keepclassmembers class com.github.luben.zstd.* {
	*;
}
