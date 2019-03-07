package org.camuthig.credentials.gradle

import com.typesafe.config.ConfigRenderOptions
import org.camuthig.credentials.core.CredentialsStore
import org.camuthig.credentials.core.FileCredentialsStore
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

open class CredentialsPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("credentials", CredentialsPluginExtension::class.java)

        val credentialsTasks = listOf(
            Pair("credentialsUpsert", UpsertTask::class.java),
            Pair("credentialsShow", ShowTask::class.java),
            Pair("credentialsDelete", DeleteTask::class.java),
            Pair("credentialsGenerate", GenerateTask::class.java),
            Pair("credentialsRekey", RekeyTask::class.java)
        )

        credentialsTasks.forEach {
            project.tasks.register(it.first, it.second) {task ->
                task.group = "Credentials"
            }
        }
    }
}

open class CredentialsPluginExtension {
    var credentialsFile: File? = null
    var masterKeyFile: File? = null
}

fun getStore(project: Project): CredentialsStore {
    val extension = project.extensions.getByType(CredentialsPluginExtension::class.javaObjectType)
    return FileCredentialsStore(
        extension.credentialsFile ?: File(project.projectDir.absolutePath + "/resources/credentials.conf.enc"),
        extension.masterKeyFile ?: File(project.projectDir.absolutePath + "/resources/master.key")
    )
}

/**
 * A Gradle task to decrypt and write the credentials file to the console
 */
open class ShowTask: DefaultTask() {
    init {
        description = "Output the existing credentials configuration"
    }

    @TaskAction
    fun apply() {
        println(getStore(project)
            .load()
            .root()
            .render(ConfigRenderOptions.defaults().setJson(false).setOriginComments(false))
        )
    }
}

/**
 * A Gradle task to update the value of a specific key in the encrypted configuration file
 */
open class UpsertTask: DefaultTask() {
    init {
        description = "Add or update a credential"
    }

    @Input
    @Option(description = "The key to create or update in the configuration file")
    lateinit var key: String

    @Input
    @Option(description = "The value to set the key to")
    lateinit var value: String

    @TaskAction
    fun apply() {
        getStore(project).upsert(key, value)
    }
}

/**
 * A Gradle task to remove a credentials from the store.
 */
open class DeleteTask: DefaultTask() {
    init {
        description = "Remove a credentials from the file"
    }

    @Input
    @Option(description = "The key of the credentials to remove in the configuration file")
    lateinit var key: String

    @TaskAction
    fun apply() {
        getStore(project).delete(key)
    }
}

open class GenerateTask: DefaultTask() {
    init {
        description = "Generate a new credentials file and master key"
    }

    @TaskAction
    fun apply() {
        getStore(project).generate()
    }
}

open class RekeyTask: DefaultTask() {
    init {
        description = "Generate a new key and reencrypt the credentials file"
    }

    @TaskAction
    fun apply() {
        getStore(project).rekey()
    }
}
