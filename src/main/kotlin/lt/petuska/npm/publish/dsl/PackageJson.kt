package lt.petuska.npm.publish.dsl

import com.google.gson.GsonBuilder
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import java.io.File
import java.io.Serializable

/**
 * Utility class for building Json Trees
 */
open class JsonObject<T> : MutableMap<String, T?> by mutableMapOf(), Serializable {

  /**
   * Creates a Json Object
   */
  fun jsonObject(block: JsonObject<Any>.() -> Unit) = JsonObject(block)

  /**
   * Creates a Json Array
   */
  fun <V> jsonArray(vararg elements: V) = mutableListOf(*elements)

  /**
   * Assigns a json value as `[this] = [value]`
   */
  infix fun String.to(value: T?) {
    this@JsonObject[this] = value
  }

  /**
   * Stringifies the current state of the object into a Json string
   */
  override fun toString(): String = gson.toJson(this)

  /**
   * Writes the current state of the object into a file as json string
   */
  fun writeTo(packageJsonFile: File) {
    packageJsonFile.ensureParentDirsCreated()
    packageJsonFile.writer().use {
      gson.toJson(this, it)
    }
  }

  companion object {
    private val gson = GsonBuilder()
      .setPrettyPrinting()
      .create()

    /**
     * Creates a Json Object
     */
    operator fun <V> invoke(block: JsonObject<V>.() -> Unit) = JsonObject<V>().apply(block)

    /**
     * Creates a Json Array
     */
    operator fun <V> invoke(vararg elements: V) = mutableListOf(*elements)
  }
}

/**
 * A class representing [package.json](https://docs.npmjs.com/files/package.json) schema. Custom fields can be added as regular map entries.
 */
class PackageJson(name: String, version: String, scope: String? = null, config: PackageJson.() -> Unit = {}) : JsonObject<Any>() {
  /**
   * [name](https://docs.npmjs.com/files/package.json#name)
   */
  var name: String by this

  /**
   * [version](https://docs.npmjs.com/files/package.json#version)
   */
  var version: String by this

  /**
   * [description](https://docs.npmjs.com/files/package.json#description-1)
   */
  var description: String? by this

  /**
   * [keywords](https://docs.npmjs.com/files/package.json#keywords)
   */
  var keywords: MutableList<String>? by this

  /**
   * [homepage](https://docs.npmjs.com/files/package.json#homepage)
   */
  var homepage: String? by this

  /**
   * [bugs](https://docs.npmjs.com/files/package.json#bugs)
   */
  var bugs: Bugs? by this

  /**
   * [bugs](https://docs.npmjs.com/files/package.json#bugs)
   */
  fun bugs(config: Bugs.() -> Unit = {}) = Bugs(config).also { bugs = it }

  /**
   * [licence](https://docs.npmjs.com/files/package.json#license)
   */
  var licence: String? by this

  /**
   * [author](https://docs.npmjs.com/files/package.json#people-fields-author-contributors)
   */
  var author: Person? by this

  /**
   * [author](https://docs.npmjs.com/files/package.json#people-fields-author-contributors)
   */
  fun author(config: Person.() -> Unit = {}) = Person(config).also { author = it }

  /**
   * [contributors](https://docs.npmjs.com/files/package.json#people-fields-author-contributors)
   */
  var contributors: MutableList<Person>? by this

  /**
   * [contributors](https://docs.npmjs.com/files/package.json#people-fields-author-contributors)
   */
  fun MutableList<Person>.contributor(config: Person.() -> Unit = {}) = Person(config).also { add(it) }

  /**
   * [files](https://docs.npmjs.com/files/package.json#files)
   */
  var files: MutableList<String>? by this

  /**
   * [main](https://docs.npmjs.com/files/package.json#main)
   */
  var main: String? by this

  /**
   * [browser](https://docs.npmjs.com/files/package.json#browser)
   */
  var browser: String? by this

  /**
   * [bin](https://docs.npmjs.com/files/package.json#bin)
   */
  var bin: JsonObject<String>? by this

  /**
   * [man](https://docs.npmjs.com/files/package.json#man)
   */
  var man: MutableList<String>? by this

  /**
   * [directories](https://docs.npmjs.com/files/package.json#directories)
   */
  var directories: Directories? by this

  /**
   * [directories](https://docs.npmjs.com/files/package.json#directories)
   */
  fun directories(config: Directories.() -> Unit = {}) = Directories(config).also { directories = it }

