package de.ph1b.audiobook.ndkGen

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI

@Suppress("unused")
open class PrepareOpus : DefaultTask() {

  var ndkDir: String = ""

  @TaskAction
  fun prepare() {
    logger.error("###")
    logger.error(ndkDir)
    if (!File(ndkDir).exists())
      throw IllegalArgumentException("Invalid ndkDir $ndkDir")

    val jniDir = File(project.projectDir, "src/main/jni")

    // if the opus sources already exist, skip
    val opusDir = File(jniDir, "libopus")

    d(opusDir.absolutePath)
    if (opusDir.listFilesSafely().isNotEmpty())
      return
    opusDir.delete()

    val opusVersion = "1.1.4"

    val dstFile = File(jniDir, "opus-$opusVersion.tar.gz")
    if (!dstFile.exists()) {
      val uri = URI.create("http://downloads.xiph.org/releases/opus/opus-$opusVersion.tar.gz")
      download(uri, dstFile)
    }
    d("downloaded to $dstFile")

    // extract and rename it
    execute(
        command = "tar -xzf \"${dstFile.absolutePath}\" -C \"${dstFile.parentFile.absolutePath}\""
    )
    val extractedFolder = File(jniDir, "opus-$opusVersion")
    extractedFolder.renameTo(opusDir)

    execute(
        command = "./convert_android_asm.sh",
        directory = jniDir
    )

    execute(
        command = "$ndkDir/ndk-build APP_ABI=\"mips64 mips x86_64 x86 arm64-v8a armeabi-v7a\" -j4",
        directory = jniDir
    )
  }
}
