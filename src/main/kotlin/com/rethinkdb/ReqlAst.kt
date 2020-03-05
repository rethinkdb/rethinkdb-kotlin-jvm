@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER", "RedundantVisibilityModifier")

package com.rethinkdb

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.rethinkdb.ast.ReqlAst
import com.rethinkdb.gen.ast.*
import com.rethinkdb.model.Arguments
import com.rethinkdb.model.OptArgs
import com.rethinkdb.net.Connection
import com.rethinkdb.net.Result
import java.util.concurrent.CompletableFuture

public inline fun <reified T> ReqlAst.run(conn: Connection, optArgs: OptArgs = OptArgs(), fetchMode: Result.FetchMode? = null): Result<T> {
    return this.run(conn, optArgs, fetchMode, jacksonTypeRef<T>())
}

public inline fun <reified T> ReqlAst.runAsync(conn: Connection, optArgs: OptArgs = OptArgs(), fetchMode: Result.FetchMode? = null): CompletableFuture<Result<T>> {
    return this.runAsync(conn, optArgs, fetchMode, jacksonTypeRef<T>())
}
