package com.github.lucasls.weddingapp.backend.main.helper.datastore

import com.google.cloud.datastore.*

fun Datastore.newKeyFactory(kind: String? = null, builder: KeyFactory.() -> Unit = {}): KeyFactory {
    return newKeyFactory()
        .apply(builder)
        .apply { kind?.let { setKind(it) } }
}

fun Datastore.put(keyFactory: KeyFactory, keyValue: String, builder: Entity.Builder.() -> Unit) {
    put(keyFactory.newEntity(keyValue, builder))
}

fun Datastore.run(gqlQuery: String, builder: GqlQuery.Builder<Entity>.() -> Unit): QueryResults<Entity> {
    return run(newGqlQuery(gqlQuery, builder))
}

fun Datastore.queryOne(gqlQuery: String, builder: GqlQuery.Builder<Entity>.() -> Unit): Entity? {
    return run(gqlQuery, builder).let {
        if (it.hasNext()) {
            it.next()
        } else {
            null
        }
    }
}

fun Datastore.query(gqlQuery: String, builder: GqlQuery.Builder<Entity>.() -> Unit): Iterable<Entity> {
    return run(gqlQuery, builder)
        .let { it.asSequence().asIterable() }
}

fun Datastore.get(keyFactory: KeyFactory, keyValue: String): Entity {
    return get(keyFactory.newKey(keyValue))
}

fun KeyFactory.newEntity(keyValue: String, builder: Entity.Builder.() -> Unit): Entity {
    return newKey(keyValue).newEntity(builder)
}

fun Entity.Builder.setValue(name: String, value: String?) = value?.let { set(name, it) }

fun Entity.Builder.setValue(name: String, value: List<String>) = value
    .map { StringValue.of(it) }
    .let { set("name", it) }

fun Entity.getStringOrNull(key: String): String? =
    Unit.takeIf { contains(key) }
        ?.let { getString(key) }

fun Entity.getBooleanOrNull(key: String): Boolean? =
    Unit.takeIf { contains(key) }
        ?.let { getBoolean(key) }

fun Key.newEntity(builder: Entity.Builder.() -> Unit): Entity {
    return Entity.newBuilder(this).apply(builder).build()
}

fun <V> newGqlQuery(resultType: Query.ResultType<V>, gql: String, builder: GqlQuery.Builder<V>.() -> Unit): GqlQuery<V> {
    return Query.newGqlQueryBuilder(resultType, gql).apply(builder).build()
}

fun newGqlQuery(gql: String, builder: GqlQuery.Builder<Entity>.() -> Unit): GqlQuery<Entity> {
    return newGqlQuery(Query.ResultType.ENTITY, gql, builder)
}