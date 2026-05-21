package es.sebas1705.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ProductFlavor
import java.io.File
import java.util.Properties

/**
 * Enum class that represents the different dimensions of the flavors.
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
enum class FlavorDimension {
    CONTENT_TYPE
}

/**
 * Enum class that represents the different flavors of the application.
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
enum class CoreFlavor(val dimension: FlavorDimension, val applicationIdSuffix : String? = null) {
    Dev(FlavorDimension.CONTENT_TYPE, ".dev"),
    Pro(FlavorDimension.CONTENT_TYPE),
}

/**
 * Method that configures the flavors of each module and centralizes all the configuration in one place.
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
fun configureFlavors(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    flavorConfigurationBlock : ProductFlavor.(flavor : CoreFlavor) -> Unit = {}
) {
    val secretsPropertiesFile = File("./app/secrets.properties")
    val secretsProperties = Properties()

    if (secretsPropertiesFile.exists()) {
        secretsPropertiesFile.inputStream().use { secretsProperties.load(it) }
    }

    commonExtension.apply {
        flavorDimensions.add(FlavorDimension.CONTENT_TYPE.name)
        CoreFlavor.values().forEach { coreFlavor ->
            productFlavors.create(coreFlavor.name) {
                dimension = coreFlavor.dimension.name
                flavorConfigurationBlock(coreFlavor)
                if (this@apply is ApplicationExtension && this is ApplicationProductFlavor) {
                    if (coreFlavor.applicationIdSuffix != null) {
                        applicationIdSuffix = coreFlavor.applicationIdSuffix
                    }
                }
                buildConfigField(
                    "String",
                    "EXAMPLE_URL",
                    "\"${secretsProperties["EXAMPLE_URL_${coreFlavor.name.uppercase()}"] ?: ""}\""
                )
            }
        }
    }
}