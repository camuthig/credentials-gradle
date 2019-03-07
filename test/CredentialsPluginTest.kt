package org.camuthig.credentials.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.*

class CredentialsPluginTest {
    lateinit var gradleBuild: File

    @Rule
    @JvmField
    var testProjectDir = TemporaryFolder()

    @Before
    fun setup() {
        gradleBuild = testProjectDir.newFile("build.gradle.kts")
        testProjectDir.newFolder("resources")
        gradleBuild.appendText("plugins { id(\"org.camuthig.credentials\") }\n")
    }

    private fun run(args: List<String>): BuildResult {
        val runner = GradleRunner.create()
            .withArguments(args)
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()

        return runner.build()
    }

    @Test
    fun `it should show a decrypted file`() {
        testProjectDir
            .newFile("resources/credentials.conf.enc")
            .writeText("uYjsLAfBEDzvZpVNfEC1H8801R+N3bDpFsyY4XRWkVE=")
        testProjectDir
            .newFile("resources/master.key")
            .writeText("thisisnotarealmasterkeybutitwork1234567890123456")

        val result = run(listOf("credentialsShow"))

        val expected = """
            credentials {
                one=one
            }
        """.trimIndent()

        assert(result.output.contains(expected))
    }

    @Test
    fun `it should add a new key value`() {
        testProjectDir
            .newFile("resources/credentials.conf.enc")
            .writeText("")
        testProjectDir
            .newFile("resources/master.key")
            .writeText("thisisnotarealmasterkeybutitwork1234567890123456")

        run(listOf("credentialsUpsert", "--key=upsert", "--value=test"))

        val results = run(listOf("credentialsShow"))

        val expected = """
            upsert=test
        """.trimIndent()

        assert(results.output.contains(expected))

        val expectedFileContents = "CTgk004yZPB0u+DndDI28g==".toByteArray()
        val encryptedFile = File(testProjectDir.root.absolutePath + "/resources/credentials.conf.enc")
        assert(encryptedFile.readBytes().contentEquals(expectedFileContents))
    }

    @Test
    fun `it should remove a key from the credentials file`() {
        testProjectDir
            .newFile("resources/credentials.conf.enc")
            .writeText("2EcJrYw7A/4nlp+uHxc3dAu2yS2aeFc9TObP2K0SV68=")
        testProjectDir
            .newFile("resources/master.key")
            .writeText("thisisnotarealmasterkeybutitwork1234567890123456")

        run(listOf("credentialsDelete", "--key=one"))

        val results = run(listOf("credentialsShow"))

        val expected = """
            two=test
        """.trimIndent()

        assert(results.output.contains(expected))

        val expectedFileContents = "hRhLeA70XifLUA1wn15+cA==".toByteArray()
        val encryptedFile = File(testProjectDir.root.absolutePath + "/resources/credentials.conf.enc")
        assert(encryptedFile.readBytes().contentEquals(expectedFileContents))
    }

    @Test
    fun `it should generate a file and new key`() {
        run(listOf("credentialsGenerate"))

        assert(File(testProjectDir.root.absolutePath + "/resources/credentials.conf.enc").exists())
        assert(File(testProjectDir.root.absolutePath + "/resources/master.key").exists())

        run(listOf("credentialsUpsert", "--key=upsert", "--value=test"))

        val results = run(listOf("credentialsShow"))

        val expected = """
            upsert=test
        """.trimIndent()

        assert(results.output.contains(expected))
    }

    @Test
    fun `it should generate a new key and reencrypt the credentials`() {
        val existingFile = "uYjsLAfBEDzvZpVNfEC1H8801R+N3bDpFsyY4XRWkVE="
        val existingKey = "thisisnotarealmasterkeybutitwork1234567890123456"
        testProjectDir
            .newFile("resources/credentials.conf.enc")
            .writeText(existingFile)
        testProjectDir
            .newFile("resources/master.key")
            .writeText(existingKey)

        run(listOf("credentialsRekey"))

        val result = run(listOf("credentialsShow"))

        val expected = """
            credentials {
                one=one
            }
        """.trimIndent()

        assert(result.output.contains(expected))

        val encryptedFile = File(testProjectDir.root.absolutePath + "/resources/credentials.conf.enc")
        val newKey = File(testProjectDir.root.absolutePath + "/resources/master.key")
        assertFalse(encryptedFile.readBytes().contentEquals(existingFile.toByteArray()))
        assertFalse(newKey.readBytes().contentEquals(existingKey.toByteArray()))
    }

    @Test
    fun `it should allow configuring the location of the file and key`() {
        testProjectDir
            .newFile("resources/configured.conf.enc")
            .writeText("uYjsLAfBEDzvZpVNfEC1H8801R+N3bDpFsyY4XRWkVE=")
        testProjectDir
            .newFile("resources/configured.key")
            .writeText("thisisnotarealmasterkeybutitwork1234567890123456")

        gradleBuild.appendText("""
            credentials {
                credentialsFile = file("resources/configured.conf.enc")
                masterKeyFile = file("resources/configured.key")
            }
        """.trimIndent())

        val result = run(listOf("credentialsShow"))

        val expected = """
            credentials {
                one=one
            }
        """.trimIndent()

        assert(result.output.contains(expected))
    }
}