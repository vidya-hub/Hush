import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.security.MessageDigest

// The ffmpeg-kit AAR is not committed to the repository. It is fetched from the
// project's GitHub release and pinned with a SHA-256 checksum, so the tree stays
// free of binary blobs while builds remain reproducible from a clean checkout.
abstract class EnsureFfmpegKitTask : DefaultTask() {
    @get:Input abstract val sha256: org.gradle.api.provider.Property<String>
    @get:Input abstract val downloadUrl: org.gradle.api.provider.Property<String>
    @get:OutputFile abstract val aar: org.gradle.api.file.RegularFileProperty

    private fun sha256Of(file: File): String =
        MessageDigest.getInstance("SHA-256").digest(file.readBytes())
            .joinToString("") { "%02x".format(it) }

    @TaskAction
    fun run() {
        val target = aar.get().asFile
        if (target.exists() && sha256Of(target) == sha256.get()) {
            return
        }
        val partial = File(target.parentFile, "ffmpegKitDownload.part")
        println("Downloading ffmpeg-kit AAR from ${downloadUrl.get()}")
        java.net.URI(downloadUrl.get()).toURL().openStream().use { input ->
            partial.outputStream().use { input.copyTo(it) }
        }
        val actual = sha256Of(partial)
        if (actual != sha256.get()) {
            partial.delete()
            throw GradleException(
                "ffmpeg-kit.aar checksum mismatch: expected ${sha256.get()}, got $actual")
        }
        if (target.exists()) {
            target.delete()
        }
        if (!partial.renameTo(target)) {
            throw GradleException("Could not move downloaded AAR into place")
        }
    }
}

val ensureFfmpegKit = tasks.register<EnsureFfmpegKitTask>("ensureFfmpegKit") {
    sha256.set("19d064dd952a51dd3015007007bf7740c66ce65c09812abbe573a3ec6858f968")
    downloadUrl.set("https://github.com/vidya-hub/Hush/releases/download/v1.0.1/ffmpeg-kit.aar")
    aar.set(layout.projectDirectory.file("ffmpeg-kit.aar"))
}

configurations.maybeCreate("default")
artifacts.add("default", file("ffmpeg-kit.aar")) {
    builtBy(ensureFfmpegKit)
}