  /**
   * [repository](https://docs.npmjs.com/files/package.json#repository)
   */
  var repository: Repository? by this

  /**
   * [repository](https://docs.npmjs.com/files/package.json#repository)
   */
  fun repository(config: Repository.() -> Unit = {}) = Repository(config).also { repository = it }

  /**
   * [scripts](https://docs.npmjs.com/files/package.json#scripts)
   */
  var scripts: JsonObject<String>? by this

  /**
   * [config](https://docs.npmjs.com/files/package.json#config)
   */
  var config: JsonObject<Any>? by this

  /**
   * [dependencies](https://docs.npmjs.com/files/package.json#dependencies)
   */
  var dependencies: JsonObject<String>? by this

  /**
   * [devDependencies](https://docs.npmjs.com/files/package.json#devdependencies)
   */
  var devDependencies: JsonObject<String>? by this

  /**
   * [peerDependencies](https://docs.npmjs.com/files/package.json#peerdependencies)
   */
  var peerDependencies: JsonObject<String>? by this

  /**
   * [bundledDependencies](https://docs.npmjs.com/files/package.json#bundleddependencies)
   */
  var bundledDependencies: JsonObject<String>? by this

  /**
   * [optionalDependencies](https://docs.npmjs.com/files/package.json#optionaldependencies)
   */
  var optionalDependencies: JsonObject<String>? by this

  /**
   * [engines](https://docs.npmjs.com/files/package.json#engines)
   */
  var engines: JsonObject<String>? by this

  /**
   * [os](https://docs.npmjs.com/files/package.json#os)
   */
  var os: MutableList<String>? by this

  /**
   * [cpu](https://docs.npmjs.com/files/package.json#cpu)
   */
  var cpu: MutableList<String>? by this

  /**
   * [private](https://docs.npmjs.com/files/package.json#private)
   */
  var private: Boolean? by this

  /**
   * [publishConfig](https://docs.npmjs.com/files/package.json#publishconfig)
   */
  var publishConfig: PublishConfig? by this

  /**
   * [publishConfig](https://docs.npmjs.com/files/package.json#publishconfig)
   */
  fun publishConfig(config: PublishConfig.() -> Unit = {}) = PublishConfig(config).also { publishConfig = it }

  init {
    this.name = "${scope?.let { "@$it/" } ?: ""}$name"
    this.version = version
    this.apply(config)
  }

  /**
   * [bugs](https://docs.npmjs.com/files/package.json#bugs)
   */
  inner class Bugs(config: Bugs.() -> Unit = {}) : JsonObject<Any>() {
    var url: String? by this
    var email: String? by this

    init {
      config()
    }
  }

  /**
   * [people field](https://docs.npmjs.com/files/package.json#people-fields-author-contributors)
   */
  inner class Person(config: Person.() -> Unit = {}) : JsonObject<Any>() {
    var name: String? by this
    var email: String? by this
    var url: String? by this

    init {
      config()
    }
  }

  /**
   * [directories](https://docs.npmjs.com/files/package.json#directories)
   */
  inner class Directories(config: Directories.() -> Unit = {}) : JsonObject<Any>() {
    /**
     * [lib](https://docs.npmjs.com/files/package.json#directorieslib)
     */
    var lib: String? by this

    /**
     * [bin](https://docs.npmjs.com/files/package.json#directoriesbin)
     */
    var bin: String? by this

    /**
     * [man](https://docs.npmjs.com/files/package.json#directoriesman)
     */
    var man: String? by this

    /**
     * [doc](https://docs.npmjs.com/files/package.json#directoriesdoc)
     */
    var doc: String? by this

    /**
     * [example](https://docs.npmjs.com/files/package.json#directoriesexample)
     */
    var example: String? by this

    /**
     * [test](https://docs.npmjs.com/files/package.json#directoriestest)
     */
    var test: String? by this

    init {
      config()
    }
  }

  /**
   * [repository](https://docs.npmjs.com/files/package.json#repository)
   */
  inner class Repository(config: Repository.() -> Unit = {}) : JsonObject<Any>() {
    var type: String? by this
    var url: String? by this
    var directory: String? by this

    init {
      config()
    }
  }

  /**
   * [publish config](https://docs.npmjs.com/files/package.json#publishconfig)
   */
  inner class PublishConfig(config: PublishConfig.() -> Unit = {}) : JsonObject<Any>() {
    var registry: String? by this
    var access: String? by this
    var tag: String? by this

    init {
      config()
    }
  }
}