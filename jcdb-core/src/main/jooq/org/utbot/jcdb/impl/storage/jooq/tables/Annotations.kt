/**
 *  Copyright 2022 UnitTestBot contributors (utbot.org)
 * <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/*
 * This file is generated by jOOQ.
 */
package org.utbot.jcdb.impl.storage.jooq.tables


import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Name
import org.jooq.Record
import org.jooq.Row8
import org.jooq.Schema
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableOptions
import org.jooq.UniqueKey
import org.jooq.impl.DSL
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl
import org.utbot.jcdb.impl.storage.jooq.DefaultSchema
import org.utbot.jcdb.impl.storage.jooq.keys.FK_ANNOTATIONS_ANNOTATIONS_1
import org.utbot.jcdb.impl.storage.jooq.keys.FK_ANNOTATIONS_CLASSES_1
import org.utbot.jcdb.impl.storage.jooq.keys.FK_ANNOTATIONS_FIELDS_1
import org.utbot.jcdb.impl.storage.jooq.keys.FK_ANNOTATIONS_METHODPARAMETERS_1
import org.utbot.jcdb.impl.storage.jooq.keys.FK_ANNOTATIONS_METHODS_1
import org.utbot.jcdb.impl.storage.jooq.keys.FK_ANNOTATIONS_SYMBOLS_1
import org.utbot.jcdb.impl.storage.jooq.keys.PK_ANNOTATIONS
import org.utbot.jcdb.impl.storage.jooq.tables.records.AnnotationsRecord


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Annotations(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, AnnotationsRecord>?,
    aliased: Table<AnnotationsRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<AnnotationsRecord>(
    alias,
    DefaultSchema.DEFAULT_SCHEMA,
    child,
    path,
    aliased,
    parameters,
    DSL.comment(""),
    TableOptions.table()
) {
    companion object {

        /**
         * The reference instance of <code>Annotations</code>
         */
        val ANNOTATIONS = Annotations()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<AnnotationsRecord> = AnnotationsRecord::class.java

    /**
     * The column <code>Annotations.id</code>.
     */
    val ID: TableField<AnnotationsRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT, this, "")

    /**
     * The column <code>Annotations.annotation_name</code>.
     */
    val ANNOTATION_NAME: TableField<AnnotationsRecord, Long?> = createField(DSL.name("annotation_name"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>Annotations.visible</code>.
     */
    val VISIBLE: TableField<AnnotationsRecord, Boolean?> = createField(DSL.name("visible"), SQLDataType.BOOLEAN.nullable(false), this, "")

    /**
     * The column <code>Annotations.parent_annotation</code>.
     */
    val PARENT_ANNOTATION: TableField<AnnotationsRecord, Long?> = createField(DSL.name("parent_annotation"), SQLDataType.BIGINT, this, "")

    /**
     * The column <code>Annotations.class_id</code>.
     */
    val CLASS_ID: TableField<AnnotationsRecord, Long?> = createField(DSL.name("class_id"), SQLDataType.BIGINT, this, "")

    /**
     * The column <code>Annotations.method_id</code>.
     */
    val METHOD_ID: TableField<AnnotationsRecord, Long?> = createField(DSL.name("method_id"), SQLDataType.BIGINT, this, "")

    /**
     * The column <code>Annotations.field_id</code>.
     */
    val FIELD_ID: TableField<AnnotationsRecord, Long?> = createField(DSL.name("field_id"), SQLDataType.BIGINT, this, "")

    /**
     * The column <code>Annotations.param_id</code>.
     */
    val PARAM_ID: TableField<AnnotationsRecord, Long?> = createField(DSL.name("param_id"), SQLDataType.BIGINT, this, "")

    private constructor(alias: Name, aliased: Table<AnnotationsRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<AnnotationsRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>Annotations</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>Annotations</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>Annotations</code> table reference
     */
    constructor(): this(DSL.name("Annotations"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, AnnotationsRecord>): this(Internal.createPathAlias(child, key), child, key, ANNOTATIONS, null)
    override fun getSchema(): Schema = DefaultSchema.DEFAULT_SCHEMA
    override fun getPrimaryKey(): UniqueKey<AnnotationsRecord> = PK_ANNOTATIONS
    override fun getKeys(): List<UniqueKey<AnnotationsRecord>> = listOf(PK_ANNOTATIONS)
    override fun getReferences(): List<ForeignKey<AnnotationsRecord, *>> = listOf(FK_ANNOTATIONS_SYMBOLS_1, FK_ANNOTATIONS_ANNOTATIONS_1, FK_ANNOTATIONS_CLASSES_1, FK_ANNOTATIONS_METHODS_1, FK_ANNOTATIONS_FIELDS_1, FK_ANNOTATIONS_METHODPARAMETERS_1)

    private lateinit var _symbols: Symbols
    private lateinit var _annotations: Annotations
    private lateinit var _classes: Classes
    private lateinit var _methods: Methods
    private lateinit var _fields_: Fields
    private lateinit var _methodparameters: Methodparameters
    fun symbols(): Symbols {
        if (!this::_symbols.isInitialized)
            _symbols = Symbols(this, FK_ANNOTATIONS_SYMBOLS_1)

        return _symbols;
    }
    fun annotations(): Annotations {
        if (!this::_annotations.isInitialized)
            _annotations = Annotations(this, FK_ANNOTATIONS_ANNOTATIONS_1)

        return _annotations;
    }
    fun classes(): Classes {
        if (!this::_classes.isInitialized)
            _classes = Classes(this, FK_ANNOTATIONS_CLASSES_1)

        return _classes;
    }
    fun methods(): Methods {
        if (!this::_methods.isInitialized)
            _methods = Methods(this, FK_ANNOTATIONS_METHODS_1)

        return _methods;
    }
    fun fields_(): Fields {
        if (!this::_fields_.isInitialized)
            _fields_ = Fields(this, FK_ANNOTATIONS_FIELDS_1)

        return _fields_;
    }
    fun methodparameters(): Methodparameters {
        if (!this::_methodparameters.isInitialized)
            _methodparameters = Methodparameters(this, FK_ANNOTATIONS_METHODPARAMETERS_1)

        return _methodparameters;
    }
    override fun `as`(alias: String): Annotations = Annotations(DSL.name(alias), this)
    override fun `as`(alias: Name): Annotations = Annotations(alias, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Annotations = Annotations(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Annotations = Annotations(name, null)

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row8<Long?, Long?, Boolean?, Long?, Long?, Long?, Long?, Long?> = super.fieldsRow() as Row8<Long?, Long?, Boolean?, Long?, Long?, Long?, Long?, Long?>
}
