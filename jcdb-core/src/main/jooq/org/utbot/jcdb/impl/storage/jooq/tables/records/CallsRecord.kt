/*
 * This file is generated by jOOQ.
 */
package org.utbot.jcdb.impl.storage.jooq.tables.records


import org.jooq.Field
import org.jooq.Record7
import org.jooq.Row7
import org.jooq.impl.TableRecordImpl
import org.utbot.jcdb.impl.storage.jooq.tables.Calls


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class CallsRecord() : TableRecordImpl<CallsRecord>(Calls.CALLS), Record7<Long?, Long?, Long?, Int?, Long?, ByteArray?, Long?> {

    var calleeClassSymbolId: Long?
        set(value) = set(0, value)
        get() = get(0) as Long?

    var calleeNameSymbolId: Long?
        set(value) = set(1, value)
        get() = get(1) as Long?

    var calleeDescHash: Long?
        set(value) = set(2, value)
        get() = get(2) as Long?

    var opcode: Int?
        set(value) = set(3, value)
        get() = get(3) as Int?

    var callerClassSymbolId: Long?
        set(value) = set(4, value)
        get() = get(4) as Long?

    var callerMethodOffsets: ByteArray?
        set(value) = set(5, value)
        get() = get(5) as ByteArray?

    var locationId: Long?
        set(value) = set(6, value)
        get() = get(6) as Long?

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row7<Long?, Long?, Long?, Int?, Long?, ByteArray?, Long?> = super.fieldsRow() as Row7<Long?, Long?, Long?, Int?, Long?, ByteArray?, Long?>
    override fun valuesRow(): Row7<Long?, Long?, Long?, Int?, Long?, ByteArray?, Long?> = super.valuesRow() as Row7<Long?, Long?, Long?, Int?, Long?, ByteArray?, Long?>
    override fun field1(): Field<Long?> = Calls.CALLS.CALLEE_CLASS_SYMBOL_ID
    override fun field2(): Field<Long?> = Calls.CALLS.CALLEE_NAME_SYMBOL_ID
    override fun field3(): Field<Long?> = Calls.CALLS.CALLEE_DESC_HASH
    override fun field4(): Field<Int?> = Calls.CALLS.OPCODE
    override fun field5(): Field<Long?> = Calls.CALLS.CALLER_CLASS_SYMBOL_ID
    override fun field6(): Field<ByteArray?> = Calls.CALLS.CALLER_METHOD_OFFSETS
    override fun field7(): Field<Long?> = Calls.CALLS.LOCATION_ID
    override fun component1(): Long? = calleeClassSymbolId
    override fun component2(): Long? = calleeNameSymbolId
    override fun component3(): Long? = calleeDescHash
    override fun component4(): Int? = opcode
    override fun component5(): Long? = callerClassSymbolId
    override fun component6(): ByteArray? = callerMethodOffsets
    override fun component7(): Long? = locationId
    override fun value1(): Long? = calleeClassSymbolId
    override fun value2(): Long? = calleeNameSymbolId
    override fun value3(): Long? = calleeDescHash
    override fun value4(): Int? = opcode
    override fun value5(): Long? = callerClassSymbolId
    override fun value6(): ByteArray? = callerMethodOffsets
    override fun value7(): Long? = locationId

    override fun value1(value: Long?): CallsRecord {
        this.calleeClassSymbolId = value
        return this
    }

    override fun value2(value: Long?): CallsRecord {
        this.calleeNameSymbolId = value
        return this
    }

    override fun value3(value: Long?): CallsRecord {
        this.calleeDescHash = value
        return this
    }

    override fun value4(value: Int?): CallsRecord {
        this.opcode = value
        return this
    }

    override fun value5(value: Long?): CallsRecord {
        this.callerClassSymbolId = value
        return this
    }

    override fun value6(value: ByteArray?): CallsRecord {
        this.callerMethodOffsets = value
        return this
    }

    override fun value7(value: Long?): CallsRecord {
        this.locationId = value
        return this
    }

    override fun values(value1: Long?, value2: Long?, value3: Long?, value4: Int?, value5: Long?, value6: ByteArray?, value7: Long?): CallsRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        this.value5(value5)
        this.value6(value6)
        this.value7(value7)
        return this
    }

    /**
     * Create a detached, initialised CallsRecord
     */
    constructor(calleeClassSymbolId: Long? = null, calleeNameSymbolId: Long? = null, calleeDescHash: Long? = null, opcode: Int? = null, callerClassSymbolId: Long? = null, callerMethodOffsets: ByteArray? = null, locationId: Long? = null): this() {
        this.calleeClassSymbolId = calleeClassSymbolId
        this.calleeNameSymbolId = calleeNameSymbolId
        this.calleeDescHash = calleeDescHash
        this.opcode = opcode
        this.callerClassSymbolId = callerClassSymbolId
        this.callerMethodOffsets = callerMethodOffsets
        this.locationId = locationId
    }
}