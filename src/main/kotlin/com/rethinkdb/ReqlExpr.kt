@file:Suppress("RedundantVisibilityModifier", "EXTENSION_SHADOWED_BY_MEMBER")

package com.rethinkdb

import com.rethinkdb.gen.ast.*

public operator fun ReqlExpr.not(): Not = this.not()

public operator fun ReqlExpr.plus(expr: Any) : Add = add(expr)

public operator fun ReqlExpr.minus(expr: Any): Sub = sub(expr)

public operator fun ReqlExpr.times(expr: Any) : Mul = mul(expr)

public operator fun ReqlExpr.div(expr: Any): Div = div(expr)

public operator fun ReqlExpr.rem(expr: Any) : Mod = mod(expr)

public infix fun ReqlExpr.and(expr: Any): And = And(expr)

public infix fun ReqlExpr.or(expr: Any): Or = Or(expr)

public infix fun ReqlExpr.eq(expr: Any) : Eq = eq(expr)

public infix fun ReqlExpr.ne(expr: Any) : Ne = ne(expr)

public infix fun ReqlExpr.gt(expr: Any) : Gt = gt(expr)

public infix fun ReqlExpr.lt(expr: Any) : Lt = lt(expr)

public infix fun ReqlExpr.ge(expr: Any) : Ge = ge(expr)

public infix fun ReqlExpr.le(expr: Any) : Le = le(expr)

public infix fun ReqlExpr.bitAnd(expr: Any): BitAnd = bitAnd(expr)

public infix fun ReqlExpr.bitOr(expr: Any): BitOr = bitOr(expr)

public infix fun ReqlExpr.bitXor(expr: Any): BitXor = bitXor(expr)

public infix fun ReqlExpr.bitSal(expr: Any): BitSal = bitSal(expr)

public infix fun ReqlExpr.bitSar(expr: Any): BitSar = bitSar(expr)
