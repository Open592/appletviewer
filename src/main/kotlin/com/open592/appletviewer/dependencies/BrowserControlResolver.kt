package com.open592.appletviewer.dependencies

import com.open592.appletviewer.config.ApplicationConfiguration
import com.open592.appletviewer.environment.Architecture
import com.open592.appletviewer.environment.Environment
import com.open592.appletviewer.environment.OperatingSystem
import com.open592.appletviewer.jar.SignedJarFileResolver
import com.open592.appletviewer.paths.ApplicationPaths
import jakarta.inject.Inject
import jakarta.inject.Singleton
import okio.Buffer

@Singleton
public class BrowserControlResolver @Inject constructor(
    private val applicationPaths: ApplicationPaths,
    private val configuration: ApplicationConfiguration,
    private val dependencyFetcher: RemoteDependencyFetcher,
    private val signedJarFileResolver: SignedJarFileResolver,
    environment: Environment,
) : RemoteDependencyResolver(
    configuration = configuration,
    type = DependencyType.BROWSERCONTROL,
) {
    /**
     * The extension of the resulting library file on the filesystem.
     *
     * NOTE: We store this as a separate value to allow for resolving the
     * library file from the jar entries without necessarily knowing the full
     * entry name. This is due to separate naming conventions between Jagex and
     * Open592's remote library files.
     *
     * @example `dll`
     */
    private val fileExtension: FileExtension

    /**
     * Identifies the operating system within the config key.
     *
     * @example `win`
     */
    private val os: OperatingSystemIdentifier

    /**
     * Identifies the architecture within the config key.
     *
     * @example `amd64`
     */
    private val arch: ArchitectureIdentifier

    /**
     * Is the architecture 64 bits or not?
     *
     * We use this value to resolve a couple different config keys, as well
     * as the resulting library filename which is saved to the filesystem.
     */
    private val is64Bit: Boolean

    /**
     * Reference to the native library file
     */
    private lateinit var libraryFile: Buffer

    init {
        val architecture = environment.getArchitecture()
        val operatingSystem = environment.getOperatingSystem()

        arch = ArchitectureIdentifier.resolve(architecture)
        fileExtension = FileExtension.resolve(operatingSystem)
        os = OperatingSystemIdentifier.resolve(operatingSystem)
        is64Bit = architecture == Architecture.X86_64
    }

    /**
     * Resolving the browsercontrol library is performed in 3 parts:
     *
     * 1. The jar archive is downloaded from the server and stored in memory.
     *      - The file is platform dependent and is specified within the jav_config.
     *      - The progress loaded is updated with the progress of the download.
     * 2. The jar archive is validated and the library is extracted.
     * 3. The library file bytes are written to disk. The location is platform dependent.
     */
    public override fun resolve() {
        val libraryFilename = getLibraryFilename()

        val jarFile = dependencyFetcher.fetchRemoteDependency(type, getUrl(getRemoteFileConfigKey()))
            ?: throw ResolveException(configuration.getContent("err_load_bc"))

        val jarEntries = signedJarFileResolver.resolveEntries(jarFile)

        libraryFile = jarEntries.getEntryByFileExtension(fileExtension.str)
            ?: throw ResolveException(configuration.getContent("err_verify_${if (is64Bit) "bc64" else "bc"}"))

        applicationPaths.saveCacheFile(libraryFilename, libraryFile)
    }

    /**
     * Get the filename where the library file will be saved on the user's filesystem.
     *
     * @example `browsercontrol64.dll`
     */
    private fun getLibraryFilename(): String {
        val name = if (is64Bit) "browsercontrol64" else "browsercontrol"

        return "$name.${fileExtension.str}"
    }

    /**
     * The configuration key used to resolve the remote browsercontrol file.
     */
    private fun getRemoteFileConfigKey(): String {
        return "browsercontrol_${os.identifier}_${arch.identifier}_jar"
    }

    private enum class ArchitectureIdentifier(val identifier: String) {
        X86("x86"),
        X86_64("amd64"),
        ;

        companion object {
            fun resolve(architecture: Architecture): ArchitectureIdentifier {
                return when (architecture) {
                    Architecture.X86 -> X86
                    Architecture.X86_64 -> X86_64
                }
            }
        }
    }

    private enum class OperatingSystemIdentifier(val identifier: String) {
        WINDOWS("win"),
        LINUX("linux"),
        ;

        companion object {
            fun resolve(operatingSystem: OperatingSystem): OperatingSystemIdentifier {
                return when (operatingSystem) {
                    OperatingSystem.WINDOWS -> WINDOWS
                    OperatingSystem.LINUX -> LINUX
                }
            }
        }
    }

    private enum class FileExtension(val str: String) {
        DLL("dll"),
        SO("so"),
        ;

        companion object {
            fun resolve(operatingSystem: OperatingSystem): FileExtension {
                return when (operatingSystem) {
                    OperatingSystem.WINDOWS -> DLL
                    OperatingSystem.LINUX -> SO
                }
            }
        }
    }
}
