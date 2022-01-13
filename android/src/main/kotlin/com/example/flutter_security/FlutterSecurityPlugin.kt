package com.example.flutter_security

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest
import android.util.Log
import com.scottyab.rootbeer.RootBeer
import android.os.Debug

/** FlutterSecurityPlugin */
@Suppress("DEPRECATION")
class FlutterSecurityPlugin : FlutterPlugin, MethodCallHandler, FlutterActivity() {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    @get:JvmName("getAdapterContext")
    private lateinit var context: Context

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_security")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "amITampered") {
            var sha1 = call.argument<String>("sha1")
            var signatureList = getApplicationSignature(context.packageName, result)
            if (signatureList.contains(sha1)) {
                result.success("notTampered")
            } else {
                result.success("tampered")
            }
        } else if (call.method == "amIJailBroken") {
            if(RootBeer(context).isRooted()) {
                result.success("jailBroken")
            } else {
                result.success("notJailBroken")
            }
        } else if (call.method == "amIDebuggable") {
            if(isDebuggable(context: context) || detectDebugger()) {
                result.success("debbugable")
            } else {
                result.success("notDebuggable")
            }
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    @SuppressLint("PackageManagerGetSignatures")
    fun getApplicationSignature(packageName: String, @NonNull result: Result): List<String> {
        val signatureList: List<String>
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // New signature
                val sig = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
                signatureList = if (sig.hasMultipleSigners()) {
                    // Send all with apkContentsSigners
                    sig.apkContentsSigners.map {
                        val digest = MessageDigest.getInstance("SHA")
                        digest.update(it.toByteArray())
                        bytesToHex(digest.digest())
                    }
                } else {
                    // Send one with signingCertificateHistory
                    sig.signingCertificateHistory.map {
                        val digest = MessageDigest.getInstance("SHA")
                        digest.update(it.toByteArray())
                        bytesToHex(digest.digest())
                    }
                }
            } else {

                val sig = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
                signatureList = sig.map {
                    val digest = MessageDigest.getInstance("SHA")
                    digest.update(it.toByteArray())
                    bytesToHex(digest.digest())
                }
            }

            return signatureList
        } catch (e: Exception) {
            // Handle error
            Log.d("error", e.toString())
            result.success("genericError")
        }
        return emptyList()
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
        val hexChars = CharArray(bytes.size * 2)
        var v: Int
        for (j in bytes.indices) {
            v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v.ushr(4)]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    // is the app debuggable ?
    // https://github.com/OWASP/owasp-mstg/blob/master/Document/0x05j-Testing-Resiliency-Against-Reverse-Engineering.md#checking-the-debuggable-flag-in-applicationinfo
    fun isDebuggable(context: Context): Boolean {
        return context.getApplicationContext().getApplicationInfo().flags and ApplicationInfo.FLAG_DEBUGGABLE !== 0
    }

    // Is the debugger connected ?
    // https://github.com/OWASP/owasp-mstg/blob/master/Document/0x05j-Testing-Resiliency-Against-Reverse-Engineering.md#isdebuggerconnected
    fun detectDebugger(): Boolean {
        return Debug.isDebuggerConnected()
    }
}
