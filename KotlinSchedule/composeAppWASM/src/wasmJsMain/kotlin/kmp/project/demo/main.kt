package kmp.project.demo

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.fetch.Response
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator


/**
 * 当前版本wasm暂不支持中文字体的显示，因此使用字体文件加载中文字体
 *
 * 字体文件存放在resources目录下，可免费商用
 *
 * 第三方字体加载方式参考JetBrains工程师提供的解决方案，若未来wasm支持中文字体，可直接使用系统字体
 */
private const val MiSans = "./MiSans-Normal.ttf"
lateinit var miFontFamily: FontFamily

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val fontFamilyResolver = LocalFontFamilyResolver.current
        val fontLodaded = remember { mutableStateOf(false) }
        if (fontLodaded.value) {
            App()
        }
        LaunchedEffect(Unit) {
            val miSansBytes = loadRes(MiSans).toByteArray()
            val fontFamily = FontFamily(listOf(Font("MiSans", miSansBytes)))
            fontFamilyResolver.preload(fontFamily)
            miFontFamily = fontFamily
            fontLodaded.value = true
        }
    }
}

suspend fun loadRes(url: String): ArrayBuffer {
    return window.fetch(url).await<Response>().arrayBuffer().await()
}

fun ArrayBuffer.toByteArray(): ByteArray {
    val source = Int8Array(this, 0, byteLength)
    return jsInt8ArrayToKotlinByteArray(source)
}

@JsFun(
    """ (src, size, dstAddr) => {
        const mem8 = new Int8Array(wasmExports.memory.buffer, dstAddr, size);
        mem8.set(src);
    }
"""
)
external fun jsExportInt8ArrayToWasm(src: Int8Array, size: Int, dstAddr: Int)

internal fun jsInt8ArrayToKotlinByteArray(x: Int8Array): ByteArray {
    val size = x.length

    @OptIn(UnsafeWasmMemoryApi::class)
    return withScopedMemoryAllocator { allocator ->
        val memBuffer = allocator.allocate(size)
        val dstAddress = memBuffer.address.toInt()
        jsExportInt8ArrayToWasm(x, size, dstAddress)
        ByteArray(size) { i -> (memBuffer + i).loadByte() }
    }
}

actual fun getSystemFontFamily(): FontFamily {
    return miFontFamily
}


//actual fun name(): String = "Web"